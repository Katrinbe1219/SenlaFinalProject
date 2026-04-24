package org.example.core.dto.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopsCurrentPricesDto {
    private Long goodId;
    private String goodName;
    private BigDecimal price;
    private Long shopId;

    private String shopName;
    private  String address;
    private Long districtId;
    private String districtName;

    private Long categoryId;
    private String categoryName;
    private Long categoryParentId;

    private String tags;
}
