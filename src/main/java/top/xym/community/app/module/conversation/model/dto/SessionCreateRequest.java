package top.xym.community.app.module.conversation.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SessionCreateRequest {

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    private Long orderId;
}