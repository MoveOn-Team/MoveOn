package com.moveon.service;

import com.moveon.dto.*;

import java.util.List;

/**
 * 즉시 운동하기
 *
 * 세 갈래로 나뉜다.
 *   시설에서  가까운 공공 체육시설            facilities
 *   야외에서  가까운 걷기·등산 코스           courses
 *   집에서    맨몸 운동 계획                 home_exercises  (아직 자료 없음)
 */
public interface IWorkoutService {

    /**
     * 종목별 가까운 시설
     *
     * @param sportId 종목번호
     * @param lat     현위치 위도
     * @param lng     현위치 경도
     */
    List<FacilityDTO> getFacilities(int sportId, double lat, double lng) throws Exception;

    /**
     * 가까운 야외 코스
     *
     * @param courseType WALK 평지 / HIKE 산길
     */
    List<CourseDTO> getCourses(String courseType, double lat, double lng) throws Exception;

    /** 시설 하나 */
    FacilityDTO getFacility(int facilityId, int sportId, double lat, double lng) throws Exception;

    /**
     * 시설에서 여는 해당 종목 강좌
     *
     * 회원 나이에 맞는 강좌를 위로 올리므로 회원번호도 받는다.
     * 청소년 강좌가 더 싸서, 요금 순으로만 줄 세우면 성인에게 청소년 강좌가 먼저 뜬다.
     */
    List<ProgramDTO> getPrograms(int userId, int facilityId, int sportId) throws Exception;

    /** 코스 하나 */
    CourseDTO getCourse(int courseId, double lat, double lng) throws Exception;

    /** 코스를 이루는 지점들. 지도에 선으로 그리는 데 쓴다. */
    List<CoursePointDTO> getCoursePoints(int courseId) throws Exception;

    /**
     * 갈래별 종목 목록. 화면 위쪽의 종목 단추를 만드는 데 쓴다.
     *
     * 화면에 종목 이름을 박아 두면 표에 종목이 늘어도 화면이 모른다.
     * 실제로 지금 표에는 당구·골프가 있는데 화면에는 없다.
     *
     * @param category FACILITY 시설에서 / OUTDOOR 야외에서 / HOME 집에서
     */
    List<SportDTO> getSports(String category) throws Exception;

    HomeWorkoutPlanDTO getHomeWorkoutPlan(int userId, String intensity, int targetMin) throws Exception;

}
