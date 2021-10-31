package org.genevaers.ccb2lr;

public class BinaryField extends CobolField {

    @Override
    public FieldType getType() {
        return FieldType.BINARY;
    }

    @Override
    public int getLength() {
        int len = getPicLength();
        // For Binary the length implies the width
        // At least in some circumstances.
        // No single byte - see https://www.ibm.com/docs/en/i/7.4?topic=clause-computational-5-comp-5-phrase-binary
        if(len > 9) {
            len = 8;
        } else if(len > 5){
            len = 4;
        } else {
            len = 2;
        }
        return len;
    }
    
}
