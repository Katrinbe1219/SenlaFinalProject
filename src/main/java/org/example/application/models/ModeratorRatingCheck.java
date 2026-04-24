package org.example.application.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.application.models.converters.ModeratorVerdictConverter;
import org.example.application.models.types.ModeratorVerdict;

import java.time.Instant;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "moderator_rating_checks")
public class ModeratorRatingCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "good_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Good good;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @Convert(converter = ModeratorVerdictConverter.class)
    private ModeratorVerdict verdict;
    private String comment;
    @Column(name = "checked_at")
    private Instant checkAt;
}
