package org.genevaers.ccb2lr;

import java.util.ArrayList;
import java.util.List;

public class CobolCollection {
    
	private List<CobolField> fields = new ArrayList<>();
	private CobolField currentField;
	private GroupField recordGroup;
	private int section;

    public void addCobolField(CobolField newField) {
		if(currentField == null) {
			recordGroup = (GroupField) newField;
		} else {
			if(newField.getSection() == currentField.getSection()) {
				currentField.addSibling(newField);
			} else if ( newField.getSection() > currentField.getSection()) {
				currentField.addChild(newField);
			} else {
				CobolField s = findSibling(newField);
				s.addSibling(newField);
			}
		}
		fields.add(newField);
		currentField = newField;
	}

	private CobolField findSibling(CobolField newField) {
		//section < than currentGroup - Find the LAST match not the first
		//CobolField grp = currentField;
		CobolField prnt = currentField.getParent();
		while(prnt != null && prnt.getSection() != newField.getSection() ) {
			// if(prnt.getType() == FieldType.GROUP || prnt.getType() == FieldType.OCCURSGROUP) {
			// 	grp = prnt;
			// }
			prnt = prnt.getParent();
		}
		// CobolField n = currentGroup.parent;
		// while (s <=  n.getSection()) {
		// 	if(n.getType() == FieldType.GROUP || n.getType() == FieldType.OCCURSGROUP) {
		// 		currentGroup = (GroupField) n;
		// 	}
		// 	n = n.next();
		// }
		return prnt;
	}

    public List<CobolField> getFields() {
        return fields;
    }

    public GroupField getRecordGroup() {
        return recordGroup;
    }
}
