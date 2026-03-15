package top.xym.community.app.common.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.dashscope.chat.options")
public class OptionConfig {
    private String  model;
    private Double temperature;
    private Integer seed;
    private Double topP;
    private Integer topK;
    private Integer maxTokens;
    private Double repetitionPenalty;
    private Boolean stream;
    private Boolean incrementalOutput;
    private Boolean enableSearch;
    private Boolean enableThinking;
    private Boolean multiModel;
    private DashScopeResponseFormat responseFormat;
    private List<DashScopeApiSpec.FunctionTool> tools;
    private Object toolChoice;

    @Bean
    public DashScopeChatOptions dashScopeChatOptions() {
        return DashScopeChatOptions.builder()
                .withModel(model)
                .withTemperature(temperature)
                .withSeed(seed)
                .withTopP(topP)
                .withTopK(topK)
                .withMaxToken(maxTokens)
                .withRepetitionPenalty(repetitionPenalty)
                .withStream(stream)
                .withIncrementalOutput(incrementalOutput)
                .withEnableSearch(enableSearch)
                .withEnableThinking(enableThinking)
                .withMultiModel(multiModel)
                .withResponseFormat(responseFormat)
                .withTools(tools)
                .withToolChoice(toolChoice)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(dashScopeChatOptions())
                .build();
    }

}
