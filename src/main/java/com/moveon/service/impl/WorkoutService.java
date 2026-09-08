package com.moveon.service.impl;

import com.moveon.dto.*;
import com.moveon.mapper.ICourseMapper;
import com.moveon.mapper.IFacilityMapper;
import com.moveon.mapper.IHomeWorkoutMapper;
import com.moveon.mapper.ISportMapper;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IWorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 즉시 운동하기. 고르고 줄 세우는 일은 SQL 에 맡기고
 * 여기서는 화면에서 넘어온 값이 쓸 수 있는 값인지만 본다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WorkoutService implements IWorkoutService {

    private final IFacilityMapper facilityMapper;
    private final ICourseMapper courseMapper;
    private final ISportMapper sportMapper;
    private final IUserMapper userMapper;
    private final IHomeWorkoutMapper homeWorkoutMapper;

    private static final int LIST_LIMIT = 10;

    @Override
    public List<FacilityDTO> getFacilities(int sportId, double lat, double lng) throws Exception {

        log.info("{}.getFacilities Start! sportId : {}", this.getClass().getName(), sportId);

        // 연령대 null, bookableOnly false.
        // 신청 창구가 없어 그냥 가서 쓰는 곳이야말로 이 화면이 찾아 줘야 하는 것이다.
        List<FacilityDTO> rList =
                facilityMapper.getNearbyFacilities(sportId, lat, lng, LIST_LIMIT, null, false);

        log.info("{}.getFacilities End! {} items", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public List<CourseDTO> getCourses(String courseType, double lat, double lng) throws Exception {

        log.info("{}.getCourses Start! courseType : {}", this.getClass().getName(), courseType);

        // 주소창에 아무 값이나 넣어도 SQL 로 흘러가지 않게 여기서 막는다
        String type = "HIKE".equals(courseType) ? "HIKE" : "WALK";

        List<CourseDTO> rList = courseMapper.getNearbyCourses(type, lat, lng, LIST_LIMIT);

        log.info("{}.getCourses End! {} items", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public FacilityDTO getFacility(int facilityId, int sportId, double lat, double lng) throws Exception {
        return facilityMapper.getFacility(facilityId, sportId, lat, lng);
    }

    @Override
    public List<ProgramDTO> getPrograms(int userId, int facilityId, int sportId) throws Exception {

        log.info("{}.getPrograms Start!", this.getClass().getName());

        // 나이에 맞는 강좌를 위로 올리려면 회원 나이가 필요하다
        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        List<ProgramDTO> rList = facilityMapper.getPrograms(facilityId, sportId, ageBand(me.getAge()));

        log.info("{}.getPrograms End! {} items", this.getClass().getName(), rList.size());

        return rList;
    }

    private String ageBand(int age) {
        if (age >= 65) {
            return "SENIOR";
        }
        if (age >= 19) {
            return "ADULT";
        }
        if (age >= 13) {
            return "TEEN";
        }
        return "CHILD";
    }

    @Override
    public CourseDTO getCourse(int courseId, double lat, double lng) throws Exception {
        return courseMapper.getCourse(courseId, lat, lng);
    }

    @Override
    public List<SportDTO> getSports(String category) throws Exception {
        return sportMapper.getSportsByCategory(category);
    }

    @Override
    public HomeWorkoutPlanDTO getHomeWorkoutPlan(int userId, String intensity, int targetMin) throws Exception {

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        String useIntensity = normalizeIntensity(intensity);
        int useTargetMin = Math.max(10, Math.min(60, targetMin));
        String ageGroup = ageGroup(me.getAge());
        String ageBand = ageBand(me.getAge());
        String bmiGrade = bmiGrade(me.getBmi());
        String gender = ("M".equals(me.getGender()) || "F".equals(me.getGender())) ? me.getGender() : "ALL";

        List<HomeExerciseDTO> raw = homeWorkoutMapper.getRuleExercises(
                ageGroup, ageBand, bmiGrade, gender, useIntensity);

        HomeWorkoutPlanDTO plan = new HomeWorkoutPlanDTO();
        plan.setAgeGroup(ageGroup);
        plan.setAgeBand(ageBand);
        plan.setBmiGrade(bmiGrade);
        plan.setGender(gender);
        plan.setIntensity(useIntensity);
        plan.setIntensityLabel(intensityLabel(useIntensity));
        plan.setTargetMin(useTargetMin);

        List<HomeExerciseDTO> picked = pickRoutine(uniqueByPhaseAndExercise(raw), useTargetMin);
        tuneSets(picked, useIntensity, useTargetMin);

        int totalMin = 0;
        int totalSets = 0;
        int warmupMin = 0;
        int mainMin = 0;
        int cooldownMin = 0;
        double totalMetMin = 0;
        for (HomeExerciseDTO item : picked) {
            int minutes = estimateMinutes(item, useIntensity);
            item.setEstimatedMin(minutes);
            item.setRestSec(restSec(useIntensity));
            totalMin += minutes;
            totalSets += item.getSets();
            if (item.getMetValue() != null) {
                totalMetMin += item.getMetValue().doubleValue() * minutes;
            }

            if ("WARMUP".equals(item.getPhase())) {
                plan.getWarmups().add(item);
                warmupMin += minutes;
            } else if ("COOLDOWN".equals(item.getPhase())) {
                plan.getCooldowns().add(item);
                cooldownMin += minutes;
            } else {
                plan.getMains().add(item);
                mainMin += minutes;
            }
            plan.getExercises().add(item);
        }

        plan.setTotalMin(totalMin);
        plan.setTotalSets(totalSets);
        plan.setExerciseCount(plan.getExercises().size());
        plan.setExerciseDate(LocalDate.now());
        plan.setWarmupMin(warmupMin);
        plan.setMainMin(mainMin);
        plan.setCooldownMin(cooldownMin);
        plan.setTotalKcal(estimateKcal(totalMetMin, me.getWeightKg()));
        return plan;
    }

    private List<HomeExerciseDTO> uniqueByPhaseAndExercise(List<HomeExerciseDTO> raw) {
        Map<String, HomeExerciseDTO> map = new LinkedHashMap<>();
        for (HomeExerciseDTO item : raw) {
            map.putIfAbsent(item.getPhase() + ":" + item.getExerciseId(), item);
        }
        return List.copyOf(map.values());
    }

    private List<HomeExerciseDTO> pickRoutine(List<HomeExerciseDTO> source, int targetMin) {
        // 고른 시간만큼 동작 수가 달라진다. 세트만 늘리면 10분과 30분이 같은 운동이 된다.
        int main = targetMin >= 30 ? 5 : targetMin <= 15 ? 2 : 3;
        int warm = targetMin >= 30 ? 3 : 2;
        int cool = targetMin >= 30 ? 2 : 1;

        List<HomeExerciseDTO> picked = new ArrayList<>();
        addPhase(picked, source, "WARMUP", warm);
        addPhase(picked, source, "MAIN", main);
        addPhase(picked, source, "COOLDOWN", cool);
        return picked;
    }

    private void addPhase(List<HomeExerciseDTO> picked, List<HomeExerciseDTO> source,
                          String phase, int limit) {
        // 이미 뽑힌 동작은 뺀다. 한 동작이 준비와 본운동에 두 번 나오면 안 된다.
        Set<Integer> taken = new HashSet<>();
        for (HomeExerciseDTO item : picked) {
            taken.add(item.getExerciseId());
        }

        List<HomeExerciseDTO> pool = new ArrayList<>();
        for (HomeExerciseDTO item : source) {
            if (phase.equals(item.getPhase()) && !taken.contains(item.getExerciseId())) {
                pool.add(item);
            }
        }
        // 섞는다. 앞에서부터 집으면 같은 사람이 매일 같은 운동을 받는다.
        Collections.shuffle(pool);
        picked.addAll(pool.subList(0, Math.min(limit, pool.size())));
    }

    private void tuneSets(List<HomeExerciseDTO> items, String intensity, int targetMin) {
        int add = targetMin >= 30 ? 1 : 0;
        int reduce = targetMin <= 15 ? 1 : 0;
        for (HomeExerciseDTO item : items) {
            int sets = Math.max(1, item.getSets());
            if ("MAIN".equals(item.getPhase())) {
                sets = sets + add - reduce;
                if ("HARD".equals(intensity)) {
                    sets++;
                }
            } else {
                // 준비와 마무리는 한 세트만. 기본값 3세트를 그대로 두면
                // 준비 자리에 든 팔굽혀펴기가 본운동만큼 힘들어진다.
                sets = 1;
            }
            item.setSets(Math.max(1, Math.min(5, sets)));
        }
    }

    private int estimateMinutes(HomeExerciseDTO item, String intensity) {
        int activeSec = item.getDurationSec() != null
                ? item.getDurationSec()
                : Optional.ofNullable(item.getReps()).orElse(10) * 4;
        int totalSec = (activeSec * item.getSets()) + (restSec(intensity) * Math.max(0, item.getSets() - 1));
        return Math.max(1, (int) Math.ceil(totalSec / 60.0));
    }

    private int restSec(String code) {
        if ("HARD".equals(code)) {
            return 60;
        }
        if ("MODERATE".equals(code)) {
            return 45;
        }
        return 30;
    }

    private String normalizeIntensity(String intensity) {
        if ("LIGHT".equals(intensity) || "HARD".equals(intensity)) {
            return intensity;
        }
        return "MODERATE";
    }

    private String intensityLabel(String intensity) {
        if ("LIGHT".equals(intensity)) {
            return "가볍게";
        }
        if ("HARD".equals(intensity)) {
            return "숨차게";
        }
        return "적당히";
    }

    private String ageGroup(int age) {
        if (age >= 70) {
            return "70대이상";
        }
        if (age <= 0) {
            return "전체";
        }
        return (age / 10 * 10) + "대";
    }

    private String bmiGrade(double bmi) {
        if (bmi <= 0) {
            return "전체";
        }
        if (bmi < 18.5) {
            return "저체중";
        }
        if (bmi < 23) {
            return "정상";
        }
        if (bmi < 25) {
            return "과체중";
        }
        return "비만";
    }

    private int estimateKcal(double totalMetMin, double weightKg) {
        double useWeight = weightKg > 0 ? weightKg : 60;
        return (int) Math.round(totalMetMin * 3.5 * useWeight / 200);
    }

    /** 코스를 이루는 지점들. 지도에 선으로 그리는 데 쓴다 */
    @Override
    public List<CoursePointDTO> getCoursePoints(int courseId) throws Exception {

        List<CoursePointDTO> points = courseMapper.getCoursePoints(courseId);
        for (CoursePointDTO pt : points) {
            pt.setPointName(cleanPointName(pt.getPointName()));
        }
        return points;
    }

    /** 자료의 지점 이름 중 '문화자원연계부' 처럼 사람에게 뜻 없는 것을 지운다 */
    private String cleanPointName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String s = name.trim();
        // matches 로 하면 이름 전체가 같아야 해서 '인량진고개진입로' 같은 것을 놓친다
        if (s.contains("문화자원") || s.contains("제목 없는") || s.contains("진입로")) {
            return null;
        }
        return s;
    }

}
