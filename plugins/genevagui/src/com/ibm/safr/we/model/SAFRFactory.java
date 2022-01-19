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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.CodeTransfer;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.EnvironmentTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupTransfer;
import com.ibm.safr.we.data.transfer.GroupUserAssociationTransfer;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.UserTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.base.SAFRObject;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class should be used for creating model objects. Methods which do
 * not have an environment ID parameter will use the environment the user is
 * currently logged into.
 */
public class SAFRFactory extends SAFRObject {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.SAFRFactory");
    
	private Map<String, CodeSet> codeSets = null; // lazily initialized
	private Map<Integer, UserExitRoutine> userExitRoutines = new HashMap<Integer, UserExitRoutine>();

	protected SAFRFactory() {
	} // package private

	/**
	 * This method creates new User.
	 * 
	 * @return User object
	 */
	public User createUser() {
		return new User("");
	}

	/**
	 * This method is used to SAFR user from SAFR database.
	 * 
	 * @param id
	 *            of the user.
	 * @return user to which id belong to.
	 * @throws SAFRNotFoundException
	 *             If user with the specified Id is not present in the database.
	 * @throws SAFRException
	 */
	public User getUser(String id) throws SAFRException {

		User user = null;
		UserTransfer userTransfer = null;
		userTransfer = DAOFactoryHolder.getDAOFactory().getUserDAO()
				.getUser(id);

		if (userTransfer == null) {
			throw new SAFRNotFoundException("User '" + id + "' not found.");
		} else {
			user = new User(userTransfer);
		}
		return user;
	}

	/**
	 * Creates new Environment.
	 * 
	 * @return Environment object.
	 */
	public Environment createEnvironment() {
		return new Environment();
	}

	/**
	 * This methods is used to get Environment having specified id.
	 * 
	 * @param id
	 *            of the Environment to get.
	 * @return Environment object having specified id.
	 * @throws SAFRNotFoundException
	 *             If Environment with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public Environment getEnvironment(Integer id) throws SAFRException {

		Environment env = null;
		EnvironmentTransfer envTransfer = null;
		envTransfer = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
				.getEnvironment(id);
		if (envTransfer == null) {
			throw new SAFRNotFoundException("Environment '" + id
					+ "' not found.");
		} else {
			env = new Environment(envTransfer);
		}
		return env;
	}

	/**
	 * This methods is used to get Environment having specified id.
	 * 
	 * @param id
	 *            of the Environment to get.
	 * @return Environment object having specified id.
	 * @throws SAFRNotFoundException
	 *             If Environment with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public Environment getEnvironment(String name) throws SAFRException {

		Environment env = null;
		EnvironmentTransfer envTransfer = null;
		envTransfer = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO().getEnvironment(name);
		if (envTransfer == null) {
			throw new SAFRNotFoundException("Environment '" + name + "' not found.");
		} else {
			env = new Environment(envTransfer);
		}
		return env;
	}
	
	/**
	 * Creates new View Folder in the current Environment..
	 * 
	 * @return View Folder object.
	 */

	public ViewFolder createViewFolder() {
		return new ViewFolder(getCurrentEnvironmentId());
	}

	/**
	 * This method is used to get view folder from SAFR database with specified
	 * view folder id in the current environment.
	 * 
	 * @param id
	 *            of the view folder to search in SAFR database.
	 * @return view folder having specified id.
	 * @throws SAFRException
	 */
	public ViewFolder getViewFolder(Integer id) throws SAFRException {
		return getViewFolder(id, getCurrentEnvironmentId());
	}

	/**
	 * This method is used to get view folder from SAFR database with specified
	 * view folder id and environment id.
	 * 
	 * @param id
	 *            of the view folder to search in SAFR database.
	 * @param environId
	 *            Environment Id from which the viewfolders are to be retrieved.
	 * @return view folder having specified id.
	 * @throws SAFRNotFoundException
	 *             If ViewFolder with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public ViewFolder getViewFolder(Integer id, Integer environId)
			throws SAFRException {
		ViewFolder viewfolder = null;
		ViewFolderTransfer viewfolderTransfer = null;
		viewfolderTransfer = DAOFactoryHolder.getDAOFactory()
				.getViewFolderDAO().getViewFolder(id, environId);
		if (viewfolderTransfer == null) {
			throw new SAFRNotFoundException("View Folder '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			viewfolder = new ViewFolder(viewfolderTransfer);
		}
		return viewfolder;
	}

	/**
	 * Creates new SAFR group.
	 * 
	 * @return newly created SAFR group.
	 */

	public Group createGroup() {
		return new Group();
	}

	/**
	 * This method is used to get Group from SAFR database with specified Group
	 * id.
	 * 
	 * @param id
	 *            of the group to search in SAFR database.
	 * @return Group having specified id.
	 * @throws SAFRNotFoundException
	 *             If Group with a specified Id is not present in the database.
	 * @throws SAFRException
	 */
	public Group getGroup(Integer id) throws SAFRException {

		Group grp = null;
		GroupTransfer grpTransfer = null;
		grpTransfer = DAOFactoryHolder.getDAOFactory().getGroupDAO().getGroup(
				id);
		if (grpTransfer == null) {
			throw new SAFRNotFoundException("Group '" + id + "' not found.");
		} else {
			grp = new Group(grpTransfer);
		}
		return grp;

	}

	/**
	 * This method is used to get Group from SAFR database with specified Group
	 * name.
	 * 
	 * @param id
	 *            of the group to search in SAFR database.
	 * @return Group having specified id.
	 * @throws SAFRNotFoundException
	 *             If Group with a specified Id is not present in the database.
	 * @throws SAFRException
	 */
	public Group getGroup(String name) throws SAFRException {

		Group grp = null;
		GroupTransfer grpTransfer = null;
		grpTransfer = DAOFactoryHolder.getDAOFactory().getGroupDAO().getGroup(name);
		if (grpTransfer == null) {
			throw new SAFRNotFoundException("Group '" + name + "' not found.");
		} else {
			grp = new Group(grpTransfer);
		}
		return grp;

	}
	
	/**
	 * Creates new Control Record in the current Environment..
	 * 
	 * @return Control Record object.
	 */
	public ControlRecord createControlRecord() {
		return new ControlRecord(getCurrentEnvironmentId());
	}

	/**
	 * Initializes and returns a new Control Record object created using a
	 * transfer object.
	 * 
	 * @return Control Record object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public ControlRecord initControlRecord(ControlRecordTransfer trans)
			throws DAOException, SAFRException {
		return new ControlRecord(trans);
	}

	/**
	 * This gets the Control Record with a specified id from the
	 * users currently logged in environment.
	 * 
	 * @param id
	 *            of the Control Record to get.
	 * @return Control Record having specified id.
	 * @throws SAFRNotFoundException
	 *             If ControlRecord with a specified Id is not in the
	 *             database.
	 * @throws SAFRException
	 */
	public ControlRecord getControlRecord(Integer id) throws SAFRException {
		return getControlRecord(id, getCurrentEnvironmentId());
	}
	/**
	 * This method is used to get Control Record with a specified id from the
	 * specified environment.
	 * 
	 * @param id
	 *            of the Control Record to get.
	 * @param environId 
     *            the Environment containing the Control Record
	 * @return Control Record having specified id.
	 * @throws SAFRNotFoundException
	 *             If ControlRecord with a specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public ControlRecord getControlRecord(Integer id, Integer environId)
			throws SAFRException {
		ControlRecord controlRecord = null;
		ControlRecordTransfer controlRecordTransfer = null;
		controlRecordTransfer = DAOFactoryHolder.getDAOFactory()
				.getControlRecordDAO().getControlRecord(id, environId);
		if (controlRecordTransfer == null) {
			throw new SAFRNotFoundException("Control Record '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			controlRecord = new ControlRecord(controlRecordTransfer);
		}
		return controlRecord;
	}

	/**
	 * Returns the CodeSet for the specified code category.
	 * 
	 * @param codeCategory
	 *            the name of the CodeSet.
	 * @return the required CodeSet
	 * @throws IllegalStateException
	 *             if the Codes have not yet been loaded by the application.
	 * @throws IllegalArgumentException
	 *             if a CodeSet cannot be found for the specified code category.
	 */

	public CodeSet getCodeSet(String codeCategory) {
		if (codeSets == null) {
			throw new IllegalStateException("Codes have not been loaded yet.");
		}
		CodeSet codeSet = codeSets.get(codeCategory);

		if (codeSet == null) {
			throw new IllegalArgumentException("Code Set '" + codeCategory
					+ "' not found.");
		}
		return codeSet;
	}

	/**
	 * This method is used to get List of code set searched by their category.
	 * 
	 * @return Map of code sets keyed by code category.
	 * @throws SAFRNotFoundException
	 *             If no Codes are available in the code table.
	 * @throws SAFRException.
	 */
	public Map<String, CodeSet> getAllCodeSets() throws SAFRException {
		if (codeSets == null) {
			List<CodeTransfer> allCodes;
			Map<String, CodeSet> codeSetMap = new HashMap<String, CodeSet>();
			try {
				allCodes = DAOFactoryHolder.getDAOFactory().getCodeSetDAO()
						.getAllCodeSets();

				if (allCodes == null || allCodes.isEmpty()) {
					throw new SAFRNotFoundException(
							"No codes are in code table.");
				}

				List<CodeTransfer> tmpList = new ArrayList<CodeTransfer>();
				// Store the first category to temporary string
				String tmpCategory = ((CodeTransfer) allCodes.get(0))
						.getCodeCategory();

				for (CodeTransfer ct : allCodes) {
					if (!ct.getCodeCategory().equals(tmpCategory)) {
						// category changed, add to cache.
						CodeSet tmpCodeSet = new CodeSet(tmpCategory, tmpList);
						codeSetMap.put(tmpCategory, tmpCodeSet);
						tmpCategory = ct.getCodeCategory();
						tmpList.clear();
					}
					tmpList.add(ct);
				}
				// if the tmpList is not empty (would be for last category) then
				// add
				// to the return list
				if (!tmpList.isEmpty()) {
					codeSetMap.put(tmpCategory, new CodeSet(tmpCategory,
							tmpList));
				}
			} catch (DAOException de) {
				throw de;
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.codeSets = codeSetMap;
		}
		return this.codeSets;
	}
	
    public void setAllCodeSets(Map<String, CodeSet> allCodeSets) {
        codeSets = allCodeSets;
    }
	
	/**
	 * Creates new Physical File in the current Environment.
	 * 
	 * @return Physical file object.
	 */
	public PhysicalFile createPhysicalFile() {
		return new PhysicalFile(getCurrentEnvironmentId());
	}

	/**
	 * Initializes and returns a new Physical File object created using a
	 * transfer object.
	 * 
	 * @return Physical File object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public PhysicalFile initPhysicalFile(PhysicalFileTransfer trans)
			throws DAOException, SAFRException {
		return new PhysicalFile(trans);
	}

	/**
	 * This method gets the specified Physical file from the user's current
	 * environment.
	 * 
	 * @param id
	 *            : the Id of the Physical file to be searched.
	 * @return the physical file having the specified id.
	 * @throws SAFRNotFoundException
	 *             If PhysicalFile with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public PhysicalFile getPhysicalFile(Integer id) throws SAFRException {
		return getPhysicalFile(id, getCurrentEnvironmentId());
	}
	
	/**
	 * This method gets the specified Physical file from the specified
	 * environment.
	 * 
	 * @param id
	 *            : the Id of the Physical file to be searched.
	 * @param environId
	 *            : the Id of the Environment to be searched.
	 * @return the physical file having the specified id and environment id.
	 * @throws SAFRNotFoundException
	 *             If PhysicalFile with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public PhysicalFile getPhysicalFile(Integer id, Integer environId)
			throws SAFRException {
		PhysicalFile physicalFile = null;
		PhysicalFileTransfer physicalFileTransfer;
		physicalFileTransfer = DAOFactoryHolder.getDAOFactory()
				.getPhysicalFileDAO()
				.getPhysicalFile(id, environId);
		if (physicalFileTransfer == null) {
			throw new SAFRNotFoundException("Physical File '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			physicalFile = new PhysicalFile(physicalFileTransfer);
		}
		return physicalFile;
	}

	public PhysicalFile getPhysicalFile(String name) throws SAFRException {
		return getPhysicalFile(name, getCurrentEnvironmentId());
	}
	
	public PhysicalFile getPhysicalFile(String name, Integer environId) {
		PhysicalFile physicalFile = null;
		PhysicalFileTransfer physicalFileTransfer;
		physicalFileTransfer = DAOFactoryHolder.getDAOFactory()
				.getPhysicalFileDAO()
				.getPhysicalFile(name, environId);
		if (physicalFileTransfer != null) {
			physicalFile = new PhysicalFile(physicalFileTransfer);
		}
		return physicalFile;
	}

	/**
	 * Creates new User Exit Routine in the current environment.
	 * 
	 * @return User Exit Routine object.
	 */
	public UserExitRoutine createUserExitRoutine() {
		UserExitRoutine userExitRoutine = new UserExitRoutine(
				getCurrentEnvironmentId());
		return userExitRoutine;
	}

	/**
	 * Initializes and returns a new User Exit Routine object created using a
	 * transfer object.
	 * 
	 * @return User Exit Routine object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public UserExitRoutine initUserExitRoutine(UserExitRoutineTransfer trans)
			throws DAOException, SAFRException {
		return new UserExitRoutine(trans);
	}

	/**
	 * This method gets the User Exit Routine with the specified id from the
	 * user's current environment.
	 * 
	 * @param id
	 *            of the User Exit Routine to get.
	 * @return User Exit Routine having specified id.
	 * @throws SAFRNotFoundException
	 *             If UserExitRoutine with the specified Id is not present in
	 *             the database.
	 * @throws SAFRException
	 */
	public UserExitRoutine getUserExitRoutine(Integer id) throws SAFRException {
		return getUserExitRoutine(id, getCurrentEnvironmentId());
	}
	
	/**
	 * This method gets the User Exit Routine with specified id from the
	 * specified Environment.
	 * 
	 * @param id
	 *            of the User Exit Routine to get.
	 * @param environId
	 *            : the Id of the Environment to be searched.
	 * @return User Exit Routine having specified id and environment id.
	 * @throws SAFRNotFoundException
	 *             If UserExitRoutine with the specified Id is not present in
	 *             the database.
	 * @throws SAFRException
	 */
	public UserExitRoutine getUserExitRoutine(Integer id, Integer environId)
			throws SAFRException {
		UserExitRoutine userExitRoutine = null;
		UserExitRoutineTransfer userExitRoutineTransfer;
		userExitRoutineTransfer = DAOFactoryHolder.getDAOFactory()
				.getUserExitRoutineDAO().getUserExitRoutine(id, environId);
		if (userExitRoutineTransfer == null) {
			throw new SAFRNotFoundException("User Exit Routine '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			userExitRoutine = new UserExitRoutine(userExitRoutineTransfer);
		}
		return userExitRoutine;
	}

	/**
	 * This method is used to get all SAFR User Exit Routines from SAFR
	 * database.
	 * 
	 * @return List of all SAFR User Exit Routines from SAFR database.
	 * 
	 * @throws DAOException
	 *             when there is an error in retrieving data from database.
	 * @throws SAFRException.
	 */
	public List<UserExitRoutine> getAllUserExitRoutines() throws DAOException,
			SAFRException {

		List<UserExitRoutineTransfer> uerts = DAOFactoryHolder.getDAOFactory()
				.getUserExitRoutineDAO().getAllUserExitRoutines(
						getCurrentEnvironmentId());

		List<UserExitRoutine> uers = new ArrayList<UserExitRoutine>();
		for (UserExitRoutineTransfer uert : uerts) {
			if (userExitRoutines.containsKey(uert.getId())) {
				uers.add(userExitRoutines.get(uert.getId()));
			} else {
				UserExitRoutine userExitRoutine = new UserExitRoutine(uert);
				uers.add(userExitRoutine);
			}
		}
		return uers;
	}

	/**
	 * Creates new Logical File in current Environment.
	 * 
	 * @return Logical File object.
	 */
	public LogicalFile createLogicalfile() {
		return new LogicalFile(getCurrentEnvironmentId());
	}

	/**
	 * Initializes and returns a new Logical File object created using a
	 * transfer object.
	 * 
	 * @return Logical File object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LogicalFile initLogicalFile(LogicalFileTransfer trans)
			throws DAOException, SAFRException {
		return new LogicalFile(trans);
	}

    /**
     * Initializes and returns a new ViewFolder object created using a
     * transfer object.
     * 
     * @return ViewFolder object.
     * @throws SAFRException
     * @throws DAOException
     */
    public ViewFolder initViewFolder(ViewFolderTransfer trans)
            throws DAOException, SAFRException {
        return new ViewFolder(trans);
    }

	
	/**
	 * This methods gets the specified Logical File from the user's
	 * current Environment.
	 * 
	 * @param id
	 *            of the Logical File to get.
	 * @return Logical File having specified id.
	 * @throws SAFRNotFoundException
	 *             If LogicalFile with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LogicalFile getLogicalFile(Integer id) throws SAFRException {
		return getLogicalFile(id, getCurrentEnvironmentId());
	}

	// public LogicalFile getLogicalFile(String name) throws SAFRException {
	// 	return getLogicalFile(name, getCurrentEnvironmentId());
	// }

	/**
	 * This methods gets the specified Logical File from the specified
	 * Environment.
	 * 
	 * @param id
	 *            of the Logical File to get.
	 * @param environId
	 *            the Environment containing the Logical File.
	 * @return Logical File having specified id.
	 * @throws SAFRNotFoundException
	 *             If LogicalFile with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LogicalFile getLogicalFile(Integer id, Integer environId)
			throws SAFRException {
		LogicalFile logicalFile = null;
		LogicalFileTransfer logicalFileTransfer = null;
		logicalFileTransfer = DAOFactoryHolder.getDAOFactory()
				.getLogicalFileDAO().getLogicalFile(id, environId);

		if (logicalFileTransfer == null) {
			throw new SAFRNotFoundException("Logical File '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			logicalFile = new LogicalFile(logicalFileTransfer);
		}

		return logicalFile;
	}

	// public LogicalFile getLogicalFile(String name, Integer environId) {
	// 	LogicalFile logicalFile = null;
	// 	LogicalFileTransfer logicalFileTransfer = null;
	// 	logicalFileTransfer = DAOFactoryHolder.getDAOFactory()
	// 			.getLogicalFileDAO().getLogicalFile(name, environId);

	// 	if (logicalFileTransfer != null) {
	// 		logicalFile = new LogicalFile(logicalFileTransfer);
	// 	}

	// 	return logicalFile;
	// }

	/**
	 * Creates new Logical Record in the current Environment.
	 * 
	 * @return Logical Record object.
	 */
	public LogicalRecord createLogicalRecord() {
		return new LogicalRecord(getCurrentEnvironmentId());
	}

	/**
	 * Initializes and returns a new Logical Record object created using a
	 * transfer object.
	 * 
	 * @return Logical Record object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LogicalRecord initLogicalRecord(LogicalRecordTransfer trans)
			throws DAOException, SAFRException {
		return new LogicalRecord(trans);
	}

	/**
	 * Initializes and returns a new Logical Record Field object created using a
	 * transfer object.
	 * 
	 * @return Logical Record Field object.
	 * @throws SAFRException
	 */
	public LRField initLRField(LRFieldTransfer trans) throws SAFRException {
		return new LRField(trans);
	}
	
	/**
	 * Gets the specified Logical Record from the user's current environment.
	 * 
	 * @param id
	 *            of the Logical Record to get.
	 * @return Logical Record having specified id.
	 * @throws SAFRNotFoundException
	 *             If LogicalRecord with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LogicalRecord getLogicalRecord(Integer id) throws SAFRException {
		return getLogicalRecord(id, getCurrentEnvironmentId());
	}

	// public LogicalRecord getLogicalRecord(String name) throws SAFRException {
	// 	return getLogicalRecord(name, getCurrentEnvironmentId());
	// }

	/**
	 * Gets the specified Logical Record from the specified Environment.
	 * 
	 * @param id
	 *            of the Logical Record to get.
	 * @param environId
	 *            Environment containing the Logical Record.
	 * @return Logical Record having specified id.
	 * @throws SAFRNotFoundException
	 *             If LogicalRecord with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LogicalRecord getLogicalRecord(Integer id, Integer environId) throws SAFRException {
		LogicalRecord logicalRecord = null;
		LogicalRecordTransfer logicalRecordTransfer = null;
		logicalRecordTransfer = DAOFactoryHolder.getDAOFactory()
				.getLogicalRecordDAO().getLogicalRecord(id, environId);
		if (logicalRecordTransfer == null) {
			throw new SAFRNotFoundException("Logical Record '" + id
					+ "' not found.");
		} else {
			logicalRecord = new LogicalRecord(logicalRecordTransfer);
		}

		return logicalRecord;
	}

	// public LogicalRecord getLogicalRecord(String name, Integer environId) throws SAFRException {
	// 	LogicalRecord logicalRecord = null;
	// 	LogicalRecordTransfer logicalRecordTransfer = null;
	// 	logicalRecordTransfer = DAOFactoryHolder.getDAOFactory()
	// 			.getLogicalRecordDAO().getLogicalRecord(name, environId);
	// 	if (logicalRecordTransfer != null) {
	// 		logicalRecord = new LogicalRecord(logicalRecordTransfer);
	// 	}

	// 	return logicalRecord;
	// }

	/**
	 * This method is used to get the LR fields of the specified Parent Logical
	 * Record.
	 * 
	 * @param parentLR
	 *            : the Logical Record whose LR Fields are to be retrieved.
	 * @return a list of LR fields whose parent LR is specified.
	 * @throws SAFRException
	 */
	public List<LRField> getLRFields(LogicalRecord parentLR)
			throws SAFRException {
		List<LRField> lrfs = new ArrayList<LRField>();
		List<LRFieldTransfer> lrfts = DAOFactoryHolder.getDAOFactory()
				.getLRFieldDAO().getLRFields(parentLR.getEnvironmentId(),
						parentLR.getId());
		for (LRFieldTransfer lrft : lrfts) {
			LRField lrf = new LRField(parentLR, lrft);
			lrfs.add(lrf);
		}
		return lrfs;
	}

	/**
	 * This method is used to get the LR fields of the specified Logical Record.
	 * 
	 * @param logicalRecordId
	 *            : the Logical Record whose LR Fields are to be retrieved.
	 * @return a list of LR fields whose parent LR is specified.
	 * @throws SAFRException
	 */
	public List<LRField> getLRFields(Integer logicalRecordId)
			throws SAFRException {
		List<LRField> lrfs = new ArrayList<LRField>();
		List<LRFieldTransfer> lrfts = DAOFactoryHolder.getDAOFactory()
				.getLRFieldDAO().getLRFields(getCurrentEnvironmentId(),
						logicalRecordId);
		for (LRFieldTransfer lrft : lrfts) {
			LRField lrf = new LRField(lrft);
			lrfs.add(lrf);
		}
		return lrfs;
	}

	/**
	 * This method is used to get the LR fields corresponding to the list of ids
	 * passed as parameter
	 * 
	 * @param envId
	 *            : the id of the environment.
	 * @param ids
	 *            : the list of Ids whose LR Fields are to be retrieved
	 * @return a list of LR fields
	 * @throws SAFRException
	 */
	public List<LRField> getLRFields(Integer envId, List<Integer> ids)
			throws SAFRException {
		List<LRField> lrfs = new ArrayList<LRField>();
		List<LRFieldTransfer> lrfts = DAOFactoryHolder.getDAOFactory()
				.getLRFieldDAO().getLRFields(envId, ids);
		for (LRFieldTransfer lrft : lrfts) {
			LRField lrf = new LRField(lrft);
			lrfs.add(lrf);
		}
		return lrfs;
	}

	/**
	 * This method is used to get the LR field with the specified ID.
	 * 
	 * @param id
	 *            : the LRField id
	 * @return an LRField
	 * @throws SAFRNotFoundException
	 *             If LRField with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LRField getLRField(Integer id, Boolean retrieveKeyInfo)
			throws SAFRException {
		LRField lrField = null;
		LRFieldTransfer lrFieldTransfer = null;
		lrFieldTransfer = DAOFactoryHolder.getDAOFactory().getLRFieldDAO()
				.getLRField(getCurrentEnvironmentId(), id, retrieveKeyInfo);

		if (lrFieldTransfer == null) {
			throw new SAFRNotFoundException("LR Field '" + id
					+ "' not found in Environment '" + getCurrentEnvironmentId() + "'.", id);
		} else {
			lrField = new LRField(lrFieldTransfer);
		}
		return lrField;
	}

	/**
	 * This method is used to get the LR field with the specified ID.
	 * 
	 * @param id : the LRField id
	 * @param envId : environment id            
	 * @return an LRField
	 * @throws SAFRNotFoundException
	 *             If LRField with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LRField getLRField(Integer id, Integer envId, Boolean retrieveKeyInfo)
			throws SAFRException {
		LRField lrField = null;
		LRFieldTransfer lrFieldTransfer = null;
		lrFieldTransfer = DAOFactoryHolder.getDAOFactory().getLRFieldDAO()
				.getLRField(envId, id, retrieveKeyInfo);

		if (lrFieldTransfer == null) {
			throw new SAFRNotFoundException("LR Field '" + id
					+ "' not found in Environment '" + envId + "'.", id);
		} else {
			lrField = new LRField(lrFieldTransfer);
		}
		return lrField;
	}
	
	/**
	 * This method is used to retrieve the Logical Record from the specified
	 * LRLFAssociation.
	 * 
	 * @param LRLFassociationId
	 *            : the id of LRLF Association from which the Logical Record is
	 *            to be retrieved.
	 * @param environmentId
	 *            : the id of the environment.
	 * @return the Logical Record object.
	 * @throws SAFRException
	 */
	public LogicalRecord getLogicalRecordFromLRLFAssociation(
			Integer LRLFassociationId, Integer environmentId)
			throws SAFRException {
		LogicalRecord logicalRecord = null;
		LogicalRecordTransfer logicalRecordTransfer = null;
		logicalRecordTransfer = DAOFactoryHolder.getDAOFactory()
				.getLogicalRecordDAO().getLogicalRecordFromLRLFAssociation(
						LRLFassociationId, environmentId);

		if (logicalRecordTransfer == null) {
			throw new SAFRNotFoundException("LR-LF Association with ID  '"
					+ LRLFassociationId + "' not found.");
		} else {
			logicalRecord = new LogicalRecord(logicalRecordTransfer);
		}

		return logicalRecord;
	}

	/**
	 * Creates new Lookup Path in the current Environment.
	 * 
	 * @return a Lookup Path object.
	 * @throws SAFRException
	 * @throws DAOException
	 *             when there is an error in retrieving data from database.
	 */
	public LookupPath createLookupPath() throws SAFRException, DAOException {
		return new LookupPath(getCurrentEnvironmentId());
	}

	/**
	 * Initializes and returns a new Lookup path object created using a transfer
	 * object.
	 * 
	 * @return Lookup path object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LookupPath initLookupPath(LookupPathTransfer trans)
			throws DAOException, SAFRException {
		return new LookupPath(trans);
	}

	/**
	 * Initializes and returns a new Lookup path step object created using a
	 * transfer object and the parent lookup path.
	 * 
	 * @return Lookup Path Step object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LookupPathStep initLookupPathStep(LookupPathStepTransfer trans,
			LookupPath parentLookupPath, LogicalRecord sourceLR,
			LogicalRecord targetLR, ComponentAssociation targetLRLFAssoc)
			throws DAOException, SAFRException {
		return new LookupPathStep(trans, parentLookupPath, sourceLR, targetLR,
				targetLRLFAssoc);
	}

	/**
	 * Initializes and returns a new Lookup path source field object created
	 * using a transfer object and the parent lookup path step.
	 * 
	 * @return Lookup Path Source Field object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public LookupPathSourceField initLookupPathSourceField(
			LookupPathSourceFieldTransfer trans, LookupPathStep parentStep)
			throws DAOException, SAFRException {
		return new LookupPathSourceField(trans, parentStep);
	}

	/**
	 * This method is used to get the Lookup path of the specified id in the
	 * current environment.
	 * 
	 * @param id
	 *            Lookup path id.
	 * @return A Lookup path.
	 * @throws SAFRException
	 */
	public LookupPath getLookupPath(Integer id) throws SAFRException {
		return getLookupPath(id, getCurrentEnvironmentId());
	}

	/**
	 * This method is used to get the Lookup path of the specified id and
	 * specified environment id.
	 * 
	 * @param id
	 *            Lookup path Id.
	 * @param environId
	 *            Environment Id from which the lookup path is retrieved .
	 * @return A LookupPath.
	 * @throws SAFRNotFoundException
	 *             If LookupPath with the specified Id is not present in the
	 *             database.
	 * @throws SAFRException
	 */
	public LookupPath getLookupPath(Integer id, Integer environId)
			throws SAFRException {
		LookupPath lookupPath = null;
		LookupPathTransfer lookupPathTransfer = null;
		lookupPathTransfer = DAOFactoryHolder.getDAOFactory().getLookupDAO()
				.getLookupPath(id, environId);

		if (lookupPathTransfer == null) {
			throw new SAFRNotFoundException("Lookup Path '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			lookupPath = new LookupPath(lookupPathTransfer);
		}

		return lookupPath;
	}

	/**
	 * This method is used to get the list of lookup paths steps present in the
	 * specified Lookup Path.
	 * 
	 * @param parentLookup
	 *            : the Lookup Path whose steps are to be retrieved.
	 * @return a list of lookup path steps present in the specified Lookup Path.
	 * @throws SAFRException
	 */
	public List<LookupPathStep> getLookupPathSteps(LookupPath parentLookup)
			throws SAFRException {
		List<LookupPathStep> lkupPathSteps = new ArrayList<LookupPathStep>();
		List<LookupPathStepTransfer> lkupStepsTrans = DAOFactoryHolder
				.getDAOFactory().getLookupPathStepDAO().getAllLookUpPathSteps(
						parentLookup.getEnvironmentId(), parentLookup.getId());
		for (LookupPathStepTransfer lkupStepsTran : lkupStepsTrans) {
			LookupPathStep lkupPathStep = new LookupPathStep(lkupStepsTran,
					parentLookup);
			if (lkupPathSteps.size() > 0) {
				// Source LF of steps>1 should be Target LF of previous
				// step. This is not stored in DB so should be loaded here.
				lkupPathStep.setSourceLRLFAssociation(lkupPathSteps.get(
						lkupPathSteps.size() - 1).getTargetLRLFAssociation());
			}
			lkupPathSteps.add(lkupPathStep);
		}
		return lkupPathSteps;
	}

	/**
	 * This method is used to get the source fields present in the specified
	 * Lookup Path step.
	 * 
	 * @param parentLookkupPathStep
	 *            : the lookup path step whose source fields are to be
	 *            retrieved.
	 * @return a list of source fields present in the specified Lookup Path
	 *         step.
	 * @throws DAOException
	 *             when there is an error in retrieving data from database.
	 * @throws SAFRException
	 */
	public List<LookupPathSourceField> getLookUpPathStepSourceFields(
			LookupPathStep parentLookkupPathStep) throws DAOException,
			SAFRException {
		List<LookupPathSourceField> lkupPathStepSourceFields = new ArrayList<LookupPathSourceField>();
		List<LookupPathSourceFieldTransfer> lkupStepsSourceFieldTrans = DAOFactoryHolder
				.getDAOFactory().getLookupPathStepDAO()
				.getLookUpPathStepSourceFields(
						parentLookkupPathStep.getEnvironmentId(),
						parentLookkupPathStep.getId());
		for (LookupPathSourceFieldTransfer lkupStepsSFTran : lkupStepsSourceFieldTrans) {
			LookupPathSourceField lkupPathStepSourceField = new LookupPathSourceField(
					lkupStepsSFTran, parentLookkupPathStep);
			lkupPathStepSourceFields.add(lkupPathStepSourceField);
		}
		return lkupPathStepSourceFields;
	}

	/**
	 * Creates new View in the current Environment.
	 * 
	 * @return a view object.
	 * @throws SAFRException
	 */
	public View createView() {
		return new View(getCurrentEnvironmentId());
	}

	/**
	 * Initializes and returns a new View object created using a transfer
	 * object.
	 * 
	 * @return View object.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public View initView(ViewTransfer trans) throws DAOException, SAFRException {
		return new View(trans);
	}

	/**
	 * Get the existing View with the specified ID in the current environment.
	 * 
	 * @param id
	 *            the View id
	 * @return the View object
	 * @throws SAFRException
	 *             In case there are inactive dependencies and the view cannot
	 *             be loaded, a subtype {@link SAFRDependencyException} will be
	 *             thrown with a list of inactive components.
	 */
	public View getView(Integer id) throws SAFRException {

		return getView(id, getCurrentEnvironmentId());
	}

	/**
	 * Get the existing View with the specified ID and environment ID.
	 * 
	 * @param id
	 *            the View id
	 * @param environId
	 *            id of the Environment containing the View
	 * @return the View object
	 * @throws SAFRNotFoundException
	 *             If View with the specified Id is not present in the database.
	 * @throws SAFRException
	 *             In case there are inactive dependencies and the view cannot
	 *             be loaded, a subtype {@link SAFRDependencyException} will be
	 *             thrown with a list of inactive components.
	 */
	public View getView(Integer id, Integer environId) throws SAFRException	{
		View view = null;
		ViewTransfer viewTransfer = null;
		viewTransfer = DAOFactoryHolder.getDAOFactory().getViewDAO().getView(
				id, environId);

		if (viewTransfer == null) {
			throw new SAFRNotFoundException("View '" + id
					+ "' not found in Environment '" + environId + "'.", id);
		} else {
			view = new View(viewTransfer);
		}

		return view;
	}
	
    public static SAFREnvironmentalComponent getComponent(ComponentType type, int id, int env) throws SAFRException {
        switch (type) {
        case ControlRecord:
            return SAFRApplication.getSAFRFactory().getControlRecord(id, env);        
        case UserExitRoutine:
            return SAFRApplication.getSAFRFactory().getUserExitRoutine(id, env);
        case PhysicalFile:
            return SAFRApplication.getSAFRFactory().getPhysicalFile(id, env);
        case LogicalFile:
            return SAFRApplication.getSAFRFactory().getLogicalFile(id, env);
        case LogicalRecord:
            return SAFRApplication.getSAFRFactory().getLogicalRecord(id, env);
        case LookupPath:
            return SAFRApplication.getSAFRFactory().getLookupPath(id, env);
        case View:
            return SAFRApplication.getSAFRFactory().getView(id, env);
        case ViewFolder:
            return SAFRApplication.getSAFRFactory().getViewFolder(id, env);
        default:
            return null;
        }
    }
	
	/**
	 * Closes the active database connection.
	 * 
	 * @throws SAFRException
	 *             if an error occurs.
	 */
	public void closeDatabaseConnection() throws SAFRException {
		try {
			DAOFactoryHolder.closeDAOFactory();
		} catch (Exception e) {
			throw new SAFRException("Unable to close database connection.", e);
		}
	}

	/**
	 * Keeps a backup copy of the active database connection.
	 * 
	 * @throws SAFRException
	 *             if an error occurs.
	 */
	public void backupDatabaseConnection() {
		DAOFactoryHolder.backupDAOFactory();
	}

	/**
	 * Keeps a backup copy of the active database connection.
	 * 
	 * @throws SAFRException
	 *             if an error occurs.
	 */
	public void restoreDatabaseConnection() throws SAFRException {
		DAOFactoryHolder.restoreDAOFactory();
	}
	
	/**
	 * Closes the active database connection.
	 * 
	 * @throws SAFRException
	 *             if an error occurs.
	 */
	public void closeBackupDatabaseConnection() throws SAFRException {
		try {
			DAOFactoryHolder.closeBackupDAOFactory();
		} catch (Exception e) {
			throw new SAFRException("Unable to close backup database connection.", e);
		}
	}
	
	/**
	 * Returns URL used for current database connection.
	 * 
	 * @return Returns URL used for current database connection.
	 * @throws DAOException
	 */
	public static String getCurrentDatabaseUrl() throws DAOException {

		return DAOFactoryHolder.getDAOFactory().getConnectionParameters()
				.getUrl();
	}

	/**
	 * Returns the current environment in which the user is logged into.
	 * 
	 * @return Current environment in which the user is logged into.
	 */
	private Integer getCurrentEnvironmentId() {
		return SAFRApplication.getUserSession().getEnvironment().getId();
	}

	/**
	 * This method is used to delete a View from a view folder. This delete
	 * action can only be performed if the user is a system admin or environment
	 * admin or if the general user has at least modify rights on the view
	 * folder containing that view. 
	 * 
	 * @param viewId
	 *            : The Id of the View which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeView(Integer viewId) throws SAFRException {
		removeView(viewId, getCurrentEnvironmentId(),false);
	}

	public void removeView(Integer viewId, Integer environId, boolean migration) throws SAFRException {

        if (SAFRApplication.getUserSession().getEditRights(ComponentType.View, viewId) == 
            EditRights.ReadModifyDelete || migration) {	    
			DAOFactoryHolder.getDAOFactory().getViewDAO().deleteView(viewId, environId);
		} else {
			throw new SAFRException("The user is not authorized to perform this deletion.");
		}
	}

	/**
	 * This method is used to delete a User Exit Routine. Deletion is allowed
	 * only if the User has delete rights on the User Exit Routine.
	 * 
	 * @param userExitRoutineId
	 *            : The Id of the User Exit Routine which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeUserExitRoutine(Integer userExitRoutineId)
			throws SAFRException {
	    
        if (SAFRApplication.getUserSession().getEditRights(ComponentType.UserExitRoutine, userExitRoutineId) 
                == EditRights.ReadModifyDelete) {
	    
			Map<ComponentType, List<DependentComponentTransfer>> userExitDependencies = DAOFactoryHolder
					.getDAOFactory().getUserExitRoutineDAO()
					.getUserExitRoutineDependencies(getCurrentEnvironmentId(),
							userExitRoutineId);
			if (!(userExitDependencies == null)
					&& !(userExitDependencies.isEmpty())) {
				// If user Exit Routine has dependencies then restrict deletion
				// and show dependencies
				throw new SAFRDependencyException(userExitDependencies);

			} else {
				DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO()
						.removeUserExitRoutine(userExitRoutineId,
								getCurrentEnvironmentId());
			}

		} else {
			throw new SAFRException("The user is not authorized to perform this deletion.");
		}
	}

	/**
	 * This method is used to delete a Control Record. Deletion is allowed only
	 * if the User is a System Admin or an Environment Admin.
	 * 
	 * @param controlRecordId
	 *            : The Id of the Control Record which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeControlRecord(Integer controlRecordId)
			throws SAFRException {
		if (SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin()) {

			List<DependentComponentTransfer> controlRecordDependencies = DAOFactoryHolder
					.getDAOFactory().getControlRecordDAO()
					.getControlRecordViewDependencies(
							getCurrentEnvironmentId(), controlRecordId);
			if (!(controlRecordDependencies == null)
					&& !(controlRecordDependencies.isEmpty())) {
				// If Control Record has dependencies then restrict deletion
				// and show dependencies
				Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
				dependencies.put(ComponentType.View, controlRecordDependencies);

				throw new SAFRDependencyException(dependencies);

			} else {
				DAOFactoryHolder.getDAOFactory().getControlRecordDAO()
						.removeControlRecord(controlRecordId,
								getCurrentEnvironmentId());
			}

		} else {
			throw new SAFRException(
					"The user is not authorized to perform this deletion.");

		}
	}

	/**
	 * This method is used to delete a Physical File. This delete action can
	 * only be performed if the user has delete rights on the specified Physical
	 * File.
	 * 
	 * @param physicalFileId
	 *            : The Id of the Physical File which is to be deleted.
	 * @throws SAFRException
	 */
	public void removePhysicalFile(Integer physicalFileId) throws SAFRException {
	    
        if (SAFRApplication.getUserSession().getEditRights(ComponentType.PhysicalFile, physicalFileId) 
                == EditRights.ReadModifyDelete) {
            
			List<DependentComponentTransfer> LFDependencies = DAOFactoryHolder.getDAOFactory().
			    getPhysicalFileDAO().getAssociatedLogicalFilesWithOneAssociatedPF(
			        getCurrentEnvironmentId(), physicalFileId);
			if (LFDependencies.size() > 0) {
				Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
				dependencies.put(ComponentType.LogicalFile, LFDependencies);
				throw new SAFRDependencyException(dependencies);
			}

			List<DependentComponentTransfer> viewDependencies = DAOFactoryHolder
					.getDAOFactory().getPhysicalFileDAO().getViewDependencies(
							getCurrentEnvironmentId(), physicalFileId);

			if (viewDependencies.size() > 0) {
				Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
				dependencies.put(ComponentType.View, viewDependencies);
				throw new SAFRDependencyException(dependencies);
			}

			DAOFactoryHolder.getDAOFactory().getPhysicalFileDAO()
					.removePhysicalFile(physicalFileId,
							getCurrentEnvironmentId());

		} else {
			throw new SAFRException("The user is not authorized to perform this deletion.");

		}
	}

	/**
	 * This method is to delete a Lookup Path.
	 * 
	 * @param lookupPathId
	 *            : The Id of the Lookup Path which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeLookupPath(Integer lookupPathId) throws SAFRException {

        if (SAFRApplication.getUserSession().getEditRights(ComponentType.LookupPath, lookupPathId) 
                == EditRights.ReadModifyDelete) {
		
    		List<DependentComponentTransfer> viewDependencies = DAOFactoryHolder
    				.getDAOFactory().getLookupDAO().getLookupPathViewDependencies(
    						getCurrentEnvironmentId(), lookupPathId, null);
    		if (viewDependencies.size() > 0) {
    			Map<ComponentType, List<DependentComponentTransfer>> dependencies = 
    			    new HashMap<ComponentType, List<DependentComponentTransfer>>();
    			dependencies.put(ComponentType.View, viewDependencies);
    			throw new SAFRDependencyException(dependencies);
    		} else {
    			DAOFactoryHolder.getDAOFactory().getLookupDAO().removeLookupPath(
    					lookupPathId, getCurrentEnvironmentId());
    		}
		
	    } else {
            String msgString = "The user is not authorized to remove this lookup path.";
            throw new SAFRException(msgString);
	    }
	}

	/**
	 * This method is used to delete a View Folder. This delete action can only
	 * be performed if the user has delete rights on the specified View Folder
	 * and the View Folder does not contain any view in it.
	 * 
	 * @param viewFolderId
	 *            : The Id of the View Folder which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeViewFolder(Integer viewFolderId,
			SAFRValidationToken safrValidationToken) throws SAFRException {
	    
        if (SAFRApplication.getUserSession().getEditRights(ComponentType.ViewFolder, viewFolderId) 
                == EditRights.ReadModifyDelete) {
            
			// if UI changes the validation token and returns something
			// else, the
			// hashCode won't match and it throws error.
			if (safrValidationToken != null
					&& safrValidationToken.getTokenId() != this.hashCode()) {
				throw new IllegalArgumentException(
						"The validation token does not identify this "
								+ this.getClass().getName());
			}
			if (safrValidationToken == null) {
				// if token is null then generate dependencies
				List<DependentComponentTransfer> viewFolderUserDependencies = DAOFactoryHolder
						.getDAOFactory().getViewFolderDAO()
						.getUserDependencies(viewFolderId);
				if (!(viewFolderUserDependencies == null)
						&& !(viewFolderUserDependencies.isEmpty())) {
					// If View Folder has User dependencies then show
					// dependencies
					// and
					// ask for user response to proceed with deletion.
					Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
					dependencies.put(ComponentType.User,
							viewFolderUserDependencies);

					SAFRDependencyException sde = new SAFRDependencyException(
							dependencies);

					// modify the token
					SAFRValidationException validationException = new SAFRValidationException(
							sde);
					SAFRValidationToken token = new SAFRValidationToken(
							this, SAFRValidationType.WARNING);
					validationException.setSafrValidationToken(token);

					throw validationException;

				} else {
					// Directly remove if no dependency on user is there.
					DAOFactoryHolder.getDAOFactory().getViewFolderDAO()
							.removeViewFolder(viewFolderId,
									getCurrentEnvironmentId());
				}

			} else {
				// if token is not null that means user has confirmed the
				// deletion inspite of the dependency.
		        if (SAFRApplication.getUserSession().getEditRights(ComponentType.ViewFolder, viewFolderId) 
		                == EditRights.ReadModifyDelete) {

					// Remove dependency on User.
					DAOFactoryHolder.getDAOFactory().getUserDAO()
							.removeViewFolderReferences(viewFolderId);
				} else {
					throw new SAFRException("The user is not authorized to perform this deletion.");
				}
			}

		} else {
			throw new SAFRException("The user is not authorized to perform this deletion.");
		}
	}

	/**
	 * This method is used to delete a Logical Record. This delete action can
	 * only be performed if the user has delete rights on the specified Logical
	 * Record and the Logical Record does not have any View or Lookup Path
	 * dependencies.
	 * 
	 * @param logicalRecordId
	 *            : The Id of the Logical Record which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeLogicalRecord(Integer logicalRecordId)
			throws SAFRException {
        if (SAFRApplication.getUserSession().getEditRights(ComponentType.LogicalRecord, logicalRecordId) 
                == EditRights.ReadModifyDelete) {
            
			Map<ComponentType, List<DependentComponentTransfer>> dependencies = 
			    new HashMap<ComponentType, List<DependentComponentTransfer>>();
			List<DependentComponentTransfer> lookupDependencies = DAOFactoryHolder
					.getDAOFactory().getLogicalRecordDAO()
					.getLRLookupDependencies(getCurrentEnvironmentId(),
							logicalRecordId, new ArrayList<Integer>());
			if (lookupDependencies != null && lookupDependencies.size() > 0) {
				dependencies.put(ComponentType.LookupPath, lookupDependencies);
			}
			List<Integer> lookupPathIds = new ArrayList<Integer>();
			for (DependentComponentTransfer depComp : lookupDependencies) {
				lookupPathIds.add(depComp.getId());
			}
			List<DependentComponentTransfer> viewDependencies = DAOFactoryHolder
					.getDAOFactory().getLogicalRecordDAO()
					.getLRViewDependencies(getCurrentEnvironmentId(),
							logicalRecordId, lookupPathIds,
							new ArrayList<Integer>());
			if (viewDependencies != null && viewDependencies.size() > 0) {
				dependencies.put(ComponentType.View, viewDependencies);
			}

			if (dependencies.size() > 0) {
				throw new SAFRDependencyException(dependencies);
			} else {

				DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
						.removeLogicalRecord(logicalRecordId,
								getCurrentEnvironmentId());
			}
		} else {
			throw new SAFRException(
					"The user is not authorized to perform this deletion.");
		}
	}

	/**
	 * This method is used to delete a Group. The user will only allowed to
	 * delete a Group if user is a System Administrator. Deletion will be
	 * restricted if Group is associated to any User or Environment.
	 * 
	 * @param groupId
	 *            : The Id of the Group which is to be deleted.
	 * @throws SAFRException
	 */
	public void removeGroup(Integer groupId) throws SAFRException {
		if ((SAFRApplication.getUserSession().getUser().isSystemAdmin())) {
			Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
			List<DependentComponentTransfer> environmentDependencies = new ArrayList<DependentComponentTransfer>();
			List<DependentComponentTransfer> userDependencies = new ArrayList<DependentComponentTransfer>();
			// Getting associating Environments..
			List<GroupEnvironmentAssociationTransfer> groupEnvAssociations = DAOFactoryHolder
					.getDAOFactory().getGroupDAO().getAssociatedEnvironments(
							groupId);
			if (groupEnvAssociations != null && groupEnvAssociations.size() > 0) {
				for (GroupEnvironmentAssociationTransfer grpEnvAssoc : groupEnvAssociations) {
					DependentComponentTransfer depComp = new DependentComponentTransfer();
					depComp.setId(grpEnvAssoc.getEnvironmentId());
					depComp.setName(grpEnvAssoc.getAssociatedComponentName());

					environmentDependencies.add(depComp);
				}
				dependencies.put(ComponentType.Environment,
						environmentDependencies);
			}

			// Getting associated Users
			List<GroupUserAssociationTransfer> groupUserAssociations = DAOFactoryHolder
					.getDAOFactory().getGroupDAO().getAssociatedUsers(groupId);
			if (groupUserAssociations != null
					&& groupUserAssociations.size() > 0) {
				for (GroupUserAssociationTransfer grpUserAssoc : groupUserAssociations) {
					DependentComponentTransfer depComp = new DependentComponentTransfer();
					// user does not have an integer id like other components so
					// set it to -1
					depComp.setId(-1);
					depComp.setName(grpUserAssoc.getUserId());

					userDependencies.add(depComp);
				}
				dependencies.put(ComponentType.User, userDependencies);
			}

			if (dependencies.size() > 0) {
				throw new SAFRDependencyException(dependencies);
			} else {

				DAOFactoryHolder.getDAOFactory().getGroupDAO().removeGroup(
						groupId);
			}

		} else {
			throw new SAFRException(
					"The user is not authorized to perform this deletion.");
		}
	}

	public void removeUser(String userId) throws SAFRException {
		if ((SAFRApplication.getUserSession().getUser().isSystemAdmin())) {
			DAOFactoryHolder.getDAOFactory().getUserDAO().removeUser(userId);
		} else {
			throw new SAFRException(
					"The user is not authorized to perform this deletion.");
		}
	}

	public void removeLogicalFile(Integer logicalFileId) throws SAFRException {
		if (SAFRApplication.getUserSession().getEditRights(
		    ComponentType.LogicalFile, logicalFileId) == EditRights.ReadModifyDelete) {

			List<DependentComponentTransfer> LRDependencies = DAOFactoryHolder
					.getDAOFactory().getLogicalFileDAO()
					.getAssociatedLogicalRecordsWithOneAssociatedLF(
							getCurrentEnvironmentId(), logicalFileId);
			if (LRDependencies.size() > 0) {
				Map<ComponentType, List<DependentComponentTransfer>> dependencies = new HashMap<ComponentType, List<DependentComponentTransfer>>();
				dependencies.put(ComponentType.LogicalRecord, LRDependencies);
				throw new SAFRDependencyException(dependencies);
			}

			Map<ComponentType, List<DependentComponentTransfer>> dependencies = DAOFactoryHolder
					.getDAOFactory().getLogicalFileDAO().getDependencies(
							getCurrentEnvironmentId(), logicalFileId);

			if (dependencies.size() > 0) {
				throw new SAFRDependencyException(dependencies);
			}

			DAOFactoryHolder
					.getDAOFactory()
					.getLogicalFileDAO()
					.removeLogicalFile(logicalFileId, getCurrentEnvironmentId());

		} else {
			throw new SAFRException(
					"The user is not authorized to perform this deletion.");

		}
	}

	/**
	 * This method will clear all the data from an environment. Only a system
	 * admin or an environment admin is allowed to clear an environment.'
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @throws SAFRException
	 *             throws SAFRException if the environment to be cleared is not
	 *             a system admin or an environment admin or if the environment
	 *             admin tries to clear an environment which doesn't have admin
	 *             rights.
	 */
	public void clearEnvironment(Integer environmentId) throws SAFRException {
	    Environment env = getEnvironment(environmentId);
        SAFRLogger.logAllSeparator(logger, Level.INFO, "Clearing environment " + env.getDescriptor() +
            " in schema " + DAOFactoryHolder.getDAOFactory().getConnectionParameters().getSchema());	    
		if (SAFRApplication.getUserSession().isSystemAdministrator() ||
		    SAFRApplication.getUserSession().isEnvironmentAdministrator(environmentId)) {
			DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.clearEnvironment(environmentId);
			// clear the data if the user is an environment admin and has admin
			// permissions on the environment to be cleared.
		} else {
			throw new SAFRException(
					"The user should be an Admin of this environment to perform clear operation.");

		}
        SAFRLogger.logAll(logger, Level.INFO, "Successful clear environment");
        SAFRLogger.logEnd(logger);		
	}

	/**
	 *This method will remove an existing environment. Only a system admin is
	 * allowed to remove an environment.
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @throws SAFRException
	 * @throws SAFRDependencyException
	 *             Thrown when dependencies found in this environment.
	 */
	public void removeEnvironment(Integer environmentId) throws SAFRException {

		if ((SAFRApplication.getUserSession().getUser().isSystemAdmin())) {
			// If there are any dependencies deps will be 1 and a
			// SAFRDepencencyException is thrown, else it will proceed with the
			// deletion of an environment.
			Boolean deps = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.hasDependencies(environmentId);
			if (deps) {
				throw new SAFRDependencyException(null);
			} else {
				DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
						.removeEnvironment(environmentId);
			}
		} else {
			throw new SAFRException(
					"The user is not authorized to perform this deletion.");

		}
	}

    public int getNextLRFieldId() {
        return DAOFactoryHolder.getDAOFactory().getLRFieldDAO().getNextKey();
    }

    public int getNextLRId() {
        return DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().getNextKey();
    }

}
