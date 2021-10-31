package org.genevaers.ccb2lr;

public class PackedField extends CobolField{

    @Override
    public FieldType getType() {
        return FieldType.PACKED;
    }

    @Override
    public int getLength() {
        int len = getPicLength();
        return (len+1)/2;
    }
    
}
