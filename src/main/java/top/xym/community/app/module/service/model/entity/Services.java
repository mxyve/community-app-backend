package top.xym.community.app.module.service.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("services")
public class Services {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("merchant_id")
    private Integer merchantId;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("name")
    private String name;

    @TableField("subtitle")
    private String subtitle;

    @TableField("cover_image")
    private String coverImage;

    @TableField("banner_images")
    private String bannerImages;

    @TableField("description")
    private String description;

    @TableField("tags")
    private String tags;

    @TableField("price_type")
    private Integer priceType;

    @TableField("base_price")
    private BigDecimal basePrice;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("unit")
    private String unit;

    @TableField("min_buy")
    private Integer minBuy;

    @TableField("max_buy")
    private Integer maxBuy;

    @TableField("total_sales")
    private Integer totalSales;

    @TableField("monthly_sales")
    private Integer monthlySales;

    @TableField("rating_score")
    private BigDecimal ratingScore;

    @TableField("rating_count")
    private Integer ratingCount;

    @TableField("service_area")
    private String serviceArea;

    @TableField("is_hot")
    private Integer isHot;

    @TableField("is_recommend")
    private Integer isRecommend;

    @TableField("status")
    private Integer status;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("address_detail")
    private String addressDetail;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 非数据库字段，用于关联展示
    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String merchantName;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("audit_reason")
    private String auditReason;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("auditor_id")
    private Long auditorId;
}