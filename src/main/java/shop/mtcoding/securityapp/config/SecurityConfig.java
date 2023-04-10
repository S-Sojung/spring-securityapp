package shop.mtcoding.securityapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 내가 커스터마이징 하는 시큐리티 설정. 그래서 본래의 시큐리티 설정이 비활성화 된다.
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CSRF 해제
        http.csrf().disable();
        // postman 접근해야함!! -CSR 할 때
        // csrf 잘못된 경로로 오는 것을 막음, 포스트 맨으로 접근하기 위해 - CSR 해야함.

        // 2. Form 로그인 설정
        http.formLogin()
                .loginPage("/loginForm")
                .usernameParameter("username") // 이걸로 username의 name값을 변경 가능하다.
                .passwordParameter("password")
                .loginProcessingUrl("/login") // Post + x-www-from-urlEncoded 가 디폴트
                .defaultSuccessUrl("/") // 원래 가려고 했던 페이지를 기억해준다
                .successHandler((req, resp, authentication) -> { // 로그인 성공 할 때마다 로그 남기고 싶을 경우 //행위를 요구.
                    // 재사용할 일 이 있을 것 같으면 클래스를 만들어주면된다. 여기는 재사용 할 것 같지 않으니 바로 람다로 사용.
                    System.out.println("디버그 : 로그인이 완료되었습니다.");
                })
                .failureHandler((req, resp, ex) -> { // 에러 로그 남기고 싶을 경우 //
                    System.out.println("디버그 : 로그인 실패 -> " + ex.getMessage());
                });

        // 3. 인증 권한 필터 설정
        // http.authorizeRequests().antMatchers(null); //이것도 가능
        http.authorizeRequests( // 이 주소는 인증이 필요하고 나머지 주소는 전부 허용 //다 열고, 필요한 것 만 막고
                authroize -> authroize.antMatchers("/users/**").authenticated()
                        .antMatchers("/manager/**")// 인증 + 권한
                        .access("hasRole('ADMIN') or hasRole('MANAGER')")
                        .antMatchers("/admin/**").hasRole("admin")
                        .anyRequest().permitAll());

        return http.build();
    }
}
