package shop.mtcoding.securityapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.securityapp.dto.ResponseDTO;
import shop.mtcoding.securityapp.dto.UserRequest;
import shop.mtcoding.securityapp.dto.UserResponse;
import shop.mtcoding.securityapp.service.UserService;

@RequiredArgsConstructor
@Controller
public class HelloController {
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    } // login은 security가 처리하기 때문에 따로 만들지 않음.

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(UserRequest.JoinDTO joinDTO) {
        UserResponse.JoinDTO data = userService.회원가입(joinDTO);
        // data.setRole(null); 트랜잭션이 종료되었기 떄문에 변경감지 안됨. Entity가 아닌 DTO 이기도 함.
        // 기존 객체를 깊은 복사 해서 하이버네이트가 관리하지 않게 함.
        ResponseDTO<?> responseDTO = new ResponseDTO<>().data(data);
        return ResponseEntity.ok().body(responseDTO);

    }
}
