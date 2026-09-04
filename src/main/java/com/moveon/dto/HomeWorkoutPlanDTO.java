package com.moveon.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HomeWorkoutPlanDTO implements Serializable {

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
