package streamingsettlement.streaming.domain.repository;

import java.util.Set;

public interface StreamingRedisRepository {
    void incrementStreamingView(String key);
    Long getStreamingView(String key);
    Set<String> getViewCountKeys(String pattern);
    void deleteKey(String key);
}
