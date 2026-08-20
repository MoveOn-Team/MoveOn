package com.moveon.service;

import com.moveon.dto.EventDTO;
import com.moveon.dto.EventSearchDTO;

import java.util.List;
import java.util.Map;


/**
 * 글에서 행사 정보를 뽑아내는 일 (Gemini)
 *
 * 규칙으로 안 되는 것만 맡긴다.
 *   날짜·금액은 생김새가 뚜렷해서 정규식으로 잘 잡힌다.
 *   장소·대상은 문장 속에 섞여 있어 규칙으로는 못 뗀다.
 *
 * 키가 없거나 호출이 실패해도 앱은 그대로 돌아가야 한다.
 * 심사위원이 키 없이 내려받아도 관리자 화면이 열려야 하기 때문이다.
 * 그래서 실패하면 예외를 던지지 않고 빈 값을 돌려준다.
 */
public interface IAiService {

    /** 쓸 수 있는 상태인지. 키가 없으면 false */
    boolean isReady();

    /**
     * 대회 홈페이지 글에서 행사 정보를 뽑는다.
     *
     * @param pageText  태그를 걷어낸 페이지 글
     * @param eventName 어느 대회를 찾는지 알려 준다.
     *                  한 페이지에 여러 대회가 적힌 곳이 있어 이름을 함께 준다.
     * @return 뽑아낸 값만 담긴 EventDTO. 실패하면 빈 DTO
     */
    EventDTO extractEvent(String pageText, String eventName);

    /**
     * 글 제목들에서 대회 이름만 뽑는다.
     *
     * 정규식으로 하다가 옮겨 왔다. 정규식은 세 가지를 못 했다.
     *   - "리아는 배불런 롯데리아 마라톤" 에서 뒤쪽만 잘라 이름을 망가뜨렸다
     *   - "잠수교 나이트런" 과 "2025 서울 잠수교 나이트런" 을 다른 대회로 봤다
     *   - "배불런", "감동의 마라톤" 같은 문장 조각을 걸러내지 못했다
     *
     * 지역도 함께 뽑는다. 이름만 보고는 어디서 열리는지 알 수 없는 대회가 많다.
     *
     * @return 이름과 지역을 담은 목록. 실패하면 빈 목록
     */
    List<EventSearchDTO> extractNames(List<String> titles);

    /**
     * 검색 결과 중 그 대회의 공식 홈페이지를 고른다.
     *
     * 도메인만 보고 고르면 뉴스 기사나 입찰 공고가 잡힌다.
     * 실제로 '롯데리아 마라톤' 에 뉴스 기사가, '강남국제평화마라톤' 에
     * 대행 용역업체 선정 공고가 공식 사이트로 잡혔다.
     *
     * 대회 전용 홈페이지가 아예 없는 대회도 많다.
     * 브랜드가 여는 행사는 접수 플랫폼만 쓰는 경우가 흔하다.
     * 그럴 때는 접수처나 안내 페이지라도 골라 주고, 무엇인지 함께 알려 준다.
     *
     * @param candidates title 과 link 를 담은 검색 결과
     * @return "종류|주소" 형태. OFFICIAL / APPLY / INFO. 쓸 만한 게 없으면 null
     */
    String pickSite(String eventName, List<Map<String, String>> candidates);

    /**
     * 같은 대회끼리 합친다.
     *
     * 검색 결과를 여러 묶음으로 나눠 뽑기 때문에 묶음 사이의 중복이 남는다.
     *   "2026 잠수교 10K 나이트런" 과 "잠수교 10k 마라톤대회" 가 따로 남는 식이다.
     *
     * 부르기 전에 지역으로 먼저 걸러 목록을 줄여야 한다.
     * 백 개가 넘는 목록을 한 번에 합치라고 하면 부를 때마다 결과가 달라진다.
     *
     * @return 합친 목록. 실패하면 받은 그대로
     */
    List<EventSearchDTO> mergeNames(List<EventSearchDTO> names);

}
