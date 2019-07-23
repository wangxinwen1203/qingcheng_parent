package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.user.User;
import com.qingcheng.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;
    //发送短信验证码
        @GetMapping("/sendSms")
    public Result sendSms(String phone) {
        userService.sendSms(phone);
        return new Result();

    }
    @RequestMapping("save")
    public Result save(@RequestBody User user,String smsCode){
            //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        String encodePassWord = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodePassWord);
        userService.add(user,smsCode);
        return new Result();

    }
}
