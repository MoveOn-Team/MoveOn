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

    /**
     * 즉시운동 탭에서 이 종목을 어떻게 다루는지.
     *
     *   RENT  장소를 시간대로 빌린다   배드민턴 탁구 테니스 농구 축구/풋살
     *   PASS  표 끊고 혼자 한다       헬스 수영 골프 스쿼시
     *   null  즉시운동에 안 나온다    (강좌로만 존재하는 종목)
     *
     * 추천 탭은 이 값을 안 본다. 거기서는 스물두 종목을 다 다룬다.
     */
    private String workoutMode;

    /** 이 회원에게 얼마나 맞는지 - 점수 재료 */

    private double traitScore; // 성향 4축 점수 평균 (0~100)

    private Double participationRate; // 같은 연령대·성별의 참여율

    private Double nearestKm; // 가장 가까운 시설·코스까지 거리(km)

    /** 최종 결과 - 점수와 순위 */

    private int totalScore;  // 지속 적합도 (성향 40% + 통계 25% + 신체 15% + 접근성 20%)

    private int rank;        // 전체 종목 중 순위. 상세 화면에서는 Top3까지 나타냄.

    private int estimatedKcal; // 30분 했을 때 예상 소모 열량. 회원 몸무게로 계산해 종목마다 달라짐.
}
