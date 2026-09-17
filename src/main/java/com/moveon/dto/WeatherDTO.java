package com.moveon.dto;

import lombok.Data;

/**
 * 추천 탭 오른쪽 위 날씨 위젯이 받는 값.
 *
 * 화면에는 기온과 아이콘만 있으면 된다. 날씨 설명은 아이콘이 이미
 * 같은 이야기를 하고 있어 화면에서 뺐다.
 */
@Data
public class WeatherDTO {

    /** 섭씨. 반올림한 정수 */
    private Integer temp;

    /** 화면이 그릴 svg 파일 이름. 예 : cloudy-day-1.svg */
    private String icon;
}
