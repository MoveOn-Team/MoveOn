package com.moveon.mapper;

import com.moveon.dto.SportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 종목 · 추천 관련 SQL
 *
 * 실제 SQL 은 src/main/resources/mapper/SportMapper.xml 에 있다.
 *
 * 점수 계산은 SQL 이 아니라 RecommendService 에서 한다.
 * 여기서는 계산에 필요한 재료(성향점수 · 참여율 · 최근접거리)만 가져온다.
 * 가중치나 정규화 방식이 바뀌어도 SQL 을 안 고치게 하기 위함이다.
 */
@Mapper
public interface ISportMapper {

    /**
     * 추천 대상 종목과 점수 재료를 한 번에 조회한다.
     *
     * @param userId 회원번호 (성향 4축 · 나이 · 성별을 여기서 읽는다)
     * @param lat    현위치 위도
     * @param lng    현위치 경도
     */
    java.util.List<SportDTO> getSportsForRecommend(@Param("userId") int userId,
                                                   @Param("lat") double lat,
                                                   @Param("lng") double lng) throws Exception;

}
