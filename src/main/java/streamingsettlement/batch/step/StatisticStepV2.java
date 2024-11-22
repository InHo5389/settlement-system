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
import streamingsettlement.batch.step.dto.DailyStatisticDto;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;
import streamingsettlement.streaming.repository.jpa.PlayHistoryJpaRepository;
import streamingsettlement.streaming.repository.jpa.StreamingAdvertisementJpaRepository;
import streamingsettlement.streaming.repository.jpa.StreamingJpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Reader에서 Group by + sum 쿼리
 * <p>
 * 인덱스 걸 시 8159초
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticStepV2 {

    private static final int CHUNK_SIZE = 5000;
    private static final int AD_INTERVAL_SECONDS = 420;

    private final DailyStatisticJpaRepository dailyStatisticJpaRepository;
    private final PlayHistoryJpaRepository playHistoryJpaRepository;

    @Bean
    @StepScope
    public RepositoryItemReader<DailyStatisticDto> playHistoryAggregationReader(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingReader start");

        LocalDate targetDate = LocalDate.parse(date);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atStartOfDay().plusDays(1);

        return new RepositoryItemReaderBuilder<DailyStatisticDto>()
                .name("dailyStatisticReader")
                .repository(playHistoryJpaRepository)
                .methodName("findDailyPlayHistoryAggregation")
                .arguments(Arrays.asList(startOfDay, endOfDay))
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of("streamingId", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<DailyStatisticDto, DailyStatistic> dailyStatisticProcessor(
            @Value("#{jobParameters[targetDate]}") String date
    ) {
        log.info("streamingSettlementProcessor start");

        LocalDateTime targetDate = LocalDate.parse(date).atStartOfDay();

        return dto -> DailyStatistic.builder()
                .streamingId(dto.getStreamingId())
                .streamingViews(dto.getStreamingView())
                .advertisementViews(dto.getTotalPlayTime() / AD_INTERVAL_SECONDS)
                .playTime(dto.getTotalPlayTime())
                .statisticDate(targetDate)
                .createdAt(LocalDateTime.now())
                .build();
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
