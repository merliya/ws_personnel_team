package com.jbhunt.personnel.team.converter;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateTimeConverter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) {
        return LocalDateTime.parse(jsonElement.getAsString(), ISO_DATE_TIME);
    }

    @Override
    public JsonElement serialize(LocalDateTime zonedDateTime, Type type,
            JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(zonedDateTime.format(ISO_DATE_TIME));
    }
}
