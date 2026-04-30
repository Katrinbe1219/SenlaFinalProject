package org.example.core.exceptions;

public class CsvInvalidStructure extends RuntimeException {
    final int row;
    public CsvInvalidStructure(String message, int row) {
        super(message);
        this.row = row;
    }

    public int getRow() {
        return row;
    }
}
