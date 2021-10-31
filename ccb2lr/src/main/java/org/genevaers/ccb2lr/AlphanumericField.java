package org.genevaers.ccb2lr;

public class AlphanumericField extends CobolField {

    @Override
    public FieldType getType() {
        return FieldType.ALPHA;
    }

    @Override
    public int getLength() {
        //look for bracketed value
        int len = 0;
        int parenStart = picCode.indexOf('(', 0);
        if(parenStart != -1) {
            int parenEnd = picCode.indexOf(')', 0);
            String lenStr = picCode.substring(parenStart+1, parenEnd);
            len = Integer.parseInt(lenStr);
        } else {
            len = picCode.length();
        }
        return len;
    }
    
}
