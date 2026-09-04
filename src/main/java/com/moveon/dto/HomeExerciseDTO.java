package com.moveon.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class HomeExerciseDTO implements Serializable {

    private int exerciseId;
    private String name;
    private String bodyPart;
    private BigDecimal metValue;
    private int defaultSets;
    private Integer defaultReps;
    private Integer defaultSec;
    private String instruction;
    private String mediaType;
    private String mediaUrl;

    private String phase;
    private int sortOrder;
    private int sets;
    private Integer reps;
    private Integer durationSec;
    private int restSec;
    private int estimatedMin;

    public String getPhaseLabel() {
        if ("WARMUP".equals(phase)) {
            return "\uC900\uBE44\uC6B4\uB3D9";
        }
        if ("COOLDOWN".equals(phase)) {
            return "\uB9C8\uBB34\uB9AC";
        }
        return "\uBCF8\uC6B4\uB3D9";
    }

    public String getVolumeLabel() {
        String unit = reps != null ? reps + "\uD68C" : durationSec + "\uCD08";
        return unit + " x " + sets + "\uC138\uD2B8";
    }
}
