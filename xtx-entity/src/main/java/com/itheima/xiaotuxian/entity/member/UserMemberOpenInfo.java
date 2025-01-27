package com.itheima.xiaotuxian.entity.member;
/*
 * @author: lbc
 * @Date: 2023-04-29 17:25:37
 * @Descripttion:
 */

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itheima.xiaotuxian.entity.AbstractBasePO;

import lombok.Data;

@Data
@TableName(value = "user_member_open_info")
public class UserMemberOpenInfo extends AbstractBasePO {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 关联的用户id
     */
    private String userId;
    /**
     * 用户名称
     */
    private String nickname;
    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 三方标识unionId
     */
     private String unionId;
    /**
     * 三方标识openId
     */
     private String openId;
    /**
     * 注册来源，1为pc，2为webapp，3为微信小程序，4为Android，5为ios
     */
    private Integer source;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 性别，男、女、未知
     */
    private String gender;
    /**
     * 生日
     */
    private LocalDate birthday;
    /**
     * 城市编码
     */
    private String cityCode;
    /**
     * 省份编码
     */
    private String provinceCode;
    /**
     * 所在区/县编码
     */
    private String countyCode;
    /**
     * 职业
     */
    private String profession;
}
