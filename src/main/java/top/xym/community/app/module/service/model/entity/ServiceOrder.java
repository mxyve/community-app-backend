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
@TableName("service_orders")
public class ServiceOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("merchant_id")
    private Integer merchantId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("service_id")
    private Integer serviceId;

    @TableField("spec_id")
    private Integer specId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("payment_method")
    private Integer paymentMethod;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("status")
    private Integer status;

    @TableField("service_time")
    private LocalDateTime serviceTime;

    @TableField("service_address")
    private String serviceAddress;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("user_remark")
    private String userRemark;

    @TableField("merchant_remark")
    private String merchantRemark;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    // 非数据库字段，用于关联展示
    @TableField(exist = false)
    private String serviceName;
    @TableField(exist = false)
    private String merchantName;
    @TableField(exist = false)
    private String userName;
}