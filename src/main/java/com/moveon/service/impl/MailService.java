package com.moveon.service.impl;

import com.moveon.dto.MailDTO;
import com.moveon.service.IMailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromMail;

    @Async
    @Override
    public void doSendMail(MailDTO pDTO) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            if (fromMail != null && !fromMail.isBlank()) {
                helper.setFrom(fromMail);
            }

            helper.setTo(pDTO.getToMail());
            helper.setSubject(pDTO.getTitle());
            helper.setText(pDTO.getContents(), true);

            mailSender.send(message);
            log.info("메일 발송 완료 : {}", pDTO.getToMail());

        } catch (Exception e) {
            log.error("메일 발송 실패 : {}", pDTO.getToMail(), e);
        }
    }
}
