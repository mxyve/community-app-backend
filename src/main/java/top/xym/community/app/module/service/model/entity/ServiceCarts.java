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
@TableName("service_carts")
public class ServiceCarts {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("merchant_id")
    private Integer merchantId;

    @TableField("user_id")
    private Long userId;

    @TableField("service_id")
    private Integer serviceId;

    @TableField("spec_id")
    private Integer specId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("selected")
    private Boolean selected;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    @TableField(exist = false)
    private String serviceName;

    @TableField(exist = false)
    private String merchantName;

    @TableField(exist = false)
    private BigDecimal basePrice;
}