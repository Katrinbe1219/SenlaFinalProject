package org.example.core.hibernate.base_settings.exportEnum;

import org.example.core.exceptions.NotCorrectInput;

public enum RecalculationInclude {
    GOODS, CATEGORIES;

    public static RecalculationInclude fromString(String value){
        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput("Unknown include value: " + value);
        }
    }
}
