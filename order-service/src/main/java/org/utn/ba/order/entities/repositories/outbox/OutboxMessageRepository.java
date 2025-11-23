package org.utn.ba.order.entities.repositories.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.utn.ba.order.entities.models.outbox.OutboxMessage;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage,Long> {
  List<OutboxMessage> findTop100ByProcessedFalseOrderByCreationTimeAsc();
}
