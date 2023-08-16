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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupField extends CobolField {

	protected Map<String, CobolField> fieldsByName = new HashMap<>();

    @Override
    public FieldType getType() {
        return FieldType.GROUP;
    }

    @Override
    public int getLength() {
        //A group itself has no length
        //Its length is derived from its fields
        int length = 0;
        CobolField c = firstChild;
        while(c != null) {
            length += c.getLength();
            c = c.getNextSibling();
        }
        return length;
    }

    @Override
    public boolean isSigned() {
        return false;
    }


    public void addField(CobolField f) {
        fieldsByName.put(f.getName(), f);
    }

    public void removeField(String name) {
        fieldsByName.remove(name);
    }
    
    public CobolField getField(String name) {
        CobolField n = next();
        boolean notfound = true;
        while(notfound && n != null) {
            if(n.getName().equals(name)) {
                notfound = false;
            } else {
                n = n.next();
            }
        }
        return n;
    }

    public Iterator<CobolField> getChildIterator() {
        return fieldsByName.values().iterator();
    }

    @Override
    public int resolvePosition(int pos) {
        position = pos;
        return pos;
    }

    public int getMatchingSection() {
        return super.getSection();
    }

    public Iterator<CobolField> getFieldIterator() {
        return fieldsByName.values().iterator();
    }


}
