package com.moveon.service.impl;

import com.moveon.dto.AdminDTO;
import com.moveon.dto.EventDTO;
import com.moveon.mapper.IAdminMapper;
import com.moveon.service.IAdminService;
import com.moveon.util.EncryptUtil;
import com.moveon.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


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
    public List<EventDTO> getAdminEventList(String status) throws Exception {

        // 주소창에 아무 값이나 넣어도 SQL 로 흘러가지 않게 여기서 막음.
        String s = (PENDING.equals(status) || PUBLISHED.equals(status) || REJECTED.equals(status))
                ? status : null;

        return adminMapper.getAdminEventList(s);
    }

    @Override
    public EventDTO getAdminEvent(int eventId) throws Exception {
        return adminMapper.getAdminEvent(eventId);
    }

    /** 검색에 지난 대회 후기가 섞여 들어와서 저장할 때 한 번 더 막는다 */
    private boolean isPast(EventDTO pDTO) {
        return pDTO.getStartDate() != null
                && pDTO.getStartDate().isBefore(java.time.LocalDate.now());
    }

    /** 하루짜리 대회는 종료일이 비어서 온다. 비면 시작일로 채운다 */
    private void fillEndDate(EventDTO pDTO) {
        if (pDTO.getEndDate() == null && pDTO.getStartDate() != null) {
            pDTO.setEndDate(pDTO.getStartDate());
        }
    }

    /** javascript: 주소는 window.open 이 실행해 버린다. &lt;input type="url"&gt; 은 그것도 통과시킨다 */
    private String cleanUrl(String url) {
        String s = UrlUtil.firstUsable(url);
        if (s == null && url != null && !url.isBlank()) {
            log.warn("쓸 수 없는 주소라 버린다 : {}", url);
        }
        return s;
    }

    @Override
    public int addEvent(EventDTO pDTO) throws Exception {

        if (isPast(pDTO)) {
            log.info("이미 열린 대회라 저장하지 않는다 : {} ({})",
                    pDTO.getTitle(), pDTO.getStartDate());
            return -1;
        }

        log.info("{}.addEvent Start! {}", this.getClass().getName(), pDTO.getTitle());

        // 좌표·행사일이 없어도 저장은 받는다. 공지가 안 올라온 대회를 담아 둬야 해서.
        // PENDING 이 '검수 대기' 와 '덜 채운 초안' 을 겸한다. 공개할 때 changeStatus 가 검사한다.
        if (pDTO.getStatus() == null || pDTO.getStatus().isBlank()) {
            pDTO.setStatus(PENDING);
        }
        if (pDTO.getSource() == null || pDTO.getSource().isBlank()) {
            pDTO.setSource("MANUAL");
        }

        fillEndDate(pDTO);
        pDTO.setHomepageUrl(cleanUrl(pDTO.getHomepageUrl()));
        pDTO.setSourceUrl(cleanUrl(pDTO.getSourceUrl()));

        int res = adminMapper.insertEvent(pDTO);

        log.info("{}.addEvent End! eventId : {}", this.getClass().getName(), pDTO.getEventId());

        return res == 0 ? 0 : pDTO.getEventId();
    }

    @Override
    public int modifyEvent(EventDTO pDTO) throws Exception {
        log.info("{}.modifyEvent Start! eventId : {}", this.getClass().getName(), pDTO.getEventId());

        fillEndDate(pDTO);
        pDTO.setHomepageUrl(cleanUrl(pDTO.getHomepageUrl()));
        pDTO.setSourceUrl(cleanUrl(pDTO.getSourceUrl()));

        return adminMapper.updateEvent(pDTO);
    }

    @Override
    public int removeEvent(int eventId) throws Exception {

        log.info("{}.removeEvent Start! eventId : {}", this.getClass().getName(), eventId);

        int res = adminMapper.deleteEvent(eventId);

        log.info("{}.removeEvent End! {}줄", this.getClass().getName(), res);

        return res;
    }

    @Override
    public int changeStatus(int eventId, String status, int adminId) throws Exception {

        // 공개할 때만 검사한다. 행사일이 없으면 날짜 칸이 깨지고, 좌표가 없으면 목록에서 빠진다.
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


}
