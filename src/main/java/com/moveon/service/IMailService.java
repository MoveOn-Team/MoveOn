package com.moveon.service;

import com.moveon.dto.MailDTO;

public interface IMailService {

    /** 보낼 때까지 기다린다. 실패하면 예외를 던진다. 결과를 화면에 알려야 하는 인증번호용 */
    void doSendMail(MailDTO pDTO) throws Exception;

    /** 기다리지 않는다. 실패는 로그만 남긴다. 안 가도 가입이 끝나는 안내 메일용 */
    void doSendMailAsync(MailDTO pDTO);
}
