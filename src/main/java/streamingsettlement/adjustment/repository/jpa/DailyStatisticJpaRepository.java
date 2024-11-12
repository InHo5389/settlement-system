package streamingsettlement.adjustment.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.adjustment.domain.entity.DailyStatistic;

public interface DailyStatisticJpaRepository extends JpaRepository<DailyStatistic,Long> {
}
