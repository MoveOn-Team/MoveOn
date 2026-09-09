package com.moveon.service;

import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;

import java.util.List;

public interface IMyPageService {

    UserProfileDTO getUserProfile(Integer userId);

    boolean updateUserProfile(UserProfileDTO userProfileDTO);

    int insertWorkoutLog(WorkoutLogDTO workoutLogDTO) throws Exception;

    /** 홈트를 끝냈을 때 남기는 기록. 종목은 '홈트', 출처는 HOME_WORKOUT 으로 고정 */
    int addHomeWorkoutLog(int userId, int durationMin, String intensity,
                          int caloriesKcal, String memo) throws Exception;

    List<WorkoutLogDTO> getTodayWorkoutList(Integer userId) throws Exception;

    WorkoutReportDTO getWorkoutReport(Integer userId) throws Exception;
}