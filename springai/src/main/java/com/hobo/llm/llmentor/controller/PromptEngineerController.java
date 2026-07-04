package com.hobo.llm.llmentor.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/prompt/engineer")
public class PromptEngineerController implements InitializingBean {

    @Autowired
    @Qualifier("dashScopeChatModel")
    private ChatModel chatModel;

    private ChatClient chatClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                // 预设角色
                // .defaultSystem("你是一个毒舌博主，说话很噎人，请根据用户问题，怼他")
                .defaultAdvisors()
                .defaultOptions(ChatOptions.builder()
                                .model("deepseek-v4-flash")
                        // .model("glm-5.1")
                        // .model("qwen3.7-max")
                        .build())
                .build();
    }

    @GetMapping("/role")
    public String role(String message) {
        return chatClient.prompt(message).call().content();
    }

    @GetMapping("/shot")
    public String shot(String message) {
        return chatClient.prompt().system("""
                请根据用户输入的数字，给出结果，不需要思考过程，直接给出数字结果即可，推理过程参考：
                1 = 5
                2 = 10
                3 = 15
                ，如果用户给的不是个数字，请回复:无法回答，请输入数字
                """).user(message).call().content();
    }

    @GetMapping("/structure")
    public String structureOutput(String message) {
        return chatClient.prompt("请你以json串的格式输出内容").system("你是个有用的助手").user(message).call().content();
    }

    @GetMapping("/step")
    public Flux<String> step(@RequestParam(value = "message") String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt("""
                    执行以下操作：
                        1-用一句话概括下面文本。
                        2-将摘要翻译成英语。
                        3-在英语摘要中列出每个人名。
                        4-输出一个 JSON 对象，其中包含以下键：english_summary，num_names。
                
                        请用换行符分隔您的答案。
                """).system("你是个ai").user(message).stream().content();
    }

    @GetMapping("/step2")
    public Flux<String> step2(@RequestParam(value = "message") String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt("""
                    请输出json格式的故事概要和人名数量，请按照以下思考方式逐步进行，最终只输出json即可：
                        step 1-用一句话概括下面文本。
                        step 2-将摘要翻译成英语。
                        step 3-在英语摘要中列出每个人名。
                        step 4-输出一个 JSON 对象，其中包含以下键：english_summary，num_names。
                        最终输出：{"english_summary": "故事概要", "num_names": 3}
                """).system("你是个ai").user(message).stream().content();
    }

    @GetMapping("/cot")
    public Flux<String> cot(@RequestParam(value = "message") String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt("""
                    一个水果摊有5箱苹果，每箱重15公斤。今天卖掉了35公斤，还剩下多少公斤苹果？
                
                                    请一步一步思考，并给出最终答案。
                """).system("你是个ai").user(message).stream().content();
    }
}
