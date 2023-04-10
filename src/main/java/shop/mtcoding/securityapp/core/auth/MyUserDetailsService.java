package shop.mtcoding.securityapp.core.auth;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.mtcoding.securityapp.model.User;
import shop.mtcoding.securityapp.model.UserRepository;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    // login + POST + FormUrlEncoded + username, password
    // 이거 리턴될때 Authentication 객체 만들어짐
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOP = userRepository.findByUsername(username);
        // Authentication 객체가 만들어지면서 비밀번호 검사도 하고, 암호화도 계속 변경해서 해준다.
        if (userOP.isPresent()) {
            return new MyUserDetails(userOP.get());
        } else {
            return null;
        }
    }
}
