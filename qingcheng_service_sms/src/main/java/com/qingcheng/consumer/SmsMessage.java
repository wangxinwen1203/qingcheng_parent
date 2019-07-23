package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;


public class SmsMessage implements MessageListener {
    @Autowired
    private SmsUtil smsUtil;
    @Value("${smsCode}")
    private String smsCode;
    @Value("${param}")
    private String param;

    @Override
    public void onMessage(Message message) {

        byte[] body = message.getBody();
        String jsonString = new String(body);
        Map<String, String> map = JSON.parseObject(jsonString, Map.class);
        String phone = map.get("phone");
        String code = map.get("code");
        System.out.println("手机号:" + phone + "验证码：" + code);
        smsUtil.sendSms(phone,smsCode,param.replace("[value]",code));
    }
}
