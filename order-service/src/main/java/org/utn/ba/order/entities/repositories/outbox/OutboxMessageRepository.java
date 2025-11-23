package org.utn.ba.order.entities.repositories.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.utn.ba.order.entities.models.outbox.OutboxMessage;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage,Long> {
}
