package com.moveon.service.impl;

import com.moveon.dto.AdminDTO;
import com.moveon.dto.EventDTO;
import com.moveon.dto.FacilityDTO;
import com.moveon.mapper.IAdminMapper;
import com.moveon.service.IAdminService;
import com.moveon.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * 관리자 로그인과 행사 등록·검수
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService implements IAdminService {

    private final IAdminMapper adminMapper;

    @Override
    public AdminDTO login(String loginId, String password) throws Exception {

        log.info("{}.login Start! loginId : {}", this.getClass().getName(), loginId);

        AdminDTO rDTO = adminMapper.getAdmin(loginId);

        // 아이디가 없을 때와 비밀번호가 틀렸을 때를 화면에서 구분하지 않는다.
        // 구분하면 어떤 아이디가 있는지 알려 주는 셈이 된다. UserService 와 같은 방식이다.
        if (rDTO == null) {
            log.info("{}.login End! 없는 아이디", this.getClass().getName());
            return null;
        }

        String hashed = EncryptUtil.encHashSHA256(password + rDTO.getSalt());
        if (!hashed.equals(rDTO.getPassword())) {
            log.info("{}.login End! 비밀번호 불일치", this.getClass().getName());
            return null;
        }

        // 세션에 담기 전에 지운다. 화면까지 흘러갈 이유가 없다.
        rDTO.setPassword(null);
        rDTO.setSalt(null);

        log.info("{}.login End! adminId : {}", this.getClass().getName(), rDTO.getAdminId());

        return rDTO;
    }

    @Override
    public List<EventDTO> getEventList(String status) throws Exception {

        // 주소창에 아무 값이나 넣어도 SQL 로 흘러가지 않게 여기서 막는다.
        String s = (PENDING.equals(status) || PUBLISHED.equals(status) || REJECTED.equals(status))
                ? status : null;

        return adminMapper.getAdminEventList(s);
    }

    @Override
    public EventDTO getEvent(int eventId) throws Exception {
        return adminMapper.getAdminEvent(eventId);
    }

    /**
     * 이미 열린 대회인지 본다.
     *
     * 검색에는 지난 대회 후기가 섞여 들어오고, 이름만 보면 지난 것인지 알 수 없다.
     * 관리자가 눈으로 거르는 데도 한계가 있어 저장할 때 한 번 더 막는다.
     * 지난 대회가 들어가면 사용자 목록에서는 안 보이지만(end_date 조건),
     * 관리자 화면만 지저분해지고 같은 대회를 또 넣게 된다.
     */
    private boolean isPast(EventDTO pDTO) {
        return pDTO.getStartDate() != null
                && pDTO.getStartDate().isBefore(java.time.LocalDate.now());
    }

    @Override
    public int addEvent(EventDTO pDTO) throws Exception {

        if (isPast(pDTO)) {
            log.info("이미 열린 대회라 저장하지 않는다 : {} ({})",
                    pDTO.getTitle(), pDTO.getStartDate());
            return -1;
        }

        // 좌표나 행사일이 없어도 저장은 받는다.
        //
        // 대회는 알겠는데 아직 공지가 안 올라온 경우가 많다.
        //   '서울마라톤' 은 3월 대회라 지금은 사이트에 아무것도 없다.
        //   '2026 여의도 나이트런' 도 이름만 검색에 걸린다.
        // 이런 걸 저장조차 못 하게 하면 관리자가 그 대회의 존재를 잊어버리고,
        // 다음에 검색할 때 또 같은 걸 붙잡고 시간을 쓴다.
        //
        // 그래서 '검수 대기' 로 담아 두고 나중에 채우게 한다.
        // 사용자 화면은 PUBLISHED 만 읽으므로 덜 채워진 것이 새어 나갈 일은 없다.
        // 공개로 바꿀 때 changeStatus 가 값이 다 있는지 본다.


        log.info("{}.addEvent Start! {}", this.getClass().getName(), pDTO.getTitle());

        // 검색으로 찾은 것도, 손으로 넣은 것도 처음에는 PENDING 이다.
        // 넣는 사람이 곧 확인하는 사람이라도 마찬가지다.
        // 대회 사이트를 열어 접수기간·참가비를 찾다 보면 한 번에 못 끝내기 때문에,
        // PENDING 이 '아직 덜 채운 초안' 자리를 겸한다.
        if (pDTO.getStatus() == null || pDTO.getStatus().isBlank()) {
            pDTO.setStatus(PENDING);
        }
        if (pDTO.getSource() == null || pDTO.getSource().isBlank()) {
            pDTO.setSource("MANUAL");
        }

        int res = adminMapper.insertEvent(pDTO);

        log.info("{}.addEvent End! eventId : {}", this.getClass().getName(), pDTO.getEventId());

        return res == 0 ? 0 : pDTO.getEventId();
    }

    @Override
    public int modifyEvent(EventDTO pDTO) throws Exception {
        log.info("{}.modifyEvent Start! eventId : {}", this.getClass().getName(), pDTO.getEventId());
        return adminMapper.updateEvent(pDTO);
    }

    @Override
    public int changeStatus(int eventId, String status, int adminId) throws Exception {

        // 공개로 올릴 때만 값이 다 있는지 본다.
        //   행사일이 없으면 목록의 날짜 칸을 못 그린다.
        //   좌표가 없으면 거리 계산이 안 되어 목록에서 통째로 빠진다.
        // 둘 다 없으면 공개해 봐야 사용자에게 안 보이거나 화면이 깨진다.
        if (PUBLISHED.equals(status)) {
            EventDTO e = adminMapper.getAdminEvent(eventId);
            if (e == null) {
                return -1;
            }
            if (e.getStartDate() == null) {
                log.info("행사일이 없어 공개하지 않는다 : {}", e.getTitle());
                return -1;
            }
            if (e.getLat() == 0 || e.getLng() == 0) {
                log.info("좌표가 없어 공개하지 않는다 : {}", e.getTitle());
                return -2;
            }
        }


        log.info("{}.changeStatus Start! {} -> {}", this.getClass().getName(), eventId, status);

        if (!PENDING.equals(status) && !PUBLISHED.equals(status) && !REJECTED.equals(status)) {
            log.warn("아는 상태값이 아니다 : {}", status);
            return 0;
        }

        return adminMapper.updateStatus(eventId, status, adminId);
    }

    // =====================================================================
    // 시설 손보기
    // =====================================================================

    @Override
    public List<FacilityDTO> getFacilityList(String gu, String keyword, String filter) throws Exception {
        return adminMapper.getAdminFacilityList(gu, keyword, filter);
    }

    @Override
    public FacilityDTO getFacility(int facilityId) throws Exception {
        return adminMapper.getAdminFacility(facilityId);
    }

    @Override
    public List<Map<String, Object>> getFacilitySports(int facilityId) throws Exception {
        return adminMapper.getAdminFacilitySports(facilityId);
    }

    @Override
    public List<String> getGuList() throws Exception {
        return adminMapper.getGuList();
    }

    /**
     * 종목은 지우고 다시 넣는다.
     *
     * 체크를 푼 것과 새로 넣은 것을 따로 셈하지 않아도 되고,
     * 예약주소만 바뀐 경우도 같은 길로 처리된다.
     * 한 시설의 종목은 많아야 스물두 줄이라 지우고 넣는 값이 싸다.
     *
     * 한 덩어리로 묶는다. 주소는 저장됐는데 종목이 날아간 상태로 끝나면 안 된다.
     */
    @Transactional
    @Override
    public void modifyFacility(int facilityId, String homepageUrl, String rentalUrl,
                               List<Integer> sportIds, Map<Integer, String> reserveUrls) throws Exception {

        log.info("{}.modifyFacility Start! facilityId : {}", this.getClass().getName(), facilityId);

        adminMapper.updateFacilityUrls(facilityId, homepageUrl, rentalUrl);
        adminMapper.deleteFacilitySports(facilityId);

        if (sportIds != null) {
            for (Integer sid : sportIds) {
                if (sid == null) {
                    continue;
                }
                adminMapper.insertFacilitySport(facilityId, sid,
                        reserveUrls == null ? null : reserveUrls.get(sid));
            }
        }

        log.info("{}.modifyFacility End! 종목 {}개", this.getClass().getName(),
                sportIds == null ? 0 : sportIds.size());
    }

}
