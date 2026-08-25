package com.moveon.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Integer userId;     // users 테이블 PK (INT)
    private String loginId;    // 아이디
    private String name;       // 이름
    private String email;      // 이메일

    // 신체 정보 (내 정보 / 회원 정보 수정 핵심 필드)
    private Double heightCm;   // 키 (height_cm)
    private Double weightKg;   // 몸무게 (weight_kg)
    private Double bmi;        // 가상 컬럼 또는 자바 연산 BMI

    // 온보딩 및 기타 사용자 정보
    private Date birthDate;
    private String gender;

    private String traitCompanion;
    private String traitCompetition;
    private String traitPlace;
    private String traitIntensity;

    // 자바 단에서 안전하게 BMI 보완 산출
    public Double getCalculatedBmi() {
        if (this.bmi != null) {
            return this.bmi;
        }
        if (this.heightCm == null || this.weightKg == null || this.heightCm <= 0 || this.weightKg <= 0) {
            return 0.0;
        }
        double heightM = this.heightCm / 100.0;
        double calc = this.weightKg / (heightM * heightM);
        return Math.round(calc * 10.0) / 10.0;
    }
}