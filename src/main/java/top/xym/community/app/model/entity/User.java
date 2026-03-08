package top.xym.community.app.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String username;

    private String password;

    private String nickName;

    private String avatar;

    private String phone;

    private String wxOpenId;

    private String email;

    private String gender;

    private String isAdmin;

    private Integer isAccountNonExpired;

    private Integer isAccountNonLocked;

    private Integer isCredentialsNonExpired;

    private Integer isEnabled;

    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleted;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}
