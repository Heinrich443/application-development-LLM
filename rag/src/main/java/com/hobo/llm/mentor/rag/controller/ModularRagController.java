package com.hobo.llm.mentor.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag/modular")
public class ModularRagController implements InitializingBean {

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    @GetMapping("/retriever")
    public String retriever(String query, @RequestParam(defaultValue = "0.3") double threshold) {
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(5)
                .similarityThreshold(threshold)
                .build();

        RewriteQueryTransformer transformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .build();

        QueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .numberOfQueries(3)
                .includeOriginal(true)
                .build();

        QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
//                .emptyContextPromptTemplate(new PromptTemplate("请回答以下问题"))
                .build();

        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryTransformers(transformer)
                .queryExpander(queryExpander)
                .queryAugmenter(queryAugmenter)
                .build();

        return chatClient.prompt(query).advisors(advisor).call().content();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//                // 查询预处理：转换查询（可选）
//                .queryTransformers(queryTransformer)
//                // 查询预处理：扩展查询（可选）
//                .queryExpander(queryExpander)
//                // 检索阶段：从向量库检索文档（必需）
//                .documentRetriever(documentRetriever)
//                // 后处理阶段：合并文档（当使用查询扩展时推荐）
//                .documentJoiner(documentJoiner)
//                // 生成阶段：构建增强提示词（可选，有默认实现）
//                .queryAugmenter(queryAugmenter)
//                .build();

        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }
}
