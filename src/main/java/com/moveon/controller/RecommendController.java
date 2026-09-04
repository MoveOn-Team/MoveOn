package com.moveon.controller;

import com.moveon.dto.CourseDTO;
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

    /**
     * 우리 시설을 '빌리는 곳' 줄로 바꾼다.
     *
     * 서울시 예약 자료와 한 목록에 거리순으로 섞어야 하는데 담는 그릇이 달라서다.
     * 종류 칸에는 종목 이름을 넣는다. 우리 자료에는 '축구장/풋살장' 같은
     * 코트 종류가 없고, 옆줄과 같은 자리에 주소를 넣으면 혼자만 다른 말을 하게 된다.
     */
    private RentalDTO asRental(FacilityDTO f, String sportName) {
        RentalDTO r = new RentalDTO();
        r.setFacilityId(f.getFacilityId());
        r.setPlaceName(f.getName());
        r.setMinClass(sportName);
        r.setDistanceKm(f.getDistanceKm());
        return r;
    }

    /**
     * 아래 탭바에서 어느 칸을 켜 둘지.
     *
     * 이 컨트롤러의 화면은 전부 추천 탭이라 값이 같다.
     * @ModelAttribute 를 붙이면 스프링이 화면을 그리기 전에 이 메서드를 불러
     * 돌려준 값을 model 에 'active' 라는 이름으로 넣어 준다.
     * 메서드마다 addAttribute 를 적을 필요가 없어진다.
     */
    @ModelAttribute("active")
    public String activeTab() {
        return "recommend";
    }

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

        // 종목 상세로 넘어갈 때 현위치를 그대로 이어줘야 거리가 달라지지 않음.
        // 회원번호는 세션에서 읽으므로 주소에 싣지 않음.
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        // 현위치를 받아 쓴 것인지, 기본 좌표로 계산한 것인지 화면에 알려줌
        model.addAttribute("usingGps", lat != null && lng != null);

        log.info("{}.recommend End!", this.getClass().getName());

        return "recommend/recommendList";
    }

    /**
     * 가까운 공공체육시설 3곳
     * programs 는 읽음.
     * 강습이 있는 곳인지 빌리는 곳인지 구분하기 위함.
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

        // 추천대상이 아닌 종목번호로 들어온 경우.
        // getSportScore 는 추천 대상 종목을 다 계산한 뒤 그중 하나를 골라 줌.
        // 그 안에 없는 번호가 들어오면 null 이 돌아옴.
        // 1) /sportDetail/12   홈트. category='HOME' 이라 계산에서 빠짐.
        // 2) /sportDetail/999  없는 번호
        if (sport == null) {
            log.info("{}.sportDetail End! 추천 대상이 아닌 종목 : {}", this.getClass().getName(), sportId);
            return "redirect:/recommend/recommendList";
        }

        List<FacilityDTO> facilities = recommendService.getNearbyFacilities(userId, sportId, myLat, myLng, LIST_SIZE);

        // 어느 시설을 볼지 정함
        //
        // 목록에서 시설을 누르면 주소에 facilityId 가 붙어 다시 들어옴.
        // 처음 들어왔을 때는 그 값이 없으므로 가장 가까운 곳을 고름.
        // 아무것도 안 고른 채로 시작하면 아래 안내와 단추가 전부 빔.
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

        // 걷기·등산은 시설이 아니라 코스로 답함
        //
        // 이 두 종목은 facility_sports 에 행이 없어 시설 목록이 늘 비었고,
        // '시설을 찾지 못했어요' 만 떠서 추천을 받고도 갈 곳을 못 알려 줬음.
        // 즉시운동 탭 '야외에서' 가 쓰는 것과 같은 서울두드림길 코스를 보여줌.
        List<CourseDTO> courses = "OUTDOOR".equals(sport.getCategory())
                ? recommendService.getNearbyCourses(sport.getName(), myLat, myLng, LIST_SIZE)
                : Collections.emptyList();
        model.addAttribute("courses", courses);

        // 서울시 공공서비스예약. 우리 시설 목록과 나란히 세움.
        //
        // 전에는 시설이 하나도 안 잡혔을 때만 대신 보여줬는데, 그러면 대부분 안 보임.
        // 이 자료의 장소 175곳 중 절반 넘게가 인재개발원 축구장·서남물재생센터 테니스장처럼
        // 공공체육시설 관리대장에 없는 곳이라 우리 시설에 붙일 수도 없음.
        // 붙이려 애쓰는 대신 따로 세움. 강습과 대관은 어차피 다른 이야기라 나란히 두는 게 맞음.
        //
        // 종목을 가리는 것은 매퍼가 함. 배드민턴·탁구·테니스·농구·축구/풋살만 값이 나옴.
        List<RentalDTO> rentals = courses.isEmpty()
                ? new ArrayList<>(recommendService.getNearbyRentals(sportId, myLat, myLng, LIST_SIZE))
                : new ArrayList<>();

        // 우리 시설 중 대관 주소를 가진 곳도 '빌리는 곳' 이라 같은 목록에 넣음.
        //
        // 우장산인조잔디구장은 강좌가 없고 강서구청 예약 화면 주소만 있음.
        // 그런 곳을 위 '가까운 시설' 에 두면 배우러 갈 수 있는 것처럼 읽힘.
        // 두 자료를 이어 붙이지 않고 섞어서 거리순으로 다시 세움.
        // 따로 담아 두면 1.0km 짜리 아래에 2.2km 짜리가 먼저 오고 개수도 넘침.
        List<FacilityDTO> learnList = new ArrayList<>();
        for (FacilityDTO f : facilities) {
            if (isUsableUrl(f.getReserveUrl()) || isUsableUrl(f.getRentalUrl())) {
                rentals.add(asRental(f, sport.getName()));
            } else {
                learnList.add(f);
            }
        }
        rentals.sort(Comparator.comparingDouble(RentalDTO::getDistanceKm));
        if (rentals.size() > LIST_SIZE) {
            rentals = rentals.subList(0, LIST_SIZE);
        }
        model.addAttribute("learnFacilities", learnList);

        Destination dest = chooseDestination(pick, programs);
        String linkUrl = dest.url();
        boolean toRental = dest.go() == Go.RENT;

        // 종목 상세보기 버튼에 쓸 말. 그 너머에서 할 수 있는 일을 그대로 적음.
        String linkLabel = (linkUrl == null) ? null : dest.go().label;

        // 방문 접수만 받는 곳인지. 화면은 이걸로 '가서 접수하세요' 안내를 붙임.
        model.addAttribute("visitOnly", pick != null && pick.isVisitOnly());

        // 고른 시설이 '그냥 가서 쓰는 곳' 인지
        //
        // 근린공원 농구장·풋살장이 여기 해당함
        // 강좌 없음 / 자치구 예약 사이트에 없음 / 서울시 공공서비스예약에도 없음
        // 빠뜨린 게 아니라 예약이라는 절차 자체가 없는 개방형 코트
        //
        // courseCount 를 함께 보는 까닭
        //   programs 는 회원 나이로 걸러 온 목록이라 비는 이유가 둘이다.
        //     ① 이 시설에 이 종목 강좌가 원래 없다    -> 개방형 코트가 맞다
        //     ② 강좌는 있는데 내 나이 대상이 아니다   -> 구민체육관이다
        //   ②까지 개방형으로 안내하면 '그냥 가서 쓰세요' 가 되는데,
        //   강남구민체육관 농구(청소년 강좌만 있음)처럼 걸어 들어갈 수 없는 곳이다.
        //   성인 기준 309쌍이 여기 걸렸다.
        //   courseCount 는 나이를 안 거르고 센 값이라 ①과 ②를 갈라 준다.
        boolean openAccess = programs.isEmpty() && linkUrl == null
                && pick != null && pick.getCourseCount() == 0;
        model.addAttribute("openAccess", openAccess);

        // 빌리러 가는 시설인지. 화면은 이걸 보고 '강좌가 없어요' 안내를 접음.
        // 대관하러 온 사람에게 강좌가 없다고 알릴 이유가 없음.
        model.addAttribute("toRental", toRental);

        // 이 회원에게 이 동네가 '배우는 곳' 인지 '빌리는 곳' 인지
        //
        // 가까운 세 곳 중 들을 수 있는 강습이 하나도 없으면 배우러 갈 곳이 아님.
        // 그때는 목록 제목부터 '지금 빌릴 수 있는 3곳' 으로 바꿈.
        // 같은 테니스라도 강남에는 강습이 있고 오금동에는 대관뿐이라 동네마다 갈림.
        //
        // courseCount 가 아니라 myCourseCount 로 봄.
        // 신월문화체육센터 축구는 강좌가 3건이지만 전부 어린이 대상이라,
        // 성인에게 '가까운 시설 3곳' 이라고 적으면 배울 데가 있는 것처럼 읽힘.
        // 아래 버튼이 나이를 거른 programs 로 목적지를 정하므로 제목도 같은 기준을 씀.
        // 위 목록에 남는 시설 기준으로 봄. 아래로 내려간 대관 시설은 셈에서 뺌.
        model.addAttribute("rentalMode",
                facilities.stream().allMatch(f -> f.getMyCourseCount() == 0));

        model.addAttribute("sport", sport);
        model.addAttribute("profile", recommendService.getProfile(userId, myLat, myLng));
        model.addAttribute("facilities", facilities);
        // 고른 시설. 화면은 이것으로 목록에서 어느 칸을 켤지도 정함.
        model.addAttribute("pick", pick);
        model.addAttribute("rentals", rentals);
        model.addAttribute("linkUrl", linkUrl);
        model.addAttribute("linkLabel", linkLabel);

        // 다른 시설을 눌렀을 때 같은 현위치로 다시 조회하도록 이어줌.
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        log.info("{}.sportDetail End!", this.getClass().getName());

        return "recommend/sportDetail";
    }

    /**
     * 버튼 너머에서 회원이 할 수 있는 일.
     *
     * 단추에 적을 말이 여기서 갈린다. 할 수 없는 일을 적으면 눌러 보고서야 알게 된다.
     */
    private enum Go {
        LEARN("예약페이지로 이동"),
        /** 장소만 빌리는 것. 축구장·농구장·테니스장. 유료든 무료든 */
        RENT("대관 신청하기"),
        /** 신청은 못 하고 보기만 하는 곳. 방문 접수 시설이 여기 해당 */
        INFO("안내 페이지로 이동");

        final String label;
        Go(String label) { this.label = label; }
    }

    /** 회원을 내보낼 곳. 주소와, 거기서 무엇을 할 수 있는지. */
    private record Destination(String url, Go go) {
        static final Destination NONE = new Destination(null, Go.INFO);
    }

    /**
     * 이 시설에서 회원이 갈 곳을 정한다.
     *
     * 배우러 가는 곳(수강신청)과 빌리러 가는 곳(대관)은
     * 같은 시설이라도 신청 창구가 다름
     * 그래서 이 시설이 무엇을 가졌는지 먼저 보고 그쪽 창구로 보냄
     *
     * 방문 접수만 받으면  시설 홈페이지 → 카카오 장소            (신청 아님, 보기만)
     * 강습이 있으면      강좌 예약 → 시설 홈페이지 → 자치구 수강신청
     * 장소만 내주면      공공서비스예약 → 시설 대관 → 자치구 대관
     */
    private Destination chooseDestination(FacilityDTO pick, List<ProgramDTO> programs) {

        if (pick == null) {
            return Destination.NONE;
        }

        // 방문 접수만 받는 시설. 노인복지관 23곳·사회복지관 60여 곳이 여기 해당한다.
        //
        // 강좌는 여는데 온라인 창구가 없어서, 자치구 수강신청으로 보내면
        // 거기엔 이 복지관 프로그램이 없다. 헛걸음이다.
        // 그래서 신청이 아니라 '무엇을 하는 곳인지' 를 보여주는 쪽으로만 보낸다.
        // 자치구 사이트는 아예 쓰지 않는다. 그 구의 체육시설 신청 창구일 뿐이다.
        // 방문 접수만 받는 시설.
        //
        // 홈페이지가 있으면 그것이 답이다.
        // 없으면 카카오 장소라도 준다. 운영시간·전화·사진이 실려 있어 안 없는 것보다는 낫다.
        // 다만 '안내 페이지' 라고 부르지는 않는다. 그건 신청할 수 있는 곳처럼 들리고,
        // 실제로 눌러 보면 옆의 길찾기와 같은 카카오맵이 떠서 속은 기분이 든다.
        // 그래서 아래에서 이 경우만 단추 이름을 '위치·전화 보기' 로 바꾼다.
        if (pick.isVisitOnly()) {
            return new Destination(firstUsable(pick.getHomepageUrl(), pick.getGuideUrl()), Go.INFO);
        }

        boolean hasCourse = programs.stream()
                .anyMatch(p -> "COURSE".equals(p.getProgramType()));

        if (hasCourse) {
            for (ProgramDTO p : programs) {
                if (isUsableUrl(p.getReservationUrl())) {
                    return new Destination(p.getReservationUrl(), Go.LEARN);
                }
            }
            String url = firstUsable(pick.getHomepageUrl(), pick.getDistrictUrl());
            return new Destination(url, Go.LEARN);
        }

        // 여기부터는 강습이 없는 시설. 곧 '장소만 내주는 곳' 이다.
        //
        // 이용권(PASS)이 있든 없든, 유료든 무료든 여기서는 똑같이 대관으로 본다.
        // 추천 탭이 답하는 물음은 '이 종목을 어디서 하나' 이고,
        // 강좌가 없으면 답은 '자리를 잡아서 하라' 하나뿐이기 때문이다.
        // 이용권이 대관과 다른 것은 맞지만, 그 차이는 즉시운동 탭에서만 뜻이 있다.

        // 사람이 확인해 둔 주소가 있으면 그게 곧 대관 창구다.
        // 송파테니스장·우장산 축구장처럼 우리 강좌 자료엔 없지만 대관은 받는 곳이 있다.
        String rent = firstUsable(pick.getReserveUrl(), pick.getRentalUrl());
        if (rent != null) {
            return new Destination(rent, Go.RENT);
        }

        // 자치구 대관 사이트는 그 구 시설들의 대관 목록이다.
        // 우리 자료에 아무 기록도 없는 시설은 그 목록에 있는지 확인된 바가 없어 보내지 않는다.
        // 근린공원 농구장을 강남구 대관으로 보내 헛걸음시키던 것이 그 경우였다.
        if (!programs.isEmpty() && isUsableUrl(pick.getDistrictRentalUrl())) {
            return new Destination(pick.getDistrictRentalUrl(), Go.RENT);
        }

        // 강좌는 있는데 회원 나이 대상이 아니어서 programs 가 빈 시설.
        //
        // 신월문화체육센터(종목 11개·강좌 92건)가 그렇다. 축구 강좌 3건이 전부 어린이라
        // 성인에게는 강습이 없는 것으로 잡히지만, 이런 곳이 자치구 사이트에 빠져 있을 리 없다.
        // 근린공원 코트를 자치구로 보내던 문제와 다른 점은 courseCount 가 증인이라는 것.
        // 강좌를 가진 시설이라는 뜻이므로 자치구 수강신청으로 보내 지금 열리는 것을 보게 한다.
        if (pick.getCourseCount() > 0) {
            String learn = firstUsable(pick.getHomepageUrl(), pick.getDistrictUrl());
            if (learn != null) {
                return new Destination(learn, Go.LEARN);
            }
        }

        // 빌릴 창구를 못 찾았으면 그 시설 홈페이지라도. 그건 신청 화면이 아니라 안내다.
        return new Destination(firstUsable(pick.getHomepageUrl()), Go.INFO);
    }

    /** 앞에서부터 쓸 만한 주소를 고름. 없으면 null. */
    private String firstUsable(String... urls) {
        for (String u : urls) {
            if (isUsableUrl(u)) {
                return u;
            }
        }
        return null;
    }

    /**
     * 링크로 쓸 수 있는 주소인지 봄.
     *
     * 공공데이터에는 값이 비어 있다는 뜻으로 "null" 이라는 글자가 그대로 들어온 행이 많음.
     * (programs.reservation_url 만 249건) 자바의 null 검사로는 걸러지지 않아
     * 그대로 두면 버튼이 깨진 주소로 연결됨.
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
     * 세션에서 로그인한 회원 번호를 가져옴. 로그인 전이면 null.
     *
     * 회원번호를 주소(?userId=)로 받으면 남의 추천 결과를 볼 수 있게 되므로
     * UserController 와 같이 세션에서만 읽음.
     */
    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }
}
