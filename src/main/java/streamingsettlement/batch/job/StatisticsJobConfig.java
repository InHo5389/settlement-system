package streamingsettlement.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.batch.listener.JobTimeListener;
import streamingsettlement.batch.reader.DailySettlementDTO;
import streamingsettlement.batch.reader.StreamingStepV3;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsJobConfig {

    private static final int CHUNK_SIZE = 5000;
    private static final int THREAD_COUNT = 5;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobTimeListener jobTimeListener;

    private final StreamingStepV3 streamingStep;

    @Bean
    public Job dailySettlementJob(Step streamingSettlementStep) {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobTimeListener)
                .start(streamingSettlementStep)
                .build();
    }

    @Bean
    @JobScope
    public Step streamingSettlementStep(
            @Value("#{jobParameters[targetDate]}") String targetDate
    ) {
        return new StepBuilder("streamingSettlementStep", jobRepository)
                .<DailySettlementDTO, StreamingSettlementHistory>chunk(CHUNK_SIZE, transactionManager)
                .reader(streamingStep.streamingReader(targetDate))
                .processor(streamingStep.streamingProcessor())
                .writer(streamingStep.streamingWriter())
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_COUNT);
        executor.setMaxPoolSize(THREAD_COUNT);
        executor.setQueueCapacity(CHUNK_SIZE);
        executor.setThreadNamePrefix("streaming-batch-");
        executor.initialize();
        return executor;
    }
}


