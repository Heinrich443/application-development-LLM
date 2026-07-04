package com.hobo.llm.llmentor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientCaller {

    private static final String API_KEY = "sk-f315527b8b6442deb0336ed4026056f3";
    private static final String API_URL = "https://llm-hheyvlcyn40cc7dk.cn-beijing.maas.aliyuncs.com/compatible-mode/v1/chat/completions";

    public static void main(String[] args) {
        String requestBody = """
                {
                    "model": "qwen3.7-max",
                    "messages": [
                        {
                            "role": "system",
                            "content": "You are a helpful assistant."
                        },
                        {
                            "role": "user",
                            "content": "你是谁？"
                        }
                    ],
                    "stream": false
                }
                """;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());

        } catch (IOException e) {
            System.err.println("Request failed: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Request interrupted");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}