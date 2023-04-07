package com.sungchul.etc.config.jwt.config;

import com.sungchul.etc.config.security.UserContext;
import com.sungchul.etc.user.mapper.UserMapper;
import com.sungchul.etc.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


//CustomUserDetailService 랑똑같음
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        UserVO userVO = userMapper.getUser(userId);
        if(userVO == null){
            throw new UsernameNotFoundException("UsernameNotFoundException");
        }

        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(userVO.getRoleId()));

        UserContext userContext = new UserContext(userVO,roles);

        return userContext;
    }


    public UserVO getUserInfo(String userId) throws UsernameNotFoundException {

        UserVO userVO = userMapper.getUser(userId);
        if(userVO == null){
            throw new UsernameNotFoundException("UsernameNotFoundException");
        }
        userVO.setPassword("");
        return userVO;
    }
}