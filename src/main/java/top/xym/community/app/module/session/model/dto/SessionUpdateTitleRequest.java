package top.xym.community.app.module.session.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改会话标题请求DTO")
public class SessionUpdateTitleRequest {

    @NotBlank(message = "会话标题不能为空")
    @Size(max = 50, message = "会话标题长度不能超过50个字符")
    @Schema(description = "新的会话标题", example = "我的AI编程助手对话")
    private String title;
}