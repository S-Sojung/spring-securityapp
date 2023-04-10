package shop.mtcoding.securityapp.service;

import javax.transaction.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.securityapp.dto.UserRequest;
import shop.mtcoding.securityapp.dto.UserResponse;
import shop.mtcoding.securityapp.model.User;
import shop.mtcoding.securityapp.model.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 1. 트랜잭션 관리 : DB 연결이 아님 DB연결은 Dispatch Servlet 부터 한다. / ioc 컨테이너가 관리하는 세션의 경우
     * 2. 영속성 객체 변경 감지
     * 3. RequestDTO 받기
     * 4. 비지니스 로직 처리하기
     * 5. ResponseDTO 응답하기
     */
    @Transactional // 이게 없으면 save 할때 잠깐만 걸렸다가 사라진다. : 잡다한 것들. 횡단관심사
    public UserResponse.JoinDTO 회원가입(UserRequest.JoinDTO joinDTO) {
        String rawPassword = joinDTO.getPassword();
        String encPassword = passwordEncoder.encode(rawPassword); // 60Byte로 설정된다.
        joinDTO.setPassword(encPassword);
        User userPS = userRepository.save(joinDTO.toEnity());
        // REST는 내가 insert 한 값을 프론트에게 무조건 돌려줘야한다.
        return new UserResponse.JoinDTO(userPS);
    }
}
