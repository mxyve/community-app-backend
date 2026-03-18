package top.xym.community.app.module.service.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("service_merchant")
public class ServiceMerchant {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("merchant_code")
    private String merchantCode;

    @TableField("name")
    private String name;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("address")
    private String address;

    @TableField("logo")
    private String logo;

    @TableField("intro")
    private String intro;

    @TableField("business_license")
    private String businessLicense;

    @TableField("rating_score")
    private BigDecimal ratingScore;

    @TableField("service_count")
    private Integer serviceCount;

    @TableField("response_time")
    private Integer responseTime;

    @TableField("status")
    private Integer status;

    @TableField("verified_at")
    private LocalDateTime verifiedAt;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("config")
    private String config;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;
}