package streamingsettlement.batch.step.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.repository.jpa.DailyStatisticJpaRepository;
import streamingsettlement.streaming.domain.entity.Streaming;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * reader,writer 개선 전
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisToDailyStatisticStepV1 {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "daily_stats:";
    private final DataSource dataSource;
    private final DailyStatisticJpaRepository dailyStatisticJpaRepository;

    @Bean
    public JdbcCursorItemReader<Streaming> streamingReader() {
        JdbcCursorItemReader<Streaming> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT id,streaming_views FROM streaming");
        reader.setRowMapper((rs, rowNum) -> Streaming.builder()
                .id(rs.getLong("id"))
                .streamingViews(rs.getLong("streaming_views"))
                .build());
        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, DailyStatistic> dailyStatisticProcessor(
            @Value("#{jobParameters[targetDate]}") String date
    ) {

        return streaming -> {
            String key = REDIS_KEY_PREFIX + date + ":" + streaming.getId();
            HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

            // Redis에서 데이터 조회, 없으면 0으로 처리
            Long playTime = Long.parseLong(hashOps.get(key, "playTime") != null ?
                    Objects.requireNonNull(hashOps.get(key, "playTime")) : "0");
            Long adViews = Long.parseLong(hashOps.get(key, "adViews") != null ?
                    Objects.requireNonNull(hashOps.get(key, "adViews")) : "0");
            Long streamingViews = Long.parseLong(hashOps.get(key, "streamingViews") != null ?
                    Objects.requireNonNull(hashOps.get(key, "streamingViews")) : "0");

            // 정산금액 계산 로직 추가 필요
            BigDecimal streamingUnitPrice = calculateStreamingUnitPrice(streaming.getStreamingViews());
            BigDecimal adUnitPrice = calculateAdUnitPrice(streaming.getStreamingViews());

            BigDecimal streamingAmount = streamingUnitPrice.multiply(BigDecimal.valueOf(streamingViews))
                    .setScale(0, RoundingMode.DOWN);

            BigDecimal advertisementAmount = adUnitPrice.multiply(BigDecimal.valueOf(adViews))
                    .setScale(0, RoundingMode.DOWN);

            return DailyStatistic.builder()
                    .streamingId(streaming.getId())
                    .streamingViews(streamingViews)
                    .advertisementViews(adViews)
                    .playTime(playTime)
                    .streamingAmount(streamingAmount)
                    .advertisementAmount(advertisementAmount)
                    .statisticDate(LocalDate.parse(date).atStartOfDay())
                    .createdAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<DailyStatistic> dailyStatisticWriter() {
        log.info("streamingSettlementWriter start");

        return new RepositoryItemWriterBuilder<DailyStatistic>()
                .repository(dailyStatisticJpaRepository)
                .methodName("save")
                .build();
    }

    private BigDecimal calculateStreamingUnitPrice(long views) {
        if (views >= 1_000_000) return BigDecimal.valueOf(1.5);
        if (views >= 500_000) return BigDecimal.valueOf(1.3);
        if (views >= 100_000) return BigDecimal.valueOf(1.1);
        return BigDecimal.valueOf(1.0);
    }

    private BigDecimal calculateAdUnitPrice(long views) {
        if (views >= 1_000_000) return BigDecimal.valueOf(20);
        if (views >= 500_000) return BigDecimal.valueOf(15);
        if (views >= 100_000) return BigDecimal.valueOf(12);
        return BigDecimal.valueOf(10);
    }
}
