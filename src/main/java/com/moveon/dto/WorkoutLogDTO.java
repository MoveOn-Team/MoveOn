package com.moveon.dto;

import lombok.Data;

@Data
public class WorkoutLogDTO {
    private Integer logId;       // log_id (PK)
    private Integer userId;      // user_id
    private Integer sportId;
    private String sportName;    // 운동 종류 (헬스, 배드민턴 등)
    private Integer durationMin; // 운동 시간(분)
    private String intensity;    // 운동 강도 (가볍게, 적당히, 숨차게)
    private Integer caloriesBurned; // 소모 칼로리
    private String workoutDate;  // 운동 일자
    private String memo;         // 메모
    private String createdAt;    // 생성일시
}