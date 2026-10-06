package com.hobo.llm.mentor.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.hobo.llm.mentor.rag.cleaner.DocumentCleaner;
import com.hobo.llm.mentor.rag.embedding.EmbeddingService;
import com.hobo.llm.mentor.rag.reader.DocumentReaderFactory;
import com.hobo.llm.mentor.rag.splitter.OverlapParagraphTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag/embedding")
public class EmbeddingController {

    @Autowired
    private EmbeddingModel embeddingModel;

    @GetMapping("/test")
    public String test() {
        for (float f: embeddingModel.embed("test")) {
            System.out.println(f);
        }
        return "success";
    }

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private DocumentReaderFactory readerFactory;

    @GetMapping("/embed")
    public String embed(String filePath) {
        List<Document> documents;

        try {
            documents = readerFactory.read(new File(filePath));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        List<Document> allChunkedDocuments = documents.stream()
                .flatMap(document -> {
                    RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(300, new String[]{"\n\n", "\n"});
                    return splitter.split(document).stream();
                })
                .collect(Collectors.toList());

        embeddingService.embedAndStore(allChunkedDocuments);

        return "success";
    }

    @GetMapping("/embed1")
    public String embed1(String filePath) {
        List<Document> documents;

        try {
            documents = readerFactory.read(new File(filePath));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        // 清洗并分段
        List<Document> allChunkedDocuments = DocumentCleaner.cleanDocuments(documents).stream()
                .flatMap(document -> {
                    OverlapParagraphTextSplitter splitter = new OverlapParagraphTextSplitter(1000, 50);
                    return splitter.split(document).stream();
                })
                .collect(Collectors.toList());

        // 向量化存储
        embeddingService.embedAndStore(DocumentCleaner.cleanDocuments(allChunkedDocuments));

        return "success";
    }
}
