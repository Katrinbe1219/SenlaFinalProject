package org.example.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

//TODO in xml register
@Entity
@Table(name = "favourites")
@Setter
@Getter
public class Favourite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "good_id")
    private Good good;

    @Column(name = "created_at")
    private Instant createdAt;
}
