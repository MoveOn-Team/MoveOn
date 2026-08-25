package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * users 테이블과 회원 화면에서 사용됨.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserDTO {

    private int userId; // 회원 번호(PK)
    private String loginId; // 로그인 아이디
    private String password; // 암호화할 비밀번호
    private String passwordConfirm; // 비밀번호 확인값
    private String salt; // 비밀번호 암호화용 사용자별 랜덤값
    private String name; // 회원 이름
    private String email; // 인증이 완료된 이메일

    private boolean ageConfirmed; // 만 14세 이상 확인 여부
    private boolean termsAgreed; // 필수 이용약관 동의 여부
    private int privacyAgreed; // 선택 개인정보 동의 여부(1 또는 0)

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private int exists; // 회원 정보 존재 여부(1 또는 0)
    private String emailCode; // 사용자가 입력한 이메일 인증번호
    private String purpose; // 이메일 인증 목적(JOIN, FIND_ID, FIND_PW)

    // 맞춤 추천의 신체 점수 계산에 쓰는 값 (getUserBody 로 조회)
    private String gender; // 성별 M / F
    private double bmi; // 키·몸무게로 자동 계산되는 가상컬럼
    private int age; // 만 나이. birth_date 로 계산해서 받음
}
