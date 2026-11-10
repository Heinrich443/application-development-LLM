package com.hobo.llm.llmentor.controller;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/websearch")
public class WebSearchController {

    @Autowired
    OpenAiChatModel chatModel;

    @GetMapping("/search")
    public String webSearch() {
        // 1. 配置搜索引擎
        TavilyWebSearchEngine searchEngine = TavilyWebSearchEngine.builder()
                .apiKey("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")
                .includeAnswer(true)
                .searchDepth("advanced")
                .build();

        // 2. 配置 Web 搜索检索器
        WebSearchContentRetriever webRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(searchEngine)
                .maxResults(5)
                .build();

        // 问题富化/改写
        CompressingQueryTransformer queryTransformer = CompressingQueryTransformer.builder()
                .chatModel(chatModel)
                .build();

        // 3. 配置 RetrievalAugmentor
        DefaultRetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(webRetriever)
                .queryTransformer(queryTransformer)
                .build();

        // 5. 创建 AI Service
        interface WebSearchAssistant {
            String chat(String userMessage);
        }

        WebSearchAssistant assistant = AiServices.builder(WebSearchAssistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(augmentor)
                .build();

        // 6. 使用 - 获取实时信息
        String answer = assistant.chat("2025年人工智能领域有哪些重大突破？");
        return answer;
    }

}