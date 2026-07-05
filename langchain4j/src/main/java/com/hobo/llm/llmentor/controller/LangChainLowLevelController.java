package com.hobo.llm.llmentor.controller;

import dev.langchain4j.data.message.UserMessage;
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

//    @RequestMapping("/stream")
//    public Flux<String> stream(String message, HttpServletResponse response) {
//        response.setCharacterEncoding("UTF-8");
//        return streamChatModel.chat();
//    }
}
