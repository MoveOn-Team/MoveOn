package com.moveon.service.impl;

import com.moveon.dto.WeatherDTO;
import com.moveon.service.IWeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * 추천 탭 날씨.
 *
 * 전에는 화면이 키를 들고 openweathermap.org 를 직접 불렀다.
 * 소스 보기 한 번이면 키가 나왔다. 서버가 대신 부르고 숫자만 내려준다.
 */
@Service
@Slf4j
public class WeatherService implements IWeatherService {

    private static final String URL =
            "https://api.openweathermap.org/data/2.5/weather"
                    + "?q=Seoul&units=metric&lang=kr&appid=";

    /**
     * 받아 둔 날씨를 이만큼 다시 쓴다.
     *
     * OpenWeather 무료 요금제는 분당 60번이고 자료 자체도 10분마다 바뀐다.
     * 사람이 추천 탭을 열 때마다 부르면 발표처럼 여럿이 몰릴 때 막힌다.
     */
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RestClient restClient;
    private final String apiKey;

    private volatile WeatherDTO cached;
    private volatile Instant cachedAt;

    public WeatherService(@Qualifier("externalRestClient") RestClient restClient,
                          @Value("${weather.api.key:}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public boolean isReady() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    @SuppressWarnings("unchecked")
    public WeatherDTO now() {

        if (!isReady()) {
            return null;
        }

        WeatherDTO hit = cached;
        if (hit != null && cachedAt != null && cachedAt.plus(TTL).isAfter(Instant.now())) {
            return hit;
        }

        try {
            Map<String, Object> body = restClient.get()
                    .uri(URI.create(URL + apiKey))
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                return hit;
            }

            WeatherDTO rDTO = new WeatherDTO();

            Map<String, Object> main = (Map<String, Object>) body.get("main");
            if (main != null && main.get("temp") instanceof Number t) {
                rDTO.setTemp((int) Math.round(t.doubleValue()));
            }

            // weather 는 배열이다. 첫 번째가 지금 날씨다.
            if (body.get("weather") instanceof java.util.List<?> list && !list.isEmpty()
                    && list.get(0) instanceof Map<?, ?> w) {
                rDTO.setIcon(svg(String.valueOf(w.get("icon"))));
            }

            cached = rDTO;
            cachedAt = Instant.now();
            return rDTO;

        } catch (Exception e) {
            // 날씨가 없어도 추천은 돌아간다. 지난번 것이 있으면 그거라도 준다.
            log.warn("날씨를 못 받았다 : {}", e.getMessage());
            return hit;
        }
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
