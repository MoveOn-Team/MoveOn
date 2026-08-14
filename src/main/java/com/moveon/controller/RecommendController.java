package com.moveon.controller;

import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.SportDTO;
import com.moveon.service.IRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 탭① 맞춤 운동 추천
 *
 *   SC-010 맞춤 운동 추천  /recommend
 *   SC-011 종목 상세       /recommend/{sportId}   (아직 미구현)
 *
 * 현위치는 안드로이드에서 GPS 로 받아 넘겨준다.
 * 좌표를 못 받으면(권한 거부·실내) 서울시청 좌표로 대체하고 화면은 그대로 보여준다.
 */
@Slf4j
@RequiredArgsConstructor
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
    public String recommend(@RequestParam(value = "userId", defaultValue = "1") int userId,
                            @RequestParam(value = "lat", required = false) Double lat,
                            @RequestParam(value = "lng", required = false) Double lng,
                            ModelMap model) throws Exception {

        log.info("{}.recommend Start!", this.getClass().getName());

        double myLat = (lat == null) ? DEFAULT_LAT : lat;
        double myLng = (lng == null) ? DEFAULT_LNG : lng;

        log.info("userId : {} / lat : {} / lng : {}", userId, myLat, myLng);

        List<SportDTO> top3 = recommendService.getTop3(userId, myLat, myLng);

        model.addAttribute("top3", top3);

        log.info("{}.recommend End!", this.getClass().getName());

        return "recommend/recommendList";
    }

    /**
     * SC-011 종목 상세
     *
     * 가까운 공공체육시설 3곳과, 그중 첫 번째 시설의 운영 강좌를 함께 내려준다.
     * 다른 시설을 눌렀을 때 강좌만 바꿔 끼우는 건 화면에서 비동기로 처리한다.
     */
    @GetMapping(value = "/recommend/{sportId}")
    public String sportDetail(@PathVariable("sportId") int sportId,
                              @RequestParam(value = "userId", defaultValue = "1") int userId,
                              @RequestParam(value = "lat", required = false) Double lat,
                              @RequestParam(value = "lng", required = false) Double lng,
                              @RequestParam(value = "facilityId", required = false) Integer facilityId,
                              ModelMap model) throws Exception {

        log.info("{}.sportDetail Start! sportId : {}", this.getClass().getName(), sportId);

        double myLat = (lat == null) ? DEFAULT_LAT : lat;
        double myLng = (lng == null) ? DEFAULT_LNG : lng;

        SportDTO sport = recommendService.getSportScore(userId, sportId, myLat, myLng);
        List<FacilityDTO> facilities = recommendService.getNearbyFacilities(sportId, myLat, myLng, 3);

        // 시설을 고르지 않았으면 가장 가까운 곳의 강좌를 보여준다
        int pickId = (facilityId != null) ? facilityId
                   : (facilities.isEmpty() ? 0 : facilities.get(0).getFacilityId());

        List<ProgramDTO> programs = (pickId == 0)
                ? java.util.Collections.emptyList()
                : recommendService.getPrograms(pickId, sportId);

        // 외부로 보낼 링크를 정한다
        //   1순위 강좌 예약 페이지 -> 2순위 시설 안내 페이지 -> 3순위 시설 홈페이지
        // 공공데이터에 링크가 없는 경우가 많아, 무엇으로 연결되는지에 따라 버튼 문구도 바꾼다.
        String linkUrl = null;
        String linkLabel = null;

        for (ProgramDTO p : programs) {
            if (p.getReservationUrl() != null && !p.getReservationUrl().isBlank()) {
                linkUrl = p.getReservationUrl();
                linkLabel = "예약페이지로 이동";
                break;
            }
        }
        if (linkUrl == null) {
            final int selected = pickId;
            FacilityDTO pick = facilities.stream()
                    .filter(f -> f.getFacilityId() == selected)
                    .findFirst().orElse(null);
            if (pick != null) {
                if (pick.getGuideUrl() != null && !pick.getGuideUrl().isBlank()) {
                    linkUrl = pick.getGuideUrl();
                    linkLabel = "안내페이지로 이동";
                } else if (pick.getHomepageUrl() != null && !pick.getHomepageUrl().isBlank()) {
                    linkUrl = pick.getHomepageUrl();
                    linkLabel = "안내페이지로 이동";
                }
            }
        }

        model.addAttribute("sport", sport);
        model.addAttribute("facilities", facilities);
        model.addAttribute("pickId", pickId);
        model.addAttribute("programs", programs);
        model.addAttribute("linkUrl", linkUrl);
        model.addAttribute("linkLabel", linkLabel);

        log.info("{}.sportDetail End!", this.getClass().getName());

        return "recommend/sportDetail";
    }

}
