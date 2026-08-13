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

    private double lat;

    private double lng;

    private String phone;

    private String homepageUrl;

    private String guideUrl; // 안내 페이지. 운영시간·요금이 바뀔 수 있어 원본으로 보낸다

    private Integer capacity; // 동시 수용 인원

    // 아래는 DB 테이블에 없는 조회 전용 값(ALIAS)

    private double distanceKm; // 현위치에서의 거리

}
