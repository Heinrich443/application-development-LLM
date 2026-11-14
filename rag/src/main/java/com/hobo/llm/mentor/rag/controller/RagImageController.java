package com.hobo.llm.mentor.rag.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hobo.llm.mentor.rag.reader.PdfMultimodalProcessor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/rag/image")
public class RagImageController {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Autowired
    private ChatModel chatModel;

    @RequestMapping("/callWithOpenAI")
    public String callWithOpenAI() throws URISyntaxException, MalformedURLException {

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .temperature(0.2d)
                .model("qwen3.8-omni-flash")
                .outputModalities(List.of("text"))
                .build();
        OpenAiChatModel multimodalChatModel = OpenAiChatModel.builder().openAiApi(OpenAiApi.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/")
                .apiKey(new SimpleApiKey(apiKey)).build()).defaultOptions(options).build();

        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_PNG, new URI("https://cdn.nlark.com/yuque/0/2025/png/5378072/1762350625634-664f1db7-e1c9-4daa-ab8e-81b6b7da5a68.png").toURL().toURI()));

        var userMessage = UserMessage.builder().text("请非常简要的描述一下你看到的这个图片?").media(mediaList).build();
        var response = multimodalChatModel.call(new Prompt(List.of(userMessage)));

        return response.getResult().getOutput().getText();
    }

    @RequestMapping("/call")
    public String call() throws URISyntaxException, MalformedURLException {

        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_PNG, new URI("https://cdn.nlark.com/yuque/0/2025/png/5378072/1762350625634-664f1db7-e1c9-4daa-ab8e-81b6b7da5a68.png").toURL().toURI()));

        var userMessage = UserMessage.builder().text("请非常简要的描述一下你看到的这个图片?")
                .media(mediaList).build();
        // return chatModel.call(new Prompt(List.of(userMessage), ChatOptions.builder().model("qwen3.8-27b").build())).getResult().getOutput().getText();
        return chatModel.call(new Prompt(List.of(userMessage), DashScopeChatOptions.builder().withModel("qwen3.8-27b").withMultiModel(true).build())).getResult().getOutput().getText();
    }

    @Autowired
    private PdfMultimodalProcessor processor;

    @GetMapping("/process")
    public String process(String filePath) {
        try {
            return processor.processPdf(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}
