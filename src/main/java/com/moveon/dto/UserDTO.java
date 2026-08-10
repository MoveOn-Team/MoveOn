package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원 정보 (users 테이블)
 *
 * DB 컬럼은 snake_case(login_id), 자바는 camelCase(loginId) 로 쓴다.
 * application.properties 의 map-underscore-to-camel-case 설정이 알아서 이어준다.
 *
 * 신체정보 · 성향 4축 컬럼은 온보딩 담당자가 필요할 때 추가한다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserDTO {

    private int userId; // 회원 PK

    private String loginId; // 아이디

    private String password; // SHA-256(비밀번호 + salt)

    private String salt; // 회원마다 다른 랜덤 문자열

    private String name; // 이름

    private String email; // 이메일 (평문 저장)

    private String termsAgreedAt; // 필수약관 동의 시각

    private int privacyAgreed; // 개인정보 수집·이용 동의(선택) 1=동의

    private String createdAt; // 가입일시

    // 아래는 DB 테이블에 없는 조회 전용 컬럼(ALIAS)

    private String existsYn; // 중복 여부. 존재하면 Y

}
