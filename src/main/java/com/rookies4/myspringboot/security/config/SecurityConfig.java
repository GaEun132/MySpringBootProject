package com.rookies4.myspringboot.security.config;

import com.rookies4.myspringboot.security.service.UserInfoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity //Authentication 인증 활성화
@EnableMethodSecurity //Authorization 권한 활성화
public class SecurityConfig {

    //passwordEncoder: 패스워드 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //UserDetailsService 인터페이스를 구현한 커스텀 객체를 Bean으로 설정하기
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoUserDetailsService();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        //DaoProvider로 변경
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        //커스텀 DeatailService 객체를 알려주기: UserDeatailsService Bean을 설정하기
        authenticationProvider.setUserDetailsService(userDetailsService());
        //BCryptPasswordEncoder Bean을 설정하기
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

/*    @Bean
//authentication(인증)을 위한 User 생성(admin, user)
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        //관리자 생성: 아이디, 비밀번호, 권한 정보를 저장한 UserDetails 객체 생성
        UserDetails admin = User.withUsername("adminboot")

                .password(encoder.encode("pwd1"))
                .roles("ADMIN")
                .build();
        //일반 사용자 생성
        UserDetails user = User.withUsername("userboot")

                .password(encoder.encode("pwd2"))
                .roles("USER")
                .build();
        //InMemoryUserDetailsManager: 메모리에 유저의 권한 정보, 아이디, 비밀번호 정보를 저장하고 관리한다.
        //2개의 UserDetails 객체를 메모리에 저장
        return new InMemoryUserDetailsManager(admin, user);
    }*/

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //csrf 기능 비활성화
        return http.csrf(csrf -> csrf.disable())
                //요청별로 권한을 설정
                .authorizeHttpRequests(auth -> {
                    //인증 필요 없는 페이지
                    auth.requestMatchers("/api/users/welcome","/userinfos/new","/api/users/**","/api/students/**").permitAll();
                    //인증이 필요한 페이지
                            //.requestMatchers("/api/users/**").authenticated();

                })
                //폼 로그인 페이지는 스프링이 디폴트로 제공하는 페이지를 사용
                //타임리프로 로그인 페이지를 만들어서 사용할 수도 있다.
                //예시 코드
                //formLogin(login → login
                //.loginPage(”/login")
                //.loginProcessingUrl(”/login-process”)
                .formLogin(withDefaults())
                .build();

    }
}