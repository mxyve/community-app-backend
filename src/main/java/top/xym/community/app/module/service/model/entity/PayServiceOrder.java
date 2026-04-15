package top.xym.community.app.module.service.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_service_order")
public class PayServiceOrder {

    // 主键ID
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 订单号（对应service_orders.order_no）
    private String orderNo;

    // 用户ID
    private Long userId;

    // 支付金额
    private BigDecimal payAmount;

    // 支付状态 0未支付 1已支付
    private Integer payStatus;

    // 支付时间
    private LocalDateTime payTime;

    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}