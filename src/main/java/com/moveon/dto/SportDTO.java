package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * 종목 정보 + 지속 적합도 점수
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class SportDTO {

    /** 이 종목이 무엇인지 - 신원 */

    private int sportId; // 종목번호

    private String name; // 종목명

    /** 어떤 성질의 운동인지 - 특성 */

    private String category; // FACILITY 시설에서 / OUTDOOR 야외에서 / HOME 집에서

    private int minPeople; // 1 혼자 / 2 상대 필요 / 3 단체

    private double metValue; // 대사당량. 칼로리 계산과 강도 판정에 사용

    /** 이 회원에게 얼마나 맞는지 - 점수 재료 */

    private double traitScore; // 성향 4축 점수 평균 (0~100)

    private Double participationRate; // 같은 연령대·성별의 참여율

    private Double nearestKm; // 가장 가까운 시설·코스까지 거리(km)

    /** 최종 결과 - 점수와 순위 */

    private int traitPoint;  // 성향   (40점 만점)

    private int statPoint;   // 통계   (25점 만점)

    private int bodyPoint;   // 신체   (15점 만점)

    private int accessPoint; // 접근성 (20점 만점)

    private int totalScore;  // 지속 적합도 (성향 40% + 통계 25% + 신체 15% + 접근성 20%)

    private int rank;        // 전체 종목 중 순위. 상세 화면에서는 Top3까지 나타냄.

    private int estimatedKcal; // 30분 했을 때 예상 소모 열량. 회원 몸무게로 계산해 종목마다 달라짐.
}
