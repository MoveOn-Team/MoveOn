package com.moveon.service.impl;

import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;
import com.moveon.mapper.IMyPageMapper;
import com.moveon.service.IMyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService implements IMyPageService {

    private final IMyPageMapper myPageMapper;

    @Override
    public UserProfileDTO getUserProfile(Integer userId) {
        if (userId == null) {
            return null;
        }
        return myPageMapper.selectUserProfileById(userId);
    }

    @Transactional
    @Override
    public boolean updateUserProfile(UserProfileDTO userProfileDTO) {
        if (userProfileDTO == null || userProfileDTO.getUserId() == null) {
            return false;
        }

        int result = myPageMapper.updateUserProfile(userProfileDTO);
        return result > 0;
    }

    @Override
    public int insertWorkoutLog(WorkoutLogDTO workoutLogDTO) throws Exception {
        log.info(this.getClass().getName() + ".insertWorkoutLog Start!");

        int res = myPageMapper.insertWorkoutLog(workoutLogDTO);

        log.info(this.getClass().getName() + ".insertWorkoutLog End!");
        return res;
    }

    /**
     * 오늘 운동 완료 목록 조회 [추가]
     */
    @Override
    public List<WorkoutLogDTO> getTodayWorkoutList(Integer userId) throws Exception {
        log.info(this.getClass().getName() + ".getTodayWorkoutList Start!");
        return myPageMapper.selectTodayWorkoutList(userId);
    }

    @Override
    public WorkoutReportDTO getWorkoutReport(Integer userId) throws Exception {
        WorkoutReportDTO report = new WorkoutReportDTO();

        // 1. 이번 주 날짜 범위 계산 (월요일 ~ 일요일)
        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        report.setWeekRangeText(monday.format(formatter) + " ~ " + sunday.format(formatter));

        // 2. 이번 주 요약 데이터 조회
        Map<String, Object> thisWeekSummary = myPageMapper.selectThisWeekSummary(userId);
        int thisWeekCount = 0;
        int thisWeekDurationMin = 0;

        if (thisWeekSummary != null) {
            thisWeekCount = ((Number) thisWeekSummary.getOrDefault("weekCount", 0)).intValue();
            thisWeekDurationMin = ((Number) thisWeekSummary.getOrDefault("weekDuration", 0)).intValue();
        }
        report.setThisWeekCount(thisWeekCount);
        report.setThisWeekDurationMin(thisWeekDurationMin);

        // 3. 지난주 대비 증감 횟수
        int lastWeekCount = myPageMapper.selectLastWeekCount(userId);
        report.setWeekDiffCount(thisWeekCount - lastWeekCount);

        // 4. 주요 요약 통계 (칼로리, 연속출석, 전체횟수)
        report.setTotalCalories(myPageMapper.selectThisWeekCalories(userId));
        report.setTotalWorkoutCount(myPageMapper.selectTotalWorkoutCount(userId));

        // 연속 출석일 계산 (추후 출석 로직 연동, 기본 0일 처리)
        report.setCurrentStreak(0);

        // 5. 많이 한 종목 Top 4 & 비율 계산
        List<WorkoutReportDTO.SportStatDTO> topSports = myPageMapper.selectTopSports(userId);
        int totalCount = report.getTotalWorkoutCount();

        if (topSports != null && !topSports.isEmpty() && totalCount > 0) {
            for (WorkoutReportDTO.SportStatDTO sport : topSports) {
                int pct = (int) Math.round(((double) sport.getCount() / totalCount) * 100);
                sport.setPercentage(pct);
            }
        }
        report.setTopSports(topSports);

        // 6. 기록 갱신 영역 (가장 오래 한 운동, 첫 기록)
        Map<String, Object> maxWorkout = myPageMapper.selectMaxDurationWorkout(userId);
        if (maxWorkout != null && !maxWorkout.isEmpty()) {
            report.setMaxDurationSportName((String) maxWorkout.get("sportName"));
            report.setMaxDurationMin(((Number) maxWorkout.get("maxDuration")).intValue());
        } else {
            report.setMaxDurationSportName("-");
            report.setMaxDurationMin(0);
        }

        String firstDate = myPageMapper.selectFirstRecordDate(userId);
        report.setFirstRecordDate(firstDate != null ? firstDate : "기록 없음");
        report.setMaxStreakDays(0); // 최장 연속 출석 기본값

        return report;
    }

    /**
     * 홈트 완료 기록.
     *
     * 회원이 따로 적지 않아도 리포트에 올라가야 한다.
     * 운동을 다 하고 나서 또 손으로 적으라고 하면 아무도 안 적는다.
     */
    @Override
    public int addHomeWorkoutLog(int userId, int durationMin, String intensity,
                                 int caloriesKcal, String memo) throws Exception {

        log.info("{}.addHomeWorkoutLog Start! userId : {}", this.getClass().getName(), userId);

        int res = myPageMapper.insertHomeWorkoutLog(userId, durationMin, intensity, caloriesKcal, memo);

        log.info("{}.addHomeWorkoutLog End! {}분 / {}kcal", this.getClass().getName(),
                durationMin, caloriesKcal);

        return res;
    }

}
