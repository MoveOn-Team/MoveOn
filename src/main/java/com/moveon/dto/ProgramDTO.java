package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 시설 운영 강좌 (programs 테이블)
 *
 * 공공데이터 원문을 그대로 담는 항목이 많다.
 * 요금·시간은 값이 없는 강좌가 흔하므로 화면에서 null 을 반드시 처리해야 한다.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProgramDTO {

    private int programId;

    private int facilityId;

    private int sportId;

    private String name; // 강좌명

    private String target; // 대상 (성인/청소년 등 원문)

    private String dayOfWeek; // 요일 원문 (월화수목금)

    private String startTime; // HH:mm. 원본 형식이 제각각이라 뽑히지 않으면 null

    private String endTime;

    private Integer fee; // 요금(원). 숫자가 아니면 null

    private Integer capacity; // 정원

    private String reservationUrl; // 예약 페이지 링크

}
