package streamingsettlement.batch.step.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.repository.jpa.PlayHistoryJpaRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * reader 개선 전 RepositoryItemReader 사용
 *
 * 1. 레디스를 활용하여 집계
 * 2. 레디스 파이프라인을 활용한 데이터 삽입
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdViewAggregationStepV1 {

    private static final int AD_INTERVAL_MINUTES = 420;
    private static final int CHUNK_SIZE = 5000;

    private final PlayHistoryJpaRepository playHistoryJpaRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "daily_stats:";

    @Bean
    @StepScope
    public RepositoryItemReader<PlayHistory> playHistoryReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay().minusMinutes(1);

        return new RepositoryItemReaderBuilder<PlayHistory>()
                .name("streamingReader")
                .repository(playHistoryJpaRepository)
                .methodName("findByCreatedAtBetween")
                .arguments(Arrays.asList(startOfDay, endOfDay))
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<PlayHistory> redisWriter(
            @Value("#{jobParameters[targetDate]}") String date) {

        return items -> {
            log.info("redisWriter 처리 시작");
            Map<String, Map<String, Long>> batchUpdates = new HashMap<>();

            // 1. 메모리에서 모든 데이터 집계
            for (PlayHistory playHistory : items) {
                String key = REDIS_KEY_PREFIX + date + ":" + playHistory.getStreamingId();
                batchUpdates.computeIfAbsent(key, k -> new HashMap<>() {{
                    put("playTime", 0L);
                    put("adViews", 0L);
                    put("streamingViews", 0L);
                }});

                Map<String, Long> values = batchUpdates.get(key);
                values.put("playTime", values.get("playTime") + playHistory.getLastPlayTime());
                values.put("adViews", values.get("adViews") + (playHistory.getLastPlayTime() / AD_INTERVAL_MINUTES));
                values.put("streamingViews", values.get("streamingViews") + 1);
            }

            // 2. Redis Pipeline 사용하여 한번에 업데이트
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                batchUpdates.forEach((key, values) -> {
                    stringRedisConn.hIncrBy(key, "playTime", values.get("playTime"));
                    stringRedisConn.hIncrBy(key, "adViews", values.get("adViews"));
                    stringRedisConn.hIncrBy(key, "streamingViews", values.get("streamingViews"));
                    stringRedisConn.expire(key, Duration.ofDays(1).toSeconds());
                });
                return null;
            });
        };
    }
}
