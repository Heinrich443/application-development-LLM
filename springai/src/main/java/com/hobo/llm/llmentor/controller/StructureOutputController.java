package com.hobo.llm.llmentor.controller;

import com.hobo.llm.llmentor.model.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/structure")
public class StructureOutputController implements InitializingBean {

    @Autowired
    @Qualifier("dashScopeChatModel")
    private ChatModel chatModel;

    private ChatClient chatClient;

    @GetMapping("/call")
    public String call(String message) {
        PromptTemplate template = PromptTemplate.builder().template("请给我推荐几本python有关的书，输出格式：{format}").build();
        BeanOutputConverter<Book> converter = new BeanOutputConverter<>(Book.class);
        String receive = chatClient.prompt(template.create(Map.of("format", converter.getFormat()))).call().content();
        Book book = converter.convert(receive);
        System.out.println(book.toString());
        return book.title() + " " + book.author() + " " + book.desc() + " " + book.price() + " " + book.publisher();
    }

    @GetMapping("/convert")
    public String convert(String message) {
        Book book = chatClient.prompt("请给我推荐几本python有关的书").call().entity(Book.class);
        System.out.println(book.toString());
        return book.title() + "-" + book.author() + "-" + book.desc() + "-" + book.price() + "-" + book.publisher();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
