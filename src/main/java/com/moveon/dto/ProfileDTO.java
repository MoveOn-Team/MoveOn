package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


/**
 * 맞춤 추천 화면 위쪽의 성향 요약 카드에 쓰는 값
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDTO {

    private String titleTop; // 카드 제목 윗줄

    private String titleBottom; // 카드 제목 아래줄

    /** 카드 안 내용 */

    private String companionLabel;   // 혼자 / 둘이서 / 여럿이

    private String competitionLabel; // 내 페이스대로 / 상관없음 / 겨루는게 좋음

    private String placeLabel;       // 실내 / 상관없음 / 실외

    private String intensityLabel;   // 가볍게 / 적당히 / 강하게

    /** 종목 상세 부제. 예) 혼자 · 내 페이스 · 실내 · 중강도 */

    private String traitSummary;

    private int age; // 만 나이

    private BigDecimal bmi;

    private String regionName; // 현위치. 예) 강서구 화곡동, 못 받으면 null
}
