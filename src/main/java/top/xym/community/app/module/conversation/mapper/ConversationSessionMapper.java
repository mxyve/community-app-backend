package top.xym.community.app.module.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.xym.community.app.module.conversation.model.entity.ConversationSession;

public interface ConversationSessionMapper extends BaseMapper<ConversationSession> {

    /**
     * 根据用户ID和商家ID查询已有会话
     */
    @Select("SELECT * FROM conversation_session " +
            "WHERE user_id = #{userId} " +
            "AND merchant_id = #{merchantId} " +
            "AND deleted = 0 " +
            "LIMIT 1")
    ConversationSession selectByUserAndMerchant(@Param("userId") Long userId,
                                                @Param("merchantId") Long merchantId);

    /**
     * 更新会话最后消息
     */
    @Update("UPDATE conversation_session SET " +
            "last_message = #{lastMessage}, " +
            "last_message_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE id = #{sessionId}")
    int updateLastMessage(@Param("sessionId") Long sessionId,
                          @Param("lastMessage") String lastMessage);

    /**
     * 增加商家未读数
     */
    @Update("UPDATE conversation_session SET " +
            "merchant_unread_count = merchant_unread_count + 1 " +
            "WHERE id = #{sessionId}")
    int incrementMerchantUnreadCount(@Param("sessionId") Long sessionId);

    /**
     * 增加用户未读数
     */
    @Update("UPDATE conversation_session SET " +
            "user_unread_count = user_unread_count + 1 " +
            "WHERE id = #{sessionId}")
    int incrementUserUnreadCount(@Param("sessionId") Long sessionId);

    /**
     * 清零商家未读数
     */
    @Update("UPDATE conversation_session SET " +
            "merchant_unread_count = 0 " +
            "WHERE id = #{sessionId}")
    int clearMerchantUnreadCount(@Param("sessionId") Long sessionId);

    /**
     * 清零用户未读数
     */
    @Update("UPDATE conversation_session SET " +
            "user_unread_count = 0 " +
            "WHERE id = #{sessionId}")
    int clearUserUnreadCount(@Param("sessionId") Long sessionId);
}