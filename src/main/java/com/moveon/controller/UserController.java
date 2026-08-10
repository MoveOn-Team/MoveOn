package com.moveon.controller;

import com.moveon.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 회원 · 인증 화면
 *
 * 경로는 화면설계서(MOVEON_화면설계서_회원.md)를 따른다.
 *   SC-001 로그인          /login
 *   SC-002 회원가입        /join
 *   SC-003 아이디 찾기      /find-id
 *   SC-004 비밀번호 찾기    /find-pw
 *   SC-005 비밀번호 재설정  /reset-pw
 *
 * 지금은 로그인 화면 이동만 있다. 나머지는 feature/auth 브랜치에서 이어서 만든다.
 */
@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final IUserService userService; // 회원관련 로직 사용하기 위한 Service 가져오기

    /**
     * 로그인 화면으로 이동
     */
    @GetMapping(value = "/login")
    public String login() {

        log.info("{}.login Start!", this.getClass().getName());

        log.info("{}.login End!", this.getClass().getName());

        return "user/login";
    }

}
