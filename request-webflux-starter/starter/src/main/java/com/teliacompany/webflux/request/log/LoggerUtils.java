package com.teliacompany.webflux.request.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teliacompany.webflux.request.utils.Constants;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.webflux.jackson.TeliaObjectMapper;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

final class LoggerUtils {
    private LoggerUtils() {
        // Util class
    }

    public static Optional<String> getContentType(HttpHeaders headers) {
        if(headers != null && headers.getContentType() != null) {
            return Optional.of(headers.getContentType().toString());
        }
        return Optional.empty();
    }


    public static String toJsonMessage(Object map, boolean flatten) {
        try {
            String message = TeliaObjectMapper.get().writeValueAsString(map);
            if(flatten) {
                return message.substring(1, message.length() - 1);
            }
            return message;
        } catch(JsonProcessingException e) {
            throw new InternalServerErrorException("Could not log request and/or response", "Internal", e);
        }
    }

    public static String toStringMessage(Map<String, Object> map) {
        Object direction = map.remove(Constants.DIRECTION);
        final StringBuilder sb = new StringBuilder()
                .append(direction).append(" message").append(Constants.CRLF)
                .append(Constants.DELIMITER).append(Constants.CRLF);
        map.forEach((key, value) -> sb.append(key).append(Constants.SEP).append(value).append(Constants.CRLF));
        return sb.append(Constants.DELIMITER).append(Constants.CRLF).toString();
    }

    public static String base64Encode(String filterPayload) {
        return Base64.getEncoder().encodeToString(filterPayload.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64EncodeHeaders(HttpHeaders httpHeaders) {
        if(httpHeaders == null || httpHeaders.isEmpty()) {
            return "";
        }
        return base64Encode(httpHeaders.entrySet().toString());
    }
}
