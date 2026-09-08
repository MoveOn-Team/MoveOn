package com.moveon.service;

import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;

import java.util.List;


/**
 * 관리자가 행사를 찾을 때 도와주는 일들
 */
public interface IEventSearchService {

    /**
     * 어떤 대회가 있는지 찾음.
     *
     * @param keyword 비우면 기본 검색어들로 훑음
     * @param refresh true 면 보관해 둔 결과를 무시하고 새로 찾음
     */
    List<EventSearchDTO> discover(String keyword, boolean refresh) throws Exception;

    /** 대회 이름으로 공식 홈페이지를 찾음. "종류|주소" 형태. 못 찾으면 null */
    String findSite(String eventName) throws Exception;

    /** 장소 이름으로 좌표를 얻음. placeName · sigungu · lat · lng 만 채워짐. 못 찾으면 null */
    EventDTO findPlace(String placeName) throws Exception;

    /**
     * 공식 사이트를 읽어 행사일·접수기간·참가비를 뽑아 채워 줌.
     * 기계가 읽은 값이라 관리자가 눈으로 확인한 뒤 저장한다.
     *
     * @param eventName 한 페이지에 여러 대회가 적힌 곳이 있어 함께 넘김
     * @return 뽑아낸 값만 담긴 EventDTO. 아무것도 못 뽑으면 빈 DTO
     */
    EventDTO readSite(String url, String eventName) throws Exception;

}
