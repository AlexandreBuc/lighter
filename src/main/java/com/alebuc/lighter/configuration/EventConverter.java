package com.alebuc.lighter.configuration;

import com.alebuc.lighter.entity.EventEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.Document;

import java.util.Date;
import java.util.Map;

public abstract class EventConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper().setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).registerModule(new JavaTimeModule());

    public static Document convertToDocument(EventEntity eventEntity) {
        try {
            return Document.parse(objectMapper.writeValueAsString(eventEntity));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventEntity convertToEvent(Document document) {
        EventEntity.EventEntityBuilder builder = EventEntity.builder()
                .key(document.get("key"))
                .value(document.get("value"))
                .partition(document.getInteger("partition"))
                .offset(document.getLong("offset"))
                .headers(document.get("headers", Map.class));
        Date createTime = document.getDate("createTime");
        if (createTime != null) {
            builder = builder.createTime(createTime.toInstant());
        }
        Date timestamp = document.getDate("timestamp");
        if (timestamp != null) {
            builder = builder.timestamp(timestamp.toInstant());
        }
        Date logAppendTime = document.getDate("lastUpdated");
        if (logAppendTime != null) {
            builder = builder.logAppendTime(logAppendTime.toInstant());
        }

        return builder.build();
    }
}
