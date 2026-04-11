package top.xym.community.app.module.service.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("service_merchant")
public class ServiceMerchant {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String merchantCode;

    private String name;

    private String displayName;

    private String contactPerson;

    private String contactPhone;

    private String email;

    private String password;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String serviceAreas;

    private String businessLicense;

    private String legalIdCardImg;

    private String logo;

    private String intro;

    private String serviceType;

    private String packageType;

    private Integer applyStatus;

    private BigDecimal ratingScore;

    private Integer serviceCount;

    private Integer responseTime;

    private Integer status;

    private Date applyTime;

    private Long auditUserId;

    private Date auditTime;

    private LocalDateTime verifiedAt;

    private String applyDesc;

    private LocalDateTime expireTime;

    private String config;

    private BigDecimal profitRatio;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}