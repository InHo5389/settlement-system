package streamingsettlement.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {
    USER("시청자"),
    CREATOR("크리에이터")
    ;

    private final String value;
}
