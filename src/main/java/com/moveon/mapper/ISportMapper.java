package com.moveon.mapper;

import com.moveon.dto.SportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 점수 계산은 SQL이 아니라 RecommendService에서 함.
 * 여기서는 계산에 필요한 재료(성향점수 · 참여율 · 최근접거리)만 가져옴.
 * 가중치나 정규화 방식이 바뀌어도 SQL 을 안 고치게 하기 위함.
 *
 * DTO에 담아 하나로 넘길 수도 있었는데 안 한 이유는,
 * SportDTO 에 lat·lng 같은 조회 조건이 들어가면 종목 정보(결과)와 섞이기 때문임.
 * FacilityDTO 는 더 심해서, lat·lng 가 이미 시설의 좌표로 쓰이고 있어 이름까지 겹침.
 */
@Mapper
public interface ISportMapper {

    /**
     * 추천 대상 종목과 점수 재료를 한 번에 조회함.
     *
     * @param companion    동반자 성향 (ALONE / PAIR / GROUP)
     * @param competition  승부 성향   (OWN_PACE / ANY / WIN)
     * @param place        장소 성향   (INDOOR / ANY / OUTDOOR)
     * @param intensity    강도 성향   (LIGHT / MODERATE / HARD)
     * @param gender       성별 (M / F). 참여율 통계를 고를 때 씀
     * @param ageGroup     연령대 ('30대', '70대이상'). 자바에서 만들어 넘김
     * @param lat          현위치 위도
     * @param lng          현위치 경도
     */
    List<SportDTO> getSportsForRecommend(@Param("companion") String companion,
                                         @Param("competition") String competition,
                                         @Param("place") String place,
                                         @Param("intensity") String intensity,
                                         @Param("gender") String gender,
                                         @Param("ageGroup") String ageGroup,
                                         @Param("lat") double lat,
                                         @Param("lng") double lng) throws Exception;

    /**
     * 갈래별 종목 목록. 즉시운동 탭의 종목 단추를 만드는 데 씀.
     *
     * @param category FACILITY 시설에서 / OUTDOOR 야외에서 / HOME 집에서
     */
    List<SportDTO> getSportsByCategory(@Param("category") String category) throws Exception;
}
