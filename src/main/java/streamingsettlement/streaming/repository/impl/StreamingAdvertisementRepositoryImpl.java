package streamingsettlement.streaming.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;
import streamingsettlement.streaming.domain.repository.StreamingAdvertisementRepository;
import streamingsettlement.streaming.repository.jpa.StreamingAdvertisementJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StreamingAdvertisementRepositoryImpl implements StreamingAdvertisementRepository {

    private final StreamingAdvertisementJpaRepository streamingAdvertisementJpaRepository;

    @Override
    public StreamingAdvertisement save(StreamingAdvertisement streamingAdvertisement) {
        return streamingAdvertisementJpaRepository.save(streamingAdvertisement);
    }

    @Override
    public Optional<StreamingAdvertisement> findByStreamingIdAndPosition(Long streamingId, Integer position) {
        return streamingAdvertisementJpaRepository.findByStreamingIdAndPosition(streamingId,position);
    }

    @Override
    public List<StreamingAdvertisement> findByStreaming(Long streamingId) {
        return streamingAdvertisementJpaRepository.findByStreamingId(streamingId);
    }
}
