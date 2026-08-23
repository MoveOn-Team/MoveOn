package com.moveon.mapper;

import com.moveon.dto.CourseDTO;
import com.moveon.dto.CoursePointDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 야외 코스 (평지 · 산길)
 *
 * 즉시운동 탭의 '야외에서' 가 쓴다.
 */
@Mapper
public interface ICourseMapper {

    /**
     * 현위치에서 가까운 코스 목록
     *
     * @param courseType WALK 평지 / HIKE 산길. 비우면 둘 다
     * @param lat        현위치 위도
     * @param lng        현위치 경도
     * @param limit      몇 개까지 가져올지
     */
    List<CourseDTO> getNearbyCourses(@Param("courseType") String courseType,
                                     @Param("lat") double lat,
                                     @Param("lng") double lng,
                                     @Param("limit") int limit) throws Exception;

    /**
     * 코스 하나
     *
     * 거리를 함께 계산하므로 현위치도 받는다.
     * 목록에서 본 거리와 상세에서 본 거리가 다르면 안 되기 때문이다.
     */
    CourseDTO getCourse(@Param("courseId") int courseId,
                        @Param("lat") double lat,
                        @Param("lng") double lng) throws Exception;

    /** 코스를 이루는 지점들. 순번 순으로 준다. */
    List<CoursePointDTO> getCoursePoints(@Param("courseId") int courseId) throws Exception;

}
