package com.example.webservice1.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@EnableWebSecurity//시큐리티 활성화
@Configuration //@Configuration 어노테이션은 해당 클래스가 Spring의 설정 클래스를 정의하는 것을 나타냄
/*
@Configuration 역할

    1. 빈 등록: Spring IoC 컨테이너에 의해 관리되는 빈(Bean)을 정의
    이 어노테이션이 붙은 클래스는 스프링이 시작할 때 ApplicationContext에 의해 읽혀지고,
    @Bean으로 정의된 메서드들이 호출되어 빈이 생성되고 관리

    2. 설정 클래스: 애플리케이션의 설정을 포함하는 클래스를 정의
    이 클래스에서 정의된 빈은 다른 빈들과 연결되거나, 애플리케이션의 전체적인 동작을 조정하는 데 사용

    따라서 이걸 안넣었을 때 안된 이유?
    ->
        빈 등록 실패: @Bean으로 정의된 메서드가 실행되지 않으므로, SecurityFilterChain 빈이 등록되지 X
        이로 인해 Spring Security 설정이 적용되지 않거나, 기본 보안 설정이 적용.

        설정 누락: 스프링이 설정 클래스를 인식하지 못하므로,
        애플리케이션의 특정 기능(예: H2 콘솔 접근 허용, OAuth2 로그인 설정 등)이 제대로 적용X.

        빼고 로그를 살펴보면 밑에 SecurityFilterChain 메서드가 실행이 안됨
        + 추가로 SecurityFilterChain도 @Bean 어노테이션을 달아서 설정 메서드라는 걸 알려야함
 */
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;

    /*
        @Bean 어노테이션 사용 이유

        1. 보안 필터 체인 미설정: @Bean 어노테이션으로 등록된 SecurityFilterChain이 없으면,
        Spring Security가 HTTP 요청에 대한 적절한 보안 필터를 적용x.
        이로 인해 서드파티 쿠키 차단이나 다른 보안 정책이 제대로 작동하지 않을 수 있음.

        2. pring Security의 기본 설정: Spring Security는 기본적으로 다양한 보안 설정을 자동으로 적용
        @Bean으로 설정된 SecurityFilterChain이 없으면,
        이러한 기본 설정이 제대로 적용되지 않아, 서드파티 쿠키나 기타 보안 관련 설정에 문제가 발생할 수 있음
    */
    @Bean //@Bean 어노테이션 확인하기
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /************확인용 로그 나중에 삭제***********/
        if (oAuth2UserService != null) {
            log.info("OAuth2UserService 주입 성공", oAuth2UserService.getClass().getName());
        } else {
            log.error("OAuth2UserService 주입 실패");
        }
        /***************************************/

        return http
                .csrf(csrfConfigurer -> csrfConfigurer.disable()) // CSRF 보호 비활성화
                .logout(logoutConfigurer -> logoutConfigurer.disable()) // 로그아웃 비활성화()
                .formLogin(formLoginConfigurer -> formLoginConfigurer
                        .loginPage("/custom-login")//로그인창 view파일
                        .permitAll()) // 폼 로그인 비활성화

                //authorizeHttpRequests 메서드는 AuthorizeHttpRequestsConfigurer 객체(authorize)를 인자로 받는 람다식을 필요로 합니다.
                .authorizeHttpRequests(authorize -> authorize // 요청에 대한 인증 절차 AuthorizeHttpRequestsConfigurer 타입의 객체로, HTTP 요청에 대한 인증 및 인가 규칙을 추가하는 데 사용됩니다.
                        .requestMatchers("/home","/","/h2-console/**","/banner/**").permitAll() // 해당 URL은 인증 절차 없이 접근 가능
                        .anyRequest().authenticated() // 그 외의 요청은 인증 필요
                )
                //https://akira6036.tistory.com/72 헤더 설정 이유확인
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .oauth2Login(oauth2Configurer -> oauth2Configurer
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService))
                        .defaultSuccessUrl("/",true))


                //로그아웃 나중에
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/home") // 로그아웃 성공 후 리다이렉트할 URL
                        .invalidateHttpSession(true) // 세션 무효화
                        .deleteCookies("JSESSIONID")) // 쿠키 삭제
                .build();

    }
}
