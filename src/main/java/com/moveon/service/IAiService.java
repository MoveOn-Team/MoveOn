package com.moveon.service;

import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;

import java.util.List;
import java.util.Map;


/**
 * 글에서 행사 정보를 뽑아내는 일 (Gemini)
 * 키가 없거나 실패해도 예외를 던지지 않고 빈 값을 돌려준다.
 */
public interface IAiService {

    /** 쓸 수 있는 상태인지. 키가 없으면 false */
    boolean isReady();

    /**
     * 대회 홈페이지 글에서 행사 정보를 뽑음.
     *
     * @param eventName 한 페이지에 여러 대회가 적힌 곳이 있어 함께 넘김
     * @return 뽑아낸 값만 담긴 EventDTO. 실패하면 빈 DTO
     */
    EventDTO extractEvent(String pageText, String eventName);

    /** 글 제목들에서 대회 이름과 지역을 뽑음. 실패하면 빈 목록 */
    List<EventSearchDTO> extractNames(List<String> titles);

    /**
     * 검색 결과 중 그 대회의 공식 홈페이지를 고름.
     * 전용 홈페이지가 없는 대회도 많아 접수처·안내 페이지도 고르고 종류를 함께 알려 준다.
     *
     * @param candidates title 과 link 를 담은 검색 결과
     * @return "종류|주소" (OFFICIAL / APPLY / INFO). 쓸 만한 게 없으면 null
     */
    String pickSite(String eventName, List<Map<String, String>> candidates);

    /**
     * 같은 대회끼리 합치고 이미 DB 에 있는지도 함께 판단.
     * 부르기 전에 지역으로 걸러 목록을 줄여야 결과가 안정적이다.
     *
     * @return 합친 목록. 판단 못 했으면 null, 그때는 부른 쪽이 이름 대조로 대신한다
     */
    List<EventSearchDTO> mergeNames(List<EventSearchDTO> names,
                                    List<String> registered,
                                    List<String> rejected);

}
