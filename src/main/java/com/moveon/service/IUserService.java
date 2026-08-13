package com.moveon.service;

import com.moveon.dto.OnboardingDTO;
import com.moveon.dto.UserDTO;

public interface IUserService {

    UserDTO getUserIdExists(UserDTO pDTO) throws Exception; // 아이디 중복 확인

    UserDTO getEmailExists(UserDTO pDTO) throws Exception; // 이메일 중복 확인

    String sendEmailCode(String email) throws Exception; // 이메일 인증번호 발송

    int insertUserInfo(UserDTO pDTO) throws Exception; // 회원가입 처리

    UserDTO login(UserDTO pDTO) throws Exception; // 로그인 정보 확인

    UserDTO searchUserId(UserDTO pDTO) throws Exception; // 아이디 찾기

    UserDTO searchPassword(UserDTO pDTO) throws Exception; // 비밀번호 찾기 대상 확인

    int updatePassword(UserDTO pDTO) throws Exception; // 비밀번호 재설정

    int saveOnboarding(OnboardingDTO pDTO) throws Exception; // 온보딩 정보 저장과 BMI 계산

    OnboardingDTO getOnboarding(OnboardingDTO pDTO) throws Exception; // 온보딩 정보 조회
}
