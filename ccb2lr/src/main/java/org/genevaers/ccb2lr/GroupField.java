package org.genevaers.ccb2lr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GroupField extends CobolField {

	protected Map<String, CobolField> fieldsByName = new HashMap<>();

    @Override
    public FieldType getType() {
        return FieldType.GROUP;
    }

    @Override
    public int getLength() {
        //A group itself has no length
        //Its length is derived from its fields
        int length = 0;
        CobolField c = firstChild;
        while(c != null) {
            if(c.getType() != FieldType.GROUP && c.getType() != FieldType.OCCURSGROUP) {
                length += c.getLength();
            }
            c = c.next();
        }
        return length;
    }

    @Override
    public boolean isSigned() {
        return false;
    }


    public void addField(CobolField f) {
        fieldsByName.put(f.getName(), f);
    }

    public void removeField(String name) {
        fieldsByName.remove(name);
    }
    
    public CobolField getField(String name) {
        CobolField n = next();
        boolean notfound = true;
        while(notfound && n != null) {
            if(n.getName().equals(name)) {
                notfound = false;
            } else {
                n = n.next();
            }
        }
        return n;
    }

    public Iterator<CobolField> getChildIterator() {
        return fieldsByName.values().iterator();
    }


    @Override
    public int resolvePosition(int pos) {
        position = pos;
        return pos;
    }

    public void close(CobolField parent) {
		//parent.addField(this);
    }

    public int getMatchingSection() {
        return super.getSection();
    }

    public Iterator<CobolField> getFieldIterator() {
        return fieldsByName.values().iterator();
    }

}
