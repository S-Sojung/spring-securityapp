package shop.mtcoding.securityapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;
import shop.mtcoding.securityapp.core.jwt.JwtAuthorizationFilter;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        // 외부에서 쓸 것도 아니고 다 DI 해서 사용할 것
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 원래는 http.addFillter를 통해서 할 수 있었는데 바뀌엇음 : SecurityConfigurerAdapter 를 상속받았엇는데
    // 이제 안하고 Bean으로 등록함
    // JWT 필터 등록이 필요함
    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            // builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
            // BasicAuthenticationFilter를 날리고 아무데나 filter를 등록해도 되고
            // BasicAuthenticationFilter를 내가 만든 필터로 바꿔치기 해도 되는데 내거로 바꿔치기 함.
            builder.addFilterAt(new JwtAuthorizationFilter(authenticationManager), BasicAuthenticationFilter.class);
            super.configure(builder);
        }
    }

    // 내가 커스터마이징 하는 시큐리티 설정. 그래서 본래의 시큐리티 설정이 비활성화 된다.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CSRF 해제
        http.csrf().disable();
        // 포스트 맨으로 접근하기 위해 - CSR 해야함.

        // 2. ifram 거부
        http.headers().frameOptions().disable();

        // 3. cors 재설정
        http.cors().configurationSource(configurationSource());

        // 4. jSessionId 사용 거부 : 세션을 안쓰는건 아니고 띄우지 않음
        // 왔던 애인지 확인하는 것 조차 stateful 상태를 저장하기 때문
        // 항상 처음 보는 애구나 하는 정책이 STATELESS
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 5. 로그인 해제
        // 알림창이 뜨면서 아이디랑 패스워드 작성하라고 함
        // http Basic정책 실행된다.
        // 모든 페이지 마다 다 작성하라고 함. 항상 페이지 갈 때마다 로그인 요청하라는 것.
        http.formLogin().disable();

        // 6. httpBasic 정책 해제 (BasicAuthenticationFilter 해제 )
        // http.httpBasic().disable();
        // 이걸 해제 하지 않고, httpBasicfilter자리에 내가 만든 필터를 넣어 줄 것임

        // 7. XSS (lucy 필터) 알아보기 : 국내 필터임 !

        // 8. 커스텀 필터 적용 ( Security 필터 교환 )
        // 여기서 한방에 등록한다 !! CustomSecurityFilterManager에서 다 add 해서
        http.apply(new CustomSecurityFilterManager());

        // Security가 성공하거나 실패했을때 successHandler failureHandler를 사용했는데 지금은 사용불가 =>
        // Stateless기 때문
        // 9. 인증 실패 처리
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
            // checkpoint -> 예외 핸들러 처리
            // dispatchServlet 전이기 때문에 Exception Handler 작동 불가능. throw 못날린다.
            // request 에서 getRemoteAddr 로 요청한 주소를 받아서 블랙리스트 처리 가능
            log.debug("디버그 : 인증 실패 : " + authException.getMessage());
            log.info("인포 : 인증 실패 : " + authException.getMessage());
            log.warn("워닝 : 인증 실패 : " + authException.getMessage());
            log.error("에러 : 인증 실패 : " + authException.getMessage());

            response.setContentType("text/plain; charset=utf-8");
            response.setStatus(401);
            response.getWriter().println("인증 실패");
        });

        // 10. 권한 실패 처리
        http.exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
            // checkpoint -> 예외 핸들러 처리
            log.debug("디버그 : 권한 실패 : " + accessDeniedException.getMessage());
            log.info("인포 : 권한 실패 : " + accessDeniedException.getMessage());
            log.warn("워닝 : 권한 실패 : " + accessDeniedException.getMessage());
            log.error("에러 : 권한 실패 : " + accessDeniedException.getMessage());

            response.setContentType("text/plain; charset=utf-8");
            response.setStatus(403);
            response.getWriter().println("권한 실패");
        });

        // 11. 인증 권한 필터 설정
        // http.authorizeRequests().antMatchers(null); //이것도 가능
        http.authorizeRequests( // 이 주소는 인증이 필요하고 나머지 주소는 전부 허용 //다 열고, 필요한 것 만 막고
                authroize -> authroize.antMatchers("/users/**").authenticated()
                        .antMatchers("/manager/**")// 인증 + 권한
                        .access("hasRole('ADMIN') or hasRole('MANAGER')")
                        .antMatchers("/admin/**").hasRole("admin")
                        .anyRequest().permitAll());

        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Javascript 요청 허용)
        configuration.addAllowedOriginPattern("*"); // 모든 IP 주소 허용 (프론트 앤드 IP만 허용 react)
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); // 옛날에는 디폴트 였다. 지금은 아닙니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
