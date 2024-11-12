package streamingsettlement.adjustment.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.adjustment.domain.entity.StreamingSettlementHistory;

public interface StreamingSettlementHistoryJpaRepository extends JpaRepository<StreamingSettlementHistory,Long> {
}
