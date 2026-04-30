package org.example.core.services.upload;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.core.dto.creating.PriceCreateDto;
import org.example.core.exceptions.CsvInvalidStructure;
import org.example.core.exceptions.NonHibernateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ImportFileCsvService {
    private static final Logger logger = LogManager.getLogger(ImportFileCsvService.class);

    @Transactional
    public List<PriceCreateDto> importPrices(InputStream inputStream) {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )){
            String headline = reader.readLine();
            if (headline.isBlank()){
                throw new CsvInvalidStructure("Header is invalid", 0);
            }
            checkHeadline(headline);

            List<String> error = new ArrayList<>();
            List<PriceCreateDto> prices = new ArrayList<>();
            String line;
            int rowNum=1;
            int length = HeadersForPriceImport.values().length;


            while((line = reader.readLine()) != null){
                rowNum++;
                if (line.isBlank()){
                    continue;
                }

                try{
                    prices.add(checkLine(line, length, rowNum));
                }catch (CsvInvalidStructure ex){
                    error.add(ex.getMessage());
                }

            }

            if (!error.isEmpty()){
                throw new CsvInvalidStructure("Ошибки валидации:\n" + String.join("\n", error), -1);
            }
            return prices;

        }
        catch (CsvInvalidStructure ex){
            throw ex;
        }
        catch(Exception e){
            logger.error("ImportFileCsvService importPrices: " + e.getMessage());
            throw new NonHibernateException(e.getMessage());
        }
    }

    private PriceCreateDto checkLine(String line, int length, int rowNum){
        String[] lineSplit = line.split(";");

        if (lineSplit.length != length){
            throw new CsvInvalidStructure("Line is invalid, length is " + lineSplit.length + " but needed: " + length, rowNum);
        }

        Long shopId = parseLong(lineSplit, HeadersForPriceImport.SHOP_ID.code, HeadersForPriceImport.SHOP_ID.header, rowNum);
        Long goodId = parseLong(lineSplit, HeadersForPriceImport.GOOD_ID.code, HeadersForPriceImport.GOOD_ID.header, rowNum);
        BigDecimal price = parseBigDecimal(lineSplit, HeadersForPriceImport.PRICE.code, HeadersForPriceImport.PRICE.header, rowNum);


        return new PriceCreateDto(shopId, goodId, price);
    }

    private Long parseLong(String[] mainLine, int index, String name, int row){
        String line = mainLine[index].trim();
        if (line.isBlank()){
            throw new CsvInvalidStructure(name + " is null " , row);
        }

        try{
            return Long.parseLong(line);
        }
        catch(NumberFormatException ex){
            throw new CsvInvalidStructure(name + " is invalid, value is " + line , row);
        }
    }

    private BigDecimal parseBigDecimal(String[] mainLine, int index, String name, int row){
        String line = mainLine[index].trim().replace(" ", "").replace(",", ".");
        if (line.isBlank()){
            throw new CsvInvalidStructure(name + " is null " , row);
        }

        try{
            return new BigDecimal(line);
        }
        catch(NumberFormatException ex){
            throw new CsvInvalidStructure(name + " is invalid, value is " + line , row);
        }
    }

    private void checkHeadline(String headline) {
        try{
            String[] headlineNames = HeadersForPriceImport.expectedHeaders();
            String[] headlinesGiven = headline.split(";");

            if (headlineNames.length != headlinesGiven.length){
                throw new CsvInvalidStructure("Wrong number of headlines: needed " + headlineNames.length +
                        ", but given: " + headlinesGiven.length, 0);
            }

            for (int i=0; i<headlineNames.length; i++){
                if (!headlineNames[i].equalsIgnoreCase(headlinesGiven[i])){
                    throw new CsvInvalidStructure("Wrong headline in column: " + i, 0);
                }
            }
        }
        catch(CsvInvalidStructure ex){
            throw ex;
        }
        catch(Exception e){
            logger.error("ImportFileCsvService checkHeadline: " + e.getMessage());
        }

    }
}
