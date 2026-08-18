package com.moveon.service;

public interface IRegionService {

    /**
     * 좌표를 "강서구 화곡동" 같은 행정동 이름으로 바꾼다.
     * 키가 없거나 조회에 실패하면 null 을 돌려준다.
     */
    String getRegionName(double lat, double lng);

}
