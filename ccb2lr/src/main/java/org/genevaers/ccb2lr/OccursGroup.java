package org.genevaers.ccb2lr;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class OccursGroup extends GroupField {

    private int times = 1;

    @Override
    public FieldType getType() {
        return FieldType.OCCURSGROUP;
    }

    @Override
    public int getLength() {
        //A group itself has no length
        //Its length is derived from its fields
        int length = 0;
        Iterator<CobolField> fit = fieldsByName.values().iterator();
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
            expandFields("");
        }
        //Following the expansion we disappear
        //Need our replacement
        CobolField c = parent;
        while(c != null) {
            pos = c.resolvePosition(pos);
            c = c.next();
        }
       return pos;
    }

    public void setTimes(int t) {
        times = t;
    }

    public int getTimes() {
        return times;
    }
    
    private void expandFields(String parentExt) {
        //Detach from parent
        // parent.removeField(name); //parent may not point to us may be sibling?
        // Prefer previous sibling over parent if prevsib null we must be child of parent
        //make times deep copies of self 
        //rename as we go 
        // add to parent again may not be parent?
        boolean connectSibling = false;
        if(previousSibling != null) {
            connectSibling = true;
        }
        CobolField connectToMe = null;
        for(int t=1; t<=times; t++) {
            GroupField newMe = (GroupField) CobolFieldFactory.makeNamelessFieldFrom(this);
            String ext = parentExt + "-" + String.format("%02d", t);
            String newName = getName() + ext;
            newMe.setName(newName);

            CobolField child = next(); //this will be our first child
            //What if the child is itself an occurs group
            while(child != null) {
                if(child.getType() == FieldType.OCCURSGROUP) {
                    ((OccursGroup)child).expandFields(ext);
                } else {
                    CobolField newChild = CobolFieldFactory.makeNamelessFieldFrom(child);
                    String newChildName = child.getName() + ext;
                    newChild.setName(newChildName);
                    newMe.addChild(newChild);
                }
                child = child.getNextSibling();
            }

            if(t == 1) {
                if(connectSibling) {
                    previousSibling.setNextSibling(newMe);
                    newMe.setPreviousSibling(previousSibling);
                } else {
                    parent.firstChild = newMe;
                    newMe.parent = parent;
                }
                connectToMe = newMe;
            } else {
                //we are a sibling of the first newME
                if(connectToMe != null) {
                    connectToMe.setNextSibling(newMe);
                    newMe.setPreviousSibling(connectToMe);
                    connectToMe = newMe;
                }
            }
        }
        if(connectToMe != null) {
            connectToMe.nextSibling = nextSibling;
        }



        // LinkedHashMap<String, CobolField> origFields = deepCopy(fields);
        // fields.clear(); 
        // for(int t=1; t<=times; t++) {
        //     GroupField newMe = (GroupField) CobolFieldFactory.makeNamedFieldFrom(this);
        //     String ext = parentExt + "-" + String.format("%02d", t);
        //     String newName = getName() + ext;
        //     newMe.setName(newName);
        //     parent.addField(newMe);
        //     Iterator<CobolField> oi = origFields.values().iterator();
        //     while(oi.hasNext()) {
        //         CobolField of = oi.next();
        //         //
        //         if(of.getType() == FieldType.OCCURSGROUP) {
        //             newMe.addField(of);
        //             ((OccursGroup)of).setParent(newMe);
        //             ((OccursGroup)of).expandFields(ext);
        //         } else {
        //             String newFieldName = of.getName() + ext;
        //             CobolField newField = CobolFieldFactory.makeNamedFieldFrom(of);
        //             newField.setName(newFieldName);
        //             newMe.addField(newField);
        //         }
        //     }
        // }
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

    public void close(CobolField parent) {
        // parent.removeField(getName());
		// for(int t=1; t<=times; t++) {
        //     GroupField newGroup = CobolFieldFactory.copyGroupWith(this, t);
        //     parent.addField(newGroup);
		// }
    }
}
