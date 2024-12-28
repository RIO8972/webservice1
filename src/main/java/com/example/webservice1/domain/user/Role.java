package com.example.webservice1.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor//Role 클래스에서 title과 key는 final 필드이므로, Role 객체가 생성될 때 두 필드를 초기화해야 합니다.
public enum Role {
    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER","일반 사용자");

    private final String title;
    private final String key;
}

