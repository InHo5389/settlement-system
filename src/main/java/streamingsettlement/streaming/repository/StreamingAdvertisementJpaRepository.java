package streamingsettlement.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;

import java.util.List;
import java.util.Optional;

public interface StreamingAdvertisementJpaRepository extends JpaRepository<StreamingAdvertisement,Long> {
    Optional<StreamingAdvertisement> findByStreamingIdAndPosition(Long streamingId, Integer position);
}
