package com.kob.backend.utils;

import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {
    public static User getUserContext(){
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        UserDetailImpl loginUser = (UserDetailImpl) authenticationToken.getPrincipal();
        return loginUser.getUser();
    }
}
