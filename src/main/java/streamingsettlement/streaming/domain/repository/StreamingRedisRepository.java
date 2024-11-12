package streamingsettlement.streaming.domain.repository;

import java.util.Set;

public interface StreamingRedisRepository {
    void saveStreamingView(String key, long streamingViews);
    void incrementStreamingView(String key);
    void incrementAdView(String key);
    Long getStreamingView(String key);
    Long getAdView(String key);
    Set<String> getViewCountKeys(String pattern);
    Set<String> getAdViewKeys(String pattern);
    void deleteKey(String key);
    void clear();
}
