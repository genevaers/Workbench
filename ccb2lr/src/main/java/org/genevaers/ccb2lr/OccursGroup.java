package org.genevaers.ccb2lr;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class OccursGroup extends GroupField {

    private int times = 1;

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

    @Override
    public boolean isSigned() {
        return false;
    }

    @Override
    public int resolvePosition(int pos) {
        position = pos;
        if(times > 1) {
            expandFields();
        }
        Iterator<CobolField> fit = fields.values().iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            pos = cbf.resolvePosition(pos);
        }
        return pos;
    }

    @Override
    public Collection<CobolField> getFields() {
        return fields.values();
    }

    @Override
    public Iterator<CobolField> getFieldIterator() {
        return fields.values().iterator();
    }

    public void setTimes(int t) {
        times = t;
    }

    public int getTimes() {
        return times;
    }
    
    private void expandFields() {
        LinkedHashMap<String, CobolField> origFields = deepCopy(fields);
        fields.clear();
        for(int t=0; t<times; t++) {
            Iterator<CobolField> oi = origFields.values().iterator();
            while(oi.hasNext()) {
                CobolField of = oi.next();
                String newName = of.getName() + "-" + String.format("%02d", t);
                CobolField newField = CobolFieldFactory.makeNamedFieldFrom(of);
                newField.setName(newName);
                fields.put(newName, newField);
            }
        }
    }

    //This will need to be recursive..
    private LinkedHashMap<String, CobolField> deepCopy(LinkedHashMap<String, CobolField> src) {
        LinkedHashMap<String, CobolField> trg= new LinkedHashMap<String, CobolField>();
        Iterator<Entry<String, CobolField>> si = src.entrySet().iterator();
        while(si.hasNext()) {
            Entry<String, CobolField> e = si.next();
            trg.put(e.getKey(), e.getValue());
        }
        return trg;
    }

    @Override
    public void close(ParentField parent) {
        parent.removeField(getName());
		for(int t=1; t<=times; t++) {
            GroupField newGroup = CobolFieldFactory.copyGroupWith(this, t);
            parent.addField(newGroup);
		}
    }
}
