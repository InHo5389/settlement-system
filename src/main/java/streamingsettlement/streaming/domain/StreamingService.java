package streamingsettlement.streaming.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import streamingsettlement.streaming.common.exception.CustomGlobalException;
import streamingsettlement.streaming.common.exception.ErrorType;
import streamingsettlement.streaming.common.util.RedisKeyUtil;
import streamingsettlement.streaming.domain.dto.StreamingDto;
import streamingsettlement.streaming.domain.dto.StreamingResponse;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.domain.repository.PlayHistoryRepository;
import streamingsettlement.streaming.domain.repository.StreamingRedisRepository;
import streamingsettlement.streaming.domain.repository.StreamingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final StreamingRepository streamingRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final StreamingRedisRepository streamingRedisRepository;

    /**
     * 최초 시청 시작 or 이어보기
     */
    @Transactional
    public StreamingResponse.Watch watch(Long streamingId, StreamingDto.Watch dto) {
        Streaming streaming = streamingRepository.findStreamingById(streamingId)
                .orElseThrow(() -> new CustomGlobalException(ErrorType.NOT_FOUND_STREAMING));

        // 최근 시청 기록 조회 (userId가 있으면 userId로, 없으면 sourceIp로)
        Optional<PlayHistory> optionalPlayHistory = dto.getUserId() != null ?
                playHistoryRepository.findTopByUserIdAndStreamingIdOrderByCreatedAtDesc(dto.getUserId(), streamingId) :
                playHistoryRepository.findTopBySourceIpAndStreamingIdOrderByCreatedAtDesc(dto.getSourceIp(), streamingId);

        // 새로운 시청이면 조회수 증가 (1시간 이전 기록은 새로운 시청으로 간주)
        if (optionalPlayHistory.isEmpty() ||
                optionalPlayHistory.get().getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            String redisKey = RedisKeyUtil.formatViewCountKey(streamingId);
            streamingRedisRepository.incrementStreamingView(redisKey);

            // 새로운 시청 기록 생성
            PlayHistory newPlayHistory = PlayHistory.builder()
                    .userId(dto.getUserId())
                    .streamingId(streamingId)
                    .lastPlayTime(0)  // 시작은 0초부터
                    .sourceIp(dto.getSourceIp())
                    .createdAt(LocalDateTime.now())
                    .build();
            playHistoryRepository.save(newPlayHistory);

            return StreamingResponse.Watch.builder()
                    .playHistoryId(newPlayHistory.getId())
                    .lastPlayTime(0)
                    .build();
        }

        // 이어보기인 경우 기존 시청 기록의 lastPlayTime 반환
        PlayHistory playHistory = optionalPlayHistory.get();
        return StreamingResponse.Watch.builder()
                .playHistoryId(playHistory.getId())
                .lastPlayTime(playHistory.getLastPlayTime())
                .build();
    }


    /**
     * 10초마다 재생시간 업데이트
     */
    @Transactional
    public void updatePlayTime(StreamingDto.UpdatePlayTime dto) {
        PlayHistory playHistory = playHistoryRepository.findById(dto.getPlayHistoryId())
                .orElseThrow(() -> new RuntimeException("시청 기록이 없습니다."));

        // 재생시간이 증가한 경우만 업데이트
        if (dto.getLastPlayTime() > playHistory.getLastPlayTime()) {
            playHistory.updateLastPlayTime(dto.getLastPlayTime());
        }
    }
}
