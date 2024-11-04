package streamingsettlement.streaming.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import streamingsettlement.streaming.api.dto.CustomApiResponse;
import streamingsettlement.streaming.api.dto.StreamingRequest;
import streamingsettlement.streaming.domain.StreamingService;
import streamingsettlement.streaming.domain.dto.StreamingResponse;

@RestController
@RequestMapping("/streamings")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping("/{streamingId}/watch")
    public CustomApiResponse<StreamingResponse.Watch> watch(
            @PathVariable Long streamingId,
            @RequestBody StreamingRequest.Watch request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getHeader("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = servletRequest.getRemoteAddr();
        }
        request.setIpAddress(clientIp);

        return CustomApiResponse.ok("영상 정보를 응답합니다.", streamingService.watch(streamingId, request.toDto()));
    }

    @PostMapping("/update-time")
    public void updatePlayTime(
            @RequestBody StreamingRequest.UpdatePlayTime request
    ) {
        streamingService.saveAdViewsToRedis(request.toDto());
    }
}
