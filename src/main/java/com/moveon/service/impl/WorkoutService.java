package com.moveon.service.impl;

import com.moveon.dto.CourseDTO;
import com.moveon.dto.CoursePointDTO;
import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.SportDTO;
import com.moveon.dto.UserDTO;
import com.moveon.mapper.ICourseMapper;
import com.moveon.mapper.IFacilityMapper;
import com.moveon.mapper.ISportMapper;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IWorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 즉시 운동하기.
 *
 * 하는 일이 거의 없다. 거르고 줄 세우는 것은 SQL 이 한다.
 * 여기서는 화면에서 넘어온 값이 아는 값인지 확인하고, 몇 개까지 가져올지 정한다.
 *
 * 추천 탭과 달리 점수를 매기지 않는다.
 * '지금 갈 수 있는 곳' 을 찾는 화면이라 기준이 거리 하나뿐이다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WorkoutService implements IWorkoutService {

    private final IFacilityMapper facilityMapper;
    private final ICourseMapper courseMapper;
    private final ISportMapper sportMapper;
    private final IUserMapper userMapper;

    /** 한 화면에 보여줄 개수.
     *
     *  '지금 갈 곳' 을 고르는 화면이라 후보가 많을수록 좋은 것이 아니다.
     *  스무 개를 주면 스무 번째가 13km 짜리라 '지금' 갈 만한 곳이 아니었다.
     *  처음에는 다섯 개만 보이고 '더보기' 로 나머지를 편다. */
    private static final int LIST_LIMIT = 10;

    @Override
    public List<FacilityDTO> getFacilities(int sportId, double lat, double lng) throws Exception {

        log.info("{}.getFacilities Start! sportId : {}", this.getClass().getName(), sportId);

        // 연령대는 null. 즉시운동 탭은 '지금 갈 수 있는 곳' 만 찾는 화면이라
        // '내가 들을 수 있는 강좌' 를 셀 일이 없다. (my_course_count 를 안 씀)
        //
        // bookableOnly 도 false. 신청 창구가 없어 그냥 가서 쓰는 곳이야말로
        // 이 화면이 찾아 줘야 하는 것이다. 축구·농구는 그런 곳이 900쌍 가까이 된다.
        List<FacilityDTO> rList =
                facilityMapper.getNearbyFacilities(sportId, lat, lng, LIST_LIMIT, null, false);

        log.info("{}.getFacilities End! {}곳", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public List<CourseDTO> getCourses(String courseType, double lat, double lng) throws Exception {

        log.info("{}.getCourses Start! courseType : {}", this.getClass().getName(), courseType);

        // 주소창에 ?type=drop 같은 걸 적어 넣어도 SQL 로 흘러가지 않게 여기서 막는다.
        // WALK 평지 / HIKE 산길.
        String type = "HIKE".equals(courseType) ? "HIKE" : "WALK";

        List<CourseDTO> rList = courseMapper.getNearbyCourses(type, lat, lng, LIST_LIMIT);

        log.info("{}.getCourses End! {}개", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public FacilityDTO getFacility(int facilityId, int sportId, double lat, double lng) throws Exception {
        return facilityMapper.getFacility(facilityId, sportId, lat, lng);
    }

    @Override
    public List<ProgramDTO> getPrograms(int userId, int facilityId, int sportId) throws Exception {

        log.info("{}.getPrograms Start!", this.getClass().getName());

        // 나이에 맞는 강좌를 위로 올리려면 회원 나이가 필요하다.
        UserDTO pDTO = new UserDTO();
        pDTO.setUserId(userId);
        UserDTO me = Optional.ofNullable(userMapper.getUserBody(pDTO)).orElseGet(UserDTO::new);

        List<ProgramDTO> rList = facilityMapper.getPrograms(facilityId, sportId, ageBand(me.getAge()));

        log.info("{}.getPrograms End! {}건", this.getClass().getName(), rList.size());

        return rList;
    }

    /**
     * 강좌 대상(target)과 맞춰볼 연령대를 고른다.
     *
     * programs.target 이 '성인', '어린이,청소년' 처럼 사람 말로 적혀 있어
     * 나이를 그대로 비교할 수 없다. 같은 말로 바꿔 놓고 SQL 이 견주게 한다.
     * 추천 탭의 RecommendService 와 같은 기준을 쓴다.
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
    public CourseDTO getCourse(int courseId, double lat, double lng) throws Exception {
        return courseMapper.getCourse(courseId, lat, lng);
    }

    @Override
    public List<SportDTO> getSports(String category) throws Exception {
        return sportMapper.getSportsByCategory(category);
    }

    /**
     * 코스를 이루는 지점들.
     *
     * 예전에는 여기서 좌표를 그림 좌표(x, y)로 옮기고 SVG 선까지 만들었다.
     * 지도 없이 개념도를 그리던 때의 일이다.
     * 지금은 카카오 지도가 좌표를 그대로 받아 그리므로 옮길 일이 없다.
     */
    @Override
    public List<CoursePointDTO> getCoursePoints(int courseId) throws Exception {

        List<CoursePointDTO> points = courseMapper.getCoursePoints(courseId);
        for (CoursePointDTO pt : points) {
            pt.setPointName(cleanPointName(pt.getPointName()));
        }
        return points;
    }

    /**
     * 원본 자료의 지점 이름 중에는 사람에게 쓸모없는 것이 섞여 있다.
     *
     * '문화자원연계부' 가 11번, '제목 없는 경로' 가 3번 나오고,
     * 한 코스 안에서 같은 이름이 세 번 되풀이되기도 한다.
     * 서울시 두드림길 자료를 만들 때 쓴 내부 분류명이라 길 안내에는 도움이 안 된다.
     *
     * 반대로 '수유역 지하철 4호선', '뚝섬유원지' 같은 이름은 그대로 쓸 만하다.
     * 그래서 지우지 않고 쓸모없는 것만 걸러 낸다.
     */
    private String cleanPointName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String s = name.trim();
        // contains 로 본다. matches 로 하면 전체가 똑같아야 해서
        // '노량진길진입로3' 처럼 앞에 글자가 붙은 것을 놓친다.
        if (s.contains("문화자원") || s.contains("제목 없는") || s.contains("진입로")) {
            return null;
        }
        return s;
    }

}
