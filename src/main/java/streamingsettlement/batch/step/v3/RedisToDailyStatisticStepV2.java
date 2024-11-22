package streamingsettlement.batch.step.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.streaming.domain.entity.Streaming;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * reader,writer 개선 후
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisToDailyStatisticStepV2 {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "daily_stats:";
    private final DataSource dataSource;

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
    public JdbcBatchItemWriter<DailyStatistic> dailyStatisticWriter() {
        return new JdbcBatchItemWriterBuilder<DailyStatistic>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO daily_statistic (
                            streaming_id,
                            streaming_amount,
                            advertisement_amount,
                            streaming_views,
                            advertisement_views,
                            play_time,
                            statistic_date,
                            created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.getStreamingId());
                    ps.setBigDecimal(2, item.getStreamingAmount());
                    ps.setBigDecimal(3, item.getAdvertisementAmount());
                    ps.setLong(4, item.getStreamingViews());
                    ps.setLong(5, item.getAdvertisementViews());
                    ps.setLong(6, item.getPlayTime());
                    ps.setTimestamp(7, Timestamp.valueOf(item.getStatisticDate()));
                    ps.setTimestamp(8, Timestamp.valueOf(item.getCreatedAt()));
                })
                .assertUpdates(false)
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
