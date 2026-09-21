package com.moveon.util;

/**
 * 위치를 못 받았을 때 쓸 기준점.
 *
 * 추천·행사·즉시운동 세 탭이 모두 거리순으로 세우므로 기준이 하나여야 한다.
 * 전에는 컨트롤러마다 따로 적어 두어 한쪽만 고치면 탭마다 다른 곳을
 * 기준으로 삼게 되어 있었다.
 */
public final class GeoPoint {

    /** 서울시청. 화면도 '서울시청 기준' 이라고 적는다 */
    public static final double DEFAULT_LAT = 37.5665;
    public static final double DEFAULT_LNG = 126.9780;

    private GeoPoint() {
    }

    /** 위도를 못 받았으면 기준점으로 */
    public static double lat(Double lat) {
        return lat != null ? lat : DEFAULT_LAT;
    }

    /** 경도를 못 받았으면 기준점으로 */
    public static double lng(Double lng) {
        return lng != null ? lng : DEFAULT_LNG;
    }
}
