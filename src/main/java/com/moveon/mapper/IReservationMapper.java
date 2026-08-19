package com.moveon.mapper;

import com.moveon.dto.ReservationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 서울시 공공서비스예약 적재 SQL
 *
 * 화면 조회는 IFacilityMapper 가 하고, 여기는 받아와 저장하는 일만 함.
 */
@Mapper
public interface IReservationMapper {

    /**
     * 받아온 예약 건을 저장함.
     *
     * 같은 svc_id 가 이미 있으면 상태와 주소만 갱신함.
     * 서울시가 접수 상태를 바꾸는 경우가 있어서임.
     */
    int insertReservations(@Param("list") List<ReservationDTO> list) throws Exception;

    /**
     * 예약 링크를 facility_sports(시설 x 종목)에 붙임.
     *
     * 시설명이 정확히 같거나, 100m 안에 있으면서 종목까지 같은 경우만 연결함.
     */
    int clearFacilityReserveUrl() throws Exception;

    int matchFacilityReserveUrl() throws Exception;
}
