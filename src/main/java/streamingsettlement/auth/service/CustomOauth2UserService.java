package streamingsettlement.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import streamingsettlement.auth.service.dto.LoginUser;
import streamingsettlement.auth.service.dto.GoogleResponse;
import streamingsettlement.auth.service.dto.OAuth2Response;
import streamingsettlement.user.domain.User;
import streamingsettlement.user.domain.UserRepository;
import streamingsettlement.user.domain.UserRoleEnum;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 구글로 부터 받은 userRequest 데이터에 대한 후처리 함수
    // 함수 종료시 @AuthenticationPrincipal 메서드가 만들어짐.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("google")){
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }

        String email =oAuth2Response.getEmail();
        String username = oAuth2Response.getProvider()+" "+email;

        String provider = oAuth2Response.getProvider();
        String name = oAuth2Response.getName();
        String password = passwordEncoder.encode(provider);

        Optional<User> optionalUser = userRepository.findByEmail(username);
        User user;
        if (optionalUser.isEmpty()){
            user = User.builder()
                    .email(username)
                    .password(password)
                    .name(name)
                    .role(UserRoleEnum.USER)
                    .build();
            userRepository.save(user);
        }else user = optionalUser.get();

        // 이게 Authentication 객체 안의 들어감.
        return new LoginUser(user,oAuth2User.getAttributes());
    }
}
