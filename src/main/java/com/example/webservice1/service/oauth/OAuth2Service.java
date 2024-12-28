package com.example.webservice1.service.oauth;

import com.example.webservice1.domain.user.OAuthAttributes;
import com.example.webservice1.domain.user.UserRepository;
import com.example.webservice1.dto.user.UserProfile;
import com.example.webservice1.domain.user.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public String getCurrentUser() { //현재 사용자 정보를 가져오는 메서드
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            return oAuth2User.getAttribute("email");
            //return (OAuth2User) authentication.getPrincipal();
        }
        return null;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {//OAuth2UserRequest 객체는 인증 요청에 대한 정보를 담고 있는 객ㅊ[
        OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);//loadUser(userRequest)메서드 호출하면
        // Oauth인증 서버에서 사용자 정보를 가져와 OAuth2User객체를 반환
        // Oauth객체에는(사용자 id, 이메일등의 정보 포함)

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); //로그인을 수행한 서비스의 이름(google/ naver/ kakao ..)


        //userNameAttributeName은 OAuth2 제공자(예: Google, Naver, Kakao 등)에서
        // 인증된 사용자의 고유 식별자(Primary Key, PK)를 나타내는 값
        /*
            여기서는 사용자에게 key 값을 반환 시킴
            google = "sub" / naver, kakao ="id" 이렇게
            나중에 이 키값으로 attribute로 받은 사용자 정보(Map형태)를 찾을 거임*** 중요
            attributes.get(userNameAttributeName) 이렇게
            굳이 userNameAttributeName를 파미터로 받는 이유? 서비스 제공자가 여러개니까 객체를 만들어서 여러개 대응가능하게 만든것
         */
        String userNameAttributeName = userRequest
                .getClientRegistration()//어떤 서비스(예: Google, Naver, Kakao)에서 로그인했는지에 대한 정보를 담고 있음
                .getProviderDetails()//해당 OAuth2 제공자에 대한 세부 정보를 반환 (제공자(예: Google, Naver, Kakao)의 정보가 포함
                .getUserInfoEndpoint() //OAuth2 제공자에서 사용자 정보를 가져오는 엔드포인트에 대한 정보를 제공. OAuth2 제공자는 특정 URL을 통해 사용자의 정보를 반환
                .getUserNameAttributeName(); //  어떤 필드가 해당 사용자의 고유 식별자인지 반환  Google의 경우 sub(subject)가 주로 사용되며, Naver는 id나 email

        //확인용 로그
        log.info(">>>>>registrationId:"+registrationId
                +"------- userNameAttributeName:"+userNameAttributeName);


        Map<String, Object> attributes = oAuth2User.getAttributes(); // 사용자가 가지고 있는 정보 attributes map에 저장


        //서비스 제공자로 부터 받은 map데이터 attributes(사용자 정보)를 dto객체로(UserProfile)로 변환하는 과정임
        UserProfile userProfile = OAuthAttributes.extract(registrationId, attributes);//userProfie dto객체를 반환 받음
        userProfile.setProvider(registrationId); //서비스 해당 식별자 저장 ex) "google", "navar", "kakao"

        updateOrSaveUser(userProfile);//사용자 데이터 DB 저장 (dto -> entity변환 후 저장) 밑에 함수 정의 있음


        Map<String, Object> customAttribute =
                getCustomAttribute(registrationId, userNameAttributeName, attributes, userProfile);
        //getCustomAttribute 메서드는 인자로 받은 다양한 정보(제공자 ID, 사용자 속성 이름, OAuth2 사용자 정보 등)를
        //기반으로 사용자에 대한 커스텀 속성을 추출하여 맵 형태로 반환하는 역할을
        // 밑에 함수 정의 있음

        return new DefaultOAuth2User(
                //Collections.singleton => 단일 요소를 가진 불변 집합을 생성
                Collections.singleton(new SimpleGrantedAuthority("USER")), //SimpleGrantedAuthority는 권한 표현을 위한 객체
                customAttribute,//커스텀 사용자 정보 (map 형태
                userNameAttributeName); //userNameAttributeName => primary key(식별자)
        // oauth인증 성공 시 위에서 커스텀한 데이터들과 권한 표현 객체를 추가한 DefaultOAuth2User객체를 반환
    }

    public Map getCustomAttribute(String registrationId,
                                  String userNameAttributeName,
                                  Map<String, Object> attributes,
                                  UserProfile userProfile) {
        /*
            1. OAuth2 로그인 흐름 중에 Spring Security가 자동으로 OAuth2Service의 loadUser 메서드를 호출합니다.
            2. DefaultOAuth2User 객체는 인증된 사용자 정보를 담고 있으며, Spring Security의 SecurityContext에 저장됩니다
         */

        // Map 하나 선언해서
        Map<String, Object> customAttribute = new ConcurrentHashMap<>();

        //사용자 데이터들 다 넣고
        customAttribute.put(userNameAttributeName, attributes.get(userNameAttributeName));
        customAttribute.put("provider", registrationId);
        customAttribute.put("name", userProfile.getUsername());
        customAttribute.put("email", userProfile.getEmail());

        //map 반환
        return customAttribute;
    }

    public User updateOrSaveUser(UserProfile userProfile) {
        User user = userRepository
                //1. Optional<User> 타입으로 반환됩니다. 이는 값이 있을 수도 있고 없을 수도 있는 경우를 처리하기 위한 것.
                .findUserByEmailAndProvider(userProfile.getEmail(), userProfile.getProvider()) //사용자 조회
                //2-1.값이 있다면? ->  이미 있는 사용자 정보 업데이트 map함수 실행 Optional<User> 타입 객체 value 파라미터로 삽입
                //updateUser는 User entity 클래스에 정의 되어 있음
                .map(value -> value.updateUser(userProfile.getUsername(), userProfile.getEmail()))//사용자 데이터 최신화
                //2-2. 값이 없다면? -> 새로운 사용자 등록
                .orElse(userProfile.toEntity());//dto -> entity변환
        //3. 리파지 터리로 db에 저장하고 저장한 엔티티 반환
        return userRepository.save(user);
    }


}
