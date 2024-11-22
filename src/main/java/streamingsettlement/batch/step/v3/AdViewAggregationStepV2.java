package streamingsettlement.batch.step.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import streamingsettlement.streaming.domain.entity.PlayHistory;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * reader 개선 후
 *
 * 1. 레디스를 활용하여 집계
 * 2. 레디스 파이프라인을 활용한 데이터 삽입
 * 3. ZeroOffsetItemReader 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdViewAggregationStepV2 {

    private static final int AD_INTERVAL_MINUTES = 420;

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "daily_stats:";

    @Bean
    @StepScope
    public ItemReader<PlayHistory> playHistoryReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay().minusMinutes(1);

        JdbcCursorItemReader<PlayHistory> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("""
                    SELECT *
                    FROM play_history
                    WHERE created_at BETWEEN ? AND ?
                    ORDER BY id ASC
                """);

        reader.setPreparedStatementSetter(ps -> {
            ps.setTimestamp(1, Timestamp.valueOf(startOfDay));
            ps.setTimestamp(2, Timestamp.valueOf(endOfDay));
        });

        reader.setRowMapper((rs, rowNum) -> {
            PlayHistory playHistory = new PlayHistory();
            playHistory.setId(rs.getLong("id"));
            playHistory.setUserId(rs.getLong("user_id"));
            playHistory.setStreamingId(rs.getLong("streaming_id"));
            playHistory.setLastPlayTime(rs.getInt("last_play_time"));
            playHistory.setSourceIp(rs.getString("source_ip"));
            playHistory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return playHistory;
        });

        return reader;
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
