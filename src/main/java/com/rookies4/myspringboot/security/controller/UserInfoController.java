package com.rookies4.myspringboot.security.controller;


import com.rookies4.myspringboot.security.models.UserInfo;
import com.rookies4.myspringboot.security.service.UserInfoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userinfos")
public class UserInfoController {
    @Autowired
    private UserInfoUserDetailsService service;

    @PostMapping("/new")
    public String addNewUser(@RequestBody UserInfo userInfo) {
        return service.addUser(userInfo);
    }
}