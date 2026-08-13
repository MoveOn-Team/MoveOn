package com.moveon.mapper;

import com.moveon.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 회원 관련 SQL
 *
 * 실제 SQL 은 src/main/resources/mapper/UserMapper.xml 에 있다.
 * 여기 함수명과 XML 의 id 가 같아야 연결된다.
 */
@Mapper
public interface IUserMapper {

    // 회원 가입 전 아이디 중복체크하기

    UserDTO getUserIdExists(UserDTO pDTO) throws Exception;

    // 추천 계산에 쓰는 회원 신체정보 (만나이 · 성별 · BMI)

    UserDTO getUserBody(UserDTO pDTO) throws Exception;

}
