package org.example.core.services.upload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.exceptions.CsvInvalidStructure;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.example.core.hibernate.objects.ShopHibImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportFileXlsxService {
    private static final Logger logger = LogManager.getLogger(ImportFileXlsxService.class);



    @Transactional
    public List<PriceCreateDto> importPrices(InputStream  inputStream) throws Exception {
        try(Workbook wb = new XSSFWorkbook(inputStream)){

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null){
                throw new CsvInvalidStructure("File has no sheets",0);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null){
                throw new CsvInvalidStructure("File has no headers",0);
            }

            validateHeaders(headerRow);

            List<String> errors = new ArrayList<>();
            List<PriceCreateDto> prices = new ArrayList<>();
            for (int i=1; i<=sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try{
                    prices.add(parseRow(row, i));
                }
                catch(CsvInvalidStructure e){
                    errors.add("Строка " + (i+1) + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()){
                throw new CsvInvalidStructure("Exception in validation: " + String.join("\n", errors), -1);
            }
            return prices;
        }catch (IllegalArgumentException e){
            if (e.getMessage().contains("(no sheets)")){
                throw new CsvInvalidStructure("File has no sheets",0);
            }else if (e.getMessage().contains("no rows")){
                throw new CsvInvalidStructure("File has no rows",0);
            }
            logger.error("IllegalArgumentException ImportFileXlsxService importPrices: " + e.getMessage());
            throw new Exception(e.getMessage());

        }
        catch (Exception e){
            logger.error("Exception ImportFileXlsxService importPrices:" + e.getMessage());
            throw e;
        }
    }

    private boolean isRowEmpty(Row row){
        for (int i=row.getFirstCellNum(); i<row.getLastCellNum(); i++){
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK){
                return false;
            }
        }

        return true;
    }

    private void validateHeaders(Row row){
        String[] expected = HeadersForPriceImport.expectedHeaders();
        for ( int i =0 ; i < expected.length; i++){
            Cell cell = row.getCell(i);
            if (cell == null){
                throw new CsvInvalidStructure("Header problem: cell " + (i+1) + " is null, but expected "
                        + expected[i] , -1);
            }

            String value = cell.getStringCellValue().trim();
            if (!expected[i].equalsIgnoreCase(value)){
                throw new CsvInvalidStructure("Header's name is no valid in " + (i+1)
                        + " table, expected " + expected[i]+ " but received " + value, -1);
            }
        }
    }

    private BigDecimal parseBigDecimalValue(Row row, int index,
                                            String fieldName, int rowNum){
        Cell cell = row.getCell(index);

        if (cell == null){
            throw new CsvInvalidStructure("Field " + fieldName + " can not be null",rowNum);
        }

        if (cell.getCellType() == CellType.NUMERIC){
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        String value = cell.getStringCellValue().trim();
        try{
            return new BigDecimal(value);
        }
        catch (NumberFormatException e){
            throw new CsvInvalidStructure("Field " + fieldName + " is an incorrect bigDecimal",rowNum);
        }
    }


    private Long parseLongCell(Row row, int index,  String fieldName, int rowNum){
        Cell cell = row.getCell(index);
        if (cell == null){
            throw new CsvInvalidStructure("Field " + fieldName + " can not be null",rowNum);
        }

        if (cell.getCellType() == CellType.NUMERIC){
            return  (long) cell.getNumericCellValue();
        }
        String value = cell.getStringCellValue().trim();
        try{
            return Long.parseLong(value);
        }
        catch (NumberFormatException e){
            throw new CsvInvalidStructure("Field " + fieldName + " is an incorrect long",rowNum);
        }
    }

    private PriceCreateDto parseRow(Row row, int rowIndex){
        Long shopId = parseLongCell(row,  HeadersForPriceImport.SHOP_ID.code, "shop_id", rowIndex);
        Long goodId = parseLongCell(row,  HeadersForPriceImport.GOOD_ID.code, "good_id", rowIndex);
        BigDecimal price = parseBigDecimalValue(row,HeadersForPriceImport.PRICE.code,
                "price",rowIndex);

        return new PriceCreateDto(shopId, goodId, price);
    }

//    @Transactional
//    protected PriceCreateAllDto toDto(PriceCreateDto dto){
//        Good good = goodHibImpl.getReferenceById(dto.getGoodId());
//        Shop shop = shopHibImpl.getReferenceById(dto.getShopId());
//        return new PriceCreateAllDto(good, shop, dto.getPrice());
//    }
}
