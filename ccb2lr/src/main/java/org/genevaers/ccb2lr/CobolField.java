package org.genevaers.ccb2lr;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
    protected String redefinedName;
    protected CobolField redefinedField;
    protected boolean redefines;
    private String[] decBits;
    protected int picLength;
    protected int fieldLength;
    private int mantissaLen;
    private boolean signed;
    private boolean nines = false;
    private boolean noBrackets  = true;

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

    public abstract FieldType getType();

    public int getLength() {
        if(redefines && redefinedField != null) {
            return redefinedField.getLength();
        } else {
            return picLength;
        }
    }

    public int getFieldLength() {
        return picLength;
    }

    protected int getBracketedLength(int parenStart, String bracketedPic) {
        int len;
        int parenEnd = bracketedPic.indexOf(')', 0);
        String lenStr = bracketedPic.substring(parenStart+1, parenEnd);
        len = Integer.parseInt(lenStr);
        return len;
    }

    protected void resolvePicLength() {
        resolveSign();
        if(picHasAVirtualDecimal()) {
            int unitLen = getUnitLength();
            mantissaLen = getMantissaLength();
            picLength = unitLen + mantissaLen;
            if(signed && nines && noBrackets) { //length -1 if all nines. No bracketed terms?
                picLength--;
            }
        } else {
            picLength =  getPicTermLength(picCode);
        }
        fieldLength =  getLength();
    }

    private void resolveSign() {
        signed = picCode.charAt(0) == 'S';
    }

    private int getMantissaLength() {
        return getPicTermLength(decBits[1]);
    }

    private int getUnitLength() {
        return getPicTermLength(decBits[0]);
    }

    private int getPicTermLength(String term) {
        int len;
        int bStart = getBracketStart(term);
        if(bStart != -1) {
            len = getBracketedLength(bStart, term);
            noBrackets = false;
        } else {
            nines = true;
            len = term.length();
        }
        return len;
    }

    private boolean picHasAVirtualDecimal() {
        decBits = picCode.split("V");
        return decBits.length == 2;
    }

    private int getBracketStart(String term) {
        return term.indexOf('(', 0);
    }

    public boolean isSigned() {
        return signed;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int resolvePosition(int pos) {
        if(redefinedName == null || redefinedName.length() == 0) {
            position = pos;  
        }
        resolvePicLength();
        return pos + fieldLength;
    }

    public void setParent(CobolField parent) {
        this.parent = parent;
    }

    public void addChild(CobolField child) {
        if(firstChild == null) {
            firstChild = child;
        } else {
            CobolField childSib = firstChild;
            while(childSib.getNextSibling() != null) {
                childSib = childSib.getNextSibling();
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
        CobolField n = next();
        while(n != null) {
            num++;
            n = n.next();
        }
        return num;
    }

    public CobolField next() {
        if(firstChild != null) {
            return firstChild;
        } else {
            if (nextSibling == null) {
                if(parent != null) {
                    CobolField s = parent.getNextSibling();
                    CobolField p = parent.getParent();
                    while(s == null && p != null) {
                        s = p.getNextSibling();
                        p = p.getParent();
                    }
                    return s;
                } else {
                    return null;
                }
             } else {
                return nextSibling;
            }
        }
    }

    public void addSibling(CobolField newField) {
        nextSibling = newField;
        newField.previousSibling = this;
        newField.parent = this.parent;
    }

    public void replaceFirstChild(GroupField newMe) {
        firstChild = newMe;
    }

    public void setRedefinedName(String name) {
        redefinedName = name;
    }

    public String getRedefinedName() {
        return redefinedName;
    }

    public void setRedefines(boolean redefines) {
        this.redefines = redefines;
    }

    public boolean isRedefines() {
        return redefines;
    }

    public int getNumberOfDecimalPlaces() {
        return mantissaLen;
    }

    public void setRedefinedField(CobolField redefinedField) {
        this.redefinedField = redefinedField;
    }

}
