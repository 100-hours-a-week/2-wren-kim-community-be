package ktb.community.be.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private final int status;
    private final String message;
    private final T data;

    private ApiResponse(HttpStatus status, String message, T data) {
        this.status = status.value();
        this.message = message;
        this.data = data;
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(HttpStatus.OK, "요청 성공", null);
    }


    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK, "요청 성공", data);
    }

    public static ApiResponse<String> error(HttpStatus status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
