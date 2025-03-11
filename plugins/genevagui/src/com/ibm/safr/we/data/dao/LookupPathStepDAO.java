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


import java.util.List;

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;

public interface LookupPathStepDAO {

	/**
	 * This method is to retrieve all the Steps from LOOKUPSTEP belonging
	 * to a particular environment and optionally from a particular lookup path.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the Lookup Path belongs.
	 * @param lookupPathId
	 *            : The Id of the Lookup Path for which steps are to be
	 *            retrieved or '0' (zero) if all the steps are to be retrieved.
	 * @return A List of LookupPathStepTransfer objects.
	 * @throws DAOException
	 */
	List<LookupPathStepTransfer> getAllLookUpPathSteps(Integer environmentId,
			Integer lookupPathId) throws DAOException;

	/**
	 * Find Target LF with multiple PF's
	 * @param environmentId
	 * @param lrlfId
	 * @return
	 * @throws DAOException
	 */
    DependentComponentTransfer getAssociatedLFDependency(
        Integer environmentId, Integer lrlfId) throws DAOException;
	
	/**
	 * This method is to retrieve a single step of a Lookup Path
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the Lookup Path belongs
	 * @param lookupPathStepId
	 *            : The Id of the LookupPath Step.
	 * @return A LookupPathStepTransfer object.
	 * @throws DAOException
	 */
	LookupPathStepTransfer getLookUpPathStep(Integer environmentId,
			Integer lookupPathStepId) throws DAOException;

	/**
	 * This method is to store a Lookup Path Step in LOOKUPSTEP. This
	 * later call methods to create or update a Lookup Path Step.
	 * 
	 * @param lkupPathStepTransfer
	 *            : The transfer object which has the values for the columns of
	 *            the Lookup Path Step which is being stored.
	 * @return A LookupPathStepTransfer object.
	 * @throws DAOException
	 */
	LookupPathStepTransfer persistLookupPathStep(
			LookupPathStepTransfer lkupPathStepTransfer) throws DAOException;

	/**
	 * This method is to remove a Lookup Path Step from LOOKUPSTEP.
	 * 
	 * @param lkupPathStepId
	 *            : The Id of the Lookup Path Step which is to be removed.
	 * @param environmentId
	 *            : The Id of the environment to which the Lookup Path Step
	 *            belongs.
	 * @throws DAOException
	 */
	void removeLookupPathStep(Integer lkupPathStepId, Integer environmentId)
			throws DAOException;

	/**
	 * This method is to retrieve the source fields from LOOKUPSRCKEY
	 * which belong to a particular environment and optionally a particular step
	 * of Lookup Path.
	 * 
	 * @param environmentId
	 *            : The Id of the environment to which the source field belongs.
	 * @param lookupStepId
	 *            : The Id of the Lookup Path Step to which the source fields
	 *            belong or '0' (zero) if all source fields are to be retrieved.
	 * @return A List of LookupPathSourceFieldTransfer objects.
	 * @throws DAOException
	 */
	List<LookupPathSourceFieldTransfer> getLookUpPathStepSourceFields(
			Integer environmentId, Integer lookupStepId) throws DAOException;

    /**
     * This function is called from the respective model class. This first
     * removes all the source fields belonging to the listed steps and then
     * then create the source fields in LOOKUPSRCKEY.
     * 
     */
    void persistLookupPathStepsSourceFields(
            List<Integer> lookupStepIds,
            List<LookupPathSourceFieldTransfer> sourceFieldsTrans)
            throws DAOException;
	
	/**
	 * This method is to remove a Lookup Path Step Source Fields from
	 * LOOKUPSRCKEY.
	 * 
	 */
	void removeLookupPathStepSourceField(Integer lkupPathStepId,
			Integer environmentId) throws DAOException;
	
	/**
	 * Get the target LRid for a lookup
	 * @param environmentId
	 * @param lookupPathName
	 * @return LRID
	 * @throws DAOException 
	 */
    int getTargetLookUpPathLrId(Integer environmentId, String lookupPathName) throws DAOException; 

}
