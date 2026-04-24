package org.example.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TagDto {
    private String name;
    private Long id;
}
