package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * 공공체육시설 (facilities 테이블)
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class FacilityDTO {

    private int facilityId;

    private String name; // 시설명

    private String guName; // 자치구

    private String roadAddr; // 도로명주소

    private String lotAddr; // 지번주소

    private double lat; // 위도

    private double lng; // 경도

    private String phone; // 전화번호

    private String homepageUrl; // 홈페이지

    private Integer capacity; // 동시 수용 인원

    /** 아래는 DB 테이블에 없는 조회 전용 값 */

    private double distanceKm; // 현위치에서의 거리

    private String reserveUrl; // 서울시 공공서비스예약 페이지. facility_sports 에서 종목별로 가져옴.

    private String districtUrl; // 자치구 시설관리공단 주소. 시설 홈페이지가 없을 때 씀
}
