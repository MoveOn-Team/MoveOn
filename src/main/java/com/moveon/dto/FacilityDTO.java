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

    private String districtUrl;

    /** 이 시설의 대관 신청 페이지. homepage_url 이 강습·소개라면 이쪽은 빌리는 쪽이다 */
    private String rentalUrl;

    /** 자치구 대관 신청 페이지. district_url 이 수강신청이라면 이쪽은 대관이다 */
    private String districtRentalUrl;

    /**
     * 이 시설이 이 종목으로 가진 강습·이용권의 수.
     *
     * 목록에서 '배우는 곳' 과 '빌리는 곳' 을 갈라 보여주는 데 쓴다.
     * 눌러 보기 전에 무엇을 하러 가는 곳인지 알 수 있어야 해서다.
     * 개수 자체는 화면에 내지 않는다. 2025년 9월 자료라 지금과 어긋난다.
     */
    private int courseCount;

    private int rentalCount; // 자치구 시설관리공단 주소. 시설 홈페이지가 없을 때 씀
}
