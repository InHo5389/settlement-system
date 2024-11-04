package streamingsettlement.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.PlayHistory;

import java.util.Optional;

public interface PlayHistoryJpaRepository extends JpaRepository<PlayHistory,Long> {
    Optional<PlayHistory> findFirstBySourceIpAndStreamingIdOrderByViewedAtDesc(String sourceIp, Long streamingId);
}
