package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

public enum BaseSortTypes {
    ASC(1, "id", "ASC"), DESC(2, "id","DESC"),
    NAME_ASC(3, "name", "ASC"), NAME_DESC(4, "name", "DESC");
    private int value;
    @Getter
    private String name;
    @Getter
    private String dir;

    private BaseSortTypes(int value, String name, String dir) {
        this.value = value;
        this.name = name;
        this.dir = dir;
    }


    @JsonCreator
    public static BaseSortTypes forValue(int value) {
        for (BaseSortTypes filter : BaseSortTypes.values()) {
            if (filter.value == value) {
                return filter;
            }
        }

        throw new IllegalArgumentException("BaseFilters unknown value: " + value);
    }
}
