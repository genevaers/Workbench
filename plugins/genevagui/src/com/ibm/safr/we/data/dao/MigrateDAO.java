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


import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;

/**
 * *This is an interface for all the methods related to migrating a metadata
 * component. All these unimplemented methods are implemented in DB2MigrateDAO.
 * 
 */
public interface MigrateDAO {

	/**
	 * This method is to log a single migration in the MIGAUDIT
	 * 
	 * @param compType
	 *            : The type of component.
	 * @param environmentId
	 *            : The Id of the source environment
	 * @param componentId
	 *            : The Id of the component.
	 * @return A boolean which is true if the any view is affected or false
	 *         otherwise.
	 * @throws DAOException
	 */
	void logMigration(
			Integer srcEnv,
			Integer trgEnv,
			ComponentType compType,
			Integer componentId,
			String componentName
	) throws DAOException;
	
}
