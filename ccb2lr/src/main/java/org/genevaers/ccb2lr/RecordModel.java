package org.genevaers.ccb2lr;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class RecordModel {
    
	private String name;
	private LinkedHashMap<String, CobolField> fields = new LinkedHashMap<>();


	public String getName() {
		return name;
	}

    public void setName(String name) {
        this.name = name;
    }

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
}
