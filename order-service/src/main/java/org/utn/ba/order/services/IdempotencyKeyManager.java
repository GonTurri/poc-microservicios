package org.utn.ba.order.services;

import org.utn.ba.order.exceptions.IdempotencyConflictException;
import java.util.Optional;

public interface IdempotencyKeyManager {
  void tryLock(String key) throws IdempotencyConflictException;

  <T> void storeResponse(String key, T responseDto);

  <T> Optional<T> getResponse(String key, Class<T> responseClass);

  void cleanupKey(String idempotencyKey);
}
