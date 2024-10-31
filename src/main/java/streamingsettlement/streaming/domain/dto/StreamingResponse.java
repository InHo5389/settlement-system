package streamingsettlement.streaming.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class StreamingResponse {
    @Getter
    @Builder
    public static class Watch{
        private Long playHistoryId;
        private int lastPlayTime;
    }
}
