package com.hrm.auth.mapper.ext;

import com.hrm.auth.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface ExtUserMapper {
    // Phương thức được gọi khi người dùng đăng nhập
    Optional<User> findByUsername(@Param("username") String username);
}
