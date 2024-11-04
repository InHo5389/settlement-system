package streamingsettlement.streaming.domain;

import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;
import streamingsettlement.streaming.domain.entity.StreamingAdvertisement;

import java.util.List;
import java.util.Optional;

public interface StreamingRepository {
    Optional<Streaming> findStreamingById(Long streamingId);
    Streaming save(Streaming streaming);

    Optional<PlayHistory> findLatestPlayHistory(String sourceIp, Long streamingId);
    PlayHistory save(PlayHistory playHistory);
    Optional<PlayHistory> findPlayHistoryById(Long playHistoryId);

    void streamingDeleteAll();
    void playHistoryDeleteAll();

    StreamingAdvertisement save(StreamingAdvertisement playHistory);
    Optional<StreamingAdvertisement> findByStreamingIdAndPosition(Long streamingId,Integer position);
}
