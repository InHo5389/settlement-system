package streamingsettlement.streaming.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import streamingsettlement.streaming.domain.repository.StreamingRedisRepository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class StreamingRedisRepositoryImpl implements StreamingRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void incrementStreamingView(String key) {
        // Redis에 해당 키가 없으면 DB의 현재 조회수로 초기화
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key,"0");
        }
        redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long getStreamingView(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    @Override
    public Set<String> getViewCountKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

}
