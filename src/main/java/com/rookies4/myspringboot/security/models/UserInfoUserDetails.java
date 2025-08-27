package com.rookies4.myspringboot.security.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoUserDetails implements UserDetails {

    private String email;
    private String password;
    private List<GrantedAuthority> authorities;
    private UserInfo userInfo;

    //생성자로 UserInfo 엔티티 객체를 주입 받는다.(db의 정보를 담아서 만들기 때문)
    public UserInfoUserDetails(UserInfo userInfo) {
        this.userInfo = userInfo;
        this.email=userInfo.getEmail();
        this.password=userInfo.getPassword();
        //userInfo.getRoles() => "ROLE_ADMIN,ROLE_USER"
        //stream 객체 생성 -> getRole()호출 후 ","를 기준으로 split -> GrantedAuthority 객체 생성
        this.authorities= Arrays.stream(userInfo.getRoles().split(","))
                .map(roleName -> new SimpleGrantedAuthority(roleName))
                //.map(SimpleGrantedAuthority::new)
                //Stream<SimpleGrantedAuthority> ==> List<SimpleGrantedAuthority> 변환
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /*
        AuthenticationManager객체가 인증을 처리할 때
        getUsername() 와 getPassword() 메서드를 호출한다.
     */

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}