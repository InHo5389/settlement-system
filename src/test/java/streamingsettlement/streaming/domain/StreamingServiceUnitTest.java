//package streamingsettlement.streaming.domain;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import streamingsettlement.streaming.domain.dto.StreamingDto;
//import streamingsettlement.streaming.domain.entity.PlayHistory;
//import streamingsettlement.streaming.domain.entity.Streaming;
//import streamingsettlement.streaming.domain.repository.StreamingRedisRepository;
//import streamingsettlement.streaming.domain.repository.StreamingRepository;
//
//import java.util.Optional;
//
//import static org.mockito.BDDMockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class StreamingServiceUnitTest {
//
//    @InjectMocks
//    private StreamingService streamingService;
//
//    @Mock
//    private StreamingRepository streamingRepository;
//
//    @Mock
//    private StreamingRedisRepository streamingRedisRepository;
//
//    @Test
//    @DisplayName("광고는 영상 7분마다 붙고 전 마지막 시청 내역이 410초일때 두번째 시청할때 900초를 봤으면" +
//            "streamingRedisRepository는 2번호출 된다.")
//    void saveAdViewsToRedis() {
//        //given
//        Long playHistoryId = 1L;
//        Long streamingId = 1L;
//        Integer lastPlayTime = 900;
//        StreamingDto.UpdatePlayTime dto = new StreamingDto.UpdatePlayTime(playHistoryId, lastPlayTime);
//
//        PlayHistory playHistory = PlayHistory.builder()
//                .streamingId(streamingId)
//                .lastAdPlayTime(410)
//                .build();
//
//        Streaming streaming = Streaming.builder().build();
//        given(playHistory.findPlayHistoryById(playHistoryId))
//                .willReturn(Optional.of(playHistory));
//        //when
//        streamingService.saveAdViewsToRedis(dto);
//        //then
//        verify(streamingRedisRepository, times(2)).incrementAdView(anyString());
//    }
//}
