package top.xym.community.app.module.session.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_session")
public class ChatSession {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    @Schema(description = "模型标题")
    private String title;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("model_name")
    @Schema(description = "模型名称")
    private String modelName;

    @TableField("agent_type")
    @Schema(description = "会话类型：normal | edu_agent | resume_agent")
    private String agentType;

    @TableField("status")
    @Schema(description = "会话状态 0-活跃 1-归档")
    private Integer status;

    @TableField("last_message")
    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @TableField("last_message_time")
    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(value = "deleted")
//    @TableLogic
    @Schema(description = "逻辑删除 0-未删除 1-已删除")
    private Integer deleted;
}
