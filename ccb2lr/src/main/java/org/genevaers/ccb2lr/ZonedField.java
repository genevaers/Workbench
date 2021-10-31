package org.genevaers.ccb2lr;

public class ZonedField extends CobolField {

    @Override
    public FieldType getType() {
        return FieldType.ZONED;
    }

    @Override
    public int getLength() {
        int len = 0;
        int parenStart = picCode.indexOf('(', 0);
        if(parenStart != -1) {
            len = getParenLength(parenStart);
        } else {
            len = picCode.length();
        }
        return len;
    }


}
