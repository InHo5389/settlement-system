package streamingsettlement.streaming.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private Integer lastAdPlayTime;
    private String sourceIp;
    private LocalDateTime viewedAt;

    @Builder
    public PlayHistory(Long userId, Long streamingId,Integer lastAdPlayTime, int lastPlayTime, String sourceIp, LocalDateTime viewedAt) {
        this.userId = userId;
        this.streamingId = streamingId;
        this.lastPlayTime = lastPlayTime;
        this.lastAdPlayTime = lastAdPlayTime;
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
// 450 /420
    public List<Integer> calculateNewAdPositions(Integer newPlayTime, int adInterval) {
        List<Integer> newAdPositions = new ArrayList<>();
        int previousAdCount = this.lastAdPlayTime / adInterval;
        int currentAdCount = newPlayTime / adInterval;

        // 시청한 새로운 광고 위치들을 수집 (previousAdCount + 1부터 currentAdCount까지)
        for (int i = previousAdCount + 1; i <= currentAdCount; i++) {
            newAdPositions.add(i * adInterval);
        }

        // 마지막 광고 시청 시점 업데이트
        this.lastAdPlayTime = currentAdCount * adInterval;
        this.lastPlayTime = newPlayTime;

        return newAdPositions;
    }
}
