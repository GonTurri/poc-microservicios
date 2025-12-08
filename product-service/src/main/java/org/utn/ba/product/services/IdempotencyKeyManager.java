package org.utn.ba.product.services;

import org.utn.ba.product.exceptions.IdempotencyConflictException;
import java.util.Optional;

public interface IdempotencyKeyManager {
  void tryLock(String key) throws IdempotencyConflictException;

  <T> void storeResponse(String key, T responseDto);

  <T> Optional<T> getResponse(String key, Class<T> responseClass);

  void cleanupKey(String idempotencyKey);
}
