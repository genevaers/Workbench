package org.genevaers.ccb2lr;

public class OccursGroup extends GroupField {

    private int times = 1;
    FieldType fieldType;

    @Override
    public FieldType getType() {
        if(fieldType == null) {
            return FieldType.OCCURSGROUP;
        } else {
            return fieldType;
        }
    }

    @Override
    public boolean isSigned() {
        return false;
    }


    public void setTimes(int t) {
        times = t;
    }

    public int getTimes() {
        return times;
    }
    
    public void resetOccurs() {
        fieldType = FieldType.GROUP;
    }

}
