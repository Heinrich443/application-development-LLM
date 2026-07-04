package com.hobo.llm.llmentor.converter;

import org.springframework.ai.converter.MapOutputConverter;

import java.util.Map;

public class BookMapOutputConverter<T> extends MapOutputConverter {
    private T t;

    public BookMapOutputConverter(T t) {
        this.t = t;
    }

    @Override
    public String getFormat() {
        String raw = """
                Your response should be in JSON format.
                The data structure for the JSON should match this Java class: %s
                the value in map shoud match this java class: %s
                Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
                Remove the ```json markdown surrounding the output including the trailing "```".
                """;

        return String.format(raw, Map.class, t.getClass());
    }
}