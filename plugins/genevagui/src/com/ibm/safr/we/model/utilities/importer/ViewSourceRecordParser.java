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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.utilities.FileUtils;

public class ViewSourceRecordParser extends RecordParser {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.model.utilities.importer.ViewSourceRecordParser");
	
	public ViewSourceRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//View-Source/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		ViewSourceTransfer trans = new ViewSourceTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("VIEWSOURCEID", record);
		trans.setId(fieldToInteger("VIEWSOURCEID", fieldValue));

		fieldValue = parseField("SRCSEQNBR", record);
		trans.setSourceSeqNo(fieldToInteger("SRCSEQNBR", fieldValue));

		fieldValue = parseField("INLRLFASSOCID", record);
		trans.setLRFileAssocId(fieldToInteger("INLRLFASSOCID", fieldValue));

		fieldValue = parseField("VIEWID", record);
		Integer viewId = fieldToInteger("VIEWID", fieldValue);
		trans.setViewId(viewId);

        fieldValue = parseField(LogicTextType.Extract_Record_Filter.getExportStr(), record);
        trans.setExtractFilterLogic(FileUtils.remBRLineEndings(fieldValue));

        fieldValue = parseField("OUTLFPFASSOCID", record);
        Integer outLFPFId = fieldToInteger("OUTLFPFASSOCID", fieldValue);
        trans.setExtractFileAssociationId(outLFPFId);
        
        fieldValue = parseField("WRITEEXITID", record);
        Integer outExitId = fieldToInteger("WRITEEXITID", fieldValue);
        trans.setWriteExitId(outExitId);
        
        fieldValue = parseField("WRITEEXITPARM", record);
        trans.setWriteExitParams(fieldValue.trim());        
        
        fieldValue = parseField("EXTRACTOUTPUTIND", record);
        trans.setExtractOutputOverride(DataUtilities.intToBoolean(fieldToInteger("EXTRACTOUTPUTIND", fieldValue)));
        
        fieldValue = parseField(LogicTextType.Extract_Record_Output.getExportStr(), record);
        trans.setExtractRecordOutput(FileUtils.remBRLineEndings(fieldValue));
        
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
		List<Integer> viewSourceFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> viewSourceTMap = null;
		if (importer.records.containsKey(ViewSourceTransfer.class)) {
			viewSourceTMap = importer.records.get(ViewSourceTransfer.class);
		} else {
			viewSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
			// map
		}

		switch (importer.getComponentType()) {
		case View:
			// XVIEWSRCLRFILEID in View Column Source
			Map<Integer, SAFRTransfer> viewColumnSourceTMap = null;
			if (importer.records.containsKey(ViewColumnSourceTransfer.class)) {
				viewColumnSourceTMap = importer.records
						.get(ViewColumnSourceTransfer.class);
			} else {
				viewColumnSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewColumnSourceTMap.values()) {
				ViewColumnSourceTransfer viewColSrcTrans = (ViewColumnSourceTransfer) tfr;
				Integer viewSourceId = viewColSrcTrans.getViewSourceId();
				if (viewSourceId > 0) {
					if (viewSourceTMap.get(viewSourceId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to View Source ["
												+ viewSourceId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewSourceFKeys.add(viewSourceId);
				}
			}

		default:
			// no RI checks
		}

	}

	/**
	 * Replace association id's in the source records with association id's from the target (when
	 * they exist).
	 */
	public void replaceAssociationIds() throws SAFRException
	{
		// ------------ check View Sources ------------------
		Map<Integer, SAFRTransfer> vsMap = importer.records.get(ViewSourceTransfer.class);
		
		if (vsMap == null)
		{
			return;
		}
		
		Collection<ComponentAssociation> existingAssocs = importer.existingAssociations
				.get(ComponentAssociation.class).values();

		// if we do this later then the envID is that of the target
		for(Map.Entry<Integer, SAFRTransfer> viewSrcEnt : vsMap.entrySet())
		{	
			ViewSourceTransfer viewSrc = (ViewSourceTransfer)viewSrcEnt.getValue();
			if (viewSrc.getLRFileAssocId() != null && viewSrc.getLRFileAssocId() != 0) {
				try {
					ComponentAssociationTransfer srcAssoc = (ComponentAssociationTransfer)
					importer.records.get(ComponentAssociationTransfer.class).get(viewSrc.getLRFileAssocId());
					
					for(ComponentAssociation trgAssoc : existingAssocs) {
						if (trgAssoc.getAssociatingComponentId().equals(srcAssoc.getAssociatingComponentId())
								&& trgAssoc.getAssociatedComponentIdNum().equals(srcAssoc.getAssociatedComponentId())) {
							if (!srcAssoc.getAssociationId().equals(trgAssoc.getAssociationId())) {
								viewSrc.setLRFileAssocId(trgAssoc.getAssociationId());
							}
						}
					}
						
				} catch (Exception e) {
					// Unexpected exception
					throw new SAFRException("Failed to find association", e);
				}
			}			
		}
	}
	
	public void checkViewSources() throws SAFRException {
		if (!importer.records.containsKey(ViewSourceTransfer.class)) {
			// no imported view sources
			return;
		}

		if (!importer.duplicateIdMap.containsKey(ViewSourceTransfer.class)) {
			// no imported view sources already exist
			return;
		}

		List<Integer> duplicateViewSrcIds = importer.duplicateIdMap
				.get(ViewSourceTransfer.class);

		for (SAFRTransfer trans : importer.records
				.get(ViewSourceTransfer.class).values()) {
			ViewSourceTransfer importedViewSrc = (ViewSourceTransfer) trans;
			if (duplicateViewSrcIds.contains(importedViewSrc.getId())) {
				// This view source already exists in DB.
				// Check that the View ID fkeys match.
				ViewSourceTransfer existingViewSrc = (ViewSourceTransfer) importer.existingComponentTransfers
						.get(ViewSourceTransfer.class).get(
								importedViewSrc.getId());

				if (!existingViewSrc.getViewId().equals(
						importedViewSrc.getViewId())) {
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"View Source ["
											+ importedViewSrc.getId()
											+ "] from View ["
											+ importedViewSrc.getViewId()
											+ "] already exists in the target Environment for a different View ["
											+ existingViewSrc.getViewId()
											+ "].");
					throw sve;
				}
			}
		}

	}
}
