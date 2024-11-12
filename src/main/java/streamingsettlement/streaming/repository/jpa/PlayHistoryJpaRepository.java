package streamingsettlement.streaming.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.PlayHistory;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PlayHistoryJpaRepository extends JpaRepository<PlayHistory,Long> {

    Optional<PlayHistory> findFirstBySourceIpAndStreamingIdOrderByCreatedAtDesc(String sourceIp, Long streamingId);

    long countByStreamingIdAndCreatedAtBetween(
            Long streamingId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
