package org.example.application.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prices_seq")
    @SequenceGenerator(name="prices_seq", sequenceName = "prices_id_seq",
            allocationSize = 75)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "good_id")
    private Good good;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    private BigDecimal price;

}
