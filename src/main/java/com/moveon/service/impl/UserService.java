package com.moveon.service.impl;

import com.moveon.dto.MailDTO;
import com.moveon.dto.OnboardingDTO;
import com.moveon.dto.UserDTO;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IMailService;
import com.moveon.service.IUserService;
import com.moveon.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private static final Set<String> GENDERS = Set.of("M", "F");
    private static final Set<String> COMPANIONS = Set.of("ALONE", "PAIR", "GROUP");
    private static final Set<String> COMPETITIONS = Set.of("OWN_PACE", "ANY", "WIN");
    private static final Set<String> PLACES = Set.of("INDOOR", "ANY", "OUTDOOR");
    private static final Set<String> INTENSITIES = Set.of("LIGHT", "MODERATE", "HARD");

    private final IUserMapper userMapper;
    private final IMailService mailService;

    @Override
    public UserDTO getUserIdExists(UserDTO pDTO) throws Exception {
        return Optional.ofNullable(userMapper.getUserIdExists(pDTO)).orElseGet(UserDTO::new);
    }

    @Override
    public UserDTO getEmailExists(UserDTO pDTO) throws Exception {
        return Optional.ofNullable(userMapper.getEmailExists(pDTO)).orElseGet(UserDTO::new);
    }

    @Override
    public String sendEmailCode(String email) throws Exception {

        String emailCode = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));

        MailDTO mailDTO = new MailDTO();
        mailDTO.setToMail(email);
        mailDTO.setTitle("[MOVE:ON] 이메일 인증번호입니다.");
        mailDTO.setContents(
            "<h2>MOVE:ON 이메일 인증</h2>" +
            "<p>아래 인증번호를 화면에 입력해 주세요.</p>" +
            "<p style='font-size:24px;font-weight:bold;'>" + emailCode + "</p>" +
            "<p>인증번호는 5분 동안 사용할 수 있습니다.</p>"
        );

        mailService.doSendMail(mailDTO);
        return emailCode;
    }

    @Override
    public int insertUserInfo(UserDTO pDTO) throws Exception {

        String salt = createSalt();
        pDTO.setSalt(salt);
        pDTO.setPassword(hashPassword(pDTO.getPassword(), salt));

        int result = userMapper.insertUserInfo(pDTO);

        if (result > 0) {
            sendWelcomeMail(pDTO);
        }

        return result;
    }

    @Override
    public UserDTO login(UserDTO pDTO) throws Exception {

        UserDTO rDTO = Optional.ofNullable(userMapper.getLoginUser(pDTO)).orElseGet(UserDTO::new);

        if (rDTO.getUserId() == 0 || rDTO.getSalt() == null || rDTO.getPassword() == null) {
            return new UserDTO();
        }

        String inputPassword = hashPassword(pDTO.getPassword(), rDTO.getSalt());

        if (!inputPassword.equals(rDTO.getPassword())) {
            return new UserDTO();
        }

        rDTO.setPassword(null);
        rDTO.setSalt(null);
        rDTO.setExists(1);
        return rDTO;
    }

    @Override
    public UserDTO searchUserId(UserDTO pDTO) throws Exception {

        UserDTO rDTO = Optional.ofNullable(userMapper.searchUserId(pDTO)).orElseGet(UserDTO::new);

        if (rDTO.getLoginId() == null) {
            rDTO.setExists(0);
        }

        return rDTO;
    }

    @Override
    public UserDTO searchPassword(UserDTO pDTO) throws Exception {

        UserDTO rDTO = Optional.ofNullable(userMapper.searchPassword(pDTO)).orElseGet(UserDTO::new);

        if (rDTO.getUserId() == 0) {
            rDTO.setExists(0);
        }

        return rDTO;
    }

    @Override
    public int updatePassword(UserDTO pDTO) throws Exception {

        String salt = createSalt();
        pDTO.setSalt(salt);
        pDTO.setPassword(hashPassword(pDTO.getPassword(), salt));

        return userMapper.updatePassword(pDTO);
    }

    @Override
    public int saveOnboarding(OnboardingDTO pDTO) throws Exception {

        validateOnboarding(pDTO);

        pDTO.setGender(normalizeCode(pDTO.getGender()));
        pDTO.setCompanion(normalizeCode(pDTO.getCompanion()));
        pDTO.setCompetition(normalizeCode(pDTO.getCompetition()));
        pDTO.setPlace(normalizeCode(pDTO.getPlace()));
        pDTO.setIntensity(normalizeCode(pDTO.getIntensity()));

        // BMI 는 계산해서 넣지 않는다.
        // users.bmi 가 키·몸무게로 자동 계산되는 가상 컬럼이라 DB 가 알아서 채운다.
        // 여기서 setBmi 를 해도 updateOnboarding 이 쓰지 않아 아무 일도 일어나지 않는다.

        return userMapper.updateOnboarding(pDTO);
    }

    @Override
    public OnboardingDTO getOnboarding(OnboardingDTO pDTO) throws Exception {

        OnboardingDTO rDTO = Optional.ofNullable(userMapper.getOnboarding(pDTO))
            .orElseGet(OnboardingDTO::new);

        rDTO.setLoggedIn(true);
        rDTO.setOnboardingCompleted(isOnboardingCompleted(rDTO));

        if (rDTO.getBmi() != null) {
            rDTO.setBmiStatus(getBmiStatus(rDTO.getBmi()));
        }

        return rDTO;
    }


    /** BMI 수치에 맞는 화면 표시 문구를 반환한다. */
    private String getBmiStatus(BigDecimal bmi) {
        if (bmi.compareTo(BigDecimal.valueOf(18.5)) < 0) {
            return "저체중";
        }
        if (bmi.compareTo(BigDecimal.valueOf(23)) < 0) {
            return "정상 체중";
        }
        if (bmi.compareTo(BigDecimal.valueOf(25)) < 0) {
            return "과체중";
        }
        return "비만";
    }

    /** 온보딩 필수 입력값과 정해진 코드값을 확인한다. */
    private void validateOnboarding(OnboardingDTO pDTO) {

        // 화면이 안 고른 값을 빈 문자열("")로 보내는 경우가 있다.
        // null 과 ""를 섞어 두면 아래 판단이 어긋나므로 먼저 null 로 맞춘다.
        if (isBlank(pDTO.getGender())) pDTO.setGender(null);
        if (isBlank(pDTO.getCompanion())) pDTO.setCompanion(null);
        if (isBlank(pDTO.getCompetition())) pDTO.setCompetition(null);
        if (isBlank(pDTO.getPlace())) pDTO.setPlace(null);
        if (isBlank(pDTO.getIntensity())) pDTO.setIntensity(null);

        // 성향 4축은 어느 경우에나 필수다
        if (pDTO.getCompanion() == null || pDTO.getCompetition() == null ||
            pDTO.getPlace() == null || pDTO.getIntensity() == null) {
            throw new IllegalArgumentException("성향 항목을 모두 선택해 주세요.");
        }

        if (!COMPANIONS.contains(normalizeCode(pDTO.getCompanion())) ||
            !COMPETITIONS.contains(normalizeCode(pDTO.getCompetition())) ||
            !PLACES.contains(normalizeCode(pDTO.getPlace())) ||
            !INTENSITIES.contains(normalizeCode(pDTO.getIntensity()))) {
            throw new IllegalArgumentException("선택한 온보딩 항목을 확인해 주세요.");
        }

        // 신체정보는 '전부 오거나 전부 안 오거나' 둘 중 하나여야 한다.
        //   첫 진단      네 값이 모두 온다
        //   다시 진단    성향만 다시 고르므로 네 값이 모두 비어 온다
        // 일부만 오면 BMI 를 계산할 수 없어 중간 상태가 저장된다.
        boolean anyBody = pDTO.getBirthDate() != null || pDTO.getGender() != null ||
                          pDTO.getHeight() != null || pDTO.getWeight() != null;
        boolean allBody = pDTO.getBirthDate() != null && pDTO.getGender() != null &&
                          pDTO.getHeight() != null && pDTO.getWeight() != null;

        if (!anyBody) {
            return; // 성향만 다시 고른 경우. 여기서 끝낸다
        }
        if (!allBody) {
            throw new IllegalArgumentException("신체 정보를 모두 입력해 주세요.");
        }

        if (pDTO.getBirthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("생년월일을 확인해 주세요.");
        }

        if (pDTO.getHeight().compareTo(BigDecimal.ZERO) <= 0 ||
            pDTO.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("키와 몸무게를 확인해 주세요.");
        }

        if (!GENDERS.contains(normalizeCode(pDTO.getGender()))) {
            throw new IllegalArgumentException("선택한 온보딩 항목을 확인해 주세요.");
        }
    }

    /** 값이 없거나 공백뿐인지 확인한다. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 온보딩 필수 정보가 모두 저장되었는지 확인한다. */
    private boolean isOnboardingCompleted(OnboardingDTO pDTO) {
        return pDTO.getBirthDate() != null && pDTO.getGender() != null &&
            pDTO.getHeight() != null && pDTO.getWeight() != null && pDTO.getBmi() != null &&
            pDTO.getCompanion() != null && pDTO.getCompetition() != null &&
            pDTO.getPlace() != null && pDTO.getIntensity() != null;
    }

    /** 화면에서 받은 선택 코드를 대문자로 통일한다. */
    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String createSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String hashPassword(String password, String salt) {
        return EncryptUtil.encHashSHA256(password + salt);
    }

    private void sendWelcomeMail(UserDTO pDTO) {

        try {
            MailDTO mailDTO = new MailDTO();
            mailDTO.setToMail(pDTO.getEmail());
            mailDTO.setTitle("[MOVE:ON] 회원가입이 완료되었습니다.");
            mailDTO.setContents(
                "<h2>MOVE:ON 회원가입 완료</h2>" +
                "<p>" + pDTO.getName() + "님, 회원가입을 환영합니다.</p>"
            );
            mailService.doSendMail(mailDTO);

        } catch (Exception e) {
            // 가입 자체는 성공했으므로 안내 메일 실패만 로그로 남긴다.
            log.warn("회원가입 안내 메일 발송 실패", e);
        }
    }
}
