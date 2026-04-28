package top.xym.community.app.module.service.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ServiceCommentCreateRequest {

    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    // 评价时必传，回复不传
    private Long orderId;

    // 商家id
    private Long merchantId;

    // 0=一级评价，>0=回复
    private Long parentCommentId;

    // 被回复人ID
    private Long toUserId;

    // 1-5星（仅评价传）
    private Integer star;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String img;

}
