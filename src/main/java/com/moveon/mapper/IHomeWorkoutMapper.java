package com.moveon.mapper;

import com.moveon.dto.HomeExerciseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IHomeWorkoutMapper {

    List<HomeExerciseDTO> getRuleExercises(@Param("ageGroup") String ageGroup,
                                           @Param("ageBand") String ageBand,
                                           @Param("bmiGrade") String bmiGrade,
                                           @Param("gender") String gender,
                                           @Param("intensity") String intensity) throws Exception;
}
