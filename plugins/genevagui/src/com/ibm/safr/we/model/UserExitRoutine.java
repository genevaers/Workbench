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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.utilities.Migration;
import com.ibm.safr.we.security.UserSession;

/**
 * This Class represents a SAFR UserExitRoutine.
 * 
 */
public class UserExitRoutine extends SAFREnvironmentalComponent {
	private Code typeCode;
	private Code languageCode;
	private String executable;
	private Boolean optimize;

	private List<String> loadWarnings;
	private Set<Integer> deactivatedViewList = new HashSet<Integer>();
	
	private Migration migrationCallback; // CQ10221

	/**
	 * This constructor is used when defining a new UserExitRoutine in the
	 * application. It will initialize the UserExitRoutine ID to zero and
	 * EnvironmentId to specified EnvironmentId to which UserExitRoutine belongs
	 * to. The UserExitRoutine ID will be reset automatically to a unique value
	 * when the UserExitRoutine object is persisted via its <code>store()</code>
	 * method.
	 * 
	 */
	public UserExitRoutine(Integer environmentId) {
		super(environmentId);
	}

	/**
	 * Create an UserExitRoutine object containing the data in the specified
	 * transfer object. Used to instantiate existing UserExitRoutine objects.
	 * 
	 * @param trans
	 *            UserExitRoutineTransfer data transfer object
	 */
	UserExitRoutine(UserExitRoutineTransfer trans) {
		super(trans);
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		UserExitRoutineTransfer trans = (UserExitRoutineTransfer) safrTrans;

		loadWarnings = new ArrayList<String>();

		try {
			this.typeCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.EXITTYPE).getCode(trans.getTypeCode()); // non-null
		} catch (IllegalArgumentException iae) {
			loadWarnings
					.add("This User-Exit Routine does not have a valid type. Please select a valid type before saving.");
			this.typeCode = null;
		}
		try {
			this.languageCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.PROGTYPE).getCode(trans.getLanguageCode()); // non-null
		} catch (IllegalArgumentException iae) {
			loadWarnings
					.add("This User-Exit Routine does not have a valid language. Please select a valid language before saving.");
			this.languageCode = null;
		}
		this.executable = trans.getExecutable();
		this.optimize = trans.isOptimize();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		UserExitRoutineTransfer trans = (UserExitRoutineTransfer) safrTrans;
		trans.setTypeCode(typeCode.getKey()); // non-null
		trans.setLanguageCode(languageCode.getKey()); // non-null
		trans.setExecutable(executable);
		trans.setOptimize(optimize);
	}

	/**
	 * Get typeCode of the UserExitRoutine.
	 * 
	 * @return typeCode of the UserExitRoutine.
	 */
	public Code getTypeCode() {
		return typeCode;
	}

	/**
	 * Set typeCode of the UserExitRoutine.
	 * 
	 * @param typeCode
	 *            to set typeCode of the UserExitRoutine.
	 * @throws NullPointerException
	 *             if the Code object is null, as this is a mandatory field and
	 *             cannot be null.
	 */
	public void setTypeCode(Code typeCode) {
		if (typeCode == null) {
			throw new NullPointerException("Type code cannot be null.");
		}
		this.typeCode = typeCode;
		markModified();
	}

	/**
	 * Get languageCode of the UserExitRoutine.
	 * 
	 * @return languageCode of the UserExitRoutine.
	 */
	public Code getLanguageCode() {
		return languageCode;
	}

	/**
	 * Set languageCode of the UserExitRoutine.
	 * 
	 * @param languageCode
	 *            to set languageCode of the UserExitRoutine.
	 * @throws NullPointerException
	 *             if the Code object is null, as this is a mandatory field and
	 *             cannot be null.
	 */
	public void setLanguageCode(Code languageCode) {
		if (languageCode == null) {
			throw new NullPointerException("Language code cannot be null.");
		}
		this.languageCode = languageCode;
		markModified();
	}

	/**
	 * Get executable of the UserExitRoutine.
	 * 
	 * @return executable of the UserExitRoutine.
	 */
	public String getExecutable() {
		return executable;
	}

	/**
	 * Set executable of the UserExitRoutine.
	 * 
	 * @param executable
	 *            to set executable of the UserExitRoutine.
	 */
	public void setExecutable(String executable) {
		this.executable = executable;
		markModified();
	}

	/**
	 * Check whether the UserExitRoutine is optimized or not.
	 * 
	 * @return true is UserExitRoutine is optimized.
	 */
	public Boolean isOptimize() {
		return optimize;
	}

	/**
	 * Set optimize of the UserExitRoutine.
	 * 
	 * @param optimize
	 *            to set optimize of the UserExitRoutine.
	 */
	public void setOptimize(Boolean optimize) {
		this.optimize = optimize;
		markModified();
	}

	/**
	 * This enum maintains the properties of a User-Exit Routine.
	 * 
	 */
	public enum Property {
		NAME, EXECUTABLE, TYPE, LANGUAGE, COMMENT
	}

	/**
	 * This method is used to validate a User Exit routine object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the User-Exit Routine, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		String name = getName();
		String executable = getExecutable();
		if ((name == null) || (name == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"User Exit Routine name cannot be empty.");
		} else {
			if (name.length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of User Exit Routine name "
		        		+ ModelUtilities.formatNameForErrMsg(
					    getName(),(isForImport() || isForMigration()))						
						+ "cannot exceed 48 characters.");
			}
			else if (this.isDuplicateName()) {
				safrValidationException
						.setErrorMessage(
								Property.NAME,
								"The User Exit Routine name '"
										+ name
										+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(name)) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The User Exit Routine name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),(isForImport() || isForMigration()))
                                        + "should begin"
                                        + " with a letter and should comprise of letters"
                                        + ", numbers, pound sign (#) and underscores only.");
            }
		}
		if ((this.executable == null) || (this.executable == "")) {
			safrValidationException.setErrorMessage(Property.EXECUTABLE,
					"User Exit Routine executable cannot be empty.");
		} else {
			if (this.executable.length() > 18) {
				safrValidationException
						.setErrorMessage(Property.EXECUTABLE,
								"The length of User Exit Routine executable "
					                + ModelUtilities.formatNameForErrMsg(
									executable,(isForImport() || isForMigration()))
									+ "cannot exceed 18 characters.");
			}
			int length = executable.length();
			Boolean flag = false;
			for (int i = 0; i < length; i++) {
				char c = executable.charAt(i);
				if ((!Character.isDigit(c)) && (!Character.isLetter(c))
						&& (!(c == '_')) && (!(c == '#')) && (!(c == '$'))) {
					flag = true;
				}
			}
			if (flag) {
				safrValidationException
						.setErrorMessage(
								Property.EXECUTABLE,
								"The User Exit Routine executable "
  						                + ModelUtilities.formatNameForErrMsg(
									    executable,(isForImport() || isForMigration()))
										+ "should comprise of letters, numbers, underscores, '$', and '#' only.");
			}
			if (this.isDuplicateExecutable()) {
				safrValidationException
						.setErrorMessage(
								Property.EXECUTABLE,
								"The User Exit Routine executable '"
										+ executable
										+ "' already exists. Please specify a different executable.");
			}
		}

		if (this.typeCode == null) {
			safrValidationException.setErrorMessage(Property.TYPE,
					"User Exit Routine type cannot be empty.");
		}
		if (this.languageCode == null) {
			safrValidationException.setErrorMessage(Property.LANGUAGE,
					"User Exit Routine language cannot be empty.");
		}
		if (getComment() != null
				&& getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment cannot be more than 254 characters.");
		}

		if (!safrValidationException.getErrorMessages().isEmpty()) {
			throw safrValidationException;
		}

        deactivatedViewList.clear();
        
        UserExitRoutineTransfer trans = null;
        if (getId() != null && getId() > 0) {
            trans = DAOFactoryHolder.getDAOFactory().
	        getUserExitRoutineDAO().getUserExitRoutine(getId(), getEnvironmentId());
        }
	    
	    if (trans != null) {
	        
            String prevTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
                    CodeCategories.EXITTYPE).getCode(trans.getTypeCode()).getKey();

            
            // CQ9966 check for cases where exit is WRITE and name/executable changed
            if ((this.typeCode.getKey().equals("WRITE"))) {
                if ( ((trans.getName() != null)) && !((name.equals(trans.getName()))) || 
                     ((trans.getExecutable() != null)) && !(this.executable.equals(trans.getExecutable())) ) {
                    
                    List<DependentComponentTransfer> userExitViewDeps = DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO().
                        getUserExitRoutineLogicViewDeps(
                            SAFRApplication.getUserSession().getEnvironment().getId(), getId());

                    if (userExitViewDeps != null && !userExitViewDeps.isEmpty() ) {
                        
                        if (isForMigration()) {
							// pass Active dependent views to migration
							for (DependentComponentTransfer dep : userExitViewDeps) {
								if (!deactivatedViewList.contains(dep.getId())) {
									migrationCallback.addUserExitViewDependency(getDescriptor(), dep);
									deactivatedViewList.add(dep.getId());
								}
							}
                        } else {
							String dependencies = "Views" + SAFRUtilities.LINEBREAK;
							for (int i = 0; i < userExitViewDeps.size(); i++) {
								Integer viewID = userExitViewDeps.get(i)
										.getId();
								String viewName = userExitViewDeps.get(i)
										.getName();
								if (!deactivatedViewList.contains(viewID)) {
									dependencies += "    " + viewName + " ["
											+ viewID + "]" + SAFRUtilities.LINEBREAK;
									deactivatedViewList.add(viewID);
								}
							}
							String opStr = "";
							if (isForImport()) {
								opStr = "Importing";
							} else {
								opStr = "Saving";
							}
							String contextMsg = "The following Views have a logic text dependency on User Exit Routine "
									+ getName()
									+ "["
									+ getId()
									+ "]. "
									+ opStr
									+ " this User Exit Routine will make them Inactive.";
							if (!getConfirmWarningStrategy().confirmWarning(
									"User Exit Routine dependency warning",
									contextMsg, dependencies)) {
								throw new SAFRCancelException(
										"Cancelled after warning about View logic text dependency on User Exit Routine.");
							}
						}                        
                    }
                } // end if name or executable changed
            } // end if WRITE exit		        
	    }
	}

	/**
	 * This method is used to check whether the User Exit Routine name already
	 * exist in the workbench.
	 * 
	 * @return true if the User Exit Routine Name already exists.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private boolean isDuplicateName() throws DAOException, SAFRException {

		try {
			UserExitRoutineTransfer userExitRoutineTransfer = null;
			userExitRoutineTransfer = DAOFactoryHolder.getDAOFactory()
					.getUserExitRoutineDAO().getDuplicateUserExitRoutine(
							getName(), getId(), getEnvironmentId());
			if (userExitRoutineTransfer == null) {
				return false;
			} else
				return true;
		} catch (DAOException de) {
			throw new SAFRException("Data access error for User Exit Routine.",
					de);
		}

	}

	/**
	 * This method is used to check whether the User Exit Routine executable
	 * already exist in the workbench.
	 * 
	 * @return true if the User Exit Routine executable already exists.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private boolean isDuplicateExecutable() throws DAOException, SAFRException {

		try {
			UserExitRoutineTransfer userExitRoutineTransfer = null;
			userExitRoutineTransfer = DAOFactoryHolder.getDAOFactory()
					.getUserExitRoutineDAO().getDuplicateUserExitExecutable(
							executable, getId(), getEnvironmentId());
			if (userExitRoutineTransfer == null) {
				return false;
			} else
				return true;
		} catch (DAOException de) {
			throw new SAFRException("Data access error for User Exit Routine.",
					de);
		}

	}

	@Override
	public void store() throws SAFRException, DAOException {
		UserSession userSession = SAFRApplication.getUserSession();
		if (isForMigration()) {
			if (userSession.isOrdinaryUser(this.getEnvironmentId())
					&& !userSession.hasPermission(Permissions.MigrateIn,
							this.getEnvironmentId())) {
				String msg = "The user is not authorized to migrate into Environment "
						+ getEnvironmentId();
				throw new SAFRException(msg);
			}
		} else {
			// CQ 7705 Shruti Shukla(21/04/10) To implement security at model
			// layer.
			if (this.id == 0) {
				if (!hasCreatePermission()) {
					throw new SAFRException(
							"The user is not authorized to create a new user exit routine.");
				}
			} else {
				if (!hasUpdateRights()) {
					throw new SAFRException(
							"The user is not authorized to update this user exit routine.");
				}
			}
		}
		
		UserExitRoutineTransfer trans = new UserExitRoutineTransfer();
		setTransferData(trans);
		try {
            // in-activate dependent views.
            if (!deactivatedViewList.isEmpty()) {
                DAOFactoryHolder.getDAOFactory().getViewDAO()
                    .makeViewsInactive(deactivatedViewList, getEnvironmentId());
            }
		    
			trans = DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO()
					.persistUserExitRoutine(trans);
			setObjectData(trans);
			
			if (isForMigration()) {
				if (userSession.getEditRights(ComponentType.UserExitRoutine, 
				    this.id,this.getEnvironmentId()) == EditRights.None) {
					// no edit rights so assign read rights
					userSession.getGroup().assignComponentEditRights(this,
							ComponentType.UserExitRoutine, EditRights.Read);
				}
			} else {
				// JAK: note, this does not replace any existing rights.
				// CQ 7675 Nikita 16/04/2010 Assign full rights to general user
				// on the User Exit Routine created by him
				if (!SAFRApplication.getUserSession().getUser().isSystemAdmin()) {
					SAFRApplication
							.getUserSession()
							.getGroup()
							.assignComponentFullRights(this,
									ComponentType.UserExitRoutine);
				}
			}

            SAFRApplication.getModelCount().incCount(this.getClass(), 1);       
			
		} catch (SAFRNotFoundException snfe) {
			throw new SAFRException(
					"The user exit routine with id "
							+ this.getId()
							+ " cannot be updated as its already been deleted from the database.",
					snfe);
		}
	
	}

	/**
	 * This method is used to check whether the User Exit Routine has any
	 * dependency.
	 * 
	 * @throws DAOException
	 * @throws SAFRDependencyException
	 */

	private void dependencyCheck() throws DAOException, SAFRDependencyException {
		Map<ComponentType, List<DependentComponentTransfer>> userExitDependencies = DAOFactoryHolder
				.getDAOFactory().getUserExitRoutineDAO()
				.getUserExitRoutineDependencies(
						SAFRApplication.getUserSession().getEnvironment()
								.getId(), getId());

		if (!(userExitDependencies == null)
				&& !(userExitDependencies.isEmpty())) {
			// If user Exit Routine has dependencies then restrict
			// change of type
			throw new SAFRDependencyException(userExitDependencies);
		}
	}

	
	/**
	 * This method is used to save as an user-exit routine based on a new name
	 * and a new executable name.
	 * 
	 * @param newName
	 *            new name of the component to be saved as.
	 * @param executable
	 *            new executable name of the component to be saved as.
	 * @return a new copy of this component.
	 * @throws SAFRValidationException
	 * @throws SAFRException
	 */
	public SAFRComponent saveAs(String newName, String newExecutable)
			throws SAFRValidationException, SAFRException {
		UserExitRoutine userExitCopy = SAFRApplication.getSAFRFactory()
				.createUserExitRoutine();
		userExitCopy.setName(newName);
		userExitCopy.setExecutable(newExecutable);
		userExitCopy.setTypeCode(this.getTypeCode());
		userExitCopy.setLanguageCode(this.getLanguageCode());
		userExitCopy.setComment(this.getComment());
		userExitCopy.setOptimize(this.isOptimize());
		userExitCopy.setConfirmWarningStrategy(this.getConfirmWarningStrategy());
		userExitCopy.validate();
		userExitCopy.store();

		return userExitCopy;
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		return null;
	}

	public List<String> getLoadWarnings() {
		return loadWarnings;
	}
	
	// CQ10221 for handling view dependency warnings
	public void setMigrationCallback(Migration migrationCallback) {
		this.migrationCallback = migrationCallback;
	}

    public Set<Integer> getDeactivatedViews() {
        return deactivatedViewList;        
    }
	
    public String getComboString() {
    	return getName() + " [" + Integer.toString(getId()) + "]   Exec: " + executable;
    }
}
