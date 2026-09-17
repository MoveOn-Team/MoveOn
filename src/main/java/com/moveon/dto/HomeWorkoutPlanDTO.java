package com.moveon.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
/* List 는 인터페이스라 Serializable 이 아니라고 경고가 뜬다.
   실제로 담기는 것은 ArrayList 이고 HomeExerciseDTO 도 Serializable 이라 문제없다. */
@SuppressWarnings("serial")
public class HomeWorkoutPlanDTO implements Serializable {

    /** 세션에 담기는 DTO 다. 값을 늘려도 이미 담긴 것을 못 읽는 일이 없게 못 박는다 */
    private static final long serialVersionUID = 1L;

    private String ageGroup;
    private String ageBand;
    private String bmiGrade;
    private String gender;
    private String intensity;
    private String intensityLabel;
    private int targetMin;
    private int totalMin;
    private int totalKcal;
    private int warmupMin;
    private int mainMin;
    private int cooldownMin;
    private int totalSets;
    private int exerciseCount;
    private int completedExerciseCount;
    private int completedSetCount;
    private int skippedSetCount;
    private LocalDate exerciseDate;

    private List<HomeExerciseDTO> warmups = new ArrayList<>();
    private List<HomeExerciseDTO> mains = new ArrayList<>();
    private List<HomeExerciseDTO> cooldowns = new ArrayList<>();
    private List<HomeExerciseDTO> exercises = new ArrayList<>();

    public boolean isEmpty() {
        return exercises.isEmpty();
    }
}
