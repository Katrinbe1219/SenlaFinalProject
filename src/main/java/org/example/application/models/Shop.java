package org.example.application.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    @JoinColumn(name = "district_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private District district;


}
