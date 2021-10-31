package org.genevaers.ccb2lr;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class GroupField extends CobolField{

	private LinkedHashMap<String, CobolField> fields = new LinkedHashMap<>();

    @Override
    public FieldType getType() {
        return FieldType.GROUP;
    }

    @Override
    public int getLength() {
        //A group itself has no length
        //Its length is derived from its fields
        int length = 0;
        Iterator<CobolField> fit = fields.values().iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            length += cbf.getLength();
        }
        return length;
    }

    public void addField(CobolField f) {
        fields.put(f.getName(), f);
    }

    public CobolField getField(String name) {
        return fields.get(name);
    }

    public Iterator<CobolField> getChildIterator() {
        return fields.values().iterator();
    }

    public int getNumberOfFields() {
        int num = 0;
        Iterator<CobolField> fit = fields.values().iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            if(cbf.getType() == FieldType.GROUP) {
                num += ((GroupField)cbf).getNumberOfFields();
            } else {
                num++;
            }
        }
        return num;
    }

    
}
