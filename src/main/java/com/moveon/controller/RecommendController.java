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
     * 가까운 공공체육시설 3곳과, 고른 시설의 신청 창구를 내려준다.
     *
     * 강좌 목록은 화면에 내지 않는다. 원본이 2025년 9월 자료라
     * 지금 열리는 강좌·요금과 어긋나서, 틀린 목록을 보여 주느니
     * 신청 페이지로 보내는 편이 낫다고 보았다.
     * programs 는 그래도 읽는다. 강습이 있는 곳인지 빌리는 곳인지 갈라야 해서다.
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

        // 추천 대상이 아닌 종목번호로 들어온 경우.
        // 홈트(category='HOME')는 갈 시설이 없어 추천 계산에서 빠지고, 없는 번호도 마찬가지다.
        // 화면에서 눌러 올 수 있는 길은 없지만 주소를 직접 치면 닿는다.
        // 그대로 두면 예외 대신 제목도 점수도 빈 화면이 떠서 고장으로 보인다.
        if (sport == null) {
            log.info("{}.sportDetail End! 추천 대상이 아닌 종목 : {}", this.getClass().getName(), sportId);
            return "redirect:/recommend/recommendList";
        }

        List<FacilityDTO> facilities = recommendService.getNearbyFacilities(sportId, myLat, myLng, 3);

        // 시설을 고르지 않았으면 가장 가까운 곳의 강좌를 보여준다
        int pickId = (facilityId != null) ? facilityId
                   : (facilities.isEmpty() ? 0 : facilities.get(0).getFacilityId());

        List<ProgramDTO> programs = (pickId == 0)
                ? Collections.emptyList()
                : recommendService.getPrograms(userId, pickId, sportId);

        // 이 종목을 할 시설이 하나도 안 잡혔을 때만 서울시 공공서비스예약을 뒤진다.
        //
        // 예전에는 '강좌가 없으면' 이것을 함께 보여줬는데,
        // 그러면 0m 앞에 시설을 세 곳 띄워 놓고 그 아래에 4.6km 짜리 목록을 또 붙이는 꼴이었다.
        // 위 목록의 시설들도 저마다 빌리는 창구로 이어지므로, 두 벌을 둘 이유가 없다.
        List<RentalDTO> rentals = facilities.isEmpty()
                ? recommendService.getNearbyRentals(sportId, myLat, myLng, 3)
                : Collections.emptyList();

        final int selected = pickId;
        FacilityDTO pick = facilities.stream()
                .filter(f -> f.getFacilityId() == selected)
                .findFirst().orElse(null);

        // 외부로 보낼 링크를 정한다.
        //
        // 갈 곳이 두 갈래다. 배우러 가는 곳(수강신청)과 빌리러 가는 곳(대관)은
        // 같은 시설이라도 신청 창구가 다르다.
        // 그래서 이 시설이 무엇을 가졌는지 먼저 보고 그쪽 창구로 보낸다.
        //
        //   강습이 있으면   강좌 예약 → 시설 홈페이지 → 자치구 수강신청
        //   이용권·대관뿐이면 공공서비스예약 → 시설 대관 → 자치구 대관 → 시설 홈페이지
        //
        // homepage_url 은 한 번 걸러 놓은 값이다.
        // 원본에는 'gwanakgongdan.or.kr' 처럼 여러 시설이 나눠 쓰는 기관 대문이 대부분이었고,
        // 그리로 보내면 회원이 거기서 시설을 다시 찾아 들어가야 했다.
        // 그런 것은 비워서 자치구 창구로 내려보냈다.
        //
        // facilities.guide_url 은 쓰지 않는다.
        // 좌표를 맞출 때 저장한 카카오맵 장소 주소라, 옆의 '길찾기' 버튼과 같은 곳으로 간다.
        boolean hasCourse = programs.stream()
                .anyMatch(p -> "COURSE".equals(p.getProgramType()));

        String linkUrl = null;
        boolean toRental = false;

        if (hasCourse) {
            for (ProgramDTO p : programs) {
                if (isUsableUrl(p.getReservationUrl())) {
                    linkUrl = p.getReservationUrl();
                    break;
                }
            }
            if (linkUrl == null && pick != null && isUsableUrl(pick.getHomepageUrl())) {
                linkUrl = pick.getHomepageUrl();
            }
            if (linkUrl == null && pick != null && isUsableUrl(pick.getDistrictUrl())) {
                linkUrl = pick.getDistrictUrl();
            }
        } else if (!programs.isEmpty()) {
            // 강습은 없고 이용권·대관만 있는 시설.
            // 오금공원테니스장처럼 '평일 4,000원 / 월회원 38,500원' 만 있는 곳이다.
            toRental = true;
            if (pick != null && isUsableUrl(pick.getReserveUrl())) {
                linkUrl = pick.getReserveUrl();          // 서울시 공공서비스예약
            }
            if (linkUrl == null && pick != null && isUsableUrl(pick.getRentalUrl())) {
                linkUrl = pick.getRentalUrl();
            }
            if (linkUrl == null && pick != null && isUsableUrl(pick.getDistrictRentalUrl())) {
                linkUrl = pick.getDistrictRentalUrl();
            }
            if (linkUrl == null && pick != null && isUsableUrl(pick.getHomepageUrl())) {
                linkUrl = pick.getHomepageUrl();
                toRental = false;
            }
        } else if (pick != null) {
            // 강좌 자료가 아예 없는 시설.
            // 그래도 갈 곳이 확인된 곳이면 보낸다.
            // 송파테니스장처럼 우리 자료엔 강좌가 없지만 대관은 받는 곳,
            // 올림픽공원 테니스경기장처럼 운영기관 안내 페이지가 있는 곳이 여기 해당한다.
            // 자치구 창구까지는 쓰지 않는다. 그 목록에 이 시설이 있는지 확인된 바 없어서다.
            if (isUsableUrl(pick.getReserveUrl())) {
                linkUrl = pick.getReserveUrl();
                toRental = true;
            } else if (isUsableUrl(pick.getRentalUrl())) {
                linkUrl = pick.getRentalUrl();
                toRental = true;
            } else if (isUsableUrl(pick.getHomepageUrl())) {
                linkUrl = pick.getHomepageUrl();
            }
        }

        // 단추에 쓸 말. 어디로 가느냐가 아니라 무엇을 하러 가느냐로 적는다.
        // 'OO구 체육시설 강좌' 처럼 목적지를 밝히는 문구도 써 봤는데,
        // 구 이름이 들어가면 길어지기만 하고 화면이 지저분해졌다.
        String linkLabel = null;
        if (linkUrl != null) {
            linkLabel = toRental ? "대관 신청하기" : "예약페이지로 이동";
        }

        // 고른 시설이 '그냥 가서 쓰는 곳' 인지.
        //
        // 근린공원 농구장·풋살장이 여기 해당한다. 자료를 세 군데서 찾아봐도 없다.
        //   강좌 없음 / 자치구 예약 사이트에 없음 / 서울시 공공서비스예약에도 없음
        // 빠뜨린 게 아니라 예약이라는 절차 자체가 없는 개방형 코트다.
        //
        // 아래 '지금 빌릴 수 있는 곳' 이 비었는지는 보지 않는다.
        // 그건 이 시설이 아니라 근처 다른 곳의 이야기라서,
        // 5km 떨어진 유료 구장이 하나 잡혔다고 눈앞의 무료 코트가 유료가 되지는 않는다.
        boolean openAccess = programs.isEmpty() && linkUrl == null;
        model.addAttribute("openAccess", openAccess);

        // 빌리러 가는 시설인지. 화면은 이걸 보고 '강좌가 없어요' 안내를 접는다.
        // 대관하러 온 사람에게 강좌가 없다고 알릴 이유가 없다.
        model.addAttribute("toRental", toRental);

        // 이 동네에서 이 종목이 '배우는 것' 인지 '빌리는 것' 인지.
        //
        // 가까운 세 곳 중 강습이 하나도 없으면 배우러 갈 곳이 아니다.
        // 그때는 목록 제목부터 '지금 빌릴 수 있는 3곳' 으로 바꾼다.
        // 같은 테니스라도 강남에는 강습이 있고 오금동에는 대관뿐이라 동네마다 갈린다.
        model.addAttribute("rentalMode",
                !facilities.isEmpty() && facilities.stream().allMatch(f -> f.getCourseCount() == 0));

        model.addAttribute("sport", sport);
        model.addAttribute("profile", recommendService.getProfile(userId, myLat, myLng));
        model.addAttribute("facilities", facilities);
        model.addAttribute("pickId", pickId);
        // 고른 시설 자체도 넘긴다.
        // 안 넘기면 화면이 pickId 와 같은 것을 찾으려고 facilities 를 다시 훑어야 한다.
        model.addAttribute("pick", pick);
        model.addAttribute("rentals", rentals);
        model.addAttribute("linkUrl", linkUrl);
        model.addAttribute("linkLabel", linkLabel);

        // 다른 시설을 눌렀을 때 같은 현위치로 다시 조회하도록 이어준다
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        log.info("{}.sportDetail End!", this.getClass().getName());

        model.addAttribute("active", "recommend");

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
