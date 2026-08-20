package com.moveon.service.impl;

import com.moveon.dto.EventDTO;
import com.moveon.mapper.IEventMapper;
import com.moveon.service.IEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 행사 목록·상세 조회.
 *
 * 하는 일이 거의 없다. 걸러내고 정렬하는 건 SQL 이 하고,
 * 여기서는 화면에서 넘어온 정렬값이 아는 값인지만 확인한다.
 *
 * 추천 탭의 RecommendService 처럼 점수를 계산할 게 없다.
 * 행사는 회원 성향과 상관없이 '가까운 순 / 마감 임박순' 두 가지로만 보여주기 때문이다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EventService implements IEventService {

    private final IEventMapper eventMapper;

    @Override
    public List<EventDTO> getEventList(String sort, double lat, double lng) throws Exception {

        log.info("{}.getEventList Start! sort : {}", this.getClass().getName(), sort);

        // 주소창에 ?sort=drop 같은 걸 적어 넣어도 SQL 로 흘러가지 않게 여기서 막는다.
        // 아는 값이 아니면 기본값인 '가까운 순' 으로 본다.
        String order = SORT_DEADLINE.equals(sort) ? SORT_DEADLINE : SORT_NEAR;

        List<EventDTO> rList = eventMapper.getEventList(order, lat, lng);

        log.info("{}.getEventList End! {}건", this.getClass().getName(), rList.size());

        return rList;
    }

    @Override
    public EventDTO getEvent(int eventId, double lat, double lng) throws Exception {

        log.info("{}.getEvent Start! eventId : {}", this.getClass().getName(), eventId);

        // 없는 번호이거나 아직 PENDING 인 행사면 SQL 이 아무 행도 안 돌려주므로 null 이 된다.
        // 주소를 직접 쳐서 검수 전 행사를 들여다보는 것도 이 조건 하나로 막힌다.
        EventDTO rDTO = eventMapper.getEvent(eventId, lat, lng);

        log.info("{}.getEvent End! {}", this.getClass().getName(),
                rDTO == null ? "없음" : rDTO.getTitle());

        return rDTO;
    }

}
