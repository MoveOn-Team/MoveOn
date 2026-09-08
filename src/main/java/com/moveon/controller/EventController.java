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
 * 행사 탭
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
     * 행사 목록
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

        // 목록 맨 위의 'O/O 갱신'. 행사마다 넣은 날이 달라 가장 최근 날을 대표로 쓴다
        model.addAttribute("updatedAt", events.stream()
                .map(EventDTO::getCollectedAt)
                .filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse((LocalDateTime) null));

        // 상세로 넘어갈 때 현위치를 이어줘야 거리 표시가 달라지지 않는다
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);
        model.addAttribute("usingGps", lat != null && lng != null);

        // 하단 탭바에서 어느 탭을 켤지. JSP 의 <c:set> 으로는 tabbar.jsp 까지 전달이 안 된다
        model.addAttribute("active", "event");

        log.info("{}.eventList End! {}건", this.getClass().getName(), events.size());

        return "event/eventList";
    }

    /**
     * 행사 상세
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

        // 없는 번호이거나 검수 전 행사를 주소로 직접 들어온 경우. 목록으로 돌려보낸다
        if (event == null) {
            log.info("{}.eventDetail End! 없는 행사 : {}", this.getClass().getName(), eventId);
            return "redirect:/event/eventList?lat=" + myLat + "&lng=" + myLng;
        }

        model.addAttribute("event", event);
        model.addAttribute("lat", myLat);
        model.addAttribute("lng", myLng);

        // 없으면 서울시청에서 잰 거리를 '현위치에서 12.3km' 라고 말하게 된다
        model.addAttribute("usingGps", lat != null && lng != null);

        model.addAttribute("active", "event");

        log.info("{}.eventDetail End!", this.getClass().getName());

        return "event/eventDetail";
    }

    /** 로그인한 회원 번호. 로그인 전이면 null */
    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }

}
