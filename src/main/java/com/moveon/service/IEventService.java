package com.moveon.service;

import com.moveon.dto.EventDTO;

import java.util.List;


/** 행사 탭 (사용자 화면). 넣고 고치는 쪽은 IAdminService */
public interface IEventService {

    String SORT_NEAR = "near";          // 가까운 순
    String SORT_DEADLINE = "deadline";  // 마감 임박순

    List<EventDTO> getEventList(String sort, double lat, double lng) throws Exception;

    /** 없거나 아직 공개 전이면 null */
    EventDTO getEvent(int eventId, double lat, double lng) throws Exception;

}
