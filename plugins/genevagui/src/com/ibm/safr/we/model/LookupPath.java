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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.LookupPathSourceFieldType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRActivatedComponent;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

public class LookupPath extends SAFRActivatedComponent {

	private Integer sourceLRId;
	private LogicalRecord sourceLR;
	private Integer targetXLRFileId;
	private ComponentAssociation targetLRLFAssociation;
	private Boolean validInd;
	private SAFRList<LookupPathStep> lookupPathSteps = new SAFRList<LookupPathStep>();
	private Set<Integer> deactivatedViewList = new HashSet<Integer>();
    private Set<Integer> migExViewList = new HashSet<Integer>();
    private Integer dummyStepId = 1;

	List<String> loadWarnings;

	LookupPath(Integer environmentId) throws SAFRException, DAOException {
		super(environmentId);
		lookupPathSteps.add(new LookupPathStep(this, 1, null, null));
		this.sourceLRId = 0;
		this.targetXLRFileId = 0;
		setValid(false); // new path is invalid by default
	}

	LookupPath(LookupPathTransfer trans) throws SAFRException, DAOException {
		super(trans);
		if (!trans.isForImport()) {
			// Check for inactive dependencies, throw SAFRDependencyException if
			// its
			// found.
			Map<ComponentType, List<DependentComponentTransfer>> inactiveLRs = new HashMap<ComponentType, List<DependentComponentTransfer>>();

			List<DependentComponentTransfer> deps = DAOFactoryHolder
					.getDAOFactory().getLookupDAO()
					.getLookupPathInactiveLogicalRecordsDependencies(
							trans.getEnvironmentId(), trans.getId());
			inactiveLRs.put(ComponentType.LogicalRecord, deps);

			if (deps != null && !deps.isEmpty()) {
				throw new SAFRDependencyException(inactiveLRs);
			}
			this.lookupPathSteps.addAll(SAFRApplication.getSAFRFactory()
					.getLookupPathSteps(this));

			loadWarnings = new ArrayList<String>();
			for (LookupPathStep step : this.lookupPathSteps.getActiveItems()) {
				for (LookupPathSourceField field : step.getSourceFields()
						.getActiveItems()) {
					if (!field.getLoadWarnings().isEmpty()) {
						this.loadWarnings.addAll(field.getLoadWarnings());
					}
				}
			}
		}
	}

	/**
	 * This method is used to check whether the Lookup path is valid.
	 * 
	 * @return true if the lookup path is valid.
	 */
	public Boolean isValid() {
		return validInd;
	}

	/**
	 * This method is used to set validity of a lookup path.
	 * 
	 * @param validInd
	 *            : set the lookup as valid if true.
	 */
	public void setValid(Boolean validInd) {
	    if (validInd != null &&
	        this.validInd != null &&
	        !this.validInd.equals(validInd)) {
            markActivated();	        
	    }
		this.validInd = validInd;
	}

	/**
	 * This method is used to get a list of steps related to this lookup.
	 * 
	 * @return a list of steps related to the lookup
	 */
	public SAFRList<LookupPathStep> getLookupPathSteps() {
		return lookupPathSteps;
	}

	public Integer getTargetLrFileAssociationId() {
	    return targetXLRFileId;
	}

    public void setTargetLrFileAssociationId(Integer targetXLRFileId) {
        this.targetXLRFileId = targetXLRFileId;
    }
	
	/**
	 * @return the lrFileAssociation
	 */
	public ComponentAssociation getTargetLrFileAssociation() throws DAOException,
			SAFRException {
		if (targetLRLFAssociation == null) {
			if (targetXLRFileId != null && targetXLRFileId > 0) {
				// lazy initialize and cache the object
				this.targetLRLFAssociation = SAFRAssociationFactory
						.getLogicalRecordToLogicalFileAssociation(
								targetXLRFileId, getEnvironmentId());
			}
		}
		return targetLRLFAssociation;
	}
	
	@Override
	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		// Set the transfer object from this object
		LookupPathTransfer trans = (LookupPathTransfer) safrTrans;
		trans.setSourceLRId(this.sourceLRId);
		trans.setTargetXLRFileId(this.targetXLRFileId);
		trans.setValidInd(this.validInd);
	}

	@Override
	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		// Set this object from the transfer object
		LookupPathTransfer trans = (LookupPathTransfer) safrTrans;
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
		this.validInd = trans.isValidInd();
	}

	/**
	 * Set the source logical record for a lookup path. This will also set the
	 * source logical record of first step in this lookup path.
	 * 
	 * @param logicalRecord
	 *            the source logical record.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void setSourceLR(LogicalRecord logicalRecord) throws SAFRException,
			DAOException {
		// first attempt to change the source LR of first step.
		LookupPathStep firstStep = (LookupPathStep) lookupPathSteps
				.getActiveItems().get(0);
		// this will throw SAFRValidationException if the current Source LR is
		// used in subsequent steps in source fields.
		firstStep.setSourceLR(logicalRecord);

		this.sourceLR = logicalRecord;
		if (sourceLR == null) {
			this.sourceLRId = 0;
		} else {
			this.sourceLRId = sourceLR.getId();
		}
		markModified();
	}
	
	
	public void setSourceLR(LogicalRecord logicalRecord,LookupPathStep currentStep) {
		// TODO Auto-generated method stub
		
		LookupPathStep firstStep = (LookupPathStep) lookupPathSteps
				.getActiveItems().get(0);
		// this will throw SAFRValidationException if the current Source LR is
		// used in subsequent steps in source fields.
		firstStep.setSourceLR(logicalRecord,currentStep.getSequenceNumber().intValue());

		this.sourceLR = logicalRecord;
		if (sourceLR == null) {
			this.sourceLRId = 0;
		} else {
			this.sourceLRId = sourceLR.getId();
		}
		markModified();
	}

	/**
	 * This method is used to set a target LR/LF association of this lookup.
	 * 
	 * @param LRLFAssociation
	 *            : The Component association which is to be set as the LR/LF
	 *            association.
	 */
	public void setTargetLRLFAssociation(ComponentAssociation LRLFAssociation) {
		this.targetLRLFAssociation = LRLFAssociation;
		if (targetLRLFAssociation == null) {
			this.targetXLRFileId = 0;
		} else {
			this.targetXLRFileId = targetLRLFAssociation.getAssociationId();
		}
		markModified();
	}

	/**
	 * This method is to create a Lookup Path Step. The newly created step gets
	 * its Source LR/LF from the previous step's target.
	 * 
	 * @return The new Step which has been created.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LookupPathStep createStep() throws SAFRException, DAOException {
		List<LookupPathStep> activeSteps = lookupPathSteps.getActiveItems();
		LookupPathStep newStep = null;
		if (activeSteps.size() > 0) {
			// steps already exist
			LookupPathStep lastStep = activeSteps.get(activeSteps.size() - 1);
			newStep = new LookupPathStep(this,
					lastStep.getSequenceNumber() + 1, lastStep.getTargetLR(),
					lastStep.getTargetLRLFAssociation());

		} else {
			// first step
			newStep = new LookupPathStep(this, 1, null, null);
		}
		newStep.setId(dummyStepId++ * -1);
		lookupPathSteps.add(newStep);
		markModified();
		return newStep;
	}

	/**
	 * This method is to remove steps of a Lookup Path. It deletes the step
	 * whose index is provided to it and also all the steps below that step. The
	 * first step in a lookup cannot be deleted.
	 * 
	 * @param index
	 *            : The index of the step which is to be deleted.
	 * @return A Boolean which specifies the successful deletion of the step(s).
	 * @throws SAFRException
	 */
	public Boolean removeStep(int index) throws SAFRException {
		int numberOfSteps = lookupPathSteps.getActiveItems().size();
		if (index == 0) {
			// Cannot delete first step
			return false;
		} else {
			List<SAFRPersistentObject> tmpList = new ArrayList<SAFRPersistentObject>();
			// remove the step with index as provided and all the steps below it
			for (int i = index; i < numberOfSteps; i++) {
				tmpList.add(lookupPathSteps.getActiveItems().get(i));
			}
			for (SAFRPersistentObject obj : tmpList) {
				lookupPathSteps.remove(obj);
			}
			// reset the lookup path's target Lr/Lf assoc to last step's target.
			LookupPathStep lastStep = lookupPathSteps.getActiveItems().get(
					lookupPathSteps.getActiveItems().size() - 1);
			setTargetLRLFAssociation(lastStep.getTargetLRLFAssociation());
			markModified();
			return true;
		}
	}

	/**
	 * Change the Source LR of next step.
	 * 
	 * @param targetLR
	 *            changed target LR of current step to be set as source of next.
	 * @param sequenceNumber
	 *            sequence number of current step.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	void changeStepTarget(LogicalRecord targetLR, Integer sequenceNumber)
			throws SAFRException, DAOException {
		if (sequenceNumber < this.lookupPathSteps.getActiveItems().size()) {
			LookupPathStep nextStep = (LookupPathStep) lookupPathSteps
					.getActiveItems().get(sequenceNumber.intValue());
			nextStep.setSourceLR(targetLR);
		}
	}

	/**
	 * Change the Source LR/LF Association of next step. This is triggered when
	 * the Target LR/LF association of a step is changed. If the changed step is
	 * last step, then, the Target LR/LF of lookup itself is changed.
	 * 
	 * @param step
	 *            the step whose target LR/LF association has changed.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	void changeStepTargetLRLFAssocistion(ComponentAssociation LRLFAssoc,
			Integer sequenceNumber) throws SAFRException, DAOException {
		if (sequenceNumber < this.lookupPathSteps.getActiveItems().size()) {
			LookupPathStep nextStep = (LookupPathStep) lookupPathSteps
					.getActiveItems().get(sequenceNumber.intValue());
			nextStep.setSourceLRLFAssociation(LRLFAssoc);
		} else if (sequenceNumber.intValue() == this.lookupPathSteps
		// changed step is the last one.
				.getActiveItems().size()) {
			setTargetLRLFAssociation(LRLFAssoc);
		}
	}

	/**
	 * Checks if the Source LR of a step is used anywhere in the current step or
	 * subsequent step's Source Fields.
	 * 
	 * @param lookupPathStep
	 *            The step to check Source LR usage.
	 * @throws SAFRException
	 *             If a usage of Source LR is found in any of the Source Fields,
	 *             in this case, exception of type SAFRValidationException will
	 *             be thrown.
	 * @throws DAOException
	 */
	protected void checkSourceLRUsage(LookupPathStep lookupPathStep)
			throws SAFRException, DAOException {
		SAFRValidationException safrValidationException = new SAFRValidationException();
		Map<Integer, List<LookupPathSourceField>> sourceFieldMap = new HashMap<Integer, List<LookupPathSourceField>>();

		// get the source LR and LR/LF of the changed Step
		LogicalRecord sourceLR = lookupPathStep.getSourceLR();
		ComponentAssociation sourceLRLFAssociation = lookupPathStep
				.getSourceLRLFAssociation();
		LogicalRecord nextStepSourceFieldSourceLR = null;
		ComponentAssociation nextStepSourceFieldSourceLRLFAssoc = null;
		// the current step number
		int seqno = lookupPathStep.getSequenceNumber().intValue();
		List<LookupPathStep> activeSteps = this.getLookupPathSteps()
				.getActiveItems();
		// loop through all the steps from the current step and check if the
		// source LR and source LR/LF association is used in any of the Source
		// Field's of these steps.
		for (int i = seqno - 1; i < activeSteps.size(); i++) {
			LookupPathStep nextStep = activeSteps.get(i);
			List<LookupPathSourceField> nextStepSourceFields = nextStep
					.getSourceFields().getActiveItems();
			for (int j = 0; j < nextStepSourceFields.size(); j++) {
				LookupPathSourceField nextStepSourceField = nextStepSourceFields
						.get(j);
				// Check only if the type is LRField. Other types don't matter.
				if (nextStepSourceField.getSourceFieldType() == LookupPathSourceFieldType.LRFIELD) {
					nextStepSourceFieldSourceLR = nextStepSourceField
							.getSourceFieldSourceLR();
					nextStepSourceFieldSourceLRLFAssoc = nextStepSourceField
							.getSourceFieldLRLFAssociation();
					if (checkLREquality(sourceLR, nextStepSourceFieldSourceLR)
							&& checkLRLFEquality(sourceLRLFAssociation,
									nextStepSourceFieldSourceLRLFAssoc)) {
						// this means this source field was chosen for the
						// current step's source LR.
						// This will also mean we can't change the current
						// step's source LR.
						if (sourceFieldMap.containsKey(i + 1)) {
							sourceFieldMap.get(i + 1).add(nextStepSourceField);
						} else {
							List<LookupPathSourceField> srcFldList = new ArrayList<LookupPathSourceField>();
							srcFldList.add(nextStepSourceField);
							sourceFieldMap.put(i + 1, srcFldList);
						}
					}
				}
			}
		}
		// prepare error message to be thrown.
		for (int stepNo : sourceFieldMap.keySet()) {
			String message = "Step : " + stepNo + SAFRUtilities.LINEBREAK;
			for (LookupPathSourceField srcFld : sourceFieldMap.get(stepNo)) {
				message += "    Field: " + srcFld.getSourceLRField().getName()
						+ SAFRUtilities.LINEBREAK;
			}
			safrValidationException
					.setErrorMessage(Property.SOURCE_LR, message);
		}
		if (!safrValidationException.getErrorMessages().isEmpty()) {
			throw safrValidationException;
		}
	}

	private boolean checkLRLFEquality(ComponentAssociation assoc1,
			ComponentAssociation assoc2) {
		if (assoc1 == null && assoc2 == null) {
			return true;
		} else if (assoc1 == null && assoc2 != null) {
			return false;
		} else if (assoc2 == null && assoc1 != null) {
			return false;
		} else {
			return assoc1.getAssociationId().equals(assoc2.getAssociationId());
		}
	}

	private boolean checkLREquality(LogicalRecord sourceLR,
			LogicalRecord nextStepSourceLR) {
		if (sourceLR == null && nextStepSourceLR == null) {
			return true;
		} else if (sourceLR == null && nextStepSourceLR != null) {
			return false;
		} else if (sourceLR != null && nextStepSourceLR == null) {
			return false;
		} else {
			return sourceLR.getId().equals(nextStepSourceLR.getId());
		}

	}

	@Override
	public void store() throws SAFRException, DAOException {

        if (isForMigration()) {
            if (!SAFRApplication.getUserSession().isAdminOrMigrateInUser(getEnvironmentId())) {
                String msg = "The user must be an Administrator or have MigrateIn permission on Environment "
                        + getEnvironmentId() + " to migrate a Lookup Path into it.";
                throw new SAFRException(msg);
            }
        } else {
            if (this.id == 0) {
                if (!hasCreatePermission()) {
                    throw new SAFRException("The user is not authorized to create this lookup path.");
                }
            } else {
                if (!hasUpdateRights()) {
                    throw new SAFRException("The user is not authorized to update this lookup path.");
                }
            }
        }
		
		List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();

		// Save the Lookup Path
		LookupPathTransfer trans = new LookupPathTransfer();
		setTransferData(trans);

		// CQ 7329 Kanchan Rauthan 04/03/2010 To show error if lookup path is
		// already deleted from database and user still tries to save it.
		boolean success = false;
		try {

			while (!success) {
				try {
					// CQ 7826 Santhosh 26/05/2010 Implement 2-phase commit DB
					// transactionality

					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					trans = DAOFactoryHolder.getDAOFactory().getLookupDAO()
							.persistLookupPath(trans);
					setObjectData(trans);
					savedObjs.add(this);

                    if (!isForMigration() && !SAFRApplication.getUserSession().isSystemAdministrator()) {
                        SAFRApplication.getUserSession().getGroup().assignComponentFullRights(
                            this, ComponentType.LookupPath);
                    }

					
					// Save Lookup Path Steps
					
                    // CQ10021 Store its source Fields in list so we can save in one operation.
					// Breaking encapsulation here for the performance improvement.
                    List<LookupPathSourceFieldTransfer> sourceFieldsTransfer = new ArrayList<LookupPathSourceFieldTransfer>();
                    List<Integer> stepIds = new ArrayList<Integer>();
                    
					for (LookupPathStep step : lookupPathSteps) {
						if (step.getPersistence() == SAFRPersistence.DELETED) {
							DAOFactoryHolder.getDAOFactory()
									.getLookupPathStepDAO()
									.removeLookupPathStep(step.getId(),
											step.getEnvironmentId());
						} else {
							step.store();	
							savedObjs.add(step);
							
							// save id and source fields for later storage
                            stepIds.add(step.getId());
					        for (SAFRPersistentObject sourceField : step.getSourceFields().getActiveItems()) {

					            // convert model object to transfer objects.
					            LookupPathSourceField lkupPathSourceField = (LookupPathSourceField) sourceField;
					            LookupPathSourceFieldTransfer lkupPathSourceFieldTrans = new LookupPathSourceFieldTransfer();
					            lkupPathSourceField.setTransferData(lkupPathSourceFieldTrans);
					            sourceFieldsTransfer.add(lkupPathSourceFieldTrans);
					        }
						}
					}
					lookupPathSteps.flushDeletedItems();
					
					// store all step source fields
                    if (sourceFieldsTransfer.size() > 0) {
                        DAOFactoryHolder.getDAOFactory()
                            .getLookupPathStepDAO().persistLookupPathStepsSourceFields(
                                    stepIds, sourceFieldsTransfer);
                    }

					// inactivate dependent views.
					if (!deactivatedViewList.isEmpty()) {
						DAOFactoryHolder.getDAOFactory().getViewDAO()
								.makeViewsInactive(deactivatedViewList,
										getEnvironmentId());
					}
					
                    SAFRApplication.getModelCount().incCount(this.getClass(), 1);
                    SAFRApplication.getModelCount().incCount(LookupPathStep.class, lookupPathSteps.size());
                    SAFRApplication.getModelCount().incCount(LookupPathSourceField.class, sourceFieldsTransfer.size());
					
					success = true;
				} catch (DAOUOWInterruptedException e) {
					// UOW interrupted so retry it
					continue;
				}

			} // end while(!success)
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			throw new SAFRException("The lookup path with id " + this.getId()
				+ " cannot be updated as its already been deleted from the database.", snfe);
		} finally {

			if (success) {
				// End Transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().end();

			} else {
				// Rollback the transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
				// reset the object state
				for (SAFRPersistentObject obj : savedObjs) {
					obj.undo();
				}

			}
		}
	}

	/**
	 * This enum maintains the properties of lookup path.
	 * 
	 */
	public enum Property {
		NAME, STEP, VIEW_DEP, SOURCE_LR
	}

	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		validate(null);
	}

	/**
	 * Validate method is used to validate a Lookup Path object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @param token
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the lookup path, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 * @throws IllegalArgumentException
	 *             if the token ID does not identify this object.
	 */
	public void validate(SAFRValidationToken token)
			throws SAFRValidationException, SAFRException, DAOException {

		if (token != null && token.getTokenId() != this.hashCode()) {
			throw new IllegalArgumentException(
					"The validation token does not identify this "
							+ this.getClass().getName());
		}

		if (token == null) {
			deactivatedViewList.clear();
		}
        deactivatedViewList.addAll(migExViewList);		
		SAFRValidationToken safrValidationToken;
		String dependencies = "";

		SAFRValidationException safrValidationException = new SAFRValidationException();
		SAFRValidator safrValidator = new SAFRValidator();
		List<LookupPathStep> activeSteps = this.getLookupPathSteps().getActiveItems();

		// check errors only if the type of token is not
		// dependency_Lookup_Warning or warning because warning is checked only
		// after all
		// the errors are fixed.
		if (token == null || 
		    (token != null && 
		     token.getValidationFailureType() != SAFRValidationType.DEPENDENCY_LOOKUP_WARNING && 
		     token.getValidationFailureType() != SAFRValidationType.WARNING)) {

			// check Lookup's name
			if (getName() == null || getName() == "") {
				safrValidationException.setErrorMessage(Property.NAME,
						"Lookup Path name cannot be empty.");
			} else {
				if (this.getName().length() > ModelUtilities.MAX_NAME_LENGTH) {
					safrValidationException.setErrorMessage(Property.NAME,
							"The length of Lookup Path name "
			        		+ ModelUtilities.formatNameForErrMsg(
						    getName(),(isForImport() || isForMigration()))						
							+ "cannot exceed 48 characters.");
				}
				else if (this.isDuplicate()) {
					safrValidationException
							.setErrorMessage(
									Property.NAME,
									"The Lookup Path name '"
											+ getName()
											+ "' already exists. Please specify a different name.");
				}
                if (!safrValidator.isNameValid(getName())) {
                    safrValidationException
                            .setErrorMessage(
                                    Property.NAME,
                                    "The Lookup Path name "
                                            + ModelUtilities.formatNameForErrMsg(
                                            getName(),(isForImport() || isForMigration()))                                          
                                            + "should begin "
                                            + "with a letter and should comprise of letters"
                                            + ", numbers, pound sign (#) and underscores only.");
                }				
			}

			// validating all the steps of this lookup path.
			for (int i = 0; i < activeSteps.size(); i++) {
				LookupPathStep activeStep = activeSteps.get(i);
				try {
					activeStep.validate();
				} catch (SAFRValidationException sve) {
					safrValidationException.setErrorMessages(Property.STEP, sve.getErrorMessages());
				}
			}

			// set token as error and throw the exception so that user can
			// fix the errors and then proceed to warnings.
			if (!safrValidationException.getErrorMessages().isEmpty()) {
				safrValidationToken = new SAFRValidationToken(this,SAFRValidationType.ERROR);
				safrValidationException.setSafrValidationToken(safrValidationToken);
				throw safrValidationException;
			}
		}
		// check for warning.Check for warning only if it is not already warning
		// or Dependency lookup warning
		// because dependency Lookup warning is checked only after warnings are
		// fixed.
		if (token == null || 
		    (token != null && 
		     token.getValidationFailureType() != SAFRValidationType.DEPENDENCY_LOOKUP_WARNING && 
		     token.getValidationFailureType() != SAFRValidationType.WARNING)) {
			for (int i = 0; i < activeSteps.size(); i++) {
				LookupPathStep activeStep = activeSteps.get(i);
				try {
					activeStep.checkValid();
				} catch (SAFRValidationException sve) {
					safrValidationException.setErrorMessages(Property.STEP, sve.getErrorMessages());
				}

			}
			if (!safrValidationException.getErrorMessages().isEmpty()) {
				safrValidationToken = new SAFRValidationToken(this,SAFRValidationType.WARNING);
				safrValidationException.setSafrValidationToken(safrValidationToken);
				throw safrValidationException;
			}
		}

		if (getId() > 0) {
			// dependency check of existing Lookup.Retrieve only those views
			// which are not in
			// exception list.
			List<DependentComponentTransfer> dependentComponents = DAOFactoryHolder
			    .getDAOFactory().getLookupDAO().getLookupPathViewDependencies(
			        getEnvironmentId(), getId(),deactivatedViewList);
			boolean hasDeps = false;
			// prepare error message to be thrown.
			if (!dependentComponents.isEmpty()) {
				Map<ComponentType, List<DependentComponentTransfer>> dependencyMap = 
				    new TreeMap<ComponentType, List<DependentComponentTransfer>>();
				dependencyMap.put(ComponentType.View, dependentComponents);
				safrValidationException.setDependencies(dependencyMap);
				dependencies += "Views" + SAFRUtilities.LINEBREAK;
				for (int i = 0; i < dependentComponents.size(); i++) {
					Integer viewID = dependentComponents.get(i).getId();
					String viewName = dependentComponents.get(i).getName();
					if (!deactivatedViewList.contains(viewID)) {
					    hasDeps = true;
						dependencies += "    " + viewName + " [" + viewID + "]" + SAFRUtilities.LINEBREAK;
						deactivatedViewList.add(viewID);
					}
				}
			}

			if (hasDeps) {
				safrValidationToken = new SAFRValidationToken(this,SAFRValidationType.DEPENDENCY_LOOKUP_WARNING);
				safrValidationException.setSafrValidationToken(safrValidationToken);
				safrValidationException.setErrorMessage(Property.VIEW_DEP,dependencies);
				throw safrValidationException;
			}
		}
	}

    public Set<Integer> getMigExViewList() {
        return migExViewList;
    }
    	
	private boolean isDuplicate() throws DAOException, SAFRException {
		LookupPathTransfer lookupPathTransfer = null;
		lookupPathTransfer = DAOFactoryHolder.getDAOFactory().getLookupDAO()
				.getDuplicateLookupPath(getName(), getId(), getEnvironmentId());

		if (lookupPathTransfer == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
	    
        if (hasCreatePermission()) {
            	   
    		LookupPath lookupPathCopy = SAFRApplication.getSAFRFactory()
    				.createLookupPath();
    		lookupPathCopy.setName(newName);
    		lookupPathCopy.setValid(this.isValid());
    		lookupPathCopy.setComment(this.getComment());
    
    		// First step of the original LookupPath.
    		LookupPathStep firstStep = this.getLookupPathSteps().get(0);
    
    		// Setting the soruceLR of the first which is created when a new
    		// LookupPath is created for the subsequent steps we have to create a
    		// step and set the SourceLR.
    		lookupPathCopy.setSourceLR(firstStep.getSourceLR());
    		List<LookupPathStep> steps = (List<LookupPathStep>) this
    				.getLookupPathSteps().getActiveItems();
    
    		for (LookupPathStep lookupStep : steps) {
    			LookupPathStep lukpStepCopy;
    			// If the sequence no is 1 get the automatically created step when a
    			// new LookupPath is created else create a new
    			// step and set the SourceLR of that step.
    			if (lookupStep.getSequenceNumber().equals(1)) {
    				lukpStepCopy = lookupPathCopy.getLookupPathSteps().get(0);
    			} else {
    				lukpStepCopy = lookupPathCopy.createStep();
    				lukpStepCopy.setSourceLR(lookupStep.getSourceLR());
    			}
    			lukpStepCopy.setSourceLRLFAssociation(lookupStep
    					.getSourceLRLFAssociation());
    			lukpStepCopy.setTargetLR(lookupStep.getTargetLR());
    			lukpStepCopy.setTargetLRLFAssociation(lookupStep
    					.getTargetLRLFAssociation());
    
    			List<LookupPathSourceField> sourceFields = lookupStep
    					.getSourceFields().getActiveItems();
    
    			for (LookupPathSourceField lookupPathSourceField : sourceFields) {
    				LookupPathSourceField srcField = lukpStepCopy
    						.addSourceField(lookupPathSourceField
    								.getSourceFieldType());
    
    				// CQ8963 call to setSourceLRField should be done before setting
    				// other properties.
    				srcField.setSourceLRField(lookupPathSourceField
    						.getSourceLRField());
    
    				srcField.setDataTypeCode(lookupPathSourceField
    						.getDataTypeCode());
    				srcField.setLength(lookupPathSourceField.getLength());
    				srcField.setDateTimeFormatCode(lookupPathSourceField
    						.getDateTimeFormatCode());
    				srcField.setScaling(lookupPathSourceField.getScaling());
    				srcField.setDecimals(lookupPathSourceField.getDecimals());
    				srcField.setSigned(lookupPathSourceField.isSigned());
    				srcField.setNumericMaskCode(lookupPathSourceField
    						.getNumericMaskCode());
    				srcField.setSourceValue(lookupPathSourceField.getSourceValue());
    				srcField.setKeySeqNbr(lookupPathSourceField.getKeySeqNbr());
    				srcField.setSourceFieldLRLFAssociation(lookupPathSourceField
    						.getSourceFieldLRLFAssociation());
    				srcField.setSourceFieldSourceLR(lookupPathSourceField
    						.getSourceFieldSourceLR());
    				srcField.setSourceFieldType(lookupPathSourceField
    						.getSourceFieldType());
    				srcField.setDefaultValue(lookupPathSourceField
    						.getDefaultValue());
    				srcField.setSymbolicName(lookupPathSourceField
    						.getSymbolicName());
    				srcField.setSortKeyLabel(lookupPathSourceField
    						.getSortKeyLabel());
    
    				srcField.setName(lookupPathSourceField.getName());
    
    				srcField.setHeaderAlignmentCode(lookupPathSourceField
    						.getHeaderAlignmentCode());
    				srcField.setHeading1(lookupPathSourceField.getHeading1());
    				srcField.setHeading2(lookupPathSourceField.getHeading2());
    				srcField.setHeading3(lookupPathSourceField.getHeading3());
    
    			}
    
    		}
    
    		lookupPathCopy.validate();
    		lookupPathCopy.store();
    
    		return lookupPathCopy;
        }
        else {
            throw new SAFRException("The user is not authorized to create a Loookup Path.");
        }        
	}

	public List<String> getLoadWarnings() {
		return loadWarnings;
	}

    public Set<Integer> getDeactivatedViews() {
        return deactivatedViewList;        
    }

}
