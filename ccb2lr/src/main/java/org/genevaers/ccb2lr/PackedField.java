package org.genevaers.ccb2lr;

public class PackedField extends CobolField{

    @Override
    public FieldType getType() {
        return FieldType.PACKED;
    }

    @Override
    public int getLength() {
        return (picLength+1)/2;
    }
    
}
