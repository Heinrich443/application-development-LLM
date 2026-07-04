package com.hobo.llm.llmentor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = {
//		com.alibaba.cloud.ai.autoconfigure.memory.Mem0ChatMemoryAutoConfiguration.class
//})
@SpringBootApplication
public class SpringAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiApplication.class, args);
	}

}
