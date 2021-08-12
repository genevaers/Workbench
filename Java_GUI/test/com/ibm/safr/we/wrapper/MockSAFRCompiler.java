package com.ibm.safr.we.wrapper;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.query.LookupQueryBean;

public class MockSAFRCompiler {

    public String getVersion() {
        return null;
    }

    public void setConnectionString(String connStr) {
    }

    public void setSchemaName(String schemaName) {
    }

    public void connect() {
    }

    public void disconnect() {
    }

    public void checkSyntax(int colNum, String text) throws SAFRException {
    }

    public void setViewSource(int logicRecId, int fileId) throws SAFRException {
    }
    
    public void setColumnInfo(Integer colNum, Integer id, String defVal,
        Integer vdpType, Code dataType, boolean isSigned, Integer colLength,
        Integer decCount, Code dateTime, Integer rounding, Code alignment,
        Code mask, Integer startPos, Integer ordPos,
        LookupQueryBean sortTitleJoin, LRField sortTitleField,
        Code effDateType, String effDateVal, Integer effDateArg) {
    }

    public byte[] compileText(int colNum, String text) throws SAFRException {
        return null;
    }

    public ArrayList<String> getErrors() {
        return null;
    }

    public ArrayList<String> getWarnings() {
        return null;
    }

    public int[] getUserExits() {
        return null;
    }

    public int[] getPhysicalFiles() {
        return null;
    }

    public int[] getLRFields() {
        return null;
    }

    public Map<Integer, List<Integer>> getLookupFields() {
        return null;
    }

    public void compileFormatCalculation(String text)
        throws SAFRException {
    }

    public void compileExtractFilter(String text)
        throws SAFRException {
    }

    public void compileExtractColumn(int colNum, String text)
        throws SAFRException {
    }

    public void compileExtractOutput(String text) {
    }

    public void compileFormatFilter(String text) throws SAFRException {
    }

    public void setColumnData(Integer colNum, Integer id, String defVal,
        Integer vdpType, Code dataType, boolean isSigned, Integer colLength,
        Integer decCount, Code dateTime, Integer rounding, Code alignment,
        Code mask, Integer startPos, Integer ordPos,
        LookupQueryBean sortTitleJoin, LRField sortTitleField,
        Code effDateType, String effDateVal, Integer effDateArg) {
        
    }

    public void setViewData(int viewId, int envId, Code viewType) throws SAFRException {
    }

    public Set<Integer> getCTColumns() {
        return null;
    }

}
