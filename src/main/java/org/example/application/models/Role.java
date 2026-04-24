package org.example.application.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.application.models.converters.RoleTypeConverter;
import org.example.application.models.types.RoleTypes;

import java.util.List;

@Entity
@Table(name = "roles")
@Setter
@Getter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @Convert(converter = RoleTypeConverter.class)
    private RoleTypes name;

    @OneToMany
    @JoinTable(name = "roles_privileges",
        joinColumns = @JoinColumn(name= "role_id"),
        inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    private List<Privilege> privileges;

}
