package org.genevaers.ccb2lr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CobolField {
    private String section; //for group levels they must increase for deeper levels
                            //We can assume that any copybook we're given is correct.
                            //Not our job to compile the copybook really
                            //But we could check for ascending levels
                            //Or should we model it as a field has fields?
    private String name;
    private String picType;
    private String picCode;
    private List<CobolField> children = new ArrayList<>();

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPicType() {
        return picType;
    }
    public void setPicType(String picType) {
        this.picType = picType;
    }
    public String getPicCode() {
        return picCode;
    }
    public void setPicCode(String picCode) {
        this.picCode = picCode;
    }
    public void addChildField(CobolField f) {
        children.add(f);
    }

    public Iterator<CobolField> getChildIterator() {
        return children.iterator();
    }
    
}
