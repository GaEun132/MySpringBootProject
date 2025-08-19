package com.rookies4.myspringboot.controller;

import com.rookies4.myspringboot.entity.UserEntity;
import com.rookies4.myspringboot.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor//userRepository 변수에 대한 생성자를 만들어준다.
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/users/edit/{id}") //사용자 정보 조회
    public String showUpdateForm(@PathVariable("id") long id,
                                 Model model) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "update-user";
    }
    @PostMapping("/users/update/{id}") //수정버튼 누르기
    public String updateUser(@PathVariable("id") long id,
                             @Valid @ModelAttribute("user") UserEntity user,
                             BindingResult result) {
        if (result.hasErrors()) {
            user.setId(id);
            return "update-user";
        }
        userRepository.save(user);
        return "redirect:/users/index";
    }

    @GetMapping("/users/signup")
    //로그인 페이지에서 object=${user}을 사용하기 때문에 user키값의 데이터가 필요하다.
    public String showSignUpForm(@ModelAttribute("user") UserEntity user) {
        return "add-user";
    }

    @PostMapping("/users/adduser")
    //@Valid: validator 구현체를 호출한다. @NotBlank가 붙어있는 곳은 validator 구현체가 동작하여 입력값을 검증한다.
    //BindingResult: springframework.validation 입력값이 올바르지 않을때 에러정보를 저장한다.
    public String addUser(@Valid @ModelAttribute("user") UserEntity user,
                          BindingResult result, Model model) {
        //검증 오류가 발생한 경우
        if (result.hasErrors()) {
            return "add-user";
        }
        //오류가 발생하지 않은 경우: 등록처리
        userRepository.save(user);
        //model.addAttribute("users", userRepository.findAll());
        //return "index";
        //User목록조회하는 Path로 URL Redirection 하기
        return "redirect:/users/index";
    }

    @GetMapping("/users/index")//ModelAndView: 페이지 이름과 데이터를 같이 저장하는 객체
    public ModelAndView index() {
        List<UserEntity> userEntityList = userRepository.findAll();
        //index.html 페이지에 key-value: users : userEntityList인 데이터를 준다.
        return new ModelAndView("index","users", userEntityList);
    }

    @GetMapping("/thymeleaf")
    public String leaf(Model model) {
        model.addAttribute("name", "스프링부트");
        return "leaf";
    }
}