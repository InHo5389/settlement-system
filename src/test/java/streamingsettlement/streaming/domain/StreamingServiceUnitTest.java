package streamingsettlement.streaming.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import streamingsettlement.streaming.domain.dto.StreamingDto;
import streamingsettlement.streaming.domain.entity.PlayHistory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class StreamingServiceUnitTest {

    @InjectMocks
    private StreamingService streamingService;

    @Mock
    private StreamingRepository streamingRepository;

    @Test
    @DisplayName("마지막 재생 시점을 갱신한다.")
    void updatePlayTime(){
        //given
        Long playHistoryId = 1L;
        Integer lastPlayTime = 5000;
        StreamingDto.UpdatePlayTime dto = new StreamingDto.UpdatePlayTime(playHistoryId, lastPlayTime);

        PlayHistory playHistory = PlayHistory.builder().lastPlayTime(0).build();
        given(streamingRepository.findPlayHistoryById(playHistoryId))
                .willReturn(Optional.of(playHistory));
        //when
        streamingService.updatePlayTimeAndAdPosition(dto);
        //then
        assertThat(playHistory.getLastPlayTime()).isEqualTo(lastPlayTime);
    }
}
