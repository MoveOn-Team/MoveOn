package com.moveon.mapper;

import com.moveon.dto.EventDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 행사 탭
 */
@Mapper
public interface IEventMapper {

    /** @param sort "near" 가까운 순 / "deadline" 마감 임박순. lat·lng 는 현위치 */
    List<EventDTO> getEventList(@Param("sort") String sort,
                                @Param("lat") double lat,
                                @Param("lng") double lng) throws Exception;

    /** 상세도 '현위치에서 5km' 를 보여줘야 해서 목록과 같은 좌표를 받는다 */
    EventDTO getEvent(@Param("eventId") int eventId,
                      @Param("lat") double lat,
                      @Param("lng") double lng) throws Exception;

}
