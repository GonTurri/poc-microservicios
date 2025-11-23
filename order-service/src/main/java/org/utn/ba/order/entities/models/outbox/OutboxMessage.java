package org.utn.ba.order.entities.models.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class OutboxMessage {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Lob
  @Column(name = "payload", nullable = false)
  private String payload;

  @Column(name = "creation_time", nullable = false)
  private LocalDateTime creationTime;

  @Column(name = "processed", nullable = false)
  private Boolean processed = false;

}
