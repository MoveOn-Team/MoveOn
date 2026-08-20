package com.moveon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping("/workout")
public class WorkoutController {

    @GetMapping("/workoutList")
    public String workoutList(@RequestParam(value = "tab", defaultValue = "facility") String tab, ModelMap model) {

        if ("outdoor".equals(tab)) {
            List<Map<String, Object>> outdoorList = new ArrayList<>();
            Map<String, Object> o1 = new HashMap<>();
            o1.put("id", 101); o1.put("name", "봉제산 둘레길"); o1.put("distance", "0.8km");
            o1.put("info1", "3.2km · 약 60분 · 보통"); o1.put("info2", "순환형 · 5호선 까치산역"); o1.put("isSelected", true);
            outdoorList.add(o1);

            Map<String, Object> o2 = new HashMap<>();
            o2.put("id", 102); o2.put("name", "우장산 근린공원 산책로"); o2.put("distance", "1.0km");
            o2.put("info1", "1.8km · 약 30분 · 쉬움"); o2.put("info2", "무장애 구간 · 5호선 우장산역");
            outdoorList.add(o2);

            Map<String, Object> o3 = new HashMap<>();
            o3.put("id", 103); o3.put("name", "강서둘레길 3코스 · 강서한강길"); o3.put("distance", "4.8km");
            o3.put("info1", "6.5km · 약 100분 · 쉬움"); o3.put("info2", "평지 · 9호선 양천향교역");
            outdoorList.add(o3);

            Map<String, Object> o4 = new HashMap<>();
            o4.put("id", 104); o4.put("name", "강서둘레길 1코스 · 개화산숲길"); o4.put("distance", "5.4km");
            o4.put("info1", "4.0km · 약 70분 · 보통"); o4.put("info2", "순환형 · 5호선 개화산역");
            outdoorList.add(o4);

            model.addAttribute("outdoorList", outdoorList);
        } else if ("home".equals(tab)) {
            // [집에서] 탭 루틴 데이터
            List<Map<String, Object>> prepList = new ArrayList<>();
            prepList.add(Map.of("name", "목 돌리기", "count", "10회 x 2세트"));
            prepList.add(Map.of("name", "어깨 스트레칭", "count", "30초 x 2세트"));

            List<Map<String, Object>> mainList = new ArrayList<>();
            mainList.add(Map.of("name", "윗몸일으키기", "count", "10회 x 3세트"));
            mainList.add(Map.of("name", "스쿼트", "count", "15회 x 3세트"));
            mainList.add(Map.of("name", "플랭크", "count", "30초 x 2세트"));

            List<Map<String, Object>> coolList = new ArrayList<>();
            coolList.add(Map.of("name", "햄스트링 스트레칭", "count", "30초 x 2세트"));

            model.addAttribute("prepList", prepList);
            model.addAttribute("mainList", mainList);
            model.addAttribute("coolList", coolList);
        } else {
            List<Map<String, Object>> facilityList = new ArrayList<>();
            Map<String, Object> f1 = new HashMap<>();
            f1.put("id", 1); f1.put("name", "강서구민올림픽체육센터 수영장"); f1.put("address", "강서구 등촌동 707-3"); f1.put("distance", "2.2km"); f1.put("isSelected", true);
            facilityList.add(f1);

            Map<String, Object> f2 = new HashMap<>();
            f2.put("id", 2); f2.put("name", "공항동문화체육센터 수영장"); f2.put("address", "강서구 송정로 45"); f2.put("distance", "3.3km");
            facilityList.add(f2);

            Map<String, Object> f3 = new HashMap<>();
            f3.put("id", 3); f3.put("name", "목동청소년수련관 수영장"); f3.put("address", "양천구 목동서로 143"); f3.put("distance", "3.3km");
            facilityList.add(f3);

            Map<String, Object> f4 = new HashMap<>();
            f4.put("id", 4); f4.put("name", "마곡레포츠센터 수영장"); f4.put("address", "강서구 양천로 251"); f4.put("distance", "3.6km");
            facilityList.add(f4);

            model.addAttribute("facilityList", facilityList);
        }

        model.addAttribute("currentTab", tab);
        model.addAttribute("active", "workout");

        return "workout/workoutList";
    }

    /**
     * 운동 진행 화면 (두 번째 사진)
     */
    @GetMapping("/workoutPlay")
    public String workoutPlay(ModelMap model) {
        model.addAttribute("active", "workout");
        return "workout/workoutPlay";
    }

    /**
     * 운동 완료 화면 (세 번째 사진)
     */
    @GetMapping("/workoutResult")
    public String workoutResult(ModelMap model) {
        model.addAttribute("active", "workout");
        return "workout/workoutResult";
    }

    @GetMapping("/workoutDetail/{id}")
    public String workoutDetail(@PathVariable("id") Long id, ModelMap model) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", id);
        detail.put("name", "강서구민올림픽체육센터 수영장");
        detail.put("category", "수영");
        detail.put("address", "서울 강서구 등촌동 707-3");
        detail.put("distance", "2.2km");
        detail.put("price", "2,000원");
        detail.put("weekdayTime", "06:00~22:00");
        detail.put("weekendTime", "09:00~18:00");
        detail.put("closedDay", "매주 월요일, 1월 1일");
        detail.put("priceStandard", "2시간 / 성인 1인");
        detail.put("capacity", "60명");
        detail.put("phone", "02-880-0000");
        detail.put("subwayInfo", "강서구 등촌동 707-3 · 9호선 등촌역 인근");

        model.addAttribute("detail", detail);
        model.addAttribute("active", "workout");

        return "workout/workoutDetail";
    }
}