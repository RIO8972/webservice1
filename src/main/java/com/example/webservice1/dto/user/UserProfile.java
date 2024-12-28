package com.example.webservice1.dto.user;


import com.example.webservice1.domain.user.User;
import lombok.Getter;

@Getter
public class UserProfile{
    private String username; // 사용자 이름
    private String provider; // 로그인한 서비스

    //private String providerId; //사용자 식별자
    private String email; // 사용자의 이메일

    public void setUserName(String userName) {
        this.username = userName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }

    public User toEntity() {
        return User.builder()
                .username(this.username)
                .email(this.email)
                .provider(this.provider)
                .build();
    }
}

