package streamingsettlement.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JobTimeListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job {} 시작: {}",
                jobExecution.getJobInstance().getJobName(),
                LocalDateTime.now());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        long executionTimeSeconds = java.time.Duration.between(startTime, endTime).getSeconds();

        log.info("Job {} 종료 시간: {}",
                jobExecution.getJobInstance().getJobName(),
                endTime);
        log.info("총 소요 시간: {}초", executionTimeSeconds);
        log.info("처리된 아이템 수: {}",
                jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getWriteCount)
                        .sum());
        log.info("Job 상태: {}", jobExecution.getStatus());
    }
}
