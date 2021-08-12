package com.ibm.safr.we.model;

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

import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFRComponent;

public class LookupPathSourceField extends SAFRField {

	private LookupPathSourceFieldType sourceFieldType;
	private Integer sourceXLRFLDId;
	private LRField sourceLRField;
	private Integer sourceXLRFileId;
	private ComponentAssociation sourceFieldLRLFAssociation;
	private LogicalRecord sourceFieldSourceLR;
	private Integer keySeqNbr;
	LookupPathStep parentLookupPathStep;
	private String symbolicName;
	private String sourceValue;

	private List<String> loadWarnings;

	/**
	 * Use this constructor to load an existing Source Field.
	 * 
	 * @param trans
	 * @param parentLookupPathStep
	 *            The step with which this source field is attached.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	LookupPathSourceField(LookupPathSourceFieldTransfer trans,
			LookupPathStep parentLookupPathStep) throws SAFRException,
			DAOException {
		super(trans);
		this.parentLookupPathStep = parentLookupPathStep;
		if (!trans.isForImport()) {
			loadWarnings = new ArrayList<String>();
			String optionalText = ", if required,";
			String text = "";

			if (!super.getLoadWarningProperties().isEmpty()) {
				for (SAFRField.Property property : super
						.getLoadWarningProperties()) {
					text = "Source Field "
							+ this.keySeqNbr
							+ " of step "
							+ this.parentLookupPathStep.getSequenceNumber()
							+ " does not have a valid "
							+ property.getText()
							+ ". Please select a valid "
							+ property.getText()
							+ (property == SAFRField.Property.DATA_TYPE ? ""
									: optionalText) + " before saving.";
					this.loadWarnings.add(text);
				}
			}
		}

	}

	/**
	 * Use this constructor for loading a Source Field for a view source.
	 * 
	 * @param trans
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LookupPathSourceField(LookupPathSourceFieldTransfer trans)
			throws SAFRException, DAOException {
		super(trans);

	}

	/**
	 * Use this constructor to create a new Source Field.
	 * 
	 * @param parentLookupPathStep
	 *            The step with which this source field is attached.
	 * @param sourceFieldType
	 * @throws SAFRException
	 * @throws DAOException
	 */
	LookupPathSourceField(LookupPathStep parentLookupPathStep,
			LookupPathSourceFieldType sourceFieldType) throws SAFRException,
			DAOException {
		super(parentLookupPathStep.getEnvironmentId());
		this.parentLookupPathStep = parentLookupPathStep;
		this.sourceFieldType = sourceFieldType;
	}

	/**
	 * This method is used to get the type of a source field.There can be three
	 * types of source field: LRField, Constant, Symbolic.
	 * 
	 * @return the type of the source field.
	 */
	public LookupPathSourceFieldType getSourceFieldType() {
		return sourceFieldType;
	}

	/**
	 * This method is used to set the type of a source field. Based on the type
	 * of source field the data is cleared for other type of source fields.
	 * 
	 * @param sourceFieldType
	 *            : one of LRField, Constant, Symbol which is to be set as the
	 *            type of the source field.
	 * @throws SAFRException
	 */
	public void setSourceFieldType(LookupPathSourceFieldType sourceFieldType)
			throws SAFRException {
		this.sourceFieldType = sourceFieldType;
		if (sourceFieldType == LookupPathSourceFieldType.LRFIELD) {
			this.setSymbolicName(null);
			this.setSourceValue(null);
		} else if (sourceFieldType == LookupPathSourceFieldType.CONSTANT) {
			this.setSourceLRField(null);
			this.setSymbolicName(null);
		} else if (sourceFieldType == LookupPathSourceFieldType.SYMBOL) {
			this.setSourceLRField(null);
		}
        getParentLookupPathStep().getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * @return The source Logical record field used as a source of this Key.
	 * @throws SAFRException
	 */
	public LRField getSourceLRField() throws SAFRException {
		// Source LR Field could be the one selected from the current step's
		// source LR or the one selected from the Source LRs of previous steps.
		// In case if the field is selected from a previous step, the source
		// LR-LF combination(sourceXLRFileId) of that step is also maintained.
		// If this ID is zero, the field is selected from the current step's
		// source LR or the Source LR of 1st step.
		if (sourceLRField == null) {
			if (this.sourceXLRFLDId != null && this.sourceXLRFLDId > 0) {
				if (this.sourceXLRFileId != null && this.sourceXLRFileId > 0) {
					// search in previous steps to find the field.
					LookupPath parentLookup = parentLookupPathStep
							.getParentLookupPath();
					for (int i = parentLookupPathStep.getSequenceNumber()
							.intValue() - 1; i > 0; i--) {
						LogicalRecord lr = parentLookup.getLookupPathSteps()
								.get(i).getSourceLR();
						for (LRField field : lr.getLRFields()) {
							if (field.getId().equals(sourceXLRFLDId)) {
								sourceLRField = field;
								sourceFieldSourceLR = lr;
								break;
							}
						}
					}
				} else {
					// search the current step to find the field.
					for (LRField field : parentLookupPathStep.getSourceLR()
							.getLRFields()) {
						if (field.getId().equals(sourceXLRFLDId)) {
							sourceLRField = field;
							sourceFieldSourceLR = parentLookupPathStep
									.getSourceLR();
							break;
						}
					}
					// search the first step to find the field.
					LookupPathStep firstStep = parentLookupPathStep
							.getParentLookupPath().getLookupPathSteps().get(0);
					for (LRField field : firstStep.getSourceLR().getLRFields()) {
						if (field.getId().equals(sourceXLRFLDId)) {
							sourceLRField = field;
							sourceFieldSourceLR = firstStep.getSourceLR();
							break;
						}
					}
				}
			}
		}
		return sourceLRField;
	}

	/**
	 * This method is used to set the source LR of a source field. This method
	 * also copies the properties of the LR Field into source field.
	 * 
	 * @param sourceLRField
	 *            : The LRField which is to be set as the source of a source
	 *            field.
	 * @throws SAFRException
	 */
	public void setSourceLRField(LRField sourceLRField) throws SAFRException {
		this.sourceLRField = sourceLRField;
		if (sourceLRField == null) {
			this.sourceXLRFLDId = null;
		} else {
			this.sourceXLRFLDId = this.sourceLRField.getId();
			// copy properties of the LR Field in the source field.
			this.setDataTypeCode(sourceLRField.getDataTypeCode());
			this.setLength(sourceLRField.getLength());
			this.setDateTimeFormatCode(sourceLRField
					.getDateTimeFormatCode());
			this.setScaling(sourceLRField.getScaling());
			this.setDecimals(sourceLRField.getDecimals());
			this.setSigned(sourceLRField.isSigned());
			this.setNumericMaskCode(sourceLRField.getNumericMaskCode());
		}
        getParentLookupPathStep().getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * This method is used to get the source LR of the source field.
	 * 
	 * @return the source LR of the source field.
	 * @throws SAFRException
	 */
	public LogicalRecord getSourceFieldSourceLR() throws SAFRException {
		if (sourceFieldSourceLR == null) {
			// this will load the Source LR too.
			getSourceLRField();
		}
		return sourceFieldSourceLR;
	}

	/**
	 * This method is used to set the Source LR of a source field.
	 * 
	 * @param logicalRecord
	 *            : The logical record which is to be set as source LR of the
	 *            source field.
	 */
	public void setSourceFieldSourceLR(LogicalRecord logicalRecord) {
		this.sourceFieldSourceLR = logicalRecord;
        getParentLookupPathStep().getParentLookupPath().markUpdated();
		markModified();
	}

	public Integer getSourceFieldLRLFAssociationId() {
	    return sourceXLRFileId;
	}
	
    public void setSourceFieldLRLFAssociationId(Integer sourceXLRFileId) {
        this.sourceXLRFileId = sourceXLRFileId;
    }
	
	/**
	 * @return the LR-LF association with which the source LR Field is attached.
	 */
	public ComponentAssociation getSourceFieldLRLFAssociation() {
		if (sourceFieldLRLFAssociation == null) {
			if (sourceXLRFileId != null && sourceXLRFileId > 0) {
				// search in previous steps to find the LR LF association.
				LookupPath parentLookup = parentLookupPathStep
						.getParentLookupPath();
				for (int i = parentLookupPathStep.getSequenceNumber()
						.intValue() - 1; i > 0; i--) {
					ComponentAssociation assoc = parentLookup
							.getLookupPathSteps().get(i)
							.getSourceLRLFAssociation();
					if (assoc.getAssociationId().equals(sourceXLRFileId)) {
						sourceFieldLRLFAssociation = assoc;
						break;
					}
				}
			}
		}
		return sourceFieldLRLFAssociation;
	}

	/**
	 * This method is used to set the component association between source
	 * Field's LR and logical file.
	 * 
	 * @param sourceFieldLRLFAssociation
	 *            : The component association which is to be set between source
	 *            Field's LR and logical file.
	 */
	public void setSourceFieldLRLFAssociation(
			ComponentAssociation sourceFieldLRLFAssociation) {
		this.sourceFieldLRLFAssociation = sourceFieldLRLFAssociation;
		if (sourceFieldLRLFAssociation == null) {
			this.sourceXLRFileId = null;
		} else {
			this.sourceXLRFileId = this.sourceFieldLRLFAssociation
					.getAssociationId();
		}
        getParentLookupPathStep().getParentLookupPath().markUpdated();
		markModified();
	}

	void setKeySeqNbr(Integer keySeqNbr) {
		if (keySeqNbr == null) {
			this.keySeqNbr = 0;
		} else {
			this.keySeqNbr = keySeqNbr;
		}
		getParentLookupPathStep().getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * This method is used to get the sequence number of the primary key of
	 * source field.
	 * 
	 * @return the sequence number of the primary key of source field.
	 */
	public Integer getKeySeqNbr() {
		return keySeqNbr;
	}

	/**
	 * This method is used to get the symbolic name of source field of type
	 * symbolic.
	 * 
	 * @return the symbolic name of the source field of type symbolic.
	 */
	public String getSymbolicName() {
		return symbolicName;
	}

	/**
	 * This method is used to set the symbolic name of the source field of type
	 * symbolic. If the type of the source field is not Symbolic then this
	 * setter will not take effect i.e, the value for symbolic name will not be
	 * persisted.
	 * 
	 * @param symbolicName
	 *            : The name which is to be set as symbolic name of a source
	 *            field of type symbolic.
	 */
	public void setSymbolicName(String symbolicName) {
		if (this.sourceFieldType != LookupPathSourceFieldType.SYMBOL) {
			return;
		}
		this.symbolicName = symbolicName;
        getParentLookupPathStep().getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * This method is used to get the source value of source field of type
	 * symbolic or constant.
	 * 
	 * @return the source value of source field of type symbolic or constant.
	 */
	public String getSourceValue() {
		return sourceValue;
	}

	/**
	 * This method is used to set the source value of a source field of type
	 * symbol or constant. If the source field is not of type Symbolic or
	 * constant then this setter will not take effect i.e, the value for source
	 * value will not be persisted.
	 * 
	 * @param sourceValue
	 *            : the value which is to be set as source value of a source
	 *            field of type symbol or constant.
	 */
	public void setSourceValue(String sourceValue) {
		if ((this.sourceFieldType == LookupPathSourceFieldType.CONSTANT)
				|| (this.sourceFieldType == LookupPathSourceFieldType.SYMBOL)) {
			this.sourceValue = sourceValue;
	        getParentLookupPathStep().getParentLookupPath().markUpdated();
			markModified();
		} else {
			return;
		}
	}

	/**
	 * This method is used to get the parent lookup path step of the source
	 * field.
	 * 
	 * @return the lookup path step which is the parent step of source field.
	 */
	public LookupPathStep getParentLookupPathStep() {
		return parentLookupPathStep;
	}

	@Override
	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		LookupPathSourceFieldTransfer trans = (LookupPathSourceFieldTransfer) safrTrans;
		// Set this object from the transfer object
		this.sourceFieldType = trans.getSourceFieldType();
		this.sourceXLRFLDId = trans.getSourceXLRFLDId();
		if (sourceLRField != null
				&& sourceLRField.getId() != trans.getSourceXLRFLDId()) {
			this.sourceLRField = null;
			this.sourceFieldSourceLR = null;
		}

		this.sourceXLRFileId = trans.getSourceXLRFileId();
		if (sourceFieldLRLFAssociation != null
				&& sourceFieldLRLFAssociation.getAssociationId() != trans
						.getSourceXLRFileId()) {
			sourceFieldLRLFAssociation = null;
		}
		this.keySeqNbr = trans.getKeySeqNbr();
		this.sourceValue = trans.getSourceValue();
		this.symbolicName = trans.getSymbolicName();
	}

	@Override
	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		LookupPathSourceFieldTransfer trans = (LookupPathSourceFieldTransfer) safrTrans;
		// Set the transfer object from this object. The data is set based on a
		// particular source field type
		trans.setSourceFieldType(this.sourceFieldType);
		trans.setKeySeqNbr(this.keySeqNbr);
		trans.setLookupPathStepId(this.parentLookupPathStep.getId());
		trans.setSourceJoinId(this.parentLookupPathStep.getParentLookupPath()
				.getId());
		if (this.sourceFieldType == LookupPathSourceFieldType.LRFIELD) {
			trans.setSourceXLRFLDId(this.sourceXLRFLDId);
			trans.setSourceXLRFileId(this.sourceXLRFileId);
			trans.setSymbolicName(null);
			trans.setSourceValue(null);
		}
		if (this.sourceFieldType == LookupPathSourceFieldType.CONSTANT) {
			trans.setSourceXLRFLDId(null);
			trans.setSourceXLRFileId(null);
			trans.setSourceValue(this.sourceValue);
			trans.setSymbolicName(null);
		}
		if (this.sourceFieldType == LookupPathSourceFieldType.SYMBOL) {
			trans.setSourceXLRFLDId(null);
			trans.setSourceXLRFileId(null);
			trans.setSymbolicName(this.symbolicName);
			trans.setSourceValue(this.sourceValue);
		}

	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO Auto-generated method stub

	}

	/**
	 * This enum maintains the properties of lookup path source field.
	 * 
	 */
	public enum Property {
		DATATYPE, LR_FIELD, CONSTANT_VALUE, SYMBOL_NAME
	}

	public void validate() throws SAFRException, DAOException {
		SAFRValidationException safrValidationException = new SAFRValidationException();
		// call validate of SAFRField.
		try {
			super.validate();
		} catch (SAFRValidationException e) {
			ArrayList<String> errorMsgs = e.getErrorMessages();
			ArrayList<String> modifiedErrorMsgs = new ArrayList<String>();
			for (int i = 0; i < errorMsgs.size(); i++) {
				String errorMessage = "    Field " + getKeySeqNbr() + ": "
						+ errorMsgs.get(i);
				modifiedErrorMsgs.add(errorMessage);
			}
			if (!modifiedErrorMsgs.isEmpty()) {
				safrValidationException.setErrorMessages(Property.DATATYPE,
						modifiedErrorMsgs);
			}
		}
		if (this.sourceFieldType.equals(LookupPathSourceFieldType.LRFIELD)) {
			if (sourceXLRFLDId == null || sourceXLRFLDId == 0) {
				safrValidationException.setErrorMessage(Property.LR_FIELD,
						"    Field " + getKeySeqNbr()
								+ ": Logical Record Field cannot be empty.");
			}
		}
		if (this.sourceFieldType.equals(LookupPathSourceFieldType.CONSTANT)) {
			if (this.getSourceValue() == null
					|| this.getSourceValue().equals("")) {
				safrValidationException.setErrorMessage(
						Property.CONSTANT_VALUE, "    Field " + getKeySeqNbr()
								+ ": Constant source value cannot be empty.");
			}
		}
		if (this.sourceFieldType.equals(LookupPathSourceFieldType.SYMBOL)) {
			if (this.getSymbolicName() == null
					|| this.getSymbolicName().equals("")) {
				safrValidationException.setErrorMessage(Property.SYMBOL_NAME,
						"    Field " + getKeySeqNbr()
								+ ": Symbol name cannot be empty");
			}
		}
		if (!safrValidationException.getErrorMessages().isEmpty()) {
			safrValidationException
					.setSafrValidationType(SAFRValidationType.ERROR);
			throw safrValidationException;
		}
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		// TODO Auto-generated method stub
		return null;
	}

	List<String> getLoadWarnings() {
		return loadWarnings;
	}

    @Override
    public boolean equals(Object rightObj) {
        if (rightObj instanceof LookupPathSourceField) {
            LookupPathSourceField rightComp = (LookupPathSourceField) rightObj;
            boolean equals = getEnvironmentId().equals(rightComp.getEnvironmentId()) && 
                             getId().equals(rightComp.getId()) &&
                             keySeqNbr.equals(rightComp.keySeqNbr);
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = getEnvironmentId() ^ getId() ^ keySeqNbr;
        return res;        
    }   
	
}
