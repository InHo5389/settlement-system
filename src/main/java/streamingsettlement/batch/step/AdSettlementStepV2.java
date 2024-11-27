package streamingsettlement.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import streamingsettlement.adjustment.domain.entity.AdSettlementHistory;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.repository.jpa.AdSettlementHistoryJpaRepository;
import streamingsettlement.batch.calculator.SettlementCalculator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 병렬 처리 : 멀티 스레드
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdSettlementStepV2 {

    private final AdSettlementHistoryJpaRepository adSettlementHistoryJpaRepository;

    private final DataSource dataSource;

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<DailyStatistic> adSettlementDailyStatisticReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {

        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startDate = targetDate.atStartOfDay();
        LocalDateTime endDate = targetDate.atStartOfDay().plusDays(1);

        JdbcCursorItemReader<DailyStatistic> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("""
                SELECT *
                FROM daily_statistic
                WHERE statistic_date >= ? AND statistic_date < ?
                ORDER BY id ASC
                        """);
        reader.setPreparedStatementSetter(ps -> {
            ps.setTimestamp(1, Timestamp.valueOf(startDate));
            ps.setTimestamp(2, Timestamp.valueOf(endDate));
        });
        reader.setRowMapper((rs, rowNum) ->
                DailyStatistic.builder()
                        .id(rs.getLong("id"))
                        .streamingId(rs.getLong("streaming_id"))
                        .streamingAmount(rs.getBigDecimal("streaming_amount"))
                        .advertisementAmount(rs.getBigDecimal("advertisement_amount"))
                        .streamingViews(rs.getLong("streaming_views"))
                        .advertisementViews(rs.getLong("advertisement_views"))
                        .playTime(rs.getLong("play_time"))
                        .statisticDate(rs.getTimestamp("statistic_date").toLocalDateTime())
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .build()
        );
        return new SynchronizedItemStreamReaderBuilder<DailyStatistic>()
                .delegate(reader)
                .build();
    }

    @Bean
    public ItemProcessor<DailyStatistic, AdSettlementHistory> adSettlementProcessor() {
        return dailyStatistic -> {

            Long adView = dailyStatistic.getAdvertisementViews();
            Long streamingView = dailyStatistic.getStreamingViews();

            BigDecimal adUnitPrice = SettlementCalculator.calculateAdUnitPrice(streamingView);
            BigDecimal adAmount = adUnitPrice.multiply(BigDecimal.valueOf(adView))
                    .setScale(0, RoundingMode.DOWN);


            log.info("광고 정산 처리 완료 - streamingId: {}, adViews: {}, adAmount: {}", dailyStatistic.getStreamingId(), adView, adAmount);
            return AdSettlementHistory.builder()
                    .streamingId(dailyStatistic.getStreamingId())
                    .adViews(adView)
                    .adAmount(adAmount)
                    .settlementAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public JdbcBatchItemWriter<AdSettlementHistory> adSettlementWriter() {
        log.info("streamingSettlementWriter start");

        return new JdbcBatchItemWriterBuilder<AdSettlementHistory>()
                .dataSource(dataSource)
                .sql("""
                INSERT INTO ad_settlement_history (
                    streaming_id,
                    ad_views,
                    ad_amount,
                    settlement_at
                ) VALUES (?, ?, ?, ?)
                """)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.getStreamingId());
                    ps.setLong(2, item.getAdViews());
                    ps.setBigDecimal(3, item.getAdAmount());
                    ps.setTimestamp(4, Timestamp.valueOf(item.getSettlementAt()));
                })
                .assertUpdates(false)
                .build();
    }
}
