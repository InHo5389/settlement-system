package streamingsettlement.streaming.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum ErrorType {
    NOT_FOUNT_USER(404, "회원을 찾을수 없습니다.");

    private final int status;
    private final String message;
}

