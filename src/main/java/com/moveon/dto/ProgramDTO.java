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

    /**
     * COURSE 강습 / PASS 이용권(일일·자유·월회원) / RENTAL 대관
     *
     * 원본은 셋을 구분하지 않고 한 표에 담는다.
     * 그래서 '일일자유 5,000원' 과 '어린이수영교실 90,000원' 이 나란히 강좌로 올라왔다.
     * 이름·요금·시간표로 갈라 넣은 값이라 원본에 있는 항목은 아니다.
     */
    private String programType;

    /**
     * 같은 이름·대상으로 열리는 반의 수.
     *
     * 원본에는 '어린이 수영' 이 요금과 시간만 달리해 네 줄로 들어 있다.
     * 그 값들은 분기마다 바뀌어 실제와 어긋나므로 화면에서 뺐고,
     * 대신 몇 개 반이 열리는지만 센다.
     */
    private int classCount;

}
