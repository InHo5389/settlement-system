package streamingsettlement.streaming.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.Streaming;

public interface StreamingJpaRepository extends JpaRepository<Streaming,Long> {
}
