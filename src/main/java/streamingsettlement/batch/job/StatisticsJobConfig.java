package streamingsettlement.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.adjustment.repository.jpa.StreamingSettlementHistoryJpaRepository;
import streamingsettlement.batch.listener.JobTimeListener;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.repository.jpa.PlayHistoryJpaRepository;
import streamingsettlement.streaming.repository.jpa.StreamingJpaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsJobConfig {

    private static final int CHUNK_SIZE = 5000;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobTimeListener jobTimeListener;

    private final StreamingJpaRepository streamingRepository;
    private final StreamingSettlementHistoryJpaRepository streamingSettlementHistoryJpaRepository;
    private final PlayHistoryJpaRepository playHistoryJpaRepository;

    @Bean
    public Job dailySettlementJob() {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobTimeListener)
                .start(streamingSettlementStep())
                .build();
    }

    @Bean
    public Step streamingSettlementStep() {
        return new StepBuilder("streamingSettlementStep", jobRepository)
                .<Streaming, StreamingSettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(streamingReader())
                .processor(streamingSettlementProcessor(null))
                .writer(streamingSettlementWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Streaming> streamingReader() {
        log.info("streamingReader start");
        return new RepositoryItemReaderBuilder<Streaming>()
                .name("streamingReader")
                .repository(streamingRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, StreamingSettlementHistory> streamingSettlementProcessor(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingSettlementProcessor start");

        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atStartOfDay().plusDays(1);

        return streaming -> {
            long dailyViews = playHistoryJpaRepository.countByStreamingIdAndCreatedAtBetween(
                    streaming.getId(),
                    startOfDay,
                    endOfDay
            );

            if (dailyViews == 0) {
                System.out.println("dailyViews == 0");
                return null;
            }

            BigDecimal unitPrice = calculateStreamingUnitPrice(dailyViews);
            BigDecimal settlementAmount = unitPrice.multiply(BigDecimal.valueOf(dailyViews))
                    .setScale(0, RoundingMode.DOWN);

            System.out.println("Streaming ID: " + streaming.getId() + " 정산 처리 완료, 금액: " + settlementAmount);

            return StreamingSettlementHistory.builder()
                    .streamingId(streaming.getId())
                    .settlementView(dailyViews)
                    .streamingAmount(settlementAmount)
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

