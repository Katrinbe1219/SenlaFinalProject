package org.example.core.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_subscriptions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                        generator = "price_subscriptions_id_seq_gen")
    @SequenceGenerator(
            name = "price_subscriptions_id_seq_gen",
            sequenceName = "price_subscriptions_id_seq",
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
    @NotNull
    private Shop shop;

    @Column(name = "created_at")
    @NotNull
    private Instant createdAt;

    @Column(name = "target_price")
    @NotNull
    private BigDecimal targetPrice;


}
