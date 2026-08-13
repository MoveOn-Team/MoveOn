package com.moveon.mapper;

import com.moveon.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * users 테이블 SQL Mapper.
 * 메서드명은 UserMapper.xml의 id와 동일하게 유지한다.
 */
@Mapper
public interface IUserMapper {

    UserDTO getUserIdExists(UserDTO pDTO) throws Exception; // 아이디 중복 여부를 1 또는 0으로 조회

    UserDTO getEmailExists(UserDTO pDTO) throws Exception; // 이메일 중복 여부를 1 또는 0으로 조회

    int insertUserInfo(UserDTO pDTO) throws Exception; // 회원 정보를 users 테이블에 저장

    UserDTO getLoginUser(UserDTO pDTO) throws Exception; // 로그인할 회원 정보를 조회

    UserDTO searchUserId(UserDTO pDTO) throws Exception; // 이름과 이메일로 아이디를 조회

    UserDTO searchPassword(UserDTO pDTO) throws Exception; // 비밀번호를 변경할 회원을 조회

    int updatePassword(UserDTO pDTO) throws Exception; // 새 비밀번호와 salt를 저장

    UserDTO getUserBody(UserDTO pDTO) throws Exception; // 맞춤 추천용 신체정보(만나이·성별·BMI) 조회
}
