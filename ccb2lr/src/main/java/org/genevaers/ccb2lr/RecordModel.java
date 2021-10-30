package org.genevaers.ccb2lr;

import java.util.ArrayList;
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
}
