package com.moveon.service;

import com.moveon.dto.SportDTO;
import com.moveon.dto.StreakDTO;
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

    /** 기록 화면에서 고를 수 있는 종목 */
    List<SportDTO> getRecordableSports();

    /** 연속 출석. 기록이 없으면 0일과 빈 일주일이 돌아온다 */
    StreakDTO getStreak(Integer userId);

    /**
     * 리포트의 AI 코치 글.
     *
     * 기록을 남길 때 미리 만들어 두므로 대개 바로 돌아온다.
     * 만들어 둔 것이 없으면 그 자리에서 만든다. 못 만들면 null.
     */
    String getReportNote(Integer userId) throws Exception;

    /** 코치 글을 쓸 만큼 기록이 쌓였는지 */
    boolean canWriteNote(WorkoutReportDTO report);
}