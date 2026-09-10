package com.moveon.controller;

import com.moveon.dto.*;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IUserService;
import com.moveon.service.IMyPageService;
import com.moveon.service.IWorkoutService;
import jakarta.servlet.http.HttpSession;
import static com.moveon.util.UrlUtil.firstUsable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/workout")
@Controller
public class WorkoutController {

    @Autowired
    private IUserMapper userMapper;

    private final IWorkoutService workoutService;

    /** 홈트를 끝냈을 때 리포트에 기록을 남기는 데 쓴다 */
    private final IMyPageService myPageService;

    @Value("${kakao.javascript.key:}")
    private String kakaoMapKey;

    private static final double DEFAULT_LAT = 37.5665;
    private static final double DEFAULT_LNG = 126.9780;
    private static final String HOME_PLAN_SESSION = "HOME_WORKOUT_PLAN";
    private static final String HOME_RESULT_SESSION = "HOME_WORKOUT_RESULT";

    @GetMapping("/workoutList")
    public String workoutList(@RequestParam(value = "tab", defaultValue = "facility") String tab,
                              @RequestParam(value = "sportId", required = false) Integer sportId,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "intensity", defaultValue = "MODERATE") String intensity,
                              @RequestParam(value = "targetMin", defaultValue = "20") int targetMin,
                              @RequestParam(value = "lat", required = false) Double lat,
                              @RequestParam(value = "lng", required = false) Double lng,
                              HttpSession session,
                              ModelMap model) throws Exception {

        double myLat = lat != null ? lat : DEFAULT_LAT;
        double myLng = lng != null ? lng : DEFAULT_LNG;

        if ("outdoor".equals(tab)) {
            String courseType = "HIKE".equals(type) ? "HIKE" : "WALK";
            List<CourseDTO> courses = workoutService.getCourses(courseType, myLat, myLng);
            model.addAttribute("courses", courses);
            model.addAttribute("courseType", courseType);

        } else if ("home".equals(tab)) {
            if (getSessionUserId(session) == null) {
                return "redirect:/user/login";
            }
            model.addAttribute("intensity", normalizeIntensityParam(intensity));
            model.addAttribute("targetMin", normalizeTargetMin(targetMin));

        } else {
            List<SportDTO> sports = workoutService.getSports("FACILITY");
            model.addAttribute("sports", sports);

            int pickId = sportId != null ? sportId : (sports.isEmpty() ? 0 : sports.get(0).getSportId());
            if (pickId > 0) {
                model.addAttribute("facilities", workoutService.getFacilities(pickId, myLat, myLng));
            }
            model.addAttribute("sportId", pickId);
        }

        model.addAttribute("currentTab", tab);
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);
        model.addAttribute("usingGps", lat != null && lng != null);
        model.addAttribute("active", "workout");

        return "workout/workoutList";
    }

    @GetMapping("/workoutDetail/{facilityId}")
    public String workoutDetail(@PathVariable("facilityId") int facilityId,
                                @RequestParam(value = "sportId", defaultValue = "0") int sportId,
                                @RequestParam(value = "lat", required = false) Double lat,
                                @RequestParam(value = "lng", required = false) Double lng,
                                ModelMap model) throws Exception {

        double myLat = lat != null ? lat : DEFAULT_LAT;
        double myLng = lng != null ? lng : DEFAULT_LNG;
        FacilityDTO facility = workoutService.getFacility(facilityId, sportId, myLat, myLng);
        if (facility == null) {
            return "redirect:/workout/workoutList";
        }

        String useUrl = firstUsable(facility.getRentalUrl(),
                facility.getReserveUrl(),
                facility.getDistrictRentalUrl(),
                facility.getHomepageUrl());

        model.addAttribute("facility", facility);
        model.addAttribute("useUrl", useUrl);
        model.addAttribute("kakaoMapKey", kakaoMapKey);
        model.addAttribute("sportId", sportId);
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);
        model.addAttribute("usingGps", lat != null && lng != null);
        model.addAttribute("active", "workout");

        return "workout/workoutDetail";
    }

    /**
     * 야외 코스 상세. 추천 탭에서도 이 화면으로 온다.
     * from 으로 어디서 왔는지 받아 뒤로가기와 탭바를 그쪽에 맞춘다.
     */
    @GetMapping("/courseDetail/{courseId}")
    public String courseDetail(@PathVariable("courseId") int courseId,
                               @RequestParam(value = "lat", required = false) Double lat,
                               @RequestParam(value = "lng", required = false) Double lng,
                               @RequestParam(value = "from", required = false) String from,
                               @RequestParam(value = "sportId", defaultValue = "0") int sportId,
                               ModelMap model) throws Exception {

        double myLat = lat != null ? lat : DEFAULT_LAT;
        double myLng = lng != null ? lng : DEFAULT_LNG;
        CourseDTO course = workoutService.getCourse(courseId, myLat, myLng);
        if (course == null) {
            return "redirect:/workout/workoutList?tab=outdoor";
        }

        boolean fromRecommend = "recommend".equals(from) && sportId > 0;

        model.addAttribute("course", course);
        model.addAttribute("points", workoutService.getCoursePoints(courseId));
        model.addAttribute("kakaoMapKey", kakaoMapKey);
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);
        model.addAttribute("usingGps", lat != null && lng != null);

        // 뒤로가기가 갈 곳과 아래 탭바에서 켤 칸
        model.addAttribute("fromRecommend", fromRecommend);
        model.addAttribute("sportId", sportId);
        model.addAttribute("active", fromRecommend ? "recommend" : "workout");

        return "workout/courseDetail";
    }

    @GetMapping("/workoutPlay")
    public String workoutPlay(HttpSession session, ModelMap model) throws Exception {
        HomeWorkoutPlanDTO plan = (HomeWorkoutPlanDTO) session.getAttribute(HOME_PLAN_SESSION);
        if (plan == null || plan.isEmpty()) {
            return "redirect:/workout/workoutList?tab=home";
        }

        // 1. 세션에서 로그인 정보 가져오기
        Object userNoObj = session.getAttribute("SS_USER_NO");
        String loginId = (String) session.getAttribute("SS_USER_ID");

        double userWeight = 65.0;

        // 2. DB에서 유저 최신 체중 조회 (IUserMapper 직접 호출)
        if (userNoObj != null || loginId != null) {
            UserDTO pDTO = new UserDTO();

            if (userNoObj instanceof Integer) {
                pDTO.setUserId((Integer) userNoObj);
            } else if (userNoObj instanceof Long) {
                pDTO.setUserId(((Long) userNoObj).intValue());
            }

            if (loginId != null) {
                pDTO.setLoginId(loginId);
            }

            // Mapper의 getLoginUser 직접 실행
            UserDTO userDTO = userMapper.getLoginUser(pDTO);

            if (userDTO != null && userDTO.getWeightKg() > 0) {
                userWeight = userDTO.getWeightKg();
            }
        }

        System.out.println("최종 적용된 체중: " + userWeight + " kg");

        model.addAttribute("userWeight", userWeight);
        model.addAttribute("homePlan", plan);
        model.addAttribute("active", "workout");
        return "workout/workoutPlay";
    }

    @GetMapping("/workoutResult")
    public String workoutResult(HttpSession session, ModelMap model) throws Exception {
        HomeWorkoutPlanDTO result = (HomeWorkoutPlanDTO) session.getAttribute(HOME_RESULT_SESSION);
        HomeWorkoutPlanDTO plan = result != null
                ? result
                : (HomeWorkoutPlanDTO) session.getAttribute(HOME_PLAN_SESSION);

        if (plan == null || plan.isEmpty()) {
            return "redirect:/workout/workoutList?tab=home";
        }

        // 1. 세션에서 로그인 정보 가져와 최신 체중 조회
        Object userNoObj = session.getAttribute("SS_USER_NO");
        String loginId = (String) session.getAttribute("SS_USER_ID");
        double userWeight = 65.0;

        if (userNoObj != null || loginId != null) {
            UserDTO pDTO = new UserDTO();
            if (userNoObj instanceof Integer) pDTO.setUserId((Integer) userNoObj);
            else if (userNoObj instanceof Long) pDTO.setUserId(((Long) userNoObj).intValue());
            if (loginId != null) pDTO.setLoginId(loginId);

            UserDTO userDTO = userMapper.getLoginUser(pDTO);
            if (userDTO != null && userDTO.getWeightKg() > 0) {
                userWeight = userDTO.getWeightKg();
            }
        }

        // 2. 세션 칼로리 값 반영
        Object burnedCalObj = session.getAttribute("burnedCalories");
        if (burnedCalObj != null) {
            int actualCalories = (Integer) burnedCalObj;
            plan.setTotalKcal(actualCalories);
        }

        // 3. 모델에 userWeight 전달 (★ 핵심!)
        model.addAttribute("userWeight", userWeight);
        model.addAttribute("homePlan", plan);
        model.addAttribute("active", "workout");
        return "workout/workoutResult";
    }

    @ResponseBody
    @PostMapping("/api/home-plan")
    public HomeWorkoutPlanDTO makeHomePlan(@RequestParam(value = "intensity", defaultValue = "MODERATE") String intensity,
                                           @RequestParam(value = "targetMin", defaultValue = "20") int targetMin,
                                           HttpSession session) throws Exception {

        Integer userId = getSessionUserId(session);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED");
        }

        HomeWorkoutPlanDTO plan = workoutService.getHomeWorkoutPlan(
                userId, normalizeIntensityParam(intensity), normalizeTargetMin(targetMin));
        session.setAttribute(HOME_PLAN_SESSION, plan);
        session.removeAttribute(HOME_RESULT_SESSION);
        return plan;
    }

    @ResponseBody
    @GetMapping("/api/home-plan")
    public HomeWorkoutPlanDTO getHomePlan(HttpSession session) {
        HomeWorkoutPlanDTO plan = (HomeWorkoutPlanDTO) session.getAttribute(HOME_PLAN_SESSION);
        return plan != null ? plan : new HomeWorkoutPlanDTO();
    }

    @ResponseBody
    @PostMapping("/api/home-result")
    public Map<String, Object> saveHomeResult(@RequestBody Map<String, Object> body,
                                              HttpSession session) {
        HomeWorkoutPlanDTO plan = (HomeWorkoutPlanDTO) session.getAttribute(HOME_PLAN_SESSION);
        if (plan == null || plan.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PLAN_REQUIRED");
        }

        // 1. 기존 카운트 데이터 수집
        plan.setCompletedExerciseCount(intValue(body.get("completedExerciseCount")));
        plan.setCompletedSetCount(intValue(body.get("completedSetCount")));
        plan.setSkippedSetCount(intValue(body.get("skippedSetCount")));

        // 2. JS에서 실시간 계산하여 보낸 칼로리 값 추출
        int burnedCalories = intValue(body.get("burnedCalories"));

        // HomeWorkoutPlanDTO에 burnedCalories 필드가 있다면 set, 없다면 세션에 별도 저장
        plan.setTotalKcal(burnedCalories);
        session.setAttribute("burnedCalories", burnedCalories);

        // 4. 리포트에 자동으로 남긴다.
        // HOME_RESULT_SESSION 이 있으면 이미 넣은 것이라 건너뛴다. 계획 하나에 기록 하나.
        // 기록에 실패해도 결과 화면은 보여준다.
        boolean already = session.getAttribute(HOME_RESULT_SESSION) != null;
        Integer userId = getSessionUserId(session);
        if (!already && userId != null) {
            try {
                int minutes = plan.getTotalMin() > 0 ? plan.getTotalMin() : plan.getTargetMin();
                myPageService.addHomeWorkoutLog(userId, minutes, plan.getIntensity(),
                        burnedCalories, "홈트 " + plan.getIntensityLabel());
            } catch (Exception e) {
                log.warn("홈트 기록을 남기지 못했다 : {}", e.getMessage());
            }
        }

        // 5. 업데이트된 plan 세션 재저장
        session.setAttribute(HOME_RESULT_SESSION, plan);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("redirectUrl", "/workout/workoutResult");
        return res;
    }

    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }

    private String normalizeIntensityParam(String intensity) {
        if ("LIGHT".equals(intensity) || "HARD".equals(intensity)) {
            return intensity;
        }
        return "MODERATE";
    }

    private int normalizeTargetMin(int targetMin) {
        if (targetMin == 10 || targetMin == 30) {
            return targetMin;
        }
        return 20;
    }

    private int intValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            return Integer.parseInt(s);
        }
        return 0;
    }


}
