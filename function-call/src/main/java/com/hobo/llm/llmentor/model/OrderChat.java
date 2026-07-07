package com.hobo.llm.llmentor.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record OrderChat(@JsonPropertyDescription("订单号") String orderId
        ,@JsonPropertyDescription("用户id") String userId
        ,@JsonPropertyDescription("对话id") String chatId
        ,@JsonPropertyDescription("对话状态") ChatStatus status) {
}

