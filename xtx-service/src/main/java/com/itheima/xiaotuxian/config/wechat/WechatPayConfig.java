package com.itheima.xiaotuxian.config.wechat;

import com.itheima.xiaotuxian.client.wechat.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName WeChatPayConfig.java
 * @Description 微信配置类
 */
@Configuration
@EnableConfigurationProperties(WechatPayProperties.class)
public class WechatPayConfig {

    @Autowired
    WechatPayProperties wechatPayProperties;

    /***
     * @description 获得配置
     * @return
     */
    public Config config(){
        return Config.builder()
            .appid(wechatPayProperties.getAppid())
            .domain(wechatPayProperties.getDomain())
            .mchId(wechatPayProperties.getMchId())
            .mchSerialNo(wechatPayProperties.getMchSerialNo())
            .apiV3Key(wechatPayProperties.getApiV3Key())
            .privateKey(wechatPayProperties.getPrivateKey())
            .notifyUrl(wechatPayProperties.getNotifyUrl())
            .build();
    }
}
