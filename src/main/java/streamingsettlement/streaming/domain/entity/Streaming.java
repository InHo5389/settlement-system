package streamingsettlement.streaming.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Streaming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private int duration;
    private long streamingViews;
    private String cdnUrl;
    private LocalDateTime createdAt;

    @Builder
    public Streaming(Long userId, String title, int duration, long streamingViews, String cdnUrl, LocalDateTime createdAt) {
        this.userId = userId;
        this.title = title;
        this.duration = duration;
        this.streamingViews = streamingViews;
        this.cdnUrl = cdnUrl;
        this.createdAt = createdAt;
    }

    public void updateViewCount(Long viewCount) {
        this.streamingViews += viewCount;
    }

    public boolean isCreator(Long userId) {
        return this.userId == userId;
    }
}
