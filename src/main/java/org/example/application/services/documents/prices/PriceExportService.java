package org.example.application.services.documents.prices;

import org.example.application.dto.export.PriceHistoryByGoodAndShop;
import org.example.application.dto.export.ShopsCurrentPricesDto;
import org.example.application.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.application.hibernate.documents.prices.PriceHibForExport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PriceExportService {

    private PriceHibForExport priceHib;
    public PriceExportService(PriceHibForExport priceHib) {
        this.priceHib = priceHib;
    }

    @Transactional
    public List<ShopsCurrentPricesDto> getShopsCurrentPrices (ExportShopsCurrentPricesFilter filters){
        return priceHib.getShopsCurrentPrices(filters);
    }

    @Transactional
    public List<PriceHistoryByGoodAndShop> getPriceHistoryByGoodId (Long goodId, Long shopId){
        return priceHib.getPriceHistoryByGoodId(goodId, shopId);
    }
}
