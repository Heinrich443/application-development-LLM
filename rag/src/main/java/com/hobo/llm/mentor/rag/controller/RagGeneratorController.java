package com.hobo.llm.mentor.rag.controller;

import com.hobo.llm.mentor.rag.generator.SqlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag/generator")
public class RagGeneratorController {

    @Autowired
    private SqlQueryService sqlQueryService;

    @GetMapping("/sql")
    public String sql(String query) {
        return sqlQueryService.text2sql(query);
    }
}
