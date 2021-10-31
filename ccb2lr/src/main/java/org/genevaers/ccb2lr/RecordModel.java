package org.genevaers.ccb2lr;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecordModel {
    
	private String name;
	private List<CobolField> fields = new ArrayList<>();


	public String getName() {
		return name;
	}

    public void setName(String name) {
        this.name = name;
    }

	public List<CobolField> getFields() {
		return fields;
	}

    public void addField(CobolField f) {
        fields.add(f);
    }

    public Iterator<CobolField> getFieldIterator() {
        return fields.iterator();
    }

    public int getLength() {
        int length = 0;
        Iterator<CobolField> fit = fields.iterator();
        while(fit.hasNext()) {
            CobolField cbf = fit.next();
            length += cbf.getLength();
        }
        return length;
    }
}
