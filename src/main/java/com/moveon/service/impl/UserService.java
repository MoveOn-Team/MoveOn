package com.moveon.service.impl;

import com.moveon.dto.MailDTO;
import com.moveon.dto.UserDTO;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IMailService;
import com.moveon.service.IUserService;
import com.moveon.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final IUserMapper userMapper;
    private final IMailService mailService;

    @Override
    public UserDTO getUserIdExists(UserDTO pDTO) throws Exception {
        return Optional.ofNullable(userMapper.getUserIdExists(pDTO)).orElseGet(UserDTO::new);
    }

    @Override
    public UserDTO getEmailExists(UserDTO pDTO) throws Exception {
        return Optional.ofNullable(userMapper.getEmailExists(pDTO)).orElseGet(UserDTO::new);
    }

    @Override
    public String sendEmailCode(String email) throws Exception {

        String emailCode = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));

        MailDTO mailDTO = new MailDTO();
        mailDTO.setToMail(email);
        mailDTO.setTitle("[MOVE:ON] 이메일 인증번호입니다.");
        mailDTO.setContents(
            "<h2>MOVE:ON 이메일 인증</h2>" +
            "<p>아래 인증번호를 화면에 입력해 주세요.</p>" +
            "<p style='font-size:24px;font-weight:bold;'>" + emailCode + "</p>" +
            "<p>인증번호는 5분 동안 사용할 수 있습니다.</p>"
        );

        mailService.doSendMail(mailDTO);
        return emailCode;
    }

    @Override
    public int insertUserInfo(UserDTO pDTO) throws Exception {

        String salt = createSalt();
        pDTO.setSalt(salt);
        pDTO.setPassword(hashPassword(pDTO.getPassword(), salt));

        int result = userMapper.insertUserInfo(pDTO);

        if (result > 0) {
            sendWelcomeMail(pDTO);
        }

        return result;
    }

    @Override
    public UserDTO login(UserDTO pDTO) throws Exception {

        UserDTO rDTO = Optional.ofNullable(userMapper.getLoginUser(pDTO)).orElseGet(UserDTO::new);

        if (rDTO.getUserId() == 0 || rDTO.getSalt() == null || rDTO.getPassword() == null) {
            return new UserDTO();
        }

        String inputPassword = hashPassword(pDTO.getPassword(), rDTO.getSalt());

        if (!inputPassword.equals(rDTO.getPassword())) {
            return new UserDTO();
        }

        rDTO.setPassword(null);
        rDTO.setSalt(null);
        rDTO.setExists(1);
        return rDTO;
    }

    @Override
    public UserDTO searchUserId(UserDTO pDTO) throws Exception {

        UserDTO rDTO = Optional.ofNullable(userMapper.searchUserId(pDTO)).orElseGet(UserDTO::new);

        if (rDTO.getLoginId() == null) {
            rDTO.setExists(0);
        }

        return rDTO;
    }

    @Override
    public UserDTO searchPassword(UserDTO pDTO) throws Exception {

        UserDTO rDTO = Optional.ofNullable(userMapper.searchPassword(pDTO)).orElseGet(UserDTO::new);

        if (rDTO.getUserId() == 0) {
            rDTO.setExists(0);
        }

        return rDTO;
    }

    @Override
    public int updatePassword(UserDTO pDTO) throws Exception {

        String salt = createSalt();
        pDTO.setSalt(salt);
        pDTO.setPassword(hashPassword(pDTO.getPassword(), salt));

        return userMapper.updatePassword(pDTO);
    }

    private String createSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String hashPassword(String password, String salt) {
        return EncryptUtil.encHashSHA256(password + salt);
    }

    private void sendWelcomeMail(UserDTO pDTO) {

        try {
            MailDTO mailDTO = new MailDTO();
            mailDTO.setToMail(pDTO.getEmail());
            mailDTO.setTitle("[MOVE:ON] 회원가입이 완료되었습니다.");
            mailDTO.setContents(
                "<h2>MOVE:ON 회원가입 완료</h2>" +
                "<p>" + pDTO.getName() + "님, 회원가입을 환영합니다.</p>"
            );
            mailService.doSendMail(mailDTO);

        } catch (Exception e) {
            // 가입 자체는 성공했으므로 안내 메일 실패만 로그로 남긴다.
            log.warn("회원가입 안내 메일 발송 실패", e);
        }
    }
}
