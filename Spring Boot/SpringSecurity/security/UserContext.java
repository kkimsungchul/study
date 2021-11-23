package com.sungchul.stock.config.security;

import com.sungchul.stock.user.vo.UserVO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class UserContext extends User {

    private final UserVO userVO;

    //여기서 전달해준 ID를 토큰에 저장함
    //다만 토큰에서 가져올때는 userDetails.getUsername() 로 가져옴. 이걸 바꾸려고 하면 UserDetails 인터페이스를 구현해야함
    public UserContext(UserVO userVO, Collection<? extends GrantedAuthority> authorities) {
        super(userVO.getUserId(), userVO.getPassword(), authorities);
        this.userVO = userVO;
    }

    public UserVO getUserVO() {
        return userVO;
    }
}
