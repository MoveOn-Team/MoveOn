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

    // =====================================================================
    // 시설 손보기
    //
    // 공공데이터에는 시설의 신청 주소가 거의 안 들어 있어 사람이 채워야 한다.
    // 종목도 마찬가지다. 배드민턴장인데 배드민턴이 안 붙어 있던 곳이 44곳이었다.
    // =====================================================================

    List<FacilityDTO> getFacilityList(String gu, String keyword, String filter) throws Exception;

    FacilityDTO getFacility(int facilityId) throws Exception;

    /** 종목 스물두 개와 이 시설의 연결 상태. 화면이 체크박스를 그리는 데 쓴다 */
    List<Map<String, Object>> getFacilitySports(int facilityId) throws Exception;

    List<String> getGuList() throws Exception;

    /**
     * 주소와 종목을 한 번에 저장한다.
     *
     * @param sportIds    체크된 종목번호
     * @param reserveUrls 종목번호별 예약주소. 값이 없는 종목은 안 담겨 있어도 된다
     */
    void modifyFacility(int facilityId, String homepageUrl, String rentalUrl,
                        List<Integer> sportIds, Map<Integer, String> reserveUrls) throws Exception;

}
