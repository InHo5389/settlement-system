package streamingsettlement.streaming.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import streamingsettlement.streaming.domain.StreamingRepository;
import streamingsettlement.streaming.domain.entity.PlayHistory;
import streamingsettlement.streaming.domain.entity.Streaming;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StreamingRepositoryImpl implements StreamingRepository {
    private final StreamingJpaRepository streamingJpaRepository;
    private final PlayHistoryJpaRepository playHistoryJpaRepository;
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
    public Optional<PlayHistory> findFirstBySourceIpAndStreamingIdOrderByViewedAtDesc(String sourceIp, Long streamingId) {
        return playHistoryJpaRepository.findFirstBySourceIpAndStreamingIdOrderByViewedAtDesc(sourceIp,streamingId);
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
    public void streamingDeleteAll() {
        streamingJpaRepository.deleteAll();;
    }

    @Override
    public void playHistoryDeleteAll() {
        playHistoryJpaRepository.deleteAll();
    }
}
