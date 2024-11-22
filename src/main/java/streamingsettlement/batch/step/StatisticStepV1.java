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
import streamingsettlement.adjustment.repository.jpa.DailyStatisticJpaRepository;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;
import streamingsettlement.streaming.repository.jpa.PlayHistoryJpaRepository;
import streamingsettlement.streaming.repository.jpa.StreamingAdvertisementJpaRepository;
import streamingsettlement.streaming.repository.jpa.StreamingJpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RepositoryItemReader 활용 배치
 * 일일 통계
 * 스트리밍 정산,광고 정산,스트리밍에 대한 총 재생 시간 저장.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticStepV1 {

    private static final int CHUNK_SIZE = 5000;
    private static final int AD_INTERVAL_SECONDS = 420;

    private final StreamingJpaRepository streamingRepository;
    private final PlayHistoryJpaRepository playHistoryJpaRepository;
    private final DailyStatisticJpaRepository dailyStatisticJpaRepository;

    @Bean
    public RepositoryItemReader<Streaming> dailyStatisticReader() {
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
    public ItemProcessor<Streaming, DailyStatistic> dailyStatisticProcessor(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingSettlementProcessor start");

        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atStartOfDay().plusDays(1);

        return streaming -> {
            List<PlayHistory> playHistories = playHistoryJpaRepository.findByStreamingIdAndCreatedAtBetween(streaming.getId(), startOfDay, endOfDay);

            long streamingViews = playHistories.size();

            long totalPlayTime = playHistories
                    .stream()
                    .mapToLong(PlayHistory::getLastPlayTime)
                    .sum();

            long advertisementViews = totalPlayTime / AD_INTERVAL_SECONDS;

            log.info("일일 통계 처리 완료 - streamingId: {}, views: {}, adViews: {}, totalPlayTime: {}",
                    streaming.getId(), streamingViews, advertisementViews, totalPlayTime);

            return DailyStatistic.builder()
                    .streamingId(streaming.getId())
                    .streamingViews(streamingViews)
                    .advertisementViews(advertisementViews)
                    .playTime(totalPlayTime)
                    .statisticDate(startOfDay)
                    .createdAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<DailyStatistic> dailyStatisticWriter() {
        log.info("streamingSettlementWriter start");

        return new RepositoryItemWriterBuilder<DailyStatistic>()
                .repository(dailyStatisticJpaRepository)
                .methodName("save")
                .build();
    }
}
