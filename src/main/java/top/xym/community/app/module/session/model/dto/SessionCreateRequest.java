package top.xym.community.app.module.session.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建会话请求DTO")
public class SessionCreateRequest {

    @NotBlank(message = "会话标题不能为空")
    @Size(max = 100, message = "会话标题不能超过100个字符")
    @Schema(description = "会话标题", example = "我的AI对话")
    private String title;

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称（需与可用模型列表中的code一致）", example = "qwen-turbo")
    private String modelName;

    @Schema(description = "会话类型", example = "edu_agent")
    private String agentType;

}