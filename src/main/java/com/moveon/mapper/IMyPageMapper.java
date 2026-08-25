package com.moveon.mapper;

import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IMyPageMapper {

    /**
     * 내 정보 조회 (PK 기준)
     */
    UserProfileDTO selectUserProfileById(@Param("userId") Integer userId);

    /**
     * 회원 정보 수정 (이름, 키, 몸무게)
     */
    int updateUserProfile(UserProfileDTO userProfileDTO);

    /**
     * 운동 기록 추가 (다른 운동 기록하기 모달)
     */
    int insertWorkoutLog(WorkoutLogDTO workoutLogDTO);

    /**
     * 오늘 운동 완료 목록 조회 [추가]
     */
    List<WorkoutLogDTO> selectTodayWorkoutList(@Param("userId") Integer userId);





    // 이번 주 총 횟수 및 총 소요시간 (월~일 기준)
    Map<String, Object> selectThisWeekSummary(@Param("userId") Integer userId);

    // 지난 주 총 횟수
    int selectLastWeekCount(@Param("userId") Integer userId);

    // 이번 주 소모 칼로리
    int selectThisWeekCalories(@Param("userId") Integer userId);

    // 전체 누적 운동 횟수
    int selectTotalWorkoutCount(@Param("userId") Integer userId);

    // 많이 한 종목 Top 4 (종목명, 횟수)
    List<WorkoutReportDTO.SportStatDTO> selectTopSports(@Param("userId") Integer userId);

    // 가장 오래 한 운동 (종목명, 시간)
    Map<String, Object> selectMaxDurationWorkout(@Param("userId") Integer userId);

    // 첫 기록 날짜 (YYYY-MM-DD)
    String selectFirstRecordDate(@Param("userId") Integer userId);
}