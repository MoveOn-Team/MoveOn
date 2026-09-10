package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * 관리자 화면 검색 결과
 * DB 테이블이 없고 네이버 검색 결과를 화면에 보여주기 위해서만 쓰고 저장하지 않음.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EventSearchDTO {

    private String name; // 대회 이름

    private String region; // 어디서 열리는지. Gemini 가 검색 요약을 읽고 판단함

    private String eventDate; // 대회일. YYYY-MM-DD 또는 YYYY-MM. 모르면 null

    private boolean pastEdition; // 지난 회차로 보임. 빼지 않고 목록 아래로만 내린다

    private boolean registered; // 이미 등록된 대회. 화면에서 흐리게 보여줌

    private boolean rejected; // 반려한 대회. '반려함' 을 붙이고 등록 단추는 남겨 둠
}
