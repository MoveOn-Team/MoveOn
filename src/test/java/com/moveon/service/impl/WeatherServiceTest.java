package com.moveon.service.impl;

import com.moveon.dto.WeatherDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 주소 조립 · 응답 읽기 · 캐시 칸만 본다.
 * 바깥을 부르지 않으므로 RestClient 는 없어도 된다.
 *
 * 이 셋이 2.5 에서 3.0 으로 옮기며 바뀐 자리다. 여기가 되돌아가면
 * 화면은 멀쩡한데 값만 조용히 서울 것이 되거나 통째로 빈다.
 */
class WeatherServiceTest {

    private final WeatherService service = new WeatherService(null, "테스트키");

    @Test
    void onecall_주소로_좌표를_들고_간다() {
        String url = service.url(35.1796, 129.0756); // 부산시청

        assertTrue(url.contains("/data/3.0/onecall"), url);
        assertTrue(url.contains("lat=35.1796"), url);
        assertTrue(url.contains("lon=129.0756"), url);
        assertFalse(url.contains("/data/2.5/"), url);
        assertFalse(url.contains("q=Seoul"), url);
    }

    @Test
    void 안_쓸_예보는_받지_않는다() {
        assertTrue(service.url(37.5665, 126.9780).contains("exclude=minutely,hourly,daily,alerts"));
    }

    @Test
    void 좌표가_0_근처여도_지수로_찍히지_않는다() {
        assertTrue(service.url(0.00001, 0.00001).contains("lat=0.00001"));
    }

    @Test
    void 기온과_아이콘은_current_안에서_꺼낸다() {
        // 3.0 응답 모양. 2.5 처럼 main 을 보면 여기서 빈다
        WeatherDTO rDTO = service.parse(Map.of(
                "current", Map.of(
                        "temp", 17.4,
                        "weather", List.of(Map.of("icon", "10n")))));

        assertEquals(17, rDTO.getTemp());
        assertEquals("rainy-4.svg", rDTO.getIcon());
    }

    @Test
    void current_가_없으면_받아_둔_값을_덮지_않는다() {
        assertNull(service.parse(Map.of("lat", 37.5665)));
        assertNull(service.parse(null));
    }

    @Test
    void 서울과_부산은_받아_둔_날씨를_나눠_쓴다() {
        assertNotEquals(WeatherService.cell(37.5665, 126.9780),
                        WeatherService.cell(35.1796, 129.0756));
    }

    @Test
    void GPS_가_조금_흔들려도_같은_칸이다() {
        assertEquals(WeatherService.cell(37.5665, 126.9780),
                     WeatherService.cell(37.5671, 126.9783));
    }
}
