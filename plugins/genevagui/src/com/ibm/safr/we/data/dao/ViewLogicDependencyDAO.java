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
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;

public interface ViewLogicDependencyDAO {

	/**
	 * This method is called from the respective model class to persist that
	 * objects state in the database.
	 * 
	 * @param viewLogicDepTfrList
	 *            : The List of transfer object whose attributes are set with
	 *            the values received from the model object.
	 * @throws DAOException
	 */
	public void persistViewLogicDependencies(
			List<ViewLogicDependencyTransfer> viewLogicDepTfrList,
			Integer viewId, Integer environmentId) throws DAOException;
	
	public List<ViewLogicDependencyTransfer> getViewDependecies(Integer viewId,
			Integer environmentId) throws DAOException;	

    public List<ViewLogicDependencyTransfer> getViewSourceFilterDependencies(Integer environmentId, 
        Integer viewId, Integer viewSourceId) throws DAOException; 
	
    public List<ViewLogicDependencyTransfer> getViewSourceOutputDependencies(Integer environmentId, 
        Integer viewId, Integer viewSourceId) throws DAOException; 
    
    public List<ViewLogicDependencyTransfer> getViewColumnSourceDependencies(
        Integer environmentId, Integer viewId, Integer viewColumnSourceId);
    
}
