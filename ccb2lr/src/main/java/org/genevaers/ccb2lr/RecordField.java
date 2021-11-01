package org.genevaers.ccb2lr;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class RecordField extends CobolField implements ParentField { 
    
	private LinkedHashMap<String, CobolField> fields = new LinkedHashMap<>();

	public Collection<CobolField> getFields() {
		return fields.values();
	}

    public void addField(CobolField f) {
        fields.put(f.getName(), f);
    }

    public Iterator<CobolField> getFieldIterator() {
        return fields.values().iterator();
    }

    public int getLength() {
        //This could now be done by getting postiion of the last field and adding its length
        int length = 0;
        Iterator<CobolField> fit = fields.values().iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            length += cbf.getLength();
        }
        return length;
    }

    public CobolField getField(String name) {
        return fields.get(name);
    }

    @Override
    public FieldType getType() {
        return FieldType.RECORD;
    }

    public Object getNumberOfFields() {
        int num = 0;
        Iterator<CobolField> fit = fields.values().iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            if(cbf.getType() == FieldType.GROUP) {
                num++; //one for the group itself
                num += ((GroupField)cbf).getNumberOfFields();
            } else {
                num++;
            }
        }
        return num;
    }

    public void resolvePositions() {
        int pos = 1;
        Iterator<CobolField> fit = fields.values().iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            pos = cbf.resolvePosition(pos);
        }
    }

}
