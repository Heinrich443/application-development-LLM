package com.hobo.llm.mentor.rag.reader;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class DocumentReaderFactory {

    @Autowired
    private List<DocumentReaderStrategy> strategies;

    public List<Document> read(File file) throws IOException {
        for (DocumentReaderStrategy strategy : strategies) {
            if (strategy.supports(file)) {
                return strategy.read(file);
            }
        }
        throw new IllegalArgumentException("Not supported file type: " + file.getName());
    }
}
