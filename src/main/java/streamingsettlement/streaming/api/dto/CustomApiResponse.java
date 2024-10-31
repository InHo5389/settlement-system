package streamingsettlement.streaming.api.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomApiResponse<T> {

    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    public CustomApiResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> CustomApiResponse<T> of(HttpStatus status, String message, T data) {
        return new CustomApiResponse<>(status, message, data);
    }

    public static <T> CustomApiResponse<T> ok(T data) {
        return new CustomApiResponse<>(HttpStatus.OK, HttpStatus.OK.name(), data);
    }

    public static <T> CustomApiResponse<T> ok(String message, T data) {
        return new CustomApiResponse<>(HttpStatus.OK, message, data);
    }
}
