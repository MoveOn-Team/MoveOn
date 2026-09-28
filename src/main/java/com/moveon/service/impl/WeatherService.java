package com.moveon.service.impl;

import com.moveon.dto.WeatherDTO;
import com.moveon.service.IWeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 추천 탭 날씨. OpenWeather Current Weather 2.5.
 *
 * 전에는 화면이 키를 들고 openweathermap.org 를 직접 불렀다.
 * 소스 보기 한 번이면 키가 나왔다. 서버가 대신 부르고 숫자만 내려준다.
 *
 * 좌표(lat, lon)로 부르므로 화면이 넘겨준 자리의 날씨가 나온다.
 * 예전에 어디서 열든 서울 기온이었던 것은 2.5 라서가 아니라 q=Seoul 로 불렀기 때문이다.
 *
 * One Call 3.0 을 쓰지 않는 까닭은 요금이다. 3.0 은 카드를 걸어 두고 하루 1000번을
 * 넘기면 부르는 만큼 청구된다. 이 주소는 로그인 없이 열려 있어 누가 좌표를 바꿔 가며
 * 부르면 그대로 돈이 나간다. 2.5 무료 등급은 한도를 넘기면 막힐 뿐 청구되지 않는다.
 */
@Service
@Slf4j
public class WeatherService implements IWeatherService {

    private static final String URL = "https://api.openweathermap.org/data/2.5/weather";

    /**
     * 받아 둔 날씨를 이만큼 다시 쓴다.
     *
     * 무료 등급은 분당 60번까지다. 발표처럼 여럿이 한꺼번에 추천 탭을 열면 넘길 수 있다.
     * 자료 자체도 10분마다 바뀌므로 그보다 자주 물을 이유가 없다.
     */
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RestClient restClient;
    private final String apiKey;

    /**
     * 좌표 한 칸당 받아 둔 날씨.
     *
     * 전에는 받아 둔 값이 통째로 하나였다. 어디서 열든 같은 서울 날씨였으니
     * 그래도 됐는데, 이제 좌표마다 답이 다르므로 한 칸이면 부산에서 연 사람이
     * 서울 기온을 받는다.
     */
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    /** 받아 둔 날씨와 받은 때 */
    private record Cached(WeatherDTO weather, Instant at) {
    }

    public WeatherService(@Qualifier("externalRestClient") RestClient restClient,
                          @Value("${weather.api.key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public boolean isReady() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 이 좌표를 물어볼 주소.
     *
     * 좌표를 그냥 이어붙이면 0 근처에서 1.0E-5 처럼 찍혀 주소가 깨진다.
     * 서울에서는 안 나는 일이지만 자릿수를 펴 두는 편이 싸다.
     */
    String url(double lat, double lon) {
        return URL
                + "?lat=" + BigDecimal.valueOf(lat).toPlainString()
                + "&lon=" + BigDecimal.valueOf(lon).toPlainString()
                + "&units=metric&lang=kr&appid=" + apiKey;
    }

    /**
     * 받아 둔 날씨를 찾을 칸.
     *
     * GPS 는 가만히 서 있어도 소수 넷째 자리가 흔들린다. 좌표를 그대로 쓰면
     * 새로 고칠 때마다 다른 칸이 되어 받아 둔 것을 한 번도 못 쓴다.
     * 소수 첫째 자리면 약 11km 다. 그 안에서 기온이 갈리지는 않는다.
     */
    // ponytail: 좌표 1자리 반올림 키. 전국으로 넓어져 칸이 많아지면 크기 상한이 필요하다
    static String cell(double lat, double lon) {
        return Math.round(lat * 10) + "," + Math.round(lon * 10);
    }

    @Override
    public WeatherDTO now(double lat, double lon) {

        if (!isReady()) {
            return null;
        }

        Cached hit = cache.get(cell(lat, lon));
        if (hit != null && hit.at().plus(TTL).isAfter(Instant.now())) {
            return hit.weather();
        }

        try {
            Map<String, Object> body = restClient.get()
                    .uri(URI.create(url(lat, lon)))
                    .retrieve()
                    .body(Map.class);

            WeatherDTO rDTO = parse(body);
            if (rDTO == null) {
                return stale(hit);
            }

            cache.put(cell(lat, lon), new Cached(rDTO, Instant.now()));
            return rDTO;

        } catch (Exception e) {
            // 날씨가 없어도 추천은 돌아간다. 지난번 것이 있으면 그거라도 준다.
            log.warn("날씨를 못 받았다 : {}", e.getMessage());
            return stale(hit);
        }
    }

    /**
     * 응답에서 위젯에 쓸 두 값만 꺼낸다. 꺼낼 게 없으면 null.
     *
     * 기온은 main.temp, 아이콘은 weather[0].icon 에 있다.
     * 3.0 은 둘 다 current 안에 있어 자리가 다르다. 다른 쪽 자리에서 읽으면
     * 화면은 멀쩡한데 값만 늘 빈다.
     */
    @SuppressWarnings("unchecked")
    WeatherDTO parse(Map<String, Object> body) {

        Map<String, Object> main =
                (body == null) ? null : (Map<String, Object>) body.get("main");

        if (main == null) {
            return null;
        }

        WeatherDTO rDTO = new WeatherDTO();

        if (main.get("temp") instanceof Number t) {
            rDTO.setTemp((int) Math.round(t.doubleValue()));
        }

        // weather 는 배열이다. 첫 번째가 지금 날씨다
        if (body.get("weather") instanceof java.util.List<?> list && !list.isEmpty()
                && list.get(0) instanceof Map<?, ?> w) {
            rDTO.setIcon(svg(String.valueOf(w.get("icon"))));
        }

        return rDTO;
    }

    /** 시간이 지난 값이라도 빈 위젯보다는 낫다. 없으면 null */
    private static WeatherDTO stale(Cached hit) {
        return hit == null ? null : hit.weather();
    }

    /**
     * OpenWeather 아이콘 코드 -> 우리가 가진 svg 파일.
     *
     * 코드는 18가지인데 파일은 12개다. 04(구름 많음)와 50(안개)처럼
     * 그림으로는 구분이 안 되는 것끼리 묶었다.
     */
    private String svg(String code) {
        return switch (code) {
            case "01d" -> "day.svg";
            case "01n" -> "night.svg";
            case "02d" -> "cloudy-day-1.svg";
            case "02n" -> "cloudy-night-1.svg";
            case "03d" -> "cloudy-day-3.svg";
            case "03n" -> "cloudy-night-3.svg";
            case "09d", "09n" -> "rainy-6.svg";
            case "10d" -> "rainy-1.svg";
            case "10n" -> "rainy-4.svg";
            case "11d", "11n" -> "thunder.svg";
            case "13d", "13n" -> "snowy-1.svg";
            case "04d", "04n", "50d", "50n" -> "cloudy.svg";
            default -> "day.svg";
        };
    }
}
