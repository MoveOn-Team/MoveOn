package com.moveon.service.impl;

import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.SportDTO;
import com.moveon.dto.UserDTO;
import com.moveon.mapper.IFacilityMapper;
import com.moveon.mapper.ISportMapper;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IRecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 지속 적합도 계산
 *
 *   지속 적합도 = 성향 40 + 통계 25 + 신체 15 + 접근성 20   (100점 만점)
 *
 * 네 항목을 각각 0~100 으로 맞춘 뒤 가중치를 곱해 더한다.
 * 먼저 100점 만점으로 맞추기 때문에 최종 점수도 0~100 이 되고,
 * 화면에 '81%' 처럼 그대로 쓸 수 있다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendService implements IRecommendService {

    private final ISportMapper sportMapper;

    private final IUserMapper userMapper; // 신체 점수 계산에 회원의 나이·BMI 가 필요하다

    private final IFacilityMapper facilityMapper; // 종목 상세의 시설·강좌 조회

    /** 가중치. 합이 1.0 이어야 한다 */
    private static final double W_TRAIT = 0.40;
    private static final double W_STAT = 0.25;
    private static final double W_BODY = 0.15;
    private static final double W_ACCESS = 0.20;

    /** 접근성 : 이 거리 이내면 만점, 이 거리 이상이면 0점 */
    private static final double NEAR_KM = 1.0;
    private static final double FAR_KM = 5.0;

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

        List<SportDTO> rList = sportMapper.getSportsForRecommend(userId, lat, lng);

        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = java.util.Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        // 통계 점수는 다른 종목과 견줘야 하므로 최대 참여율을 먼저 구한다
        double maxRate = rList.stream()
                .map(SportDTO::getParticipationRate)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max().orElse(0);

        for (SportDTO dto : rList) {
            double trait = dto.getTraitScore();
            double stat = statScore(dto.getParticipationRate(), maxRate);
            double body = bodyScore(dto, me);
            double access = accessScore(dto.getNearestKm());

            dto.setTraitPoint((int) Math.round(trait * W_TRAIT));
            dto.setStatPoint((int) Math.round(stat * W_STAT));
            dto.setBodyPoint((int) Math.round(body * W_BODY));
            dto.setAccessPoint((int) Math.round(access * W_ACCESS));

            dto.setTotalScore((int) Math.round(
                    trait * W_TRAIT + stat * W_STAT + body * W_BODY + access * W_ACCESS));
        }

        // 점수 내림차순. 같으면 sport_id 순으로 고정해 새로고침마다 순서가 바뀌지 않게 한다
        rList.sort(Comparator.comparingInt(SportDTO::getTotalScore).reversed()
                .thenComparingInt(SportDTO::getSportId));

        log.info("{}.getAllScores End!", this.getClass().getName());

        return rList;
    }

    @Override
    public SportDTO getSportScore(int userId, int sportId, double lat, double lng) throws Exception {

        log.info("{}.getSportScore Start! sportId : {}", this.getClass().getName(), sportId);

        // 목록과 상세의 적합도가 다르면 안 되므로 같은 계산을 그대로 쓴다
        SportDTO rDTO = getAllScores(userId, lat, lng).stream()
                .filter(s -> s.getSportId() == sportId)
                .findFirst()
                .orElse(null);

        log.info("{}.getSportScore End!", this.getClass().getName());

        return rDTO;
    }

    @Override
    public List<FacilityDTO> getNearbyFacilities(int sportId, double lat, double lng, int limit)
            throws Exception {

        log.info("{}.getNearbyFacilities Start!", this.getClass().getName());

        List<FacilityDTO> rList = facilityMapper.getNearbyFacilities(sportId, lat, lng, limit);

        log.info("{}.getNearbyFacilities End! {}곳", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public List<ProgramDTO> getPrograms(int facilityId, int sportId) throws Exception {

        log.info("{}.getPrograms Start!", this.getClass().getName());

        List<ProgramDTO> rList = facilityMapper.getPrograms(facilityId, sportId);

        log.info("{}.getPrograms End! {}건", this.getClass().getName(), rList.size());

        return rList;
    }

    /**
     * 통계 점수 (0~100)
     *
     * 참여율을 그대로 쓰면 걷기(40%)와 탁구(2%)의 차이가 너무 커서
     * 하위 종목이 전부 0점으로 눌린다. 제곱근을 씌워 격차를 완만하게 만든다.
     *
     * 조사 자료가 없는 종목은 0점이 아니라 중앙값을 준다.
     * 자료가 없다는 이유로 순위에서 밀리면 안 되기 때문이다.
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
     * 1km 이내면 만점, 5km 이상이면 0점, 그 사이는 직선으로 줄인다.
     * 갈 수 있는 시설·코스가 하나도 없으면 0점이다. 갈 곳이 없으면 지속할 수 없다.
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
     * 회원의 BMI · 나이와 종목의 강도(met_value)를 견줘 기본 70점에서 가감한다.
     * 가중치가 15% 로 가장 낮아 최종 점수에 미치는 영향은 최대 15점이다.
     * 너무 정교하게 만들 필요는 없고, 무리가 되는 조합만 걸러주면 된다.
     *
     * BMI 등급은 대한비만학회 기준을 따른다.
     *   저체중 18.5 미만 / 정상 18.5~23 / 과체중 23~25 / 비만 25 이상
     */
    private double bodyScore(SportDTO dto, UserDTO me) {

        double score = 70;

        double bmi = me.getBmi();
        int age = me.getAge();

        boolean hard = dto.getMetValue() >= 6.5;                       // 숨차는 강도
        boolean lowImpact = "수영".equals(dto.getName())
                         || "걷기".equals(dto.getName());              // 관절 부담이 적은 종목
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
