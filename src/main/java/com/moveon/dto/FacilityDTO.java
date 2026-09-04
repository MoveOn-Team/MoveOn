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

    private Integer capacity; // 동시 수용 인원. 1,378곳 중 64곳만 값이 있어 화면에서는 있을 때만 보여줌

    /* -----------------------------------------------------------------
       회원을 밖으로 내보낼 주소 넷.

       갈 곳이 '배우러(수강신청)' 와 '빌리러(대관)' 두 갈래이고,
       각 갈래마다 시설이 가진 주소와 자치구가 가진 주소가 따로 있다.

                        배우러              빌리러
         시설 것        homepageUrl        rentalUrl
         자치구 것      districtUrl        districtRentalUrl

       시설 것을 먼저 쓰고 없으면 자치구 것으로 내려간다.
       고르는 규칙은 RecommendController.sportDetail 에 있다.
       ----------------------------------------------------------------- */

    private String homepageUrl; // 배우러 · 시설 — 그 시설 홈페이지 (facilities)

    private String rentalUrl; // 빌리러 · 시설 — 그 시설 대관 신청 페이지 (facilities)

    /** 아래는 DB 테이블에 없는 조회 전용 값 */

    private double distanceKm; // 현위치에서의 거리

    private String districtUrl; // 배우러 · 자치구 — 그 구 수강신청 (district_sites)

    private String districtRentalUrl; // 빌리러 · 자치구 — 그 구 대관 신청 (district_sites)

    /** 빌리러 · 서울시 — 공공서비스예약(yeyak.seoul.go.kr).
     *  위 넷과 달리 '시설 x 종목' 단위. 같은 복합시설이라도 테니스장만 예약 대상인 곳이 있음. */
    private String reserveUrl;

    /** 안내만 · 카카오 장소 페이지 (facilities.guide_url, 812곳)
     *  좌표를 맞출 때 저장해 둔 것. 전화·주소·운영시간이 실려 있어
     *  신청 창구가 없는 시설을 보낼 마지막 자리로 씀. */
    private String guideUrl;

    /**
     * 방문 접수만 받는 시설인지.
     *
     * 노인복지관·사회복지관이 여기 해당함. 강좌는 여는데 온라인 신청 창구가 없음.
     * ('회원증 지참하여 3층 사무실 내방 신청' — 예약 주소를 가진 강좌 행이 0건)
     * 이런 곳을 자치구 수강신청으로 보내면 눌러도 그 강좌가 없어 헛걸음이 됨.
     * 판단 근거는 FacilityMapper 의 visitOnly 조각에 있음.
     */
    private boolean visitOnly;

    /**
     * 이 시설이 이 종목으로 가진 강습의 수. 나이를 따지지 않은 값.
     *
     * '여기가 개방형 코트인가' 를 가리는 데 씀.
     * 0 이면 우리 자료에 강좌가 아예 없는 곳이라 근린공원 코트로 볼 수 있음.
     */
    private int courseCount;

    /**
     * 그중 회원 나이로 들을 수 있는 강습의 수.
     *
     * 목록 제목을 '가까운 시설' 로 할지 '지금 빌릴 수 있는 곳' 으로 할지 가르는 데 씀.
     * 가까운 세 곳이 전부 0 이면 이 회원에게 이 동네는 배우는 곳이 아니라 빌리는 곳임.
     *
     * courseCount 와 나누어 둔 까닭은 FacilityMapper.getNearbyFacilities 주석에 있음.
     */
    private int myCourseCount;
}
