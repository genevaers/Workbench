package org.genevaers.sycadas;

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


import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.stringtemplate.v4.compiler.STParser.mapExpr_return;

public class LookupRef {
    private String name;
    private int id;
    private Map<String, Integer> lookupFieldsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasFields() {
        return lookupFieldsByName.size() > 0;
    }

    public Map<String, Integer> getLookupFieldsByName() {
        return lookupFieldsByName;
    }

    public void setLookupFieldsByName(Map<String, Integer> lookFieldsByName) {
        this.lookupFieldsByName = lookFieldsByName;
    }

    public Stream<Entry<String, Integer>> getLookFieldsStream() {
        return lookupFieldsByName.entrySet().stream();
    }

    public Stream<Integer> getLookFieldIdsStream() {
        return lookupFieldsByName.values().stream();
    }

    public Integer getField(String name) {
        return lookupFieldsByName.get(name);
    }

    @Override
    public String toString() {
        return name + "[" + id + "]" + fieldsToString() ;
    }

    private String fieldsToString() {
        StringBuilder sb = new StringBuilder("\n    Fields");
        lookupFieldsByName.entrySet().stream().forEach(e -> sb.append("\n        " + e.getKey() + "[" + e.getValue() + "]"));
        return sb.toString();
    }
}