package com.moveon.util;

import java.util.Set;

/**
 * 종목의 성격에 따라 화면에서 달리 말해야 하는 것들.
 */
public final class SportRule {

    /**
     * 즉시운동 탭에서 '대관' 이라고 말할 종목. 테니스·농구·축구/풋살.
     *
     * 헬스·수영처럼 가서 이용하는 종목에 '대관 신청하기' 라고 적으면
     * 눌러 보기 전에는 무슨 말인지 알 수 없다.
     *
     * 탁구·배드민턴은 뺐다. 강습이 있는 시설 비율이 다른 종목보다 높아
     * '배우러 가는 곳' 으로 읽히는 편이 맞다.
     *
     *     탁구 39%  배드민턴 28%  테니스 19%  농구 5%  축구/풋살 4%
     *
     * 골프도 없다. 공공시설의 골프는 대관이 아니라 개인 타석 예약이라
     * 성격이 이용권에 가깝다.
     *
     * 추천 탭의 대관 목록(FacilityMapper)은 다섯 종목 그대로다.
     * 거기는 '빌릴 수 있는 곳' 을 모아 보여 주는 자리라 기준이 다르다.
     */
    private static final Set<Integer> COURT = Set.of(6, 7, 8);

    private SportRule() {
    }

    public static boolean isCourt(int sportId) {
        return COURT.contains(sportId);
    }
}
