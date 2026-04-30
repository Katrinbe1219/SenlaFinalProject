package org.example.core.services.upload;

import java.util.Arrays;
import java.util.Comparator;

public enum HeadersForPriceImport {
    GOOD_ID(0,"GOOD_ID"), SHOP_ID(1,"SHOP_ID"), PRICE(2,"PRICE");

    public int code;
    public  String header;

    private HeadersForPriceImport(int code, String header) {
        this.code = code;
        this.header = header;
    }

    public static String[] expectedHeaders(){
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(c-> c.code))
                .map(c -> c.header)
                .toArray(String[]::new);
    }
}
