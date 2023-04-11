package shop.mtcoding.securityapp.core.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import shop.mtcoding.securityapp.model.User;

@Getter
public class MyUserDetails implements UserDetails {

    private User user;

    // principal에 user 객체를 넣기 위함.
    public MyUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> "ROLE_" + user.getRole()); // hasRole 로 설정한 Role값과 맞는지 비교해서 true false를 반환해준다.
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() { // 비밀번호를 많이 틀리면 계정 잠궈버리기 //데이터에 count가 필요하긴 함
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 회원을 탈퇴할 경우 삭제하지 않고 비활성화만 시킴.
        return user.getStatus();
    }

}
