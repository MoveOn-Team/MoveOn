package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * 빌릴 수 있는 곳
 *
 * 두 곳에서 온다.
 *   서울시 공공서비스예약(reservation_raw)  facilityId = 0
 *   우리 시설 중 대관 주소를 가진 곳         facilityId > 0
 * 회원에게는 둘 다 '빌리는 곳' 이라 한 목록에 거리순으로 섞는다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RentalDTO {

    private String placeName; // 장소명

    private String minClass; // 풋살장 / 축구장 / 테니스장

    private String payYn; // 유료 / 무료

    private String svcUrl; // 예약 페이지

    private double distanceKm; // 현위치에서의 거리

    /**
     * 우리 시설이면 그 번호, 서울시 예약 자료면 0.
     *
     * 누르면 갈 곳이 다르다.
     * 우리 시설은 상세 화면으로 들어가 거기서 대관 단추를 누르고,
     * 서울시 예약 자료는 바로 바깥 예약 화면으로 나간다.
     */
    private int facilityId;

}
