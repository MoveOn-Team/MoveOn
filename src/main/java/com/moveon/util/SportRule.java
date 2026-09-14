package com.moveon.util;

import java.util.Set;

/**
 * 종목의 성격에 따라 화면에서 달리 말해야 하는 것들.
 */
public final class SportRule {

    /**
     * 장소를 시간대로 빌리는 종목.
     *
     * 배드민턴·탁구·테니스·농구·축구/풋살. 코트를 통째로 빌려 쓰는 것들이다.
     * 헬스·수영·요가처럼 가서 이용하는 종목에 '대관 신청하기' 라고 적으면
     * 눌러 보기 전에는 무슨 말인지 알 수 없다.
     *
     * 골프는 뺐다. 공공시설의 골프는 대관이 아니라 개인 타석 예약이라
     * 성격이 이용권에 가깝다. FacilityMapper 의 대관 조회도 같은 기준이다.
     */
    private static final Set<Integer> COURT = Set.of(4, 5, 6, 7, 8);

    private SportRule() {
    }

    public static boolean isCourt(int sportId) {
        return COURT.contains(sportId);
    }
}
