package org.genevaers.ccb2lr;

import java.util.Collection;
import java.util.Iterator;

public interface ParentField {

	public Collection<CobolField> getFields();

    public void addField(CobolField f);

    public Iterator<CobolField> getFieldIterator();

    public int getSection();

    public ParentField getParent();

    public void removeField(String name);

}
