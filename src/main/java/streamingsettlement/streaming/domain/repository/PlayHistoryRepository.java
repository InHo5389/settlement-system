package streamingsettlement.streaming.domain.repository;

import streamingsettlement.streaming.domain.entity.PlayHistory;

import java.util.Optional;

public interface PlayHistoryRepository {

    Optional<PlayHistory> findLatestPlayHistory(String sourceIp, Long streamingId);
    PlayHistory save(PlayHistory playHistory);
    Optional<PlayHistory> findPlayHistoryById(Long playHistoryId);
    void playHistoryDeleteAll();
}
