package com.moveon.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkoutReportDTO {
    // 이번 주 요약 (8/24 ~ 8/30)
    private String weekRangeText;      // 예: "8/24 ~ 8/30"
    private int thisWeekCount;         // 이번 주 운동 횟수
    private int thisWeekDurationMin;   // 이번 주 운동 총 시간(분)
    private int weekDiffCount;         // 지난주 대비 증감 횟수 (예: +1, -2)

    // 주요 요약 통계
    private int totalCalories;         // 이번 주 소모 칼로리
    private int currentStreak;         // 현재 연속 출석일
    private int totalWorkoutCount;     // 전체 누적 운동 횟수

    // 많이 한 종목 목록
    private List<SportStatDTO> topSports;

    // 최근 8주 운동 횟수 목록
    private List<WeeklyStatDTO> weeklyStats;

    // 기록 갱신 영역
    private String maxDurationSportName; // 가장 오래 한 운동 이름
    private int maxDurationMin;          // 가장 오래 한 운동 시간(분)
    private int maxStreakDays;           // 최장 연속 출석일
    private String firstRecordDate;      // 첫 기록 날짜 (YYYY년 M월 D일)

    // --- Inner DTOs ---
    @Data
    public static class SportStatDTO {
        private String sportName;
        private int count;
        private int percentage;
    }

    @Data
    public static class WeeklyStatDTO {
        private String weekLabel; // 예: "6/15", "이번"
        private int count;
    }
}