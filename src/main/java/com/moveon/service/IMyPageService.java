package com.moveon.service;

import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;

import java.util.List;

public interface IMyPageService {

    UserProfileDTO getUserProfile(Integer userId);

    boolean updateUserProfile(UserProfileDTO userProfileDTO);

    int insertWorkoutLog(WorkoutLogDTO workoutLogDTO) throws Exception;

    List<WorkoutLogDTO> getTodayWorkoutList(Integer userId) throws Exception;

    WorkoutReportDTO getWorkoutReport(Integer userId) throws Exception;
}