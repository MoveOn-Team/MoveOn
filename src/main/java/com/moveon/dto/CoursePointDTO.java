package com.moveon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 코스를 이루는 지점 하나
 *
 * 카카오 지도에 코스 선을 그리는 데 쓴다.
 * 좌표를 그대로 넘기므로 옮기거나 바꾸지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class CoursePointDTO {

    private int courseId;

    /** 경로 순번 */
    private int seq;

    private double lat;
    private double lng;

    /** 지점 이름. 315개 중 299개에 들어 있다 */
    private String pointName;
}
