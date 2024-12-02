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
        // 스트리밍 조회
        Streaming streaming = streamingRepository.findStreamingById(streamingId)
                .orElseThrow(() -> new CustomGlobalException(ErrorType.NOT_FOUND_STREAMING));

        // creator 여부 체크
        boolean isCreator = streaming.isCreator(dto.getUserId());

        // 어뷰징 체크: creator가 아니고 30초 이내 재시청인 경우
        if (!isCreator && !isValidAccess(streamingId, dto)) {
            // 어뷰징으로 판단되면 이전 재생 시점만 반환
            int lastPlayTime = getLastPlayTime(dto.getUserId(), streamingId, dto.getSourceIp());
            Long lastHistoryId = getRecentPlayHistory(dto.getUserId(), streamingId, dto.getSourceIp())
                    .map(PlayHistory::getId)
                    .orElse(null);

            return StreamingResponse.Watch.builder()
                    .playHistoryId(lastHistoryId)
                    .lastPlayTime(lastPlayTime)
                    .build();
        }

        // 정상적인 시청이고 creator가 아닌 경우 조회수 증가
        if (!isCreator) {
            incrementViewCount(streamingId);
            setAccessRecord(streamingId, dto);
        }

        // 유효한 시청만 PlayHistory 생성
        PlayHistory playHistory = createPlayHistory(dto, streamingId, isCreator);
        int lastPlayTime = getLastPlayTime(dto.getUserId(), streamingId, dto.getSourceIp());

        return StreamingResponse.Watch.builder()
                .playHistoryId(playHistory.getId())
                .lastPlayTime(lastPlayTime)
                .build();
    }

    /**
     * 10초마다 재생시간 업데이트
     */
    @Transactional
    public void updatePlayTime(StreamingDto.UpdatePlayTime dto) {
        PlayHistory playHistory = playHistoryRepository.findById(dto.getPlayHistoryId())
                .orElseThrow(() -> new CustomGlobalException(ErrorType.NOT_FOUND_HISTORY));

        // 재생시간이 증가한 경우만 업데이트
        if (dto.getLastPlayTime() > playHistory.getLastPlayTime()) {
            playHistory.updateLastPlayTime(dto.getLastPlayTime());
        }
    }

    private boolean isValidAccess(Long streamingId, StreamingDto.Watch dto) {
        String key = getAccessKey(streamingId, dto);
        return streamingRedisRepository.getStreamingView(key) == null;
    }

    private void setAccessRecord(Long streamingId, StreamingDto.Watch dto) {
        String key = getAccessKey(streamingId, dto);
        streamingRedisRepository.incrementStreamingView(key);
    }

    private String getAccessKey(Long streamingId, StreamingDto.Watch dto) {
        String identifier = dto.getUserId() != null ?
                "user:" + dto.getUserId() :
                "ip:" + dto.getSourceIp();
        return "streaming:access:" + streamingId + ":" + identifier;
    }

    private void incrementViewCount(Long streamingId) {
        String redisKey = RedisKeyUtil.formatViewCountKey(streamingId);
        streamingRedisRepository.incrementStreamingView(redisKey);
    }

    private PlayHistory createPlayHistory(StreamingDto.Watch dto, Long streamingId, boolean isCreator) {
        PlayHistory playHistory = PlayHistory.create(
                dto.getUserId(),
                streamingId,
                dto.getSourceIp(),
                isCreator
        );
        return playHistoryRepository.save(playHistory);
    }

    private Optional<PlayHistory> getRecentPlayHistory(Long userId, Long streamingId, String sourceIp) {
        if (userId != null) {
            return playHistoryRepository.findTopByUserIdAndStreamingIdOrderByCreatedAtDesc(userId, streamingId);
        }
        return playHistoryRepository.findTopBySourceIpAndStreamingIdOrderByCreatedAtDesc(sourceIp, streamingId);
    }

    private int getLastPlayTime(Long userId, Long streamingId, String sourceIp) {
        return getRecentPlayHistory(userId, streamingId, sourceIp)
                .map(PlayHistory::getLastPlayTime)
                .orElse(0);
    }
}
