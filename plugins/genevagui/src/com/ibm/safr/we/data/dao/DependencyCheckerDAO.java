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
import java.util.Map;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.DependencyUsageType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;

/**
 *This is an interface for all the methods related to Dependency Checker
 * utility. All these unimplemented methods are implemented in
 * DB2DependencyCheckerDAO.
 * 
 */
public interface DependencyCheckerDAO {

	/**
	 * This method is to get all the dependent metadata components for a given
	 * metadata component.
	 * 
	 * @param compType
	 *            : The type of metadata component.
	 * @param componentId
	 *            : The Id of the metadata component.
	 * @param environmentId
	 *            : The Id of the environment to which the component belongs.
	 * @return A Map with the details of the type of dependent components.
	 * @throws DAOException
	 */
	public Map<ComponentType, Map<DependencyUsageType, List<DependentComponentTransfer>>> getDependentComponents(
			ComponentType compType, Integer componentId, Integer environmentId, boolean directDepsOnly)
			throws DAOException;

}
