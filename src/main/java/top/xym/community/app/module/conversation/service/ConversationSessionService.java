package top.xym.community.app.module.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.conversation.enums.SessionStatusEnum;
import top.xym.community.app.module.conversation.mapper.ConversationSessionMapper;
import top.xym.community.app.module.conversation.model.dto.SessionCreateRequest;
import top.xym.community.app.module.conversation.model.dto.SessionResponse;
import top.xym.community.app.module.conversation.model.entity.ConversationSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationSessionService {

    private final ConversationSessionMapper sessionMapper;

    /**
     * 创建会话（居民端）
     */
    @Transactional
    public SessionResponse createSession(SessionCreateRequest request, Long userId) {
        // 检查是否已存在未删除的会话
        ConversationSession existingSession = sessionMapper.selectByUserAndMerchant(userId, request.getMerchantId());
        if (existingSession != null && existingSession.getDeleted() == 0) {
            log.info("会话已存在，sessionId: {}", existingSession.getId());
            return convertToResponse(existingSession);
        }

        // 创建新会话
        ConversationSession session = new ConversationSession();
        session.setUserId(userId);
        session.setMerchantId(request.getMerchantId());
        session.setOrderId(request.getOrderId());
        session.setLastMessage("");
        session.setLastMessageTime(LocalDateTime.now());
        session.setUserUnreadCount(0);
        session.setMerchantUnreadCount(0);
        session.setStatus(SessionStatusEnum.ACTIVE);
        session.setUserDeleted(0);
        session.setMerchantDeleted(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setDeleted(0);

        sessionMapper.insert(session);
        log.info("创建会话成功，sessionId: {}", session.getId());

        return convertToResponse(session);
    }

    /**
     * 获取或创建会话（发送消息时调用）
     */
    @Transactional
    public ConversationSession getOrCreateSession(Long userId, Long merchantId, Long orderId) {
        ConversationSession session = sessionMapper.selectByUserAndMerchant(userId, merchantId);
        if (session != null && session.getDeleted() == 0) {
            return session;
        }

        ConversationSession newSession = new ConversationSession();
        newSession.setUserId(userId);
        newSession.setMerchantId(merchantId);
        newSession.setOrderId(orderId);
        newSession.setLastMessage("");
        newSession.setLastMessageTime(LocalDateTime.now());
        newSession.setUserUnreadCount(0);
        newSession.setMerchantUnreadCount(0);
        newSession.setStatus(SessionStatusEnum.ACTIVE);
        newSession.setUserDeleted(0);
        newSession.setMerchantDeleted(0);
        newSession.setCreateTime(LocalDateTime.now());
        newSession.setUpdateTime(LocalDateTime.now());
        newSession.setDeleted(0);

        sessionMapper.insert(newSession);
        return newSession;
    }

    /**
     * 获取用户会话列表（分页）
     */
    public PageResponse<SessionResponse> getUserSessions(Long userId, Long current, Long size) {
        Page<ConversationSession> page = new Page<>(current, size);
        LambdaQueryWrapper<ConversationSession> queryWrapper = new LambdaQueryWrapper<ConversationSession>()
                .eq(ConversationSession::getUserId, userId)
                .eq(ConversationSession::getUserDeleted, 0)
                .eq(ConversationSession::getDeleted, 0)
                .orderByDesc(ConversationSession::getLastMessageTime);

        Page<ConversationSession> sessionPage = sessionMapper.selectPage(page, queryWrapper);

        List<SessionResponse> records = sessionPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                sessionPage.getCurrent(),
                sessionPage.getSize(),
                sessionPage.getTotal(),
                sessionPage.getPages(),
                records
        );
    }

    /**
     * 获取会话详情（居民端，带权限校验）
     */
    public ConversationSession getSessionWithAuth(Long sessionId, Long userId) {
        ConversationSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getDeleted() == 1) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该会话");
        }
        return session;
    }

    /**
     * 更新最后消息
     */
    @Transactional
    public void updateLastMessage(Long sessionId, String lastMessage) {
        ConversationSession session = new ConversationSession();
        session.setId(sessionId);
        session.setLastMessage(lastMessage);
        session.setLastMessageTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    /**
     * 增加商家未读数（居民发消息时）
     */
    public void incrementMerchantUnreadCount(Long sessionId) {
        sessionMapper.incrementMerchantUnreadCount(sessionId);
    }

    /**
     * 清零用户未读数（居民已读消息时）
     */
    public void clearUserUnreadCount(Long sessionId) {
        sessionMapper.clearUserUnreadCount(sessionId);
    }

    /**
     * 删除会话（软删除，居民端）
     */
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ConversationSession session = getSessionWithAuth(sessionId, userId);
        session.setUserDeleted(1);
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);
        log.info("会话删除成功，sessionId: {}", sessionId);
    }

    /**
     * 关闭会话（居民端）
     */
    @Transactional
    public void closeSession(Long sessionId, Long userId) {
        ConversationSession session = getSessionWithAuth(sessionId, userId);
        session.setStatus(SessionStatusEnum.USER_CLOSED);
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);
        log.info("会话关闭成功，sessionId: {}", sessionId);
    }

    /**
     * 实体转响应DTO
     */
    private SessionResponse convertToResponse(ConversationSession session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setUserId(session.getUserId());
        response.setMerchantId(session.getMerchantId());
        response.setOrderId(session.getOrderId());
        response.setLastMessage(session.getLastMessage());
        response.setLastMessageTime(session.getLastMessageTime());
        response.setUserUnreadCount(session.getUserUnreadCount());
        response.setMerchantUnreadCount(session.getMerchantUnreadCount());
        response.setStatus(session.getStatus());
        response.setCreateTime(session.getCreateTime());
        response.setUpdateTime(session.getUpdateTime());
        return response;
    }
}