package org.example.notification_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "availability_subscriptions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilitySubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "availability_subscriptions_id_seq_gen")
    @SequenceGenerator(
            name = "availability_subscriptions_id_seq_gen",
            sequenceName = "availability_subscriptions_id_seq",
            allocationSize = 50
    )
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private User user;

    @JoinColumn(name="good_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private  Good good;

    @JoinColumn(name = "shop_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;

    @Column(name = "created_at")
    private Instant createdAt;
}
