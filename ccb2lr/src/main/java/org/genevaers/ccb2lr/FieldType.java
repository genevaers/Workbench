package org.genevaers.ccb2lr;

public enum FieldType {
    ALPHA("Alphanumeric", "ALNUM"),
    ZONED("Zoned Decimal", "NUMER"),
    PACKED("Packed Decimal", "PACKD"),
    BINARY("Binary", "BINRY"), 
    OCCURSGROUP("Alphanumeric", "ALNUM"), 
    GROUP("Alphanumeric", "ALNUM");

    private String dataType;
    private String code;
    private FieldType(String dt, String c) {
        dataType = dt;
        code = c;
    }

    public String getDataType() {
        return dataType;
    }

    public String getCode() {
        return code;
    }
}