package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 종목 정보 + 지속 적합도 점수 (sports 테이블)
 *
 * 앞쪽 필드는 DB 에서 그대로 읽어오는 값이고,
 * 뒤쪽 점수 필드는 RecommendService 에서 계산해 채운다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SportDTO {

    private int sportId;

    private String name; // 종목명

    private String category; // FACILITY 시설에서 / OUTDOOR 야외에서 / HOME 집에서

    private int isIndoor; // 실내 여부

    private int minPeople; // 1 혼자 / 2 상대 필요 / 3 단체

    private int isCompetitive; // 겨루는 종목인지

    private double metValue; // 대사당량. 칼로리 계산과 강도 판정에 사용

    private String description;

    // 아래는 DB 테이블에 없는 조회 전용 값(ALIAS)

    private double traitScore; // 성향 4축 점수 평균 (0~100)

    private Double participationRate; // 국민생활체육조사 참여율(%). 자료가 없으면 null

    private Double nearestKm; // 가장 가까운 시설·코스까지 거리(km). 갈 곳이 없으면 null

    // 아래는 서비스에서 계산해 채우는 값

    private int traitPoint;  // 성향   (40점 만점)

    private int statPoint;   // 통계   (25점 만점)

    private int bodyPoint;   // 신체   (15점 만점)

    private int accessPoint; // 접근성 (20점 만점)

    private int totalScore;  // 지속 적합도 (0~100)

}
