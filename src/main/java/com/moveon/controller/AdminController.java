package com.moveon.controller;

import com.moveon.dto.AdminDTO;
import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;
import com.moveon.dto.FacilityDTO;
import com.moveon.service.IAdminService;
import com.moveon.service.IEventSearchService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 화면
 * 세션 키를 회원(SS_USER_NO)과 나눈다. 회원이 로그인했다고 관리자 화면이 열리면 안 된다.
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/admin")
@Controller
public class AdminController {

    private final IAdminService adminService;
    private final IEventSearchService eventSearchService;

    // =====================================================================
    // 로그인
    // =====================================================================

    @GetMapping(value = "/login")
    public String loginPage(HttpSession session) {
        // 이미 로그인했으면 목록으로 보낸다
        return getAdminId(session) == null ? "admin/adminLogin" : "redirect:/admin/eventAdmin";
    }

    @PostMapping(value = "/login")
    public String login(@RequestParam("loginId") String loginId,
                        @RequestParam("password") String password,
                        HttpSession session,
                        ModelMap model) throws Exception {

        log.info("{}.login Start!", this.getClass().getName());

        AdminDTO rDTO = adminService.login(loginId, password);

        if (rDTO == null) {
            model.addAttribute("msg", "아이디 또는 비밀번호를 확인해 주세요.");
            return "admin/adminLogin";
        }

        session.setAttribute("SS_ADMIN_NO", rDTO.getAdminId());
        session.setAttribute("SS_ADMIN_NAME", rDTO.getName());

        log.info("{}.login End! adminId : {}", this.getClass().getName(), rDTO.getAdminId());

        return "redirect:/admin/eventAdmin";
    }

    @GetMapping(value = "/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("SS_ADMIN_NO");
        session.removeAttribute("SS_ADMIN_NAME");
        return "redirect:/admin/login";
    }

    // =====================================================================
    // 행사 목록
    // =====================================================================

    @GetMapping(value = "/eventAdmin")
    public String eventAdmin(@RequestParam(value = "status", required = false) String status,
                             HttpSession session,
                             ModelMap model) throws Exception {

        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }

        List<EventDTO> events = adminService.getAdminEventList(status);

        model.addAttribute("events", events);
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("adminName", session.getAttribute("SS_ADMIN_NAME"));

        return "admin/eventAdmin";
    }

    // =====================================================================
    // 등록 · 수정
    // =====================================================================

    /**
     * eventId 가 없으면 새로 넣는 화면, 있으면 고치는 화면.
     * name 이 오면 검색에서 고른 대회라 제목을 미리 채워 준다.
     */
    @GetMapping(value = "/eventForm")
    public String eventForm(@RequestParam(value = "eventId", required = false) Integer eventId,
                            @RequestParam(value = "name", required = false) String name,
                            HttpSession session,
                            ModelMap model) throws Exception {

        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }

        EventDTO event = (eventId == null) ? new EventDTO() : adminService.getAdminEvent(eventId);
        if (event == null) {
            return "redirect:/admin/eventAdmin";
        }

        // 검색에서 고르고 들어온 경우. 사이트를 찾아 읽고 값까지 미리 채워 둔다
        boolean autoFilled = false;
        if (eventId == null && name != null && !name.isBlank()) {
            event.setTitle(name);
            event.setSource("NAVER_SEARCH");

            // "종류|주소" 로 온다. OFFICIAL(전용 홈페이지) / APPLY(접수처) / INFO(정보 페이지).
            // 무엇인지 화면에 알려 줘야 관리자가 그대로 둘지 판단할 수 있다.
            String found = eventSearchService.findSite(name);
            String site = null;
            if (found != null) {
                int bar = found.indexOf('|');
                if (bar > 0) {
                    model.addAttribute("linkKind", found.substring(0, bar));
                    site = found.substring(bar + 1);
                } else {
                    site = found;
                }
                event.setHomepageUrl(site);
                event.setSourceUrl(site);
            }

            // 사이트를 못 찾았어도 해 본다. 검색 요약만으로 채워지는 대회가 있다
            {
                EventDTO read = eventSearchService.readSite(site, name);
                if (read.getStartDate() != null) {
                    event.setStartDate(read.getStartDate());
                    event.setEndDate(read.getEndDate());
                    autoFilled = true;
                }
                if (read.getApplyEnd() != null) {
                    event.setApplyEnd(read.getApplyEnd());
                    autoFilled = true;
                }
                if (read.getFeeText() != null) {
                    event.setFeeText(read.getFeeText());
                    autoFilled = true;
                }
                if (read.getApplyStart() != null) {
                    event.setApplyStart(read.getApplyStart());
                    autoFilled = true;
                }
                if (read.getPlaceName() != null) {
                    event.setPlaceName(read.getPlaceName());
                    autoFilled = true;

                    // 좌표까지 바로 채운다. 버튼을 누르게 뒀더니 좌표 없는 행사만 쌓였다
                    EventDTO place = eventSearchService.findPlace(read.getPlaceName());
                    if (place != null) {
                        event.setLat(place.getLat());
                        event.setLng(place.getLng());
                        event.setSigungu(place.getSigungu());
                        event.setPlaceName(place.getPlaceName());   // 카카오의 정식 이름
                    }
                }
                if (read.getDistances() != null) {
                    event.setDistances(read.getDistances());
                }
                if (read.getTarget() != null) {
                    event.setTarget(read.getTarget());
                }
                if (read.getContact() != null) {
                    event.setContact(read.getContact());
                }
                if (read.getEventType() != null) {
                    event.setEventType(read.getEventType());
                }
            }
        }

        model.addAttribute("event", event);
        model.addAttribute("isNew", eventId == null);
        model.addAttribute("autoFilled", autoFilled);

        return "admin/eventForm";
    }

    @PostMapping(value = "/eventForm")
    public String saveEvent(@ModelAttribute EventDTO pDTO,
                            HttpSession session) throws Exception {

        Integer adminId = getAdminId(session);
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        log.info("{}.saveEvent Start! eventId : {}", this.getClass().getName(), pDTO.getEventId());

        if (pDTO.getEventId() == 0) {
            // -1 이면 이미 열린 대회. 그냥 목록으로 보내면 왜 안 들어갔는지 알 수 없다
            if (adminService.addEvent(pDTO) < 0) {
                log.info("{}.saveEvent End! 이미 열린 대회", this.getClass().getName());
                return "redirect:/admin/eventForm?past=1";
            }
        } else {
            adminService.modifyEvent(pDTO);
        }

        return "redirect:/admin/eventAdmin";
    }

    // =====================================================================
    // 승인 · 반려
    // =====================================================================

    @PostMapping(value = "/changeStatus")
    public String changeStatus(@RequestParam("eventId") int eventId,
                               @RequestParam("status") String status,
                               HttpSession session) throws Exception {

        Integer adminId = getAdminId(session);
        if (adminId == null) {
            return "redirect:/admin/login";
        }

        int res = adminService.changeStatus(eventId, status, adminId);

        // 공개는 행사일과 좌표가 다 있어야 된다. 왜 안 됐는지 알려 준다.
        if (res < 0) {
            return "redirect:/admin/eventAdmin?fail=" + (res == -1 ? "date" : "pos");
        }

        return "redirect:/admin/eventAdmin";
    }

    // =====================================================================
    // 삭제 (반려는 '대회는 맞는데 지금 것이 아니다', 삭제는 '애초에 대회가 아니었다')
    // 되돌릴 수 없어 POST 로만 받는다. GET 이면 주소만 알아도 지워진다.
    // =====================================================================

    @PostMapping(value = "/deleteEvent")
    public String deleteEvent(@RequestParam("eventId") int eventId,
                              HttpSession session) throws Exception {

        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }

        log.info("{}.deleteEvent Start! eventId : {}", this.getClass().getName(), eventId);

        int res = adminService.removeEvent(eventId);

        // 지워진 게 없으면 이미 남이 지운 것이다
        return "redirect:/admin/eventAdmin" + (res > 0 ? "?deleted=1" : "");
    }

    // =====================================================================
    // 검색 (화면에서 비동기로 부른다)
    // =====================================================================

    /** 어떤 대회가 있는지 찾는다 */
    @GetMapping(value = "/searchEvents")
    @ResponseBody
    public List<EventSearchDTO> searchEvents(@RequestParam(value = "keyword", required = false) String keyword,
                                             @RequestParam(value = "refresh", required = false) String refresh,
                                             HttpSession session) throws Exception {

        if (getAdminId(session) == null) {
            return List.of();
        }
        return eventSearchService.discover(keyword, "1".equals(refresh));
    }

    /** 장소 이름으로 좌표를 얻는다. 등록 화면의 '좌표 찾기' 버튼이 부른다 */
    @GetMapping(value = "/findPlace")
    @ResponseBody
    public Map<String, Object> findPlace(@RequestParam("placeName") String placeName,
                                         HttpSession session) throws Exception {

        Map<String, Object> res = new HashMap<>();

        if (getAdminId(session) == null) {
            res.put("ok", false);
            return res;
        }

        EventDTO place = eventSearchService.findPlace(placeName);
        res.put("ok", place != null);
        if (place != null) {
            res.put("place", place);
        }
        return res;
    }

    // =====================================================================
    // 시설 손보기
    //
    // 공공데이터에는 시설의 신청 주소가 거의 안 들어 있다.
    //   홈페이지가 있는 시설  1,341곳 중 130곳
    // 종목도 이름과 어긋난 곳이 많았다. 배드민턴장인데 배드민턴이 없던 곳이 44곳.
    // 사람이 사이트를 열어 확인하고 채우는 자리라 관리자 화면에 둔다.
    // =====================================================================

    @GetMapping(value = "/facilityAdmin")
    public String facilityAdmin(@RequestParam(value = "gu", required = false) String gu,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "filter", required = false) String filter,
                                HttpSession session,
                                ModelMap model) throws Exception {

        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }

        log.info("{}.facilityAdmin Start! gu : {} / keyword : {}", this.getClass().getName(), gu, keyword);

        model.addAttribute("facilities", adminService.getFacilityList(gu, keyword, filter));
        model.addAttribute("guList", adminService.getGuList());
        model.addAttribute("gu", gu == null ? "" : gu);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("filter", filter == null ? "" : filter);
        model.addAttribute("adminName", session.getAttribute("SS_ADMIN_NAME"));

        return "admin/facilityAdmin";
    }

    @GetMapping(value = "/facilityForm")
    public String facilityForm(@RequestParam("facilityId") int facilityId,
                               HttpSession session,
                               ModelMap model) throws Exception {

        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }

        FacilityDTO facility = adminService.getFacility(facilityId);
        if (facility == null) {
            return "redirect:/admin/facilityAdmin";
        }

        model.addAttribute("facility", facility);
        model.addAttribute("sports", adminService.getFacilitySports(facilityId));
        model.addAttribute("adminName", session.getAttribute("SS_ADMIN_NAME"));

        return "admin/facilityForm";
    }

    /**
     * 저장.
     *
     * 종목 예약주소는 화면에서 reserveUrl_6 처럼 종목번호를 붙여 보낸다.
     * 종목이 스물두 개라 칸을 하나씩 받으면 파라미터가 스물두 줄이 된다.
     */
    @PostMapping(value = "/facilityForm")
    public String saveFacility(@RequestParam("facilityId") int facilityId,
                               @RequestParam(value = "homepageUrl", required = false) String homepageUrl,
                               @RequestParam(value = "rentalUrl", required = false) String rentalUrl,
                               @RequestParam(value = "sportIds", required = false) List<Integer> sportIds,
                               @RequestParam Map<String, String> all,
                               HttpSession session) throws Exception {

        if (getAdminId(session) == null) {
            return "redirect:/admin/login";
        }

        Map<Integer, String> reserveUrls = new HashMap<>();
        for (Map.Entry<String, String> e : all.entrySet()) {
            if (e.getKey().startsWith("reserveUrl_")) {
                reserveUrls.put(Integer.parseInt(e.getKey().substring("reserveUrl_".length())),
                                e.getValue());
            }
        }

        adminService.modifyFacility(facilityId, homepageUrl, rentalUrl, sportIds, reserveUrls);

        // 고치던 자리로 돌아온다. 여러 곳을 잇달아 손보는 화면이라
        // 목록으로 튕기면 검색어와 스크롤을 매번 다시 잡아야 한다.
        return "redirect:/admin/facilityForm?facilityId=" + facilityId + "&saved=1";
    }

    private Integer getAdminId(HttpSession session) {
        Object adminId = session.getAttribute("SS_ADMIN_NO");
        return adminId instanceof Number ? ((Number) adminId).intValue() : null;
    }

}
