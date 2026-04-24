package org.example.core.hibernate.base_settings.exportEnum;

import org.example.core.exceptions.NotCorrectInput;

public enum ModeratorRecalcInclude {
    MODERATORS, GOODS;

    public static ModeratorRecalcInclude getFormat(String value) {

        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput(" ModeratorRecalcInclude Unknown include value: " + value);
        }
    }
}
