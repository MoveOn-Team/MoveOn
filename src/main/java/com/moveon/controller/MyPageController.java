package com.moveon.controller;

import com.moveon.dto.MsgDTO;
import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;
import com.moveon.service.IMyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final IMyPageService myPageService;

    /**
     * 내 정보 메인 페이지 (myPage.jsp)
     */
    @GetMapping("/myPage")
    public String myPage(HttpSession session, Model model) throws Exception {
        Integer loginUserId = (Integer) session.getAttribute("SS_USER_NO");

        if (loginUserId == null) {
            return "redirect:/user/login";
        }

        // 1. 회원 프로필 정보 조회
        UserProfileDTO userProfile = myPageService.getUserProfile(loginUserId);
        model.addAttribute("user", userProfile);

        // 2. 오늘 운동 완료 목록 조회
        List<WorkoutLogDTO> todayWorkoutList = myPageService.getTodayWorkoutList(loginUserId);
        model.addAttribute("todayWorkoutList", todayWorkoutList);

        // 기록 모달의 종목 칩. 표에서 가져와야 종목이 늘어도 화면이 따라간다
        model.addAttribute("sports", myPageService.getRecordableSports());

        model.addAttribute("streak", myPageService.getStreak(loginUserId));

        model.addAttribute("active", "myPage");

        return "user/myPage";
    }

    /**
     * 회원 정보 수정 페이지 호출 (profileEdit.jsp)
     */
    @GetMapping("/profileEdit")
    public String profileEditForm(HttpSession session, Model model) {
        Integer loginUserId = (Integer) session.getAttribute("SS_USER_NO");

        if (loginUserId == null) {
            return "redirect:/user/login";
        }

        UserProfileDTO userProfile = myPageService.getUserProfile(loginUserId);
        model.addAttribute("user", userProfile);

        return "user/profileEdit";
    }

    @GetMapping("/workoutReport")
    public String workoutReport(HttpSession session, Model model) throws Exception {
        Integer loginUserId = (Integer) session.getAttribute("SS_USER_NO");

        if (loginUserId == null) {
            return "redirect:/user/login";
        }

        // 운동 리포트 종합 데이터 조회
        WorkoutReportDTO report = myPageService.getWorkoutReport(loginUserId);
        model.addAttribute("report", report);

        // 코치 글은 화면이 뜬 뒤에 따로 받아 온다. 아래 api 참고.
        model.addAttribute("aiReady", myPageService.canWriteNote(report));

        return "user/workoutReport"; // /WEB-INF/views/user/workoutReport.jsp 호출
    }

    @GetMapping("/copyright")
    public String copyrightPage() {
        return "user/copyright"; // WEB-INF/views/user/copyright.jsp 호출
    }

    /**
     * 리포트의 AI 코치 글.
     *
     * 기록을 남길 때 미리 만들어 두므로 대개 바로 돌아온다.
     * 없을 때만 그 자리에서 만드느라 2~3초 걸린다.
     * 화면은 이 칸만 기다리고 나머지 리포트는 먼저 그린다.
     */
    @GetMapping("/api/reportNote")
    @ResponseBody
    public MsgDTO reportNote(HttpSession session) throws Exception {

        MsgDTO rDTO = new MsgDTO();

        Integer loginUserId = (Integer) session.getAttribute("SS_USER_NO");
        if (loginUserId == null) {
            return rDTO;
        }

        // 못 만들면 msg 가 비어 나가고 화면은 그 칸을 접는다
        rDTO.setMsg(myPageService.getReportNote(loginUserId));
        return rDTO;
    }

    /**
     * 회원 정보 수정 요청 처리 (POST)
     */
    @PostMapping("/profileEdit")
    public String updateProfile(@ModelAttribute UserProfileDTO formDto,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Integer loginUserId = (Integer) session.getAttribute("SS_USER_NO");

        if (loginUserId == null) {
            return "redirect:/user/login";
        }

        // 로그인된 사용자의 ID를 DTO에 바인딩 (보안 목적)
        formDto.setUserId(loginUserId);

        boolean isUpdated = myPageService.updateUserProfile(formDto);

        if (isUpdated) {
            // 수정 성공 시 메시지 전달 후 내 정보 메인으로 리다이렉트
            redirectAttributes.addFlashAttribute("msg", "회원 정보가 성공적으로 수정되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "정보 수정에 실패했습니다.");
        }

        return "redirect:/user/myPage";
    }
    /**
     * 운동 기록 등록 (AJAX 모달 제출)
     */
    @ResponseBody
    @PostMapping("/recordWorkout")
    public MsgDTO recordWorkout(@RequestBody WorkoutLogDTO pDTO, HttpSession session) {
        log.info(this.getClass().getName() + ".recordWorkout Start!");

        MsgDTO msgDTO = new MsgDTO();

        // 1. 세션에서 로그인 사용자 ID 확인
        Integer userId = (Integer) session.getAttribute("SS_USER_NO");
        if (userId == null) {
            msgDTO.setMsg("로그인이 필요한 서비스입니다.");
            return msgDTO;
        }

        pDTO.setUserId(userId);

        try {
            // 2. 운동 기록 저장
            int res = myPageService.insertWorkoutLog(pDTO);

            if (res > 0) {
                msgDTO.setMsg("운동 기록이 성공적으로 저장되었습니다.");
            } else {
                msgDTO.setMsg("운동 기록 저장에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("운동 기록 저장 중 오류 발생", e);
            msgDTO.setMsg("서버 오류가 발생했습니다.");
        }

        log.info(this.getClass().getName() + ".recordWorkout End!");
        return msgDTO;
    }
}