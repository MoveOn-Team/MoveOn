package com.moveon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 야외 코스 (평지 · 산길)
 *
 * 서울시 두드림길 자료를 모아 둔 courses 표를 그대로 받는다.
 * 두드림길은 '걸어서 즐기는 길' 사업이라 본격 등산로가 아니다.
 * 산이 낀 코스도 동네 뒷산을 도는 자락길이라 화면에서는 '산길' 이라 부른다.
 *
 * 지금 자료에서 비어 있는 칸이 있다.
 *   guName      43개 전부 없음
 *   subwayInfo  43개 전부 없음
 *   features    43개 전부 없음
 * 화면에서 이 칸들을 쓸 때는 반드시 빈 값인지 보고 그려야 한다.
 * 없는 값을 자리만 잡아 두면 "· " 같은 구분점만 덩그러니 남는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private int courseId;
    private String name;

    /**
     * WALK 평지 / HIKE 산길
     *
     * 표에 든 값은 예전 이름 그대로다. 화면에서만 평지·산길로 부른다.
     * 값을 바꾸려면 enum 을 고쳐야 하고 팀원 DB 까지 함께 손봐야 해서 두었다.
     */
    private String courseType;

    private String guName;

    /** 코스 길이 (km) */
    private Double distanceKm;

    /** 걷는 데 걸리는 시간 (분) */
    private Integer durationMin;

    /** EASY / NORMAL / HARD */
    private String difficulty;

    /** 무장애·데크·계단 같은 특징 */
    private String features;

    /** LOOP 순환형 / ONE_WAY 편도 */
    private String loopType;

    /** 이어지는 지하철역 */
    private String subwayInfo;

    private double startLat;
    private double startLng;

    /** DUDREAM 두드림길 / DULLE 둘레길 / MOUNTAIN 산길 */
    private String source;

    /** 조회 전용. 현위치에서 코스 시작점까지의 거리 (km) */
    private double distanceFromMe;
}
