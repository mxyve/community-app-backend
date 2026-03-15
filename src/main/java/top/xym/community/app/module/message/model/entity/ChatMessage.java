package top.xym.community.app.module.message.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chat_message")
public class ChatMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role;

    @TableField("content")
    private String content;

    @TableField("model_name")
    private String modelName;

    @TableField("image_urls")
    private String imageUrls;

    @TableField("tokens")
    private Integer tokens;

    @TableField("has_thinking")
    private Integer hasThinking;

    @TableField("thinking_content")
    private String thinkingContent;

    @TableField("web_search")
    private Integer webSearch;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

//    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}