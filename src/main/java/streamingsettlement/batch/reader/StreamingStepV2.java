package streamingsettlement.batch.reader;

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
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.adjustment.repository.jpa.StreamingSettlementHistoryJpaRepository;
import streamingsettlement.streaming.repository.jpa.PlayHistoryJpaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * Reader에서 집계 쿼리로 진행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingStepV2 {

    private static final int CHUNK_SIZE = 5000;

    private final StreamingSettlementHistoryJpaRepository streamingSettlementHistoryJpaRepository;
    private final PlayHistoryJpaRepository playHistoryJpaRepository;


    @Bean
    @StepScope
    public RepositoryItemReader<DailySettlementDTO> streamingReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingReader start");
        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atStartOfDay().plusDays(1);

        return new RepositoryItemReaderBuilder<DailySettlementDTO>()
                .name("streamingReader")
                .repository(playHistoryJpaRepository)
                .methodName("findDailySettlements")
                .arguments(Arrays.asList(startOfDay, endOfDay))
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("streamingId", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<DailySettlementDTO, StreamingSettlementHistory> streamingProcessor() {

        return dto -> {
            log.info("Processing DailySettlementDTO: {} ,{}", dto.getStreamingId(),dto.getViewCount());

            BigDecimal unitPrice = calculateStreamingUnitPrice(dto.getViewCount());
            BigDecimal settlementAmount = unitPrice.multiply(BigDecimal.valueOf(dto.getViewCount()))
                    .setScale(0, RoundingMode.DOWN);

            return StreamingSettlementHistory.builder()
                    .streamingId(dto.getStreamingId())
                    .settlementView(dto.getViewCount())
                    .streamingAmount(settlementAmount)
                    .settlementAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<StreamingSettlementHistory> streamingWriter() {
        log.info("streamingWriter start");
        return new RepositoryItemWriterBuilder<StreamingSettlementHistory>()
                .repository(streamingSettlementHistoryJpaRepository)
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
