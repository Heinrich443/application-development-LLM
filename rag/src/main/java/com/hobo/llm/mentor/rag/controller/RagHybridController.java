package com.hobo.llm.mentor.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.hobo.llm.mentor.rag.embedding.EmbeddingService;
import com.hobo.llm.mentor.rag.es.ElasticSearchService;
import com.hobo.llm.mentor.rag.es.EsDocumentChunk;
import com.hobo.llm.mentor.rag.reader.DocumentReaderFactory;
import com.hobo.llm.mentor.rag.rerank.RerankUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rag/hybrid")
public class RagHybridController {

    @Autowired
    private DocumentReaderFactory selector;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private DocumentReaderFactory readerFactory;

    @RequestMapping("write")
    public String write(String filePath) throws Exception {
        // 1. 加载文档
        List<Document> documents = selector.read(new File(filePath));

        // 3. 文档分片
        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(
                // 每块最大字符数
                100,
                new String[]{"。"}
        );
        List<Document> apply = splitter.apply(documents);

        // 4. 存储到ES
        List<EsDocumentChunk> esDocs = apply.stream().map(doc -> {
            EsDocumentChunk es = new EsDocumentChunk();
            es.setId(doc.getId());
            es.setContent(doc.getText());
            es.setMetadata(doc.getMetadata());
            return es;
        }).toList();

        elasticSearchService.bulkIndex(esDocs);

        // 向量化存储
        embeddingService.embedAndStore(apply);

        return "success";
    }

    @RequestMapping("searchFromEs")
    public List<EsDocumentChunk> searchFromEs(String keyword) throws Exception {
        return elasticSearchService.searchByKeyword(keyword);
    }

    @RequestMapping("searchFromVector")
    public List<Document> searchFromVector(String keyword) throws Exception {
        return embeddingService.similaritySearch(keyword);
    }

    @RequestMapping("searchFromHybrid")
    public List<String> searchFromHybrid(String keyword) throws Exception {
        List<EsDocumentChunk> esDocs = elasticSearchService.searchByKeyword(keyword);
        System.out.println(esDocs);
        System.out.println("============");

        List<Document> vectorDocs = embeddingService.similaritySearch(keyword);
        System.out.println(vectorDocs);
        System.out.println("============");

//        List<String> results = RerankUtil.rrfFusion(vectorDocs, esDocs, 5);
        List<String> results = RerankUtil.rerankFusion(vectorDocs, esDocs, keyword,5);
        System.out.println(results);
        System.out.println("============");

        return results;
    }

    @Autowired
    private ChatModel chatModel;

    @RequestMapping("chatToHybrid")
    public String chatToHybrid(String keyword) throws Exception {
        List<EsDocumentChunk> esDocs = elasticSearchService.searchByKeyword(keyword);
        List<Document> vectorDocs = embeddingService.similaritySearch(keyword);

//        List<String> results = RerankUtil.rrfFusion(vectorDocs, esDocs, 5);
        List<String> results = RerankUtil.rerankFusion(vectorDocs, esDocs, keyword,5);

        String promt = """
                请根据以下文档内容，回答用户问题。
                注意：只能参考文档内容回答，不要自己推理
                文档内容：
                {contents}
                
                用户问题：
                {question}
                """;

        return chatModel.call(new PromptTemplate(promt).create(Map.of("contents", results, "question", keyword))).getResult().getOutput().getText();
    }
}
