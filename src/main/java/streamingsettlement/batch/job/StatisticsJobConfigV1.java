package streamingsettlement.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import streamingsettlement.adjustment.domain.entity.AdSettlementHistory;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.batch.listener.JobTimeListener;
import streamingsettlement.batch.step.AdSettlementStepV1;
import streamingsettlement.batch.step.StatisticStepV1;
import streamingsettlement.batch.step.StreamingSettlementStepV1;
import streamingsettlement.streaming.domain.entity.Streaming;

/**
 * StatisticStepV1
 * 인덱스 적용
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsJobConfigV1 {

    private static final int CHUNK_SIZE = 5000;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobTimeListener jobTimeListener;

    private final StatisticStepV1 statisticStep;
    private final StreamingSettlementStepV1 streamingSettlementStep;
    private final AdSettlementStepV1 adSettlementStep;

    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .listener(jobTimeListener)
                .start(dailyStatisticStep(null))
                .next(streamingSettlementStep(null))
                .next(adSettlementStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step dailyStatisticStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("dailyStatisticStep", jobRepository)
                .<Streaming, DailyStatistic>chunk(CHUNK_SIZE, transactionManager)
                .reader(statisticStep.dailyStatisticReader())
                .processor(statisticStep.dailyStatisticProcessor(targetDate))
                .writer(statisticStep.dailyStatisticWriter())
                .build();
    }

    @Bean
    @JobScope
    public Step streamingSettlementStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("streamingSettlementStep", jobRepository)
                .<DailyStatistic, StreamingSettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(streamingSettlementStep.streamingSettlementDailyStatisticReader(targetDate))
                .processor(streamingSettlementStep.streamingSettlementProcessor())
                .writer(streamingSettlementStep.streamingSettlementWriter())
                .build();
    }

    @Bean
    @JobScope
    public Step adSettlementStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("streamingSettlementStep", jobRepository)
                .<DailyStatistic, AdSettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(adSettlementStep.adSettlementDailyStatisticReader(targetDate))
                .processor(adSettlementStep.adSettlementProcessor())
                .writer(adSettlementStep.adSettlementWriter())
                .build();
    }
}
