package com.scenebackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// 单例模式，确保全局只有一个ObjectMapper实例
public class ObjectMapperSingleton {
    private static final ObjectMapper INSTANCE = new ObjectMapper();

    static {
        INSTANCE.registerModule(new JavaTimeModule());
        INSTANCE.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }
}
