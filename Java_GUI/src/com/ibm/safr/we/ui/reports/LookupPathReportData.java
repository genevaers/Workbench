package com.ibm.safr.we.ui.reports;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * The class is used for displaying the LookupPath report data.
 * 
 */
public class LookupPathReportData implements IReportData {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.LookupPathReportData");

	private Map<Integer, LookupPathRD> lookupPathPropertiesMap = new HashMap<Integer, LookupPathRD>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> lookupIDs = new ArrayList<Integer>();

	/**
	 * Constructor for class LookUpPathReportData which accepts ids of the
	 * LookupPaths whose report is to be generated.
	 * 
	 * @param lookupPathIdsList
	 *            list of ids of the LookupPaths whose report is to be
	 *            generated.
	 * @throws SAFRException
	 */
	public LookupPathReportData(List<Integer> lookupPathIdsList)
			throws SAFRException {
		for (Integer lookupPathId : lookupPathIdsList) {
			loadData(null, lookupPathId);
		}
	}

	/**
	 * Constructor for class LookUpPathReportData which accepts a lookup model
	 * object of the LookupPath whose report is to be generated.
	 * 
	 * @param lookupPath
	 *            LookupPath whose report is to be generated.
	 * @throws SAFRException
	 */
	public LookupPathReportData(LookupPath lookupPath) throws SAFRException {
		loadData(lookupPath, lookupPath.getId());
	}

	private void loadData(LookupPath lookupPath, Integer id) {
		try {
			if (lookupPath == null) {
				lookupPath = SAFRApplication.getSAFRFactory().getLookupPath(id);
			}
			LookupPathRD lookupPathProperties = new LookupPathRD(lookupPath);
			lookupPathPropertiesMap.put(id, lookupPathProperties);
			// CQ 8165. Nikita. 01/07/2010
			// retain the original order in which the list of Lookup Path ids
			// was passed
			lookupIDs.add(id);
		} catch (SAFRDependencyException e) {
			errorMsgList
					.add("Lookup Path: "
							+ id
							+ "- Unable to load as below dependent components are inactive:" + SAFRUtilities.LINEBREAK
							+ e.getDependencyString(4));
		} catch (Exception ex) {
            logger.log(Level.SEVERE, "Unable to load", ex);
			errorMsgList.add("Lookup Path: " + id
					+ " - Unable to load due to below unexpected error:" + SAFRUtilities.LINEBREAK + "    "
					+ ex.toString());
		}
	}

	/************************************************************************************************************************/

	/**
	 * This method returns the {@link LookupPathRD} object used to display data
	 * of the specified LookupPath id.
	 * 
	 * @param lookupPathId
	 *            of the LookupPath whose {@link LookupPathRD} object is to be
	 *            returned.
	 * @return the {@link LookupPathRD} object of the specified LookupPath id.
	 */
	public List<LookupPathRD> getLookupPathReportData() {
		List<LookupPathRD> lkPropertiesList = new ArrayList<LookupPathRD>();
		// CQ 8165. Nikita. 01/07/2010
		// generate report based on original order in which Lookup Path ids were
		// passed
		for (Integer id : lookupIDs) {
			lkPropertiesList.add(lookupPathPropertiesMap.get(id));
		}
		return lkPropertiesList;
	}

	/************************************************************************************************************************/

	/**
	 * This method is used to get the list of LookupStepRD.
	 * 
	 * @param LKId
	 *            : the ID of the lookup for which the list is to be retrieved.
	 * @return the list of LookupStepRD
	 */
	public List<LookupStepRD> getLookupPathStepListReportData(Integer LKId) {
		if (!lookupPathPropertiesMap.isEmpty()) {
			LookupPathRD lkPropertyReport = lookupPathPropertiesMap.get(LKId);
			return lkPropertyReport.getLookupPathStepRDList();
		}
		return null;
	}

	/************************************************************************************************************************/

	/**
	 * This method is used to get the properties of Lookup Path source and
	 * target fields.
	 * 
	 * @param LkId
	 *            : the ID of the lookup for which the list is to be retrieved.
	 * @param stepNo
	 *            : the number of the lookup path step for which the list of
	 *            source fields id to be retrieved.
	 * @return the list of LookupPathSourceTargetFieldRD.
	 */
	public List<LookupPathSourceTargetFieldRD> getLookupSourceTargetFieldListReportData(
			Integer LkId, Integer stepNo) {
		List<LookupPathSourceTargetFieldRD> lookupPathSourceTargetFields = new ArrayList<LookupPathSourceTargetFieldRD>();
		LookupStepRD lookupPathStepRD = null;
		if (!lookupPathPropertiesMap.isEmpty()) {
			List<LookupStepRD> lookupStepRDList = getLookupPathStepListReportData(LkId);
			for (LookupStepRD lookupStepRD : lookupStepRDList) {
				if (lookupStepRD.getLookupPathStepNo().equals(stepNo)) {
					lookupPathStepRD = lookupStepRD;
				}
			}
			lookupPathSourceTargetFields.addAll(lookupPathStepRD
					.getLookupPathSourceTargetFieldRDList());

			return lookupPathSourceTargetFields;
		}
		return null;
	}

	/************************************************************************************************************************/

	/**
	 * A class used to display data of the LookupPath in the LookupPath report.
	 * 
	 */
	public class LookupPathRD {

		private Integer lookupPathId;
		private String lookupPathName;
		private Boolean valid;
		private List<LookupStepRD> lookupStepRDList;

		/**
		 * Constructor for the LookupPathRD.
		 * 
		 * @param lookupPath
		 *            object for which LookupPathRD is to be created.
		 * @throws SAFRException
		 */
		public LookupPathRD(LookupPath lookupPath) throws SAFRException {
			this.lookupPathId = lookupPath.getId();
			this.lookupPathName = lookupPath.getName();
			this.valid = lookupPath.isValid();

			List<LookupStepRD> lookupStepRDList = new ArrayList<LookupStepRD>();
			for (LookupPathStep lookupPathStep : lookupPath
					.getLookupPathSteps().getActiveItems()) {

				lookupStepRDList.add(new LookupStepRD(lookupPathStep));
			}

			this.lookupStepRDList = lookupStepRDList;
		}

		public Integer getLookupPathId() {
			return lookupPathId;
		}

		public String getLookupPathName() {
			if (this.lookupPathName == null) {
				this.lookupPathName = "";
			}
			return UIUtilities.getComboString(lookupPathName, lookupPathId);
		}

		public String getValid() {
			if (valid) {
				return "Active";
			} else {
				return "Inactive";
			}
		}

		public List<LookupStepRD> getLookupPathStepRDList() {
			return lookupStepRDList;
		}

	}

	/************************************************************************************************************************/

	/**
	 * This class is used to store data of the LookupPath step used to display
	 * in the LookupPath report.
	 * 
	 */
	public class LookupStepRD {
		private Integer lookupPathStepNo;
		private List<LookupPathSourceTargetFieldRD> lookupPathSourceTargetFieldRDList;
		private Integer lookupSourceFieldsTotalLength;
		private Integer lookupTargetFieldsTotalLength;
		private String lookupStepSourceLRLF;
		private String lookupStepTargetLRLF;

		/**
		 * Constructor of the LookupPathStepRD class.
		 * 
		 * @param lookupPathStep
		 *            object for which LookupStepRD is to be created.
		 * @throws SAFRException
		 */
		public LookupStepRD(LookupPathStep lookupPathStep) throws SAFRException {
			this.lookupPathStepNo = lookupPathStep.getSequenceNumber();
			String lookupStepSourceLR = null;
			String lookupStepTargetLF = null;
			String lookupStepTargetLR = null;
			if (lookupPathStep.getSourceLR() != null) {
				String stepSourceLRName = lookupPathStep.getSourceLR()
						.getName();
				Integer stepSourceLRID = lookupPathStep.getSourceLR().getId();
				lookupStepSourceLR = UIUtilities.getComboString(
						stepSourceLRName, stepSourceLRID);
			}
			if (lookupPathStep.getTargetLRLFAssociation() != null) {
				String stepTargetLRName = lookupPathStep
						.getTargetLRLFAssociation()
						.getAssociatingComponentName();
				Integer stepTargetLRID = lookupPathStep
						.getTargetLRLFAssociation().getAssociatingComponentId();
				String stepTargetLFName = lookupPathStep
						.getTargetLRLFAssociation()
						.getAssociatedComponentName();
				Integer stepTargetLFID = lookupPathStep
						.getTargetLRLFAssociation()
						.getAssociatedComponentIdNum();
				lookupStepTargetLF = UIUtilities.getComboString(
						stepTargetLFName, stepTargetLFID);
				lookupStepTargetLR = UIUtilities.getComboString(
						stepTargetLRName, stepTargetLRID);
			}

			this.lookupStepSourceLRLF = lookupStepSourceLR;
			if (lookupStepTargetLR != null && lookupStepTargetLF != null) {
				this.lookupStepTargetLRLF = lookupStepTargetLR + "."
						+ lookupStepTargetLF;
			} else {
				this.lookupStepTargetLRLF = "";
			}
			List<LRField> lookupPathTargetFields = new ArrayList<LRField>();
			if (lookupPathStep.getTargetLR() != null) {
				for (LRField targetField : lookupPathStep.getTargetLR()
						.getLRFields().getActiveItems()) {
					if (targetField.getKeyType().equals(
							LRFieldKeyType.PRIMARYKEY)) {
						lookupPathTargetFields.add(targetField);
					}
				}
			}
			List<LookupPathSourceTargetFieldRD> lookupPathSourceTargetFieldRDList = new ArrayList<LookupPathSourceTargetFieldRD>();
			if (lookupPathStep.getSourceFields().getActiveItems().size() >= lookupPathTargetFields
					.size()) {
				for (int i = 0; i < lookupPathStep.getSourceFields()
						.getActiveItems().size(); i++) {
					LookupPathSourceField lkSourceField = lookupPathStep
							.getSourceFields().getActiveItems().get(i);
					if (lookupPathTargetFields.size() <= i) {
						lookupPathSourceTargetFieldRDList
								.add(new LookupPathSourceTargetFieldRD(
										lkSourceField, null));
					} else {
						lookupPathSourceTargetFieldRDList
								.add(new LookupPathSourceTargetFieldRD(
										lkSourceField, lookupPathTargetFields
												.get(i)));
					}
				}
			} else {
				for (int i = 0; i < lookupPathTargetFields.size(); i++) {
					LRField lkTargetField = lookupPathTargetFields.get(i);
					if (lookupPathStep.getSourceFields().getActiveItems()
							.size() <= i) {
						lookupPathSourceTargetFieldRDList
								.add(new LookupPathSourceTargetFieldRD(null,
										lkTargetField));
					} else {
						lookupPathSourceTargetFieldRDList
								.add(new LookupPathSourceTargetFieldRD(
										lookupPathStep.getSourceFields().get(i),
										lkTargetField));
					}
				}
			}

			this.lookupPathSourceTargetFieldRDList = lookupPathSourceTargetFieldRDList;
			lookupSourceFieldsTotalLength = lookupPathStep.getSourceLength();
			lookupTargetFieldsTotalLength = lookupPathStep.getTargetLength();

		}

		public Integer getLookupPathStepNo() {
			return lookupPathStepNo;
		}

		public List<LookupPathSourceTargetFieldRD> getLookupPathSourceTargetFieldRDList() {
			return lookupPathSourceTargetFieldRDList;
		}

		public Integer getLookupSourceFieldsTotalLength() {
			return lookupSourceFieldsTotalLength;
		}

		public Integer getLookupTargetFieldsTotalLength() {
			return lookupTargetFieldsTotalLength;
		}

		public String getLookupStepSourceLRLF() {
			return lookupStepSourceLRLF;
		}

		public String getLookupStepTargetLRLF() {
			return lookupStepTargetLRLF;
		}

	}

	/************************************************************************************************************************/

	/**
	 * This class is used to store data of the LookupPath source field, which is
	 * used to display in the LookupPath report.
	 * 
	 */
	public class LookupPathSourceTargetFieldRD {
		private String sFieldType;
		private String sLogicalFileName;
		private String sLogicalRecordName;
		private String sSourceFieldName;
		private Integer sFieldId;
		private Integer sLength;
		private String tFieldName;
		private Integer tFieldId;
		private Integer tFieldLength;

		/**
		 * Constructor of the class LookupPathSourceFieldRD.
		 * 
		 * @param field
		 *            object for which LookupPathSourceFieldRD is to be created.
		 * @throws SAFRException
		 */
		public LookupPathSourceTargetFieldRD(LookupPathSourceField sourceField,
				LRField targetField) throws SAFRException {
			if (sourceField != null && targetField != null) {
				if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.CONSTANT)) {
					this.sFieldType = "Constant";
				} else if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.LRFIELD)) {
					this.sFieldType = "LR Field";
				} else if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.SYMBOL)) {
					this.sFieldType = "Symbol";
				}

				if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.LRFIELD)) {
					if (sourceField.getSourceFieldLRLFAssociation() != null) {
						this.sLogicalFileName = sourceField
								.getSourceFieldLRLFAssociation()
								.getAssociatedComponentName();
					} else {
						this.sLogicalFileName = "";
					}
					if (sourceField.getSourceFieldSourceLR() != null) {
						this.sLogicalRecordName = sourceField
								.getSourceFieldSourceLR().getName();
					} else {
						this.sLogicalRecordName = "";
					}
					if (sourceField.getSourceLRField() != null) {
						this.sSourceFieldName = sourceField.getSourceLRField()
								.getName();
					} else {
						this.sSourceFieldName = "";
					}
					if (sourceField.getSourceLRField() != null) {
						this.sFieldId = sourceField.getSourceLRField().getId();
					}
					this.sLength = sourceField.getLength();
				} else if ((sourceField.getSourceFieldType()
						.equals(LookupPathSourceFieldType.CONSTANT))
						|| (sourceField.getSourceFieldType()
								.equals(LookupPathSourceFieldType.SYMBOL))) {

					this.sLogicalFileName = "";
					this.sLogicalRecordName = "";
					if (sourceField.getSourceFieldType().equals(
							LookupPathSourceFieldType.SYMBOL)) {
						this.sSourceFieldName = sourceField.getSymbolicName();
					} else {
						this.sSourceFieldName = "CONSTANT";
					}
					this.sFieldId = 0;
					this.sLength = sourceField.getLength();
				}
				this.tFieldName = targetField.getName();
				this.tFieldId = targetField.getId();
				this.tFieldLength = targetField.getLength();
			} else if (sourceField == null && targetField != null) {
				this.sFieldType = "";
				this.sLogicalFileName = "";
				this.sLogicalRecordName = "";
				this.sSourceFieldName = "";
				this.tFieldName = targetField.getName();
				this.tFieldId = targetField.getId();
				this.tFieldLength = targetField.getLength();
			} else if (sourceField != null && targetField == null) {
				if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.CONSTANT)) {
					this.sFieldType = "Constant";
				} else if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.LRFIELD)) {
					this.sFieldType = "LR Field";
				} else if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.SYMBOL)) {
					this.sFieldType = "Symbol";
				}

				if (sourceField.getSourceFieldType().equals(
						LookupPathSourceFieldType.LRFIELD)) {
					if (sourceField.getSourceFieldLRLFAssociation() != null) {
						this.sLogicalFileName = sourceField
								.getSourceFieldLRLFAssociation()
								.getAssociatedComponentName();
					} else {
						this.sLogicalFileName = "";
					}
					if (sourceField.getSourceFieldSourceLR() != null) {
						this.sLogicalRecordName = sourceField
								.getSourceFieldSourceLR().getName();
					} else {
						this.sLogicalRecordName = "";
					}
					if (sourceField.getSourceLRField() != null) {
						this.sSourceFieldName = sourceField.getSourceLRField()
								.getName();
					} else {
						this.sSourceFieldName = "";
					}
					if (sourceField.getSourceLRField() != null) {
						this.sFieldId = sourceField.getSourceLRField().getId();
					}
					this.sLength = sourceField.getLength();
				} else if ((sourceField.getSourceFieldType()
						.equals(LookupPathSourceFieldType.CONSTANT))
						|| (sourceField.getSourceFieldType()
								.equals(LookupPathSourceFieldType.SYMBOL))) {

					this.sLogicalFileName = "";
					this.sLogicalRecordName = "";
					if (sourceField.getSourceFieldType().equals(
							LookupPathSourceFieldType.SYMBOL)) {
						this.sSourceFieldName = sourceField.getSymbolicName();
					} else {
						this.sSourceFieldName = "CONSTANT";
					}
					this.sFieldId = 0;
					this.sLength = sourceField.getLength();
				}
				this.tFieldName = "";
			}

		}

		public String getSFieldType() {
			return sFieldType;
		}

		public String getSLogicalFileName() {
			return sLogicalFileName;
		}

		public String getSLogicalRecordName() {
			return sLogicalRecordName;
		}

		public String getSSourceFieldName() {
			return sSourceFieldName;
		}

		public Integer getSFieldId() {
			return sFieldId;
		}

		public Integer getSLength() {
			return sLength;
		}

		public String getTFieldName() {
			return tFieldName;
		}

		public Integer getTFieldId() {
			return tFieldId;
		}

		public Integer getTFieldLength() {
			return tFieldLength;
		}

	}

	/************************************************************************************************************************/

	public List<String> getErrors() {
		return errorMsgList;
	}

	public boolean hasData() {
		return (!this.lookupPathPropertiesMap.isEmpty());
	}

	/************************************************************************************************************************/

}
