package com.moveon.dto;

import lombok.Data;

import java.util.List;

/**
 * 연속 출석.
 *
 * 하루에 몇 번을 하든 하루로 친다. 두 번 했다고 이틀이 되면
 * 회원이 화면을 믿지 않는다.
 */
@Data
public class StreakDTO {

    /** 오늘(또는 어제)까지 며칠째 이어졌는지 */
    private int current;

    /** 지금까지 가장 길었던 날 수 */
    private int max;

    /** 이번 주 월~일 일곱 칸. 그날 운동했으면 true */
    private List<Boolean> week;
}
