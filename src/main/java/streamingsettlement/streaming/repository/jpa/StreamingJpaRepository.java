package streamingsettlement.streaming.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.Streaming;

import java.util.List;

public interface StreamingJpaRepository extends JpaRepository<Streaming,Long> {
    Page<Streaming> findByIdBetween(Long firstId, Long secondId, Pageable pageable);
}
