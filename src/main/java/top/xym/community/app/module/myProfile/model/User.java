package top.xym.community.app.module.myProfile.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user") // 表名
public class User {

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID) // 推荐雪花算法ID
    private Long userId;
    /**
     * 登录账户
     */
    @TableField("username")
    private String username;
    /**
     * 登录密码
     */
    @TableField("password")
    private String password;
    /**
     * 姓名
     */
    @TableField("nick_name")
    private String nickName;
    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;
    /**
     * 电话
     */
    @TableField("phone")
    private String phone;
    /**
     * 微信openId
     */
    @TableField("wx_open_id")
    private String wxOpenId;
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    /**
     * 性别
     */
    @TableField("gender")
    private String gender;
    /**
     * 是否是超级管理员 1：是 0：否
     */
    @TableField("is_admin")
    private String isAdmin;
    /**
     * 租户 ID（0 = 居民，>0 = 商家）
     */
    @TableField("tenant_id")
    private Long tenantId;
    /**
     * 省
     */
    @TableField("province")
    private String province;
    /**
     * 市
     */
    @TableField("city")
    private String city;
    /**
     * 区
     */
    @TableField("district")
    private String district;
    /**
     * 账户是否过期 1 未过期，0已过期
     */
    @TableField("is_account_non_expired")
    private Integer isAccountNonExpired;
    /**
     * 帐户是否被锁定(1 未锁定，0已锁定)
     */
    @TableField("is_account_non_locked")
    private Integer isAccountNonLocked;
    /**
     * 密码是否过期(1未过期，0已过期)
     */
    @TableField("is_credentials_non_expired")
    private Integer isCredentialsNonExpired;
    /**
     * 帐户是否可用(1 可用，0 删除用户)
     */
    @TableField("is_enabled")
    private Integer isEnabled;
    /**
     * 逻辑删除字段：0 未删除，1已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}