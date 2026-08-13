package com.moveon.mapper;

import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 시설 · 강좌 조회 SQL
 *
 * 맞춤추천 종목 상세(SC-011)와 즉시운동 탭(SC-020~021)에서 함께 쓴다.
 */
@Mapper
public interface IFacilityMapper {

    /**
     * 해당 종목을 할 수 있는 가장 가까운 시설 목록
     *
     * @param sportId 종목번호
     * @param lat     현위치 위도
     * @param lng     현위치 경도
     * @param limit   몇 곳까지 가져올지 (맞춤추천 상세는 3곳)
     */
    List<FacilityDTO> getNearbyFacilities(@Param("sportId") int sportId,
                                          @Param("lat") double lat,
                                          @Param("lng") double lng,
                                          @Param("limit") int limit) throws Exception;

    /**
     * 특정 시설에서 여는 해당 종목 강좌 목록
     */
    List<ProgramDTO> getPrograms(@Param("facilityId") int facilityId,
                                 @Param("sportId") int sportId) throws Exception;

}
