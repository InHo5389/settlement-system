package streamingsettlement.streaming.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import streamingsettlement.streaming.domain.dto.StreamingDto;
import streamingsettlement.streaming.domain.dto.StreamingResponse;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class StreamingServiceTest {

    @Autowired
    private StreamingService streamingService;

    @Autowired
    private StreamingRepository streamingRepository;

    @BeforeEach
    void setUp() {
        streamingRepository.streamingDeleteAll();
        streamingRepository.playHistoryDeleteAll();
    }

    @Test
    @DisplayName("영상 재생시 시청기록을 생성한다." +
            " 반환값은 새로운 시청기록 id,조회된 시청기록 재생시점이다")
    void firstView() {
        //given
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .title("여행 vlog")
                .duration(100000000)
                .totalViews(0)
                .createdAt(LocalDateTime.now())
                .cdnUrl("cdn.url")
                .build());
        StreamingDto.Watch dto = new StreamingDto.Watch(null, "127.0.0.1");
        //when
        StreamingResponse.Watch response = streamingService.watch(streaming.getId(), dto);

        Optional<PlayHistory> optionalPlayHistory = streamingRepository.findFirstBySourceIpAndStreamingIdOrderByViewedAtDesc(dto.getSourceIp(), streaming.getId());
        PlayHistory playHistory = optionalPlayHistory.get();
        //then
        assertThat(response).extracting("playHistoryId", "lastPlayTime")
                .containsExactlyInAnyOrder(playHistory.getId(), 0);
    }

    @Test
    @DisplayName("영상 최초 조회시 조회수를 1 올린다.")
    void firstView_plusViewCount() {
        //given
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .totalViews(0)
                .build());
        StreamingDto.Watch dto = new StreamingDto.Watch(null, "127.0.0.1");
        //when
        streamingService.watch(streaming.getId(), dto);
        Streaming savedStreaming = streamingRepository.findStreamingById(streaming.getId()).get();
        //then
        assertThat(savedStreaming.getTotalViews()).isEqualTo(1);
    }

    @Test
    @DisplayName("영상 최초 조회시가 아닐시 조회수를 올리지 않는다.")
    void nonFirstView() {
        //given
        String ipAddress = "127.0.0.1";
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .totalViews(50)
                .build());
        StreamingDto.Watch dto = new StreamingDto.Watch(null, ipAddress);

        streamingRepository.save(PlayHistory.builder()
                .userId(null)
                .streamingId(streaming.getId())
                .sourceIp(ipAddress)
                .build());

        //when
        streamingService.watch(streaming.getId(), dto);
        Streaming savedStreaming = streamingRepository.findStreamingById(streaming.getId()).get();
        //then
        assertThat(savedStreaming.getTotalViews()).isEqualTo(streaming.getTotalViews());
    }
}