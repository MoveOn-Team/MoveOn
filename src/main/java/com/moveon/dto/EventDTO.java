package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 지역 스포츠 행사 (events 테이블)
 *
 * 다른 탭과 달리 이 자료는 공공 API 로 모으지 않는다.
 * 스포츠 행사가 들어 있는 공공데이터를 다섯 군데 전수 조사했지만
 * 지금 서울에서 열리는 것으로 얻어지는 건 4건뿐이었다.
 * 그래서 관리자가 검색으로 찾아 직접 넣고, 넣은 것 중 확인을 마친 것만 화면에 나간다.
 *
 * 아래는 사용자 화면(목록·상세)이 쓰는 값만 담았다.
 * status · source · approved_by 같은 관리자용 컬럼은 관리자 화면을 만들 때 더한다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EventDTO {

    private int eventId;

    private String title; // 행사 이름

    private String eventType; // 종목 (마라톤 / 걷기 / 자전거)

    private String sido; // 시·도. 지금은 서울만 다룬다

    private String sigungu; // 자치구

    private String placeName; // 장소 이름

    private double lat; // 위도

    private double lng; // 경도

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate; // 행사 시작일

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate; // 행사 종료일. 하루짜리면 시작일과 같다

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applyStart; // 접수 시작일

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate applyEnd; // 접수 마감일

    private String target; // 참가 대상. 없으면 제한 없음

    private String distances; // "5km,10km,하프" 처럼 쉼표로. 화면에서 칩으로 쪼갠다

    /**
     * 참가비. 숫자가 아니라 문자열이다.
     * "전종목 50,000원", "하프 100,000원 / 10km 70,000원" 처럼
     * 종목마다 다른 경우가 많아 원문을 그대로 보여주는 편이 정확하다.
     */
    private String feeText;

    private String contact; // 문의처

    private String homepageUrl; // 대회 공식 홈페이지. '사이트로 이동' 버튼

    private LocalDateTime collectedAt; // 언제 넣었는지. 목록 위의 '갱신' 표시

    /** 아래는 관리자 화면에서만 쓴다. 사용자 화면 조회에는 담지 않는다. */

    /**
     * PENDING   아직 덜 채운 초안. 대회 사이트를 열어 값을 찾다 보면 한 번에 못 끝낸다
     * PUBLISHED 다 채우고 원본을 확인했다. 이것만 사용자 화면에 나간다
     * REJECTED  알고 보니 작년 것이거나 취소된 대회
     */
    private String status;

    private String source; // 어떻게 찾았나. NAVER_SEARCH / MANUAL

    private String sourceUrl; // 어디서 확인했나. 대회 공식 홈페이지를 적는다

    private Integer approvedBy; // 승인한 관리자 번호

    private LocalDateTime approvedAt;

    /** 아래는 DB 테이블에 없는 조회 전용 값 */

    private double distanceKm; // 현위치에서의 거리. SQL 이 계산해 넣는다

    /**
     * 접수 마감까지 남은 날. SQL 의 DATEDIFF 결과다.
     *   양수  아직 접수 중       -> '접수 D-4'
     *   0     오늘 마감
     *   음수  이미 마감
     *   null  접수 기간을 모름 (홈페이지에 안 적힌 대회가 있다)
     * 세 경우를 화면에서 구분해야 해서 int 가 아니라 Integer 다.
     */
    private Integer dday;

    /**
     * 접수 상태. SQL 이 계산해 넣는다.
     *
     * 마라톤은 '선착순 마감' 이 많아 접수 마감일이 아예 없는 대회가 흔하다.
     * 그래서 dday 만으로는 화면에 무엇을 보여줄지 정할 수 없다.
     *
     *   OPEN     접수 중. dday 가 있으면 'D-4', 없으면 '선착순'
     *   BEFORE   아직 접수 전. 'M/D 접수 시작' 으로 알려 준다
     *   CLOSED   접수 끝남. 목록 맨 아래로 내린다
     *   UNKNOWN  접수 정보를 모름. 배지를 만들지 않는다
     */
    private String applyState;
}
