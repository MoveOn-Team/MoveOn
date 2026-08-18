package com.moveon.mapper;

import com.moveon.dto.OnboardingDTO;
import com.moveon.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface IUserMapper {

    // 회원가입 아이디 중복 여부를 1 또는 0으로 조회

    UserDTO getUserIdExists(UserDTO pDTO) throws Exception;

    // 회원가입 이메일 중복 여부를 1 또는 0으로 조회

    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    // 회원 정보를 users 테이블에 저장

    int insertUserInfo(UserDTO pDTO) throws Exception;

    // 로그인할 회원 정보를 조회

    UserDTO getLoginUser(UserDTO pDTO) throws Exception;

    // 이름과 이메일로 아이디를 조회

    UserDTO searchUserId(UserDTO pDTO) throws Exception;

    // 비밀번호를 변경할 회원을 조회

    UserDTO searchPassword(UserDTO pDTO) throws Exception;

    // 새 비밀번호와 salt를 저장

    int updatePassword(UserDTO pDTO) throws Exception;

    // 신체 정보와 운동 성향을 저장

    int updateOnboarding(OnboardingDTO pDTO) throws Exception;

    // 저장된 온보딩 정보를 조회

    OnboardingDTO getOnboarding(OnboardingDTO pDTO) throws Exception;

    // 맞춤 추천용 신체정보(만나이·성별·BMI) 조회
  
    UserDTO getUserBody(UserDTO pDTO) throws Exception;
}
