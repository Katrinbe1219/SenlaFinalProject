package org.example.core.services.upload;

import org.example.core.exceptions.CsvInvalidStructure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@ExtendWith(MockitoExtension.class)
public class ImportFileCsvServiceTest {
    @InjectMocks
    ImportFileCsvService service;

    @Test
    @Tag("negative")
    @DisplayName("importPriceIfHeaderDoesNotExist")
    void importPriceIfHeaderDoesNotExist() {
        InputStream in = toStream("\nsmth;smth");
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, ()->
                service.importPrices(in)
                );
        Assertions.assertTrue(ex.getMessage().contains("Header can not be blank"));

    }

    @Test
    @Tag("negative")
    @DisplayName("importPriceIfFieldsAreInvalid")
    void importPriceIfFieldsAreInvalid() {
        // пропущен во второй строчке price
        InputStream in = toStream("good_id;shop_id;price\n1;1;1\n2;2\n3;3;3");
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, ()->
                service.importPrices(in)
        );

        Assertions.assertTrue(ex.getMessage().contains("Line is invalid, length is "));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPriceIfHeaderLengthIsInvalid")
    void importPriceIfHeaderLengthIsInvalid() {
        InputStream in = toStream("good_id;shop_id\nsmth;smth");
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, ()->
                service.importPrices(in)
        );

        Assertions.assertTrue(ex.getMessage().contains("Wrong number of headlines:"));
    }

    @Test
    @Tag("negative")
    @DisplayName("importPriceIfHeaderFieldsIsInvalid")
    void importPriceIfHeaderFieldsIsInvalid() {
        InputStream in = toStream("good_id;shop_id;not_valid_id\nsmth;smth");
        CsvInvalidStructure ex = Assertions.assertThrows(CsvInvalidStructure.class, ()->
                service.importPrices(in)
        );
        Assertions.assertTrue(ex.getMessage().contains("Wrong headline in column: "));
    }



    @Test
    @Tag("positive")
    @DisplayName("importPriceIfSuccessful")
    void importPriceIfSuccessful() {
        InputStream in = toStream("good_id;shop_id;price\n1;1;1\n2;2;2");
        Assertions.assertDoesNotThrow(()->
                service.importPrices(in)
        );
    }

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }


}
