package org.example.application.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.application.models.converters.GoodStatusFromModeratorConverter;
import org.example.application.models.types.GoodStatusFromModerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "goods")
@Setter
@Getter
@NoArgsConstructor
public class Good {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "updated_at")
    private Instant updatedAt;
    @Column(name = "created_at")
    private Instant createdAt;

    @Convert(converter = GoodStatusFromModeratorConverter.class)
    @Column(name="moderator_status")
    GoodStatusFromModerator moderatorStatus;

    private Double rate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "goods_tags",
            joinColumns = @JoinColumn(name="good_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

}
