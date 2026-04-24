package org.example.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.core.models.converters.RatingStatusConverter;
import org.example.core.models.converters.RatingTriggerTypeConverter;
import org.example.core.models.types.RatingTriggerType;
import org.example.core.models.types.RatingStatus;

import java.time.Instant;
import java.time.LocalDate;

@Table(name = "rating_recalculation_log")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RatingRecalculationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rating_recalculation_log_seq")
    @SequenceGenerator(name="rating_recalculation_log_seq", sequenceName = "rating_recalculation_log_id_seq",
            allocationSize = 75)
    private Long id;

    @JoinColumn(name="good_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Good good;

    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "old_rate")
    private Double oldRate;
    @Column(name = "new_rate")
    private Double newRate; // показывает текущий на момент перерасчитки


    @Column(name = "recalculated_at")
    private Instant recalculatedAt;
    @Column(name="status")
    @Convert(converter = RatingStatusConverter.class)
    private RatingStatus ratingStatus;

    @Column(name="triggered_by")
    @Convert(converter = RatingTriggerTypeConverter.class)
    private RatingTriggerType triggeredBy;

}
