package com.example.spring_ai_tutorial.domain.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatRequestDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDeserializeWithAllFields() throws Exception {
        // Given
        String json = "{\"query\":\"Hello, how are you?\",\"model\":\"gpt-4\"}";

        // When
        ChatRequestDto dto = objectMapper.readValue(json, ChatRequestDto.class);

        // Then
        assertEquals("Hello, how are you?", dto.getQuery());
        assertEquals("gpt-4", dto.getModel());
    }

    @Test
    void testDeserializeWithDefaultModel() throws Exception {
        // Given
        String json = "{\"query\":\"Hello, how are you?\"}";

        // When
        ChatRequestDto dto = objectMapper.readValue(json, ChatRequestDto.class);

        // Then
        assertEquals("Hello, how are you?", dto.getQuery());
        assertEquals("gpt-3.5-turbo", dto.getModel());
    }

    @Test
    void testDeserializeWithNullModel() throws Exception {
        // Given
        String json = "{\"query\":\"Hello, how are you?\",\"model\":null}";

        // When
        ChatRequestDto dto = objectMapper.readValue(json, ChatRequestDto.class);

        // Then
        assertEquals("Hello, how are you?", dto.getQuery());
        assertEquals("gpt-3.5-turbo", dto.getModel());
    }
}