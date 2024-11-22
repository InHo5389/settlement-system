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
import streamingsettlement.batch.step.StreamingSettlementStepV1;
import streamingsettlement.batch.step.v3.AdViewAggregationStepV2;
import streamingsettlement.batch.step.v3.RedisToDailyStatisticStepV2;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;

/**
 * 레디스를 활용한 집계
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsJobConfigV3 {

    private static final int CHUNK_SIZE = 5000;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobTimeListener jobTimeListener;

    private final AdViewAggregationStepV2 adViewAggregationStep;
    private final RedisToDailyStatisticStepV2 redisToDailyStatisticStep;
    private final AdSettlementStepV1 adSettlementStep;
    private final StreamingSettlementStepV1 streamingSettlementStep;

    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .listener(jobTimeListener)
                .start(aggregateDataStep(null))
                .next(processDataStep(null))
                .next(adSettlementStep(null))
                .next(dailyStatisticStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step aggregateDataStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("aggregateDataStep", jobRepository)
                .<PlayHistory, PlayHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(adViewAggregationStep.playHistoryReader(targetDate))
                .writer(adViewAggregationStep.redisWriter(targetDate))
                .build();
    }

    @Bean
    @JobScope
    public Step processDataStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("processDataStep", jobRepository)
                .<Streaming, DailyStatistic>chunk(CHUNK_SIZE, transactionManager)
                .reader(redisToDailyStatisticStep.streamingReader())
                .processor(redisToDailyStatisticStep.dailyStatisticProcessor(targetDate))
                .writer(redisToDailyStatisticStep.dailyStatisticWriter())
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

    @Bean
    @JobScope
    public Step dailyStatisticStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("dailyStatisticStep", jobRepository)
                .<DailyStatistic, StreamingSettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(streamingSettlementStep.streamingSettlementDailyStatisticReader(targetDate))
                .processor(streamingSettlementStep.streamingSettlementProcessor())
                .writer(streamingSettlementStep.streamingSettlementWriter())
                .build();
    }
}
