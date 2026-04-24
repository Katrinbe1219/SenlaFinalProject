package org.example.application.dto.patching;

import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopPatchDto {
    private Long id;
    private String name;
    private String address;
    private Long districtId;
}
