package top.xym.community.app.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发送消息请求DTO")
public class MessageSendRequest {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID（必须是当前用户的未删除会话）", example = "1")
    private Long sessionId;

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "聊天所用模型（需从模型列表接口获取）", example = "qwen-turbo")
    private String modelName;

    @NotBlank(message = "提问内容不能为空")
    @Schema(description = "用户提问内容", example = "什么是Spring Boot的自动配置？")
    private String content;

    @Schema(description = "图片URL列表（多个图片用逗号分隔）")
    private String imageUrls;

    @Schema(description = "是否开启联网搜索（1=开启，0=关闭）", defaultValue = "0")
    private Integer webSearch = 0;

    @Schema(description = "是否开启深度思考（1=开启，0=关闭）", defaultValue = "0")
    private Integer hasThinking = 0;

    @Schema(description = "是否开启长思考（针对长文本/复杂问题，1=开启，0=关闭）", defaultValue = "0")
    private Integer needLongThink = 0; // 实体类无对应字段，暂作为提示词参数

    @Schema(description = "附件列表（支持图片、文档等，若无可为空）")
    private List<MessageAttachmentDTO> attachments; // 实体类无对应字段，暂不存储

    // 附件子DTO（封装附件信息）
    @Data
    @Schema(description = "消息附件DTO")
    public static class MessageAttachmentDTO {
        @NotBlank(message = "附件类型不能为空")
        @Schema(description = "附件类型（image=图片，doc=文档，video=视频等）", example = "image")
        private String type;

        @NotBlank(message = "附件URL不能为空")
        @Schema(description = "附件访问URL（前端上传后获取的地址）", example = "https://xxx.com/attach/123.png")
        private String url;

        @Schema(description = "附件名称", example = "示例图片.png")
        private String name;

        @Schema(description = "附件大小（单位：字节）", example = "204800")
        private Long size;
    }
}