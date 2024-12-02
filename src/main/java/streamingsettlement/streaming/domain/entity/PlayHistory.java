package streamingsettlement.streaming.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class PlayHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long streamingId;
    private int lastPlayTime;
    private String sourceIp;
    private boolean isCreatorView;
    private LocalDateTime createdAt;

    @Builder
    private PlayHistory(Long userId, Long streamingId, Integer lastPlayTime, String sourceIp, LocalDateTime createdAt, boolean isCreatorView) {
        this.userId = userId;
        this.streamingId = streamingId;
        this.lastPlayTime = lastPlayTime;
        this.sourceIp = sourceIp;
        this.createdAt = createdAt;
        this.isCreatorView = isCreatorView;
    }

    public static PlayHistory create(Long userId, Long streamingId, String sourceIp, boolean isCreatorView) {
        return PlayHistory.builder()
                .userId(userId)
                .streamingId(streamingId)
                .lastPlayTime(0)
                .sourceIp(sourceIp)
                .createdAt(LocalDateTime.now())
                .isCreatorView(isCreatorView)
                .build();
    }

    public void updateLastPlayTime(int lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }
}
