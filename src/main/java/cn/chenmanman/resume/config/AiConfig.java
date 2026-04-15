package cn.chenmanman.resume.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient resumeChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一个专业的中文简历优化助手，名字叫陈慢慢。")
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel("qwen-flash")
                                .withTemperature(0.4)
                                .withTopP(0.8)
                                .build()
                )
                .build();
    }
}