package streamingsettlement.streaming.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import streamingsettlement.streaming.common.exception.CustomGlobalException;
import streamingsettlement.streaming.common.exception.ErrorType;
import streamingsettlement.streaming.domain.dto.StreamingDto;
import streamingsettlement.streaming.domain.dto.StreamingResponse;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final StreamingRepository streamingRepository;

    /**
     * ip주소,스트리밍 id로 시청 히스토리에서 찾고 없으면 최초 재생으로 조회수 증가
     * 새로운 시청 기록 생성할때 그 전 마지막 재생 시점으로 생성 어차피 폴링으로 업데이트 시켜줄거기 때문
     */
    @Transactional
    public StreamingResponse.Watch watch(Long streamingId, StreamingDto.Watch dto) {
        Streaming streaming = streamingRepository.findStreamingById(streamingId)
                .orElseThrow(() -> new CustomGlobalException(ErrorType.NOT_FOUND_STREAMING));

        Optional<PlayHistory> optionalPlayHistory = streamingRepository.findFirstBySourceIpAndStreamingIdOrderByViewedAtDesc(dto.getSourceIp(), streamingId);
        if (optionalPlayHistory.isEmpty()) {
            streaming.increaseView();
        }

        PlayHistory playHistory = PlayHistory.create(dto.getUserId(), streamingId, optionalPlayHistory, dto.getSourceIp());
        streamingRepository.save(playHistory);

        return StreamingResponse.Watch.builder()
                .playHistoryId(playHistory.getId())
                .lastPlayTime(playHistory.getLastPlayTime())
                .build();
    }

    @Transactional
    public void updatePlayTime(StreamingDto.UpdatePlayTime dto) {
        PlayHistory playHistory = streamingRepository.findPlayHistoryById(dto.getPlayHistoryId())
                .orElseThrow(() -> new RuntimeException("시청 기록이 없습니다."));
        playHistory.updateLastPlayTime(dto.getLastPlayTime());
    }
}
