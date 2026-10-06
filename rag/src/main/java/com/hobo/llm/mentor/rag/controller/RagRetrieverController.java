package com.hobo.llm.mentor.rag.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hobo.llm.mentor.rag.embedding.EmbeddingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag/retriever")
public class RagRetrieverController implements InitializingBean {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private PgVectorStore vectorStore;

    @GetMapping("/query")
    public String query(String query, double threshold) {
        List<Document> documents = embeddingService.similaritySearch(SearchRequest.builder()
                        .query(query).similarityThreshold(threshold).build());

        for (Document document : documents) {
            System.out.println(document.getText());
            System.out.println("==============");
        }

        return "success";
    }

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @GetMapping("/retrieve")
    public String retrieve(String query, double threshold) {
        List<Document> documents = embeddingService.similaritySearch(SearchRequest.builder()
                        .query(query).similarityThreshold(threshold).build());

        String documentContent = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n=========文档分隔线===========\n\n"));

        // 2. 构建提示词模板
        String promptTemplate = """
                请基于以下提供的参考文档内容，回答用户的问题。
                如果参考文档中没有相关信息，请直接说明"没有找到相关信息"，不要编造内容。
                
                参考文档:
                {documents}
                
                用户问题: {question}
                """;

        PromptTemplate prompt = new PromptTemplate(promptTemplate);
        Prompt realPrompt = prompt.create(Map.of("documents", documentContent, "question", query));
        return chatModel.call(realPrompt).getResult().getOutput().getText();
    }

    @GetMapping("/retrieveAdvisor")
    public String retrieveAdvisor(String query) {
        return chatClient.prompt(query).call().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 构建提示词模板
        PromptTemplate promptTemplate = new PromptTemplate("""
                请基于以下提供的参考文档内容，回答用户的问题。
                如果参考文档中没有相关信息，请直接说明"没有找到相关信息"，不要编造内容。
                
                参考文档:
                {question_answer_context}
                
                用户问题: {query}
                """);

        SearchRequest searchRequest = SearchRequest.builder().similarityThreshold(0.5).topK(5).build();

        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(searchRequest).promptTemplate(promptTemplate).build();

        chatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(advisor)
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .temperature(0.7)
                                .build()
                )
                .build();
    }
}
