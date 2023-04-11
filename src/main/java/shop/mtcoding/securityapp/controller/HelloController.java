package shop.mtcoding.securityapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.mtcoding.securityapp.core.auth.MyUserDetails;
import shop.mtcoding.securityapp.core.jwt.MyJwtProvider;
import shop.mtcoding.securityapp.dto.ResponseDTO;
import shop.mtcoding.securityapp.dto.UserRequest;
import shop.mtcoding.securityapp.dto.UserResponse;
import shop.mtcoding.securityapp.model.UserRepository;
import shop.mtcoding.securityapp.service.UserService;

/**
 * 로그 레벨 : trace, debug, info, warn, error
 * Sysout을 많이 남기면, nohub의 out 파일에 log들에 내가 sysout 한것들도 다 남는다. : 배포할 땐 다 지워야함..!
 * 아래 설정 덕에 DEBUG 이상의 로그는 콘솔에 다 남을 것임
 * 이제 배포할땐 INFO 이상 으로 바꾸면 콘솔에 DEBUG가 뜨지 않을 것 .
 * 
 * logging:
 * level:
 * '[shop.mtcoding.securityapp]': DEBUG # DEBUG 레벨부터 에러 확인할 수 있게 설정하기
 * '[org.hibernate.type]': TRACE # 콘솔 쿼리에 ? 에 주입된 값 보기
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class HelloController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @GetMapping("/users/1")
    public ResponseEntity<?> userCheck(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        // AuthenticationPrincipal는 Authentication에 있는 pricipal 자리에 들어간다.
        // 이 자리에는 user 및 role이 있다.
        // String username = myUserDetails.getUser().getUsername();
        Long id = myUserDetails.getUser().getId(); // Username이 필요하면 여기서 Reponsitory를 통해 받아야함.
        String role = myUserDetails.getUser().getRole();
        return ResponseEntity.ok().body(id + " : " + role);
    }

    @GetMapping("/")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    // login은 security가 따로 처리 했었는데 지금 은 loginfilter를 뺏음
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest.LoginDTO loginDTO) {

        String jwt = userService.로그인(loginDTO);
        // 이 jwt를 나중에 security store에 넣을 것 앱에서!
        return ResponseEntity.ok().header(MyJwtProvider.HEADER, jwt).body("로그인 완료");
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(UserRequest.JoinDTO joinDTO) {
        // select 가능
        UserResponse.JoinDTO data = userService.회원가입(joinDTO);
        // select 불가능
        // data.setRole(null); 트랜잭션이 종료되었기 떄문에 변경감지 안됨. Entity가 아닌 DTO 이기도 함.
        // 기존 객체를 깊은 복사 해서 하이버네이트가 관리하지 않게 함.
        ResponseDTO<?> responseDTO = new ResponseDTO<>().data(data);
        return ResponseEntity.ok().body(responseDTO);
    }
}
