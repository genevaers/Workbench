package com.ibm.safr.we.model.query;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.GroupUserAssociation;
import com.ibm.safr.we.model.associations.UserGroupAssociation;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;

/**
 * 
 * A utility class used for query operations against the database which require
 * just a minimal set of information in each result row, rather than a complete
 * model object. Generally used for displaying lists of meta-data components.
 * This class is state-less so its query methods are defined as static.
 */
public class SAFRQuery {

	/**
	 * This method is used to retrieve all the Physical Files from
	 * PHYFILE table with only selected columns. As the number of columns
	 * selected is less, the performance improves while populating the list of
	 * Physical Files.
	 * <p>
	 * If the logged in user is a System Administrator or a User with Admin
	 * permission on the current Environment, it returns all of the Physical
	 * Files for the specified environment. For a general User it will return
	 * only the Physical Files that the User's login Group has at least Read
	 * access to.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the Physical Files
	 *            belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all PhysicalFileQueryBean objects.
	 * @throws DAOException
	 */
	static public List<PhysicalFileQueryBean> queryAllPhysicalFiles(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getPhysicalFileDAO()
				.queryAllPhysicalFiles(environmentId, sortType);
	}

	/**
	 * This method is used to retrieve all the Control Records from CONTROLREC
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Control
	 * Records.
	 * 
	 * @param environmentId
	 *            : the Id of the environment to which the Control Records
	 *            belong.
	 *@param sortType
	 *            : the type of sorting needed on the list(by Id or by name).
	 * @return A list of all ControlRecordQueryBean objects.
	 * @throws DAOException
	 */
	static public List<ControlRecordQueryBean> queryAllControlRecords(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getControlRecordDAO()
				.queryAllControlRecords(environmentId, sortType);
	}

	/**
	 * This method is used to retrieve all the Environments from ENVIRON
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Environments.
	 * <p>
	 * If the logged in user is a System Administrator it will return all
	 * Environments. For a general User (with or without Admin rights on the
	 * current Environment), it will return just the Environments associated
	 * with any Groups that User belongs to.
	 * 
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of EnvironmentQueryBean objects.
	 * @throws DAOException
	 */
	static public List<EnvironmentQueryBean> queryAllEnvironments(
			SortType sortType) throws DAOException {
		if (SAFRApplication.getUserSession().isSystemAdministrator()) {
			return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.queryAllEnvironments(sortType);
		} else {
			return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.queryAllEnvironments(
							sortType,
							SAFRApplication.getUserSession().getUser()
									.getUserid(), false);
		}
	}

	/**
	 * Returns a list of query beans for all Environments accessible by a User.
	 * <p>
	 * If the specified user is a System Administrator it will return all
	 * Environments. For a general User (with or without Admin rights on the
	 * current Environment), it will return just the Environments associated
	 * with any Groups that User belongs to.
	 * 
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @param user
	 *            : Current user.
	 * @return A list of EnvironmentQueryBean objects.
	 * @throws DAOException
	 */
	static public List<EnvironmentQueryBean> queryAllEnvironments(
			SortType sortType, User user) throws DAOException {
		if (user.isSystemAdmin()) {
			return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.queryAllEnvironments(sortType);
		} else {
			return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.queryAllEnvironments(sortType, user.getUserid(), false);
		}
	}

	/**
	 * This method is used to retrieve all the Groups from GROUP table
	 * with only selected columns. As the number of columns selected is less,
	 * the performance improves while populating the list of Groups.
	 * 
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all GroupQueryBean objects.
	 * @throws DAOException
	 */
	static public List<GroupQueryBean> queryAllGroups(SortType sortType)
			throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getGroupDAO().queryAllGroups(
				sortType);
	}

	/**
	 * This method is used to retrieve all the View Folders from VIEWFOLDER
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of View Folders.
	 * <p>
	 * If the logged in user is a System Administrator or a User with Admin
	 * permission on the specified Environment, it returns all of the View Folders
	 * for the specified environment. For a general User it will return only the
	 * View Folders that the User's login Group has at least Read access to.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the View Folders belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all ViewFolderQueryBean objects.
	 * @throws DAOException
	 */
	static public List<ViewFolderQueryBean> queryAllViewFolders(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getViewFolderDAO()
				.queryAllViewFolders(environmentId, sortType);
	}

	/**
	 * This method returns a list of ViewFolderQueryBeans for all View Folders
	 * that the user has at least Modify rights on.
	 * <p>
	 * If the logged in user is a System Administrator or a User with Admin
	 * permission on the specified Environment, it returns all of the View
	 * Folders for the specified environment. For a general User it will return
	 * only the View Folders that the User's login Group has at least Modify
	 * right on.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the View Folders belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all ViewFolderQueryBean objects.
	 * @throws SAFRException
	 */
	static public List<ViewFolderQueryBean> queryAllViewFoldersAtLeastModifyRights(
			Integer environmentId, SortType sortType) throws SAFRException {
		List<ViewFolderQueryBean> atLeastReadRightsList = queryAllViewFolders(
				environmentId, sortType);
		if (SAFRApplication.getUserSession().isSystemAdministrator()) {
			return atLeastReadRightsList;
		}
		// user is an ordinary user
		List<ViewFolderQueryBean> atLeastModifyRightsList = new ArrayList<ViewFolderQueryBean>();
		for (ViewFolderQueryBean bean : atLeastReadRightsList) {
			if (bean.getRights() == EditRights.ReadModify || 
			    bean.getRights() == EditRights.ReadModifyDelete) {
				atLeastModifyRightsList.add(bean);
			}
		}
		return atLeastModifyRightsList;
	}

	/**
	 * This function is used to retrieve all the Views from VIEW table with
	 * only selected columns. As the number of columns selected is less, the
	 * performance improves while populating the list of Views.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Views belong.
	 * @param viewFolderId
	 *            : The Id of the View Folder from which Views are to be
	 *            extracted. If its -1 then it will bring views from all View
	 *            Folders.
	 * @param sortType
	 *            : The type on which the result is to be sorted(by Id or name).
	 * @return A list of all ViewQueryBean objects.
	 * @throws DAOException
	 */
	static public List<ViewQueryBean> queryAllViews(Integer environmentId,
			Integer viewFolderId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViews(
				sortType, environmentId, viewFolderId);
	}

    /**
     * This function is used to retrieve all the Views from VIEW table with
     * only selected columns. As the number of columns selected is less, the
     * performance improves while populating the list of Views.
     * 
     * @param environmentId
     *            : the ID of the environment to which the Views belong.
     * @param viewFolderId
     *            : The Id of the View Folder from which Views are to be
     *            extracted. If its -1 then it will bring views from all View
     *            Folders.
     * @param sortType
     *            : The type on which the result is to be sorted(by Id or name).
     * @return A list of all ViewQueryBean objects.
     * @throws DAOException
     */
    static public List<ViewQueryBeanConv> queryAllViewsConv(Integer environmentId,
            Integer viewFolderId, SortType sortType) throws DAOException {
        List<ViewQueryBean> beans =  DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViews(
                sortType, environmentId, viewFolderId);
        List<ViewQueryBeanConv> beancs = new ArrayList<ViewQueryBeanConv>();
        for (ViewQueryBean bean : beans) {
            beancs.add(new ViewQueryBeanConv(bean));
        }
        return beancs;
    }
	
	/**
	 * This method is used to retrieve all the Users from USER table with
	 * only selected columns. As the number of columns selected is less, the
	 * performance improves while populating the list of Users.
	 * 
	 * @return A list of all UserQueryBean objects.
	 * @throws DAOException
	 */
	static public List<UserQueryBean> queryAllUsers() throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getUserDAO().queryAllUsers();
	}

	/**
	 * This method is used to retrieve all the User-Exit Routines from
	 * EXIT table with only selected columns. As the number of columns
	 * selected is less, the performance improves while populating the list of
	 * User-Exit Routines.
	 * <p>
	 * If the logged in user is a System Administrator or a User with Admin
	 * permission on the current Environment, it returns all of the User Exit
	 * Routines for the specified environment. For a general User it will return
	 * only the User Exit Routines that the User's login Group has at least Read
	 * access to.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routines
	 *            belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all UserExitRoutineQueryBean objects.
	 * @throws DAOException
	 */
	static public List<UserExitRoutineQueryBean> queryAllUserExitRoutines(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO()
				.queryAllUserExitRoutines(environmentId, sortType);
	}

	/**
	 *This method is used to retrieve the User-Exit Routines, of specified type
	 * from EXIT table with only selected columns.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the User Exit Routines
	 *            belong.
	 * @param typeCodeKey
	 *            : The type of the User Exit Routines.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of UserExitRoutineQueryBean objects which matches the type
	 *         specified.
	 * @throws DAOException
	 */
	static public List<UserExitRoutineQueryBean> queryUserExitRoutines(
			Integer environmentId, String typeCodeKey, SortType sortType)
			throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO()
				.queryUserExitRoutines(environmentId, typeCodeKey, sortType);
	}

	/**
	 * This method is used to retrieve all the Logical Files from LOGFILE
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Logical
	 * Files.
	 * <p>
	 * If the logged in user is a System Administrator or a User with Admin
	 * permission on the current Environment, it returns all of the Logical
	 * Files for the specified environment. For a general User it will return
	 * only the Logical Files that the User's login Group has at least Read
	 * access to.
	 * 
	 * @param environmentId
	 *            : The id of the environment to which the Logical Files belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all LogicalFileQueryBean objects.
	 * @throws DAOException
	 */
	static public List<LogicalFileQueryBean> queryAllLogicalFiles(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLogicalFileDAO()
				.queryAllLogicalFiles(environmentId, sortType);
	}

	/**
	 * This function is used to retrieve all the Logical Records from LOGREC
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Logical
	 * Records.
	 * <p>
	 * If the logged in user is a System Administrator or a User with Admin
	 * permission on the current Environment, it returns all of the Logical
	 * Records for the specified environment. For a general User it will return
	 * only the Logical Records that the User's login Group has at least Read
	 * access to.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical records
	 *            belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all LogicalRecordQueryBean objects.
	 * @throws DAOException
	 */
	static public List<LogicalRecordQueryBean> queryAllLogicalRecords(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.queryAllLogicalRecords(environmentId, sortType);
	}

	/**
	 * This function is used to retrieve all the Lookup Paths from LOOKUP
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Lookup Paths.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Lookup Paths belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all LookupQueryBean objects.
	 * @throws DAOException
	 */
	static public List<LookupQueryBean> queryAllLookups(Integer environmentId,
			SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLookupDAO().queryAllLookups(
				environmentId, sortType);
	}

	/**
	 * This function is used to retrieve all the Lookup Paths from LOOKUP
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Lookup Paths.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Lookup Paths belong.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all LookupQueryBean objects.
	 * @throws DAOException
	 */
	static public List<OldCompilerJoinQueryBean> queryOldCompilerJoinFields(Integer envId,
			Integer srcLRId) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getOldCompilerDAO().getJoinFields(
				envId, srcLRId);
	}

	/**
	 * This method is to retrieve all the possible Physical Files which can be
	 * associated with the Logical File. The Physical Files which are already
	 * associated with the Logical File are not included.
	 * 
	 * @param logicalFile
	 *            : The model object of the Logical File.
	 * @param environmentId
	 *            : the ID of the environment to which the Logical File belongs.
	 * @return A list of PhysicalFileQueryBean objects.
	 * @throws DAOException
	 */
	static public List<PhysicalFileQueryBean> queryPossiblePFAssociations(
			LogicalFile logicalFile, Integer environmentId) throws DAOException {
		List<PhysicalFileQueryBean> result = new ArrayList<PhysicalFileQueryBean>();
		//String notInParam = "";
		List<Integer> notInParam  = new ArrayList<>();
		List<FileAssociation> associatedPFs = logicalFile
				.getPhysicalFileAssociations().getActiveItems();
		for (FileAssociation association : associatedPFs) {
			FileAssociation assoc = association;
			notInParam.add(assoc.getAssociatedComponentIdNum());
		}
		result = DAOFactoryHolder.getDAOFactory().getLogicalFileDAO()
				.queryPossiblePFAssociations(environmentId, notInParam);
		return result;
	}

	/**
	 * This method is to retrieve all the possible Logical Files which can be
	 * associated with the Logical Record. The Logical Files which are already
	 * associated with the Logical Record are not included.
	 * 
	 * @param logicalRecord
	 *            : The model object of the Logical Record.
	 * @param environmentId
	 *            : the ID of the environment to which the Logical Record
	 *            belongs.
	 * @return A list of LogicalFileQueryBean objects.
	 * @throws DAOException
	 */
	static public List<LogicalFileQueryBean> queryPossibleLFAssociations(
			LogicalRecord logicalRecord, Integer environmentId)
			throws DAOException {

		List<LogicalFileQueryBean> result = new ArrayList<LogicalFileQueryBean>();
		List<Integer> notInParam = new ArrayList<>(); 
		List<ComponentAssociation> associatedLFs = logicalRecord
				.getLogicalFileAssociations().getActiveItems();
		notInParam.add(0);
		for (ComponentAssociation association : associatedLFs) {
			ComponentAssociation assoc = association;
			notInParam.add(assoc.getAssociatedComponentIdNum());
		}
		result = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.queryPossibleLFAssociations(environmentId, notInParam);
		return result;
	}

    public static List<ViewQueryBean> queryPossibleViewAssociations(ViewFolder viewFolder, int environmentId) throws DAOException
    {
        List<ViewQueryBean> result = new ArrayList<ViewQueryBean>();
        List<Integer> notInParam = new ArrayList<Integer>();
        if (viewFolder != null) {
            List<ViewFolderViewAssociation> associatedViews = viewFolder.getViewAssociations().getActiveItems();
            for (ViewFolderViewAssociation association : associatedViews) {
            	notInParam.add(association.getAssociatedComponentIdNum());
            }
        }
        result = DAOFactoryHolder.getDAOFactory().getViewFolderDAO().
            queryPossibleViewAssociations(environmentId, notInParam);
        return result;
    }
	
	/**
	 * This method is to retrieve all the possible Users which can be associated
	 * with the Group. The Users which are already associated with the Group are
	 * not included.
	 * 
	 * @param group
	 *            : The model object of the Group.
	 * @return A list of UserQueryBean objects.
	 * @throws DAOException
	 */
	static public List<UserQueryBean> queryPossibleUserAssociations(Group group)
			throws DAOException {

		List<UserQueryBean> result = new ArrayList<UserQueryBean>();
		List<String> notInParam = new ArrayList<>();
		List<GroupUserAssociation> associatedUsers = group
				.getGroupUserAssociations().getActiveItems();
		for (GroupUserAssociation association : associatedUsers) {
			GroupUserAssociation assoc = association;
			notInParam.add(assoc.getAssociatedComponentIdString());
		}
		result = DAOFactoryHolder.getDAOFactory().getGroupDAO()
				.queryPossibleUserAssociations(notInParam);
		return result;
	}

    public static Collection<? extends GroupQueryBean> queryPossibleUserGroupAssociations(User user) {
        
        List<GroupQueryBean> beans = queryAllGroups(SortType.SORT_BY_ID);
    	if(user.getAssociatedGroups() != null) {
    		List<UserGroupAssociation> assList = user.getAssociatedGroups().getActiveItems();
	        List<GroupQueryBean> remove = new ArrayList<GroupQueryBean>();
	        
	        for (UserGroupAssociation ass : assList) {
	            for (GroupQueryBean bean : beans) {
	                if (ass.getAssociatedComponentIdNum().equals(bean.getId())) {
	                    remove.add(bean);
	                    break;
	                }
	            }
	        }
	        beans.removeAll(remove);
    	}
        return beans;
    }

	
	/**
	 * This method is to retrieve all the possible environments which can be
	 * associated with this Group.
	 * 
	 * @param group
	 *            : The model object of the Group.
	 * @return A list of EnvironmentQueryBean objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public List<EnvironmentQueryBean> queryPossibleEnvironmentAssociations(
			Group group) throws DAOException {

		List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();
		List<GroupEnvironmentAssociation> associatedEnvironments = group
				.getAssociatedEnvironments().getActiveItems();
		List<Integer> associatedEnvIds = new ArrayList<Integer>();
		for (GroupEnvironmentAssociation association : associatedEnvironments) {
			GroupEnvironmentAssociation assoc = association;
			associatedEnvIds.add(assoc.getAssociatedComponentIdNum());

		}
		result = DAOFactoryHolder.getDAOFactory().getGroupDAO()
				.queryPossibleEnvironmentAssociations(associatedEnvIds);
		return result;
	}

	/**
	 * This method is to retrieve all the possible Groups which can be
	 * associated with this environment.
	 * 
	 * @param environment
	 *            : The model object of the Environment.
	 * @return A list of GroupQueryBean objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public List<GroupQueryBean> queryPossibleGroupAssociations(
			Environment environment) throws DAOException {

		List<GroupQueryBean> result = new ArrayList<GroupQueryBean>();
		List<GroupEnvironmentAssociation> associatedGroups = environment
				.getAssociatedGroups().getActiveItems();
		List<Integer> associatedGrpIds = new ArrayList<Integer>();
		for (GroupEnvironmentAssociation association : associatedGroups) {
			GroupEnvironmentAssociation assoc = association;
			associatedGrpIds.add(assoc.getAssociatingComponentId());

		}
		result = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
				.queryPossibleGroupAssociations(associatedGrpIds);
		return result;
	}

	/**
	 * This method is to retrieve all the possible components which can be
	 * associated with this Group.
	 * 
	 * @param comType
	 *            : The type of component. One of Physical File, Logical File,
	 *            Logical Record, User Exit Routine and View Folder.
	 * @param group
	 *            : The model object of the Group.
	 * @param environmentId
	 *            : The Id of the environment to which components belong.
	 * @return A list of EnvironmentalQueryBean objects which contains the
	 *         components which can be associated with the Group.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public List<EnvironmentalQueryBean> queryPossibleComponentAssociation(
			ComponentType comType, Group group, Integer environmentId)
			throws DAOException, SAFRException {

		List<EnvironmentalQueryBean> result = new ArrayList<EnvironmentalQueryBean>();
		List<GroupComponentAssociation> associatedComponents = group
				.getComponentRights(comType, environmentId).getActiveItems();

		List<Integer> associatedComponentIds = new ArrayList<Integer>();
		for (GroupComponentAssociation association : associatedComponents) {
			GroupComponentAssociation assoc = association;
			associatedComponentIds.add(assoc.getAssociatedComponentIdNum());

		}
		result = DAOFactoryHolder.getDAOFactory().getGroupDAO()
				.queryPossibleComponentAssociations(comType, environmentId,
						associatedComponentIds);

		return result;
	}

	/**
	 * This method is to return all the Logical Records which are currently in
	 * active state. The Logical Record list is ordered by the Logical Record
	 * name.
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of LogicalRecordQueryBean objects for all active Logical
	 *         Records ordered by name.
	 * @throws DAOException
	 */
	static public List<LogicalRecordQueryBean> queryAllActiveLogicalRecords(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.queryAllActiveLogicalRecords(environmentId, sortType);
	}

	/**
	 * This function is used to retrieve all the Views from VIEW table with
	 * only selected columns. The selected columns include the information of
	 * View Folder id and name to which the Views belong. The function doesn't
	 * return any information about the version history or Check Out status. As
	 * the number of columns selected is less, the performance improves while
	 * populating the list of Views.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Views belong.
	 * @param sortType
	 *            : The type on which the result is to be sorted(by Id or name).
	 * @return A list of all ViewQueryBean objects.
	 * @throws DAOException
	 */
	static public List<ViewQueryBean> queryAllViews(Integer environmentId,
			SortType sortType)
			throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViews(
				sortType, environmentId, SAFRApplication.getUserSession().isSystemAdministrator());
	}

    /**
     * 
     * @param environmentId
     * @param sortType
     * @param admin
     * @return
     * @throws DAOException
     */
    static public List<ViewQueryBean> queryAllViews(Integer environmentId,
            SortType sortType, boolean admin)
            throws DAOException {
        return DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllViews(
                sortType, environmentId, admin);
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    static public List<ViewTransfer> queryAllLogicBlocks()
        throws DAOException {
        return DAOFactoryHolder.getDAOFactory().getViewDAO().queryAllLogicBlocks();
    }

    /**
     * 
     * @param environmentId
     * @param viewId
     * @return
     * @throws DAOException
     */
    static public ViewQueryBean queryView(Integer environmentId, Integer viewId)
            throws DAOException {
        return DAOFactoryHolder.getDAOFactory().getViewDAO().queryView(environmentId, viewId);
    }

	/**
	 * This method is to populate all the lists of different metadata components
	 * which are used in View Properties.
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @return A Map with Component type as key and value as a list of
	 *         EnvironmentalQueryBean objects.
	 * @throws DAOException
	 */
	static public Map<ComponentType, List<EnvironmentalQueryBean>> queryViewPropertiesLists(
			Integer environmentId) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getViewDAO()
				.queryViewPropertiesLists(environmentId);
	}

	/**
	 * This method takes the Id of an LR Field and return the Query Bean of the
	 * Logical Record to which the Field belongs.
	 * 
	 * @param LRFieldId
	 *            : The Id of the LR Field.
	 * @param environmentId
	 *            : The Id of the environment to which Logical Record belongs.
	 * @return A LogicalRecordQueryBean object for Logical Record to which the
	 *         field belongs.
	 * @throws DAOException
	 */
	static public LogicalRecordQueryBean queryLogicalRecordByField(Integer LRFieldId,
			Integer environmentId) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.queryLogicalRecordByField(LRFieldId, environmentId);
	}

	/**
	 * 
	 * @param LRID
	 * @param environmentId
	 * @return
	 */
    public static LogicalRecordQueryBean queryLogicalRecord(Integer LRID, Integer environmentId) {
        return DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
            .queryLogicalRecord(LRID, environmentId);
    }
	
	/**
	 * This method takes the Id of an Lookup Path and return the Query Bean of
	 * the Lookup Path.
	 * 
	 * @param lookupPathId
	 *            : The Id of the Lookup Path.
	 * @param environmentId
	 *            : The Id of the environment to which Lookup Path belongs.
	 * @return A LookupQueryBean object for Lookup Path.
	 * @throws DAOException
	 */
	static public LookupQueryBean queryLookupPath(Integer lookupPathId,
			Integer environmentId) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLookupDAO().queryLookupPath(
				lookupPathId, environmentId);
	}

	/**
	 * Returns a list of query beans representing the Groups that the specified
	 * User belongs to and which are associated with the specified Environment.
	 * This is used, for example, to identify the Groups a User can choose from
	 * at login. The list is sorted by Group name.
	 * 
	 * @param userId
	 * @param environmentId
	 * @return a List of GroupQueryBean
	 * @throws DAOException
	 */
	static public List<GroupQueryBean> queryGroups(String userId,
			Integer environmentId) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getGroupDAO().queryGroups(
				userId, environmentId);
	}

	/**
	 * This method returns a sorted list of query beans representing the
	 * Environments accessible by the user's login Group. If the user is a
	 * System Administrator it returns all Environments.
	 * 
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @param onlyWithAdminRights
	 *            : returns environment on which user has environment admin
	 *            rights if true.
	 * @return A list of EnvironmentQueryBean objects.
	 * @throws DAOException
	 */
	static public List<EnvironmentQueryBean> queryEnvironmentsForLoggedInUser(
			SortType sortType, boolean onlyWithAdminRights) throws DAOException {
		List<EnvironmentQueryBean> result = new ArrayList<EnvironmentQueryBean>();

		if (SAFRApplication.getUserSession().isSystemAdministrator()) {
			result = DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.queryAllEnvironments(sortType);
		} else {
			result = DAOFactoryHolder
					.getDAOFactory()
					.getEnvironmentDAO()
					.queryEnvironmentsForGroup(
							sortType,
							SAFRApplication.getUserSession().getGroup().getId(),
							onlyWithAdminRights);
		}

		return result;
	}

	/***
	 * This method is to query all the environments associated with the user's
	 * current login Group on which the group has permissions to perform Batch
	 * Activate Lookup Paths.
	 * 
	 * @param sortType
	 *            the type of sorting(name or id).
	 * @return
	 * @throws DAOException
	 */
	static public List<EnvironmentQueryBean> queryEnvironmentsForBAL(
			SortType sortType) throws DAOException {

		if (SAFRApplication.getUserSession().getUser().isSystemAdmin()) {
			return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
					.queryAllEnvironments(sortType);
		} else {
			return  DAOFactoryHolder.getDAOFactory()
			    .getEnvironmentDAO().queryBALEnvironments(sortType);			
		}
	}
	
    /***
     * Returns all lookup available for a particular environment
     * @param environmentId
     * @param sortType
     * @return
     * @throws DAOException
     */
	
	static public List<LookupQueryBean> queryLookupsForBAL(Integer environmentId,
        SortType sortType) throws DAOException {
	    if (SAFRApplication.getUserSession().isAdminOrMigrateInUser(environmentId)) {
	        return DAOFactoryHolder.getDAOFactory().getLookupDAO().queryAllLookups(
	            environmentId, sortType);
	    }
	    else {
	        return DAOFactoryHolder.getDAOFactory().getLookupDAO().queryLookupsForBAL(
	            environmentId, sortType);
	    }
	}
	

	/***
	 * This method is to query all the environments that contain a component
	 * 
	 * @param id
	 *          id of the component
	 *        type
	 *          type of the component
	 * @return
	 * @throws DAOException
	 */
	static public List<EnvComponentQueryBean> queryComponentsInEnvironments(Integer id,
			ComponentType type) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO().queryComponentEnvironments(id, type);
	}
	
	/**
	 * Returns a list of query beans representing the View Folders from the
	 * specified Environment that a user will have access to. If the user is a
	 * system administrator, this will be all View Folders in the Environment.
	 * If not, it will be the View Folders in the Environment that the specified
	 * Group has access to. This is used, for example, when setting the Default
	 * View Folder for a User.
	 * 
	 * @param environmentId
	 *            target Environment containing the required View Folders
	 * @param groupId
	 *            target Group that must have access to the View Folders, where
	 *            isSystemAdmin is false (ignored if isSystemAdmin is true)
	 * @param User
	 *            the user who is the subject of this query
	 * @param sortType
	 *            sort by ID or Name
	 * @return a list of ViewFolderQueryBean
	 * @throws DAOException
	 */
	static public List<ViewFolderQueryBean> queryViewFolders(
			Integer environmentId, Integer groupId, User user, SortType sortType)
			throws DAOException {
		boolean isSystemAdmin = user.isSystemAdmin();
		return DAOFactoryHolder.getDAOFactory().getViewFolderDAO()
				.queryViewFolders(environmentId, groupId, isSystemAdmin,
						sortType);
	}

	static public List<LogicalRecordFieldQueryBean> queryAllLogicalRecordFields(
			Integer environmentId, SortType sortType) throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLRFieldDAO()
				.queryAllLogicalRecordFields(sortType, environmentId);
	}

	static public List<LogicalRecordFieldQueryBean> queryAllLogicalRecordFields(
			Integer environmentId, Integer logicalRecordId, SortType sortType)
			throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLRFieldDAO()
				.queryAllLogicalRecordFields(sortType, environmentId,
						logicalRecordId);
	}
	
	static public List<LogicalRecordFieldQueryBean> queryLRFields(
			Integer environmentId, SortType sortType)
			throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLRFieldDAO().queryLRFields(
				sortType, environmentId);
	}
	
	static public List<LRIndexQueryBean> queryLRIndexes(Integer environmentId)
			throws DAOException {
		return DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.queryLRIndexes(environmentId);
	}
	
    public static List<EnvironmentQueryBean> queryEnvironmentsForBAV(SortType sortByName) throws DAOException {

        if (SAFRApplication.getUserSession().getUser().isSystemAdmin()) {
            return DAOFactoryHolder.getDAOFactory().getEnvironmentDAO()
                    .queryAllEnvironments(sortByName);
        } else {
            return  DAOFactoryHolder.getDAOFactory()
                .getEnvironmentDAO().queryBAVEnvironments(sortByName);            
        }
    }


}