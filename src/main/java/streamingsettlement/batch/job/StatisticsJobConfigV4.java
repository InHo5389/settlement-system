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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import streamingsettlement.adjustment.domain.entity.AdSettlementHistory;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.batch.listener.JobTimeListener;
import streamingsettlement.batch.step.AdSettlementStepV2;
import streamingsettlement.batch.step.StreamingSettlementStepV2;
import streamingsettlement.batch.step.v3.AdViewAggregationStepV3;
import streamingsettlement.batch.step.v3.RedisToDailyStatisticStepV3;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;

/**
 * 병렬 처리 : 멀티 스레드
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsJobConfigV4 {

    private static final int CHUNK_SIZE = 5000;
    private static final int THREAD_COUNT = 5;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobTimeListener jobTimeListener;

    private final AdViewAggregationStepV3 adViewAggregationStep;
    private final RedisToDailyStatisticStepV3 redisToDailyStatisticStep;
    private final AdSettlementStepV2 adSettlementStep;
    private final StreamingSettlementStepV2 streamingSettlementStep;

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
                .taskExecutor(taskExecutor())
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
                .taskExecutor(taskExecutor())
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
                .taskExecutor(taskExecutor())
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
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_COUNT);
        executor.setMaxPoolSize(THREAD_COUNT);
        executor.setQueueCapacity(CHUNK_SIZE);
        executor.setThreadNamePrefix("batch-");
        executor.initialize();
        return executor;
    }
}
