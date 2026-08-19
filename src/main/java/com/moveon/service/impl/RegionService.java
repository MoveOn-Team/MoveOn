package com.moveon.service.impl;

import com.moveon.service.IRegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 좌표를 행정동 이름으로 바꾼다 (카카오 로컬 API)
 *
 * 화면에 "강서구 화곡동 기준" 이라고 밝히기 위한 것이다.
 * 거리 계산 자체는 좌표로 하므로 이 값이 없어도 추천은 그대로 나온다.
 * 그래서 실패하면 예외를 던지지 않고 null 을 돌려준다.
 */
@Slf4j
@Service
public class RegionService implements IRegionService {

    private static final String URL =
            "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

    private final RestClient restClient;

    private final String kakaoKey;

    public RegionService(@Value("${kakao.rest.key:}") String kakaoKey) {
        this.kakaoKey = kakaoKey;
        this.restClient = RestClient.create();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getRegionName(double lat, double lng) {

        if (kakaoKey == null || kakaoKey.isBlank()) {
            log.info("kakao.rest.key 가 없어 지역명을 건너뛴다");
            return null;
        }

        try {
            Map<String, Object> body = restClient.get()
                    .uri(URL + "?x={x}&y={y}", lng, lat)   // 카카오는 x 가 경도, y 가 위도다
                    .header("Authorization", "KakaoAK " + kakaoKey)
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                return null;
            }

            List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
            if (documents == null || documents.isEmpty()) {
                return null;
            }

            // region_type 은 H(행정동)와 B(법정동)가 함께 온다. 생활 감각에 가까운 H 를 먼저 쓴다.
            Map<String, Object> pick = documents.stream()
                    .filter(d -> "H".equals(d.get("region_type")))
                    .findFirst()
                    .orElse(documents.get(0));

            String gu = str(pick.get("region_2depth_name"));   // 강서구
            String dong = str(pick.get("region_3depth_name")); // 화곡동

            if (gu.isEmpty() && dong.isEmpty()) {
                return null;
            }
            return (gu + " " + dong).trim();

        } catch (Exception e) {
            // 위치 표시는 곁가지라 실패해도 화면은 그대로 보여준다
            log.warn("지역명 조회 실패 : {}", e.getMessage());
            return null;
        }
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

}
