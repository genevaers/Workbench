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
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.EnvironmentTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * Represents a SAFR Environment. Environments are used to logically partition
 * data within the SAFR database.
 * 
 */
public class Environment extends SAFRComponent {

	private InitializationParameters initParams;
	private SAFRAssociationList<GroupEnvironmentAssociation> associatedGroups;
	private List<Group> associatedGroupsList;
	private boolean crRequired;

	/*
	 * Holds initialization parameters for a SAFR Environment. These are set
	 * once during environment creation and then never modified
	 */
	private class InitializationParameters {

		public String controlRecClientName = "Default_Install";
		public String groupName = "Administrators";
	}

	/**
	 * The default constructor is used when defining a new Environment in the
	 * application. It will initialize the environment ID to zero. The ID will
	 * be reset automatically to a unique value when the Environment object is
	 * first persisted via its <code>store()</code> method.
	 */
	public Environment() {
		super();
		this.initParams = new InitializationParameters();
		associatedGroups = new SAFRAssociationList<GroupEnvironmentAssociation>();
	}

	/**
	 * Create an Environment object containing the data in the specified
	 * transfer object. Used to instantiate existing Environment objects from
	 * the database.
	 * 
	 * @param Environment
	 *            DAO data transfer object
	 */
	Environment(EnvironmentTransfer trans) {
		super(trans);
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		EnvironmentTransfer trans = (EnvironmentTransfer) safrTrans;
		if (initParams != null) {
			trans.setControlRecClientName(initParams.controlRecClientName);
			trans.setGroupName(initParams.groupName);
		}
	}

	/**
	 * This method initializes a new Environment object with the specified
	 * default initialization parameters. This involves associating the
	 * Environment with a new Control Record, a new View Folder and either a new
	 * or existing Group. This behaviour only applies to newly created
	 * Environments that have not yet been persisted. It does not apply to
	 * existing (persistent) Environment objects.
	 * 
	 * @param controlRecClientName
	 *            name of the new Control Record
	 * @param groupName
	 *            name of a new or existing Group
	 * 
	 * @throws IllegalStateException
	 *             if the Environment already exists (if it's not new)
	 */
	public void initialize(String controlRecClientName, String groupName) {
		if (this.initParams != null) {
			this.initParams.controlRecClientName = controlRecClientName;
			this.initParams.groupName = groupName;
		} else {
			throw new IllegalStateException(
					"Cannot initialize an existing Environment");
		}

	}

	@Override
	public void store() throws SAFRException, DAOException {
	    
		if (SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin(id)) {
		    
			EnvironmentTransfer trans = new EnvironmentTransfer();
			setTransferData(trans);

			// CQ 7329 Kanchan Rauthan 04/03/2010 To show error if environment
			// is
			// already deleted from database and user still tries to save it.
			try {
				trans = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
						.persistEnvironment(trans);
				setObjectData(trans);				
				if(!crRequired){
					DAOFactoryHolder.getDAOFactory().getControlRecordDAO().deleteAllControlRecordsFromEnvironment(trans.getId());
				}

			} catch (SAFRNotFoundException snfe) {
				throw new SAFRException("The environment with id "+ this.getId()+ 
				    " cannot be updated as its already been deleted from the database.", snfe);
			}
		} else {
			if (this.id > 0) {
				throw new SAFRException(
						"The user is not authorized to update this environment.");
			} else {
				throw new SAFRException(
						"The user is not authorized to create a new environment.");
			}
		}
	}

	/**
	 * This enum maintains the properties of environment.
	 * 
	 */
	public enum Property {
		NAME, CR_NAME, GROUP_NAME, COMMENT
	}

	/**
	 * Validate method is used to validate an Environment object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the environment, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {

		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		if ((this.getName() == null) || (this.getName() == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"Environment name cannot be empty.");
		} else {
			if (this.getName().length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of environment name "
		        		+ ModelUtilities.formatNameForErrMsg(
						getName(),isForImport())						
					    + "cannot exceed 48 characters.");
			}
			else if (isDuplicate()) {
				safrValidationException
						.setErrorMessage(
								Property.NAME,
								"The environment name '"
										+ getName()
										+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(getName())) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The environment name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),isForImport())
                                        + "should begin with"
                                        + " a letter and should comprise of letters, numbers"
                                        + " , pound sign (#) and underscores only.");
            }
		}
		if (getId() == 0) {

			if ((initParams.controlRecClientName == null)
					|| (initParams.controlRecClientName == "")) {
				safrValidationException.setErrorMessage(Property.CR_NAME,
						"Control Record name cannot be empty.");
			} else {
				if (initParams.controlRecClientName.length() > ModelUtilities.MAX_NAME_LENGTH) {
					safrValidationException.setErrorMessage(Property.CR_NAME,
							"The length of Control Record name '"
									+ initParams.controlRecClientName
									+ "' cannot exceed 48 characters.");
				}
				if (!safrValidator.isNameValid(initParams.controlRecClientName)) {
					safrValidationException
							.setErrorMessage(
									Property.CR_NAME,
									"The Control Record name '"
											+ initParams.controlRecClientName
											+ "' should begin"
											+ " with a letter and should comprise of letters,"
											+ " numbers, pound sign (#) and underscores only.");
				}
			}
			if ((initParams.groupName == null) || (initParams.groupName == "")) {
				safrValidationException.setErrorMessage(Property.GROUP_NAME,
						"Group name cannot be empty.");
			} else {
				if (initParams.groupName.length() > ModelUtilities.MAX_NAME_LENGTH) {
					safrValidationException.setErrorMessage(
							Property.GROUP_NAME, "The length of Group name '"
									+ initParams.groupName + "' cannot"
									+ " exceed 48 characters.");
				}
				if (!safrValidator.isNameValid(initParams.groupName)) {
					safrValidationException
							.setErrorMessage(
									Property.GROUP_NAME,
									"The Group name '"
											+ initParams.groupName
											+ "' should begin"
											+ " with a letter and should comprise of letters,"
											+ " numbers, pound sign (#) and underscores only.");
				}
			}
		}
		if (this.getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment cannot be more than 254 characters.");
		}
		if (!safrValidationException.getErrorMessages().isEmpty())
			throw safrValidationException;
	}

	/**
	 * This method is used to check whether the Component name already exist in
	 * the workbench.
	 * 
	 * @return True if Component with given name exists in database.
	 */
	public boolean isDuplicate() throws DAOException, SAFRException {

		try {
			EnvironmentTransfer envTransfer = null;
			envTransfer = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.getDuplicateEnvironment(getName(), getId());
			if (envTransfer == null) {
				return false;
			} else
				return true;

		} catch (DAOException de) {
			throw new SAFRException("Data access error for Environment", de);
		}

	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		return null;
	}

	/**
	 * This method is used to get a list of all the groups associated to the
	 * environment.
	 * 
	 * @return a list of all the associated groups.
	 * @throws DAOException
	 */
	public SAFRList<GroupEnvironmentAssociation> getAssociatedGroups()
			throws DAOException {
		// Lazily instantiated.
		if (associatedGroups == null) {
			associatedGroups = SAFRAssociationFactory
					.getEnvironmentToGroupAssociations(this,
							SortType.SORT_BY_NAME);
		}
		return associatedGroups;
	}

	/**
	 * This method is used store the associated Groups.
	 * 
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void storeAssociatedGroups() throws DAOException, SAFRException {
		if (SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin(getId())) {
			// save associated Groups

			Map<GroupEnvironmentAssociationTransfer, GroupEnvironmentAssociation> map = new HashMap<GroupEnvironmentAssociationTransfer, GroupEnvironmentAssociation>();

			List<GroupEnvironmentAssociationTransfer> newAssociatedGrpsTrnsList = new ArrayList<GroupEnvironmentAssociationTransfer>();
			List<GroupEnvironmentAssociationTransfer> oldAssociatedGrpsTrnsList = new ArrayList<GroupEnvironmentAssociationTransfer>();
			List<GroupEnvironmentAssociationTransfer> deletedAssociatedGrpsTrnsList = new ArrayList<GroupEnvironmentAssociationTransfer>();

			int associationSize = associatedGroups.size();
			for (int i = 0; i < associationSize; i++) {
				GroupEnvironmentAssociation association = associatedGroups
						.get(i);
				if (association.getPersistence() == SAFRPersistence.DELETED) {
					GroupEnvironmentAssociationTransfer assocTrans = new GroupEnvironmentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					deletedAssociatedGrpsTrnsList.add(assocTrans);
				} else if ((association.getPersistence() == SAFRPersistence.NEW)) {
					// create transfer object
					GroupEnvironmentAssociationTransfer assocTrans = new GroupEnvironmentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					newAssociatedGrpsTrnsList.add(assocTrans);
					map.put(assocTrans, association);
				} else if ((association.getPersistence() == SAFRPersistence.MODIFIED)) {
					GroupEnvironmentAssociationTransfer assocTrans = new GroupEnvironmentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					oldAssociatedGrpsTrnsList.add(assocTrans);
					map.put(assocTrans, association);
				}

			}

			// call DAO to add ,delete,update, if any list has elements.
			if ((newAssociatedGrpsTrnsList.size() > 0)
					|| (oldAssociatedGrpsTrnsList.size() > 0)
					|| (deletedAssociatedGrpsTrnsList.size() > 0)) {
				// CQ 7826 Santhosh 26/05/2010 Implement 2-phase commit DB
				// transactionality
				boolean success = false;
				List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();
				try {

					while (!success) {
						try {
							// Begin Transaction
							DAOFactoryHolder.getDAOFactory().getDAOUOW()
									.begin();
							DAOFactoryHolder.getDAOFactory().getGroupDAO()
									.persistGroupEnvironmentAssociations(
											newAssociatedGrpsTrnsList,
											oldAssociatedGrpsTrnsList,
											deletedAssociatedGrpsTrnsList);
							associatedGroups.flushDeletedItems();
							for (GroupEnvironmentAssociationTransfer assocTrans : map
									.keySet()) {

								// CQ8815;Mustufa
								assocTrans.setPersistent(true);

								map.get(assocTrans).setObjectFromTransfer(
										assocTrans);
								savedObjs.add(map.get(assocTrans));
							}

							success = true;
						} catch (DAOUOWInterruptedException e) {
							// UOW interrupted so retry it
							continue;
						}

					} // end while(!success)
				}

				finally {

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
		} else {
			throw new SAFRException(
					"The current user is not authorized to associate groups to environment.");
		}

	}

	/**
	 * This method is used store the Components associated with the group with
	 * their read,modify and delete rights.
	 * 
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void storeComponentRights() throws DAOException, SAFRException {
		for (Group group : associatedGroupsList) {
			group.storeComponentRights();
		}
	}

	public void setAssociatedGroupsList(List<Group> associatedGroupsList) {
		this.associatedGroupsList = associatedGroupsList;
	}

	public void setCrRequired(boolean selection) {
		crRequired = selection;
		
	}
}
