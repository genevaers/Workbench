package org.genevaers.ccb2lr;

public abstract class CobolField {

    protected CobolField parent;
    protected CobolField firstChild;
    protected CobolField previousSibling;
    protected CobolField nextSibling;
    private int section; //for group levels they must increase for deeper levels
                            //We can assume that any copybook we're given is correct.
                            //Not our job to compile the copybook really
                            //But we could check for ascending levels
                            //Or should we model it as a field has fields?
    protected String name;
    private String picType;
    protected String picCode;
    protected int position = 0;

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

    public void setPosition(int position) {
        this.position = position;
    }

    public int resolvePosition(int pos) {
        position = pos;  
        return pos + getLength();
    }

    public void setParent(CobolField parent) {
        this.parent = parent;
    }

    public void addChild(CobolField child) {
        if(firstChild == null) {
            firstChild = child;
        } else {
            CobolField childSib = firstChild;
            CobolField nextSib = childSib.getNextSibling();
            while(nextSib != null) {
                childSib = nextSib;
                nextSib = childSib.getNextSibling();
            }
            childSib.setNextSibling(child);
        }
        child.parent = this;
    }

    public void setPreviousSibling(CobolField previousSibling) {
        this.previousSibling = previousSibling;
    }

    public void setNextSibling(CobolField nextSibling) {
        this.nextSibling = nextSibling;
    }

    public CobolField getParent() {
        return parent;
    }

    public CobolField getFirstChild() {
        return firstChild;
    }

    public CobolField getNextSibling() {
        return nextSibling;
    }

    public CobolField getPreviousSibling() {
        return previousSibling;
    }

    /**
     *  Number of fields before expanding occurs groups
     */
    public int getNumberOfCobolFields() {
        int num = 0;
        if(firstChild != null) {
            num += firstChild.getNumberOfCobolFields();
        }
        CobolField sib = nextSibling;
        while(sib != null) {
            num += nextSibling.getNumberOfCobolFields();
            num++;
        }



        // Iterator<CobolField> fit = fieldsByName.values().iterator();
        // while(fit.hasNext()) {
        //     CobolField cbf = fit.next();
        //     if(cbf.getType() == FieldType.GROUP) {
        //         num += ((GroupField)cbf).getNumberOfFields();
        //     }
        //     num++;
        // }
        return num;
    }

    public CobolField next() {
        if(firstChild != null) {
            return firstChild;
        } else {
            if (nextSibling == null) {
                CobolField s = parent.getNextSibling();
                CobolField p = parent.getParent();
                while(s == null && p != null) {
                    s = p.getNextSibling();
                    p = parent.getParent();
                }
                return s;
            } else {
                return nextSibling;
            }
        }
    }

}
