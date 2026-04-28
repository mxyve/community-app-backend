package top.xym.community.app.module.service.model.dto;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ServiceCommentPageRequest {
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;
    private long current = 1;
    private long size = 10;
}