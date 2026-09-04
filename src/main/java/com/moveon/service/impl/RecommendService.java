package com.moveon.service.impl;

import com.moveon.dto.*;
import com.moveon.mapper.ICourseMapper;
import com.moveon.mapper.IFacilityMapper;
import com.moveon.mapper.ISportMapper;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IRecommendService;
import com.moveon.service.IRegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


/**
 * 지속 적합도 계산
 *
 * 지속 적합도 = 성향 40 + 통계 25 + 신체 15 + 접근성 20   (100점 만점)
 *
 * 네 항목을 각각 0~100 으로 맞춘 뒤 가중치를 곱해 더함.
 * 먼저 100점 만점으로 맞추기 때문에 최종 점수도 0~100 이 되고,
 * 화면에 '81%' 처럼 그대로 쓸 수 있음.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendService implements IRecommendService {

    private final ISportMapper sportMapper;

    private final IUserMapper userMapper; // 신체 점수 계산에 회원의 나이·BMI 가 필요함

    private final IFacilityMapper facilityMapper; // 종목 상세의 시설·강좌 조회

    private final ICourseMapper courseMapper; // 걷기·등산은 시설이 아니라 코스로 답함

    private final IRegionService regionService; // 좌표 -> 동네 이름

    /** 가중치. 합이 1.0 이어야 함 */
    private static final double W_TRAIT = 0.40;
    private static final double W_STAT = 0.25;
    private static final double W_BODY = 0.15;
    private static final double W_ACCESS = 0.20;

    /** 접근성 : 이 거리 이내면 만점, 이 거리 이상이면 0점 */
    private static final double NEAR_KM = 1.0;
    private static final double FAR_KM = 5.0;

    /* -----------------------------------------------------------------
       성향 코드를 화면에 쓸 우리말로 바꾸는 표

       같은 코드라도 자리에 따라 쓰는 말이 다름.
         알약    승부욕 · 낮음      (성향의 세기)
         부제    내 페이스에 잘 맞아요  (성향의 내용)
       ----------------------------------------------------------------- */

    private static final Map<String, String> COMPANION_LABEL =
            Map.of("ALONE", "혼자", "PAIR", "둘이서", "GROUP", "여럿이");

    private static final Map<String, String> COMPETITION_LABEL =
            Map.of("OWN_PACE", "낮음", "ANY", "보통", "WIN", "높음");

    private static final Map<String, String> PLACE_LABEL =
            Map.of("INDOOR", "실내", "ANY", "상관없음", "OUTDOOR", "실외");

    private static final Map<String, String> INTENSITY_LABEL =
            Map.of("LIGHT", "약", "MODERATE", "중", "HARD", "강");

    /** 카드 제목 윗줄에 쓰는 말 */
    private static final Map<String, String> COMPANION_WORD =
            Map.of("ALONE", "혼자서", "PAIR", "둘이서", "GROUP", "여럿이");

    private static final Map<String, String> COMPETITION_WORD =
            Map.of("OWN_PACE", "꾸준히", "ANY", "즐기며", "WIN", "겨루며");

    /** 카드 제목 아랫줄과 상세 부제에 쓰는 말 */
    private static final Map<String, String> PLACE_WORD =
            Map.of("INDOOR", "실내", "ANY", "실내외", "OUTDOOR", "야외");

    private static final Map<String, String> INTENSITY_WORD =
            Map.of("LIGHT", "저강도", "MODERATE", "중강도", "HARD", "고강도");

    /** 상세 부제의 승부욕 표현 */
    private static final Map<String, String> COMPETITION_PACE =
            Map.of("OWN_PACE", "내 페이스", "ANY", "가볍게", "WIN", "승부");

    @Override
    public ProfileDTO getProfile(int userId, double lat, double lng) throws Exception {

        log.info("{}.getProfile Start!", this.getClass().getName());

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = userMapper.getUserBody(pDTO);

        OnboardingDTO oParam = new OnboardingDTO();
        oParam.setUserId(userId);
        OnboardingDTO trait = userMapper.getOnboarding(oParam);

        if (me == null || trait == null) {
            log.info("{}.getProfile End! 회원 또는 성향 정보 없음", this.getClass().getName());
            return null;
        }

        ProfileDTO rDTO = new ProfileDTO();

        rDTO.setAge(me.getAge());
        rDTO.setBmi(trait.getBmi());

        rDTO.setCompanionLabel(label(COMPANION_LABEL, trait.getCompanion()));
        rDTO.setCompetitionLabel(label(COMPETITION_LABEL, trait.getCompetition()));
        rDTO.setPlaceLabel(label(PLACE_LABEL, trait.getPlace()));
        rDTO.setIntensityLabel(label(INTENSITY_LABEL, trait.getIntensity()));

        // 혼자서 꾸준히 / 실내 중강도 타입
        rDTO.setTitleTop(label(COMPANION_WORD, trait.getCompanion())
                + " " + label(COMPETITION_WORD, trait.getCompetition()));
        rDTO.setTitleBottom(label(PLACE_WORD, trait.getPlace())
                + " " + label(INTENSITY_WORD, trait.getIntensity()) + " 타입");

        // 혼자 · 내 페이스 · 실내 · 중강도
        rDTO.setTraitSummary(String.join(" · ",
                label(COMPANION_LABEL, trait.getCompanion()),
                label(COMPETITION_PACE, trait.getCompetition()),
                label(PLACE_WORD, trait.getPlace()),
                label(INTENSITY_WORD, trait.getIntensity())));

        // 동네 이름은 없어도 화면이 뜨도록 실패를 허용함
        rDTO.setRegionName(regionService.getRegionName(lat, lng));

        log.info("{}.getProfile End!", this.getClass().getName());

        return rDTO;
    }

    /** 코드에 맞는 우리말을 찾음. 없는 코드가 들어와도 화면이 깨지지 않게 빈 칸을 줌. */
    private String label(Map<String, String> table, String code) {
        if (code == null) {
            return "";
        }
        return table.getOrDefault(code, "");
    }

    /**
     * 나이를 sport_retention_stats 의 연령대 형식으로 바꿈.
     *
     * 조사 자료가 '20대' '30대' '70대이상' 으로 저장돼 있어 그 형식에 맞춰야 매칭됨.
     */
    private String ageGroup(int age) {
        if (age >= 70) {
            return "70대이상";
        }
        return (age / 10 * 10) + "대";
    }

    /**
     * 30분 운동했을 때 예상 소모 열량(kcal)
     *
     * 열량(kcal) = MET × 몸무게(kg) × 시간(h)
     *
     * MET 는 종목마다, 몸무게는 회원마다 달라 사람과 종목에 따라 값이 바뀜.
     *
     * null 검사는 값이 비어 들어와도 화면이 죽지 않게 하는 안전장치임.
     */
    private int kcal(double met, BigDecimal weightKg) {
        if (weightKg == null || weightKg.doubleValue() <= 0) {
            return 0;
        }
        return (int) Math.round(met * weightKg.doubleValue() * 0.5);
    }

    /**
     * 강좌 대상(target)과 맞춰볼 연령대를 고른다.
     *
     * programs.target 이 '성인', '어린이,청소년' 처럼 사람 말로 적혀 있어
     * 나이를 그대로 비교할 수 없다. 같은 말로 바꿔 놓고 SQL 이 견주게 한다.
     */
    private String ageBand(int age) {
        if (age >= 65) {
            return "SENIOR";
        }
        if (age >= 19) {
            return "ADULT";
        }
        if (age >= 13) {
            return "TEEN";
        }
        return "CHILD";
    }

    @Override
    public List<SportDTO> getTop3(int userId, double lat, double lng) throws Exception {

        log.info("{}.getTop3 Start!", this.getClass().getName());

        List<SportDTO> rList = getAllScores(userId, lat, lng);

        // 4위 이하는 화면에 보여주지 않는다
        List<SportDTO> top3 = rList.size() > 3 ? rList.subList(0, 3) : rList;

        log.info("{}.getTop3 End!", this.getClass().getName());

        return top3;
    }

    @Override
    public List<SportDTO> getAllScores(int userId, double lat, double lng) throws Exception {

        log.info("{}.getAllScores Start!", this.getClass().getName());

        // 회원의 나이·성별 (신체 점수와 통계 조회에 씀)
        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        // 회원이 고른 성향 4축 (아래 점수 루프의 trait 변수와 헷갈리지 않게 myTrait 으로 둠)
        OnboardingDTO oParam = new OnboardingDTO();
        oParam.setUserId(userId);
        OnboardingDTO myTrait = Optional.ofNullable(userMapper.getOnboarding(oParam))
                .orElseGet(OnboardingDTO::new);

        List<SportDTO> rList = sportMapper.getSportsForRecommend(
                myTrait.getCompanion(), myTrait.getCompetition(),
                myTrait.getPlace(), myTrait.getIntensity(),
                me.getGender(), ageGroup(me.getAge()),
                lat, lng);

        // 통계 점수는 다른 종목과 견줘야 하므로 최대 참여율을 먼저 구함
        double maxRate = rList.stream()
                .map(SportDTO::getParticipationRate)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max().orElse(0);

        for (SportDTO dto : rList) {
            double trait = dto.getTraitScore();
            double stat = statScore(dto.getParticipationRate(), maxRate);
            double body = bodyScore(dto, me);
            double access = accessScore(dto.getNearestKm());

            // 네 항목을 따로 담아 두지 않음.
            // 화면은 합계만 쓰고, 어떻게 나온 점수인지는 이 코드로 설명함.
            dto.setTotalScore((int) Math.round(
                    trait * W_TRAIT + stat * W_STAT + body * W_BODY + access * W_ACCESS));

            dto.setEstimatedKcal(kcal(dto.getMetValue(), myTrait.getWeight()));
        }

        // 점수 내림차순. 같으면 sport_id 순으로 고정해 새로고침마다 순서가 바뀌지 않게함
        rList.sort(Comparator.comparingInt(SportDTO::getTotalScore).reversed()
                .thenComparingInt(SportDTO::getSportId));

        // 정렬이 끝난 뒤에 순위를 매김.
        for (int i = 0; i < rList.size(); i++) {
            rList.get(i).setRank(i + 1);
        }

        log.info("{}.getAllScores End!", this.getClass().getName());

        return rList;
    }

    @Override
    public SportDTO getSportScore(int userId, int sportId, double lat, double lng) throws Exception {

        log.info("{}.getSportScore Start! sportId : {}", this.getClass().getName(), sportId);

        // 목록과 상세의 적합도가 다르면 안 되므로 같은 계산을 그대로 씀
        SportDTO rDTO = getAllScores(userId, lat, lng).stream()
                .filter(s -> s.getSportId() == sportId)
                .findFirst()
                .orElse(null);

        log.info("{}.getSportScore End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public List<FacilityDTO> getNearbyFacilities(int userId, int sportId, double lat, double lng, int limit)
            throws Exception {

        log.info("{}.getNearbyFacilities Start!", this.getClass().getName());

        // 나이는 my_course_count 를 세는 데만 쓴다. 시설 자체를 거르지는 않는다.
        // 성인 대상 강좌가 없다고 그 시설을 목록에서 빼면 빌리러 갈 곳까지 사라진다.
        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        // bookableOnly = true. 추천 탭은 꾸준히 다닐 곳을 고르는 화면이라
        // 신청이든 예약이든 창구가 있는 곳만 본다. 동네 코트는 즉시운동 탭이 맡는다.
        List<FacilityDTO> rList =
                facilityMapper.getNearbyFacilities(sportId, lat, lng, limit, ageBand(me.getAge()), true);

        log.info("{}.getNearbyFacilities End! {}곳", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public List<CourseDTO> getNearbyCourses(String sportName, double lat, double lng, int limit)
            throws Exception {

        log.info("{}.getNearbyCourses Start! sportName : {}", this.getClass().getName(), sportName);

        // 점수 계산에서 접근성을 잴 때 쓰는 기준과 같아야 함.
        // SportMapper.getSportsForRecommend 의 IF(s.name = '등산', 'HIKE', 'WALK') 와 짝임.
        // 한쪽만 고치면 '1.2km' 라고 적어 놓고 다른 코스를 보여주게 됨.
        String courseType = "등산".equals(sportName) ? "HIKE" : "WALK";

        List<CourseDTO> rList = courseMapper.getNearbyCourses(courseType, lat, lng, limit);

        log.info("{}.getNearbyCourses End! {}개", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public List<ProgramDTO> getPrograms(int userId, int facilityId, int sportId) throws Exception {

        log.info("{}.getPrograms Start!", this.getClass().getName());

        // 나이에 맞는 강좌를 위로 올리려면 회원 나이가 필요하다.
        // 청소년 강좌가 더 싸서, 요금 순으로만 정렬하면 성인 회원에게 청소년 강좌가 먼저 뜬다.
        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        List<ProgramDTO> rList = facilityMapper.getPrograms(facilityId, sportId, ageBand(me.getAge()));

        log.info("{}.getPrograms End! {}건 (연령대 {})",
                this.getClass().getName(), rList.size(), ageBand(me.getAge()));

        return rList;
    }

    /** 축구/풋살. 이 종목만 풋살장·축구장 두 종류를 함께 봄 */
    private static final int SPORT_FOOTBALL = 8;

    @Override
    public List<RentalDTO> getNearbyRentals(int sportId, double lat, double lng, int limit)
            throws Exception {

        log.info("{}.getNearbyRentals Start! sportId : {}", this.getClass().getName(), sportId);

        List<RentalDTO> rList = (sportId == SPORT_FOOTBALL)
                ? facilityMapper.getNearbyFootballRentals(lat, lng, limit)
                : facilityMapper.getNearbyRentals(sportId, lat, lng, limit);

        log.info("{}.getNearbyRentals End! {}곳", this.getClass().getName(), rList.size());

        return rList;
    }

    /**
     * 통계 점수 (0~100)
     *
     * 참여율을 그대로 쓰면 걷기(40%)와 탁구(2%)의 차이가 너무 커서
     * 하위 종목이 전부 0점으로 눌리기 때문에 제곱근을 씌워 격차를 완만하게 만듦.
     *
     * 조사 자료가 없는 종목은 0점이 아니라 중앙값을 줌.
     * 자료가 없다는 이유로 순위에서 밀리면 안 되기 때문.
     */
    private double statScore(Double rate, double maxRate) {
        if (rate == null || maxRate <= 0) {
            return 50;
        }
        return Math.sqrt(rate / maxRate) * 100;
    }

    /**
     * 접근성 점수 (0~100)
     *
     * 1km 이내면 만점, 5km 이상이면 0점, 그 사이는 직선으로 줄임.
     * 갈 수 있는 시설·코스가 하나도 없으면 0점.
     */
    private double accessScore(Double km) {
        if (km == null) {
            return 0;
        }
        if (km <= NEAR_KM) {
            return 100;
        }
        if (km >= FAR_KM) {
            return 0;
        }
        return (FAR_KM - km) / (FAR_KM - NEAR_KM) * 100;
    }

    /**
     * 신체 점수 (0~100)
     *
     * 회원의 BMI · 나이와 종목의 강도(met_value)를 견줘 기본 70점에서 가감함.
     * 가중치가 15% 로 가장 낮아 최종 점수에 미치는 영향은 최대 15점임
     *
     * BMI 등급은 대한비만학회 기준을 따름
     *   저체중 18.5 미만 / 정상 18.5~23 / 과체중 23~25 / 비만 25 이상
     */
    private double bodyScore(SportDTO dto, UserDTO me) {

        double score = 70;

        double bmi = me.getBmi();
        int age = me.getAge();

        boolean hard = dto.getMetValue() >= 6.5;                       // 숨차는 강도

        /* 관절 부담이 적은 종목
           MET 과는 다른 축이라 강도로 가를 수 없다.
           수영은 MET 6.0 으로 걷기(3.5)보다 훨씬 높지만 무릎에 실리는 것이 없고,
           배드민턴은 MET 5.5 로 더 낮은데 점프와 런지가 많아 부담이 크다. */
        boolean lowImpact = "수영".equals(dto.getName())
                         || "아쿠아로빅".equals(dto.getName())          // 물속이라 체중이 안 실림
                         || "요가/필라테스".equals(dto.getName())       // 매트 운동
                         || "걷기".equals(dto.getName());

        boolean strength = "헬스".equals(dto.getName());               // 근력 운동

        if (bmi >= 23) {                 // 과체중 이상
            if (hard) {
                score -= 20;             // 무릎·심장에 부담
            }
            if (lowImpact) {
                score += 20;             // 체중 실리지 않는 운동을 권한다
            }
        } else if (bmi > 0 && bmi < 18.5) {   // 저체중
            if (strength) {
                score += 15;             // 근육량을 늘리는 편이 낫다
            }
            if (hard) {
                score -= 10;
            }
        }

        if (age >= 60) {                 // 고령
            if (hard) {
                score -= 25;
            }
            if (lowImpact) {
                score += 15;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

}
