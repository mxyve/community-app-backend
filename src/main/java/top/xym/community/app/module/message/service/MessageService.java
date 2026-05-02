package top.xym.community.app.module.message.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import top.xym.community.app.mapper.UserMapper;
import top.xym.community.app.model.entity.User;
import top.xym.community.app.module.message.mapper.ChatMessageMapper;
import top.xym.community.app.module.message.model.dto.MessageResponse;
import top.xym.community.app.module.message.model.dto.MessageSendRequest;
import top.xym.community.app.module.message.model.entity.ChatMessage;
import top.xym.community.app.module.message.tool.CommunityKnowledgeTool;
import top.xym.community.app.module.message.tool.CommunityServiceTool;
import top.xym.community.app.module.message.tool.OrderTool;
import top.xym.community.app.module.session.model.dto.SessionResponse;
import top.xym.community.app.module.session.model.dto.SessionUpdateTitleRequest;
import top.xym.community.app.module.session.service.SessionService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.micrometer.core.instrument.util.StringEscapeUtils.escapeJson;


/**
 * 消息服务：处理用户消息发送、AI流式响应、消息存储与查询
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final SessionService sessionService;
    private final ChatModel chatModel;

    private final UserMapper userMapper;

    private final CommunityServiceTool communityServiceTool;
    private final CommunityKnowledgeTool communityKnowledgeTool;

    private ChatClient chatClient;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultTools(communityServiceTool, communityKnowledgeTool)
                .build();
    }

    /**
     * 发送消息（非流式，一次性返回）
     */
    public String sendMessageStream(MessageSendRequest request, Long userId) {
        // ==================== 语音输入识别 ====================
        if (request.getAudio() != null && !request.getAudio().isEmpty()) {
            byte[] audio = java.util.Base64.getDecoder().decode(request.getAudio());
            String userText = speechToText(audio);
            request.setContent(userText);
        }

        try {
            validateSession(request.getSessionId(), userId);

            // 先查用户
            User user = userMapper.selectById(userId);

            // AI提取用户输入的地址
            List<String> address = extractProvinceCityDistrict(request.getContent());
            String province = address.get(0);
            String city = address.get(1);
            String district = address.get(2);

            // 最终规则：
            // 用户给了任何地址 → 纯用用户的
            // 用户完全没给地址 → 用数据库的
            boolean userNotProvideAnyAddress = (province == null && city == null && district == null);
            if (userNotProvideAnyAddress) {
                province = user.getProvince();
                city = user.getCity();
                district = user.getDistrict();
            }

            saveUserMessage(request, userId);

            // ======================
            // 普通对话 → 一次性返回 { text, audio }
            // ======================
            List<String> imageUrlList = new ArrayList<>();
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                for (MessageSendRequest.MessageAttachmentDTO attachment : request.getAttachments()) {
                    if ("image".equalsIgnoreCase(attachment.getType())) {
                        imageUrlList.add(attachment.getUrl());
                    }
                }
            }

            Prompt prompt = buildPrompt(request.getContent(), imageUrlList,
                    request.getSessionId(), province, city, district);

            // 同步获取完整回答
            String fullText;
            boolean isServiceRequest = false;

            try {
                // 第一步：让AI只做意图判断（超轻量，不会超时）
                String intentPrompt = """
                    你只需要回答【yes】或【no】
                    规则：
                    1. 用户要【查找服务、推荐服务、附近服务、有什么服务】→ 回答 yes
                    2. 其他所有问题 → 回答 no
                    3. 只允许回答一个单词：yes 或 no
                    用户问题：%s
                """.formatted(request.getContent());

                String intent = chatClient.prompt(intentPrompt).call().content().trim().toLowerCase();
                isServiceRequest = "yes".equals(intent);

                // 第二步：如果是查服务 → 直接返回JSON
                if (isServiceRequest) {
                    fullText = communityServiceTool.queryCommunityService(
                            userId, province, city, district, request.getContent()
                    );
                } else {
                    // 正常对话（图片、问题、聊天都走这里）
                    fullText = chatClient.prompt(prompt).call().content();
                }

            } catch (Exception e) {
                // AI报错 → 直接返回错误，不返回服务列表！
                fullText = "抱歉，我暂时无法回答你的问题，请稍后再试。";
            }

            // 服务JSON不合成语音，普通对话才语音
            byte[] aiAudioBytes = new byte[0];
            String aiAudioBase64 = "";
            if (!fullText.trim().startsWith("[")) {
                aiAudioBytes = textToSpeech(fullText);
                aiAudioBase64 = java.util.Base64.getEncoder().encodeToString(aiAudioBytes);
            }

            // 保存消息
            ChatMessage assistantMessage = saveAssistantMessageTemp(request, userId);
            updateAssistantMessageFullContent(assistantMessage.getId(), fullText, "completed", aiAudioBase64);
            updateSessionLastMessage(request.getSessionId(), fullText);
            autoGenerateSessionTitle(request.getSessionId(), userId, fullText);

            // 服务卡片直接返回JSON数组，普通对话返回 {text,audio}
            if (fullText.trim().startsWith("[")) {
                return fullText;
            } else {
                return "{\"text\":\"" + escapeJson(fullText) + "\",\"audio\":\"" + aiAudioBase64 + "\"}";
            }

        } catch (Exception e) {
            throw new RuntimeException("请求失败：" + e.getMessage());
        }
    }

    /**
     * 智能提取地址：
     * 支持：省、省市、省市区
     * 提取不到 → 返回 [null, null, null]
     */
    private List<String> extractProvinceCityDistrict(String userText) {
        if (userText == null || userText.isBlank()) {
            return Arrays.asList(null, null, null);
        }

        try {
            // 超级严格指令！！！
            String prompt = """
                任务：从用户输入里提取【省、市、区】
                规则【必须严格遵守，违反会导致严重错误】：
                1. 用户没提到的，必须填空，不能自己编、不能自己猜、不能自己补！
                2. 只提取用户明确说的内容
                3. 必须严格按格式返回，只返回这一行，不要其他任何文字
                格式：省,市,区
                现在提取用户输入：%s
            """.formatted(userText);

            String result = chatClient.prompt(prompt).call().content().trim();

            if (result == null || result.isBlank()) {
                return Arrays.asList(null, null, null);
            }

            String[] parts = result.split(",", -1);
            String province = parts.length > 0 ? parts[0].trim() : null;
            String city = parts.length > 1 ? parts[1].trim() : null;
            String district = parts.length > 2 ? parts[2].trim() : null;

            // 空字符串 → 转 null
            province = province.isEmpty() ? null : province;
            city = city.isEmpty() ? null : city;
            district = district.isEmpty() ? null : district;

            return Arrays.asList(province, city, district);

        } catch (Exception e) {
            return Arrays.asList(null, null, null);
        }
    }

    public void updateSessionLastMessage(Long sessionId, String fullContent) {
        try {
            sessionService.updateLastMessage(sessionId, fullContent);
        } catch (Exception e) {
            // 打印日志（不影响核心流程）
            System.err.println("更新会话最后消息失败：sessionId=" + sessionId + ", error=" + e.getMessage());
        }
    }

    /**
     * 分页查询会话内的消息历史（按创建时间升序）
     * @param sessionId 会话ID
     * @param userId 用户ID（权限校验）
     * @param current 页码（从1开始）
     * @param size 每页条数
     * @return 消息历史分页结果
     */
    public List<MessageResponse> getSessionMessages(Long sessionId, Long userId, Long current, Long size) {
        // 1. 验证会话合法性
        validateSession(sessionId, userId);

        // 2. 构建查询条件：会话ID+未删除+按创建时间升序
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, 0)
                .orderByAsc(ChatMessage::getCreateTime);

        // 3. 执行分页查询（MyBatis-Plus语法）
        Page<ChatMessage> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        Page<ChatMessage> messagePage = chatMessageMapper.selectPage(page, queryWrapper);

        // 4. 转换为响应DTO
        return messagePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 验证会话合法性（用户是否有权访问该会话）
     */
    public void validateSession(Long sessionId, Long userId) {
        try {
            sessionService.getSession(sessionId, userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("会话不存在或无权访问：" + e.getMessage());
        }
    }

    /**
     * 构建 RAG 增强提示词（带向量检索 + 知识库过滤）
     */
    private Prompt buildPrompt(String userContent, List<String> imageUrlList, Long sessionId,
                               String province, String city, String district) {
        List<ChatMessage> historyMessages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .eq(ChatMessage::getDeleted, 0)
                        .orderByAsc(ChatMessage::getCreateTime)
                        // 最近5条记忆，防止过长
                        .last("LIMIT 5")
        );

        List<Message> messages = new ArrayList<>();

        // 把历史消息加入 prompt
        for (ChatMessage msg : historyMessages) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
            }
        }

        String enhancedUserMessage = userContent;

        // ====================== 智能体系统指令 ======================
        messages.add(new SystemMessage("""
            你是智能社区服务助手，严格遵守：
            
            1. 用户【找服务、查服务、推荐服务、附近服务】
               → 调用 CommunityServiceTool
               → 必须【原封不动返回工具的JSON数组】，不能加任何文字，不能解释
            
            2. 用户【问怎么下单、怎么预约、流程、问题、聊天、问候】
               → 不要调用任何工具
               → 直接用自然语言友好回答
            
            3. 用户【问社区知识、政策、公告】
               → 调用 CommunityKnowledgeTool
               → 用自然语言回答
            
            4. 绝对不能把JSON改成文字，必须原样返回给前端渲染卡片
            
            用户地区：%s %s %s
        """.formatted(province, city, district)));

        // ====================== 图片处理 ======================
        List<Media> mediaList = new ArrayList<>();
        if (imageUrlList != null && !imageUrlList.isEmpty()) {
            for (String imageUrl : imageUrlList) {
                try {
                    Media media = new Media(
                            imageUrl.toLowerCase().endsWith("jpeg") ? MimeTypeUtils.IMAGE_JPEG : MimeTypeUtils.IMAGE_PNG,
                            new UrlResource(imageUrl.trim())
                    );
                    mediaList.add(media);
                } catch (Exception ignored) {}
            }
        }

        // ====================== 用户消息 ======================
        UserMessage userMessage = UserMessage.builder()
                .text(enhancedUserMessage)
                .media(mediaList)
                .build();

        if (CollUtil.isNotEmpty(mediaList)) {
            userMessage.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        }
        messages.add(userMessage);

        boolean hasImage = !mediaList.isEmpty();

        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(hasImage ? "qwen-vl-max-latest" : "qwen-plus")
                .withMultiModel(hasImage)
                .withVlHighResolutionImages(true)
                .withTemperature(0.2)
                .withTopP(0.6)
                .build();

        return new Prompt(messages, options);
    }

    /**
     * 保存用户消息到数据库
     */
    public void saveUserMessage(MessageSendRequest request, Long userId) {
        List<String> urls = request.getAttachments() == null ? List.of()
                : request.getAttachments().stream()
                .filter(a -> "image".equalsIgnoreCase(a.getType()))
                .map(MessageSendRequest.MessageAttachmentDTO::getUrl)
                .collect(Collectors.toList());
        ChatMessage userMessage = new ChatMessage();
        userMessage.setImageUrls(urls.isEmpty() ? null : String.join(",", urls));
        userMessage.setSessionId(request.getSessionId());
        userMessage.setUserId(userId);
        userMessage.setRole("user"); // 角色：用户
        userMessage.setContent(request.getContent());
        userMessage.setModelName(request.getModelName());
        userMessage.setStatus(1); // 状态：1-成功
        userMessage.setCreateTime(LocalDateTime.now());
        userMessage.setUpdateTime(LocalDateTime.now());
        userMessage.setDeleted(0); // 未删除

        chatMessageMapper.insert(userMessage);
    }

    /**
     * 临时保存助手消息（初始状态：处理中）
     */
    public ChatMessage saveAssistantMessageTemp(MessageSendRequest request, Long userId) {
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(request.getSessionId());
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant"); // 角色：AI助手
        assistantMessage.setContent(""); // 初始内容为空，后续更新
        assistantMessage.setModelName(request.getModelName());
        assistantMessage.setStatus(0); // 状态：0-处理中
        assistantMessage.setCreateTime(LocalDateTime.now());
        assistantMessage.setUpdateTime(LocalDateTime.now());
        assistantMessage.setDeleted(0); // 未删除

        chatMessageMapper.insert(assistantMessage);
        return assistantMessage;
    }

    /**
     * 流式响应结束后，更新助手消息的完整内容和状态
     */
    public void updateAssistantMessageFullContent(Long messageId, String fullContent, String status, String audioBase64) {
        if (messageId == 0) {
            return;
        }

        // 构建更新对象
        ChatMessage updateMsg = new ChatMessage();
        updateMsg.setId(messageId);
        updateMsg.setContent(fullContent); // 保存完整内容
        if (audioBase64 != null && !audioBase64.isEmpty()) {
            updateMsg.setAudio(audioBase64);
        }
        updateMsg.setStatus("completed".equals(status) ? 1 : 2); // 1-成功，2-失败
        updateMsg.setUpdateTime(LocalDateTime.now());

        // 构建更新条件：消息ID+角色为助手+未删除
        LambdaQueryWrapper<ChatMessage> updateWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getId, messageId)
                .eq(ChatMessage::getRole, "assistant")
                .eq(ChatMessage::getDeleted, 0);

        int rows = chatMessageMapper.update(updateMsg, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("助手消息更新失败");
        }
    }

    /**
     * 自动生成会话标题（如果用户未指定标题）
     */
    public void autoGenerateSessionTitle(Long sessionId, Long userId, String fullText) {
        try {
            // 1. 查询当前会话信息
            SessionResponse session = sessionService.getSession(sessionId, userId);
            // 2. 判断是否需要自动生成标题（标题为空、默认值或过短）
            if (session.getTitle() == null || session.getTitle().trim().isEmpty()
                    || session.getTitle().equals("新会话") || session.getTitle().length() < 5) {
                // 3. 提取AI响应前20字作为标题（末尾加省略号）
                String autoTitle = fullText.length() > 20 ? fullText.substring(0, 20) + "..." : fullText;
                // 4. 更新会话标题：用 Setter 方法赋值（无需构造函数）
                SessionUpdateTitleRequest titleRequest = new SessionUpdateTitleRequest();
                titleRequest.setTitle(autoTitle); // 直接调用 setTitle 方法
                sessionService.updateSessionTitle(sessionId, userId, titleRequest);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 将ChatMessage实体转换为前端响应DTO
     */
    public MessageResponse convertToResponse(ChatMessage chatMessage) {
        MessageResponse response = new MessageResponse(
                chatMessage.getId(),
                chatMessage.getSessionId(),
                chatMessage.getUserId(),
                chatMessage.getRole(),
                chatMessage.getContent(),
                chatMessage.getAudio(),
                chatMessage.getModelName(),
                chatMessage.getImageUrls(),
                null, // tokens：ChatMessage 无该字段，设为 null
                0,    // hasThinking：默认 0（未开启）
                null, // thinkingContent：默认 null
                0,    // webSearch：默认 0（未开启）
                chatMessage.getStatus(), // 消息状态
                "",   // statusDesc：先传空字符串，后续通过方法设置
                chatMessage.getCreateTime(),
                chatMessage.getUpdateTime()
        );
        response.setAudio(chatMessage.getAudio());
        // 调用状态转换方法，设置 statusDesc
        response.setStatusDesc(chatMessage.getStatus());
        return response;
    }

    // ==================== 阿里云语音配置 ====================
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    // 你提供的APPKEY
    private final String NLS_APP_KEY = "3OpIBd1uEnBKO139";

    // 缓存阿里云Token（避免频繁申请）
    private String nlsToken;
    // 缓存Token过期时间
    private long tokenExpireTime;

    /**
     * 阿里云官方标准：获取语音服务访问 Token（长期稳定版）
     */
    public String getNlsToken() {
        if (nlsToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return nlsToken;
        }

        try {
            String regionId = "cn-shanghai";
            String domain = "nls-meta.cn-shanghai.aliyuncs.com";
            String version = "2019-02-28";
            String action = "CreateToken";

            DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setDomain(domain);
            request.setVersion(version);
            request.setAction(action);
            request.setMethod(MethodType.GET);
            request.setProtocol(ProtocolType.HTTPS);

            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();

            // 关键：打印阿里云返回的真实内容
            System.out.println("阿里云返回：" + data);

            JSONObject json = JSON.parseObject(data);
            // 兼容错误结构
            if (json.containsKey("Token")) {
                nlsToken = json.getJSONObject("Token").getString("Id");
                long expireTime = json.getJSONObject("Token").getLong("ExpireTime");
                tokenExpireTime = expireTime * 1000 - 120000;
            } else {
                throw new RuntimeException("获取Token失败，返回：" + data);
            }

            System.out.println("✅ Token获取成功：" + nlsToken);
            return nlsToken;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取Token失败：" + e.getMessage());
        }
    }

    // ========================== 语音识别（音频 → 文字）==========================
    public String speechToText(byte[] audioBytes) {
        System.out.println("收到音频数据，大小：" + (audioBytes != null ? audioBytes.length : 0) + " 字节");
        try {
            String token = getNlsToken();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL("https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/asr").openConnection();
            conn.setRequestMethod("POST");
            // ✅ 修改 Content-Type 支持 mp3
            conn.setRequestProperty("Content-Type", "audio/mp3;rate=16000");
            conn.setRequestProperty("X-NLS-Token", token);
            conn.setRequestProperty("X-NLS-Appkey", NLS_APP_KEY);
            conn.setDoOutput(true);

            conn.getOutputStream().write(audioBytes);

            // 检查响应状态
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        String errorMsg = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                        System.err.println("ASR错误响应：" + errorMsg);
                        throw new RuntimeException("语音识别失败：" + errorMsg);
                    }
                }
                throw new RuntimeException("语音识别失败，HTTP状态码：" + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            System.out.println("ASR响应：" + result.toString());

            com.google.gson.JsonObject json = new com.google.gson.JsonParser().parse(result.toString()).getAsJsonObject();
            if (json.has("result")) {
                return json.get("result").getAsString();
            } else if (json.has("error")) {
                throw new RuntimeException("语音识别错误：" + json.get("error").getAsString());
            }
            return "";
        } catch (Exception e) {
            throw new RuntimeException("语音识别失败：" + e.getMessage(), e);
        }
    }

    // ========================== 语音合成（文字 → 音频）==========================
    public byte[] textToSpeech(String text) {
        try {
            if (text == null || text.isBlank()) {
                return new byte[0];
            }

            String token = getNlsToken();
            URL url = new URL("https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/tts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-NLS-Token", token);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // 国内站正确格式：直接传参，不需要 payload 嵌套
            JSONObject params = new JSONObject();
            params.put("appkey", NLS_APP_KEY);
            params.put("text", text);
            params.put("voice", "xiaoyun");
            params.put("format", "mp3");
            params.put("sample_rate", 16000);
            params.put("volume", 50);
            params.put("speed", 0);
            params.put("pitch", 0);

            String json = params.toString();
            conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        String errorMsg = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                        System.err.println("阿里云TTS错误响应：" + errorMsg);
                    }
                }
                throw new RuntimeException("语音合成接口返回状态码：" + responseCode);
            }

            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            System.err.println("语音合成异常：" + e.getMessage());
            return new byte[0];
        }
    }

}