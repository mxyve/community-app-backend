package top.xym.community.app.module.service.model.dto;

import lombok.Data;

@Data
public class OrderPageRequest {
    private Long current = 1L;
    private Long size = 10L;
    private Integer status; // 订单状态筛选
}