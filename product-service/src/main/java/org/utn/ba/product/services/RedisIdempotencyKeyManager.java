package org.utn.ba.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.utn.ba.product.exceptions.IdempotencyConflictException;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisIdempotencyKeyManager implements IdempotencyKeyManager {

  private static final Duration KEY_TIMEOUT = Duration.ofMinutes(5);
  private static final String PROCESSING_VALUE = "PROCESSING";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void tryLock(String key) throws IdempotencyConflictException {
    Boolean success = redisTemplate.opsForValue().setIfAbsent(key, PROCESSING_VALUE, KEY_TIMEOUT);
    if (success == null || !success) throw new IdempotencyConflictException();
  }

  @Override
  public <T> void storeResponse(String key, T responseDto) {
    try {
      redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(responseDto), KEY_TIMEOUT);
    } catch (Exception e) {
      redisTemplate.delete(key);
      throw new RuntimeException("Error serializing response to json in redis ", e);
    }
  }

  @Override
  public <T> Optional<T> getResponse(String key, Class<T> responseClass) {
    String value = redisTemplate.opsForValue().get(key);
    if (value == null || value.equals(PROCESSING_VALUE)) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(objectMapper.readValue(value, responseClass));
    } catch (Exception e) {
      redisTemplate.delete(key);
      return Optional.empty();
    }

  }

  @Override
  public void cleanupKey(String idempotencyKey) {
    redisTemplate.delete(idempotencyKey);
  }
}

