package com.moveon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 비동기(Ajax) 요청의 결과를 화면에 돌려줄 때 쓰는 공통 DTO
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MsgDTO {

    private int result; // 성공 : 1 / 실패 : 그 외

    private String msg; // 메시지
}
