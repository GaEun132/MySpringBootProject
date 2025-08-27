package com.rookies4.myspringboot.security.service;

import com.rookies4.myspringboot.security.models.UserInfo;
import com.rookies4.myspringboot.security.models.UserInfoRepository;
import com.rookies4.myspringboot.security.models.UserInfoUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserInfoRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //로그인 폼에 입력한 아이디값이 자동으로 넘어온다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //입력한 username이 db에 있는지 조회한다.
        Optional<UserInfo> optionalUserInfo = repository.findByEmail(username);
        //있으면 사용자 정보를 담아서 UserDetails 객체를 반환한다.
        return optionalUserInfo.map(userInfo -> new UserInfoUserDetails(userInfo))
                //userInfo.map(UserInfoUserDetails::new)
                //db에 없으면 예외를 던진다.
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + username));

    }
    //UserInfo 엔티티 등록(컨트롤러에
    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        UserInfo savedUserInfo = repository.save(userInfo);
        return savedUserInfo.getName() + " user added!!";
    }


}