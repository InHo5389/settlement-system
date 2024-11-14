package streamingsettlement.streaming.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;
import streamingsettlement.batch.reader.DailySettlementDTO;
import streamingsettlement.streaming.domain.entity.PlayHistory;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PlayHistoryJpaRepository extends JpaRepository<PlayHistory, Long> {

    Optional<PlayHistory> findFirstBySourceIpAndStreamingIdOrderByCreatedAtDesc(String sourceIp, Long streamingId);

    long countByStreamingIdAndCreatedAtBetween(
            Long streamingId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("""
            SELECT new streamingsettlement.batch.reader.DailySettlementDTO(
                p.streamingId,
                COUNT(p)
            )
            FROM PlayHistory p
            WHERE p.createdAt BETWEEN :startDate AND :endDate
            GROUP BY p.streamingId
            HAVING COUNT(p) > 0
            """)
    Page<DailySettlementDTO> findDailySettlements(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}

