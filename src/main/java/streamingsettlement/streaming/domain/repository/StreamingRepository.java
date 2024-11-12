package streamingsettlement.streaming.domain.repository;

import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;

import java.util.List;
import java.util.Optional;

public interface StreamingRepository {
    Optional<Streaming> findStreamingById(Long streamingId);
    Streaming save(Streaming streaming);
    void streamingDeleteAll();
}
