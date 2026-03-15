package top.xym.community.app.module.session.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xym.community.app.model.dto.PageResponse;
import top.xym.community.app.module.message.mapper.ChatMessageMapper;
import top.xym.community.app.module.message.model.entity.ChatMessage;
import top.xym.community.app.module.session.mapper.ChatSessionMapper;
import top.xym.community.app.module.session.model.dto.SessionCreateRequest;
import top.xym.community.app.module.session.model.dto.SessionResponse;
import top.xym.community.app.module.session.model.dto.SessionUpdateTitleRequest;
import top.xym.community.app.module.session.model.entity.ChatSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SessionService {

    @Autowired
    private ChatSessionMapper sessionMapper;
    @Autowired
    private ChatMessageMapper messageMapper;
    @Autowired
    private ChatClient dashScopeChatClient;

    /**
     * 创建会话
     */
    public SessionResponse createSession(SessionCreateRequest request, Long userId) {
        ChatSession session = new ChatSession();
        session.setTitle(request.getTitle());
        session.setUserId(userId);
        session.setModelName(request.getModelName());
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now()); // 补充创建时间
        session.setUpdateTime(LocalDateTime.now()); // 补充更新时间
        session.setDeleted(0);
        String agentType = request.getAgentType();
        if (agentType == null || agentType.trim().isEmpty()) {
            agentType = "normal";
        }
        session.setAgentType(agentType);

        sessionMapper.insert(session);
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
     * 分页获取用户所有会话
     */
    public PageResponse<SessionResponse> getUserSessions(Long userId, Long current, Long size) {
        Page<ChatSession> page = new Page<>(current, size);
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getDeleted, 0)
                .orderByDesc(ChatSession::getCreateTime);
        Page<ChatSession> sessionPage = sessionMapper.selectPage(page, queryWrapper);
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


    public SessionResponse getSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getDeleted, 0)
        );
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        return convertToResponse(session);
    }

    // 更新会话标题
    public SessionResponse updateSessionTitle(Long sessionId, Long userId, SessionUpdateTitleRequest request) {
        ChatSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getDeleted, 0)
        );
        if (session == null) {
            throw new RuntimeException("会话不存在或已删除");
        }
        session.setTitle(request.getTitle());
        session.setUpdateTime(LocalDateTime.now());
        int rows = sessionMapper.updateById(session);
        if (rows == 0) {
            throw new RuntimeException("会话标题修改失败");
        }
        return convertToResponse(session);
    }

    public void updateLastMessage(Long sessionId, String mergedLastMessage) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || session.getDeleted() == 1) {
            throw new RuntimeException("会话不存在或已删除");
        }
        ChatSession updateSession = new ChatSession();
        updateSession.setId(sessionId);
        updateSession.setLastMessage(mergedLastMessage);
        updateSession.setLastMessageTime(LocalDateTime.now());
        updateSession.setUpdateTime(LocalDateTime.now());
        LambdaQueryWrapper<ChatSession> updateWrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getDeleted, 0);
        int rows = sessionMapper.update(updateSession, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("会话最后消息更新失败");
        }
    }

    // 删除会话
    public void deleteSession(Long sessionId, Long userId) {
        // 检查会话是否存在且属于当前用户
        ChatSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId)
        );
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 先删除关联的 chat_message
        LambdaQueryWrapper<ChatMessage> messageWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId);
        messageMapper.delete(messageWrapper);

        // 再删除会话
        sessionMapper.deleteById(sessionId);
    }

    /**
     * 根据用户第一条消息生成并更新会话标题
     */
    public SessionResponse generateSessionTitle(Long sessionId, Long userId, String userFirstMessage) {
        // 1. 校验会话归属
        ChatSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getId, sessionId)
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getDeleted, 0)
        );
        if (session == null) {
            throw new RuntimeException("会话不存在或已删除");
        }

        // 2. 调用AI生成标题（核心逻辑）
        String autoTitle = generateTitleByAI(userFirstMessage);

        // 3. 更新会话标题
        session.setTitle(autoTitle);
        session.setUpdateTime(LocalDateTime.now());
        int rows = sessionMapper.updateById(session);
        if (rows == 0) {
            throw new RuntimeException("会话标题生成失败");
        }
        log.info("会话{}自动生成标题：{}", sessionId, autoTitle);
        return convertToResponse(session);
    }

    /**
     * AI生成标题的核心方法（复用参考逻辑）
     */
    private String generateTitleByAI(String userFirstMessage) {
        try {
            // 提示词：强调10字以内、概括性、无特殊符号
            String prompt = """
                任务：为用户的提问生成10字以内的概括性会话标题，仅返回标题文本。
                要求：
                1. 必须是概括性短语，不是消息截取或缩写
                2. 严格≤10字，无空格、无特殊符号
                3. 包含核心关键词
                用户提问：%s
                标题：
                """.formatted(userFirstMessage);

            // 调用通义千问生成标题
            String rawTitle = dashScopeChatClient.prompt(prompt).call().content().trim();

            // 兜底处理（AI返回无效时）
            String finalTitle = rawTitle.isEmpty() || rawTitle.length() > 10
                    ? generateKeywordTitle(userFirstMessage)
                    : rawTitle.replaceAll("\\s+", "").replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");

            return finalTitle.isEmpty() ? "新会话-" + System.currentTimeMillis() / 1000 : finalTitle;
        } catch (Exception e) {
            log.error("AI生成标题失败，使用兜底逻辑", e);
            return generateKeywordTitle(userFirstMessage);
        }
    }

    /**
     * 兜底：提取关键词生成标题
     */
    private String generateKeywordTitle(String userFirstMessage) {
        String trimmedMsg = userFirstMessage.trim()
                .replace("什么是", "")
                .replace("如何", "")
                .replace("怎么", "")
                .replace("？", "")
                .replace("：", "");
        String[] keywords = trimmedMsg.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(keywords.length, 2); i++) {
            if (!keywords[i].isEmpty()) {
                sb.append(keywords[i]);
            }
        }
        String title = sb.toString();
        return title.length() > 10 ? title.substring(0, 10) : (title.isEmpty() ? "新会话" : title);
    }

}
