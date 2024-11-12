package streamingsettlement.streaming.domain.repository;

import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;

import java.util.List;
import java.util.Optional;

public interface StreamingAdvertisementRepository {
    StreamingAdvertisement save(StreamingAdvertisement playHistory);
    Optional<StreamingAdvertisement> findByStreamingIdAndPosition(Long streamingId, Integer position);
    List<StreamingAdvertisement> findByStreaming(Long streamingId);
}
