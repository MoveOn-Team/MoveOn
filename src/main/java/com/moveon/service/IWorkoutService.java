package com.moveon.service;

import com.moveon.dto.*;

import java.util.List;

/**
 * 즉시 운동하기 — 시설에서 / 야외에서 / 집에서
 */
public interface IWorkoutService {

    /** 종목별 가까운 시설 */
    List<FacilityDTO> getFacilities(int sportId, double lat, double lng) throws Exception;

    /** 가까운 야외 코스. courseType 은 WALK 평지 / HIKE 산길 */
    List<CourseDTO> getCourses(String courseType, double lat, double lng) throws Exception;

    FacilityDTO getFacility(int facilityId, int sportId, double lat, double lng) throws Exception;

    /** 시설에서 여는 해당 종목 강좌. 회원 나이에 맞는 것을 위로 올린다 */
    List<ProgramDTO> getPrograms(int userId, int facilityId, int sportId) throws Exception;

    CourseDTO getCourse(int courseId, double lat, double lng) throws Exception;

    /** 코스를 이루는 지점들. 지도에 선으로 그리는 데 쓴다 */
    List<CoursePointDTO> getCoursePoints(int courseId) throws Exception;

    /** 화면 위쪽 종목 단추. category 는 FACILITY / OUTDOOR / HOME */
    List<SportDTO> getSports(String category) throws Exception;

    HomeWorkoutPlanDTO getHomeWorkoutPlan(int userId, String intensity, int targetMin) throws Exception;

}
