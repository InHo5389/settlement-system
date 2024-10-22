package streamingsettlement.auth.jwt;

public interface JwtUtil {
    String SECRET = "secret"; // HS256(대칭키)
    int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 일주일
    String TOKEN_PREFIX = "Bearer ";
    String Header = "Authorization";
}
