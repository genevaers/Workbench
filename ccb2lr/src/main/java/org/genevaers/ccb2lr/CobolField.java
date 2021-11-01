package org.genevaers.ccb2lr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CobolField {
    private int section; //for group levels they must increase for deeper levels
                            //We can assume that any copybook we're given is correct.
                            //Not our job to compile the copybook really
                            //But we could check for ascending levels
                            //Or should we model it as a field has fields?
    private String name;
    private String picType;
    protected String picCode;
    protected int position = 0;
    private ParentField parent;

    public enum FieldType {
        ALPHA,
        ZONED,
        PACKED,
        BINARY, 
        GROUP, RECORD
    }

    public int getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = Integer.parseInt(section);
    }

    public void setSection(int s) {
        section = s;
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

    abstract public FieldType getType();

    abstract public int getLength();

    protected int getParenLength(int parenStart) {
        int len;
        int parenEnd = picCode.indexOf(')', 0);
        String lenStr = picCode.substring(parenStart+1, parenEnd);
        len = Integer.parseInt(lenStr);
        return len;
    }

    protected int getPicLength() {
        int len;
        int parenStart = picCode.indexOf('(', 0);
        if(parenStart != -1) {
            len = getParenLength(parenStart);
        } else {
            len = picCode.length();
        }
        return len;
    }

    public boolean isSigned() {
        return picCode.charAt(0) == 'S';
    }

    public int getPosition() {
        return position;
    }

    public int resolvePosition(int pos) {
        position = pos;
        return pos + getLength();
    }

    public void setParent(ParentField parent) {
        this.parent = parent;
    }

    public ParentField getParent() {
        return parent;
    }

}
