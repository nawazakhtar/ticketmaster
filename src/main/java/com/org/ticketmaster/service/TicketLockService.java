package com.org.ticketmaster.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketLockService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final String KEY_PREFIX = "ticket:lock:";

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(Long ticketId, Long userId) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key(ticketId), String.valueOf(userId), LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public boolean isLocked(Long ticketId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(ticketId)));
    }

    public Optional<Long> getLockOwner(Long ticketId) {
        String value = redisTemplate.opsForValue().get(key(ticketId));
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    public void releaseLock(Long ticketId) {
        redisTemplate.delete(key(ticketId));
    }

    private String key(Long ticketId) {
        return KEY_PREFIX + ticketId;
    }
}