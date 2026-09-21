package com.moveon.mapper;

import com.moveon.dto.SportDTO;
import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * '오늘' 과 '이번 주 월요일' 은 자바가 정해서 넘긴다.
 *
 * 전에는 CURDATE() 를 썼는데, DB 서버 시계가 사흘 뒤처져 있던 날
 * 연속 출석(자바 기준)에는 오늘 기록이 잡히고 오늘 목록(DB 기준)에는
 * 안 잡혔다. 기준이 둘이면 어느 쪽이 맞는지 화면만 보고는 알 수 없다.
 */
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
     * 홈트를 끝냈을 때 남기는 기록
     *
     * 손으로 넣는 기록과 나눠 두었다. 종목은 '홈트' 로 못 박고,
     * 강도는 계획이 들고 있는 LIGHT/MODERATE/HARD 를 그대로 쓰며,
     * source 는 HOME_WORKOUT 이라 리포트에서 손으로 적은 것과 구분된다.
     */
    int insertHomeWorkoutLog(@Param("userId") int userId,
                             @Param("today") java.time.LocalDate today,
                             @Param("durationMin") int durationMin,
                             @Param("intensity") String intensity,
                             @Param("caloriesKcal") int caloriesKcal,
                             @Param("memo") String memo);

    /**
     * 오늘 운동 완료 목록 조회 [추가]
     */
    List<WorkoutLogDTO> selectTodayWorkoutList(@Param("userId") Integer userId,
                                               @Param("today") java.time.LocalDate today);





    // 이번 주 총 횟수 및 총 소요시간 (월~일 기준)
    Map<String, Object> selectThisWeekSummary(@Param("userId") Integer userId,
                                              @Param("monday") java.time.LocalDate monday);

    // 지난 주 총 횟수
    int selectLastWeekCount(@Param("userId") Integer userId,
                            @Param("monday") java.time.LocalDate monday);

    // 이번 주 소모 칼로리
    int selectThisWeekCalories(@Param("userId") Integer userId,
                               @Param("monday") java.time.LocalDate monday);

    // 전체 누적 운동 횟수
    int selectTotalWorkoutCount(@Param("userId") Integer userId);

    // 많이 한 종목 Top 4 (종목명, 횟수)
    List<WorkoutReportDTO.SportStatDTO> selectTopSports(@Param("userId") Integer userId);

    // 가장 오래 한 운동 (종목명, 시간)
    Map<String, Object> selectMaxDurationWorkout(@Param("userId") Integer userId);

    // 첫 기록 날짜 (YYYY-MM-DD)
    String selectFirstRecordDate(@Param("userId") Integer userId);

    /**
     * 최근 몇 주간 주별 운동 횟수.
     *
     * 기록이 없는 주는 줄이 안 나온다. 빈 주도 막대 자리를 잡아야 하므로
     * 서비스에서 주 목록을 만들어 두고 여기 값을 얹는다.
     *
     * @param from 첫 주의 월요일
     * @return monday(월요일 날짜) · cnt(그 주 횟수)
     */
    List<Map<String, Object>> selectWeeklyCounts(@Param("userId") Integer userId,
                                                 @Param("from") java.time.LocalDate from);

    /** 손으로 적을 수 있는 종목. 홈트는 자동으로 기록되므로 뺀다 */
    List<SportDTO> selectRecordableSports();

    /** 운동한 날짜를 최근 순으로. 하루에 두 번 해도 하루로 접는다 */
    List<java.time.LocalDate> selectExerciseDates(@Param("userId") Integer userId);
}