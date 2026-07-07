package com.hobo.llm.llmentor.controller;

import com.hobo.llm.llmentor.model.ChatStatus;
import com.hobo.llm.llmentor.model.OrderChat;
import com.hobo.llm.llmentor.tools.OrderTool;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/pdd/refund")
@RequiredArgsConstructor
public class PddRefundController {

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @Autowired
    private OrderTool orderTool;

    /**
     * 前端将 userId 和 orderId 传入，根据 id 信息生成一个对话，并将订单信息和对话信息保存到记忆中
     * @param userId
     * @param orderId
     * @param response
     * @return
     */
    @RequestMapping("/newChat")
    public OrderChat newChat(String userId, String orderId, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        // 生成一个 chatId，后续对话中延用
        // 模拟数据库创建一个 chat 的记录，获取它的唯一 id
        String chatId = UUID.randomUUID().toString();

        return chatClient.prompt()
                .user(String.format("我要咨询订单相关的售后问题，我的用户id是%s，订单号是%s，本地对话id是%s，当前状态是%s", userId, orderId, chatId, ChatStatus.CHAT_START))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param("chat_memory_retrieve_size", 100))
                .call().entity(OrderChat.class);
    }

    @RequestMapping("/chat")
    public Flux<String> chat(String question, String chatId, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt()
                .user(question).tools(orderTool)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId).param("chat_memory_retrieve_size", 100))
                .stream().content();
    }

    @Value("classpath:prompts/pdd_refund_system_prompt.st")
    private Resource systemPrompt;

    @PostConstruct
    public void init() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(systemPrompt)
                .build();
    }
}
