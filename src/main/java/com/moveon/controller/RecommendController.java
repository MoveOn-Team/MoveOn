package com.moveon.controller;

import com.moveon.dto.CourseDTO;
import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.RentalDTO;
import com.moveon.dto.SportDTO;
import com.moveon.service.IRecommendService;
import jakarta.servlet.http.HttpSession;
import static com.moveon.util.UrlUtil.firstUsable;
import static com.moveon.util.UrlUtil.isUsable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;


/**
 * 맞춤 운동 추천 탭
 *
 * 현위치는 안드로이드에서 GPS 로 받아 넘겨줌
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

    /** 목록 하나에 보여줄 개수. 시설·코스·대관 모두 이 값을 쓴다. */
    private static final int LIST_SIZE = 3;

    /** 추천 목록 위에 오늘 날씨를 띄우는 데 쓴다. 화면(JS)이 직접 부른다 */
    @Value("${weather.api.key:}")
    private String weatherApiKey;

    /** 우리 시설을 서울시 예약 자료와 한 목록에 섞기 위해 그릇을 맞춘다 */
    private RentalDTO asRental(FacilityDTO f, String sportName) {
        RentalDTO r = new RentalDTO();
        r.setFacilityId(f.getFacilityId());
        r.setPlaceName(f.getName());
        r.setMinClass(sportName);
        r.setDistanceKm(f.getDistanceKm());
        r.setLat(f.getLat());
        r.setLng(f.getLng());
        return r;
    }

    /** 아래 탭바에서 켤 칸. 이 컨트롤러의 화면은 전부 추천 탭이라 한 곳에 모아 둔다 */
    @ModelAttribute("active")
    public String activeTab() {
        return "recommend";
    }

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

        model.addAttribute("profile", recommendService.getProfile(userId, myLat, myLng));

        // 상세로 넘어갈 때 현위치를 이어줘야 거리가 달라지지 않는다
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);
        model.addAttribute("usingGps", lat != null && lng != null);
        model.addAttribute("weatherApiKey", weatherApiKey);

        log.info("{}.recommend End!", this.getClass().getName());

        return "recommend/recommendList";
    }

    /** 종목 상세. 가까운 시설을 '배우는 곳' 과 '빌리는 곳' 으로 갈라 보여준다 */
    @GetMapping(value = "/sportDetail/{sportId}")
    public String sportDetail(@PathVariable("sportId") int sportId,
                              HttpSession session,
                              @RequestParam(value = "lat", required = false) Double lat,
                              @RequestParam(value = "lng", required = false) Double lng,
                              @RequestParam(value = "facilityId", required = false) Integer facilityId,
                              @RequestParam(value = "place", required = false) String place,
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

        // 추천 대상이 아닌 번호. 홈트(category='HOME')와 없는 번호가 여기 온다
        if (sport == null) {
            log.info("{}.sportDetail End! 추천 대상이 아닌 종목 : {}", this.getClass().getName(), sportId);
            return "redirect:/recommend/recommendList";
        }

        // 아래에서 둘로 가르므로 넉넉히 가져온다. 세 곳만 뽑으면 강좌 시설이
        // 자리를 다 먹었을 때 대관 시설은 후보에도 못 든다
        List<FacilityDTO> facilities =
                recommendService.getNearbyFacilities(userId, sportId, myLat, myLng, LIST_SIZE * 4);

        // 목록에서 누르면 facilityId 가 붙어 다시 들어온다. 없으면 가장 가까운 곳.
        // 아무것도 안 고르면 아래 안내와 단추가 전부 빈다
        FacilityDTO pick = null;
        if (facilityId != null) {
            pick = facilities.stream()
                    .filter(f -> f.getFacilityId() == facilityId)
                    .findFirst().orElse(null);
        }
        if (pick == null && !facilities.isEmpty()) {
            pick = facilities.get(0); // 엉뚱한 번호면 가장 가까운 곳으로
        }
        List<ProgramDTO> programs = (pick == null)
                ? Collections.emptyList()
                : recommendService.getPrograms(userId, pick.getFacilityId(), sportId);

        // 걷기·등산은 facility_sports 에 행이 없어 시설 목록이 늘 빈다.
        // 즉시운동 탭이 쓰는 서울두드림길 코스로 대신 답한다
        List<CourseDTO> courses = "OUTDOOR".equals(sport.getCategory())
                ? recommendService.getNearbyCourses(sport.getName(), myLat, myLng, LIST_SIZE)
                : Collections.emptyList();
        model.addAttribute("courses", courses);

        // 서울시 공공서비스예약. 장소 175곳 중 절반 넘게가 공공체육시설 관리대장에
        // 없어 우리 시설에 붙일 수 없다. 붙이는 대신 따로 세운다.
        // 종목을 가리는 것은 매퍼가 한다
        List<RentalDTO> rentals = courses.isEmpty()
                ? new ArrayList<>(recommendService.getNearbyRentals(sportId, myLat, myLng, LIST_SIZE))
                : new ArrayList<>();

        // 대관 주소를 가진 우리 시설도 '빌리는 곳' 이다. 위 목록에 두면 배우러
        // 갈 수 있는 것처럼 읽힌다. 이어 붙이지 않고 섞어서 거리순으로 다시 세운다
        List<FacilityDTO> learnList = new ArrayList<>();
        for (FacilityDTO f : facilities) {
            if (isUsable(f.getReserveUrl()) || isUsable(f.getRentalUrl())) {
                rentals.add(asRental(f, sport.getName()));
            } else {
                learnList.add(f);
            }
        }
        // 가른 뒤에 각자 세 곳씩 자른다
        rentals.sort(Comparator.comparingDouble(RentalDTO::getDistanceKm));
        if (rentals.size() > LIST_SIZE) {
            rentals = rentals.subList(0, LIST_SIZE);
        }
        if (learnList.size() > LIST_SIZE) {
            learnList = learnList.subList(0, LIST_SIZE);
        }
        model.addAttribute("learnFacilities", learnList);

        Destination dest = chooseDestination(pick, programs);
        String linkUrl = dest.url();
        boolean toRental = dest.go() == Go.RENT;

        String linkLabel = (linkUrl == null) ? null : dest.go().label;

        // 서울시 예약 장소를 눌러도 바깥으로 바로 내보내지 않는다. 그러면 길찾기를
        // 쓸 수 없다. 우리 화면에 남기고 나가는 것은 '대관 신청하기' 가 맡는다
        String pickName = (pick == null) ? null : pick.getName();
        double pickLat = (pick == null) ? 0 : pick.getLat();
        double pickLng = (pick == null) ? 0 : pick.getLng();

        if (place != null && !place.isBlank()) {
            for (RentalDTO r : rentals) {
                if (r.getFacilityId() == 0 && place.equals(r.getPlaceName())) {
                    pickName = r.getPlaceName();
                    pickLat = r.getLat();
                    pickLng = r.getLng();
                    linkUrl = r.getSvcUrl();
                    linkLabel = Go.RENT.label;
                    toRental = true;
                    break;
                }
            }
        }
        model.addAttribute("pickName", pickName);
        model.addAttribute("pickLat", pickLat);
        model.addAttribute("pickLng", pickLng);
        model.addAttribute("pickPlace", place == null ? "" : place);

        model.addAttribute("visitOnly", pick != null && pick.isVisitOnly());

        // 예약 절차 자체가 없는 개방형 코트인지. 근린공원 농구장·풋살장이 그렇다.
        //
        // programs 는 나이로 걸러 온 값이라 비는 이유가 둘이다.
        //   ① 강좌가 원래 없다        -> 개방형 코트
        //   ② 내 나이 대상이 아니다   -> 구민체육관. 걸어 들어갈 수 없다
        // courseCount 는 나이를 안 거른 값이라 둘을 갈라 준다
        boolean openAccess = programs.isEmpty() && linkUrl == null
                && pick != null && pick.getCourseCount() == 0;
        model.addAttribute("openAccess", openAccess);

        // 화면은 이걸 보고 '강좌가 없어요' 안내를 접는다
        model.addAttribute("toRental", toRental);

        // 목록 제목을 '지금 빌릴 수 있는 3곳' 으로 바꿀지. 같은 테니스라도
        // 강남에는 강습이 있고 오금동에는 대관뿐이라 동네마다 갈린다.
        //
        // 나이를 거른 myCourseCount 로 본다. 아래 버튼도 같은 기준이라 맞춰야 한다.
        // facilities 가 아니라 learnList 로 보는 것도 마찬가지 이유다.
        // 저쪽은 조회용이라 대관 시설까지 섞인 열두 곳이다
        model.addAttribute("rentalMode",
                learnList.stream().allMatch(f -> f.getMyCourseCount() == 0));

        model.addAttribute("sport", sport);
        model.addAttribute("profile", recommendService.getProfile(userId, myLat, myLng));
        model.addAttribute("facilities", facilities);
        model.addAttribute("pick", pick);
        model.addAttribute("rentals", rentals);
        model.addAttribute("linkUrl", linkUrl);
        model.addAttribute("linkLabel", linkLabel);

        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        log.info("{}.sportDetail End!", this.getClass().getName());

        return "recommend/sportDetail";
    }

    /** 버튼 너머에서 회원이 할 수 있는 일. 단추에 적을 말이 여기서 갈린다 */
    private enum Go {
        LEARN("예약페이지로 이동"),
        /** 장소만 빌리는 것. 유료든 무료든 */
        RENT("대관 신청하기"),
        /** 신청은 못 하고 보기만 하는 곳 */
        INFO("안내 페이지로 이동");

        final String label;
        Go(String label) { this.label = label; }
    }

    private record Destination(String url, Go go) {
        static final Destination NONE = new Destination(null, Go.INFO);
    }

    /**
     * 회원을 내보낼 곳. 같은 시설이라도 수강신청과 대관은 창구가 달라서,
     * 이 시설이 무엇을 가졌는지 보고 그쪽으로 보낸다.
     *
     * 방문 접수만 받으면  시설 홈페이지 → 카카오 장소            (신청 아님, 보기만)
     * 강습이 있으면      강좌 예약 → 시설 홈페이지 → 자치구 수강신청
     * 장소만 내주면      공공서비스예약 → 시설 대관 → 자치구 대관
     */
    private Destination chooseDestination(FacilityDTO pick, List<ProgramDTO> programs) {

        if (pick == null) {
            return Destination.NONE;
        }

        // 방문 접수만 받는 시설 114곳. 강좌는 여는데 온라인 창구가 없어서
        // 자치구 수강신청으로 보내면 헛걸음이다. 신청이 아니라 안내만 준다.
        //
        // 홈페이지가 없으면(59곳) 주소를 주지 않는다. 카카오 장소로 보내면
        // 바로 옆 '길찾기' 와 같은 화면이 떠서 속은 기분이 든다.
        // 그때는 화면이 전화번호 안내로 넘어간다
        if (pick.isVisitOnly()) {
            return new Destination(firstUsable(pick.getHomepageUrl()), Go.INFO);
        }

        boolean hasCourse = programs.stream()
                .anyMatch(p -> "COURSE".equals(p.getProgramType()));

        if (hasCourse) {
            for (ProgramDTO p : programs) {
                if (isUsable(p.getReservationUrl())) {
                    return new Destination(p.getReservationUrl(), Go.LEARN);
                }
            }
            String url = firstUsable(pick.getHomepageUrl(), pick.getDistrictUrl());
            return new Destination(url, Go.LEARN);
        }

        // 여기부터는 강습이 없는 시설. 이용권이든 대관이든 여기서는 같게 본다.
        // 추천 탭의 물음은 '어디서 하나' 라 답이 '자리를 잡아서 하라' 하나뿐이다.
        //
        // 사람이 확인해 둔 주소가 있으면 그게 곧 대관 창구다
        String rent = firstUsable(pick.getReserveUrl(), pick.getRentalUrl());
        if (rent != null) {
            return new Destination(rent, Go.RENT);
        }

        // 자치구 대관 사이트는 그 구 시설들의 목록이다. 우리 자료에 아무 기록도 없는
        // 시설은 거기 있는지 확인된 바가 없어 보내지 않는다.
        // 근린공원 농구장을 강남구 대관으로 보내 헛걸음시키던 것이 그 경우였다
        if (!programs.isEmpty() && isUsable(pick.getDistrictRentalUrl())) {
            return new Destination(pick.getDistrictRentalUrl(), Go.RENT);
        }

        // 강좌는 있는데 나이 대상이 아니어서 programs 가 빈 시설.
        // courseCount 가 증인이라 자치구 수강신청으로 보내도 헛걸음이 아니다
        if (pick.getCourseCount() > 0) {
            String learn = firstUsable(pick.getHomepageUrl(), pick.getDistrictUrl());
            if (learn != null) {
                return new Destination(learn, Go.LEARN);
            }
        }

        // 창구를 못 찾았으면 홈페이지라도. 신청 화면이 아니라 안내다
        return new Destination(firstUsable(pick.getHomepageUrl()), Go.INFO);
    }

    /** 주소(?userId=)로 받으면 남의 추천을 볼 수 있어 세션에서만 읽는다 */
    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }
}
