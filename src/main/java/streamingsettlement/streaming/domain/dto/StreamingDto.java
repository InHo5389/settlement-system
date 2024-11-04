package streamingsettlement.streaming.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class StreamingDto {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class Watch{
        private Long userId;
        private String sourceIp;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdatePlayTime{
        private Long playHistoryId;
        private Integer lastPlayTime;
    }
}
