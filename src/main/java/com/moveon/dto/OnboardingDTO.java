package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 온보딩 신체 정보와 운동 성향을 전달하는 DTO.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingDTO {

    @JsonIgnore
    private int userId; // 로그인 세션에서 가져오는 회원 번호

    private LocalDate birthDate; // 생년월일
    private String gender; // 성별(M, F)
    private BigDecimal height; // 키(cm)
    private BigDecimal weight; // 몸무게(kg)
    private BigDecimal bmi; // 서버에서 계산한 BMI
    private String bmiStatus; // BMI 상태 문구

    private String companion; // 운동 동반 성향(ALONE, PAIR, GROUP)
    private String competition; // 승부 성향(OWN_PACE, ANY, WIN)
    private String place; // 운동 장소 성향(INDOOR, ANY, OUTDOOR)
    private String intensity; // 운동 강도 성향(LIGHT, MODERATE, HARD)

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private boolean loggedIn; // 로그인 여부

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private boolean onboardingCompleted; // 온보딩 입력 완료 여부
}
