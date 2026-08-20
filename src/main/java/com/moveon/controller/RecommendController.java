package com.moveon.controller;

import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.RentalDTO;
import com.moveon.dto.SportDTO;
import com.moveon.service.IRecommendService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * 탭① 맞춤 운동 추천
 *
 *   SC-010 맞춤 운동 추천  /recommend/recommendList
 *   SC-011 종목 상세       /recommend/sportDetail/{sportId}   (JSP 아직 없음)
 *
 * 클래스에 붙인 @RequestMapping("/recommend") 가 아래 메서드 주소 앞에 모두 붙는다.
 * 그래서 메서드에는 /recommend 를 다시 쓰지 않는다.
 *
 * 현위치는 안드로이드에서 GPS 로 받아 넘겨준다.
 * 좌표를 못 받으면(권한 거부·실내) 서울시청 좌표로 대체하고 화면은 그대로 보여준다.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value="/recommend")
@Controller
public class RecommendController {

    private final IRecommendService recommendService;

    /** GPS 를 못 받았을 때 쓸 기본 좌표 (서울시청) */
    private static final double DEFAULT_LAT = 37.5665;
    private static final double DEFAULT_LNG = 126.9780;

    /**
     * 맞춤 운동 추천 화면
     */
    @GetMapping(value = "/recommendList")
    public String recommend(HttpSession session,
                            @RequestParam(value = "lat", required = false) Double lat,
                            @RequestParam(value = "lng", required = false) Double lng,
                            ModelMap model) throws Exception {

        log.info("{}.recommend Start!", this.getClass().getName());

        Integer userId = getSessionUserId(session);
        if (userId == null) {
            log.info("{}.recommend End! 비로그인 접근", this.getClass().getName());
            return "redirect:/user/login";
        }

        double myLat = (lat == null) ? DEFAULT_LAT : lat;
        double myLng = (lng == null) ? DEFAULT_LNG : lng;

        log.info("userId : {} / lat : {} / lng : {}", userId, myLat, myLng);

        List<SportDTO> top3 = recommendService.getTop3(userId, myLat, myLng);

        model.addAttribute("top3", top3);

        // 화면 위쪽 성향 요약 카드
        model.addAttribute("profile", recommendService.getProfile(userId, myLat, myLng));

        // 종목 상세로 넘어갈 때 현위치를 그대로 이어줘야 거리가 달라지지 않는다.
        // 회원번호는 세션에서 읽으므로 주소에 싣지 않는다.
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        // 현위치를 받아 쓴 것인지, 기본 좌표로 계산한 것인지 화면에 알려준다
        model.addAttribute("usingGps", lat != null && lng != null);
        model.addAttribute("active", "recommend");

        log.info("{}.recommend End!", this.getClass().getName());

        return "recommend/recommendList";
    }

    /**
     * SC-011 종목 상세
     *
     * 가까운 공공체육시설 3곳과, 그중 첫 번째 시설의 운영 강좌를 함께 내려준다.
     * 다른 시설을 눌렀을 때 강좌만 바꿔 끼우는 건 화면에서 비동기로 처리한다.
     */
    @GetMapping(value = "/sportDetail/{sportId}")
    public String sportDetail(@PathVariable("sportId") int sportId,
                              HttpSession session,
                              @RequestParam(value = "lat", required = false) Double lat,
                              @RequestParam(value = "lng", required = false) Double lng,
                              @RequestParam(value = "facilityId", required = false) Integer facilityId,
                              ModelMap model) throws Exception {

        log.info("{}.sportDetail Start! sportId : {}", this.getClass().getName(), sportId);

        Integer userId = getSessionUserId(session);
        if (userId == null) {
            log.info("{}.sportDetail End! 비로그인 접근", this.getClass().getName());
            return "redirect:/user/login";
        }

        double myLat = (lat == null) ? DEFAULT_LAT : lat;
        double myLng = (lng == null) ? DEFAULT_LNG : lng;

        SportDTO sport = recommendService.getSportScore(userId, sportId, myLat, myLng);
        List<FacilityDTO> facilities = recommendService.getNearbyFacilities(sportId, myLat, myLng, 3);

        // 시설을 고르지 않았으면 가장 가까운 곳의 강좌를 보여준다
        int pickId = (facilityId != null) ? facilityId
                   : (facilities.isEmpty() ? 0 : facilities.get(0).getFacilityId());

        List<ProgramDTO> programs = (pickId == 0)
                ? Collections.emptyList()
                : recommendService.getPrograms(userId, pickId, sportId);

        // 강좌가 없으면 대관 가능한 곳을 대신 보여준다.
        // 축구·풋살처럼 '강좌 수강' 이 아니라 '구장 대관' 이 정상인 종목이 있다.
        List<RentalDTO> rentals = programs.isEmpty()
                ? recommendService.getNearbyRentals(sportId, myLat, myLng, 3)
                : Collections.emptyList();

        // 외부로 보낼 링크를 정한다
        //   1순위 강좌 예약 페이지        programs.reservation_url
        //   2순위 서울시 공공서비스예약   facility_sports.reserve_url
        //   3순위 시설 홈페이지          facilities.homepage_url
        //
        // facilities.guide_url 은 쓰지 않는다.
        // 좌표를 맞출 때 저장한 카카오맵 장소 주소라, 옆의 '길찾기' 버튼과 같은 곳으로 간다.
        // 셋 다 없으면 버튼을 만들지 않는다. 실제로 그런 시설이 대부분이다.
        final int selected = pickId;
        FacilityDTO pick = facilities.stream()
                .filter(f -> f.getFacilityId() == selected)
                .findFirst().orElse(null);

        String linkUrl = null;
        String linkLabel = null;

        for (ProgramDTO p : programs) {
            if (isUsableUrl(p.getReservationUrl())) {
                linkUrl = p.getReservationUrl();
                linkLabel = "예약페이지로 이동";
                break;
            }
        }
        if (linkUrl == null && pick != null && isUsableUrl(pick.getReserveUrl())) {
            linkUrl = pick.getReserveUrl();
            linkLabel = "예약페이지로 이동";
        }
        if (linkUrl == null && pick != null && isUsableUrl(pick.getHomepageUrl())) {
            linkUrl = pick.getHomepageUrl();
            linkLabel = "예약페이지로 이동";
        }
        // 시설 홈페이지가 없으면 그 시설이 속한 자치구 시설관리공단으로 보낸다.
        //
        // 이 시설의 페이지가 아니라는 점이 중요하다.
        // 청소년수련관·복지관은 청소년재단·복지재단이 따로 운영해서
        // 시설관리공단 목록에 없는 경우가 많다.
        // 그래서 'OO구 수강신청' 처럼 이 시설 것으로 읽히는 문구를 쓰지 않고,
        // 'OO구 체육시설 강좌' 로 지역 전체를 가리킨다는 걸 드러낸다.
        if (linkUrl == null && pick != null && isUsableUrl(pick.getDistrictUrl())) {
            linkUrl = pick.getDistrictUrl();
            linkLabel = "예약페이지로 이동";
        }

        model.addAttribute("sport", sport);
        model.addAttribute("profile", recommendService.getProfile(userId, myLat, myLng));
        model.addAttribute("facilities", facilities);
        model.addAttribute("pickId", pickId);
        model.addAttribute("programs", programs);
        model.addAttribute("rentals", rentals);
        model.addAttribute("linkUrl", linkUrl);
        model.addAttribute("linkLabel", linkLabel);

        // 다른 시설을 눌렀을 때 같은 현위치로 다시 조회하도록 이어준다
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        log.info("{}.sportDetail End!", this.getClass().getName());

        return "recommend/sportDetail";
    }

    /**
     * 링크로 쓸 수 있는 주소인지 본다.
     *
     * 공공데이터에는 값이 비어 있다는 뜻으로 "null" 이라는 글자가 그대로 들어온 행이 많다.
     * (programs.reservation_url 만 249건) 자바의 null 검사로는 걸러지지 않아
     * 그대로 두면 버튼이 깨진 주소로 연결된다.
     */
    private boolean isUsableUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String v = url.trim();
        if ("null".equalsIgnoreCase(v) || "-".equals(v)) {
            return false;
        }
        return v.startsWith("http://") || v.startsWith("https://");
    }

    /**
     * 세션에서 로그인한 회원 번호를 가져온다. 로그인 전이면 null.
     *
     * 회원번호를 주소(?userId=)로 받으면 남의 추천 결과를 볼 수 있게 되므로
     * UserController 와 같이 세션에서만 읽는다.
     */
    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }

}
