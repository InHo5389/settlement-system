package streamingsettlement.auth.service.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import streamingsettlement.user.domain.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
public class LoginUser implements OAuth2User {

    private User user;
    private Map<String, Object> attributes;

    // 일반 로그인 생성자
    public LoginUser(User user) {
        this.user = user;
    }

    // 소셜 로그인 생성자
    // attributes를 토대로 user를 만들거임
    public LoginUser(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> "ROLE_" + user.getRole());
        return authorities;
    }

    @Override
    public String getName() {
        return user.getName();
    }
}
