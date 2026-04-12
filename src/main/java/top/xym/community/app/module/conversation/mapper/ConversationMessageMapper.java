package top.xym.community.app.module.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.xym.community.app.module.conversation.model.entity.ConversationMessage;

import java.util.List;

public interface ConversationMessageMapper extends BaseMapper<ConversationMessage> {

    /**
     * 查询会话中某用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM conversation_message " +
            "WHERE session_id = #{sessionId} " +
            "AND receiver_id = #{receiverId} " +
            "AND is_read = 0 " +
            "AND deleted = 0")
    int countUnreadMessages(@Param("sessionId") Long sessionId,
                            @Param("receiverId") Long receiverId);

    /**
     * 标记会话中某用户的消息为已读
     */
    @Update("UPDATE conversation_message SET " +
            "is_read = 1, " +
            "read_time = NOW() " +
            "WHERE session_id = #{sessionId} " +
            "AND receiver_id = #{receiverId} " +
            "AND is_read = 0")
    int markMessagesAsRead(@Param("sessionId") Long sessionId,
                           @Param("receiverId") Long receiverId);

    /**
     * 查询会话中某用户的所有未读消息
     */
    @Select("SELECT * FROM conversation_message " +
            "WHERE session_id = #{sessionId} " +
            "AND receiver_id = #{receiverId} " +
            "AND is_read = 0 " +
            "AND deleted = 0 " +
            "ORDER BY create_time ASC")
    List<ConversationMessage> selectUnreadMessages(@Param("sessionId") Long sessionId,
                                                   @Param("receiverId") Long receiverId);
}