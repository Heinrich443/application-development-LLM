package com.hobo.llm.llmentor.controller;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;

@RestController
@RequestMapping("/langchain")
public class LangChainLowLevelController {

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private OpenAiStreamingChatModel streamChatModel;

    @RequestMapping("/call")
    public String call(String message) {
        return chatModel.chat(message);
    }

    @RequestMapping("/stream")
    public void stream(String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        streamChatModel.chat(message, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.println(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                System.out.println("=====end====");
            }

            @Override
            public void onError(Throwable error) {
                System.out.println(error.toString());
            }
        });
    }

    @RequestMapping("/stream1")
    public Flux<String> stream1(String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        Flux<String> flux = Flux.create(sink -> {
            streamChatModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    sink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            });
        });
        return flux;
    }

    @RequestMapping("/memory")
    public String memory(HttpServletResponse response) {
        List<ChatMessage> messages = new ArrayList<>();

        //第一轮对话
        messages.add(systemMessage("你是一个点餐助手"));
        messages.add(userMessage("给我点一个汉堡，两个鸡腿，一杯可乐"));
        AiMessage answer = chatModel.chat(messages).aiMessage();
        System.out.println(answer);
        System.out.println("======");

        messages.add(answer);

        //第二轮对话
        messages.add(userMessage("刚才菜点多了，去掉一个鸡腿，再加一杯可乐吧?"));
        AiMessage answer1 = chatModel.chat(messages).aiMessage();
        System.out.println(answer1);
        System.out.println("======");

        messages.add(answer1);

        //第三轮对话
        messages.add(userMessage("我现在总共点了哪些东西？"));
        AiMessage answer2 = chatModel.chat(messages).aiMessage();
        System.out.println(answer2);
        System.out.println("======");

        return answer2.text();
    }

    @RequestMapping("/memory1")
    public String memory1(HttpServletResponse response) {
        ChatMemory messages = MessageWindowChatMemory.withMaxMessages(10);

        //第一轮对话
        messages.add(systemMessage("你是一个点餐助手"));
        messages.add(userMessage("给我点一个汉堡，两个鸡腿，一杯可乐"));
        AiMessage answer = chatModel.chat(messages.messages()).aiMessage();
        System.out.println(answer);
        System.out.println("======");

        messages.add(answer);

        //第二轮对话
        messages.add(userMessage("刚才菜点多了，去掉一个鸡腿，再加一杯可乐吧?"));
        AiMessage answer1 = chatModel.chat(messages.messages()).aiMessage();
        System.out.println(answer1);
        System.out.println("======");

        messages.add(answer1);

        //第三轮对话
        messages.add(userMessage("我现在总共点了哪些东西？"));
        AiMessage answer2 = chatModel.chat(messages.messages()).aiMessage();
        System.out.println(answer2);
        System.out.println("======");

        return answer2.text();
    }

    @RequestMapping("/structure")
    public String structure() {

        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("Person")
                        .rootElement(JsonObjectSchema.builder()
                                .addStringProperty("name")
                                .addIntegerProperty("age")
                                .addNumberProperty("height")
                                .addBooleanProperty("married")
                                .required("name", "age", "height", "married")
                                .build())
                        .build())
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(responseFormat)
                .messages(UserMessage.from("""
                        John is 42 years old and lives an independent life.
                        He stands 1.75 meters tall and carries himself with confidence.
                        Currently unmarried, he enjoys the freedom to focus on his personal goals and interests.
                        """))
                .build();

//        ChatRequest chatRequest = ChatRequest.builder()
//                .responseFormat(responseFormat)
//                .messages(UserMessage.from("""
//                        John is 42 years old and lives an independent life.
//                        He stands 1.75 meters tall and carries himself with confidence.
//                        Currently unmarried, he enjoys the freedom to focus on his personal goals and interests.output in json format
//                        """))
//                .build();

        return chatModel.chat(chatRequest).aiMessage().text();
    }
}
