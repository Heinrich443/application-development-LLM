package com.hobo.llm.llmentor.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/client")
public class ChatClientController implements InitializingBean {

    @Qualifier("dashScopeChatModel")
    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                ).defaultSystem("1 + 1")
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .temperature(0.7)
                                .build()
                )
                .build();
    }

    @RequestMapping("/callSimple")
    public String callSimple(String message) {
        return chatClient.prompt(message).call().content();
    }

    @GetMapping("/callOverwrite")
    public String callOverwrite(String message) {
        // 覆盖默认的 SystemMessage
        // 重新设置系统提示词
        return chatClient.prompt(message).system("加上3").call().content();
    }

    @RequestMapping("/callUser")
    public String callUser(String message) {
        return chatClient.prompt().user(message).call().content();
    }

    @RequestMapping("/stream")
    public Flux<String> stream(String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(message).stream().content();
    }

    @GetMapping("/call")
    public String call(String message) {
        // 追加 SystemMessage
        // prompt 方法中相当于对话内容，一种实现记忆的方式
        return chatClient.prompt(new Prompt(new SystemMessage("加上3"), new UserMessage(message))).call().content();
    }
}
