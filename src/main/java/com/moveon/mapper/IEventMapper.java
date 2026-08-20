package com.moveon.mapper;

import com.moveon.dto.EventDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 탭③ 지역 스포츠 행사
 *
 * 여기 메서드 이름은 EventMapper.xml 의 id 와 글자 하나까지 같아야 한다.
 * 다르면 뜨는 오류가 "Invalid bound statement (not found)" 인데
 * 무엇이 어긋났는지는 안 알려주므로 이름부터 확인하는 편이 빠르다.
 */
@Mapper
public interface IEventMapper {

    /**
     * 행사 목록
     *
     * @param sort "near" 가까운 순 / "deadline" 마감 임박순
     * @param lat  현위치 위도
     * @param lng  현위치 경도
     */
    List<EventDTO> getEventList(@Param("sort") String sort,
                                @Param("lat") double lat,
                                @Param("lng") double lng) throws Exception;

    /**
     * 행사 한 건
     *
     * 거리를 함께 계산해야 상세 화면에서도 '현위치에서 5km' 를 보여줄 수 있다.
     * 그래서 목록과 같은 좌표를 받는다.
     */
    EventDTO getEvent(@Param("eventId") int eventId,
                      @Param("lat") double lat,
                      @Param("lng") double lng) throws Exception;

}
