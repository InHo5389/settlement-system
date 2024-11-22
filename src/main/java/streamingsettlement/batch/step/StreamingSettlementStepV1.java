package streamingsettlement.batch.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.adjustment.repository.jpa.DailyStatisticJpaRepository;
import streamingsettlement.adjustment.repository.jpa.StreamingSettlementHistoryJpaRepository;
import streamingsettlement.batch.calculator.SettlementCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingSettlementStepV1 {

    private static final int CHUNK_SIZE = 5000;

    private final DailyStatisticJpaRepository dailyStatisticJpaRepository;
    private final StreamingSettlementHistoryJpaRepository streamingSettlementHistoryJpaRepository;

    @Bean
    @StepScope
    public RepositoryItemReader<DailyStatistic> streamingSettlementDailyStatisticReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingReader start");

        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startDate = targetDate.atStartOfDay();
        LocalDateTime endDate = targetDate.atStartOfDay().plusDays(1);

        return new RepositoryItemReaderBuilder<DailyStatistic>()
                .name("streamingSettlementReader")
                .repository(dailyStatisticJpaRepository)
                .methodName("findByStatisticDateBetween")
                .arguments(Arrays.asList(startDate, endDate))
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<DailyStatistic, StreamingSettlementHistory> streamingSettlementProcessor() {
        return dailyStatistic -> {

            Long streamingView = dailyStatistic.getStreamingViews();

            BigDecimal streamingUnitPrice = SettlementCalculator.calculateStreamingUnitPrice(streamingView);
            BigDecimal streamingAmount = streamingUnitPrice.multiply(BigDecimal.valueOf(streamingView))
                    .setScale(0, RoundingMode.DOWN);

            log.info("스트리밍 정산 처리 완료 - streamingId: {}, streamingView: {}, streamingAmount: {}",dailyStatistic.getStreamingId(), streamingView, streamingAmount);

            return StreamingSettlementHistory.builder()
                    .streamingId(dailyStatistic.getStreamingId())
                    .streamingViews(streamingView)
                    .streamingAmount(streamingAmount)
                    .settlementAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<StreamingSettlementHistory> streamingSettlementWriter() {
        log.info("streamingSettlementWriter start");

        return new RepositoryItemWriterBuilder<StreamingSettlementHistory>()
                .repository(streamingSettlementHistoryJpaRepository)
                .methodName("save")
                .build();
    }
}
