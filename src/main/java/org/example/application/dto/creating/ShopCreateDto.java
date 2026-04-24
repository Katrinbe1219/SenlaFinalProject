package org.example.application.dto.creating;

import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class ShopCreateDto {
    private String name;
    private String address;
    private Long districtId;
}
