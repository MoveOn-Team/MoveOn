package com.moveon.mapper;

import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.RentalDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 맞춤 운동 추천 상세와 즉시운동 탭에서 함께 씀.
 */
@Mapper
public interface IFacilityMapper {

    /**
     * 시설 하나
     *
     * 종목번호도 함께 받음. 예약 링크가 '시설 x 종목' 단위이기 때문.
     * 같은 복합시설이라도 테니스장만 예약 대상인 경우가 있음.
     */
    FacilityDTO getFacility(@Param("facilityId") int facilityId,
                            @Param("sportId") int sportId,
                            @Param("lat") double lat,
                            @Param("lng") double lng) throws Exception;

    /**
     * 해당 종목을 할 수 있는 가장 가까운 시설 목록
     *
     * @param sportId 종목번호
     * @param lat     현위치 위도
     * @param lng     현위치 경도
     * @param limit   몇 곳까지 가져올지 (맞춤 추천 상세는 3곳)
     * @param ageBand 회원 연령대 (CHILD / TEEN / ADULT / SENIOR)
     *                my_course_count 를 셀 때만 씀. null 이면 나이를 따지지 않는다.
     * @param bookableOnly 신청·예약 창구가 있는 곳만 볼지.
     *                     추천 탭은 true, 즉시운동 탭은 false.
     *                     동네 공원 코트는 '지금 나가서 할 곳' 이라 즉시운동 탭이 맡는다.
     */
    List<FacilityDTO> getNearbyFacilities(@Param("sportId") int sportId,
                                          @Param("lat") double lat,
                                          @Param("lng") double lng,
                                          @Param("limit") int limit,
                                          @Param("ageBand") String ageBand,
                                          @Param("bookableOnly") boolean bookableOnly) throws Exception;

    /**
     * 특정 시설에서 여는 해당 종목 강좌 목록
     *
     * @param facilityId 시설번호
     * @param sportId    종목번호
     * @param ageBand    회원 연령대 (CHILD / TEEN / ADULT / SENIOR)
     *                   나이에 맞는 강좌를 위로 올리는 데 씀.
     */
    List<ProgramDTO> getPrograms(@Param("facilityId") int facilityId,
                                 @Param("sportId") int sportId,
                                 @Param("ageBand") String ageBand) throws Exception;

    /**
     * 대관 가능한 가까운 곳 (서울시 공공서비스예약)
     *
     * 강좌가 없는 종목에서 대신 보여준다.
     */
    List<RentalDTO> getNearbyRentals(@Param("sportId") int sportId,
                                     @Param("lat") double lat,
                                     @Param("lng") double lng,
                                     @Param("limit") int limit) throws Exception;

    /** 축구/풋살은 풋살장·축구장 두 종류를 함께 본다 */
    List<RentalDTO> getNearbyFootballRentals(@Param("lat") double lat,
                                             @Param("lng") double lng,
                                             @Param("limit") int limit) throws Exception;

}
