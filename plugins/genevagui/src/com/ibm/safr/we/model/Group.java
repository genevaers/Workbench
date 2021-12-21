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
import java.util.Set;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.GroupComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupTransfer;
import com.ibm.safr.we.data.transfer.GroupUserAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.GroupUserAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * This class represents a SAFR Group which is created to group a collection of
 * users for identical accesses.
 * 
 */
public class Group extends SAFRComponent {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.Group");
    
	private static final Integer MAX_NAME_LENGTH = 48;
	private SAFRAssociationList<GroupUserAssociation> groupUserAssociations;
	private SAFRAssociationList<GroupEnvironmentAssociation> associatedEnvironments;
	private Map<Integer, Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>>> associatedComponents = new HashMap<Integer, Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>>>();

	/**
	 * This default constructor is used when defining a new Group in the
	 * application. The group ID will be initialized to zero, indicating the
	 * object is not yet persistent. A unique ID will be generated when the
	 * object is first persisted.
	 */
	Group() {
		super();
		groupUserAssociations = new SAFRAssociationList<GroupUserAssociation>();
		associatedEnvironments = new SAFRAssociationList<GroupEnvironmentAssociation>();

	}

	/**
	 * This constructor is used to instantiate an existing Group object from the
	 * database. It accepts the object's values via a transfer object.
	 * 
	 * @param trans
	 *            Group data transfer object
	 */
	Group(GroupTransfer trans) throws DAOException, SAFRException {
		super(trans);
		groupUserAssociations = SAFRAssociationFactory
				.getGroupToUserAssociations(this);
	}

	public SAFRList<GroupUserAssociation> getGroupUserAssociations() {
		return groupUserAssociations;
	}

	public SAFRList<GroupEnvironmentAssociation> getAssociatedEnvironments()
			throws DAOException {
		// Lazily instantiated.
		if (associatedEnvironments == null) {
			associatedEnvironments = SAFRAssociationFactory
					.getGroupToEnvironmentAssociations(this);
		}
		return associatedEnvironments;
	}

    public Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> getEnvironmentComponentRights(int environment)
        throws SAFRException {
        // retrieve all rights for an environment
        getComponentRights(ComponentType.UserExitRoutine,environment);
        getComponentRights(ComponentType.PhysicalFile,environment);
        getComponentRights(ComponentType.LogicalFile,environment);
        getComponentRights(ComponentType.LogicalRecord,environment);
        getComponentRights(ComponentType.LookupPath,environment);
        getComponentRights(ComponentType.View,environment);
        getComponentRights(ComponentType.ViewFolder,environment);
        return associatedComponents.get(environment);
    }
	
	/**
	 * Return a list of GroupComponentAssociations from the specified
	 * environment for the specified component type. These contain this Group's
	 * EditRights for these Components.
	 * 
	 * @param componentType
	 * @param environmentId
	 * @return
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public SAFRList<GroupComponentAssociation> getComponentRights(
			ComponentType componentType, Integer environmentId)
			throws DAOException, SAFRException {
		SAFRAssociationList<GroupComponentAssociation> grpComponentAssociationList = null;

		// Lazily instantiated.
		// check if the map contains map with key as environment id. if no
		// create new map and add it to class level map.
		if (!associatedComponents.containsKey(environmentId)) {

			Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> cmpTypGrpCmpAssociationMap = new HashMap<ComponentType, SAFRAssociationList<GroupComponentAssociation>>();
			associatedComponents.put(environmentId, cmpTypGrpCmpAssociationMap);
		}

		Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> cmpTypGrpCmpAssociationMap = associatedComponents
				.get(environmentId);
		// check if the map contains list with key as component type.if no then
		// retrieve it from database.
		if (!cmpTypGrpCmpAssociationMap.containsKey(componentType)) {

			grpComponentAssociationList = SAFRAssociationFactory
					.getGroupToComponentAssociations(componentType,
							environmentId, this);
			cmpTypGrpCmpAssociationMap.put(componentType,
					grpComponentAssociationList);

		} else {
			grpComponentAssociationList = cmpTypGrpCmpAssociationMap
					.get(componentType);
		}

		return grpComponentAssociationList;
	}

	/**
	 * Return a GroupComponentAssociation from the specified environment for the
	 * specified component type and ID. This contains this Group's EditRights
	 * for this Component.
	 * 
	 * @param componentType
	 *            : The type of component.
	 * @param environmentId
	 *            : The id of the environment to which component belongs.
	 * @param id
	 *            : The id of the component.
	 * @return A GroupComponentAssociation object.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public GroupComponentAssociation getComponentAssociation(
			ComponentType componentType, Integer environmentId, Integer id)
			throws DAOException, SAFRException {
		SAFRList<GroupComponentAssociation> grpCompAssocList = getComponentRights(
				componentType, environmentId);
		GroupComponentAssociation result = null;
		for (GroupComponentAssociation grpCompAssoc : grpCompAssocList) {
			if (grpCompAssoc.getAssociatedComponentIdNum().equals(id)) {
				result = grpCompAssoc;
				break;
			}
		}
		return result;
	}

	@Override
	public void store() throws SAFRException, DAOException {
		GroupTransfer trans = new GroupTransfer();
		setTransferData(trans);

        if (SAFRApplication.getUserSession().isSystemAdministrator()) {		
    		// CQ 7329 Kanchan Rauthan 04/03/2010 To show error if group is
    		// already deleted from database and user still tries to save it.
    		try {
    			trans = DAOFactoryHolder.getDAOFactory().getGroupDAO().persistGroup(trans);
    			setObjectData(trans);
    		} catch (SAFRNotFoundException snfe) {
    			snfe.printStackTrace();
    			throw new SAFRException("The group with id "+ this.getId()
    			    + " cannot be updated as its already been deleted from the database.",snfe);
    		}
        } else {
            throw new SAFRException("The user is not authorized to create/modify groups.");            
        }
	}

	/**
	 * This method is used store the associated users.
	 * 
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void storeAssociatedUsers() throws DAOException, SAFRException {
		// save associated Users
		//String deletionIds = "";
		List<String> deletionIds = new ArrayList<>();
		List<GroupUserAssociationTransfer> list = new ArrayList<GroupUserAssociationTransfer>();
		HashMap<GroupUserAssociationTransfer, GroupUserAssociation> map = new HashMap<GroupUserAssociationTransfer, GroupUserAssociation>();
		int associationSize = groupUserAssociations.size();
		for (int i = 0; i < associationSize; i++) {
			GroupUserAssociation association = groupUserAssociations.get(i);
			if (association.getPersistence() == SAFRPersistence.DELETED) {
				deletionIds.add(association.getAssociatedComponentIdString());
			} else {
				if (association.getPersistence() == SAFRPersistence.NEW) {

					// create transfer object
					GroupUserAssociationTransfer assocTrans = new GroupUserAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					list.add(assocTrans);
					map.put(assocTrans, association);
				}
			}
		}

		// CQ 7826 Santhosh 26/05/2010 Implement 2-phase commit DB
		// transactionality
		boolean success = false;
		List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();
		try {

			while (!success) {
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();

					// call DAO to delete
					if (deletionIds.size() > 0) {
						DAOFactoryHolder.getDAOFactory().getGroupDAO().deleteAssociatedUser(getId(), deletionIds);
					}

					// call DAO to add
					if (list.size() > 0) {
						list = DAOFactoryHolder.getDAOFactory().getGroupDAO()
								.persistAssociatedUsers(list, getId());
						for (GroupUserAssociationTransfer assocTrans : list) {
							map.get(assocTrans).setObjectFromTransfer(
									assocTrans);
							savedObjs.add(map.get(assocTrans));
						}
					}

					groupUserAssociations.flushDeletedItems();

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

	/**
	 * This method is used store the associated Environments.
	 * 
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void storeAssociatedEnvironments() throws DAOException,
			SAFRException {
		if (SAFRApplication.getUserSession().isSystemAdministrator()) {
			// save associated Environments

			Map<GroupEnvironmentAssociationTransfer, GroupEnvironmentAssociation> map = new HashMap<GroupEnvironmentAssociationTransfer, GroupEnvironmentAssociation>();

			List<GroupEnvironmentAssociationTransfer> newAssociatedEnvsTrnsList = new ArrayList<GroupEnvironmentAssociationTransfer>();
			List<GroupEnvironmentAssociationTransfer> oldAssociatedEnvsTrnsList = new ArrayList<GroupEnvironmentAssociationTransfer>();
			List<GroupEnvironmentAssociationTransfer> deletedAssociatedEnvsTrnsList = new ArrayList<GroupEnvironmentAssociationTransfer>();

			int associationSize = associatedEnvironments.size();
			for (int i = 0; i < associationSize; i++) {
				GroupEnvironmentAssociation association = associatedEnvironments
						.get(i);
				if (association.getPersistence() == SAFRPersistence.DELETED) {
					GroupEnvironmentAssociationTransfer assocTrans = new GroupEnvironmentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					deletedAssociatedEnvsTrnsList.add(assocTrans);
				} else if ((association.getPersistence() == SAFRPersistence.NEW)) {
					// create transfer object
					GroupEnvironmentAssociationTransfer assocTrans = new GroupEnvironmentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					newAssociatedEnvsTrnsList.add(assocTrans);
					map.put(assocTrans, association);
				} else if ((association.getPersistence() == SAFRPersistence.MODIFIED)) {
					GroupEnvironmentAssociationTransfer assocTrans = new GroupEnvironmentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					oldAssociatedEnvsTrnsList.add(assocTrans);
					map.put(assocTrans, association);
				}

			}

			// call DAO to add ,delete,update, if any list has elements.
			if ((newAssociatedEnvsTrnsList.size() > 0)
					|| (oldAssociatedEnvsTrnsList.size() > 0)
					|| (deletedAssociatedEnvsTrnsList.size() > 0)) {
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
											newAssociatedEnvsTrnsList,
											oldAssociatedEnvsTrnsList,
											deletedAssociatedEnvsTrnsList);
							associatedEnvironments.flushDeletedItems();
							for (GroupEnvironmentAssociationTransfer assocTrans : map
									.keySet()) {
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
		} else {
			throw new SAFRException(
					"The current user is not authorized to associate environments to group.");
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

		Map<ComponentType, SAFRAssociationList<GroupComponentAssociation>> compTypListMap = new HashMap<ComponentType, SAFRAssociationList<GroupComponentAssociation>>();
		Set<Integer> envIdKeySet = associatedComponents.keySet();
		// iterate over environment Id key set.
		for (Integer envIdKey : envIdKeySet) {

			Set<ComponentType> compTypKeySet = associatedComponents.get(
					envIdKey).keySet();
			// iterate over component type key set.
			for (ComponentType compTypeKey : compTypKeySet) {

				SAFRAssociationList<GroupComponentAssociation> grpCmpAssoList = null;
				if (compTypListMap.get(compTypeKey) == null) {
					grpCmpAssoList = new SAFRAssociationList<GroupComponentAssociation>();
					compTypListMap.put(compTypeKey, grpCmpAssoList);
				}
				grpCmpAssoList = compTypListMap.get(compTypeKey);
				grpCmpAssoList.addAll(associatedComponents.get(envIdKey).get(
						compTypeKey));
			}
		}

		Set<ComponentType> compTypListMapKeySet = compTypListMap.keySet();
		for (ComponentType componentType : compTypListMapKeySet) {
			SAFRAssociationList<GroupComponentAssociation> grpCmpAssoList = compTypListMap
					.get(componentType);

			// Lists for saving associated component rights.
			List<GroupComponentAssociationTransfer> newAssociatedCompTrnsList = new ArrayList<GroupComponentAssociationTransfer>();
			List<GroupComponentAssociationTransfer> oldAssociatedCompTrnsList = new ArrayList<GroupComponentAssociationTransfer>();
			List<GroupComponentAssociationTransfer> deletedAssociatedCompTrnsList = new ArrayList<GroupComponentAssociationTransfer>();

			Map<GroupComponentAssociationTransfer, GroupComponentAssociation> map = new HashMap<GroupComponentAssociationTransfer, GroupComponentAssociation>();

			for (GroupComponentAssociation association : grpCmpAssoList) {
				// check if the component is old.If it is not old then check its
				// persistence and add it to the list to pass it to DAO.
				if (association.getPersistence() != SAFRPersistence.OLD) {
					GroupComponentAssociationTransfer assocTrans = new GroupComponentAssociationTransfer();
					association.setTransferFromObject(assocTrans);
					// create transfer object for modified
					// association
					if (association.getPersistence() == SAFRPersistence.MODIFIED) {
						oldAssociatedCompTrnsList.add(assocTrans);
						map.put(assocTrans, association);
					} else if (association.getPersistence() == SAFRPersistence.DELETED) {
						deletedAssociatedCompTrnsList.add(assocTrans);
					} else if (association.getPersistence() == SAFRPersistence.NEW) {
						newAssociatedCompTrnsList.add(assocTrans);
						map.put(assocTrans, association);
					}
				}
			}
			// call DAO to add ,delete,update, if any list has elements.
			if ((newAssociatedCompTrnsList.size() > 0)
					|| (oldAssociatedCompTrnsList.size() > 0)
					|| (deletedAssociatedCompTrnsList.size() > 0)) {
				// CQ 7826 Santhosh 26/05/2010 Implement 2-phase commit DB
				// transactionality
				boolean success = false;
				try {

					while (!success) {
						try {
							// Begin Transaction
							DAOFactoryHolder.getDAOFactory().getDAOUOW()
									.begin();
							DAOFactoryHolder.getDAOFactory().getGroupDAO()
									.persistComponentEditRights(componentType,
											this.id, newAssociatedCompTrnsList,
											oldAssociatedCompTrnsList,
											deletedAssociatedCompTrnsList);
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
						continue;
					}
				}

			}

			// set model to post persist state
			for (Integer envIdKey : envIdKeySet) {
				SAFRAssociationList<GroupComponentAssociation> grpCompAssoList = associatedComponents
						.get(envIdKey).get(componentType);
				grpCompAssoList.flushDeletedItems();
				for (GroupComponentAssociation ass : grpCompAssoList) {
				    ass.setPersistence(SAFRPersistence.OLD);
				}
			}
		}
	}

	/**
	 * This enum maintains the properties of a group.
	 * 
	 */
	public enum Property {
		NAME, COMMENT
	}

	/**
	 * Validate method is used to validate a Group object.If any validation
	 * condition is not met then this method throws a list of all the error
	 * messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the group, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * 
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		if ((getName() == null) || (getName() == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"Group name cannot be empty.");
		} else {
			if (getName().length() > MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of Group name "
		        		+ ModelUtilities.formatNameForErrMsg(
					    getName(),isForImport()) +	 																	
						"cannot exceed 48 characters.");
			}
			else if (this.isDuplicate()) {
				safrValidationException
						.setErrorMessage(
								Property.NAME,
								"The Group name '"
										+ getName()
										+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(getName())) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The Group name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),isForImport())
                                        + "should start with a letter and should comprise of letters"
                                        + ", numbers, pound sign (#) and underscores only.");
            }
			if (getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH) {
				safrValidationException.setErrorMessage(Property.COMMENT,
						"Comment cannot be more than 254 characters.");
			}
		}
		if (!safrValidationException.getErrorMessages().isEmpty())
			throw safrValidationException;
	}

	/**
	 * This method is used to check whether the Group name already exist in the
	 * workbench.
	 * 
	 * @return true if the Group name already exists.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private boolean isDuplicate() throws DAOException, SAFRException {

		try {
			GroupTransfer groupTransfer = null;
			groupTransfer = DAOFactoryHolder.getDAOFactory().getGroupDAO()
					.getDuplicateGroup(getName(), getId());
			if (groupTransfer == null) {
				return false;
			} else
				return true;
		} catch (DAOException de) {
			throw new SAFRException("Data access error for Group.", de);
		}
	}

	/**
	 * This method is used to assign full rights to a component based on the
	 * component type.
	 * 
	 * @param component
	 *            component on which the full rights to be assigned to.
	 * @param componentType
	 *            type of the component.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void assignComponentFullRights(SAFREnvironmentalComponent component,
			ComponentType componentType) throws DAOException, SAFRException {

	    // first check if the users role already gives full rights
	    if (SAFRApplication.getUserSession().getRoleEditRights(
	        componentType, component.getEnvironmentId()) != EditRights.ReadModifyDelete) {
	        SAFRList<GroupComponentAssociation> currAssocs = getComponentRights(
                componentType, component.getEnvironmentId());
            GroupComponentAssociation componentAssoc = new GroupComponentAssociation(
                    this, component.getName(), component.getId(), component
                            .getEnvironmentId());
            componentAssoc.setRights(EditRights.ReadModifyDelete);
            currAssocs.add(componentAssoc);
            storeComponentRights();	        
	    }
	}

	/**
	 * This method is used to assign this group the specified edit rights to the
	 * specified component.
	 * 
	 * @param component
	 *            component on which the full rights to be assigned to.
	 * @param componentType
	 *            type of the component.
	 * @param editRights
	 *            the EditRights to be assigned.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public void assignComponentEditRights(SAFREnvironmentalComponent component,
			ComponentType componentType, EditRights editRights)
			throws DAOException, SAFRException {

        // first check if the users role already gives full rights
        if (SAFRApplication.getUserSession().getRoleEditRights(
            componentType, component.getEnvironmentId()) != editRights) {
	    
    		SAFRList<GroupComponentAssociation> currAssocs = getComponentRights(
    				componentType, component.getEnvironmentId());
    		GroupComponentAssociation componentAssoc = new GroupComponentAssociation(
    				this, component.getName(), component.getId(),
    				component.getEnvironmentId());
    		componentAssoc.setRights(editRights);
    		currAssocs.add(componentAssoc);
    		storeComponentRights();
        }
	}

    /**
     * This method is used to clear edit rights for a component
     * 
     * @param component
     *            component to clear the edit rights from
     * @param componentType
     *            type of the component.
     * @throws DAOException
     * @throws SAFRException
     */
    public void clearComponentEditRights(SAFREnvironmentalComponent component,
            ComponentType componentType)
            throws DAOException, SAFRException {

        SAFRList<GroupComponentAssociation> currAssocs = getComponentRights(
                componentType, component.getEnvironmentId());
        boolean removed = false;
        for (GroupComponentAssociation ass : currAssocs) {
            if (ass.getAssociatedComponentIdNum() == component.getId()) {
                currAssocs.remove(ass);
                removed = true;
                break;
            }
        }
        if (removed) {
            storeComponentRights();
        }
    }
	
	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {

        if (!SAFRApplication.getUserSession().isSystemAdministrator()) {
            throw new SAFRException("The user is not authorized to create/modify groups.");                        
        }
	    
		Group groupCopy = SAFRApplication.getSAFRFactory().createGroup();
		groupCopy.setName(newName);
		groupCopy.setComment(this.getComment());
		groupCopy.validate();
		groupCopy.store();
		// Store Environment associations for a newly created group.
		SAFRList<GroupEnvironmentAssociation> grpCopyEnvAssocs = groupCopy
				.getAssociatedEnvironments();

		for (GroupEnvironmentAssociation assoc : this
				.getAssociatedEnvironments()) {

			GroupEnvironmentAssociation environmentAssociationCopy = new GroupEnvironmentAssociation(
					groupCopy, assoc.getAssociatedComponentName(), assoc
							.getAssociatedComponentIdNum());
			environmentAssociationCopy.setEnvRole(assoc.getEnvRole());
			if (assoc.canCreateLogicalFile()) {
				environmentAssociationCopy.addPermission(Permissions.CreateLogicalFile);
			}
			if (assoc.canCreateLogicalRecord()) {
				environmentAssociationCopy.addPermission(Permissions.CreateLogicalRecord);
			}
			if (assoc.canCreatePhysicalFile()) {
				environmentAssociationCopy.addPermission(Permissions.CreatePhysicalFile);
			}
			if (assoc.canCreateUserExitRoutine()) {
				environmentAssociationCopy.addPermission(Permissions.CreateUserExitRoutine);
			}
            if (assoc.canCreateLookupPath()) {
                environmentAssociationCopy.addPermission(Permissions.CreateLookupPath);
            }
            if (assoc.canCreateView()) {
                environmentAssociationCopy.addPermission(Permissions.CreateView);
            }
			if (assoc.canCreateViewFolder()) {
				environmentAssociationCopy.addPermission(Permissions.CreateViewFolder);
			}
			if (assoc.canMigrateIn()) {
				environmentAssociationCopy.addPermission(Permissions.MigrateIn);
			}
			grpCopyEnvAssocs.add(environmentAssociationCopy);

			copyComponentAssociations(groupCopy, assoc,ComponentType.PhysicalFile);
			copyComponentAssociations(groupCopy, assoc,ComponentType.LogicalFile);
			copyComponentAssociations(groupCopy, assoc,ComponentType.LogicalRecord);
			copyComponentAssociations(groupCopy, assoc,ComponentType.UserExitRoutine);
            copyComponentAssociations(groupCopy, assoc,ComponentType.LookupPath);
            copyComponentAssociations(groupCopy, assoc,ComponentType.View);
			copyComponentAssociations(groupCopy, assoc,ComponentType.ViewFolder);
		}
		groupCopy.storeComponentRights();
		groupCopy.storeAssociatedEnvironments();

		// Store Group User Association of a newly created group.
		SAFRList<GroupUserAssociation> grpCopyUserAssocs = groupCopy
				.getGroupUserAssociations();
		for (GroupUserAssociation groupUserAssociation : this.getGroupUserAssociations()) {
			GroupUserAssociation groupUserAssociationCopy = new GroupUserAssociation(
				groupCopy, groupUserAssociation.getAssociatedComponentName(), 
				groupUserAssociation.getAssociatedComponentIdString());
			grpCopyUserAssocs.add(groupUserAssociationCopy);
		}
		groupCopy.storeAssociatedUsers();

		return groupCopy;
	}

	private void copyComponentAssociations(Group groupCopy,
			GroupEnvironmentAssociation assoc, ComponentType compType)
			throws DAOException, SAFRException {
		for (GroupComponentAssociation compAssoc : this.getComponentRights(
				compType, assoc.getEnvironmentId())) {
			SAFRList<GroupComponentAssociation> grpCopyComponentAssocs = groupCopy
					.getComponentRights(compType, compAssoc.getEnvironmentId());

			GroupComponentAssociation copyComponentAssociation = new GroupComponentAssociation(
					groupCopy, compAssoc.getAssociatedComponentName(),
					compAssoc.getAssociatedComponentIdNum(), compAssoc.getEnvironmentId());
			copyComponentAssociation.setRights(compAssoc.getRights());
			grpCopyComponentAssocs.add(copyComponentAssociation);
		}

	}

}
