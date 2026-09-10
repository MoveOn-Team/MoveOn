package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 지역 스포츠 행사 (events 테이블)
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EventDTO {

    /** 아래는 사용자 화면(목록·상세)이 쓰는 값만 담았음. */

    private int eventId; // 행사 번호

    private String title; // 행사 이름

    private String eventType; // 종목 (마라톤 / 걷기 / 자전거)

    private String sigungu; // 자치구

    private String placeName; // 장소 이름

    private double lat; // 위도

    private double lng; // 경도

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate; // 행사 시작일

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate; // 행사 종료일. 하루짜리면 시작일과 같음

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applyStart; // 접수 시작일

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applyEnd; // 접수 마감일

    private String target; // 참가 대상. 없으면 제한 없음

    private String distances; // "5km,10km,하프" 처럼 쉼표로. 화면에서 칩으로 쪼갬

    private String feeText; // 참가비

    private String contact; // 문의처

    private String homepageUrl; // 대회 공식 홈페이지. '사이트로 이동' 버튼

    private LocalDateTime collectedAt; // 언제 넣었는지. 목록 위의 '갱신' 표시


    /** 아래는 관리자 화면에서만 씀 */

    private String status; // PENDING / PUBLISHED / REJECTED

    private String source; // 어떻게 찾았나. NAVER_SEARCH / MANUAL

    private String sourceUrl; // 어디서 확인했나. 대회 공식 홈페이지를 적는다


    /** 아래는 DB 테이블에 없는 조회 전용 값 (SQL이 계산해줌) */

    private double distanceKm; // 현위치에서의 거리

    private Integer dday; // 접수 마감까지 남은 날

    private String applyState; // 접수 배지 종류 OPEN / BEFORE / CLOSED / UNKNOWN

    private boolean outsideArea; // 서울·경기 밖. findPlace 가 세우고 등록 화면이 경고에 쓴다
}
