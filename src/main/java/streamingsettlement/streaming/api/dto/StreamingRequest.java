package streamingsettlement.streaming.api.dto;

import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import streamingsettlement.streaming.domain.dto.StreamingDto;

public class StreamingRequest {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Watch{
        private Long userId;
        private String ipAddress;

        public StreamingDto.Watch toDto() {
            return StreamingDto.Watch.builder()
                    .userId(userId)
                    .sourceIp(ipAddress)
                    .build();
        }
    }

    @Getter
    @Setter
    public static class UpdatePlayTime {
        private Long playHistoryId;
        private Integer lastPlayTime;

        public StreamingDto.UpdatePlayTime toDto() {
            return StreamingDto.UpdatePlayTime.builder()
                    .playHistoryId(playHistoryId)
                    .lastPlayTime(lastPlayTime)
                    .build();
        }
    }
}
