package com.ibm.safr.we.data.dao;

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


import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.query.LRIndexQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;

/**
 * This class is an interface for all the methods related to Logical Record
 * metadata component. All these unimplemented methods are implemented in
 * DB2LogicalRecordDAO.
 * 
 */
public interface LogicalRecordDAO {

	/**
	 * This function is used to retrieve a Logical Record with the specified ID.
	 * 
	 * @param id
	 *            : The ID of the Logical Record which is to be retrieved.
	 * @param environmentId
	 *            : The ID of the environment to which the Logical Record
	 *            belongs.
	 * @return Logical Record transfer objects which contains the details of all
	 *         the fields for the Logical Record retrieved from LOGREC table.
	 * @throws DAOException
	 */
    LogicalRecordTransfer getLogicalRecord(Integer id, Integer environmentId)
            throws DAOException;
    LogicalRecordTransfer getLogicalRecord(String name, Integer environmentId)
            throws DAOException;

	/**
	 * This function is used to retrieve all the Logical Records from LOGREC
	 * table with only selected columns. As the number of columns selected is
	 * less, the performance improves while populating the list of Logical
	 * Records.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical records
	 *            belongs.
	 * @param sortType
	 *            : the type of sorting(by Id or name).
	 * @return A list of all LogicalRecordBean objects.
	 * @throws DAOException
	 */
	List<LogicalRecordQueryBean> queryAllLogicalRecords(Integer environmentId,
			SortType sortType) throws DAOException;

	/**
	 * This function is called from the respective model class. This later calls
	 * respective function either to create or update a Logical Record.
	 * 
	 * @param logicalRecordTransfer
	 *            : The transfer objects whose parameters are set with the
	 *            values received from the UI.
	 * @return The transfer objects whose value are set according to the columns
	 *         of created or updated Logical Record in LOGREC table.
	 * @throws DAOException
	 *             if any SQLException occurs.
	 * @throws SAFRNotFoundException
	 *             if no rows are updated.
	 */
	LogicalRecordTransfer persistLogicalRecord(
			LogicalRecordTransfer logicalRecordTransfer) throws DAOException,
			SAFRNotFoundException;

	/**
	 * This function is used to retrieve a Logical Record with the specified
	 * name.
	 * 
	 * @param logicalRecordName
	 *            : The name of the Logical Record which is to be retrieved.
	 * @param logicalRecordId
	 *            : The ID which should not be equal to the id of the Logical
	 *            Record found.
	 * @param environmentId
	 *            : The ID of the environment to which the Logical Record
	 *            belongs.
	 * @return a Logical Record transfer objects which contains the details of
	 *         all the fields for the Logical Record retrieved from LOGREC
	 *         table for the specified name.
	 * @throws DAOException
	 */
	LogicalRecordTransfer getDuplicateLogicalRecord(String logicalRecordName,
			Integer logicalRecordId, Integer environmentId) throws DAOException;

	/**
	 * This function is used to delete an Logical Record from the LOGREC table.
	 * 
	 * @param id
	 *            : The ID of the Logical Record which is to be deleted.
	 * @param environmentId
	 *            : The ID of the environment to which the Logical Record
	 *            belongs.
	 * @throws DAOException
	 */
	void removeLogicalRecord(Integer id, Integer environmentId)
			throws DAOException;

	/**
	 * Returns the Logical Files associated with this Logical Record as list of
	 * ComponentAssociationTransfer objects. If the logged in user is a System
	 * Administrator or a User with Admin rights on the current Environment, it
	 * will return all associated Logical Files. For a general User, it will
	 * return only the associated files that the User has edit rights to.
	 * 
	 * @param id
	 *            : the ID of the Logical Record
	 * @param environmentId
	 *            : the ID of the environment to which the Logical Record
	 *            belongs
	 * @return A List of ComponentAssociationTransfer objects
	 * @throws DAOException
	 */
	List<ComponentAssociationTransfer> getAssociatedLogicalFiles(Integer id,
			Integer environmentId) throws DAOException;

	/**
	 * Returns the LR Index associated with the Logical Record if one exists.
	 * 
	 * @param lrId
	 *            the ID of the Logical Record
	 * @param environmentId
	 *            the ID of the environment to which this Logical Record belongs
	 * @return the LRIndexTransfer object
	 * @throws DAOException
	 */
	LRIndexTransfer getLRIndex(Integer lrId, Integer environmentId)
			throws DAOException;

	/**
	 * Returns the specified LR Indexes.
	 * 
	 * @param lrIndexes
	 *            a List of LRIndex IDs
	 * @param environmentId
	 *            the ID of the Environment containing the LR Indexes
	 * @return a List of LRIndexTransfer objects
	 * @throws DAOException
	 */
	List<LRIndexTransfer> getLRIndexes(List<Integer> lrIndexIds, Integer environmentId)
			throws DAOException;

	/**
	 * Returns the LR Index Fields for the specified LR Index.
	 * 
	 * @param lrIndexId
	 * 			the ID of the parent LR Index
	 * @param environid
	 * 			the ID of the Environment containing the LR Index Fields 
	 * @return a List of LRIndexFieldTransfer objects
	 * @throws DAOException
	 */
	public List<LRIndexFieldTransfer> getLRIndexFields(Integer lrIndexId, Integer environid) throws DAOException;
	
	/**
	 * Insert or update the LRIndex for this Logical Record.
	 * 
	 * @param lrIndexTransfer
	 *            the LRIndexTransfer object
	 * @return the LRIndexTransfer
	 * @throws DAOException
	 */
	LRIndexTransfer persistLRIndex(LRIndexTransfer lrIndexTransfer)
			throws DAOException;

	/**
	 * Delete the specified LRIndex and clear the foreign key reference to it
	 * in its parent Logical Record.
	 * 
	 * @param lrIndexId
	 *            the LRIndex ID
	 * @param lrId
	 *            the Logical Record ID
	 * @param environmentId
	 *            the ID of the Logical Record's Environment
	 * @throws DAOException
	 */
	void removeLRIndex(Integer lrIndexId, Integer lrId, Integer environmentId) throws DAOException;

	/**
	 * Delete the LR index for the specified LR and clear the foreign key reference to it
	 * in the LR.
	 * 
	 * @param lrId
	 *            the Logical Record ID
	 * @param environmentId
	 *            the ID of the Logical Record's Environment
	 * @throws DAOException
	 */
	void removeLRIndexForLR(Integer lrId, Integer environmentId) throws DAOException;

	/**
	 * Delete any existing LRIndexField content, then insert the new content.
	 * 
	 * @param lrIndexFieldTransfers
	 *            the List of LRIndexFieldTransfer objects
	 * @return the List of LRIndexFieldTransfer objects
	 * @throws DAOException
	 */
	void persistLRIndexFields(List<LRIndexFieldTransfer> lrIndexTransfers)
			throws DAOException;

	/**
	 * Delete the LRIndexField content for the specified LRIndex.
	 * 
	 * @param lrIndexId
	 *            the LRIndex ID
	 * @param environmentId
	 *            the ID of the LRIndex's Environment
	 * @throws DAOException
	 */
	void removeLRIndexFields(Integer lrIndexId, Integer environmentId)
			throws DAOException;

	/**
	 * Delete the LRIndexField content for the specified LR.
	 * 
	 * @param lrId
	 *            the LR ID
	 * @param environmentId
	 *            the ID of the LR's Environment
	 * @throws DAOException
	 */
	void removeLRIndexFieldsForLR(Integer lrId, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to retrieve all the possible Logical Files which can be
	 * associated with this Logical Record.
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical Record
	 *            belongs
	 * @param notInParam
	 *            : A comma delimited String which contains the IDs of Logical
	 *            Files which are already associated with the Logical Record.
	 * @return A list of LogicalFileQueryBean objects.
	 * @throws DAOException
	 */
	List<LogicalFileQueryBean> queryPossibleLFAssociations(
			Integer environmentId, List<Integer> notInParam) throws DAOException;

	/**
	 * This method is called from the respective model class. This later calls
	 * create of associations of Logical Record with Logical Files.
	 * 
	 * @param componentAssociationTransfers
	 *            : A list of association objects which are to be created in
	 *            LRLFASSOC.
	 * @param logicalRecordId
	 *            : The ID of the Logical Record.
	 * @return A list of ComponentAssociationTransfer objects which are created
	 *         in LRLFASSOC.
	 * @throws DAOException
	 */
	List<ComponentAssociationTransfer> persistAssociatedLF(
			List<ComponentAssociationTransfer> componentAssociationTransfers,
			Integer logicalRecordId) throws DAOException;

	/**
	 * This method is to delete an association in LRLFASSOC
	 * 
	 * @param environmentId
	 *            : the ID of the environment to which the Logical Record
	 *            belongs.
	 * @param inList
	 *            : A comma delimited String which contains the IDs of
	 *            associations which are to be deleted
	 * @throws DAOException
	 */
	void deleteAssociatedLF(Integer environmentId, List<Integer> inList)
			throws DAOException;

	/**
	 * This method is to check Lookup dependencies for LR Fields.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical Record belongs.
	 * @param fieldsToBeDeleted
	 *            : A comma delimited String which contains the IDs of the
	 *            fields whose dependencies are to be checked.
	 * 
	 * @return A List of Strings which contains name and id of the Lookup
	 *         dependent on the Field.
	 * @throws DAOException
	 */
	Map<Integer, List<DependentComponentTransfer>> getFieldLookupDependencies(
			Integer environmentId, List<Integer> fieldsToBeDeleted) throws DAOException;

	/**
	 * This method is to check View dependencies for LR Fields.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical Record belongs.
	 * @param fieldsToBeDeleted
	 *            : A comma delimited String which contains the IDs of the
	 *            fields whose dependencies are to be checked.
	 * @return A List of Strings which contains name and id of the View
	 *         dependent on the Field.
	 * @throws DAOException
	 */
	Map<Integer, List<DependentComponentTransfer>> getFieldViewDependencies(
			Integer environmentId, List<Integer> fieldsToBeDeleted) throws DAOException;

	/**
	 * This method checks if the LR is being used as a Source or Target of any
	 * Lookup
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical Record belongs.
	 * @param logicalRecordId
	 *            : The ID of the Logical Record.
	 * @return A List of DependentComponentTransfer objects which contains name
	 *         and id of the Lookup dependent on this Logical Record.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getLRLookupDependencies(
			Integer environmentId, Integer logicalRecordId,
			Collection<Integer> exceptionList) throws DAOException;

	/**
	 * This method checks the view dependencies of the Logical Record
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which Logical Record belongs.
	 * @param logicalRecordId
	 *            : The ID of the Logical Record.
	 * @param lookupIdList
	 *            : The list of the Lookups on which Logical Record depends.
	 *            Further on these lookups the views depends where this lookup
	 *            is used as a Source of a column or as a Sort title path of a
	 *            sort key or where this lookup is used in the logic text.
	 * @return A List of DependentComponentTransfer objects which contains name
	 *         and id of the Views dependent on this Logical Record.
	 * @throws DAOException
	 */
	List<DependentComponentTransfer> getLRViewDependencies(
			Integer environmentId, Integer logicalRecordId,
			Collection<Integer> lookupIdList, Collection<Integer> exceptionList)
			throws DAOException;

	/**
	 * This method is used to deactivate the views.
	 * 
	 * @param viewIds
	 *            : The list of Ids of the views which are to be deactivated.
	 * @param environmentId
	 *            : The Id of the environment to which these lookups belong.
	 * @throws DAOException
	 */
	Map<Integer, List<DependentComponentTransfer>> getAssociatedLFLookupDependencies(
			Integer environmentId, List<Integer> LRLFAssociationIds)
			throws DAOException;

	Map<Integer, List<DependentComponentTransfer>> getAssociatedLFViewDependencies(
			Integer environmentId, List<Integer> LRLFAssociationIds)
			throws DAOException;

	/**
	 * This method is to return a Logical Record through an LR-LF association
	 * Id.
	 * 
	 * @param LRLFAssociationId
	 *            : The Id of LR-LF association.
	 * @param environmentId
	 *            : The Id of the environment to which the association belongs.
	 * @return A LogicalRecordTransfer object.
	 * @throws DAOException
	 */
	LogicalRecordTransfer getLogicalRecordFromLRLFAssociation(
			Integer LRLFAssociationId, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to retrieve a LR-LF association object through the
	 * association Id.
	 * 
	 * @param LRLFAssociationId
	 *            : The Id of LR-LF association.
	 * @param environmentId
	 *            : The Id of the environment to which the association belongs.
	 * @return A ComponentAssociationTransfer object.
	 * @throws DAOException
	 */
	ComponentAssociationTransfer getTargetLogicalFileAssociation(
			Integer LRLFAssociationId, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to return all the Logical Records which are currently in
	 * active state. The Logical Record list is ordered by the Logical Record
	 * name.
	 * 
	 * @param environmentId
	 *            : The Id of the environment.
	 * @param sortType
	 *            : whether the sorting of the list be done on the basis of Id
	 *            or name.
	 * @return A list of LogicalRecordQueryBean objects for all active Logical
	 *         Records ordered by name.
	 * @throws DAOException
	 */
	List<LogicalRecordQueryBean> queryAllActiveLogicalRecords(
			Integer environmentId, SortType sortType) throws DAOException;

	/**
	 * Get the LR-LF association with the specified association Id. Treats the
	 * Logical Record as the associating object and the Logical File as the
	 * associated object.
	 * 
	 * @param associationId
	 *            : The LR-LF association Id.
	 * @param environmentId
	 *            : The Id of the environment.
	 * @return ComponentAssociationTransfer
	 * @throws DAOException
	 */
	ComponentAssociationTransfer getLRLFAssociation(Integer associationId,
			Integer environmentId) throws DAOException;
	
	/**
	 * Get the LR-LF association with the specified LR, LF Id. Treats the
	 * Logical Record as the associating object and the Logical File as the
	 * associated object.
	 * 
	 * @param LRId
	 *            : The LR Id.
	 * @param LFId
	 *            : The LF Id.
	 * @param environmentId
	 *            : The Id of the environment.
	 * @return ComponentAssociationTransfer
	 * @throws DAOException
	 */
	ComponentAssociationTransfer getLRLFAssociation(
			Integer LRId,
			Integer LFId,
			Integer environmentId) throws DAOException;

	/**
	 * Returns the LRLF associations in the specified Environment sorted by
	 * association ID.
	 * 
	 * @param environmentId
	 *            the environment containing the associations.
	 * @return a List of ComponentAssociation objects
	 * @throws DAOException
	 */
	List<ComponentAssociationTransfer> getLRLFAssociations(Integer environmentId)
			throws DAOException;	
	
	/**
	 * This method takes the Id of an LR Field and return the Query Bean of the
	 * Logical Record to which the Field belongs.
	 * 
	 * @param LRFieldId
	 *            : The Id of the LR Field.
	 * @return A LogicalRecordQueryBean object for Logical Record to which the
	 *         field belongs.
	 * @throws DAOException
	 */
	LogicalRecordQueryBean queryLogicalRecordByField(Integer LRFieldId,
			Integer environmentId) throws DAOException;
	
	/**
	 * Returns all LRIndexes in the specified environment sorted by ID.
	 * 
	 * @param environmentId the Environment containing the LRIndexes
	 * @return a List of LRIndexQueryBean objects
	 * @throws DAOException
	 */
	List<LRIndexQueryBean> queryLRIndexes(Integer environmentId) throws DAOException;

	/**
	 * 
	 * @param lRID
	 * @param environmentId
	 * @return
	 */
    LogicalRecordQueryBean queryLogicalRecord(Integer lRID, Integer environmentId);
    
    Integer getNextKey();

	
}
