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
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.adjustment.repository.jpa.StreamingSettlementHistoryJpaRepository;
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
public class StreamingSettlementStepV2 {

    private final DataSource dataSource;
    private final StreamingSettlementHistoryJpaRepository streamingSettlementHistoryJpaRepository;

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<DailyStatistic> streamingSettlementDailyStatisticReader(
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
    public ItemProcessor<DailyStatistic, StreamingSettlementHistory> streamingSettlementProcessor() {
        return dailyStatistic -> {

            Long streamingView = dailyStatistic.getStreamingViews();

            BigDecimal streamingUnitPrice = SettlementCalculator.calculateStreamingUnitPrice(streamingView);
            BigDecimal streamingAmount = streamingUnitPrice.multiply(BigDecimal.valueOf(streamingView))
                    .setScale(0, RoundingMode.DOWN);

            log.info("스트리밍 정산 처리 완료 - streamingId: {}, streamingView: {}, streamingAmount: {}", dailyStatistic.getStreamingId(), streamingView, streamingAmount);

            return StreamingSettlementHistory.builder()
                    .streamingId(dailyStatistic.getStreamingId())
                    .streamingViews(streamingView)
                    .streamingAmount(streamingAmount)
                    .settlementAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public JdbcBatchItemWriter<StreamingSettlementHistory> streamingSettlementWriter() {
        log.info("streamingSettlementWriter start");

        return new JdbcBatchItemWriterBuilder<StreamingSettlementHistory>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO streaming_settlement_history (
                            streaming_id,
                            streaming_views,
                            streaming_amount,
                            settlement_at
                        ) VALUES ( ?, ?, ?, ?)
                        """)
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, item.getStreamingId());
                    ps.setLong(2, item.getStreamingViews());
                    ps.setBigDecimal(3, item.getStreamingAmount());
                    ps.setTimestamp(4, Timestamp.valueOf(item.getSettlementAt()));
                })
                .assertUpdates(false)
                .build();
    }
}
