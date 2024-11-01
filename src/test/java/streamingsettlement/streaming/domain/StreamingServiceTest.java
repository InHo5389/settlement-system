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
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;

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
        assertThat(savedStreaming.getStreamingViews()).isEqualTo(1);
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
        assertThat(savedStreaming.getStreamingViews()).isEqualTo(streaming.getStreamingViews());
    }

    @Test
    @DisplayName("광고는 7분에 하나 등록되는데 14분이 지났을때 광고 조회수 2개가 올라가야한다.")
    void shouldIncreaseAdViewCountByTwoAfter14Minutes() {
        //given
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .totalViews(50)
                .build());
        int lastPlayTime = 0;
        PlayHistory playHistory = PlayHistory.builder()
                .streamingId(streaming.getId())
                .userId(null)
                .lastPlayTime(lastPlayTime)
                .build();
        streamingRepository.save(playHistory);
        StreamingAdvertisement firstAd = streamingRepository.save(StreamingAdvertisement.builder()
                .streamingId(streaming.getId())
                .position(420)
                .build());

        StreamingAdvertisement secondAd = streamingRepository.save(StreamingAdvertisement.builder()
                .streamingId(streaming.getId())
                .position(840)
                .build());



        StreamingDto.UpdatePlayTime dto = new StreamingDto.UpdatePlayTime(playHistory.getId(), 840);
        //when
        streamingService.updatePlayTimeAndAdPosition(dto);
        Streaming savedStreaming = streamingRepository.findStreamingById(streaming.getId()).get();
        //then
        Assertions.assertThat(savedStreaming.getAdViews()).isEqualTo(2);
    }

    @Test
    @DisplayName("광고는 7분에 하나 등록되는데 13분이 지났을때 광고 조회수 1개가 올라가야한다.")
    void shouldIncreaseAdViewCountByOneAfter13Minutes() {
        //given
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .totalViews(50)
                .build());
        int lastPlayTime = 0;
        PlayHistory playHistory = PlayHistory.builder()
                .streamingId(streaming.getId())
                .userId(null)
                .lastPlayTime(lastPlayTime)
                .build();
        streamingRepository.save(playHistory);
        StreamingAdvertisement firstAd = streamingRepository.save(StreamingAdvertisement.builder()
                .streamingId(streaming.getId())
                .position(420)
                .build());

        StreamingAdvertisement secondAd = streamingRepository.save(StreamingAdvertisement.builder()
                .streamingId(streaming.getId())
                .position(840)
                .build());



        StreamingDto.UpdatePlayTime dto = new StreamingDto.UpdatePlayTime(playHistory.getId(), 780);
        //when
        streamingService.updatePlayTimeAndAdPosition(dto);
        Streaming savedStreaming = streamingRepository.findStreamingById(streaming.getId()).get();
        //then
        Assertions.assertThat(savedStreaming.getAdViews()).isEqualTo(1);
    }
    @Test
    @DisplayName("광고는 7분에 하나 등록되는데 6분 50초가 지났을때 광고 조회수 오르지 않아야 한다.")
    void shouldIncreaseAdViewCountByOneAfter6Minutes50Sec() {
        //given
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .totalViews(50)
                .build());
        int lastPlayTime = 0;
        PlayHistory playHistory = PlayHistory.builder()
                .streamingId(streaming.getId())
                .userId(null)
                .lastPlayTime(lastPlayTime)
                .build();
        streamingRepository.save(playHistory);
        StreamingAdvertisement firstAd = streamingRepository.save(StreamingAdvertisement.builder()
                .streamingId(streaming.getId())
                .position(420)
                .build());

        StreamingAdvertisement secondAd = streamingRepository.save(StreamingAdvertisement.builder()
                .streamingId(streaming.getId())
                .position(840)
                .build());



        StreamingDto.UpdatePlayTime dto = new StreamingDto.UpdatePlayTime(playHistory.getId(), 410);
        //when
        streamingService.updatePlayTimeAndAdPosition(dto);
        Streaming savedStreaming = streamingRepository.findStreamingById(streaming.getId()).get();
        //then
        Assertions.assertThat(savedStreaming.getAdViews()).isEqualTo(0);
    }
}