package com.moveon.service.impl;

import com.moveon.dto.UserDTO;
import com.moveon.mapper.IUserMapper;
import com.moveon.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final IUserMapper userMapper; // 회원관련 SQL 사용하기 위한 Mapper 가져오기

    @Override
    public UserDTO getUserIdExists(UserDTO pDTO) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        // 조회 결과가 없어서 null 이 오면 빈 DTO 를 대신 올린다
        UserDTO rDTO = Optional.ofNullable(userMapper.getUserIdExists(pDTO)).orElseGet(UserDTO::new);

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return rDTO;
    }

}
