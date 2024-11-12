package streamingsettlement.streaming.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import streamingsettlement.streaming.domain.repository.StreamingRepository;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.repository.jpa.StreamingAdvertisementJpaRepository;
import streamingsettlement.streaming.repository.jpa.StreamingJpaRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StreamingRepositoryImpl implements StreamingRepository {
    private final StreamingJpaRepository streamingJpaRepository;
    private final StreamingAdvertisementJpaRepository streamingAdvertisementJpaRepository;

    @Override
    public Optional<Streaming> findStreamingById(Long streamingId) {
        return streamingJpaRepository.findById(streamingId);
    }

    @Override
    public Streaming save(Streaming streaming) {
        return streamingJpaRepository.save(streaming);
    }

    @Override
    public void streamingDeleteAll() {
        streamingJpaRepository.deleteAll();;
    }
}
