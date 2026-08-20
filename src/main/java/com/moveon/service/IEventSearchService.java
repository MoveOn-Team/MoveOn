package com.moveon.service;

import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;

import java.util.List;


/**
 * 관리자가 행사를 찾을 때 도와주는 일들
 *
 * 스케줄러로 돌지 않는다. 관리자가 버튼을 눌렀을 때만 밖에 물어본다.
 * 결과를 DB 에 저장하지도 않는다. 화면에 보여주고 끝이다.
 */
public interface IEventSearchService {

    /**
     * 어떤 대회가 있는지 찾는다.
     *
     * 네이버 블로그·뉴스에서 제목을 모아 대회 이름만 뽑고,
     * 같은 대회를 여러 글이 다룬 것을 하나로 묶어 언급이 많은 순으로 돌려준다.
     *
     * @param keyword 관리자가 넣은 검색어. 비우면 기본 검색어들로 훑는다
     * @param refresh true 면 보관해 둔 결과를 무시하고 새로 찾는다.
     *                Gemini 가 매번 똑같이 답하지 않아, 화면을 열 때마다 새로 찾으면
     *                목록에 뜨는 대회 수가 계속 달라진다. 그래서 잠깐 보관해 두고 쓴다.
     */
    List<EventSearchDTO> discover(String keyword, boolean refresh) throws Exception;

    /**
     * 대회 이름으로 공식 홈페이지를 찾는다. 못 찾으면 null.
     *
     * 웹문서 검색에서 블로그·커뮤니티를 걸러내고,
     * 같은 도메인이 여러 번 나온 것을 공식으로 본다.
     * 대회 사이트는 대회요강·코스안내처럼 여러 쪽이 함께 걸리기 때문이다.
     */
    String findSite(String eventName) throws Exception;

    /**
     * 장소 이름으로 좌표와 자치구를 얻는다. 못 찾으면 null.
     *
     * 돌려주는 값이 행사에 채워 넣을 것뿐이라 EventDTO 를 그대로 쓴다.
     * placeName · sido · sigungu · lat · lng 만 채워지고 나머지는 비어 있다.
     */
    EventDTO findPlace(String placeName) throws Exception;

    /**
     * 공식 사이트를 읽어 행사일·접수기간·참가비를 뽑아 채워 준다.
     *
     * 규칙으로 뽑는 것이라 완전하지 않다.
     * 날짜와 금액은 생김새가 뚜렷해서 잘 잡히지만,
     * 장소는 문장 속에 섞여 있어 사람이 보고 넣어야 한다.
     *
     * 그래서 채워 넣은 값도 반드시 관리자가 눈으로 확인한 뒤 저장한다.
     * 채우지 못한 칸은 건드리지 않고 비워 둔다.
     *
     * @param url       대회 공식 홈페이지
     * @param eventName 어느 대회인지. 한 페이지에 여러 대회가 적힌 곳이 있다
     * @return 뽑아낸 값만 담긴 EventDTO. 아무것도 못 뽑으면 빈 DTO
     */
    EventDTO readSite(String url, String eventName) throws Exception;

}
