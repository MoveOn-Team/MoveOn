package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * 대관 가능한 곳 (reservation_raw)
 *
 * 서울시 공공서비스예약에서 받아온 값.
 * 축구·풋살·테니스처럼 강좌보다 구장 대관이 많은 종목에서 씀.
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

}
