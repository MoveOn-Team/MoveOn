package com.moveon.mapper;

import com.moveon.dto.AdminDTO;
import com.moveon.dto.EventDTO;
import com.moveon.dto.FacilityDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 관리자 화면 (로그인 + 행사 등록·검수)
 *
 * 사용자 화면이 쓰는 IEventMapper 와 나눠 두었다.
 * 저쪽은 PUBLISHED 만 보고, 이쪽은 모든 상태를 본다.
 * 한 파일에 섞으면 "이 조회는 검수 전 것도 나오나" 를 매번 따져야 한다.
 */
@Mapper
public interface IAdminMapper {

    /** 로그인 아이디로 관리자 한 명. 없으면 null */
    AdminDTO getAdmin(@Param("loginId") String loginId) throws Exception;

    /**
     * 관리자용 행사 목록
     *
     * @param status 비우면 전부. PENDING / PUBLISHED / REJECTED 중 하나면 그것만
     */
    List<EventDTO> getAdminEventList(@Param("status") String status) throws Exception;

    /** 수정 화면에 띄울 행사 한 건. 상태와 상관없이 가져온다 */
    EventDTO getAdminEvent(@Param("eventId") int eventId) throws Exception;

    /**
     * 등록된 대회 이름 목록. 검색 결과에 '등록됨' 표시를 붙이는 데 쓴다.
     * 반려한 것은 빠져 있다.
     */
    List<String> getEventTitles() throws Exception;

    /**
     * 반려한 대회 이름 목록.
     *
     * 위 목록과 나눠 받는다. 한 덩어리로 받으면 반려한 대회도 '이미 등록됨' 으로
     * 회색 처리되어, 잘못 반려한 것을 검색으로 다시 찾을 수 없다.
     */
    List<String> getRejectedTitles() throws Exception;

    int insertEvent(EventDTO pDTO) throws Exception;

    /** 채워 넣은 칸만 바꾼다. 비운 칸 때문에 이미 있던 값이 지워지지 않게 한다 */
    int updateEvent(EventDTO pDTO) throws Exception;

    /**
     * 승인 / 반려
     *
     * PUBLISHED 로 바꿀 때만 누가 언제 승인했는지 남긴다.
     */
    int updateStatus(@Param("eventId") int eventId,
                     @Param("status") String status,
                     @Param("adminId") int adminId) throws Exception;

    /**
     * 아예 지운다. 되돌릴 수 없다.
     *
     * 반려는 '대회는 맞는데 지금 것이 아니다' 이고, 이건 '대회가 아니었다' 이다.
     * 검색이 잘못 물어 온 글 제목을 남겨 두면 이름 대조에 걸려 멀쩡한 대회를 가린다.
     */
    int deleteEvent(@Param("eventId") int eventId) throws Exception;

}
