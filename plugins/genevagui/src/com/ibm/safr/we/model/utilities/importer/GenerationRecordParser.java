package com.ibm.safr.we.model.utilities.importer;

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


import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

public class GenerationRecordParser extends RecordParser {

    public GenerationRecordParser(ComponentImporter importer) {
        super(importer);
    }

    @Override
    protected String getRecordExpression() {
        return "//Generation/Record";
    }

    @Override
    protected SAFRTransfer parseRecord(Node record) throws SAFRValidationException, XPathExpressionException {
        String fieldValue;
        GenerationTransfer trans = new GenerationTransfer();
        trans.setId(0);
        
        fieldValue = parseField("XMLVERSION", record);
        trans.setXmlVersion(fieldToInteger("XMLVERSION", fieldValue));

        fieldValue = parseField("PROGRAM", record);
        trans.setProgram(fieldValue.trim());
        
        fieldValue = parseField("FILENAME", record);
        trans.setFileName(fieldValue.trim());

        fieldValue = parseField("CREATEDTIMESTAMP", record);
        trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));

        fieldValue = parseField("CREATEDUSERID", record);
        trans.setCreateBy(fieldValue.trim());

        return trans;
    }

    @Override
    public void checkReferentialIntegrity() throws SAFRValidationException {
    }

}
