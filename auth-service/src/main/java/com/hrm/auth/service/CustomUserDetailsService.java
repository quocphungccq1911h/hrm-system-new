package com.hrm.auth.service;

import com.hrm.auth.mapper.ext.ExtUserMapper;
import com.hrm.auth.model.User;
import com.hrm.auth.security.AuthUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ExtUserMapper extUserMapper;

    public CustomUserDetailsService(ExtUserMapper extUserMapper) {
        this.extUserMapper = extUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = extUserMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new AuthUserDetails(user);
    }
}
