package com.moveon.service;

public interface IReservationService {

    /**
     * 서울시 공공서비스예약(체육시설)을 다시 받아와 저장하고,
     * 시설 x 종목에 예약 링크를 다시 붙인다.
     *
     * @return 받아온 건수
     */
    int syncReservations() throws Exception;

}
