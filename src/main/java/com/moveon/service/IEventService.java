package com.moveon.service;

import com.moveon.dto.EventDTO;

import java.util.List;


/**
 * 탭③ 지역 스포츠 행사 (사용자 화면)
 *
 * 관리자가 넣고 고치는 쪽은 AdminEventService 로 따로 만든다.
 * 보여주는 일과 고치는 일은 성격이 달라서 한 파일에 넣으면 나중에 못 알아본다.
 */
public interface IEventService {

    /** 정렬 기준 : 가까운 순 */
    String SORT_NEAR = "near";

    /** 정렬 기준 : 마감 임박순 */
    String SORT_DEADLINE = "deadline";

    /**
     * 행사 목록
     *
     * @param sort 화면 토글에서 온 값. 아는 값이 아니면 가까운 순으로 본다.
     */
    List<EventDTO> getEventList(String sort, double lat, double lng) throws Exception;

    /**
     * 행사 한 건. 없거나 아직 공개 전이면 null.
     */
    EventDTO getEvent(int eventId, double lat, double lng) throws Exception;

}
