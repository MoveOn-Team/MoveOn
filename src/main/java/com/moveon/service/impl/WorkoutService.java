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
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 利됱떆 ?대룞?섍린.
 *
 * ?섎뒗 ?쇱씠 嫄곗쓽 ?녿떎. 嫄곕Ⅴ怨?以??몄슦??寃껋? SQL ???쒕떎.
 * ?ш린?쒕뒗 ?붾㈃?먯꽌 ?섏뼱??媛믪씠 ?꾨뒗 媛믪씤吏 ?뺤씤?섍퀬, 紐?媛쒓퉴吏 媛?몄삱吏 ?뺥븳??
 *
 * 異붿쿇 ??낵 ?щ━ ?먯닔瑜?留ㅺ린吏 ?딅뒗??
 * '吏湲?媛????덈뒗 怨? ??李얜뒗 ?붾㈃?대씪 湲곗???嫄곕━ ?섎굹肉먯씠??
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

        List<FacilityDTO> rList = facilityMapper.getNearbyFacilities(sportId, lat, lng, LIST_LIMIT);

        log.info("{}.getFacilities End! {} items", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public List<CourseDTO> getCourses(String courseType, double lat, double lng) throws Exception {

        log.info("{}.getCourses Start! courseType : {}", this.getClass().getName(), courseType);

        // 二쇱냼李쎌뿉 ?type=drop 媛숈? 嫄??곸뼱 ?ｌ뼱??SQL 濡??섎윭媛吏 ?딄쾶 ?ш린??留됰뒗??
        // WALK ?됱? / HIKE ?곌만.
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

        // ?섏씠??留욌뒗 媛뺤쥖瑜??꾨줈 ?щ━?ㅻ㈃ ?뚯썝 ?섏씠媛 ?꾩슂?섎떎.
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

        List<HomeExerciseDTO> picked = pickRoutine(uniqueByPhaseAndExercise(raw));
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

    private List<HomeExerciseDTO> pickRoutine(List<HomeExerciseDTO> source) {
        List<HomeExerciseDTO> picked = new ArrayList<>();
        addPhase(picked, source, "WARMUP", 2);
        addPhase(picked, source, "MAIN", 3);
        addPhase(picked, source, "COOLDOWN", 1);
        return picked;
    }

    private void addPhase(List<HomeExerciseDTO> picked, List<HomeExerciseDTO> source,
                          String phase, int limit) {
        int count = 0;
        for (HomeExerciseDTO item : source) {
            if (phase.equals(item.getPhase())) {
                picked.add(item);
                count++;
                if (count == limit) {
                    return;
                }
            }
        }
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

    /**
     * 肄붿뒪瑜??대（??吏?먮뱾.
     *
     * ?덉쟾?먮뒗 ?ш린??醫뚰몴瑜?洹몃┝ 醫뚰몴(x, y)濡???린怨?SVG ?좉퉴吏 留뚮뱾?덈떎.
     * 吏???놁씠 媛쒕뀗?꾨? 洹몃━???뚯쓽 ?쇱씠??
     * 吏湲덉? 移댁뭅??吏?꾧? 醫뚰몴瑜?洹몃?濡?諛쏆븘 洹몃━誘濡???만 ?쇱씠 ?녿떎.
     */
    @Override
    public List<CoursePointDTO> getCoursePoints(int courseId) throws Exception {

        List<CoursePointDTO> points = courseMapper.getCoursePoints(courseId);
        for (CoursePointDTO pt : points) {
            pt.setPointName(cleanPointName(pt.getPointName()));
        }
        return points;
    }

    /**
     * ?먮낯 ?먮즺??吏???대쫫 以묒뿉???щ엺?먭쾶 ?몃え?녿뒗 寃껋씠 ?욎뿬 ?덈떎.
     *
     * '臾명솕?먯썝?곌퀎遺' 媛 11踰? '?쒕ぉ ?녿뒗 寃쎈줈' 媛 3踰??섏삤怨?
     * ??肄붿뒪 ?덉뿉??媛숈? ?대쫫????踰??섑??대릺湲곕룄 ?쒕떎.
     * ?쒖슱???먮뱶由쇨만 ?먮즺瑜?留뚮뱾 ?????대? 遺꾨쪟紐낆씠??湲??덈궡?먮뒗 ?꾩??????쒕떎.
     *
     * 諛섎?濡?'?섏쑀??吏?섏쿋 4?몄꽑', '?앹꽟?좎썝吏' 媛숈? ?대쫫? 洹몃?濡???留뚰븯??
     * 洹몃옒??吏?곗? ?딄퀬 ?몃え?녿뒗 寃껊쭔 嫄몃윭 ?몃떎.
     */
    private String cleanPointName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String s = name.trim();
        // contains 濡?蹂몃떎. matches 濡??섎㈃ ?꾩껜媛 ?묎컳?꾩빞 ?댁꽌
        // '?몃웾吏꾧만吏꾩엯濡?' 泥섎읆 ?욎뿉 湲?먭? 遺숈? 寃껋쓣 ?볦튇??
        if (s.contains("문화자원") || s.contains("제목 없는") || s.contains("진입로")) {
            return null;
        }
        return s;
    }

}
