package com.moveon.service;

import com.moveon.dto.AdminDTO;
import com.moveon.dto.EventDTO;

import java.util.List;


/**
 * 관리자 로그인과 행사 등록·검수
 *
 * 사용자에게 보여주는 EventService 와 나눠 두었다.
 * 저쪽은 PUBLISHED 만 읽고, 이쪽은 모든 상태를 읽고 고친다.
 */
public interface IAdminService {

    String PENDING = "PENDING";
    String PUBLISHED = "PUBLISHED";
    String REJECTED = "REJECTED";

    /** 로그인. 성공하면 관리자 정보, 실패하면 null */
    AdminDTO login(String loginId, String password) throws Exception;

    List<EventDTO> getEventList(String status) throws Exception;

    EventDTO getEvent(int eventId) throws Exception;

    /** 새 행사를 넣는다. 넣은 행사의 번호를 돌려준다 */
    int addEvent(EventDTO pDTO) throws Exception;

    int modifyEvent(EventDTO pDTO) throws Exception;

    /** 승인 / 반려. 아는 상태값이 아니면 아무것도 하지 않는다 */
    int changeStatus(int eventId, String status, int adminId) throws Exception;

}
