package streamingsettlement.streaming.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum ErrorType {
    NOT_FOUND_USER(404, "회원을 찾을수 없습니다."),
    NOT_FOUND_HISTORY(404,"시청 기록이 없습니다."),
    NOT_FOUND_STREAMING(404,"등록된 영상이 없습니다."),
    ;

    private final int status;
    private final String message;
}

