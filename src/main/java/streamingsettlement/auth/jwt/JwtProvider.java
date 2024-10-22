package streamingsettlement.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import streamingsettlement.auth.service.dto.LoginUser;
import streamingsettlement.user.domain.User;
import streamingsettlement.user.domain.UserRoleEnum;

import java.util.Date;

public class JwtProvider {
    public static String create(LoginUser loginUser) {
        String jwtToken = JWT.create()
                .withSubject("streaming")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtUtil.EXPIRATION_TIME))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())
                .sign(Algorithm.HMAC512(JwtUtil.SECRET));

        return jwtToken;
    }

    // 토큰 검증 시 return 되는 LoginUser 객체를 강제로 시큐리티 세션에 직접 주입
    public static LoginUser verify(String token) {
        DecodedJWT decodedJWT = JWT
                .require(Algorithm.HMAC512(JwtUtil.SECRET))
                .build()
                .verify(token);

        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
        User user = User.builder().id(id).role(UserRoleEnum.valueOf(role)).build();
        return new LoginUser(user);
    }
}
