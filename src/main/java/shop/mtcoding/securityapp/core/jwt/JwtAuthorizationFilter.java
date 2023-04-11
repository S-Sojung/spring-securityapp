package shop.mtcoding.securityapp.core.jwt;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import shop.mtcoding.securityapp.core.auth.MyUserDetails;
import shop.mtcoding.securityapp.model.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//모든 주소에서 발동. 특정 주소에서 발동하게 하고 싶으면 if로 다 걸어야함.
//BasicAuthenticationFilter과 바꿔치기 하지 말고 일반적인 필터로 만들어서 필요한 부분만 가도록 하는게 좋다.
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String prefixJwt = request.getHeader(MyJwtProvider.HEADER);
        if (prefixJwt == null) {
            chain.doFilter(request, response);
            return;
        }

        String jwt = prefixJwt.replace(MyJwtProvider.TOKEN_PREFIX, "");
        try {
            DecodedJWT decodedJWT = MyJwtProvider.verify(jwt);
            Long id = decodedJWT.getClaim("id").asLong();
            String role = decodedJWT.getClaim("role").asString();

            User user = User.builder().id(id).role(role).build();
            // 정상적인 토큰인지 확인하고 그거가지고 user 객체를 가짜로 만들어서 SecurityContextHolder에 넣어줌
            // 권한이 들어갔기 때문에 권한체크 가능!

            MyUserDetails myUserDetails = new MyUserDetails(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    myUserDetails,
                    myUserDetails.getPassword(),
                    myUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (SignatureVerificationException sve) {
            // 토큰이 있으면 세션이 만들어 질거고, 아니면 세션이 만들어 지지 않을 것.
            // 실패해도 그냥 넘겨도 됨. 주소에 대한 권한 처리를 Security에게 넘길 것이기 때문
            log.debug("디버그 : 토큰 검증 실패");
            // chain.doFilter(request, response);
            // return;
        } catch (TokenExpiredException tee) {
            log.error("디버그 : 토큰 만료됨");
        } finally {
            chain.doFilter(request, response);
        }

    }
}