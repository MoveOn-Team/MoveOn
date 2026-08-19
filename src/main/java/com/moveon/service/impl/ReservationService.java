package com.moveon.service.impl;

import com.moveon.dto.ReservationDTO;
import com.moveon.mapper.IReservationMapper;
import com.moveon.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 서울시 공공서비스예약(체육시설) 자동 갱신
 *
 * 예약 주소가 월마다 달라진다.
 *   8월 서서울호수공원 풋살장 대여 -> svc_id A
 *   9월 서서울호수공원 풋살장 대여 -> svc_id B (주소가 다르다)
 *
 * 그래서 한 번 받아두면 시간이 갈수록 쓸 수 있는 건수가 줄어든다.
 * 매달 1일에 다시 받아와 항상 최신 예약을 보여준다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService implements IReservationService {

    private final IReservationMapper reservationMapper;

    @Value("${seoul.book.api.key:}")
    private String apiKey;

    /** 서울 열린데이터광장 서비스명 */
    private static final String SERVICE = "ListPublicReservationSport";

    /** 한 번에 받아올 수 있는 최대 행 수 (열린데이터광장 제한) */
    private static final int PAGE = 1000;

    /**
     * 매달 1일 새벽 4시에 돌린다.
     *
     * 서울시가 다음 달 예약을 전달 초에 등록하므로 월 1회면 충분하다.
     * 사람이 없는 시간에 두어 조회가 느려지는 걸 피한다.
     */
    @Scheduled(cron = "0 0 4 1 * *")
    public void scheduledSync() {
        try {
            int n = syncReservations();
            log.info("대관 정보 자동 갱신 완료 : {}건", n);
        } catch (Exception e) {
            // 갱신에 실패해도 기존 데이터로 화면은 그대로 돌아간다.
            // 예외를 밖으로 던지면 스케줄러가 멈추므로 여기서 삼킨다.
            log.error("대관 정보 자동 갱신 실패 : {}", e.getMessage());
        }
    }

    @Override
    public int syncReservations() throws Exception {

        log.info("{}.syncReservations Start!", this.getClass().getName());

        if (apiKey == null || apiKey.isBlank()) {
            log.info("seoul.book.api.key 가 없어 건너뛴다");
            return 0;
        }

        List<ReservationDTO> all = new ArrayList<>();
        int start = 1;

        while (true) {
            List<ReservationDTO> page = fetch(start, start + PAGE - 1);
            if (page.isEmpty()) {
                break;
            }
            all.addAll(page);
            if (page.size() < PAGE) {
                break;
            }
            start += PAGE;
        }

        if (all.isEmpty()) {
            log.info("{}.syncReservations End! 받아온 건 없음", this.getClass().getName());
            return 0;
        }

        reservationMapper.insertReservations(all);

        // 예약 링크를 시설에 다시 붙인다. 지난달 링크가 남지 않도록 지우고 시작한다.
        reservationMapper.clearFacilityReserveUrl();
        int matched = reservationMapper.matchFacilityReserveUrl();

        log.info("{}.syncReservations End! {}건 저장 / 시설 연결 {}건",
                this.getClass().getName(), all.size(), matched);

        return all.size();
    }

    /** 한 페이지를 받아 DTO 목록으로 바꾼다 */
    private List<ReservationDTO> fetch(int start, int end) throws Exception {

        String url = "http://openapi.seoul.go.kr:8088/" + apiKey
                   + "/xml/" + SERVICE + "/" + start + "/" + end + "/";

        HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();
        con.setConnectTimeout(20000);
        con.setReadTimeout(60000);

        List<ReservationDTO> list = new ArrayList<>();

        try (InputStream in = con.getInputStream()) {
            NodeList rows = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new InputStreamReader(in, StandardCharsets.UTF_8)))
                    .getElementsByTagName("row");

            for (int i = 0; i < rows.getLength(); i++) {
                Element e = (Element) rows.item(i);

                ReservationDTO r = new ReservationDTO();
                r.setSvcId(text(e, "SVCID"));
                r.setMinClass(text(e, "MINCLASSNM"));
                r.setSvcStat(text(e, "SVCSTATNM"));
                r.setSvcName(text(e, "SVCNM"));
                r.setPayYn(text(e, "PAYATNM"));
                r.setPlaceName(text(e, "PLACENM"));
                r.setUseTarget(text(e, "USETGTINFO"));
                r.setSvcUrl(text(e, "SVCURL"));
                r.setLng(decimal(text(e, "X")));
                r.setLat(decimal(text(e, "Y")));
                r.setOpenBgn(text(e, "SVCOPNBGNDT"));
                r.setOpenEnd(text(e, "SVCOPNENDDT"));
                r.setRcptBgn(text(e, "RCPTBGNDT"));
                r.setRcptEnd(text(e, "RCPTENDDT"));
                r.setAreaName(text(e, "AREANM"));
                r.setTelNo(text(e, "TELNO"));
                r.setVMin(text(e, "V_MIN"));
                r.setVMax(text(e, "V_MAX"));

                // 서비스 ID 가 없으면 저장할 수 없다(기본키)
                if (r.getSvcId() != null) {
                    list.add(r);
                }
            }
        }

        return list;
    }

    /** 태그 값을 꺼낸다. 값이 없거나 문자열 "null" 이면 null 로 맞춘다. */
    private String text(Element e, String tag) {
        NodeList n = e.getElementsByTagName(tag);
        if (n.getLength() == 0) {
            return null;
        }
        String v = n.item(0).getTextContent();
        if (v == null) {
            return null;
        }
        v = v.trim();
        return (v.isEmpty() || "null".equalsIgnoreCase(v)) ? null : v;
    }

    /** 좌표 문자열을 숫자로 바꾼다. 숫자가 아니면 null 로 둔다. */
    private BigDecimal decimal(String v) {
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
