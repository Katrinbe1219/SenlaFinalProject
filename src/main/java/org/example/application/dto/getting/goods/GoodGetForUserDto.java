package org.example.application.dto.getting.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodGetForUserDto {
    private Long id;
    private String name;
    private String category;
    private String unit;
    private Double rate;
    private String tags;
    private String description;

}
