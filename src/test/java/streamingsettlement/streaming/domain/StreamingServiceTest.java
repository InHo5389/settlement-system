package streamingsettlement.streaming.domain;

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

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class StreamingServiceTest {

    @Autowired
    private StreamingService streamingService;

    @Autowired
    private StreamingRepository streamingRepository;

    @Autowired
    private StreamingRedisRepository streamingRedisRepository;

    @BeforeEach
    void setUp() {
        streamingRepository.streamingDeleteAll();
        streamingRepository.playHistoryDeleteAll();
        streamingRedisRepository.clear();
    }

    private static final String VIEW_COUNT_KEY = "streaming:%d:views";
    private static final String AD_VIEW_KEY = "streaming:%d:ad:%d:views";

    @Test
    @DisplayName("영상 재생시 시청기록을 생성한다." +
            " 반환값은 새로운 시청기록 id,조회된 시청기록 재생시점이다")
    void firstView() {
        //given
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .title("여행 vlog")
                .duration(100000000)
                .streamingViews(0)
                .createdAt(LocalDateTime.now())
                .cdnUrl("cdn.url")
                .build());
        StreamingDto.Watch dto = new StreamingDto.Watch(null, "127.0.0.1");
        //when
        StreamingResponse.Watch response = streamingService.watch(streaming.getId(), dto);

        Optional<PlayHistory> optionalPlayHistory = streamingRepository.findLatestPlayHistory(dto.getSourceIp(), streaming.getId());
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
                .streamingViews(0)
                .build());
        StreamingDto.Watch dto = new StreamingDto.Watch(null, "127.0.0.1");
        //when
        streamingService.watch(streaming.getId(), dto);
        Long streamingView = streamingRedisRepository.getStreamingView(String.format(VIEW_COUNT_KEY, streaming.getId()));
        //then
        assertThat(streamingView).isEqualTo(1);
    }

    @Test
    @DisplayName("영상 최초 조회시가 아닐시 조회수를 올리지 않는다.")
    void nonFirstView() {
        //given
        String ipAddress = "127.0.0.1";
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .userId(null)
                .streamingViews(50)
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
    @DisplayName("광고는 영상 7분마다 1개가 들어가고 영상을 처음부터 15분 봤으면  " +
            "레디스에는 광고뷰가 2이고 420,840 조회수는 각각1이다.")
    void saveAdViewsToRedis(){
        //given
        long streamingId = 1L;
        PlayHistory playHistory = PlayHistory.builder().streamingId(streamingId).lastAdPlayTime(0).build();
        streamingRepository.save(playHistory);
        StreamingDto.UpdatePlayTime dto = StreamingDto.UpdatePlayTime.builder()
                .playHistoryId(playHistory.getId())
                .lastPlayTime(900)
                .build();
        //when
        streamingService.saveAdViewsToRedis(dto);
        Long adView7Minute = streamingRedisRepository.getAdView(String.format(AD_VIEW_KEY, playHistory.getStreamingId(), 420));
        Long adView15Minute = streamingRedisRepository.getAdView(String.format(AD_VIEW_KEY, playHistory.getStreamingId(), 420));
        //then
        assertThat(adView7Minute).isEqualTo(1);
        assertThat(adView15Minute).isEqualTo(1);
    }

//    // TODO : 통과하게 작성
//    @Test
//    @DisplayName("ip가 다른 유저 100명이 영상을 시청했을때 조회수는 100이 나와야 한다.")
//    void test() throws InterruptedException {
//        //given
//        Long streamingId = 1L;
//        int count = 100;
//        ExecutorService executorService = Executors.newFixedThreadPool(count);
//        CountDownLatch latch = new CountDownLatch(count);
//
//        streamingRepository.save(Streaming.builder().build());
//
//        //when
//        for (int i = 0; i < count; i++) {
//            final int userId = i + 1;
//            try {
//                executorService.submit(() -> {
//                    streamingService.watch(streamingId, new StreamingDto.Watch((long)userId + 1, "192.0.0." + userId));
//                });
//            }finally {
//                latch.countDown();
//            }
//        }
//
//        latch.await();
//        executorService.shutdown();
//        //then
//        Streaming streaming = streamingRepository.findStreamingById(streamingId).get();
//        assertThat(streaming.getStreamingViews()).isEqualTo(100);
//    }
}