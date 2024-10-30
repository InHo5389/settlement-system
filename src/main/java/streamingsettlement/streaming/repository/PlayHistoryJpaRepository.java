package streamingsettlement.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.PlayHistory;

public interface PlayHistoryJpaRepository extends JpaRepository<PlayHistory,Long> {
}
