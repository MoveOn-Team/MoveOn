package com.moveon.service;

import com.moveon.dto.UserDTO;

public interface IUserService {

    // 회원 가입 전 아이디 중복체크하기

    UserDTO getUserIdExists(UserDTO pDTO) throws Exception;

}
