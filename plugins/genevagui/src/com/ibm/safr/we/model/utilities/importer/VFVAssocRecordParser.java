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
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

public class VFVAssocRecordParser extends RecordParser {

    public VFVAssocRecordParser(ComponentImporter importer) {
        super(importer);
    }

    @Override
    protected String getRecordExpression() {
        return "//VF-V-Association/Record";
    }

    @Override
    protected SAFRTransfer parseRecord(Node record)
        throws SAFRValidationException, XPathExpressionException {
        String fieldValue;
        ViewFolderViewAssociationTransfer trans = new ViewFolderViewAssociationTransfer();

        trans.setEnvironmentId(importer.getTargetEnvironmentId());

        fieldValue = parseField("VFVASSOCID", record);
        trans.setAssociationId(fieldToInteger("VFVASSOCID", fieldValue));

        fieldValue = parseField("VIEWFOLDERID", record);
        trans.setAssociatingComponentId(fieldToInteger("VIEWFOLDERID",fieldValue));

        fieldValue = parseField("VIEWID", record);
        trans.setAssociatedComponentId(fieldToInteger("VIEWID",fieldValue));

        fieldValue = parseField("CREATEDTIMESTAMP", record);
        trans.setCreateTime(fieldToDate("CREATEDTIMESTAMP", fieldValue));

        fieldValue = parseField("CREATEDUSERID", record);
        trans.setCreateBy(fieldValue.trim());

        fieldValue = parseField("LASTMODTIMESTAMP", record);
        trans.setModifyTime(fieldToDate("LASTMODTIMESTAMP", fieldValue));

        fieldValue = parseField("LASTMODUSERID", record);
        trans.setModifyBy(fieldValue.trim());

        return trans;
    }

    @Override
    public void checkReferentialIntegrity() throws SAFRValidationException {
    }

}
