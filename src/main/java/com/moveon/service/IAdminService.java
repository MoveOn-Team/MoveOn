package com.moveon.service;

import com.moveon.dto.AdminDTO;
import com.moveon.dto.EventDTO;
import com.moveon.dto.FacilityDTO;

import java.util.List;
import java.util.Map;


/**
 * 관리자 로그인과 행사 등록·검수
 *
 * 사용자에게 보여주는 EventService 와 나눠 두었음.
 * 저쪽은 PUBLISHED 만 읽고, 이쪽은 모든 상태를 읽고 고침.
 */
public interface IAdminService {

    String PENDING = "PENDING";
    String PUBLISHED = "PUBLISHED";
    String REJECTED = "REJECTED";

    /** 로그인. 성공하면 관리자 정보, 실패하면 null */
    AdminDTO login(String loginId, String password) throws Exception;

    List<EventDTO> getAdminEventList(String status) throws Exception;

    EventDTO getAdminEvent(int eventId) throws Exception;

    /** 새 행사를 넣음. 넣은 행사의 번호를 돌려줌 */
    int addEvent(EventDTO pDTO) throws Exception;

    int modifyEvent(EventDTO pDTO) throws Exception;

    /** 승인 / 반려. 아는 상태값이 아니면 아무것도 하지 않음 */
    int changeStatus(int eventId, String status, int adminId) throws Exception;

    /**
     * 행사를 완전히 지움.
     * @return 지운 줄 수. 없는 번호면 0
     */
    int removeEvent(int eventId) throws Exception;

}
