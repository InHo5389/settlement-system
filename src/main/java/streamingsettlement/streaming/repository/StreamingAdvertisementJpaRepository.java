package streamingsettlement.streaming.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;

public interface StreamingAdvertisementJpaRepository extends JpaRepository<StreamingAdvertisement,Long> {
}
