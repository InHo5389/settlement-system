package streamingsettlement.streaming.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import streamingsettlement.batch.step.dto.DailyStatisticDto;
import streamingsettlement.streaming.domain.entity.PlayHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlayHistoryJpaRepository extends JpaRepository<PlayHistory, Long> {

    Optional<PlayHistory> findFirstBySourceIpAndStreamingIdOrderByCreatedAtDesc(String sourceIp, Long streamingId);

    Optional<PlayHistory> findTopByUserIdAndStreamingIdOrderByCreatedAtDesc(Long userId, Long streamingId);

    Optional<PlayHistory> findTopBySourceIpAndStreamingIdOrderByCreatedAtDesc(String sourceIp, Long streamingId);

    Page<PlayHistory> findByCreatedAtBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    List<PlayHistory> findByStreamingIdAndCreatedAtBetween(
            Long id,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
            SELECT new streamingsettlement.batch.step.dto.DailyStatisticDto(
                ph.streamingId,
                COUNT(ph.id),
                COALESCE(SUM(ph.lastPlayTime), 0))
            FROM PlayHistory ph
            WHERE ph.createdAt BETWEEN :startDate AND :endDate
            GROUP BY ph.streamingId
            ORDER BY ph.streamingId ASC
            """)
    Page<DailyStatisticDto> findDailyPlayHistoryAggregation(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}

