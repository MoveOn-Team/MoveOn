package com.moveon.controller;

import com.moveon.dto.MsgDTO;
import com.moveon.dto.OnboardingDTO;
import com.moveon.dto.UserDTO;
import com.moveon.service.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private static final String JOIN = "JOIN"; // 회원가입 이메일 인증 구분값
    private static final String FIND_ID = "FIND_ID"; // 아이디 찾기 이메일 인증 구분값
    private static final String FIND_PW = "FIND_PW"; // 비밀번호 찾기 이메일 인증 구분값

    private static final String EMAIL_CODE = "EMAIL_CODE_"; // 세션에 저장할 인증번호 키
    private static final String EMAIL_ADDRESS = "EMAIL_ADDRESS_"; // 세션에 저장할 이메일 키
    private static final String EMAIL_EXPIRES_AT = "EMAIL_EXPIRES_AT_"; // 세션에 저장할 만료 시각 키
    private static final String EMAIL_VERIFIED = "EMAIL_VERIFIED_"; // 세션에 저장할 인증 완료 키

    private static final long EMAIL_CODE_VALID_MILLIS = 5 * 60 * 1000L; // 인증번호 유효시간 5분

    // 비밀번호는 8~16자리이며 영문, 숫자, 특수문자를 각각 하나 이상 포함한다.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{8,16}$"
    );

    private final IUserService userService; // 회원 관련 비즈니스 로직 호출

    /** 로그인 JSP 화면으로 이동한다. */
    @GetMapping(value = "/login")
    public String login() {
        log.info("{}.login Start!", this.getClass().getName());
        log.info("{}.login End!", this.getClass().getName());
        return "user/login";
    }

    /** 회원가입 JSP 화면으로 이동한다. */
    @GetMapping(value = "/join")
    public String join() {
        log.info("{}.join Start!", this.getClass().getName());
        log.info("{}.join End!", this.getClass().getName());
        return "user/join";
    }

    /** 아이디 찾기 JSP 화면으로 이동한다. */
    @GetMapping(value = "/find-id")
    public String findId() {
        log.info("{}.findId Start!", this.getClass().getName());
        log.info("{}.findId End!", this.getClass().getName());
        return "user/find-id";
    }

    /** 비밀번호 찾기 JSP 화면으로 이동한다. */
    @GetMapping(value = "/find-password")
    public String findPassword() {
        log.info("{}.findPassword Start!", this.getClass().getName());
        log.info("{}.findPassword End!", this.getClass().getName());
        return "user/find-password";
    }

    /** 로그인한 회원을 온보딩 JSP 화면으로 이동시킨다. */
    @RequestMapping(value = "/onboarding", method = RequestMethod.GET)
    public String onboarding(HttpSession session) {

        log.info("{}.onboarding Start!", this.getClass().getName());

        if (getSessionUserId(session) == null) {
            log.info("온보딩 화면 접근 결과 : 로그인 필요");
            log.info("{}.onboarding End!", this.getClass().getName());
            return "redirect:/login";
        }

        log.info("온보딩 화면 접근 결과 : 성공");
        log.info("{}.onboarding End!", this.getClass().getName());
        return "user/onboarding";
    }

    /** 회원가입 아이디 중복 체크 */
    @ResponseBody
    @PostMapping(value = "/user/getUserIdExists")
    public UserDTO getUserIdExists(UserDTO pDTO) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());
        log.info("loginId : {}", pDTO.getLoginId());

        if (isBlank(pDTO.getLoginId())) {
            UserDTO rDTO = new UserDTO();
            rDTO.setExists(1);
            log.info("{}.getUserIdExists End!", this.getClass().getName());
            return rDTO;
        }

        UserDTO rDTO = userService.getUserIdExists(pDTO);
        log.info("exists : {}", rDTO.getExists());
        log.info("{}.getUserIdExists End!", this.getClass().getName());
        return rDTO;
    }

    /** 회원가입 이메일 중복 체크 */
    @ResponseBody
    @PostMapping(value = "/user/getEmailExists")
    public UserDTO getEmailExists(UserDTO pDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());
        log.info("email : {}", pDTO.getEmail());

        if (isBlank(pDTO.getEmail())) {
            UserDTO rDTO = new UserDTO();
            rDTO.setExists(1);
            log.info("{}.getEmailExists End!", this.getClass().getName());
            return rDTO;
        }

        UserDTO rDTO = userService.getEmailExists(pDTO);
        log.info("exists : {}", rDTO.getExists());
        log.info("{}.getEmailExists End!", this.getClass().getName());
        return rDTO;
    }

    /** 이메일 인증번호 발송 */
    @ResponseBody
    @PostMapping(value = "/user/sendEmailCode")
    public MsgDTO sendEmailCode(UserDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.sendEmailCode Start!", this.getClass().getName());
        log.info("purpose : {} / loginId : {} / name : {} / email : {}",
                pDTO.getPurpose(), pDTO.getLoginId(), pDTO.getName(), pDTO.getEmail());

        String purpose = normalizePurpose(pDTO.getPurpose());

        if (purpose == null || isBlank(pDTO.getEmail())) {
            log.info("{}.sendEmailCode End!", this.getClass().getName());
            return message("이메일과 인증 목적을 확인해 주세요.");
        }

        MsgDTO validationResult = validateEmailRequest(pDTO, purpose);
        if (validationResult != null) {
            log.info("{}.sendEmailCode End!", this.getClass().getName());
            return validationResult;
        }

        try {
            String emailCode = userService.sendEmailCode(pDTO.getEmail());

            session.setAttribute(EMAIL_CODE + purpose, emailCode);
            session.setAttribute(EMAIL_ADDRESS + purpose, pDTO.getEmail());
            session.setAttribute(
                    EMAIL_EXPIRES_AT + purpose,
                    System.currentTimeMillis() + EMAIL_CODE_VALID_MILLIS
            );
            session.removeAttribute(EMAIL_VERIFIED + purpose);

            log.info("{}.sendEmailCode End!", this.getClass().getName());
            return message("인증번호를 발송했습니다.");

        } catch (Exception e) {
            log.error("인증번호 메일 발송 실패", e);
            log.info("{}.sendEmailCode End!", this.getClass().getName());
            return message("인증번호 발송에 실패했습니다.");
        }
    }

    /** 이메일 인증번호 확인 */
    @ResponseBody
    @PostMapping(value = "/user/verifyEmailCode")
    public MsgDTO verifyEmailCode(UserDTO pDTO, HttpSession session) {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());
        log.info("purpose : {} / email : {}", pDTO.getPurpose(), pDTO.getEmail());

        String purpose = normalizePurpose(pDTO.getPurpose());

        if (purpose == null || isBlank(pDTO.getEmail()) || isBlank(pDTO.getEmailCode())) {
            log.info("{}.verifyEmailCode End!", this.getClass().getName());
            return message("인증 정보를 확인해 주세요.");
        }

        String savedCode = (String) session.getAttribute(EMAIL_CODE + purpose);
        String savedEmail = (String) session.getAttribute(EMAIL_ADDRESS + purpose);
        Long expiresAt = (Long) session.getAttribute(EMAIL_EXPIRES_AT + purpose);

        if (savedCode == null || savedEmail == null || expiresAt == null) {
            log.info("{}.verifyEmailCode End!", this.getClass().getName());
            return message("인증번호를 먼저 요청해 주세요.");
        }

        if (System.currentTimeMillis() > expiresAt) {
            clearEmailSession(session, purpose);
            log.info("{}.verifyEmailCode End!", this.getClass().getName());
            return message("인증번호가 만료되었습니다.");
        }

        if (!savedEmail.equals(pDTO.getEmail()) || !savedCode.equals(pDTO.getEmailCode())) {
            log.info("{}.verifyEmailCode End!", this.getClass().getName());
            return message("인증번호가 일치하지 않습니다.");
        }

        session.setAttribute(EMAIL_VERIFIED + purpose, savedEmail);
        session.removeAttribute(EMAIL_CODE + purpose);
        session.removeAttribute(EMAIL_EXPIRES_AT + purpose);

        log.info("{}.verifyEmailCode End!", this.getClass().getName());
        return message("이메일 인증이 완료되었습니다.");
    }

    /** 회원가입 */
    @ResponseBody
    @PostMapping(value = "/user/insertUserInfo")
    public MsgDTO insertUserInfo(UserDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());
        log.info("loginId : {} / name : {} / email : {} / ageConfirmed : {} / termsAgreed : {}",
                pDTO.getLoginId(), pDTO.getName(), pDTO.getEmail(),
                pDTO.isAgeConfirmed(), pDTO.isTermsAgreed());

        if (isBlank(pDTO.getName()) || isBlank(pDTO.getLoginId()) ||
                isBlank(pDTO.getEmail()) || isBlank(pDTO.getPassword())) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("필수 입력값을 확인해 주세요.");
        }

        if (!isValidPassword(pDTO.getPassword())) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("비밀번호는 8~16자리 영문, 숫자, 특수문자를 포함해야 합니다.");
        }

        if (!pDTO.getPassword().equals(pDTO.getPasswordConfirm())) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("비밀번호 확인이 일치하지 않습니다.");
        }

        if (!pDTO.isAgeConfirmed() || !pDTO.isTermsAgreed()) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("필수 약관에 동의해 주세요.");
        }

        if (!isEmailVerified(session, JOIN, pDTO.getEmail())) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("이메일 인증을 완료해 주세요.");
        }

        if (userService.getUserIdExists(pDTO).getExists() == 1) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("이미 사용 중인 아이디입니다.");
        }

        if (userService.getEmailExists(pDTO).getExists() == 1) {
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("이미 가입된 이메일입니다.");
        }

        try {
            int result = userService.insertUserInfo(pDTO);

            if (result > 0) {
                clearEmailSession(session, JOIN);
                log.info("회원가입 처리 결과 : 성공");
                log.info("{}.insertUserInfo End!", this.getClass().getName());
                return message("회원가입이 완료되었습니다.");
            }

            log.info("회원가입 처리 결과 : 실패");
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("회원가입에 실패했습니다.");

        } catch (DataIntegrityViolationException e) {
            log.warn("회원가입 중 아이디 또는 이메일 중복 발생");
            log.info("{}.insertUserInfo End!", this.getClass().getName());
            return message("이미 사용 중인 아이디 또는 이메일입니다.");
        }
    }

    /** 로그인 */
    @ResponseBody
    @PostMapping(value = "/user/loginProc")
    public MsgDTO loginProc(UserDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!", this.getClass().getName());
        log.info("loginId : {}", pDTO.getLoginId());

        if (session.getAttribute("SS_USER_ID") != null) {
            log.info("{}.loginProc End!", this.getClass().getName());
            return message("이미 로그인되어 있습니다.");
        }

        if (isBlank(pDTO.getLoginId()) || isBlank(pDTO.getPassword())) {
            log.info("{}.loginProc End!", this.getClass().getName());
            return message("아이디와 비밀번호를 입력해 주세요.");
        }

        UserDTO rDTO = userService.login(pDTO);

        if (rDTO.getUserId() == 0) {
            log.info("로그인 처리 결과 : 실패");
            log.info("{}.loginProc End!", this.getClass().getName());
            return message("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        session.setAttribute("SS_USER_NO", rDTO.getUserId());
        session.setAttribute("SS_USER_ID", rDTO.getLoginId());
        session.setAttribute("SS_USER_NAME", rDTO.getName());

        log.info("로그인 처리 결과 : 성공 / userId : {}", rDTO.getUserId());
        log.info("{}.loginProc End!", this.getClass().getName());
        return message("로그인되었습니다.");
    }

    /** 로그아웃 */
    @ResponseBody
    @PostMapping(value = "/user/logout")
    public MsgDTO logout(HttpSession session) {
        log.info("{}.logout Start!", this.getClass().getName());
        session.invalidate();
        log.info("{}.logout End!", this.getClass().getName());
        return message("로그아웃되었습니다.");
    }

    /** 아이디 찾기 */
    @ResponseBody
    @PostMapping(value = "/user/searchUserId")
    public UserDTO searchUserId(UserDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.searchUserId Start!", this.getClass().getName());
        log.info("name : {} / email : {}", pDTO.getName(), pDTO.getEmail());

        if (!isEmailVerified(session, FIND_ID, pDTO.getEmail())) {
            UserDTO rDTO = new UserDTO();
            rDTO.setExists(0);
            log.info("{}.searchUserId End!", this.getClass().getName());
            return rDTO;
        }

        UserDTO rDTO = userService.searchUserId(pDTO);

        if (rDTO.getExists() == 1) {
            clearEmailSession(session, FIND_ID);
        }

        log.info("아이디 찾기 처리 결과 exists : {}", rDTO.getExists());
        log.info("{}.searchUserId End!", this.getClass().getName());
        return rDTO;
    }

    /** 비밀번호 찾기 대상 확인 */
    @ResponseBody
    @PostMapping(value = "/user/searchPassword")
    public UserDTO searchPassword(UserDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.searchPassword Start!", this.getClass().getName());
        log.info("loginId : {} / email : {}", pDTO.getLoginId(), pDTO.getEmail());

        if (!isEmailVerified(session, FIND_PW, pDTO.getEmail())) {
            UserDTO rDTO = new UserDTO();
            rDTO.setExists(0);
            log.info("{}.searchPassword End!", this.getClass().getName());
            return rDTO;
        }

        UserDTO rDTO = userService.searchPassword(pDTO);

        if (rDTO.getExists() == 1) {
            session.setAttribute("RESET_LOGIN_ID", pDTO.getLoginId());
            session.setAttribute("RESET_EMAIL", pDTO.getEmail());
        }

        log.info("비밀번호 찾기 대상 확인 결과 exists : {}", rDTO.getExists());
        log.info("{}.searchPassword End!", this.getClass().getName());
        return rDTO;
    }

    /** 비밀번호 재설정 */
    @ResponseBody
    @PostMapping(value = "/user/newPassword")
    public MsgDTO newPassword(UserDTO pDTO, HttpSession session) throws Exception {

        log.info("{}.newPassword Start!", this.getClass().getName());
        log.info("loginId : {} / email : {}", pDTO.getLoginId(), pDTO.getEmail());

        String resetLoginId = (String) session.getAttribute("RESET_LOGIN_ID");
        String resetEmail = (String) session.getAttribute("RESET_EMAIL");

        if (resetLoginId == null || resetEmail == null ||
                !resetLoginId.equals(pDTO.getLoginId()) || !resetEmail.equals(pDTO.getEmail())) {
            log.info("{}.newPassword End!", this.getClass().getName());
            return message("비밀번호 찾기와 이메일 인증을 먼저 완료해 주세요.");
        }

        if (!isValidPassword(pDTO.getPassword())) {
            log.info("{}.newPassword End!", this.getClass().getName());
            return message("비밀번호는 8~16자리 영문, 숫자, 특수문자를 포함해야 합니다.");
        }

        if (!pDTO.getPassword().equals(pDTO.getPasswordConfirm())) {
            log.info("{}.newPassword End!", this.getClass().getName());
            return message("비밀번호 확인이 일치하지 않습니다.");
        }

        int result = userService.updatePassword(pDTO);

        if (result > 0) {
            session.removeAttribute("RESET_LOGIN_ID");
            session.removeAttribute("RESET_EMAIL");
            clearEmailSession(session, FIND_PW);
            log.info("비밀번호 변경 처리 결과 : 성공");
            log.info("{}.newPassword End!", this.getClass().getName());
            return message("비밀번호가 변경되었습니다.");
        }

        log.info("비밀번호 변경 처리 결과 : 실패");
        log.info("{}.newPassword End!", this.getClass().getName());
        return message("비밀번호 변경에 실패했습니다.");
    }

    /** 로그인한 회원의 온보딩 정보 저장 */
    @ResponseBody
    @PostMapping(value = "/user/onboarding")
    public MsgDTO saveOnboarding(
            @RequestBody OnboardingDTO pDTO,
            HttpSession session
    ) throws Exception {

        log.info("{}.saveOnboarding Start!", this.getClass().getName());

        Integer userId = getSessionUserId(session);
        if (userId == null) {
            log.info("{}.saveOnboarding End!", this.getClass().getName());
            return message("로그인이 필요합니다.");
        }

        pDTO.setUserId(userId);

        try {
            int result = userService.saveOnboarding(pDTO);

            log.info("온보딩 저장 결과 : {} / userId : {}", result > 0 ? "성공" : "실패", userId);
            log.info("{}.saveOnboarding End!", this.getClass().getName());
            return result > 0
                    ? message("온보딩 정보가 저장되었습니다.")
                    : message("온보딩 정보 저장에 실패했습니다.");

        } catch (IllegalArgumentException e) {
            log.info("온보딩 입력값 확인 실패 : {}", e.getMessage());
            log.info("{}.saveOnboarding End!", this.getClass().getName());
            return message(e.getMessage());
        }
    }

    /** 로그인한 회원의 온보딩 정보 조회 */
    @ResponseBody
    @GetMapping(value = "/user/onboarding")
    public OnboardingDTO getOnboarding(HttpSession session) throws Exception {

        log.info("{}.getOnboarding Start!", this.getClass().getName());

        Integer userId = getSessionUserId(session);
        if (userId == null) {
            log.info("{}.getOnboarding End!", this.getClass().getName());
            return new OnboardingDTO();
        }

        OnboardingDTO pDTO = new OnboardingDTO();
        pDTO.setUserId(userId);

        OnboardingDTO rDTO = userService.getOnboarding(pDTO);
        log.info("온보딩 완료 여부 : {} / userId : {}", rDTO.isOnboardingCompleted(), userId);
        log.info("{}.getOnboarding End!", this.getClass().getName());
        return rDTO;
    }

    /** 인증 목적에 맞는 회원 정보인지 확인한다. */
    private MsgDTO validateEmailRequest(UserDTO pDTO, String purpose) throws Exception {

        if (JOIN.equals(purpose)) {
            if (userService.getEmailExists(pDTO).getExists() == 1) {
                return message("이미 가입된 이메일입니다.");
            }

        } else if (FIND_ID.equals(purpose)) {
            if (isBlank(pDTO.getName()) ||
                    userService.searchUserId(pDTO).getExists() != 1) {
                return message("일치하는 회원 정보가 없습니다.");
            }

        } else if (FIND_PW.equals(purpose)) {
            if (isBlank(pDTO.getLoginId()) ||
                    userService.searchPassword(pDTO).getExists() != 1) {
                return message("일치하는 회원 정보가 없습니다.");
            }
        }

        return null;
    }

    /** 입력된 이메일 인증 목적을 정해진 구분값으로 변환한다. */
    private String normalizePurpose(String purpose) {

        if (purpose == null) {
            return null;
        }

        String value = purpose.trim().toUpperCase();

        if (JOIN.equals(value) || FIND_ID.equals(value) || FIND_PW.equals(value)) {
            return value;
        }

        return null;
    }

    /** 세션에 해당 이메일의 인증 완료 정보가 있는지 확인한다. */
    private boolean isEmailVerified(HttpSession session, String purpose, String email) {
        Object verifiedEmail = session.getAttribute(EMAIL_VERIFIED + purpose);
        return email != null && email.equals(verifiedEmail);
    }

    /** 사용이 끝난 이메일 인증 정보를 세션에서 제거한다. */
    private void clearEmailSession(HttpSession session, String purpose) {
        session.removeAttribute(EMAIL_CODE + purpose);
        session.removeAttribute(EMAIL_ADDRESS + purpose);
        session.removeAttribute(EMAIL_EXPIRES_AT + purpose);
        session.removeAttribute(EMAIL_VERIFIED + purpose);
    }

    /** 비밀번호가 화면에서 정한 형식에 맞는지 확인한다. */
    private boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /** 문자열이 비어 있거나 공백만 있는지 확인한다. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 세션에서 로그인한 회원 번호를 가져온다. */
    private Integer getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute("SS_USER_NO");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }

    /** 화면에 전달할 실행 결과 메시지를 만든다. */
    private MsgDTO message(String msg) {
        MsgDTO rDTO = new MsgDTO();
        rDTO.setMsg(msg);
        return rDTO;
    }
}
