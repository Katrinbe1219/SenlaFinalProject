package org.example.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "reviews")
@NoArgsConstructor
public class Review {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reviews_seq")
//    @SequenceGenerator(name="reviews_seq", sequenceName = "reviews_id_seq",
//            allocationSize = 75)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name="good_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Good good;

    @JoinColumn(name="user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String review;
    private Integer rate;
    @Column(name="created_at")
    private Instant createdAt;
    private Boolean blocked;
    @Column(name = "blocked_at")
    private Instant blockedAt;

    @JoinColumn(name="blocked_by")
    @ManyToOne(fetch = FetchType.LAZY)
    private User blockedBy;


}
