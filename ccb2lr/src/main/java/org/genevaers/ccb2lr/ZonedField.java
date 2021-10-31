package org.genevaers.ccb2lr;

public class ZonedField extends CobolField {

    @Override
    public FieldType getType() {
        return FieldType.ZONED;
    }

    @Override
    public int getLength() {
        return getPicLength();
    }


}
