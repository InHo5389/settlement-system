package streamingsettlement.adjustment.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;
import streamingsettlement.batch.step.dto.DailyStatisticDto;

import java.time.LocalDateTime;

public interface DailyStatisticJpaRepository extends JpaRepository<DailyStatistic, Long> {
    Page<DailyStatistic> findByStatisticDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
            SELECT new streamingsettlement.batch.step.dto.DailyStatisticDto(
                ph.streamingId,
                COUNT(ph.id),
                :startDate)
            FROM PlayHistory ph
            WHERE ph.createdAt BETWEEN :startDate AND :endDate
            GROUP BY ph.streamingId
            """)
    Page<DailyStatisticDto> countPlayHistoryGroupByStreamingId(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
