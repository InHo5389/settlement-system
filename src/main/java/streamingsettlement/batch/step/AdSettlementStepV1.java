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
import streamingsettlement.adjustment.domain.entity.AdSettlementHistory;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.repository.jpa.AdSettlementHistoryJpaRepository;
import streamingsettlement.adjustment.repository.jpa.DailyStatisticJpaRepository;
import streamingsettlement.batch.calculator.SettlementCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdSettlementStepV1 {

    private static final int CHUNK_SIZE = 5000;

    private final DailyStatisticJpaRepository dailyStatisticJpaRepository;
    private final AdSettlementHistoryJpaRepository adSettlementHistoryJpaRepository;

    @Bean
    @StepScope
    public RepositoryItemReader<DailyStatistic> adSettlementDailyStatisticReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingReader start");

        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startDate = targetDate.atStartOfDay();
        LocalDateTime endDate = targetDate.atStartOfDay().plusDays(1);

        return new RepositoryItemReaderBuilder<DailyStatistic>()
                .name("adSettlementReader")
                .repository(dailyStatisticJpaRepository)
                .methodName("findByStatisticDateBetween")
                .arguments(Arrays.asList(startDate, endDate))
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
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


            log.info("광고 정산 처리 완료 - streamingId: {}, adViews: {}, adAmount: {}",dailyStatistic.getStreamingId(), adView, adAmount);
            return AdSettlementHistory.builder()
                    .streamingId(dailyStatistic.getStreamingId())
                    .adViews(adView)
                    .adAmount(adAmount)
                    .settlementAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<AdSettlementHistory> adSettlementWriter() {
        log.info("streamingSettlementWriter start");

        return new RepositoryItemWriterBuilder<AdSettlementHistory>()
                .repository(adSettlementHistoryJpaRepository)
                .methodName("save")
                .build();
    }
}
