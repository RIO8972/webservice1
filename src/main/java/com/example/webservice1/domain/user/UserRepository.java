package com.example.webservice1.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //이 메서드는 email과 provider를 파라미터로 받아, 해당 이메일과 제공자(provider)에 해당하는 사용자 정보를 조회
    //Optional<User>반환값은? 값이 있을 수도 있고 없을 수도 있기에
    /*
    자동 구현: Spring Data JPA는 findUserByEmailAndProvider 메서드의 이름과 매개변수명을 분석하여
    User 엔티티에서 해당 필드를 기반으로 쿼리를 자동으로 생성합니다.
     */
    Optional<User> findUserByEmailAndProvider(String email, String provider);

    Optional<User> findUserByEmail(String email);

    Optional<User> findByProvider(String provider);


    //Optional<User> findByEmail(String email);
}
