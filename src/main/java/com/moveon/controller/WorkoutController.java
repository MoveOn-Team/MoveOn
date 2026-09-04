package com.moveon.controller;

import com.moveon.dto.CourseDTO;
import com.moveon.dto.FacilityDTO;
import com.moveon.dto.SportDTO;
import com.moveon.service.IWorkoutService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 현위치는 안드로이드에서 GPS 로 받아 넘겨준다.
 * 좌표를 못 받으면(권한 거부·실내) 서울시청 좌표로 대체하고 화면은 그대로 보여준다.
 * 추천·행사 탭과 같은 방식이다.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/workout")
@Controller
public class WorkoutController {

    private final IWorkoutService workoutService;

    /**
     * 카카오 지도 JavaScript 키.
     *
     * REST 키와 다른 키다. 지도를 화면에 띄우는 데는 이쪽이 필요하다.
     * 이 키는 HTML 에 그대로 실려 나가지만 그래도 된다.
     * 카카오가 '등록된 도메인에서 온 요청' 만 받아 주기 때문이다.
     *
     * 없으면 빈 문자열이 된다. 그때는 지도 자리에 안내 글만 나온다.
     */
    @Value("${kakao.javascript.key:}")
    private String kakaoMapKey;

    /** GPS 를 못 받았을 때 쓸 기본 좌표 (서울시청) */
    private static final double DEFAULT_LAT = 37.5665;
    private static final double DEFAULT_LNG = 126.9780;

    @GetMapping("/workoutList")
    public String workoutList(@RequestParam(value = "tab", defaultValue = "facility") String tab,
                              @RequestParam(value = "sportId", required = false) Integer sportId,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "lat", required = false) Double lat,
                              @RequestParam(value = "lng", required = false) Double lng,
                              ModelMap model) throws Exception {

        log.info("{}.workoutList Start! tab : {}", this.getClass().getName(), tab);

        double myLat = (lat != null) ? lat : DEFAULT_LAT;
        double myLng = (lng != null) ? lng : DEFAULT_LNG;

        if ("outdoor".equals(tab)) {

            // 평지와 산길을 한 목록에 섞지 않는다.
            // 같은 3km 라도 오르막이 있으면 걸리는 시간과 힘이 다르다.
            String courseType = "HIKE".equals(type) ? "HIKE" : "WALK";
            List<CourseDTO> courses = workoutService.getCourses(courseType, myLat, myLng);

            model.addAttribute("courses", courses);
            model.addAttribute("courseType", courseType);

        } else if ("home".equals(tab)) {

            // 집에서 탭은 아직 자료가 없다.
            // home_exercises 표는 만들어져 있으나 비어 있어, 계획을 만들 재료가 없다.
            // 없는 것을 있는 척 보여주지 않고 화면이 그대로 알리게 둔다.
            model.addAttribute("homeReady", false);

        } else {

            // 종목 단추는 표에서 가져온다. 화면에 이름을 박아 두면 종목이 늘어도 화면이 모른다.
            List<SportDTO> sports = workoutService.getSports("FACILITY");
            model.addAttribute("sports", sports);

            // 고른 종목이 없으면 첫 번째 종목을 본다. 빈 화면으로 시작하지 않게 한다.
            int pickId = (sportId != null) ? sportId
                       : (sports.isEmpty() ? 0 : sports.get(0).getSportId());

            if (pickId > 0) {
                List<FacilityDTO> facilities = workoutService.getFacilities(pickId, myLat, myLng);
                model.addAttribute("facilities", facilities);
            }
            model.addAttribute("sportId", pickId);
        }

        model.addAttribute("currentTab", tab);
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        // 현위치를 받아 쓴 것인지, 기본 좌표로 계산한 것인지 화면에 알려준다
        model.addAttribute("usingGps", lat != null && lng != null);
        model.addAttribute("active", "workout");

        log.info("{}.workoutList End!", this.getClass().getName());

        return "workout/workoutList";
    }

    /**
     * 시설 상세
     *
     * 종목번호를 함께 받는다. 같은 시설에서 여러 종목을 하므로
     * 어떤 종목으로 들어왔는지 알아야 그 종목 강좌만 보여줄 수 있다.
     */
    @GetMapping("/workoutDetail/{facilityId}")
    public String workoutDetail(@PathVariable("facilityId") int facilityId,
                                @RequestParam(value = "sportId", defaultValue = "0") int sportId,
                                @RequestParam(value = "lat", required = false) Double lat,
                                @RequestParam(value = "lng", required = false) Double lng,
                                HttpSession session,
                                ModelMap model) throws Exception {

        double myLat = (lat != null) ? lat : DEFAULT_LAT;
        double myLng = (lng != null) ? lng : DEFAULT_LNG;

        FacilityDTO facility = workoutService.getFacility(facilityId, sportId, myLat, myLng);

        // 없는 번호로 들어오면 목록으로 돌려보낸다. 빈 상세 화면을 보여줄 이유가 없다.
        if (facility == null) {
            return "redirect:/workout/workoutList";
        }

        // 로그인하지 않아도 시설은 볼 수 있게 둔다.
        // 회원번호는 나이에 맞는 강좌를 위로 올리는 데만 쓰므로, 없으면 순서만 기본값이 된다.
        Object userNo = session.getAttribute("SS_USER_NO");
        int userId = (userNo instanceof Number) ? ((Number) userNo).intValue() : 0;

        // 즉시운동 탭은 '오늘 가서 쓰는' 화면이다.
        // 그래서 강습 수강신청이 아니라 빌리고 이용하는 창구로 보낸다.
        //   1순위 그 시설의 대관 페이지        facilities.rental_url
        //   2순위 서울시 공공서비스예약        facility_sports.reserve_url
        //   3순위 자치구 대관                district_sites.rental_url
        //   4순위 그 시설 홈페이지            facilities.homepage_url
        // 추천 탭(RecommendController)은 반대로 수강신청 쪽을 먼저 본다.
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
     * 야외 코스 상세
     *
     * 추천 탭의 걷기·등산에서도 이 화면으로 온다. 같은 자료라 화면을 또 만들지 않았다.
     * 대신 from 으로 어디서 왔는지 받아 뒤로가기와 아래 탭바를 그쪽에 맞춘다.
     * 없으면 즉시운동 탭에서 온 것으로 본다.
     */
    @GetMapping("/courseDetail/{courseId}")
    public String courseDetail(@PathVariable("courseId") int courseId,
                               @RequestParam(value = "lat", required = false) Double lat,
                               @RequestParam(value = "lng", required = false) Double lng,
                               @RequestParam(value = "from", required = false) String from,
                               @RequestParam(value = "sportId", defaultValue = "0") int sportId,
                               ModelMap model) throws Exception {

        double myLat = (lat != null) ? lat : DEFAULT_LAT;
        double myLng = (lng != null) ? lng : DEFAULT_LNG;

        CourseDTO course = workoutService.getCourse(courseId, myLat, myLng);

        // 없는 번호로 들어오면 목록으로 돌려보낸다. 빈 상세 화면을 보여줄 이유가 없다.
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

    /** 운동 진행 화면 */
    @GetMapping("/workoutPlay")
    public String workoutPlay(ModelMap model) {
        model.addAttribute("active", "workout");
        return "workout/workoutPlay";
    }

    /** 운동 완료 화면 */
    @GetMapping("/workoutResult")
    public String workoutResult(ModelMap model) {
        model.addAttribute("active", "workout");
        return "workout/workoutResult";
    }

    /**
     * 앞에서부터 쓸 만한 주소를 고른다.
     *
     * 공공데이터에는 값이 비었다는 뜻으로 "null" 이라는 글자가 그대로 들어온 행이 많다.
     * 자바의 null 검사로는 걸러지지 않아 그대로 두면 깨진 주소로 연결된다.
     */
    private String firstUsable(String... urls) {
        for (String u : urls) {
            if (u == null) {
                continue;
            }
            String v = u.trim();
            if (v.isEmpty() || "null".equalsIgnoreCase(v) || "-".equals(v)) {
                continue;
            }
            if (v.startsWith("http://") || v.startsWith("https://")) {
                return v;
            }
        }
        return null;
    }

}
