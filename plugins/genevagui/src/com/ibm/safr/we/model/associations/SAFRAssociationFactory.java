package com.ibm.safr.we.model.associations;

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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupEnvironmentAssociationTransfer;
import com.ibm.safr.we.data.transfer.GroupUserAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.view.View;

//TODO JAK: work in progress

public class SAFRAssociationFactory {

	/**
	 * This method is to get the Logical File associations of a Physical File.
	 * 
	 * @param physicalFile
	 *            : The model object of the Physical File for which Logical File
	 *            associations are to be retrieved.
	 * @return A list of FileAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<FileAssociation> getPhysicalFileToLogicalFileAssociations(
			PhysicalFile physicalFile) throws DAOException, SAFRException {

		List<FileAssociationTransfer> fileAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getPhysicalFileDAO()
				.getAssociatedLogicalFiles(physicalFile.getId(),
						physicalFile.getEnvironment().getId());

		SAFRAssociationList<FileAssociation> fileAssociations = new SAFRAssociationList<FileAssociation>();
		for (FileAssociationTransfer fileAssociationtransfer : fileAssociationTransfers) {

			FileAssociation fileAssociation = new FileAssociation(
					fileAssociationtransfer, physicalFile);
			fileAssociations.add(fileAssociation);
		}

		return fileAssociations;
	}

	/**
	 * This method is to get the Physical File associations of a Logical File.
	 * 
	 * @param logicalFile
	 *            : The model object of the Logical File for which Physical File
	 *            associations are to be retrieved.
	 * @return A list of FileAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<FileAssociation> getLogicalFileToPhysicalFileAssociations(
			LogicalFile logicalFile) throws DAOException, SAFRException {

		List<FileAssociationTransfer> fileAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getLogicalFileDAO()
				.getAssociatedPhysicalFiles(logicalFile.getId(),
						logicalFile.getEnvironment().getId());
		SAFRAssociationList<FileAssociation> fileAssociations = new SAFRAssociationList<FileAssociation>();
		for (FileAssociationTransfer fileAssociationtransfer : fileAssociationTransfers) {

			FileAssociation fileAssociation = new FileAssociation(
					fileAssociationtransfer, logicalFile);
			fileAssociations.add(fileAssociation);
		}

		return fileAssociations;
	}

	/**
	 * This method is used to get the Physical File associations of a Logical
	 * File.
	 * 
	 * @param logicalFileQueryBean
	 *            The query bean object of the Logical File for which Physical
	 *            File associations are to be retrieved.
	 * @return A list of FileAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<FileAssociation> getLogicalFileToPhysicalFileAssociations(
			LogicalFileQueryBean logicalFileQueryBean) throws DAOException,
			SAFRException {

		List<FileAssociationTransfer> fileAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getLogicalFileDAO()
				.getAssociatedPhysicalFiles(logicalFileQueryBean.getId(),
						logicalFileQueryBean.getEnvironmentId());
		SAFRAssociationList<FileAssociation> fileAssociations = new SAFRAssociationList<FileAssociation>();
		for (FileAssociationTransfer fileAssociationtransfer : fileAssociationTransfers) {

			FileAssociation fileAssociation = new FileAssociation(
					fileAssociationtransfer);
			fileAssociations.add(fileAssociation);
		}

		return fileAssociations;
	}

	/**
	 * Return all LF-PF associations from the specified Environment.
	 * 
	 * @param environmentId the source of the associations
	 * @return a List of FileAssociation objects
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<FileAssociation> getLogicalFileToPhysicalFileAssociations(
			Integer environmentId) throws DAOException,
			SAFRException {

		List<FileAssociationTransfer> fileAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getLogicalFileDAO()
				.getLFPFAssociations(environmentId);
		SAFRAssociationList<FileAssociation> fileAssociations = new SAFRAssociationList<FileAssociation>();
		for (FileAssociationTransfer fileAssociationtransfer : fileAssociationTransfers) {

			FileAssociation fileAssociation = new FileAssociation(
					fileAssociationtransfer);
			fileAssociations.add(fileAssociation);
		}

		return fileAssociations;
	}
	
	static public FileAssociation getLogicalFileToPhysicalFileAssociation(
			Integer associationId, Integer environmentId) throws DAOException,
			SAFRException {
		FileAssociationTransfer trans = DAOFactoryHolder.getDAOFactory()
				.getLogicalFileDAO().getLFPFAssociation(associationId,
						environmentId);
		FileAssociation fileAssociation = new FileAssociation(trans);
		return fileAssociation;
	}
	
	static public FileAssociation getLogicalFileToPhysicalFileAssociation(
			Integer logicalFileId, Integer physicalFileId, Integer environmentId)
			throws DAOException, SAFRException {
		FileAssociationTransfer trans = DAOFactoryHolder.getDAOFactory()
				.getLogicalFileDAO().getLFPFAssociation(logicalFileId,
						physicalFileId, environmentId);
		FileAssociation fileAssociation = null;
		if (trans != null) {
			fileAssociation = new FileAssociation(trans);
		}
		return fileAssociation;
	}
	

	/**
	 * This method is to get the Logical Record associations of a Logical File.
	 * 
	 * @param logicalFile
	 *            : The model object of the Logical File for which Logical
	 *            Record associations are to be retrieved.
	 * @return A list of ComponentAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<ComponentAssociation> getLogicalFileToLogicalRecordAssociations(
			LogicalFile logicalFile) throws DAOException, SAFRException {
		List<ComponentAssociationTransfer> componentAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getLogicalFileDAO()
				.getAssociatedLogicalRecords(logicalFile.getId(),
						logicalFile.getEnvironment().getId());
		SAFRAssociationList<ComponentAssociation> componentAssociations = new SAFRAssociationList<ComponentAssociation>();
		for (ComponentAssociationTransfer componentAssociationTransfer : componentAssociationTransfers) {
			ComponentAssociation componentAssociation = new ComponentAssociation(
					componentAssociationTransfer, logicalFile);
			componentAssociations.add(componentAssociation);
		}
		return componentAssociations;
	}

	/**
	 * This method is to get the Logical File associations of a Logical Record.
	 * 
	 * @param logicalRecord
	 *            : The model object of the Logical Record for which Logical
	 *            File associations are to be retrieved.
	 * @return A list of ComponentAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<ComponentAssociation> getLogicalRecordToLogicalFileAssociations(
			LogicalRecord logicalRecord) throws DAOException, SAFRException {
		return getLogicalRecordToLogicalFileAssociations(logicalRecord.getId(),
				logicalRecord.getEnvironment().getId());

	}

	/**
	 * This method is to get the Logical file associations of a logical record.
	 * 
	 * @param logicalRecordId
	 *            : the id of the Logical Record for which Logical File
	 *            associations are to be retrieved.
	 * @param environmentId
	 *            : the id of the environment to which the Logical record nad
	 *            logical file belong.
	 * @return A list of ComponentAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<ComponentAssociation> getLogicalRecordToLogicalFileAssociations(
			Integer logicalRecordId, Integer environmentId)
			throws DAOException, SAFRException {
		List<ComponentAssociationTransfer> componentAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getLogicalRecordDAO()
				.getAssociatedLogicalFiles(logicalRecordId, environmentId);
		SAFRAssociationList<ComponentAssociation> componentAssociations = new SAFRAssociationList<ComponentAssociation>();
		for (ComponentAssociationTransfer componentAssociationTransfer : componentAssociationTransfers) {
			ComponentAssociation componentAssociation = new ComponentAssociation(
					componentAssociationTransfer);
			componentAssociations.add(componentAssociation);
		}
		return componentAssociations;
	}

	/**
	 * Returns all LRLF associations from the specified environment.
	 * 
	 * @param environmentId
	 *            : the id of the environment containing the LRLF associations.
	 * @return A list of ComponentAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<ComponentAssociation> getLogicalRecordToLogicalFileAssociations(
			Integer environmentId)
			throws SAFRException {
		List<ComponentAssociationTransfer> componentAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getLogicalRecordDAO()
				.getLRLFAssociations(environmentId);
		SAFRAssociationList<ComponentAssociation> componentAssociations = new SAFRAssociationList<ComponentAssociation>();
		for (ComponentAssociationTransfer componentAssociationTransfer : componentAssociationTransfers) {
			ComponentAssociation componentAssociation = new ComponentAssociation(
					componentAssociationTransfer);
			componentAssociations.add(componentAssociation);
		}
		return componentAssociations;
	}

	/**
	 * This method is to retrieve the Logical File association for a given
	 * Logical Record and an association Id.
	 * 
	 * @param LRLFAssociationId
	 *            : The LR-LF association Id.
	 * @param logicalRecord
	 *            : The model object of the Logical Record for which Logical
	 *            File association is to be retrieved.
	 * @return A ComponentAssociation object.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public ComponentAssociation getLogicalRecordToLogicalFileAssociation(
			Integer LRLFAssociationId, LogicalRecord logicalRecord)
			throws DAOException, SAFRException {
		ComponentAssociationTransfer componentAssociationTransfer = DAOFactoryHolder
				.getDAOFactory().getLogicalRecordDAO()
				.getTargetLogicalFileAssociation(LRLFAssociationId,
						logicalRecord.getEnvironment().getId());

		ComponentAssociation componentAssociation = new ComponentAssociation(
				componentAssociationTransfer, logicalRecord);

		return componentAssociation;
	}

	/**
	 * Return the Logical Record to Logical File association for the specified
	 * environment ID and association ID.
	 * 
	 * @param LRLFAssociationId
	 * @param environmentId
	 * @return Component Association
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public ComponentAssociation getLogicalRecordToLogicalFileAssociation(
			Integer LRLFAssociationId, Integer environmentId)
			throws DAOException, SAFRException {
		ComponentAssociationTransfer transfer = DAOFactoryHolder
				.getDAOFactory().getLogicalRecordDAO().getLRLFAssociation(
						LRLFAssociationId, environmentId);

		ComponentAssociation componentAssociation = new ComponentAssociation(
				transfer);

		return componentAssociation;
	}

	/**
	 * Return the Logical Record to Logical File association for the specified
	 * environment ID, LR ID and LF ID.
	 * 
	 * @param LRId
	 * @param LFId
	 * @param environmentId
	 * @return Component Association
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public ComponentAssociation getLogicalRecordToLogicalFileAssociation(
			Integer LRId, Integer LFId, Integer environmentId)
			throws DAOException, SAFRException {
		ComponentAssociationTransfer transfer = DAOFactoryHolder
				.getDAOFactory().getLogicalRecordDAO().getLRLFAssociation(
						LRId, LFId, environmentId);

		ComponentAssociation componentAssociation = null;
		if (transfer != null) {
	        componentAssociation = new ComponentAssociation(transfer);		    
		}
		return componentAssociation;
	}
	
	/**
	 * This method is to get the User associations of a Group.
	 * 
	 * @param group
	 *            : The model object of the Group for which User associations
	 *            are to be retrieved.
	 * @return A List of GroupUserAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<GroupUserAssociation> getGroupToUserAssociations(
			Group group) throws DAOException, SAFRException {
		List<GroupUserAssociationTransfer> groupUserAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getGroupDAO()
				.getAssociatedUsers(group.getId());
		SAFRAssociationList<GroupUserAssociation> groupUserAssociations = new SAFRAssociationList<GroupUserAssociation>();
		for (GroupUserAssociationTransfer groupUserAssociationTransfer : groupUserAssociationTransfers) {
			GroupUserAssociation groupUserAssociation = new GroupUserAssociation(
					groupUserAssociationTransfer, group);
			groupUserAssociations.add(groupUserAssociation);
		}
		return groupUserAssociations;

	}

	/**
	 * This method is to get the Environment associations of a Group.
	 * 
	 * @param group
	 *            : The model object of the Group for which Environment
	 *            associations are to be retrieved.
	 * @return A list of GroupEnvironmentAssociation objects.
	 * @throws DAOException
	 */
	static public SAFRAssociationList<GroupEnvironmentAssociation> getGroupToEnvironmentAssociations(
			Group group) throws DAOException {
		List<GroupEnvironmentAssociationTransfer> grpEnvAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getGroupDAO().getAssociatedEnvironments(
						group.getId());
		SAFRAssociationList<GroupEnvironmentAssociation> grpEnvAssociations = new SAFRAssociationList<GroupEnvironmentAssociation>();
		for (GroupEnvironmentAssociationTransfer grpEnvAssociationTransfer : grpEnvAssociationTransfers) {
			GroupEnvironmentAssociation grpEnvAssociation = new GroupEnvironmentAssociation(
					grpEnvAssociationTransfer, group);
			grpEnvAssociations.add(grpEnvAssociation);
		}
		return grpEnvAssociations;

	}

	/**
	 * This method is to get the Environment association of a Group.
	 * 
	 * @param group
	 *            : The model object of the Group for which Environment
	 *            association is to be retrieved.
	 * @param environment
	 *            : The model object of the Environment.
	 * @return A GroupEnvironmentAssociation object.
	 * @throws DAOException
	 */
	static public GroupEnvironmentAssociation getGroupToEnvironmentAssociation(
			Group group, Integer environmentId) throws DAOException {
		GroupEnvironmentAssociationTransfer grpEnvAssociationTransfer = DAOFactoryHolder
				.getDAOFactory().getGroupDAO().getAssociatedEnvironment(
						group.getId(), environmentId);
		if (grpEnvAssociationTransfer == null) {
		    return null;
		}
		else {
    		GroupEnvironmentAssociation grpEnvAssociation = new GroupEnvironmentAssociation(
    				grpEnvAssociationTransfer, group);    
    		return grpEnvAssociation;
		}
	}

	/**
	 * This method is to get the Component associations of a Group.
	 * 
	 * @param componentType
	 *            : The type of component. One of Physical File, Logical File,
	 *            Logical Record, User Exit Routine and View Folder.
	 * @param environmentId
	 *            : The Id of the environment to which components belong.
	 * @param group
	 *            : The model object of the Group for which Component
	 *            associations are to be retrieved.
	 * @return A list of GroupComponentAssociation objects.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	static public SAFRAssociationList<GroupComponentAssociation> getGroupToComponentAssociations(
			ComponentType componentType, Integer environmentId, Group group)
			throws DAOException, SAFRException {
		List<GroupComponentAssociationTransfer> grpCmpAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getGroupDAO().getComponentEditRights(
						componentType, environmentId, group.getId());

		SAFRAssociationList<GroupComponentAssociation> grpCmpAssociations = new SAFRAssociationList<GroupComponentAssociation>();
		for (GroupComponentAssociationTransfer grpCmpAssociationTransfer : grpCmpAssociationTransfers) {
			GroupComponentAssociation grpCmpAssociation = new GroupComponentAssociation(
					grpCmpAssociationTransfer, group);
			grpCmpAssociations.add(grpCmpAssociation);
		}
		return grpCmpAssociations;

	}

	/**
	 * This method is to get the Environment to Group associations for an
	 * Environment.
	 * 
	 * @param environment
	 *            : The model object of the environment for which Group
	 *            associations are to be retrieved.
	 * @param sortType
	 *            :the sort sequence (Id or Name)
	 * @return A list of GroupEnvironmentAssociation objects.In
	 *         {@link GroupEnvironmentAssociation} objects associating component
	 *         will be Group and associated component will be environment.
	 * @throws DAOException
	 */
	static public SAFRAssociationList<GroupEnvironmentAssociation> getEnvironmentToGroupAssociations(
			Environment environment, SortType sortType) throws DAOException {
		List<GroupEnvironmentAssociationTransfer> grpEnvAssociationTransfers = DAOFactoryHolder
				.getDAOFactory().getEnvironmentDAO().getAssociatedGroups(
						environment.getId(), sortType);
		SAFRAssociationList<GroupEnvironmentAssociation> grpEnvAssociations = new SAFRAssociationList<GroupEnvironmentAssociation>();

		for (GroupEnvironmentAssociationTransfer grpEnvAssociationTransfer : grpEnvAssociationTransfers) {
			GroupEnvironmentAssociation grpEnvAssociation = new GroupEnvironmentAssociation(
					grpEnvAssociationTransfer);
			grpEnvAssociations.add(grpEnvAssociation);
		}
		return grpEnvAssociations;

	}

    public static SAFRAssociationList<ViewFolderViewAssociation> getViewFolderToViewAssociations(ViewFolder viewFolder) {
        List<ViewFolderViewAssociationTransfer> transfers = DAOFactoryHolder.getDAOFactory().
            getViewFolderDAO().getVFVAssociation(viewFolder.getEnvironmentId(), viewFolder.getId());

        SAFRAssociationList<ViewFolderViewAssociation> associations = new SAFRAssociationList<ViewFolderViewAssociation>();
        for (ViewFolderViewAssociationTransfer transfer : transfers) {
            associations.add(new ViewFolderViewAssociation(transfer));
        }
    
        return associations;
    }

    public static SAFRAssociationList<ViewFolderViewAssociation> getViewFolderToViewAssociations(Integer environmentId) {
        List<ViewFolderViewAssociationTransfer> transfers = DAOFactoryHolder.getDAOFactory().
            getViewFolderDAO().getVFVAssociations(environmentId);

        SAFRAssociationList<ViewFolderViewAssociation> associations = new SAFRAssociationList<ViewFolderViewAssociation>();
        for (ViewFolderViewAssociationTransfer transfer : transfers) {
            associations.add(new ViewFolderViewAssociation(transfer));
        }
    
        return associations;
    }
    
    public static SAFRAssociationList<ViewFolderViewAssociation> getViewToViewFolderAssociations(View view, boolean admin) {
        List<ViewFolderViewAssociationTransfer> transfers = DAOFactoryHolder.getDAOFactory().
            getViewDAO().getVVFAssociation(view.getEnvironmentId(), view.getId(), admin);

        SAFRAssociationList<ViewFolderViewAssociation> associations = new SAFRAssociationList<ViewFolderViewAssociation>();
        for (ViewFolderViewAssociationTransfer transfer : transfers) {
            associations.add(new ViewFolderViewAssociation(transfer));
        }
    
        return associations;
    }

}
