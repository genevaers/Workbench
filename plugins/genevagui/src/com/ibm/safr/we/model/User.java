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

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserGroupAssociationTransfer;
import com.ibm.safr.we.data.transfer.UserTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.UserGroupAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * Represents a user of the SAFR system.
 * 
 */

public class User extends SAFRPersistentObject {

	private String userid;
	private String password; // TODO encode or encrypt
	private String firstName;
	private String middleInitial;
	private String lastName;
	private String email;
	private int logLevel; // TODO confirm data type
	private int maxCompileErrors;
	private Integer defaultEnvironmentId; // set at instantiation
	private Environment defaultEnvironment; // lazily initialized
	private Integer defaultViewFolderId; // set at instantiation
	private ViewFolder defaultViewFolder; // lazily initialized
	private boolean authenticated = false;
	private Integer defaultGroupId; // set at instantiation
	private Group defaultGroup; // lazily initialized
	private boolean isSystemAdmin;
	private SAFRAssociationList<UserGroupAssociation> userGroupAssociations;

	/**
	 * Create a User with the specified userid. To be used when defining a new
	 * user in the application. Only the User's userid will be set. All other
	 * User attributes will need to be set after the User object is created.
	 * 
	 * @param userid
	 *            userid.
	 */
	public User(String id) {
		super();
		setPersistence(SAFRPersistence.NEW);
		this.userid = id;
		userGroupAssociations = new SAFRAssociationList<UserGroupAssociation>();

	}

	/**
	 * Create a User populated with the data in the specified data transfer
	 * object. For instantiating existing users, already defined in the
	 * application. The object's attributes will be set using the transfer
	 * object.
	 * 
	 * @param trans
	 *            the UserTransfer object containing the state of this object.
	 */
	protected User(UserTransfer trans) {
		super(trans);
		setPersistence(SAFRPersistence.OLD);
	}

	/**
	 * Implements inherited abstract method. Copies data from the transfer
	 * object into this object.
	 */
	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		UserTransfer trans = (UserTransfer) safrTrans;
		this.userid = trans.getUserid();
		// CQ 7824 Nikita. 23/04/2010. Don't decode SAFR password on DB
		// retrieval.
		this.password = trans.getPassword();
		this.setComment(trans.getComments());
		this.firstName = trans.getFirstName();
		this.middleInitial = trans.getMiddleInitial();
		this.lastName = trans.getLastName();
		this.email = trans.getEmail();
		this.logLevel = trans.getLogLevel();
		this.maxCompileErrors = trans.getMaxCompileErrors();
		this.defaultEnvironmentId = trans.getDefaultEnvironmentId();
		if (defaultEnvironment != null
				&& defaultEnvironment.getId() != trans
						.getDefaultEnvironmentId()) {
			this.defaultEnvironment = null;
		}
		this.defaultViewFolderId = trans.getDefaultViewFolderId();
		if (defaultViewFolder != null
				&& defaultViewFolder.getId() != trans.getDefaultViewFolderId()) {
			this.defaultViewFolder = null;
		}
		this.isSystemAdmin = trans.isAdmin();
		this.defaultGroupId = trans.getDefaultGroupId();
		if (defaultGroup != null
				&& defaultGroup.getId() != trans.getDefaultGroupId()) {
			this.defaultGroup = null;
		}
	}

	/**
	 * Implements inherited abstract method. Copies data from this object into
	 * the transfer data object.
	 */
	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		UserTransfer trans = (UserTransfer) safrTrans;
		trans.setPersistent(isPersistent());
		trans.setUserid(userid);
		// CQ 7824 Nikita. 23/04/2010. Set the already encrypted pwd in the
		// transfer object.
		trans.setPassword(password);
		trans.setComments(getComment());
		trans.setCreateBy(getCreateBy());
		trans.setModifyBy(getModifyBy());
		trans.setFirstName(firstName);
		trans.setMiddleInitial(middleInitial);
		trans.setLastName(lastName);
		trans.setEmail(email);
		trans.setLogLevel(logLevel);
		trans.setMaxCompileErrors(maxCompileErrors);
		trans.setDefaultEnvironmentId(defaultEnvironmentId);
		trans.setDefaultViewFolderId(defaultViewFolderId);
		trans.setAdmin(isSystemAdmin);
		if (!isSystemAdmin) {
			// only a general user has a default group
			trans.setDefaultGroupId(defaultGroupId);
		} else {
			trans.setDefaultGroupId(0);
		}
	}

	/**
	 * Get unique userid of the User.
	 * 
	 * @return userid of the User.
	 */
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userId) {
		this.userid = userId;
		markModified();
	}

	// There is no setter method for ID, as it can only be set by a
	// constructor.
	/**
	 * Get unique password of the User.
	 * 
	 * @return password of the User.
	 */
	public String getPassword() {
		return "";
	}

;	public void setPassword(String password) {
	}

	/**
	 * Get unique firstName of the User.
	 * 
	 * @return firstName of the User.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Set firstName of the User.
	 * 
	 * @param firstName
	 *            to set firstName of the User.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
		markModified();
	}

	/**
	 * Get unique middleInitial of the User.
	 * 
	 * @return middleInitial of the User.
	 */
	public String getMiddleInitial() {
		return middleInitial;
	}

	/**
	 * Set middleInitial of the User.
	 * 
	 * @param middleInitial
	 *            to set middleInitial of the User.
	 */
	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
		markModified();
	}

	/**
	 * Get unique lastName of the User.
	 * 
	 * @return lastName of the User.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Set lastName of the User.
	 * 
	 * @param lastName
	 *            to set lastName of the User.
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
		markModified();
	}

	/**
	 * Get email of the User.
	 * 
	 * @return email of the User.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set email of the User.
	 * 
	 * @param email
	 *            to set email of the User.
	 */
	public void setEmail(String email) {
		this.email = email;
		markModified();
	}

	/**
	 * Get logLevel of the User.
	 * 
	 * @return logLevel of the User.
	 */
	public int getLogLevel() {
		return logLevel;
	}

	/**
	 * Set logLevel of the User.
	 * 
	 * @param logLevel
	 *            to set logLevel of the User.
	 */
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
		markModified();
	}

	/**
	 * Get maxCompileErrors of the User.
	 * 
	 * @return maxCompileErrors of the User.
	 */
	public int getMaxCompileErrors() {
		return maxCompileErrors;
	}

	/**
	 * Set maxCompileErrors of the User.
	 * 
	 * @param maxCompileErrors
	 *            to set maxCompileErrors of the User.
	 */
	public void setMaxCompileErrors(int maxCompileErrors) {
		this.maxCompileErrors = maxCompileErrors;
		markModified();
	}

	/**
	 * Get default Environment of the User.
	 * 
	 * @return default Environment of the User. Returns null if there is no
	 *         default Environment specified for the user or if the default
	 *         Environment specified is not found in the database.
	 * @throws SAFRException
	 */
	public Environment getDefaultEnvironment() throws SAFRException {
		if (defaultEnvironment == null) {
			if (defaultEnvironmentId != null && defaultEnvironmentId > 0) {
				try {
					this.defaultEnvironment = SAFRApplication.getSAFRFactory()
							.getEnvironment(defaultEnvironmentId);
				} catch (SAFRNotFoundException snfe) {
					// if not found return NULL
				}
			}
		}
		return defaultEnvironment;
	}

	/**
	 * Set defaultEnvironment of the User.
	 * 
	 * @param defaultEnvironment
	 *            to set defaultEnvironment of the User.
	 */
	public void setDefaultEnvironment(Environment defaultEnvironment) {
		this.defaultEnvironment = defaultEnvironment;
		if (defaultEnvironment == null) {
			this.defaultEnvironmentId = 0;
		} else {
			this.defaultEnvironmentId = defaultEnvironment.getId();
		}
		markModified();
	}

	/**
	 * Get default View Folder of the User.
	 * 
	 * @return default View Folder of the User. Returns null if there is no
	 *         default View Folder specified for the user or if the default View
	 *         Folder specified is not found in the given Environment in the
	 *         database.
	 * @throws SAFRException
	 */
	public ViewFolder getDefaultViewFolder() throws SAFRException {
		if (defaultViewFolder == null) {
			if (defaultViewFolderId != null && defaultViewFolderId > 0) {
				try {
					this.defaultViewFolder = SAFRApplication.getSAFRFactory()
							.getViewFolder(defaultViewFolderId,
									defaultEnvironmentId);
				} catch (SAFRNotFoundException snfe) {
					// if not found return NULL
				}
			}
		}
		return defaultViewFolder;
	}

	public Integer getDefaultViewFolderId() {
		return defaultViewFolderId;
	}

	Integer getDefaultEnvironmentId() {
		return defaultEnvironmentId;
	}

	/**
	 * Set defaultViewFolder of the User.
	 * 
	 * @param defaultViewFolder
	 *            to set defaultViewFolder of the User.
	 */
	public void setDefaultViewFolder(ViewFolder defaultViewFolder) {
		this.defaultViewFolder = defaultViewFolder;
		if (defaultViewFolder == null) {
			this.defaultViewFolderId = 0;
		} else {
			this.defaultViewFolderId = defaultViewFolder.getId();
		}
		markModified();
	}

	/**
	 * Get the default Group of the user. Only a general user has a default
	 * Group associated with it. If the user is a System Administrator, this
	 * method will return null.
	 * 
	 * @return default {@link Group} of the {@link User}. Returns null if there
	 *         is no default Group specified for the user or if the default
	 *         Group specified is not found in the database.
	 * @throws SAFRException
	 */
	public Group getDefaultGroup() throws SAFRException {
		if (isSystemAdmin) {
			return null;
		}
		if (defaultGroup == null) {
			if (defaultGroupId != null && defaultGroupId > 0) {
				try {
					this.defaultGroup = SAFRApplication.getSAFRFactory()
							.getGroup(defaultGroupId);
				} catch (SAFRNotFoundException snfe) {
					// if not found return NULL
				}
			}
		}
		return defaultGroup;
	}

	/**
	 * Set a default Group for the user.
	 * 
	 * @param defaultGroup
	 *            to set for the user.
	 * @throws IllegalArgumentException
	 *             if the user is a System Administrator, as only a general user
	 *             has a default Group associated with it.
	 */
	public void setDefaultGroup(Group defaultGroup) {
		if (isSystemAdmin) {
			throw new IllegalArgumentException();
		} else {
			this.defaultGroup = defaultGroup;
			if (defaultGroup == null) {
				this.defaultGroupId = 0;
			} else {
				this.defaultGroupId = defaultGroup.getId();
			}
			markModified();
		}
	}

	/**
	 * Get whether User is Authenticated or not.
	 * 
	 * @return true if the User is authenticated else false.
	 */
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	public void setUserPreferences(Integer environmentId, Integer groupId)
			throws DAOException {
		if (groupId == null) {
			groupId = new Integer(0);
		}
		DAOFactoryHolder.getDAOFactory().getUserDAO().setUserPreferences(
				this.getUserid(), environmentId, groupId);
	}

	/**
	 * Get whether the User is a System Administrator or not.
	 * 
	 * @return true if the User is a System Administrator, and false if he is a
	 *         General User.
	 */
	public boolean isSystemAdmin() {
		return isSystemAdmin;
	}

	/**
	 * Set whether the User is a System Administrator or not. The default group
	 * is set to null and the default group id is set to 0 when a user is
	 * converted to a System Administrator, as a System Administrator does not
	 * have a default group associated with it.
	 * 
	 * @param isSystemAdmin
	 *            true if the User is a System Administrator, and false if he is
	 *            a General User.
	 */
	public void setSystemAdmin(boolean isSystemAdmin) {
		if (isSystemAdmin) {
			this.defaultGroup = null;
			this.defaultGroupId = 0;
		}
		this.isSystemAdmin = isSystemAdmin;
		markModified();
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// CQ 7705 Shruti Shukla(22/04/10) To implement security at model layer.
		if (SAFRApplication.getUserSession().isSystemAdministrator()
				|| SAFRApplication.getUserSession().getUser().getUserid()
						.equals(this.userid)) {
			UserTransfer trans = new UserTransfer();
			setTransferData(trans);
			// CQ 7329 Kanchan Rauthan 04/03/2010 To show error if user is
			// already deleted from database and user still tries to save it.
			try {
				trans = DAOFactoryHolder.getDAOFactory().getUserDAO()
						.persistUser(trans);
				setObjectData(trans);
				setPersistence(SAFRPersistence.OLD);
			} catch (SAFRNotFoundException snfe) {
				snfe.printStackTrace();
				throw new SAFRException(
						"The user with id "
								+ this.getUserid()
								+ " cannot be updated as its already been deleted from the database.",
						snfe);
			}
		} else {
			if (this.userid != null) {
				throw new SAFRException(
						"The user is not authorized to update this user.");
			} else {
				throw new SAFRException(
						"The user is not authorized to create a new user.");
			}
		}

	}

	/**
	 * This enum maintains the properties of a user.
	 * 
	 */
	public enum Property {
		USERID, MAX_COMPILATION_ERROR, PASSWORD, LAST_NAME, FIRST_NAME, COMMENT
	}

	/**
	 * Validate method is used to validate a User object.If any validation
	 * condition is not met then this method throws a list of all the error
	 * messages.
	 * 
	 * @throws SAFRException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the user, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		SAFRValidationException safrValidationException = new SAFRValidationException();
		if ((this.userid == null) || (this.userid == "")) {
			safrValidationException.setErrorMessage(Property.USERID,
					"User ID cannot be empty.");
		} else {
			if (this.userid.length() < 4 || this.userid.length() > 8) {
				safrValidationException
						.setErrorMessage(
								Property.USERID,
								"Length of User ID '"
										+ userid
										+ "' should be greater than or equal to 4 and less than or equal to 8 characters.");
			} else {

				if (this.userid.contains(" ")) {
					safrValidationException.setErrorMessage(Property.USERID,
							"User ID '" + userid
									+ "' should not contain spaces.");
				}

				int length = userid.length();
				Boolean flag = false;
				for (int i = 0; i < length; i++) {
					char c = userid.charAt(i);
					if ((!Character.isDigit(c)) && (!Character.isLetter(c))
							&& (!(c == '_')) && (!(c == '!')) && (!(c == '@'))
							&& (!(c == '#')) && (!(c == '$')) && (!(c == '%'))
							&& (!(c == '^')) && (!(c == '&')) && (!(c == '*'))) {
						flag = true;
					}
				}
				if (flag) {
					safrValidationException.setErrorMessage(Property.USERID,
					    "The User ID '"+ userid + "' should comprise of letters, numbers, underscores, '!', '@', '$', '%', '^', '&&' and '*' only.");
				}
				if (!isPersistent() && this.isDuplicate()) {
					safrValidationException.setErrorMessage(Property.USERID,
					    "The User ID '"+ userid+ "' already exists.Please specify a different User ID.");
				}

			}
		}
		//CQ8291 Shruti(12/10/10). Moved this check outside else loop.
		if (this.maxCompileErrors < 0) {
			safrValidationException
					.setErrorMessage(Property.MAX_COMPILATION_ERROR,
							"Maximum compilation error should be greater than or equal to zero.");
		}
		if (this.getPassword().length() > 20) {
			safrValidationException.setErrorMessage(Property.PASSWORD,
					"Password length should not be more than 20");
		}
		if (this.firstName.length() > 50) {
			safrValidationException.setErrorMessage(Property.FIRST_NAME,
					"First name should not have length more than 50");
		}
		if (this.lastName.length() > 50) {
			safrValidationException.setErrorMessage(Property.LAST_NAME,
					"Last name should not have length more than 50");
		}
		if (this.getComment().length() > 200) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment length should not be more than 200 characters");
		}
		if (!safrValidationException.getErrorMessages().isEmpty()) {
			throw safrValidationException;
		}
	}

	private boolean isDuplicate() throws DAOException, SAFRException {
		UserTransfer userTransfer = null;
		userTransfer = DAOFactoryHolder.getDAOFactory().getUserDAO()
				.getDuplicateUser(userid);
		if (userTransfer == null) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Temporary workaround to make user screen work. should be removed later.
	 * 
	 * @param persistent
	 */
	public void setPersistent(boolean persistent) {
		if (persistent) {
			setPersistence(SAFRPersistence.OLD);
		} else {
			setPersistence(SAFRPersistence.NEW);
		}
		markModified();
	}

	/**
	 * This method is used to get the associated groups of the User.
	 * 
	 * @return userGroupAssociation.
	 * @throws SAFRException
	 */
	public SAFRAssociationList<UserGroupAssociation> getAssociatedGroups()
			throws SAFRException {
		if (userGroupAssociations == null) {
//			userGroupAssociations = new SAFRAssociationList<UserGroupAssociation>();
//		}
		List<UserGroupAssociationTransfer> groupUserAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getUserDAO().getAssociatedGroups(
						this.getUserid());
		SAFRAssociationList<UserGroupAssociation> userGroupAssociationsList = new SAFRAssociationList<UserGroupAssociation>();
		for (UserGroupAssociationTransfer groupUserAssociationTransfer : groupUserAssociationTransfers) {

			UserGroupAssociation userGroupAssoc = new UserGroupAssociation(
					groupUserAssociationTransfer);
			userGroupAssociationsList.add(userGroupAssoc);
			this.userGroupAssociations = userGroupAssociationsList;
		}
		}
		
		return userGroupAssociations;
	}

	public User saveAs(String newUserId, Boolean storeAsssociatedGroups)
			throws SAFRValidationException, SAFRException {
		User userCopy = SAFRApplication.getSAFRFactory().createUser();
		userCopy.setUserid(newUserId);
		userCopy.setPassword(this.getPassword());
		userCopy.setSystemAdmin(this.isSystemAdmin());
		userCopy.setComment(this.getComment());
		userCopy.setFirstName(this.getFirstName());
		userCopy.setMiddleInitial(this.getMiddleInitial());
		userCopy.setLastName(this.getLastName());
		userCopy.setEmail(this.getEmail());
		userCopy.setLogLevel(this.getLogLevel());
		userCopy.setMaxCompileErrors(this.getMaxCompileErrors());
		userCopy.setDefaultEnvironment(this.getDefaultEnvironment());
		if (!isSystemAdmin) {
			userCopy.setDefaultGroup(this.getDefaultGroup());
		}
		userCopy.setDefaultViewFolder(this.getDefaultViewFolder());
		userCopy.validate();

		// CQ 7826 Santhosh 26/05/2010 Implement 2-phase commit DB
		// transactionality
		boolean success = false;
		try {

			while (!success) {
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();

					userCopy.store();
					// store associated groups of the userCopy if the user
					// selects yes in
					// the UI Layer.
					if (storeAsssociatedGroups) {
						SAFRAssociationList<UserGroupAssociation> usrCopyUserGroupAssocs = userCopy
								.getAssociatedGroups();
						for (UserGroupAssociation userGroupAssociation : this
								.getAssociatedGroups()) {
							UserGroupAssociation userGroup = new UserGroupAssociation(
									userCopy, userGroupAssociation
											.getAssociatedComponentIdNum(),
									userGroupAssociation
											.getAssociatingComponentName());

							usrCopyUserGroupAssocs.add(userGroup);
						}

						userCopy.storeAssociatedGroups(usrCopyUserGroupAssocs,
								userCopy);
					}

					success = true;
				} catch (DAOUOWInterruptedException e) {
					// UOW interrupted so retry it
					continue;
				}

			} // end while(!success)

		} catch (DAOException de) {
			de.printStackTrace();

		} finally {

			if (success) {
				// End Transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().end();

			} else {
				// Rollback the transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
			}
		}

		return userCopy;
	}

    /**
     * This method is used store the associated users.
     * 
     * @throws DAOException
     * @throws SAFRException
     */
    public void storeGroupAssociations() throws DAOException, SAFRException {
        // save associated Users
    	List<Integer> deletionIds = new ArrayList<>();
        List<UserGroupAssociationTransfer> list = new ArrayList<UserGroupAssociationTransfer>();
        HashMap<UserGroupAssociationTransfer, UserGroupAssociation> map = 
            new HashMap<UserGroupAssociationTransfer, UserGroupAssociation>();
        
        for (UserGroupAssociation association : userGroupAssociations) {
            if (association.getPersistence() == SAFRPersistence.DELETED) {
                deletionIds.add(association.getAssociatedComponentIdNum());

            } else {
                if (association.getPersistence() == SAFRPersistence.NEW) {
                    // create transfer object
                    UserGroupAssociationTransfer assocTrans = new UserGroupAssociationTransfer();
                    association.setTransferFromObject(assocTrans);
                    list.add(assocTrans);
                    map.put(assocTrans, association);
                }
            }            
        }

        boolean success = false;
        List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();
        try {

            while (!success) {
                try {
                    // Begin Transaction
                    DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();

                    // call DAO to delete
                    if (deletionIds.size() > 0) {
                        DAOFactoryHolder.getDAOFactory().getUserDAO().deleteAssociatedGroups(
                            userid, deletionIds);
                    }

                    // call DAO to add
                    if (list.size() > 0) {
                        list = DAOFactoryHolder.getDAOFactory().getUserDAO().persistAssociatedGroups(
                            list, userid);
                        for (UserGroupAssociationTransfer assocTrans : list) {
                            map.get(assocTrans).setObjectFromTransfer(assocTrans);
                            savedObjs.add(map.get(assocTrans));
                        }
                    }
                    userGroupAssociations.flushDeletedItems();

                    success = true;
                } catch (DAOUOWInterruptedException e) {
                    // UOW interrupted so retry it
                    continue;
                }

            } // end while(!success)

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
	
	private void storeAssociatedGroups(
			SAFRList<UserGroupAssociation> assocList, User userCopy)
			throws DAOException, SAFRException {
		// save associated Users
		List<UserGroupAssociationTransfer> list = new ArrayList<UserGroupAssociationTransfer>();
		int associationSize = assocList.size();
		for (int i = 0; i < associationSize; i++) {
			UserGroupAssociation association = assocList.get(i);
			if (association.getPersistence() == SAFRPersistence.NEW) {

				// create transfer object
				UserGroupAssociationTransfer assocTrans = new UserGroupAssociationTransfer();
				association.setTransferFromObject(assocTrans);
				list.add(assocTrans);
			}
		}

		// call DAO to add
		if (list.size() > 0) {
			list = DAOFactoryHolder.getDAOFactory().getUserDAO()
					.persistAssociatedGroups(list, userCopy.getUserid());
		}

	}
	
	public SAFRAssociationList<UserGroupAssociation> getuserGroupAssociations(){
		return this.userGroupAssociations;
	}
}
