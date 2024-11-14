package streamingsettlement.adjustment.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.adjustment.domain.entity.AdSettlementHistory;

public interface AdSettlementHistoryJpaRepository extends JpaRepository<AdSettlementHistory,Long> {
}
