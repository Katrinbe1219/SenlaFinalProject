package org.example.core.services.documents.prices.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OptionForUpload {
    STOP(1), SKIP(2), REPLACE(3);
    private int value;
    private OptionForUpload(int value) {
        this.value = value;
    }

    @JsonCreator
    public static OptionForUpload forValue(int value) {
        for (OptionForUpload option : values()) {
            if (option.value == value) {
                return option;
            }
        }

        throw new IllegalArgumentException("OptionForUpload Unknown Value: " + value);
    }

    public static OptionForUpload getValue(String check) {
        if (check == null){
            throw new IllegalArgumentException("OptionForUpload: Null can not be given ");
        }
        for (OptionForUpload value: values()){
            if (value.name().equalsIgnoreCase(check)){
                return value;
            }
        }

        throw new IllegalArgumentException("OptionForUpload Unknown Value: " + check);
    }
}
