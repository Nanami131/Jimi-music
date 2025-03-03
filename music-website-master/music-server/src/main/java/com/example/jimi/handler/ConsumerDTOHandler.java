package com.example.jimi.handler;

import com.example.jimi.model.domain.ConsumerDTO;

public class ConsumerDTOHandler {
    // 借用 ThreadLocal 存储用户信息
    private static final ThreadLocal<ConsumerDTO> CONSUMER_DTO_THREAD_LOCAL =new ThreadLocal<>();

    public static ConsumerDTO getConsumerInfo(){
        return CONSUMER_DTO_THREAD_LOCAL.get();
    }

    public static void setConsumerInfo(ConsumerDTO consumerDTO){
        CONSUMER_DTO_THREAD_LOCAL.set(consumerDTO);
    }

    public static void removeConsumerInfo(){
        CONSUMER_DTO_THREAD_LOCAL.remove();
    }
}
