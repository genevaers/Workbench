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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Node;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DataUtilities;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.utilities.FileUtils;

public class ViewRecordParser extends RecordParser {

	static public Map<Integer,String> inactiveviews = new HashMap<Integer,String>();

	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.utilities.importer.ViewRecordParser");
	
	public ViewRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//View/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;

		fieldValue = parseField("VIEWID", record);

		ViewTransfer trans = null;
		if (importer.records.get(ViewTransfer.class) == null ||
			importer.records.get(ViewTransfer.class).get(new Integer(fieldValue)) == null) {
			trans = new ViewTransfer();
			trans.setId(fieldToInteger("VIEWID", fieldValue));
			trans.setEnvironmentId(importer.getTargetEnvironmentId());					
		}
		else {
			// Reuse transfer object used to parse the matching View record.
			// if it exists
			trans = (ViewTransfer) importer.records.get(
					ViewTransfer.class).get(new Integer(fieldValue));
			
		}

		fieldValue = parseField("NAME", record);
		trans.setName(fieldValue.trim());

		fieldValue = parseField("EFFDATE", record);
		trans.setEffectiveDate(fieldToDate("EFFDATE", fieldValue));

		fieldValue = parseField("VIEWSTATUSCD", record);
		trans.setStatusCode(fieldValue.trim());

		fieldValue = parseField("VIEWTYPECD", record);
		trans.setTypeCode(fieldValue.trim());

		fieldValue = parseField("EXTRACTFILEPARTNBR", record);
		trans.setWorkFileNumber(fieldToInteger("EXTRACTFILEPARTNBR",fieldValue));

		fieldValue = parseField("OUTPUTMEDIACD", record);
		trans.setOutputFormatCode(fieldValue.trim());

		fieldValue = parseField("OUTPUTLRID", record);
		trans.setOutputLRId(fieldToInteger("OUTPUTLRID", fieldValue));

		fieldValue = parseField("LFPFASSOCID", record);
		trans.setExtractFileAssocId(fieldToInteger("LFPFASSOCID",fieldValue));

		fieldValue = parseField("PAGESIZE", record);
		trans.setPageSize(fieldToInteger("PAGESIZE", fieldValue));

		fieldValue = parseField("LINESIZE", record);
		trans.setLineSize(fieldToInteger("LINESIZE", fieldValue));

		fieldValue = parseField("ZEROSUPPRESSIND", record);
		trans.setZeroSuppressInd(DataUtilities.intToBoolean(fieldToInteger("ZEROSUPPRESSIND", fieldValue)));

		fieldValue = parseField("EXTRACTMAXRECCNT", record);
		trans.setExtractMaxRecCount(fieldToInteger("EXTRACTMAXRECCNT",fieldValue));

		fieldValue = parseField("EXTRACTSUMMARYIND", record);
		trans.setExtractSummaryIndicator(DataUtilities.intToBoolean(fieldToInteger("EXTRACTSUMMARYIND", fieldValue)));

		fieldValue = parseField("EXTRACTSUMMARYBUF", record);
		trans.setExtractSummaryBuffer(fieldToInteger("EXTRACTSUMMARYBUF",fieldValue));

		fieldValue = parseField("OUTPUTMAXRECCNT", record);
		trans.setOutputMaxRecCount(fieldToInteger("OUTPUTMAXRECCNT",fieldValue));

		fieldValue = parseField("CONTROLRECID", record);
		trans.setControlRecId(fieldToInteger("CONTROLRECID", fieldValue));

		fieldValue = parseField("WRITEEXITID", record);
		trans.setWriteExitId(fieldToInteger("WRITEEXITID", fieldValue));

		fieldValue = parseField("WRITEEXITSTARTUP", record);
		trans.setWriteExitParams(fieldValue.trim());

		fieldValue = parseField("FORMATEXITID", record);
		trans.setFormatExitId(fieldToInteger("FORMATEXITID", fieldValue));

		fieldValue = parseField("FORMATEXITSTARTUP", record);
		trans.setFormatExitParams(fieldValue.trim());

		fieldValue = parseField("FILEFLDDELIMCD", record);
		trans.setFieldDelimCode(fieldValue.trim());

		fieldValue = parseField("FILESTRDELIMCD", record);
		trans.setStringDelimCode(fieldValue.trim());

        fieldValue = parseField(LogicTextType.Format_Record_Filter.getExportStr(), record);
        trans.setFormatFilterlogic(FileUtils.remBRLineEndings(fieldValue));
		
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

        fieldValue = parseField("COMPILER", record);
        trans.setCompilerVersion(fieldValue.trim());
		
        fieldValue = parseField("LASTACTTIMESTAMP", record);
        trans.setActivatedTime(fieldToDate("LASTACTTIMESTAMP", fieldValue));

        fieldValue = parseField("LASTACTUSERID", record);
        trans.setActivatedBy(fieldValue.trim());
		
		return trans;
	}

	public void removeinactiveviews() {
		
		SAFRValidationException sve = new SAFRValidationException();
		Map<Integer, SAFRTransfer> viewTMap = null;
		if (importer.records.containsKey(ViewTransfer.class)) {
			viewTMap = importer.records.get(ViewTransfer.class);
		} else {
			viewTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}
		
		Iterator<Integer> it = viewTMap.keySet().iterator();
		Integer viewid = null;
		while(it.hasNext()) {
			Integer id = it.next();
			SAFRTransfer value = viewTMap.get(id);
			ViewTransfer vt = (ViewTransfer) value;
			if(vt.getStatusCode().equals("INACT")) {
				viewid = vt.getId();
				inactiveviews.put(viewid, vt.getName());
				it.remove();		
			}
		}

		
		Map<Integer, SAFRTransfer> viewsourceTMap = null;
		if (importer.records.containsKey(ViewTransfer.class)) {
			viewsourceTMap = importer.records.get(ViewSourceTransfer.class);
		} else {
			viewsourceTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}
		Iterator<Integer> it2 = viewsourceTMap.keySet().iterator();
		
		while(it2.hasNext()) {
			Integer id = it2.next();
			SAFRTransfer value = viewsourceTMap.get(id);
			ViewSourceTransfer vt = (ViewSourceTransfer) value;
			if(vt.getViewId().equals(viewid)) {
				it2.remove();		
			}
		}
		
		Map<Integer, SAFRTransfer> viewcolumnTMap = null;
		if (importer.records.containsKey(ViewColumnTransfer.class)) {
			viewcolumnTMap = importer.records.get(ViewColumnTransfer.class);
		} else {
			viewcolumnTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}
		Iterator<Integer> it3 = viewcolumnTMap.keySet().iterator();
		while(it3.hasNext()) {
			Integer id = it3.next();
			SAFRTransfer value = viewcolumnTMap.get(id);
			ViewColumnTransfer vct = (ViewColumnTransfer) value;
			if(vct.getViewId().equals(viewid)) {
				it3.remove();		
			}
		}
		
		Map<Integer, SAFRTransfer> viewcolumnsrcTMap = null;
		if (importer.records.containsKey(ViewColumnSourceTransfer.class)) {
			viewcolumnsrcTMap = importer.records.get(ViewColumnSourceTransfer.class);
		} else {
			viewcolumnsrcTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}
		Iterator<Integer> it4 = viewcolumnsrcTMap.keySet().iterator();

		while(it4.hasNext()) {
			Integer id = it4.next();
			SAFRTransfer value = viewcolumnsrcTMap.get(id);
			ViewColumnSourceTransfer vct = (ViewColumnSourceTransfer) value;
			if(vct.getViewId().equals(viewid)) {
				it4.remove();		
			}
		}
	}
	
	
	@Override
	public void checkReferentialIntegrity() throws SAFRValidationException {
		removeinactiveviews();
		SAFRValidationException sve = new SAFRValidationException();
		List<Integer> viewFKeys = new ArrayList<Integer>();
		Map<Integer, SAFRTransfer> viewTMap = null;
		if (importer.records.containsKey(ViewTransfer.class)) {
			viewTMap = importer.records.get(ViewTransfer.class);
		} else {
			viewTMap = new HashMap<Integer, SAFRTransfer>();// empty map
		}
		switch (importer.getComponentType()) {
        case ViewFolder:
		case View:
			// VIEWID in view sources
			Map<Integer, SAFRTransfer> viewSourceTMap = null;
			if (importer.records.containsKey(ViewSourceTransfer.class)) {
				viewSourceTMap = importer.records.get(ViewSourceTransfer.class);
			} else {
				viewSourceTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewSourceTMap.values()) {
				ViewSourceTransfer viewSrcTrans = (ViewSourceTransfer) tfr;
				Integer viewId = viewSrcTrans.getViewId();
				if (viewId > 0) {
					if (viewTMap.get(viewId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Source["
												+ viewSrcTrans.getId()
												+ "] refers to View ["
												+ viewId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewFKeys.add(viewId);
				}

			}

			// VIEWID in View Columns
			Map<Integer, SAFRTransfer> viewColumnTMap = null;
			if (importer.records.containsKey(ViewColumnTransfer.class)) {
				viewColumnTMap = importer.records.get(ViewColumnTransfer.class);
			} else {
				viewColumnTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewColumnTMap.values()) {
				ViewColumnTransfer viewColTrans = (ViewColumnTransfer) tfr;
				Integer viewId = viewColTrans.getViewId();
				if (viewId > 0) {
					if (viewTMap.get(viewId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column ["
												+ viewColTrans.getId()
												+ "] refers to View ["
												+ viewId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewFKeys.add(viewId);
				}
			}

			// VIEWID in View Column Sources
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
				Integer viewId = viewColSrcTrans.getViewId();
				if (viewId > 0) {
					if (viewTMap.get(viewId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Column Source ["
												+ viewColSrcTrans.getId()
												+ "] refers to View ["
												+ viewId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewFKeys.add(viewId);
				}
			}

			// VIEWID in View Sort Keys
			Map<Integer, SAFRTransfer> viewSortKeyTMap = null;
			if (importer.records.containsKey(ViewSortKeyTransfer.class)) {
				viewSortKeyTMap = importer.records
						.get(ViewSortKeyTransfer.class);
			} else {
				viewSortKeyTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewSortKeyTMap.values()) {
				ViewSortKeyTransfer viewSortKeyTrans = (ViewSortKeyTransfer) tfr;
				Integer viewId = viewSortKeyTrans.getViewId();
				if (viewId > 0) {
					if (viewTMap.get(viewId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Sort Key ["
												+ viewSortKeyTrans.getId()
												+ "] refers to View ["
												+ viewId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewFKeys.add(viewId);
				}
			}

			// VIEWID in View Header Footers
			Map<Integer, SAFRTransfer> viewHFTMap = null;
			if (importer.records.containsKey(HeaderFooterItemTransfer.class)) {
				viewHFTMap = importer.records
						.get(HeaderFooterItemTransfer.class);
			} else {
				viewHFTMap = new HashMap<Integer, SAFRTransfer>();// empty
				// map
			}
			for (SAFRTransfer tfr : viewHFTMap.values()) {
				HeaderFooterItemTransfer viewHFTrans = (HeaderFooterItemTransfer) tfr;
				Integer viewId = viewHFTrans.getViewId();
				if (viewId > 0) {
					if (viewTMap.get(viewId) == null) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View Header Footer Item ["
												+ viewHFTrans.getId()
												+ "] refers to View ["
												+ viewId
												+ "] but this is not in the import file.");
						throw sve;
					}
					viewFKeys.add(viewId);
				}
			}
		default:
			// No RI checks for other components

		}// end of switch

		// Check that every View primary key has a matching foreign key
		for (Integer viewPKey : viewTMap.keySet()) {
			ViewTransfer viewT = (ViewTransfer) viewTMap.get(viewPKey);
			// check only if View is active.
			if (viewT.getStatusCode().equals(
					SAFRApplication.getSAFRFactory().getCodeSet(
							CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE)
							.getKey())) {
				if (!viewFKeys.contains(viewPKey)) {
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"View ["
											+ viewPKey
											+ "] is not referenced by any component in the import file.");
					throw sve;
				}
			}
		}
	}

	/**
	 * Replace association id's in the source records with association id's from the target (when
	 * they exist).
	 */
	public void replaceAssociationIds() throws SAFRException
	{
		// ------------ check Views  ------------------
		Map<Integer, SAFRTransfer> vMap = importer.records.get(ViewTransfer.class);
		
		if (vMap == null)
		{
			return;
		}
		
		Collection<ComponentAssociation> existingAssocs = importer.existingAssociations
				.get(FileAssociation.class).values();

		// if we do this later then the envID is that of the target
		for(Map.Entry<Integer, SAFRTransfer> viewEnt : vMap.entrySet())
		{
			ViewTransfer view = (ViewTransfer)viewEnt.getValue();
			if (view.getExtractFileAssocId() != null && view.getExtractFileAssocId() > 0) {
				try {
					FileAssociationTransfer srcAssoc = (FileAssociationTransfer)
					importer.records.get(FileAssociationTransfer.class).get(view.getExtractFileAssocId());
										
					for(ComponentAssociation trgAssoc : existingAssocs) {
						if (trgAssoc.getAssociatingComponentId().equals(srcAssoc.getAssociatingComponentId())
								&& trgAssoc.getAssociatedComponentIdNum().equals(srcAssoc.getAssociatedComponentId())) {
							if (!srcAssoc.getAssociationId().equals(trgAssoc.getAssociationId())) {
								view.setExtractFileAssocId(trgAssoc.getAssociationId());
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
	
}
