package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * 관리자 계정 (admins 테이블)
 *
 * 회원(users)과 따로 간다. 세션 키도 SS_ADMIN_NO 로 구분한다.
 * 회원이 주소를 직접 쳐서 관리자 화면에 들어오는 일을 막기 위해서다.
 *
 * 비밀번호를 담는 방식은 UserDTO 와 같다.
 *   password = EncryptUtil.encHashSHA256(입력한 비밀번호 + salt)
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AdminDTO {

    private int adminId;

    private String loginId;

    private String password;

    /** 같은 비밀번호를 써도 저장되는 값이 달라지게 하는 무작위 문자열 */
    private String salt;

    private String name;

    private LocalDateTime createdAt;
}
