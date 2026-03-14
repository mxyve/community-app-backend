package top.xym.community.app.module.session.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xym.community.app.module.session.mapper.ChatSessionMapper;
import top.xym.community.app.module.session.model.dto.SessionCreateRequest;
import top.xym.community.app.module.session.model.dto.SessionResponse;
import top.xym.community.app.module.session.model.entity.ChatSession;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    /**
     * 创建会话
     */
    public SessionResponse createSession(SessionCreateRequest request, Long userId) {
        ChatSession session = new ChatSession();
        session.setTitle(request.getTitle());
        session.setUserId(userId);
        session.setModelName(request.getModelName());
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setDeleted(0);
        String agentType = request.getAgentType();
        if (agentType == null || agentType.trim().isEmpty()) {
            agentType = "normal";
        }
        session.setAgentType(agentType);

        chatSessionMapper.insert(session);
        return convertToResponse(session);
    }

    private SessionResponse convertToResponse(ChatSession session) {
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getModelName(),
                session.getAgentType(),
                session.getStatus(),
                session.getLastMessage(),
                session.getLastMessageTime(),
                session.getCreateTime(),
                session.getUpdateTime()
        );
    }

    /**
     * 删除会话
     */
    public void deleteSession(Long sessionId, Long userId) {
        // 检查会话是否存在且属于当前用户
        ChatSession session = chatSessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId)
        );
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 删除会话
        chatSessionMapper.deleteById(sessionId);
    }

}
