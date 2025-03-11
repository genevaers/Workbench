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


import java.util.List;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

public class LookupPathStep extends SAFREnvironmentalComponent {

	private Integer sequenceNumber;
	private LookupPath parentLookupPath;
	private Integer sourceLRId;
	private LogicalRecord sourceLR;
	private ComponentAssociation sourceLRLFAssociation;
	private Integer targetXLRFileId;
	private ComponentAssociation targetLRLFAssociation;
	private LogicalRecord targetLR;
	private SAFRList<LookupPathSourceField> sourceFields = new SAFRList<LookupPathSourceField>();

	/**
	 * Use this Ctor to load an existing step from database.
	 * 
	 * @param trans
	 * @param lookupPath
	 * @throws SAFRException
	 * @throws DAOException
	 */
	LookupPathStep(LookupPathStepTransfer trans, LookupPath lookupPath)
			throws SAFRException, DAOException {
		super(trans);
		this.parentLookupPath = lookupPath;
		if (!trans.isForImport()) {
			// to get all the source Fields
			this.sourceFields.addAll(SAFRApplication.getSAFRFactory()
					.getLookUpPathStepSourceFields(this));
		}
	}

	/**
	 * Use this Ctor to create a new step.
	 * 
	 * @param parentLookupPath
	 * @param stepSeqNumber
	 * @param stepSourceLR
	 * @param stepSourceLRLFAssociation
	 * @throws DAOException
	 * @throws SAFRException
	 */
	LookupPathStep(LookupPath parentLookupPath, Integer stepSeqNumber,
			LogicalRecord stepSourceLR,
			ComponentAssociation stepSourceLRLFAssociation)
			throws SAFRException, DAOException {
		super(parentLookupPath.getEnvironmentId());
		this.parentLookupPath = parentLookupPath;
		this.sequenceNumber = stepSeqNumber;
		this.sourceFields = new SAFRList<LookupPathSourceField>();
		setSourceLR(stepSourceLR);
		setSourceLRLFAssociation(stepSourceLRLFAssociation);
		setTargetLRLFAssociation(null);
	}

	/**
	 * Use this Ctor to instantiate a step from an import file (XML).
	 * 
	 * TODO check that lr ids set from trans match the lr object args
	 * 
	 * @param trans
	 * @param lookupPath
	 * @param sourceLR
	 * @param targetLR
	 * @throws SAFRException
	 * @throws DAOException
	 */
	LookupPathStep(LookupPathStepTransfer trans, LookupPath lookupPath,
			LogicalRecord sourceLR, LogicalRecord targetLR,
			ComponentAssociation targetLRLFAssoc) throws SAFRException,
			DAOException {
		super(trans);
		this.parentLookupPath = lookupPath;
		this.sourceLR = sourceLR;
		this.targetLR = targetLR;
		this.targetLRLFAssociation = targetLRLFAssoc;
		if (!trans.isForImport()) {
			// to get all the source Fields
			this.sourceFields.addAll(SAFRApplication.getSAFRFactory()
					.getLookUpPathStepSourceFields(this));
		}
	}

	/**
	 * This method is used to get the sequence number of a step in lookup path.
	 * 
	 * @return the sequence number of a step in lookup path.
	 */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * This method is used to set the sequence number of a step in lookup path.
	 * 
	 * @param sequenceNumber
	 *            : The Integer value which is to be set as sequence number of a
	 *            step in lookup path.
	 */
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
        getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * This method is used to get the parent lookup path of a step.
	 * 
	 * @return the lookup path which is the parent of the step.
	 */
	public LookupPath getParentLookupPath() {
		return parentLookupPath;
	}

	/**
	 * This method is used to get the list of source fields related to a step in
	 * lookup path.
	 * 
	 * @return a list of source fields related to a step in lookup path.
	 */
	public SAFRList<LookupPathSourceField> getSourceFields() {
		return sourceFields;
	}

	@Override
	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		LookupPathStepTransfer trans = (LookupPathStepTransfer) safrTrans;
		// Set this object from the transfer object
		this.sequenceNumber = trans.getSequenceNumber();
		this.sourceLRId = trans.getSourceLRId();
		if (sourceLR != null && sourceLR.getId() != trans.getSourceLRId()) {
			this.sourceLR = null;
		}
		this.targetXLRFileId = trans.getTargetXLRFileId();
		if (targetLRLFAssociation != null
				&& targetLRLFAssociation.getAssociationId() != trans
						.getTargetXLRFileId()) {
			this.targetLRLFAssociation = null;
		}
	}

	@Override
	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		LookupPathStepTransfer trans = (LookupPathStepTransfer) safrTrans;
		// Set the transfer object from this object
		trans.setSequenceNumber(this.sequenceNumber);
		trans.setJoinId(this.parentLookupPath.getId());
		trans.setSourceLRId(this.sourceLRId);
		trans.setTargetXLRFileId(this.targetXLRFileId);
	}

	/**
	 * This method is used to get source LR of a step in lookup path.
	 * 
	 * @return the source LR of a step in lookup path.
	 * @throws SAFRException
	 *             when source Logical record specified for this lookup path
	 *             step is not found in database.
	 */
	public LogicalRecord getSourceLR() throws SAFRException {
		if (sourceLR == null) {
			if (sourceLRId != null && sourceLRId > 0) {
				this.sourceLR = SAFRApplication.getSAFRFactory()
						.getLogicalRecord(sourceLRId, getEnvironmentId());
			}
		}
		return sourceLR;
	}
	
	/**
	 * This method is used to get source LR Id of a step in lookup path. This information is used in migration to speed up processing
	 * 
	 * @return the source LR Id of a step in lookup path.
	 */
	
	public Integer getSourceLRId()
	{
		return sourceLRId;
	}

	/**
	 * This function is used to set source LR of a step. The Source LR should be
	 * explicitly set for only 1st step because for all other steps the source
	 * LR(s) is same as the target LR(s) of previous step.
	 * 
	 * @param logicalRecord
	 *            The Logical Record which is to be set as the source LR.
	 * @throws SAFRException
	 *             : when an existing source LR fields are used in this step's
	 *             source fields.
	 * @throws DAOException
	 */
	void setSourceLR(LogicalRecord logicalRecord) throws SAFRException,
			DAOException {
		if (getSourceLR() != null) {
			// first check the usage of this source LR in source fields.
			parentLookupPath.checkSourceLRUsage(this);
		}
		// source LR is not used.
		this.sourceLR = logicalRecord;
		if (sourceLR == null) {
			this.sourceLRId = 0;
		} else {
			this.sourceLRId = sourceLR.getId();
		}
        getParentLookupPath().markUpdated();
		markModified();
	}

	public void setSourceLR(LogicalRecord logicalRecord, int intValue) {
		// TODO Auto-generated method stub
		if (getSourceLR() != null && intValue==1) {
			// first check the usage of this source LR in source fields.
			
			parentLookupPath.checkSourceLRUsage(this);
		}
		// source LR is not used.
		this.sourceLR = logicalRecord;
		if (sourceLR == null) {
			this.sourceLRId = 0;
		} else {
			this.sourceLRId = sourceLR.getId();
		}
        getParentLookupPath().markUpdated();
		markModified();
	}
	
	public Integer getTargetLRLFAssociationId() {
	    return targetXLRFileId;
	}
	
    public void setTargetLRLFAssociationId(Integer targetXLRFileId) {
        this.targetXLRFileId = targetXLRFileId;
    }
	
	/**
	 * This method is used to get the association between the target LR and the
	 * logical file of a step.
	 * 
	 * @return a Component association between target LR and logical file of a
	 *         step.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public ComponentAssociation getTargetLRLFAssociation()
			throws SAFRException, DAOException {
		if (targetLRLFAssociation == null) {
			if (targetXLRFileId != null && targetXLRFileId > 0) {
				this.targetLRLFAssociation = SAFRAssociationFactory
						.getLogicalRecordToLogicalFileAssociation(
								targetXLRFileId, getTargetLR());
			}
		}
		return targetLRLFAssociation;
	}

	/**
	 * Sets target LR/LF combination of this step. This method will also change
	 * the source LR/LF combination of next step, if it exists.
	 * 
	 * @param LRLFAssoc
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void setTargetLRLFAssociation(ComponentAssociation LRLFAssoc)
			throws SAFRException, DAOException {
		parentLookupPath.changeStepTargetLRLFAssocistion(LRLFAssoc, this
				.getSequenceNumber());
		this.targetLRLFAssociation = LRLFAssoc;
		if (LRLFAssoc == null) {
			this.targetXLRFileId = 0;
		} else {
			this.targetXLRFileId = targetLRLFAssociation.getAssociationId();
		}
        getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * This method is used to get the target LR of a step in lookup path.
	 * 
	 * @return the target LR of a step in lookup path.
	 * @throws SAFRException
	 *             when target logical record specified for this step is not
	 *             found in database.
	 */
	public LogicalRecord getTargetLR() throws SAFRException {
		if (targetLR == null) {
			if (targetXLRFileId != null && targetXLRFileId > 0) {
				targetLR = SAFRApplication.getSAFRFactory()
						.getLogicalRecordFromLRLFAssociation(targetXLRFileId,
								this.getEnvironment().getId());
			}
		}
		return targetLR;
	}

	/**
	 * Sets target LR of this step. This method will also change the source LR
	 * of next step, if it exists.
	 * 
	 * @param targetLR
	 *            Target Logical Record.
	 * @throws SAFRException
	 *             If the target LR is used in any of the subsequent source
	 *             fields.
	 * @throws DAOException
	 */
	public void setTargetLR(LogicalRecord targetLR) throws SAFRException,
			DAOException {
		// If the target LR is in the last step then no need to change the
		// source LR of next step.
		int numberOfSteps = parentLookupPath.getLookupPathSteps()
				.getActiveItems().size();
		int seqNo = this.getSequenceNumber().intValue();
		if (numberOfSteps == seqNo) {
			this.targetLR = targetLR;
			setTargetLRLFAssociation(null);
		} else {
			// attempt to change Source lr of next step. This will trigger
			// dependency check which will throw SAFRValidationException if a
			// dependency is found.

			parentLookupPath.changeStepTarget(targetLR, this
					.getSequenceNumber());
			this.targetLR = targetLR;
			setTargetLRLFAssociation(null);
		}
        getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * This method is used to get the association between source LR of a step
	 * and the logical file.
	 * 
	 * @return the component association between source LR of a step and the
	 *         logical file.
	 */
	public ComponentAssociation getSourceLRLFAssociation() {
		return sourceLRLFAssociation;
	}

	public void setSourceLRLFAssociation(ComponentAssociation sourceLRLFAssociation)
			throws SAFRException, DAOException {
		if (this.sourceLRLFAssociation != null) {
			// first check the usage of this source LR/LF Association in source
			// fields.
			parentLookupPath.checkSourceLRUsage(this);
		}
		this.sourceLRLFAssociation = sourceLRLFAssociation;
		markModified();
	}

	/**
	 * Adds a new source field. The sequence number is calculated automatically
	 * based on the number of source fields already present in this step.
	 * 
	 * @param sourceFieldType
	 *            type of source field to be added. A value from Enum
	 *            {@link LookupPathSourceFieldType}
	 * @return
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LookupPathSourceField addSourceField(
			LookupPathSourceFieldType sourceFieldType) throws SAFRException,
			DAOException {
		LookupPathSourceField newSourceField = new LookupPathSourceField(this,
				sourceFieldType);
		newSourceField.setKeySeqNbr(sourceFields.getActiveItems().size() + 1);
		sourceFields.add(newSourceField);
        getParentLookupPath().markUpdated();
		markModified();
		return newSourceField;
	}

	/**
	 * Removes a source field. The sequence numbers of other source fields are
	 * recalculated.
	 * 
	 * @param sourceField
	 *            Source field to be removed.Cannot be null.
	 * 
	 * @throws NullPointerException
	 *             when sourceField to be deleted is null.
	 */
	public void deleteSourceField(LookupPathSourceField sourceField) {
		if (sourceField == null) {
			throw new NullPointerException(
					"Source field to be deleted cannot be null.");
		}
		sourceFields.remove(sourceField);
		int numberOfFields = sourceFields.getActiveItems().size();

		for (int i = 0; i < numberOfFields; i++) {
			((LookupPathSourceField) sourceFields.getActiveItems().get(i))
					.setKeySeqNbr(i + 1);
		}
        getParentLookupPath().markUpdated();
		markModified();
	}

	/**
	 * Checks the valid state of this step. A step is valid if its complete and
	 * the target length matches its source length.
	 * 
	 */
	public void checkValid() throws SAFRException {
		SAFRValidationException sve = new SAFRValidationException();
		try {
			checkComplete();
		} catch (SAFRValidationException e) {
			sve.setErrorMessages(Property.STEP, e.getErrorMessages());
		}
		if (getSourceLength() != getTargetLength()) {
			sve.setErrorMessage(Property.STEP,
					"The primary key is not matched properly for step  "
							+ getSequenceNumber());
		}
		if (!sve.getErrorMessages().isEmpty()) {
			throw sve;
		}
	}

	/**
	 * Checks the completion status of this step. A step is complete if it has a
	 * target LR/LF defined and the target LR's primary key length is greater
	 * than 0.
	 * 
	 */
	public void checkComplete() throws SAFRException {
		SAFRValidationException sve = new SAFRValidationException();
		if (this.targetXLRFileId == null || this.targetXLRFileId == 0) {
			sve.setErrorMessage(Property.SOURCE_FIELDS,
					"There is no valid Target File/LR pair selected for step "
							+ getSequenceNumber());
		}
		// CQ 8402. Nikita. 16/08/2010.
		// Don't display this message if a target LR is not selected
		if (this.targetXLRFileId != null && this.targetXLRFileId != 0 && getTargetLength() <= 0) {
			sve.setErrorMessage(Property.SOURCE_FIELDS,
					"The primary key for Target used in step "
							+ getSequenceNumber() + " has a width zero.");
		}
		if (!sve.getErrorMessages().isEmpty()) {
			throw sve;
		}
	}

	/**
	 * Returns the total length of all the source fields in this step.
	 * 
	 * @return int the total length of all the source fields in this step.
	 */
	public int getSourceLength() {
		int length = 0;
		for (SAFRPersistentObject field : sourceFields.getActiveItems()) {
			length += ((LookupPathSourceField) field).getLength();
		}
		return length;
	}

	/**
	 * Returns the total length of primary keys in target LR.
	 * 
	 * @return int
	 * @throws SAFRException
	 */
	public int getTargetLength() throws SAFRException {
		if (getTargetLR() != null) {
			return getTargetLR().getPrimayKeyLength().intValue();
		}
		return 0;
	}

	/**
	 * This method is used to move a source field, specified by a zero relative
	 * index, to one position up.
	 * 
	 * @param index
	 *            : The zero relative index of source field which is to be moved
	 *            one position up.
	 */
	public void moveFieldUp(int index) {
		List<LookupPathSourceField> activeItems = sourceFields.getActiveItems();
		if (index > (activeItems.size() - 1) || index <= 0) {
			return;
		}
		LookupPathSourceField tmpField = activeItems.get(index - 1);
		LookupPathSourceField item1 = activeItems.get(index);
		int moveToIndex = sourceFields.indexOf(tmpField);
		int moveFromIndex = sourceFields.indexOf(item1);
		Integer tempSequenceNumber = tmpField.getKeySeqNbr();
		tmpField.setKeySeqNbr(item1.getKeySeqNbr());
		item1.setKeySeqNbr(tempSequenceNumber);
		sourceFields.remove(moveToIndex);
		sourceFields.add(moveFromIndex, tmpField);
		markModified();
	}

	/**
	 * This method is used to move a Source Field, specified by a zero relative
	 * index, to one position down.
	 * 
	 * @param index
	 *            : The zero relative index of Source Field which is to be moved
	 *            one position down.
	 */
	public void moveFieldDown(int index) {
		List<LookupPathSourceField> activeItems = sourceFields.getActiveItems();
		if (index >= (activeItems.size() - 1) || index < 0) {
			return;
		}
		LookupPathSourceField tmpField = activeItems.get(index + 1);
		LookupPathSourceField item1 = activeItems.get(index);
		int moveToIndex = sourceFields.indexOf(tmpField);
		int moveFromIndex = sourceFields.indexOf(item1);
		Integer tempSequenceNumber = tmpField.getKeySeqNbr();
		tmpField.setKeySeqNbr(item1.getKeySeqNbr());
		item1.setKeySeqNbr(tempSequenceNumber);
		sourceFields.remove(moveToIndex);
		sourceFields.add(moveFromIndex, tmpField);
		markModified();
	}

	@Override
	public void store() throws SAFRException, DAOException {

		// Save Lookup Path Step
		LookupPathStepTransfer trans = new LookupPathStepTransfer();
		setTransferData(trans);
		trans = DAOFactoryHolder.getDAOFactory().getLookupPathStepDAO()
				.persistLookupPathStep(trans);
		setObjectData(trans);
	}

	/**
	 * This enum maintains the properties of lookup path step.
	 * 
	 */
	public enum Property {
		SOURCE_FIELDS, STEP, TARGETLF
	}

    private String getLFDependencyString() throws DAOException {
        String dependencies = "";
        
        // check for more than one PF association
        // means we cannot be the target of any lookup path steps
        DependentComponentTransfer depComp = DAOFactoryHolder.getDAOFactory().
            getLookupPathStepDAO().getAssociatedLFDependency(getEnvironmentId(), targetXLRFileId);
        
        if (depComp != null) {
            dependencies = "Target Logical File with multiple Physical Files: " + SAFRUtilities.LINEBREAK;
            dependencies += "    " + depComp.getName()
                + " [" + depComp.getId() + "]" + SAFRUtilities.LINEBREAK;
        }            
        return dependencies;
    }
	
	public void validate() throws SAFRException, DAOException {
		SAFRValidationException safrValidationException = new SAFRValidationException();
		
		// validate target
		String dependencyMessage = this.getLFDependencyString();
        if (!dependencyMessage.equals("")) {
            SAFRValidationToken token = new SAFRValidationToken(
                this,SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR);
            safrValidationException.setSafrValidationToken(token);            
            safrValidationException.setErrorMessage(Property.STEP,dependencyMessage);
        }       
        if(!isForImport() && !isForMigration() && isTargetLFisToken()) {
            safrValidationException.setErrorMessage(Property.TARGETLF,
                "    Target LF cannot be a token.");
        }
        
		
		// validate source fields
		List<LookupPathSourceField> activeSourceFields = this.getSourceFields().getActiveItems();
		String errorMessage = "";
		
		// source LR must be set
		if (getSourceLR() == null) {
			safrValidationException.setErrorMessage(Property.SOURCE_FIELDS,
			    "    Source Logical Record cannot be empty.");
		}
		
		// target LR-LF must be set
		if (getTargetLR() != null && getTargetLRLFAssociation() == null) {
			safrValidationException.setErrorMessage(Property.SOURCE_FIELDS,
			    "    Target Logical File cannot be empty if Target Logical Record is selected.");
		}
		
		// validate all source fields in the step
		for (int i = 0; i < activeSourceFields.size(); i++) {
			LookupPathSourceField activeSourceField = activeSourceFields.get(i);
			try {
				activeSourceField.validate();
			} catch (SAFRValidationException sve) {
				safrValidationException.setErrorMessages(
				    Property.SOURCE_FIELDS, sve.getErrorMessages());
			}
		}

		if (!safrValidationException.getErrorMessages().isEmpty()) {
			String msg = safrValidationException.getMessageString();
			if (parentLookupPath.getId() != null && parentLookupPath.getId() > 0 && 
			    (isForImport() || isForMigration()) ) {
				errorMessage = "Lookup Path " + "'"
						+ parentLookupPath.getName() + " ["
						+ parentLookupPath.getId() + "]' " + ":" + SAFRUtilities.LINEBREAK + "  Step "
						+ getSequenceNumber() + ":" + SAFRUtilities.LINEBREAK
						+ msg.substring(0, msg.length() - 1);
			}
			else {
                errorMessage = "Step " + getSequenceNumber() + ":" + SAFRUtilities.LINEBREAK
                + msg.substring(0, msg.length() - 1);               			    
			}
			safrValidationException.clearMessages();
			safrValidationException.setErrorMessage(Property.SOURCE_FIELDS,errorMessage);
			safrValidationException.setSafrValidationType(SAFRValidationType.ERROR);
			throw safrValidationException;
		}
	}

	private boolean isTargetLFisToken() {
	    boolean token = false;
	    //Now that we know how to do this it could be done as a query?
	    //Like the multiple PF check - or indeed merged with that process?
	    //since we iterate through the PFs of a target
	    //Or moved down into the LF where it probably belongs
	    ComponentAssociation lrlfAssoc = getTargetLRLFAssociation();
        if (lrlfAssoc != null) {
            Integer targetLF = lrlfAssoc.getAssociatedComponentIdNum();
            LogicalFile LF = SAFRApplication.getSAFRFactory().getLogicalFile(targetLF, this.getEnvironment().getId());
            SAFRList<FileAssociation> pfAssocs = LF.getPhysicalFileAssociations();
            for(FileAssociation lfpfAssoc : pfAssocs){
                if(token == false)
                {
                    PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(lfpfAssoc.getAssociatedComponentIdNum(), this.getEnvironment().getId());
                    Code pfType = pf.getFileTypeCode();
                    if(pfType.getDescription().equals("Token")) {
                        token = true;
                    }
                }
            }
        }
        return token;
    }

    @Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		// TODO Auto-generated method stub
		return null;
	}

}
