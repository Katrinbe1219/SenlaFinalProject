package org.example.core.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "outbox_seq_gen")
    @SequenceGenerator(
            name="outbox_seq_gen",
            sequenceName = "outbox_events_id_seq",
            allocationSize = 50
    )
    private Long id;

    @NotNull
    private String topic;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String information;
}
