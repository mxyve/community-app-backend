package top.xym.community.app.module.message.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceVO {
    private Long id;
    private String serviceName;
    private BigDecimal price;
    private BigDecimal avgStar;
    private String serviceArea;
    private String merchantName;

    private String coverImage;
    private String description;
    private Long serviceId;
}