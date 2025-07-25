package com.fbc.ai.service.meta;

import com.fbc.ai.domain.dto.ApiResponseMetaDto;
import com.fbc.ai.service.ApiMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OpenAI API 메타데이터 추출 서비스 구현체
 *
 * ChatResponse에서 메타데이터를 추출하여 ApiResponseMetaDto 객체로 변환합니다.
 */
@Slf4j
@Service
public class ApiMetaServiceImpl implements ApiMetaService {

    /**
     * ChatResponse에서 메타데이터를 추출하여 ApiResponseMetaDto 객체로 반환합니다.
     *
     * @param response ChatResponse 객체
     * @param model 사용된 모델명 (메타데이터에서 추출할 수 없는 경우 사용)
     * @return 메타데이터 객체, 추출 실패 시 기본값 반환
     */
    public ApiResponseMetaDto extractMetadata(ChatResponse response, String model) {
        if (response == null) {
            return new ApiResponseMetaDto(0, 0, 0, model);
        }

        try {
            // Spring AI의 ChatResponse에서 메타데이터 추출 시도
            ChatResponseMetadata metadata = response.getMetadata();

            // 메타데이터에서 토큰 사용량 정보 추출 시도
            Integer promptTokens = 0;
            Integer completionTokens = 0;
            Integer totalTokens = 0;

            // 리플렉션을 사용하여 메타데이터에서 usage 정보 추출 시도
            try {
                Object usageObj = getFieldValue(metadata, "usage");
                if (usageObj != null) {
                    log.debug("메타데이터 usage 객체 타입: {}", usageObj.getClass().getName());
                    if (usageObj instanceof Map) {
                        Map<String, Object> usage = (Map<String, Object>) usageObj;
                        promptTokens = getIntValue(usage, "prompt_tokens");
                        completionTokens = getIntValue(usage, "completion_tokens");
                        totalTokens = getIntValue(usage, "total_tokens");
                        log.debug("메타데이터에서 토큰 사용량 추출 성공: prompt={}, completion={}, total={}", 
                                promptTokens, completionTokens, totalTokens);
                    } else {
                        log.debug("메타데이터 usage 객체가 Map이 아님: {}", usageObj);
                        // Map이 아닌 경우 리플렉션을 사용하여 필드에서 직접 값을 추출
                        promptTokens = getIntValueFromObject(usageObj, "promptTokens", "prompt_tokens");
                        completionTokens = getIntValueFromObject(usageObj, "completionTokens", "completion_tokens");
                        totalTokens = getIntValueFromObject(usageObj, "totalTokens", "total_tokens");
                        log.debug("메타데이터에서 리플렉션으로 토큰 사용량 추출: prompt={}, completion={}, total={}", 
                                promptTokens, completionTokens, totalTokens);
                    }
                }
            } catch (Exception e) {
                log.debug("메타데이터에서 usage 정보 추출 실패: {}", e.getMessage());
            }

            // 직접 ChatResponse에서 usage 정보 추출 시도
            if (totalTokens == 0) {
                try {
                    Object usageObj = getFieldValue(response, "usage");
                    if (usageObj != null) {
                        log.debug("응답 usage 객체 타입: {}", usageObj.getClass().getName());
                        if (usageObj instanceof Map) {
                            Map<String, Object> usage = (Map<String, Object>) usageObj;
                            promptTokens = getIntValue(usage, "prompt_tokens");
                            completionTokens = getIntValue(usage, "completion_tokens");
                            totalTokens = getIntValue(usage, "total_tokens");
                            log.debug("응답에서 직접 토큰 사용량 추출 성공: prompt={}, completion={}, total={}", 
                                    promptTokens, completionTokens, totalTokens);
                        } else {
                            log.debug("응답 usage 객체가 Map이 아님: {}", usageObj);
                            // Map이 아닌 경우 리플렉션을 사용하여 필드에서 직접 값을 추출
                            promptTokens = getIntValueFromObject(usageObj, "promptTokens", "prompt_tokens");
                            completionTokens = getIntValueFromObject(usageObj, "completionTokens", "completion_tokens");
                            totalTokens = getIntValueFromObject(usageObj, "totalTokens", "total_tokens");
                            log.debug("응답에서 리플렉션으로 토큰 사용량 추출: prompt={}, completion={}, total={}", 
                                    promptTokens, completionTokens, totalTokens);
                        }
                    }
                } catch (Exception e) {
                    log.debug("응답에서 직접 usage 정보 추출 실패: {}", e.getMessage());
                }
            }

            return new ApiResponseMetaDto(promptTokens, completionTokens, totalTokens, model);
        } catch (Exception e) {
            log.warn("메타데이터 추출 중 오류 발생: {}", e.getMessage());
            return new ApiResponseMetaDto(0, 0, 0, model);
        }
    }

    /**
     * Map에서 정수 값을 안전하게 추출합니다.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 리플렉션을 사용하여 객체의 필드 값을 가져옵니다.
     */
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (Exception e) {
            log.debug("필드 접근 중 오류: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 클래스 계층 구조에서 필드를 찾습니다.
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> searchType = clazz;
        while (searchType != null && !Object.class.equals(searchType)) {
            for (java.lang.reflect.Field field : searchType.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * 객체에서 정수 값을 안전하게 추출합니다.
     * 여러 필드 이름을 시도하여 값을 찾습니다.
     */
    private Integer getIntValueFromObject(Object obj, String... fieldNames) {
        if (obj == null) {
            return 0;
        }

        for (String fieldName : fieldNames) {
            try {
                Object value = getFieldValue(obj, fieldName);
                if (value != null) {
                    if (value instanceof Integer) {
                        return (Integer) value;
                    } else if (value instanceof Number) {
                        return ((Number) value).intValue();
                    } else if (value instanceof String) {
                        try {
                            return Integer.parseInt((String) value);
                        } catch (NumberFormatException e) {
                            // 다음 필드 시도
                        }
                    }
                }
            } catch (Exception e) {
                // 다음 필드 시도
            }
        }

        return 0;
    }
}
