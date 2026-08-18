package com.moveon.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


/**
 * 서울시 공공서비스예약 원본 한 건 (reservation_raw)
 *
 * API 응답을 그대로 담는 그릇.
 * 화면에 쓰는 값은 RentalDTO 가 따로 있고, 이건 적재 전용.
 */
@Getter
@Setter
public class ReservationDTO {

    private String svcId;      // 서비스 ID. 같은 장소라도 월마다 달라짐

    private String minClass;   // 소분류 (테니스장 등). 종목 매칭에 쓴다

    private String svcStat;    // 예약 상태 (접수중 / 예약마감)

    private String svcName;    // 서비스명

    private String payYn;      // 유료 / 무료

    private String placeName;  // 장소명

    private String useTarget;  // 이용 대상

    private String svcUrl;     // 예약 페이지 주소

    private BigDecimal lng;    // API 의 X

    private BigDecimal lat;    // API 의 Y

    private String openBgn;

    private String openEnd;

    private String rcptBgn;    // 접수 시작

    private String rcptEnd;    // 접수 종료. 지난 건을 거르는 기준이다

    private String areaName;

    private String telNo;

    private String vMin;

    private String vMax;

}
