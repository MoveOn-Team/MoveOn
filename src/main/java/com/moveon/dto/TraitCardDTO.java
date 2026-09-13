package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


/**
 * 추천 화면 위쪽의 성향 요약 카드.
 *
 * 마이페이지의 회원 정보는 UserProfileDTO 다. 이름이 겹치지 않게 둔다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraitCardDTO {

    /** 예) 혼자서 꾸준히 */
    private String titleTop;

    /** 예) 실내 중강도 타입 */
    private String titleBottom;

    /* 카드 안 알약. 앞에 '동반자' 같은 말이 붙어서 정도만 적는다 */

    private String companionLabel;   // 혼자 / 둘이서 / 여럿이
    private String competitionLabel; // 낮음 / 보통 / 높음
    private String placeLabel;       // 실내 / 상관없음 / 실외
    private String intensityLabel;   // 약 / 중 / 강

    /** 종목 상세 부제. 예) 혼자 · 내 페이스 · 실내 · 중강도 */
    private String traitSummary;

    private int age; // 만 나이

    private BigDecimal bmi;

    private String regionName; // 현위치. 예) 강서구 화곡동, 못 받으면 null
}
