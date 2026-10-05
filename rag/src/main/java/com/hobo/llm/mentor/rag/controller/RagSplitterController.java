package com.hobo.llm.mentor.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.hobo.llm.mentor.rag.cleaner.DocumentCleaner;
import com.hobo.llm.mentor.rag.reader.DocumentReaderFactory;
import com.hobo.llm.mentor.rag.splitter.OverlapParagraphTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagSplitterController {
    @Autowired
    private DocumentReaderFactory documentReaderFactory;

    @RequestMapping("/split")
    public String split(String filePath) {
        File file = new File(filePath);
        List<Document> documents;
        try {
            documents = documentReaderFactory.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Document document : documents) {
            System.out.println("before chunk: " + document.getText());
            /*TokenTextSplitter splitter = new TokenTextSplitter(
                    // 每块最多 600 tokens
                    600,
                    // 每块至少 400 字符再考虑断点
                    300,
                    // 太短的不做嵌入
                    5,
                    // 最多拆分8000块
                    8000,
                    // 保留句号、换行符
                    true
            );*/
            OverlapParagraphTextSplitter splitter = new OverlapParagraphTextSplitter(100, 5);

            List<Document> chunks = splitter.split(document);
            System.out.println("after chunk: ");
            for (Document chunk: chunks) {
                System.out.println(chunk.getText());
            }
            System.out.println("==================");
        }

        return "success";
    }

    @RequestMapping("/splitRecursive")
    public String splitRecursive(String filePath) {
        File file = new File(filePath);
        List<Document> documents;
        try {
            documents = documentReaderFactory.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Document document : documents) {
            System.out.println("before chunk: " + document.getText());
            RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(500, new String[]{"\n\n", "\n"});

            List<Document> chunks = splitter.split(document);
            System.out.println("after chunk: ");
            for (Document chunk: chunks) {
                System.out.println(chunk.getText());
            }
            System.out.println("==================");
        }

        return "success";
    }
}
