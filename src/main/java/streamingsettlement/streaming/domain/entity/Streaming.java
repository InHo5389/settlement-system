package streamingsettlement.streaming.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Streaming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private int duration;
    private long totalViews;
    private String cdnUrl;
    private LocalDateTime createdAt;

    @Builder
    public Streaming(Long userId, String title, int duration, long totalViews, String cdnUrl, LocalDateTime createdAt) {
        this.userId = userId;
        this.title = title;
        this.duration = duration;
        this.totalViews = totalViews;
        this.cdnUrl = cdnUrl;
        this.createdAt = createdAt;
    }

    public void increaseView(){
        this.totalViews++;
    }
}
