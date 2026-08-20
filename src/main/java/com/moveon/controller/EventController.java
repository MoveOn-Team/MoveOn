package com.moveon.controller;

import com.moveon.dto.EventDTO;
import com.moveon.service.IEventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 탭③ 지역 스포츠 행사
 *
 *   SC-030 행사 목록  /event/eventList
 *   SC-031 행사 상세  /event/eventDetail/{eventId}
 *
 * 클래스에 붙인 @RequestMapping("/event") 가 메서드 주소 앞에 모두 붙는다.
 * 메서드에 /event 를 다시 쓰면 /event/event/... 가 되어 404 가 난다.
 * 추천 탭에서 실제로 겪었던 일이다.
 *
 * 현위치는 추천 탭과 같은 방식으로 받는다.
 * 못 받으면 서울시청 좌표로 대신하고 화면은 그대로 보여준다.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/event")
@Controller
public class EventController {

    private final IEventService eventService;

    /** GPS 를 못 받았을 때 쓸 기본 좌표 (서울시청) */
    private static final double DEFAULT_LAT = 37.5665;
    private static final double DEFAULT_LNG = 126.9780;

    /**
     * SC-030 행사 목록
     */
    @GetMapping(value = "/eventList")
    public String eventList(HttpSession session,
                            @RequestParam(value = "sort", required = false) String sort,
                            @RequestParam(value = "lat", required = false) Double lat,
                            @RequestParam(value = "lng", required = false) Double lng,
                            ModelMap model) throws Exception {

        log.info("{}.eventList Start!", this.getClass().getName());

        if (getSessionUserId(session) == null) {
            log.info("{}.eventList End! 비로그인 접근", this.getClass().getName());
            return "redirect:/user/login";
        }

        double myLat = (lat == null) ? DEFAULT_LAT : lat;
        double myLng = (lng == null) ? DEFAULT_LNG : lng;

        String order = IEventService.SORT_DEADLINE.equals(sort)
                ? IEventService.SORT_DEADLINE : IEventService.SORT_NEAR;

        List<EventDTO> events = eventService.getEventList(order, myLat, myLng);

        model.addAttribute("events", events);

        // 정렬 토글에서 지금 눌린 쪽을 표시하고, 반대쪽 링크를 만들 때 쓴다
        model.addAttribute("sort", order);

        // 목록 맨 위의 'O/O 갱신'.
        // 행사마다 넣은 날이 달라서, 그중 가장 최근 날을 대표로 보여준다.
        model.addAttribute("updatedAt", events.stream()
                .map(EventDTO::getCollectedAt)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse((LocalDateTime) null));

        // 상세로 넘어갈 때 현위치를 이어줘야 거리 표시가 달라지지 않는다
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);
        model.addAttribute("usingGps", lat != null && lng != null);

        log.info("{}.eventList End! {}건", this.getClass().getName(), events.size());

        return "event/eventList";
    }

    /**
     * SC-031 행사 상세
     */
    @GetMapping(value = "/eventDetail/{eventId}")
    public String eventDetail(@PathVariable("eventId") int eventId,
                              HttpSession session,
                              @RequestParam(value = "lat", required = false) Double lat,
                              @RequestParam(value = "lng", required = false) Double lng,
                              ModelMap model) throws Exception {

        log.info("{}.eventDetail Start! eventId : {}", this.getClass().getName(), eventId);

        if (getSessionUserId(session) == null) {
            log.info("{}.eventDetail End! 비로그인 접근", this.getClass().getName());
            return "redirect:/user/login";
        }

        double myLat = (lat == null) ? DEFAULT_LAT : lat;
        double myLng = (lng == null) ? DEFAULT_LNG : lng;

        EventDTO event = eventService.getEvent(eventId, myLat, myLng);

        // 없는 번호를 치거나, 아직 검수가 안 끝난 행사를 주소로 직접 들어온 경우.
        // 빈 화면을 보여주느니 목록으로 돌려보낸다.
        if (event == null) {
            log.info("{}.eventDetail End! 없는 행사 : {}", this.getClass().getName(), eventId);
            return "redirect:/event/eventList?lat=" + myLat + "&lng=" + myLng;
        }

        model.addAttribute("event", event);
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        log.info("{}.eventDetail End!", this.getClass().getName());

        return "event/eventDetail";
    }

    /**
     * 세션에서 로그인한 회원 번호를 가져온다. 로그인 전이면 null.
     *
     * 행사 목록 자체는 회원 정보가 없어도 만들 수 있지만,
     * 탭바 안쪽 화면이라 다른 탭과 같은 기준으로 로그인을 확인한다.
     */
    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }

}
