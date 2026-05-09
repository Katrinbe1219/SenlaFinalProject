package org.example.core.services.documents.prices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.export.PriceHistoryByGoodAndShop;
import org.example.core.dto.export.ShopsCurrentPricesDto;
import org.example.core.hibernate.base_settings.filters.exporting.ExportShopsCurrentPricesFilter;
import org.example.core.hibernate.documents.prices.PriceHibForExport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PriceExportService {
    private static final Logger logger = LogManager.getLogger(PriceExportService.class);
    private PriceHibForExport priceHib;
    public PriceExportService(PriceHibForExport priceHib) {
        this.priceHib = priceHib;
    }

    @Transactional
    public List<ShopsCurrentPricesDto> getShopsCurrentPrices (ExportShopsCurrentPricesFilter filters){
        try{
            return priceHib.getShopsCurrentPrices(filters);
        }catch(Exception e){
            logger.error("PriceExportService getShopsCurrentPrices:" + e.getMessage());
            throw e;
        }

    }

    @Transactional
    public List<PriceHistoryByGoodAndShop> getPriceHistoryByGoodId (Long goodId, Long shopId){
        try{
            return priceHib.getPriceHistoryByGoodId(goodId, shopId);
        }catch (Exception e){
            logger.error("PriceExportService getPriceHistoryByGoodId:" + e.getMessage());
            throw e;
        }

    }
}
