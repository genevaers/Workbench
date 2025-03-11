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


import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

/**
 * Represents a SAFR ControlRecord.A Control Record can be seen in 'Hardcopy' of
 * a report.
 * 
 */
public class ControlRecord extends SAFREnvironmentalComponent {

	private static final Integer MIN_BEGIN_PERIOD_VALUE = 0;
	private static final int MAX_BEGIN_PERIOD_LIMIT = 9;
	private static final int MAX_END_PERIOD_LIMIT = 9;
	private Integer firstFiscalMonth;
	private Integer beginPeriod;
	private Integer endPeriod;

	/**
	 * This constructor is used when defining a new ControlRecord in the
	 * application. It will initialize the ControlRecord ID to zero and
	 * EnvironmentId to specified EnvironmentId to which control record belongs
	 * to. The ControlRecord ID will be reset automatically to a unique value
	 * when the ControlRecord object is persisted via its <code>store()</code>
	 * method.
	 */
	public ControlRecord(Integer environmentId) {
		super(environmentId);
	}

	/**
	 * Create an ControlRecord object containing the data in the specified
	 * transfer object. Used to instantiate existing ControlRecord objects.
	 * 
	 * @param trans
	 *            ControlRecord data transfer object
	 */
	ControlRecord(ControlRecordTransfer trans) {
		super(trans);
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		ControlRecordTransfer trans = (ControlRecordTransfer) safrTrans;
		this.firstFiscalMonth = trans.getFirstFiscalMonth();
		this.beginPeriod = trans.getBeginPeriod();
		this.endPeriod = trans.getEndPeriod();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		ControlRecordTransfer trans = (ControlRecordTransfer) safrTrans;
		trans.setFirstFiscalMonth(firstFiscalMonth);
		trans.setBeginPeriod(beginPeriod);
		trans.setEndPeriod(endPeriod);
	}

	/**
	 * Get FirstFiscalMonth of the Control Record.
	 * 
	 * @return FirstFiscalMonth of the Control Record.
	 */
	public Integer getFirstFiscalMonth() {
		return firstFiscalMonth;
	}

	/**
	 * Set FirstFiscalMonth of the Control Record.
	 * 
	 * @param firstFiscalMonth
	 *            to set FirstFiscalMonth of the Control Record.
	 */
	public void setFirstFiscalMonth(Integer firstFiscalMonth) {
		this.firstFiscalMonth = firstFiscalMonth;
		markModified();
	}

	/**
	 * Get BeginPeriod of the Control Record.
	 * 
	 * @return BeginPeriod of the Control Record.
	 */
	public Integer getBeginPeriod() {
		return beginPeriod;
	}

	/**
	 * Set BeginPeriod of the Control Record.
	 * 
	 */
	public void setBeginPeriod(Integer beginPeriod) {
		this.beginPeriod = beginPeriod;
		markModified();
	}

	/**
	 * Get EndPeriod of the Control Record.
	 * 
	 * @return EndPeriod of the Control Record.
	 */
	public Integer getEndPeriod() {
		return endPeriod;
	}

	/**
	 * Set EndPeriod of the Control Record.
	 * 
	 */
	public void setEndPeriod(Integer endPeriod) {
		this.endPeriod = endPeriod;
		markModified();
	}

	@Override
	public void store() throws SAFRException, DAOException {
		if (isForMigration()) {
			if (!SAFRApplication.getUserSession().isAdminOrMigrateInUser(getEnvironmentId())) {
				String msg = "The user must be an Administrator or have MigrateIn permission on Environment "
						+ getEnvironmentId() + " to migrate a Control Record into it.";
				throw new SAFRException(msg);
			}
		} else {
			if (this.id == 0) {
				if (!SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin(getEnvironmentId())) {
					throw new SAFRException("The user is not authorized to create a new control record.");
				}
			} else {
				if (!SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin(getEnvironmentId())) {
					throw new SAFRException("The user is not authorized to update this control record.");
				}
			}
		}
		
		ControlRecordTransfer trans = new ControlRecordTransfer();
		setTransferData(trans);

		// CQ 7329 Kanchan Rauthan 03/03/2010 To show error if control record is
		// already deleted from database and user still tries to save it.
		try {
			trans = DAOFactoryHolder.getDAOFactory().getControlRecordDAO()
					.persistControlRecord(trans);

			setObjectData(trans);
			
	        SAFRApplication.getModelCount().incCount(this.getClass(), 1);			

		} catch (SAFRNotFoundException snfe) {
			throw new SAFRException(
					"The control record with id "
							+ this.getId()
							+ " cannot be updated as its already been deleted from the database.",
					snfe);
		}		
	}

	/**
	 * This enum maintains the properties of control record.
	 * 
	 */
	public enum Property {
		NAME, BEGIN_PERIOD, END_PERIOD, MAX_EXTRACT_FILES, COMMENT, FIRST_FISCAL_MONTH;
	}

	/**
	 * Validate method is used to validate a Control Record object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the control record, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		if ((getName() == null) || (getName() == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"Control Record name cannot be empty.");
		} else {
			if (getName().length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of Control Record name " 
		        		+ ModelUtilities.formatNameForErrMsg(
					    getName(),(isForImport() || isForMigration()))						
								+ "cannot exceed 48 characters.");
			}
			else if (this.isDuplicate()) {
				safrValidationException
						.setErrorMessage(
								Property.NAME,
								"The Control Record name '"
										+ getName()
										+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(getName())) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The Control Record name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),(isForImport() || isForMigration()))
                                        + "should begin "
                                        + "with a letter and should comprise of letters"
                                        + ",numbers, pound sign (#) and underscores only.");
            }
		}
		if ((getComment() != null)
				&& (getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH)) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment cannot be more than 254 characters.");
		}
		if ((this.beginPeriod != null)
				&& (this.beginPeriod.toString().length()) > MAX_BEGIN_PERIOD_LIMIT) {
			safrValidationException
					.setErrorMessage(Property.BEGIN_PERIOD,
							"The Beginning Period should not be greater than 9 digits.");
		}
		if ((this.beginPeriod != null)
				&& (this.beginPeriod < MIN_BEGIN_PERIOD_VALUE)) {
			safrValidationException
					.setErrorMessage(Property.BEGIN_PERIOD,
							"The Beginning Period should be greater than or equal to zero.");
		}
		if ((this.endPeriod != null)
				&& (this.endPeriod.toString().length()) > MAX_END_PERIOD_LIMIT) {
			safrValidationException.setErrorMessage(Property.END_PERIOD,
					"The Ending Period should not be greater than 9 digits.");
		}
		if ((this.endPeriod != null) && (this.beginPeriod >= this.endPeriod)) {
			safrValidationException
					.setErrorMessage(Property.END_PERIOD,
							"The Ending Period should be greater than Beginning Period.");
		}
		if (this.firstFiscalMonth == null || this.firstFiscalMonth <= 0) {
			safrValidationException.setErrorMessage(
					Property.FIRST_FISCAL_MONTH,
					"First Fiscal Month cannot be empty.");
		}
		if (!safrValidationException.getErrorMessages().isEmpty())
			throw safrValidationException;
	}

	/**
	 * This method is used to check whether the Control Record name already
	 * exist in the workbench.
	 * 
	 * @param componentName
	 * @return True if Component with given name exists in database.
	 */
	private boolean isDuplicate() throws DAOException, SAFRException {
		ControlRecordTransfer controlRecordTransfer = null;
		controlRecordTransfer = DAOFactoryHolder.getDAOFactory()
				.getControlRecordDAO().getDuplicateControlRecord(getName(),
						getId(), getEnvironmentId());
		if (controlRecordTransfer == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public ControlRecord saveAs(String newName) throws SAFRValidationException,
			DAOException, SAFRException {

		ControlRecord ctrlRecCopy = SAFRApplication.getSAFRFactory()
				.createControlRecord();
		ctrlRecCopy.setName(newName);
		ctrlRecCopy.setComment(this.getComment());
		ctrlRecCopy.setFirstFiscalMonth(this.getFirstFiscalMonth());
		ctrlRecCopy.setBeginPeriod(this.getBeginPeriod());
		ctrlRecCopy.setEndPeriod(this.getEndPeriod());
		ctrlRecCopy.validate();
		ctrlRecCopy.store();

		return ctrlRecCopy;
	}

}
