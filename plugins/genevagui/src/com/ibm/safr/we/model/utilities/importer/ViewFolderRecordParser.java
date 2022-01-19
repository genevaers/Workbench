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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

public class ViewFolderRecordParser extends RecordParser {

    public ViewFolderRecordParser(ComponentImporter importer) {
        super(importer);
    }

    @Override
    protected String getRecordExpression() {
        return "//ViewFolder/Record";
    }

    @Override
    protected SAFRTransfer parseRecord(Node record)
        throws SAFRValidationException, XPathExpressionException {
        String fieldValue;
        ViewFolderTransfer trans = new ViewFolderTransfer();

        trans.setEnvironmentId(importer.getTargetEnvironmentId());

        fieldValue = parseField("VIEWFOLDERID", record);
        trans.setId(fieldToInteger("VIEWFOLDERID", fieldValue));

        fieldValue = parseField("NAME", record);
        trans.setName(fieldValue.trim());

        fieldValue = parseField("COMMENTS", record);
        trans.setComments(fieldValue.trim());

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
        SAFRValidationException sve = new SAFRValidationException();
        Set<Integer> vfKeys = new HashSet<Integer>();

        Map<Integer, SAFRTransfer> vfMap = null;
        if (importer.records.containsKey(ViewFolderTransfer.class)) {
            vfMap = importer.records.get(ViewFolderTransfer.class);
        } else {
            vfMap = new HashMap<Integer, SAFRTransfer>(); // empty map
        }
        
        Map<Integer, SAFRTransfer> vfvMap = null;
        if (importer.records.containsKey(ViewFolderViewAssociationTransfer.class)) {
            vfvMap = importer.records.get(ViewFolderViewAssociationTransfer.class);
        } else {
            vfvMap = new HashMap<Integer, SAFRTransfer>(); // empty map
        }
        
        for (SAFRTransfer tfr : vfvMap.values()) {
            ViewFolderViewAssociationTransfer vfv = (ViewFolderViewAssociationTransfer) tfr;
            Integer vfId = vfv.getAssociatingComponentId();
            if (vfId > 0) {
                if (!vfMap.containsKey(vfId)) {
                    sve.setErrorMessage(importer.getCurrentFile().getName(),
                        "View Folder-View association ["+ vfv.getAssociatedComponentName()+ "] refers to View Folder ["+ 
                        vfId + "] but this is not present in the import file.");
                    throw sve;
                }
                vfKeys.add(vfId);
            }
        }
        
    }

}
