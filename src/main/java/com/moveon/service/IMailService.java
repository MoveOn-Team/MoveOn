package com.moveon.service;

import com.moveon.dto.MailDTO;

public interface IMailService {

    void doSendMail(MailDTO pDTO); // MailDTO의 받는 사람, 제목, 내용으로 메일 발송
}
