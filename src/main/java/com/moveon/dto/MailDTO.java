package com.moveon.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 메일 발송에 필요한 값.
 */
@Getter
@Setter
@ToString
public class MailDTO {

    private String toMail; // 받는 사람 이메일 주소

    private String title; // 메일 제목

    private String contents; // 메일 본문
}
