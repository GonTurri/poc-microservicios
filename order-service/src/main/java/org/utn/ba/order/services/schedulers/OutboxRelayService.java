package org.utn.ba.order.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.utn.ba.order.entities.models.outbox.OutboxMessage;
import org.utn.ba.order.entities.repositories.outbox.OutboxMessageRepository;
import org.utn.ba.order.services.MessagePublisher;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayService {
  private final OutboxMessageRepository outboxMessageRepository;
  private final MessagePublisher messagePublisher;

  @Scheduled(fixedDelay = 5000)
  @SchedulerLock(name = "outboxMessageRelayLock",
      lockAtMostFor = "PT4S",
      lockAtLeastFor = "PT1S")
  @Transactional
  public void processOutboxMessages() {
    List<OutboxMessage> messages = outboxMessageRepository
        .findTop100ByProcessedFalseOrderByCreationTimeAsc();

    if (messages.isEmpty()) {
      log.info("No outbox messages to process where found");
      return;
    }

    messages.forEach(message -> {
      try {
        messagePublisher.publish(message.getTopic(),message.getKey(),message.getPayload());

        message.markProcessed();
        outboxMessageRepository.save(message);
      } catch (Exception e) {
        log.error(e.getMessage(),e);
      }
    });


  }
}
