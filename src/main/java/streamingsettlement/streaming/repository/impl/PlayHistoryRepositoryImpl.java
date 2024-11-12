package streamingsettlement.streaming.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import streamingsettlement.streaming.domain.repository.PlayHistoryRepository;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.repository.jpa.PlayHistoryJpaRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlayHistoryRepositoryImpl implements PlayHistoryRepository{

    private final PlayHistoryJpaRepository playHistoryJpaRepository;

    @Override
    public Optional<PlayHistory> findLatestPlayHistory(String sourceIp, Long streamingId) {
        return playHistoryJpaRepository.findFirstBySourceIpAndStreamingIdOrderByCreatedAtDesc(sourceIp,streamingId);
    }

    @Override
    public PlayHistory save(PlayHistory playHistory) {
        return playHistoryJpaRepository.save(playHistory);
    }

    @Override
    public Optional<PlayHistory> findPlayHistoryById(Long playHistoryId) {
        return playHistoryJpaRepository.findById(playHistoryId);
    }

    @Override
    public void playHistoryDeleteAll() {
        playHistoryJpaRepository.deleteAll();
    }

}
