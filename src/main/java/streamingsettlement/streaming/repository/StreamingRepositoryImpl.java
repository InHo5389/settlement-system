package streamingsettlement.streaming.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import streamingsettlement.streaming.domain.StreamingRepository;

@Repository
@RequiredArgsConstructor
public class StreamingRepositoryImpl implements StreamingRepository {
    private final StreamingJpaRepository streamingJpaRepository;
    private final PlayHistoryJpaRepository playHistoryJpaRepository;
    private final StreamingAdvertisementJpaRepository streamingAdvertisementJpaRepository;
}
