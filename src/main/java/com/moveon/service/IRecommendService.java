package com.moveon.service;

import com.moveon.dto.CourseDTO;
import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProfileDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.RentalDTO;
import com.moveon.dto.SportDTO;

import java.util.List;


public interface IRecommendService {

    // 화면 위쪽 성향 요약 카드 (성향 4축 · 나이 · BMI · 현위치 동네)

    ProfileDTO getProfile(int userId, double lat, double lng) throws Exception;

    // 지속 적합도 TOP3 (화면에 보여줄 상위 3개)

    List<SportDTO> getTop3(int userId, double lat, double lng) throws Exception;

    // 전체 종목 점수. TOP3 와 종목 상세가 같은 값을 쓰도록 여기서 한 번에 계산한다

    List<SportDTO> getAllScores(int userId, double lat, double lng) throws Exception;

    // 종목 하나의 점수. 목록과 같은 값이 나와야 하므로 같은 계산을 재사용한다

    SportDTO getSportScore(int userId, int sportId, double lat, double lng) throws Exception;

    // 해당 종목을 할 수 있는 가까운 시설 3곳
    // 회원번호를 받는 것은 '내가 들을 수 있는 강좌 수' 를 함께 세기 위함이다

    List<FacilityDTO> getNearbyFacilities(int userId, int sportId, double lat, double lng, int limit) throws Exception;

    // 걷기·등산은 시설이 아니라 코스로 답한다. 종목 이름으로 평지/산길을 가른다

    List<CourseDTO> getNearbyCourses(String sportName, double lat, double lng, int limit) throws Exception;

    // 특정 시설의 해당 종목 강좌

    List<ProgramDTO> getPrograms(int userId, int facilityId, int sportId) throws Exception;

    // 대관 가능한 가까운 곳. 강좌가 없는 종목에서 대신 보여준다

    List<RentalDTO> getNearbyRentals(int sportId, double lat, double lng, int limit) throws Exception;

}
