package org.example.application.hibernate.base_settings.filters.exporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportShopsCurrentPricesFilter {
    private boolean isTags;
    private boolean isCategories;
    private boolean isShops;
    private List<Long> shopsIds;



}
