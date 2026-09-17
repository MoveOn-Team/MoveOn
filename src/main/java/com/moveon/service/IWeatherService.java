package com.moveon.service;

import com.moveon.dto.WeatherDTO;

public interface IWeatherService {

    /** 키가 있는지. 없으면 화면이 위젯을 접는다 */
    boolean isReady();

    /**
     * 지금 서울 날씨.
     *
     * 못 받으면 null. 날씨는 없어도 추천이 돌아가므로 예외를 올리지 않는다.
     */
    WeatherDTO now();
}
