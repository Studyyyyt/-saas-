package com.example.springboot.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class FlexibleDateDeserializer extends JsonDeserializer<Date> {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return new Date(parser.getLongValue());
        }

        String text = parser.getValueAsString();
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.matches("^\\d{13}$")) {
            return new Date(Long.parseLong(normalized));
        }
        if (normalized.matches("^\\d{10}$")) {
            return new Date(Long.parseLong(normalized) * 1000L);
        }

        Date parsed = parseAsInstant(normalized);
        if (parsed != null) {
            return parsed;
        }

        parsed = parseAsLocalDateTime(normalized);
        if (parsed != null) {
            return parsed;
        }

        parsed = parseAsLocalDate(normalized);
        if (parsed != null) {
            return parsed;
        }

        throw InvalidFormatException.from(
                parser,
                "日期格式不支持，应为 yyyy-MM-dd、yyyy-MM-dd HH:mm:ss 或 ISO-8601",
                normalized,
                Date.class
        );
    }

    private Date parseAsInstant(String value) {
        try {
            return Date.from(Instant.parse(value));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(OffsetDateTime.parse(value).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private Date parseAsLocalDateTime(String value) {
        String isoNormalized = value.replace(' ', 'T');
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoNormalized);
            return Date.from(dateTime.atZone(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return Date.from(dateTime.atZone(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_TIME_MINUTE_FORMATTER);
            return Date.from(dateTime.atZone(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private Date parseAsLocalDate(String value) {
        try {
            LocalDate localDate = LocalDate.parse(value);
            return Date.from(localDate.atStartOfDay(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
