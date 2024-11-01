package streamingsettlement.streaming.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Entity
@NoArgsConstructor
public class PlayHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long streamingId;
    private int lastPlayTime;
    private Integer lastViewedAdPosition;
    private String sourceIp;
    private LocalDateTime viewedAt;

    @Builder
    public PlayHistory(Long userId, Long streamingId, int lastPlayTime, String sourceIp, LocalDateTime viewedAt) {
        this.userId = userId;
        this.streamingId = streamingId;
        this.lastPlayTime = lastPlayTime;
        this.lastViewedAdPosition = 0;
        this.sourceIp = sourceIp;
        this.viewedAt = viewedAt;
    }

    public static PlayHistory create(Long userId, Long streamingId, Optional<PlayHistory> optionalPlayHistory, String sourceIp) {
        return PlayHistory.builder()
                .userId(userId)
                .streamingId(streamingId)
                .lastPlayTime(optionalPlayHistory.map(PlayHistory::getLastPlayTime).orElse(0))
                .sourceIp(sourceIp)
                .viewedAt(LocalDateTime.now())
                .build();
    }

    public void updateLastViewedAdPosition(Integer newPlayTime, Streaming streaming, int adInterval) {
        // 이전 재생 시점이 몇 번째 광고까지 봤는지
        int previousAdCount = this.lastViewedAdPosition / adInterval;

        // 현재 재생 시점이 몇 번째 광고까지 봤는지
        int currentAdCount = newPlayTime / adInterval;

        // 새로 본 광고 수만큼 조회수 증가
        if (currentAdCount > previousAdCount) {
            streaming.incrementAdViews(currentAdCount - previousAdCount);
            this.lastViewedAdPosition = currentAdCount * adInterval;
        }

        this.lastPlayTime = newPlayTime;
    }
}
