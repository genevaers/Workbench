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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.FileAssociation;

/**
 * This class will parse a &LT;File-Partition&GT; &LT;Record&GT; element into a
 * FileAssociationTransfer object.
 */
public class LFPFAssocRecordParser extends RecordParser {

	public LFPFAssocRecordParser(ComponentImporter importer) {
		super(importer);
	}

	@Override
	protected String getRecordExpression() {
		return "//LF-PF-Association/Record";
	}

	@Override
	protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException {

		String fieldValue;
		FileAssociationTransfer trans = new FileAssociationTransfer();

		trans.setEnvironmentId(importer.getTargetEnvironmentId());

		fieldValue = parseField("LFPFASSOCID", record);
		trans.setAssociationId(fieldToInteger("LFPFASSOCID", fieldValue));

		fieldValue = parseField("PHYFILEID", record);
		trans.setAssociatedComponentId(fieldToInteger("PHYFILEID",fieldValue));

		fieldValue = parseField("PARTSEQNBR", record);
		trans.setSequenceNo(fieldToInteger("PARTSEQNBR", fieldValue));

		fieldValue = parseField("LOGFILEID", record);
		trans.setAssociatingComponentId(fieldToInteger("LOGFILEID",fieldValue));

		// ignore CHILDFILEID and CHILDTYPE as it is hard coded to 0 while
		// creating associations.

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
		List<Integer> lfpfFKeys = new ArrayList<Integer>();

		Map<Integer, SAFRTransfer> fatMap = null;
		if (importer.records.containsKey(FileAssociationTransfer.class)) {
			fatMap = importer.records.get(FileAssociationTransfer.class);
		} else {
			fatMap = new HashMap<Integer, SAFRTransfer>(); // empty map
		}

		// Check for orphaned LFPF foreign keys.
		// XFILEPARTITIONID in View

		switch (importer.getComponentType()) {
		case ViewFolder:
		case View:
			// XFILEPARTITIONID in View
			Map<Integer, SAFRTransfer> vwtMap = null;
			if (importer.records.containsKey(ViewTransfer.class)) {
				vwtMap = importer.records.get(ViewTransfer.class);
			} else {
				vwtMap = new HashMap<Integer, SAFRTransfer>(); // empty map
			}
			for (SAFRTransfer tfr : vwtMap.values()) {
				ViewTransfer vwt = (ViewTransfer) tfr;
				Integer xFilePartitionId = vwt.getExtractFileAssocId();
				if (xFilePartitionId != null && xFilePartitionId > 0) {
					if (!fatMap.containsKey(xFilePartitionId)) {
						sve
								.setErrorMessage(
										importer.getCurrentFile().getName(),
										"View ["
												+ vwt.getId()
												+ "] refers to File Association["
												+ xFilePartitionId
												+ "] but this is not present in the import file.");
						throw sve;
					}
					lfpfFKeys.add(xFilePartitionId);
				}
			}
		default:
			// no action for other component types
		} // end switch

		// reverse check is not required as there may be some LFPF
		// associations which are not referred to by any component.

	}

	/**
	 * Checks the following rules that apply to LF/PF associations, which appear
	 * as File-Partition records in the import XML.
	 * <ol>
	 * <li>
	 * If the LF/PF pair is already associated by some other LFPF association
	 * row in the target Environment, the File-Partition record cannot be
	 * imported because this would violate the unique constraint on the LF/PF
	 * pair (composite key). Report the error and stop importing the file.
	 * <li>
	 * If the LFPF association already exists in the target Environment (that
	 * is, the association ID of the File-Partition record is the same as an
	 * existing association ID), then the existing details must be replaced by 
	 * the imported details, however:
	 * <ul>
	 * <li>
	 * if the LF ID is different (the PARENTFILEID of the existing association is
	 * different to the imported PARENTFILEID), then this is an error because 
	 * Import cannot simply change the Logical File in an existing LFPF association.
	 * <li> 
	 * if the PF ID is different (the CHILDPARTITIONID of the existing association
	 * is different to the imported CHILDPARTITIONID), meaning the existing PF ID will 
	 * be replaced by the imported PF ID, then if any existing Views depend on this 
	 * association for their Output LF/PF, issue a dependency warning and request
	 * confirmation to continue. 
	 * </ul>
	 * </ol>
	 * 
	 * @throws SAFRValidationException
	 *             if a rule is violated
	 * @throws SAFRException
	 */
	public void checkLFPFAssociations() throws SAFRException {

		if (!importer.records.containsKey(FileAssociationTransfer.class)) {
			return;
		}

		for (SAFRTransfer trans : importer.records.get(
				FileAssociationTransfer.class).values()) {
			FileAssociationTransfer fat = (FileAssociationTransfer) trans;
			if (importer.duplicateIdMap
					.containsKey(FileAssociationTransfer.class)
					&& importer.duplicateIdMap.get(
							FileAssociationTransfer.class).contains(
							fat.getAssociationId())) {

				FileAssociation dupAssoc = (FileAssociation) importer.existingAssociations
						.get(FileAssociation.class).get(fat.getAssociationId());

				// Rule: If the imported LF id of this association is different
				// to the one in target DB, then stop import. This is an error,
				// LF id shouldn't be automatically replaced in an association.

				boolean sameLF = dupAssoc.getAssociatingComponentId().equals(
						fat.getAssociatingComponentId());
				if (!sameLF) {
					// can't import
					SAFRValidationException sve = new SAFRValidationException();
					sve
							.setErrorMessage(
									importer.getCurrentFile().getName(),
									"File-Partition association ["
											+ fat.getAssociationId()
											+ "] imported with LF ["
											+ fat.getAssociatingComponentId()
											+ "] already exists in the target Environment for LF '"
											+ dupAssoc
													.getAssociatingComponentName()
											+ " ["
											+ dupAssoc
													.getAssociatingComponentId()
											+ "]'.");
					throw sve;
				} else {
					// Rule: if the imported PF id is different then import will
					// modify the PF id of an existing
					// association, warn the user about any Views that reference
					// this association as their Output LF/PF and prompt for
					// confirmation to proceed.
					if (!dupAssoc.getAssociatedComponentIdNum().equals(
							fat.getAssociatedComponentId())) {
						// different PF
						// check if there are any views which may be affected
						String dependencies = "";
						List<DependentComponentTransfer> dependentComponents;
						dependentComponents = DAOFactoryHolder.getDAOFactory()
								.getLogicalFileDAO()
								.getAssociatedPFViewDependencies(
										importer.getTargetEnvironmentId(),
										fat.getAssociationId());

						if (!dependentComponents.isEmpty()) {
							// dependencies found, warn user
							dependencies += "VIEWS :" + SAFRUtilities.LINEBREAK;
							for (DependentComponentTransfer depComp : dependentComponents) {
								dependencies += "    " + depComp.getName()
										+ " [" + depComp.getId() + "]" + SAFRUtilities.LINEBREAK;
							}

							StringBuffer buffer = new StringBuffer();
							buffer.append("File '");
							buffer.append(importer.getCurrentFile().getName());
							buffer.append("' contains File-Partition record [");
							buffer.append(fat.getAssociationId());
							buffer.append("] which associates LF [");
							buffer.append(fat.getAssociatingComponentId());
							buffer.append("] and PF [");
							buffer.append(fat.getAssociatedComponentId());
							buffer.append("]. LFPF association [");
							buffer.append(dupAssoc.getAssociationId());
							buffer
									.append("] already exists in the target environment, but it associates LF [");
							buffer.append(dupAssoc.getAssociatingComponentId());
							buffer.append("] and PF [");
							buffer.append(dupAssoc
									.getAssociatedComponentIdNum());
							buffer
									.append("]. This association will be replaced with the imported details, ");
							buffer
									.append("but note that the following Views are dependent on this association ");
							buffer.append("for their Output LF and PF. ");
							buffer
									.append(SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + "Do you want to continue importing this file?");
							String shortMsg = buffer.toString();

							if (!importer.getConfirmWarningStrategy()
									.confirmWarning("View dependencies",
											shortMsg, dependencies)) {
								// user doesn't want to continue with this
								// import
								SAFRValidationException sve = new SAFRValidationException();
								sve
										.setErrorMessage(importer
												.getCurrentFile().getName(),
												"Import cancelled on warning about View dependencies on LF/PF associations.");
								throw sve;
							}
						}
					}
				}

				// the existing association should be updated
				fat.setPersistent(true);

				// done, OK to import
			} 
		}
	}

}
