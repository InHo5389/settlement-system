package streamingsettlement.streaming.common.util;

public class RedisKeyUtil {
    private RedisKeyUtil() {
    }

    public static final String VIEW_COUNT_KEY = "streaming:%d:views";

    public static String formatViewCountKey(Long streamId) {
        return String.format(VIEW_COUNT_KEY, streamId);
    }
}
