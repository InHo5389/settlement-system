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
public class StreamingAdvertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long streamingId;
    private Long advertisementId;
    private Integer position;
    private LocalDateTime createdAt;

    @Builder
    public StreamingAdvertisement(Long streamingId, Long advertisementId, Integer position) {
        this.streamingId = streamingId;
        this.advertisementId = advertisementId;
        this.position = position;
        this.createdAt = LocalDateTime.now();
    }
}
