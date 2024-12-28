package com.example.webservice1.domain.user;

import com.example.webservice1.dto.user.UserProfile;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;


/*
1. 사용자가 OAuth2 로그인으로 인증을 수행하면, 서버는 제공자에 맞는 사용자 정보를 포함한 Map<String, Object>를 받습니다.
2. OAuthAttributes.extract() 메서드는 registrationId(로그인 제공자 이름)에 따라 적절한 OAuthAttributes를 찾아서, 그 Function을 사용해 UserProfile 객체를 생성합니다.
3. extract 메서드는 attributes에서 사용자 정보를 추출하여 UserProfile 객체로 반환합니다.
*/
public enum OAuthAttributes { //enum이 메서드를 정의하거나 람다식을 통해 각 상수마다 다른 동작 지정
    //각 상수는 해당 OAuth 제공자로부터 받은 사용자 정보를 처리하는 람다 함수를 갖고있음

    GOOGLE("google", (attribute) -> {
        // registrationId "google" (식별자)
        // attribute 사용자 정보가 담긴 map
        //구글(OAuth제공자)로 부터 받은 사용자 정보를 UserProfile객체로 변환하는 과정
        UserProfile userProfile = new UserProfile();
        userProfile.setUserName((String)attribute.get("name"));
        userProfile.setEmail((String)attribute.get("email"));

        return userProfile;
    }),

    NAVER("naver", (attribute) -> {
        UserProfile userProfile = new UserProfile();

        Map<String, String> responseValue = (Map)attribute.get("response");

        userProfile.setUserName(responseValue.get("name"));
        userProfile.setEmail(responseValue.get("email"));

        return userProfile;
    }),

    KAKAO("kakao", (attribute) -> {

        Map<String, Object> account = (Map)attribute.get("kakao_account");
        Map<String, String> profile = (Map)account.get("profile");

        UserProfile userProfile = new UserProfile();
        userProfile.setUserName(profile.get("nickname"));
        userProfile.setEmail((String)account.get("email"));

        return userProfile;
    });

    private final String registrationId; // 로그인한 서비스(ex) google, naver..)
    private final Function<Map<String, Object>, UserProfile> of; // 로그인한 사용자의 정보를 통하여 UserProfile을 가져옴
    //Function<T,R>은 T를 입력받아 R을 반환하는 함수형 인터페이스

    OAuthAttributes(String registrationId, Function<Map<String, Object>, UserProfile> of) { //생성자
        this.registrationId = registrationId;
        this.of = of;
    }

    public static UserProfile extract(String registrationId, Map<String, Object> attributes) {
        //OAuth2Servie에서 ->> UserProfile userProfile = OAuthAttributes.extract(registrationId, attributes); 여기서 호출됨
        // 위 호출문 에서 파라미터로 받은 attributes는 서비스 제공자가 보내준 사용자의 정보를 map에 저장한 것
        /*
            OAuthAttributes.extract 메서드의 반환문은 UserProfile 객체
            이 메서드는 registrationId와 attributes를 기반으로 적절한 OAuthAttributes 상수를 찾아서,
            그 상수에 정의된 람다식을 사용하여 사용자 정보를 UserProfile 객체로 변환.

            여기 메서드에서 위 상수들 사용
         */

        return Arrays.stream(values())
                //values() 메서드는 OAuthAttributes enum의 모든 상수를 배열로 반환 => 그냥 열거형 클래스에서 values()로 쓰면 됨
                //그다음 Arrays.stream(values())으로 배열을 스트림으로 반환
                .filter(value -> registrationId.equals(value.registrationId))
                //String equals함수임
                //value에서 registrationId 아이디가 같은걸 찾는 과정
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .of.apply(attributes);
        //Fucnction 인터페이스 R apply(T) T를 입력받아서 R을 반환하는 함수 => T를 파라미터로 한 of 함수 실행
        // 위에서 정의한 그대로 UserProfile dto객체를 만들어서 반환

    }
}
