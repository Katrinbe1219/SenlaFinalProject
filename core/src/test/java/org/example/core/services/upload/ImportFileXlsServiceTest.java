package org.example.core.services.upload;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.core.exceptions.CsvInvalidStructure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@ExtendWith(MockitoExtension.class)
public class ImportFileXlsServiceTest {

    @InjectMocks
    ImportFileXlsxService service;

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfHeaderDoesNotExist")
    void importPricesIfHeaderDoesNotExist() throws IOException {
        InputStream in = createXlsx(
                null,
                new String[]{"1", "1", "1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains("File has no headers"));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfHeaderIsInvalidFirst")
    void importPricesIfHeaderIsInvalidFirst() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id"},
                new String[]{"1", "1", "1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains(" is null, but expected "));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfHeaderIsInvalidSecond")
    void importPricesIfHeaderIsInvalidSecond() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id", "smth_non_valid"},
                new String[]{"1", "1", "1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains("Header's name is no valid in"));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfHeaderLengthIsInvalid")
    void importPricesIfHeaderLengthIsInvalid() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id",""},
                new String[]{"1", "1", "1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains("Header's name is no valid in "));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfSheetDoesNotExist")
    void importPricesIfSheetDoesNotExist() throws IOException {
        Workbook wb = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        InputStream in = new ByteArrayInputStream(out.toByteArray());
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains("File has no sheets"));
    }


    @Test
    @Tag("negative")
    @DisplayName("importPricesIfPriceIsNull")
    void importPricesIfPriceIsNull() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id","price"},
                new String[]{"1", "1", "1"},
                new String[]{"1", "1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains(" can not be null"));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfPriceIsInvalid")
    void importPricesIfPriceIsInvalid() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id","price"},
                new String[]{"1", "1", "1"},
                new String[]{"1", "1","asd"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains(" is an incorrect bigDecimal"));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfHeaderLengthIsInvalid")
    void importPricesIfShopIdIsNull() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id","price"},
                new String[]{"1", "1", "1"},
                new String[]{"1", null,"1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains(" can not be null"));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPricesIfShopIdIsInvalid")
    void importPricesIfShopIdIsInvalid() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id","price"},
                new String[]{"1", "1", "1"},
                new String[]{"1", "asd","1"}
        );
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, () ->
                service.importPrices(in));
        Assertions.assertTrue(ex.getMessage().contains("is an incorrect long"));
    }

    @Test
    @Tag("positive")
    @DisplayName("importPricesIfSuccessful")
    void importPricesIfSuccessful() throws IOException {
        InputStream in = createXlsx(
                new String[]{"good_id", "shop_id","price"},
                new String[]{"1", "1", "1"},
                new String[]{"2", "2","2"}
        );
     Assertions.assertDoesNotThrow( () ->
                service.importPrices(in));

    }




    private InputStream createXlsx(
            String[] headers,
            String[] ... rows
    ) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();


        if (headers != null) {
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

            }
        }


        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < rows[i].length; j++) {
                if (rows[i][j]!=null){
                    row.createCell(j).setCellValue(rows[i][j]);
                }

            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
