package org.example.application.hibernate.base_settings.exportEnum;

import org.example.application.exceptions.NotCorrectInput;

public enum ExportFormat {
    CSV, XLSX;

    public static ExportFormat getFormat(String value) {

        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput(" ExportFormat Unknown include value: " + value);
        }
    }
}
