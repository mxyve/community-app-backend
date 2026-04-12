package top.xym.community.app.module.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.xym.community.app.module.conversation.enums.MsgTypeEnum;
import top.xym.community.app.module.conversation.enums.ReceiverTypeEnum;
import top.xym.community.app.module.conversation.enums.SenderTypeEnum;
import top.xym.community.app.module.conversation.mapper.ConversationMessageMapper;
import top.xym.community.app.module.conversation.model.dto.MessageResponse;
import top.xym.community.app.module.conversation.model.dto.MessageSendRequest;
import top.xym.community.app.module.conversation.model.entity.ConversationMessage;
import top.xym.community.app.module.conversation.model.entity.ConversationSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMessageService {

    private final ConversationMessageMapper messageMapper;
    private final ConversationSessionService sessionService;

    /**
     * 发送消息（居民端）
     */
    @Transactional
    public MessageResponse sendMessage(MessageSendRequest request, Long userId) {
        // 1. 获取或创建会话
        ConversationSession session;
        if (request.getSessionId() == null) {
            // 居民发消息，receiverId 就是商家ID
            session = sessionService.getOrCreateSession(userId, request.getReceiverId(), null);
        } else {
            session = sessionService.getSessionWithAuth(request.getSessionId(), userId);
        }

        // 2. 保存消息
        ConversationMessage message = new ConversationMessage();
        message.setSessionId(session.getId());
        message.setSenderId(userId);
        message.setSenderType(SenderTypeEnum.USER);
        message.setReceiverId(request.getReceiverId());
        message.setReceiverType(ReceiverTypeEnum.MERCHANT);
        message.setMsgType(request.getMsgType() != null ? request.getMsgType() : MsgTypeEnum.TEXT);
        message.setContent(request.getContent());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        message.setDeleted(0);

        messageMapper.insert(message);

        // 3. 更新会话最后消息
        sessionService.updateLastMessage(session.getId(), request.getContent());

        // 4. 增加商家未读数
        sessionService.incrementMerchantUnreadCount(session.getId());

        log.info("消息发送成功，messageId: {}, sessionId: {}", message.getId(), session.getId());

        return convertToResponse(message);
    }

    /**
     * 获取会话历史消息（分页）
     */
    public List<MessageResponse> getSessionMessages(Long sessionId, Long userId, Long current, Long size) {
        // 验证权限
        sessionService.getSessionWithAuth(sessionId, userId);

        Page<ConversationMessage> page = new Page<>(current, size);
        LambdaQueryWrapper<ConversationMessage> queryWrapper = new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getSessionId, sessionId)
                .eq(ConversationMessage::getDeleted, 0)
                .orderByAsc(ConversationMessage::getCreateTime);

        Page<ConversationMessage> messagePage = messageMapper.selectPage(page, queryWrapper);

        return messagePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 拉取新消息（轮询专用）
     */
    public List<MessageResponse> getNewMessages(Long sessionId, Long userId, Long fromId) {
        // 验证权限
        sessionService.getSessionWithAuth(sessionId, userId);

        LambdaQueryWrapper<ConversationMessage> queryWrapper = new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getSessionId, sessionId)
                // ！！！ 只查比客户端已有消息 ID 更大的消息（即新消息）
                .gt(ConversationMessage::getId, fromId)
                .eq(ConversationMessage::getDeleted, 0)
                .orderByAsc(ConversationMessage::getId);

        List<ConversationMessage> messages = messageMapper.selectList(queryWrapper);
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 标记消息已读（居民端）
     */
    @Transactional
    public void markMessagesAsRead(Long sessionId, Long userId) {
        // 验证权限
        sessionService.getSessionWithAuth(sessionId, userId);

        // 标记消息为已读
        LambdaQueryWrapper<ConversationMessage> updateWrapper = new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getSessionId, sessionId)
                .eq(ConversationMessage::getReceiverId, userId)
                .eq(ConversationMessage::getReceiverType, ReceiverTypeEnum.USER)
                .eq(ConversationMessage::getIsRead, 0);

        ConversationMessage updateMessage = new ConversationMessage();
        updateMessage.setIsRead(1);
        updateMessage.setReadTime(LocalDateTime.now());

        // 批量更新多条符合条件的消息
        messageMapper.update(updateMessage, updateWrapper);

        // 清零用户未读数
        sessionService.clearUserUnreadCount(sessionId);

        log.info("标记已读成功，sessionId: {}, userId: {}", sessionId, userId);
    }

    /**
     * 删除消息（居民端，软删除）
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ConversationMessage message = messageMapper.selectById(messageId);
        if (message == null || message.getDeleted() == 1) {
            throw new RuntimeException("消息不存在");
        }

        // 验证权限：只有发送者可以删除自己的消息
        if (!message.getSenderId().equals(userId) || message.getSenderType() != SenderTypeEnum.USER) {
            throw new RuntimeException("无权删除该消息");
        }

        message.setDeleted(1);
        messageMapper.updateById(message);
        log.info("消息删除成功，messageId: {}", messageId);
    }

    /**
     * 获取会话未读数（居民端）
     */
    public Integer getUnreadCount(Long sessionId, Long userId) {
        sessionService.getSessionWithAuth(sessionId, userId);

        LambdaQueryWrapper<ConversationMessage> queryWrapper = new LambdaQueryWrapper<ConversationMessage>()
                .eq(ConversationMessage::getSessionId, sessionId)
                .eq(ConversationMessage::getReceiverId, userId)
                .eq(ConversationMessage::getReceiverType, ReceiverTypeEnum.USER)
                .eq(ConversationMessage::getIsRead, 0)
                .eq(ConversationMessage::getDeleted, 0);

        return Math.toIntExact(messageMapper.selectCount(queryWrapper));
    }

    /**
     * 实体转响应DTO
     */
    private MessageResponse convertToResponse(ConversationMessage message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSessionId(message.getSessionId());
        response.setSenderId(message.getSenderId());
        response.setSenderType(message.getSenderType());
        response.setReceiverId(message.getReceiverId());
        response.setReceiverType(message.getReceiverType());
        response.setMsgType(message.getMsgType());
        response.setContent(message.getContent());
        response.setIsRead(message.getIsRead());
        response.setReadTime(message.getReadTime());
        response.setCreateTime(message.getCreateTime());
        return response;
    }
}