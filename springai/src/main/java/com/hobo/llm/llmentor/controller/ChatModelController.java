package com.hobo.llm.llmentor.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/model")
public class ChatModelController {

    @Autowired
    private DashScopeChatModel chatModel;

    @RequestMapping("/call/string")
    public String callString(String message) {
        return chatModel.call(message);
    }

    @RequestMapping("/call/messages")
    public String callMessages(String message) {
        SystemMessage sysMsg = new SystemMessage("请用英文回答问题");
        UserMessage userMsg = new UserMessage(message);
        return chatModel.call(sysMsg, userMsg);
    }

    @RequestMapping("/call/prompt")
    public String callPrompt(String message) {
        SystemMessage sysMsg = new SystemMessage("请如实回答问题");
        UserMessage userMsg = new UserMessage(message);
        ChatOptions chatOptions = ChatOptions.builder().model("deepseek-r1").build();
        Prompt prompt = Prompt.builder().messages(sysMsg, userMsg).chatOptions(chatOptions).build();
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    @RequestMapping("/stream/string")
    public Flux<String> streamString(String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatModel.stream(message);
    }
}