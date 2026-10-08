package com.hobo.llm.mentor.rag.controller;

import com.hobo.llm.mentor.rag.router.QueryRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag/route")
public class RagRouteController {

    @Autowired
    private QueryRouteService queryRouteService;

    @GetMapping
    public String route(String query) {
        return queryRouteService.route(query);
    }
}
