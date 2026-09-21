package com.moveon.service.impl;

import com.moveon.dto.SportDTO;
import com.moveon.dto.StreakDTO;
import com.moveon.dto.UserProfileDTO;
import com.moveon.dto.WorkoutLogDTO;
import com.moveon.dto.WorkoutReportDTO;
import com.moveon.mapper.IMyPageMapper;
import com.moveon.service.IAiService;
import com.moveon.service.IMyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService implements IMyPageService {

    private final IMyPageMapper myPageMapper;
    private final IAiService aiService;

    /** 코치 글을 뒤에서 만드는 데 쓴다. ExternalApiConfig 가 만들어 준다 */
    private final ExecutorService searchExecutor;

    /** 코치 글을 쓰려면 기록이 이만큼은 있어야 한다. 한두 번 가지고는 할 말이 없다 */
    private static final int MIN_LOGS = 3;

    /** 회원 x 주 단위로 담아 둔다. 기록을 남길 때 미리 채워 둔다 */
    private final Map<String, String> noteCache = new ConcurrentHashMap<>();

    @Override
    public UserProfileDTO getUserProfile(Integer userId) {
        if (userId == null) {
            return null;
        }
        return myPageMapper.selectUserProfileById(userId);
    }

    @Transactional
    @Override
    public boolean updateUserProfile(UserProfileDTO userProfileDTO) {
        if (userProfileDTO == null || userProfileDTO.getUserId() == null) {
            return false;
        }

        int result = myPageMapper.updateUserProfile(userProfileDTO);
        return result > 0;
    }

    /** 강도는 이 셋뿐이다. 화면에서 다른 값이 와도 표에는 못 들어가게 막는다 */
    private static final Set<String> INTENSITIES = Set.of("LIGHT", "MODERATE", "HARD");

    @Override
    public int insertWorkoutLog(WorkoutLogDTO workoutLogDTO) throws Exception {
        log.info(this.getClass().getName() + ".insertWorkoutLog Start!");

        // 날짜는 화면 말을 듣지 않고 서버가 정한다.
        // 브라우저·서버·DB 가 각자 오늘을 알면 시계 하나만 틀어져도 기록이 샌다.
        workoutLogDTO.setWorkoutDate(LocalDate.now().toString());

        if (!INTENSITIES.contains(workoutLogDTO.getIntensity())) {
            log.warn("모르는 강도라 중간으로 둔다 : {}", workoutLogDTO.getIntensity());
            workoutLogDTO.setIntensity("MODERATE");
        }

        int res = myPageMapper.insertWorkoutLog(workoutLogDTO);

        if (res > 0) {
            refreshNote(workoutLogDTO.getUserId());
        }

        log.info(this.getClass().getName() + ".insertWorkoutLog End!");
        return res;
    }

    /**
     * 오늘 운동 완료 목록 조회 [추가]
     */
    @Override
    public List<WorkoutLogDTO> getTodayWorkoutList(Integer userId) throws Exception {
        log.info(this.getClass().getName() + ".getTodayWorkoutList Start!");
        return myPageMapper.selectTodayWorkoutList(userId, LocalDate.now());
    }

    @Override
    public WorkoutReportDTO getWorkoutReport(Integer userId) throws Exception {
        WorkoutReportDTO report = new WorkoutReportDTO();

        // 1. 이번 주 날짜 범위 계산 (월요일 ~ 일요일)
        LocalDate now = LocalDate.now();
        LocalDate monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        report.setWeekRangeText(monday.format(formatter) + " ~ " + sunday.format(formatter));

        // 2. 이번 주 요약 데이터 조회
        Map<String, Object> thisWeekSummary = myPageMapper.selectThisWeekSummary(userId, monday);
        int thisWeekCount = 0;
        int thisWeekDurationMin = 0;

        if (thisWeekSummary != null) {
            thisWeekCount = ((Number) thisWeekSummary.getOrDefault("weekCount", 0)).intValue();
            thisWeekDurationMin = ((Number) thisWeekSummary.getOrDefault("weekDuration", 0)).intValue();
        }
        report.setThisWeekCount(thisWeekCount);
        report.setThisWeekDurationMin(thisWeekDurationMin);

        // 3. 지난주 대비 증감 횟수
        int lastWeekCount = myPageMapper.selectLastWeekCount(userId, monday);
        report.setWeekDiffCount(thisWeekCount - lastWeekCount);

        // 4. 주요 요약 통계 (칼로리, 연속출석, 전체횟수)
        report.setTotalCalories(myPageMapper.selectThisWeekCalories(userId, monday));
        report.setTotalWorkoutCount(myPageMapper.selectTotalWorkoutCount(userId));

        // 연속 출석. 아래 최장 기록과 같은 조회를 쓰므로 한 번만 센다
        StreakDTO streak = getStreak(userId);
        report.setCurrentStreak(streak.getCurrent());

        // 5. 많이 한 종목 Top 4 & 비율 계산
        List<WorkoutReportDTO.SportStatDTO> topSports = myPageMapper.selectTopSports(userId);
        int totalCount = report.getTotalWorkoutCount();

        if (topSports != null && !topSports.isEmpty() && totalCount > 0) {
            for (WorkoutReportDTO.SportStatDTO sport : topSports) {
                int pct = (int) Math.round(((double) sport.getCount() / totalCount) * 100);
                sport.setPercentage(pct);
            }
        }
        report.setTopSports(topSports);

        // 6. 기록 갱신 영역 (가장 오래 한 운동, 첫 기록)
        Map<String, Object> maxWorkout = myPageMapper.selectMaxDurationWorkout(userId);
        if (maxWorkout != null && !maxWorkout.isEmpty()) {
            report.setMaxDurationSportName((String) maxWorkout.get("sportName"));
            report.setMaxDurationMin(((Number) maxWorkout.get("maxDuration")).intValue());
        } else {
            report.setMaxDurationSportName("-");
            report.setMaxDurationMin(0);
        }

        String firstDate = myPageMapper.selectFirstRecordDate(userId);
        report.setFirstRecordDate(firstDate != null ? firstDate : "기록 없음");
        report.setMaxStreakDays(streak.getMax());

        report.setWeeklyStats(weeklyStats(userId, monday));

        return report;
    }

    /** 차트에 그릴 주 수. 여덟 주는 막대가 얇아져 읽히지 않는다 */
    private static final int CHART_WEEKS = 6;

    /**
     * 최근 몇 주의 주별 운동 횟수.
     *
     * 기록이 없는 주는 조회 결과에 아예 없다. 그대로 쓰면 막대가 빠져
     * 주 간격이 들쭉날쭉해지므로, 여기서 주 목록을 먼저 만들고 값을 얹는다.
     *
     * @param monday 이번 주 월요일
     */
    private List<WorkoutReportDTO.WeeklyStatDTO> weeklyStats(Integer userId, LocalDate monday) {

        LocalDate from = monday.minusWeeks(CHART_WEEKS - 1L);

        // 월요일 -> 횟수
        Map<LocalDate, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : myPageMapper.selectWeeklyCounts(userId, from)) {
            Object d = row.get("monday");
            LocalDate key = (d instanceof java.sql.Date sd) ? sd.toLocalDate() : LocalDate.parse(d.toString());
            counts.put(key, ((Number) row.get("cnt")).intValue());
        }

        DateTimeFormatter label = DateTimeFormatter.ofPattern("M/d");
        List<WorkoutReportDTO.WeeklyStatDTO> rList = new ArrayList<>();

        for (int i = 0; i < CHART_WEEKS; i++) {
            LocalDate week = from.plusWeeks(i);
            WorkoutReportDTO.WeeklyStatDTO dto = new WorkoutReportDTO.WeeklyStatDTO();
            // 마지막 칸은 날짜 대신 '이번' 이라고 적는다. 어디가 지금인지 한눈에 보인다
            dto.setWeekLabel(i == CHART_WEEKS - 1 ? "이번" : week.format(label));
            dto.setCount(counts.getOrDefault(week, 0));
            rList.add(dto);
        }
        return rList;
    }

    @Override
    public List<SportDTO> getRecordableSports() {
        return myPageMapper.selectRecordableSports();
    }

    // =====================================================================
    // 연속 출석
    // =====================================================================

    /**
     * 운동한 날짜를 훑어 연속 일수를 센다.
     *
     * 오늘 아직 안 했다고 기록이 끊긴 것은 아니다. 하루는 기다려 준다.
     * 어제까지 이어졌으면 그 수를 그대로 보여주고, 그제부터 비었으면 0 이다.
     * 저녁에 운동하는 사람이 아침에 열었을 때 0 이 뜨면 그만두게 된다.
     */
    @Override
    public StreakDTO getStreak(Integer userId) {

        StreakDTO rDTO = new StreakDTO();

        // 최근 순으로 온다. 하루에 두 번 했어도 한 줄이다
        List<LocalDate> days = myPageMapper.selectExerciseDates(userId);

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 월~일 일곱 칸
        Set<LocalDate> set = new HashSet<>(days);
        List<Boolean> week = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            week.add(set.contains(monday.plusDays(i)));
        }
        rDTO.setWeek(week);

        if (days.isEmpty()) {
            return rDTO;
        }

        // 지금 이어지는 날 수
        LocalDate latest = days.get(0);
        if (!latest.isBefore(today.minusDays(1))) {
            int n = 1;
            LocalDate prev = latest;
            for (int i = 1; i < days.size(); i++) {
                if (!days.get(i).equals(prev.minusDays(1))) {
                    break;
                }
                prev = days.get(i);
                n++;
            }
            rDTO.setCurrent(n);
        }

        // 가장 길었던 날 수
        int best = 1;
        int run = 1;
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).equals(days.get(i - 1).minusDays(1))) {
                run++;
            } else {
                run = 1;
            }
            best = Math.max(best, run);
        }
        rDTO.setMax(best);

        return rDTO;
    }

    // =====================================================================
    // AI 코치 글
    // =====================================================================

    @Override
    public boolean canWriteNote(WorkoutReportDTO report) {
        return aiService.isReady() && report != null && report.getTotalWorkoutCount() >= MIN_LOGS;
    }

    @Override
    public String getReportNote(Integer userId) throws Exception {

        WorkoutReportDTO report = getWorkoutReport(userId);
        if (!canWriteNote(report)) {
            return null;
        }

        String key = noteKey(userId, report);
        String hit = noteCache.get(key);
        if (hit != null) {
            return hit;
        }

        // 미리 만들어 둔 것이 없을 때만 여기서 만든다. 화면이 2~3초 기다린다.
        String note = aiService.writeReportNote(report);
        if (note != null) {
            noteCache.put(key, note);
        }
        return note;
    }

    /**
     * 기록이 하나 늘면 글도 달라진다. 기록을 남기는 김에 미리 만들어 둔다.
     *
     * 뒤에서 돌리는 까닭은 저장 응답을 붙잡지 않기 위해서다.
     * 운동을 끝낸 사람이 Gemini 를 기다릴 이유가 없다.
     * 여기서 실패해도 리포트를 열 때 다시 만들므로 조용히 넘어간다.
     */
    private void refreshNote(int userId) {
        if (!aiService.isReady()) {
            return;
        }
        searchExecutor.submit(() -> {
            try {
                WorkoutReportDTO report = getWorkoutReport(userId);
                if (!canWriteNote(report)) {
                    return;
                }
                String note = aiService.writeReportNote(report);
                if (note != null) {
                    noteCache.put(noteKey(userId, report), note);
                    log.info("코치 글을 미리 만들어 두었다. userId : {}", userId);
                }
            } catch (Exception e) {
                log.warn("코치 글 미리 만들기 실패 : {}", e.getMessage());
            }
        });
    }

    /** 주가 바뀌면 열쇠가 달라져 지난주 글은 쓰이지 않는다 */
    private String noteKey(Integer userId, WorkoutReportDTO report) {
        return userId + "|" + report.getWeekRangeText();
    }


    /**
     * 홈트 완료 기록.
     *
     * 회원이 따로 적지 않아도 리포트에 올라가야 한다.
     * 운동을 다 하고 나서 또 손으로 적으라고 하면 아무도 안 적는다.
     */
    @Override
    public int addHomeWorkoutLog(int userId, int durationMin, String intensity,
                                 int caloriesKcal, String memo) throws Exception {

        log.info("{}.addHomeWorkoutLog Start! userId : {}", this.getClass().getName(), userId);

        int res = myPageMapper.insertHomeWorkoutLog(userId, LocalDate.now(),
                durationMin, intensity, caloriesKcal, memo);

        if (res > 0) {
            refreshNote(userId);
        }

        log.info("{}.addHomeWorkoutLog End! {}분 / {}kcal", this.getClass().getName(),
                durationMin, caloriesKcal);

        return res;
    }

}
