package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    @Reference
    private LoginLogService loginLogService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        //记录日志
        System.out.println("记录日志");
        String name = authentication.getName();
        String ip = httpServletRequest.getRemoteAddr();
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginName(name);
        loginLog.setIp(ip);
        loginLog.setLoginTime(new Date());
        String header = httpServletRequest.getHeader("user-agent");
        String browserName = WebUtil.getBrowserName(header);
        loginLog.setBrowserName(browserName);
        loginLog.setLocation(WebUtil.getCityByIP(ip));//根据ip拿到地点

        loginLogService.add(loginLog);
        //然后转发的主页面，不然他会失效
    httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest,httpServletResponse);


    }
}
