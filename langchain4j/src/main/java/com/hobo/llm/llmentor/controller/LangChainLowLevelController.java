package com.hobo.llm.llmentor.controller;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
}
