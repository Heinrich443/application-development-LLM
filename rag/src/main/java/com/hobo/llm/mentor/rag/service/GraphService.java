package com.hobo.llm.mentor.rag.service;

import com.hobo.llm.mentor.rag.model.DirectorMoviesDto;
import com.hobo.llm.mentor.rag.repo.MovieGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphService {

    @Autowired
    private MovieGraphRepository repository;

    public String retrieveContext(String movieName) {
        List<DirectorMoviesDto> results = repository.findOtherMoviesBySameDirector(movieName);

        if (results.isEmpty()) {
            return "未找到导演过《" + movieName + "》的导演的其他作品。";
        }

        StringBuilder sb = new StringBuilder();
        for (DirectorMoviesDto row : results) {
            String director = (String) row.getDirector();
            @SuppressWarnings("unchecked")
            List<String> movies = (List<String>) row.getOtherMovies();
            sb.append(String.format("- 导演 %s 还执导了：%s\n", director, String.join("、", movies)));
        }
        return sb.toString().trim();

    }

}