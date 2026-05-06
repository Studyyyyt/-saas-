package com.example.springboot.service;

import com.example.springboot.entity.PortalAccessToken;
import com.example.springboot.mapper.PortalAccessTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PortalAccessTokenService {

    private final PortalAccessTokenMapper mapper;
    private final Map<String, PortalAccessToken> inMemoryStore = new ConcurrentHashMap<>();

    @Autowired
    public PortalAccessTokenService(PortalAccessTokenMapper mapper) {
        this.mapper = mapper;
    }

    public PortalAccessTokenService() {
        this.mapper = null;
    }

    public String issueToken(String tokenType, Long subjectId, String payload, Duration ttl) {
        String token = UUID.randomUUID().toString().replace("-", "");
        PortalAccessToken record = new PortalAccessToken();
        record.setToken(token);
        record.setToken_type(tokenType);
        record.setSubject_id(subjectId);
        record.setPayload(payload);
        record.setExpires_at(Date.from(Instant.now().plus(ttl == null ? Duration.ofMinutes(15) : ttl)));
        if (mapper == null) {
            inMemoryStore.put(composeKey(tokenType, token), record);
        } else {
            mapper.insert(record);
        }
        return token;
    }

    public PortalAccessToken resolveActiveToken(String tokenType, String token) {
        if (!StringUtils.hasText(tokenType) || !StringUtils.hasText(token)) {
            return null;
        }
        if (mapper == null) {
            PortalAccessToken record = inMemoryStore.get(composeKey(tokenType, token));
            if (record == null) {
                return null;
            }
            if (record.getConsumed_at() != null) {
                return null;
            }
            if (record.getExpires_at() == null || !record.getExpires_at().after(new Date())) {
                inMemoryStore.remove(composeKey(tokenType, token));
                return null;
            }
            return record;
        }
        return mapper.selectActive(token, tokenType);
    }

    public PortalAccessToken consumeToken(String tokenType, String token) {
        PortalAccessToken record = resolveActiveToken(tokenType, token);
        if (record == null) {
            return null;
        }
        if (mapper == null) {
            record.setConsumed_at(new Date());
            inMemoryStore.remove(composeKey(tokenType, token));
        } else {
            mapper.consume(record.getId());
        }
        return record;
    }

    private String composeKey(String tokenType, String token) {
        return tokenType + "::" + token;
    }
}
