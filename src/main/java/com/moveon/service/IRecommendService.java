package com.moveon.service;

import com.moveon.dto.FacilityDTO;
import com.moveon.dto.ProgramDTO;
import com.moveon.dto.SportDTO;

import java.util.List;

public interface IRecommendService {

    // 지속 적합도 TOP3 (화면에 보여줄 상위 3개)

    List<SportDTO> getTop3(int userId, double lat, double lng) throws Exception;

    // 전체 종목 점수. TOP3 와 종목 상세가 같은 값을 쓰도록 여기서 한 번에 계산한다

    List<SportDTO> getAllScores(int userId, double lat, double lng) throws Exception;

    // 종목 하나의 점수. 목록과 같은 값이 나와야 하므로 같은 계산을 재사용한다

    SportDTO getSportScore(int userId, int sportId, double lat, double lng) throws Exception;

    // 해당 종목을 할 수 있는 가까운 시설 3곳

    List<FacilityDTO> getNearbyFacilities(int sportId, double lat, double lng, int limit) throws Exception;

    // 특정 시설의 해당 종목 강좌

    List<ProgramDTO> getPrograms(int facilityId, int sportId) throws Exception;

}
