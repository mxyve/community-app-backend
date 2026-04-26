package top.xym.community.app.module.message.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息响应 DTO（用于前端展示消息历史、消息详情）
 */
@Data
@AllArgsConstructor
@Schema(description = "消息响应DTO")
public class MessageResponse {

    @Schema(description = "消息ID", example = "1001")
    private Long id;

    @Schema(description = "所属会话ID", example = "2001")
    private Long sessionId;

    @Schema(description = "发送者用户ID", example = "3001")
    private Long userId;

    @Schema(description = "消息角色（user=用户，assistant=助手）", example = "user")
    private String role;

    @Schema(description = "消息内容", example = "什么是Spring Boot？请详细说明核心特性")
    private String content;

    @Schema(description = "语音")
    private String audio;

    @Schema(description = "所用AI模型名称", example = "qwen-turbo")
    private String modelName;

    @Schema(description = "消息图片URLs（多个图片用逗号分隔）", example = "https://example.com/image1.jpg,https://example.com/image2.jpg")
    private String imageUrls;

    @Schema(description = "消息Token数（可选）", example = "68")
    private Integer tokens;

    @Schema(description = "是否开启深度思考（0=否，1=是）", example = "1")
    private Integer hasThinking;

    @Schema(description = "思考内容（仅助手消息可能有值）", example = "用户询问Spring Boot，需先定义，再讲自动配置、起步依赖等核心特性...")
    private String thinkingContent;

    @Schema(description = "是否开启联网搜索（0=否，1=是）", example = "0")
    private Integer webSearch;

    @Schema(description = "消息状态（0=处理中，1=成功，2=失败）", example = "1")
    private Integer status;

    @Schema(description = "消息状态描述（前端直接展示，无需转换）", example = "成功")
    private String statusDesc; // 新增状态描述字段，方便前端使用

    @Schema(description = "消息创建时间", example = "2025-11-20 15:30:45")
    private LocalDateTime createTime;

    @Schema(description = "消息更新时间", example = "2025-11-20 15:31:20")
    private LocalDateTime updateTime;

    // 新增：状态转换方法（可在 convertToResponse 中调用）
    public void setStatusDesc(Integer status) {
        if (status == null) {
            this.statusDesc = "未知";
            return;
        }
        switch (status) {
            case 0:
                this.statusDesc = "处理中";
                break;
            case 1:
                this.statusDesc = "成功";
                break;
            case 2:
                this.statusDesc = "失败";
                break;
            default:
                this.statusDesc = "未知";
        }
    }
}