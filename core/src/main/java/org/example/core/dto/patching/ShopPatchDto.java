package org.example.core.dto.patching;

import jakarta.annotation.security.DenyAll;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopPatchDto {
    @NotNull(message = "id can not be null")
    @Positive(message = "id must be > 0")
    private Long id;
    @NullOrNotBlank
    private String name;
    @NullOrNotBlank
    private String address;
    @Positive(message = "districtId must be >0")
    private Long districtId;
}
