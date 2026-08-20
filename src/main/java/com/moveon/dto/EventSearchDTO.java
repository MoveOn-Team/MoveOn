package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;


/**
 * 관리자 화면 검색 결과 한 줄
 *
 * DB 테이블이 없다. 네이버 검색 결과를 화면에 보여주기 위해서만 쓰고 저장하지 않는다.
 * 저장하면 작년 대회 후기와 광고가 수백 건 쌓여 검수 화면 자체를 못 쓰게 된다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EventSearchDTO {

    /** 글 제목에서 뽑아낸 대회 이름 */
    private String name;

    /**
     * 몇 개의 글에서 언급됐는지. 0 이면 세지 않았다는 뜻이다.
     *
     * Gemini 로 이름을 뽑을 때는 세지 않는다.
     * 잡음을 걸러 주고 같은 대회를 합쳐 주므로 셀 이유가 없고,
     * 이름이 짧으면 다른 대회 제목까지 먹어 엉뚱한 값이 나오기 때문이다.
     *
     * Gemini 를 못 쓸 때만 이 숫자로 문장 조각을 걸러낸다.
     */
    private int mentions;

    /**
     * 어디서 열리는지. Gemini 가 검색 요약을 읽고 판단한다.
     *
     * 이름만 보고 거르면 안 된다.
     * '2026 인사이더런' 처럼 이름에 지역이 없는 대회가 많고,
     * 반대로 '아식스 서울신문 고프리런' 은 주최사 이름의 '서울' 때문에
     * 서울 대회로 잘못 통과한다.
     */
    private String region;

    /** 이미 등록된 대회인지. 같은 이름이 DB 에 있으면 화면에서 흐리게 보여준다 */
    private boolean registered;
}
