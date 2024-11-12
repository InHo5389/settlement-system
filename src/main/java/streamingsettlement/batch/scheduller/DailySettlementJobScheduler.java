package streamingsettlement.batch.scheduller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySettlementJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job dailySettlementJob;

    @Scheduled(cron = "0 56 21 * * *")  // 매일 오후 5시 22분
    public void runJob() {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addString("targetDate", "2024-03-01")
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(dailySettlementJob, parameters);
        } catch (Exception e) {
            log.error("Daily settlement job failed", e);
        }
    }
}
