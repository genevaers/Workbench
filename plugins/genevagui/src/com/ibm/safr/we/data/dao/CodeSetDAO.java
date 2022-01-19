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
import com.ibm.safr.we.data.transfer.CodeTransfer;

/**
 *This class is an interface for all the methods related to Code Set. All these
 * unimplemented methods are implemented in DB2CodeSetDAO.
 * 
 */
public interface CodeSetDAO {
	/**
	 * This method is used to retrieve a code set with the specified code
	 * category.
	 * 
	 * @param codeCategory
	 *            : This id the category of the code which is to be retrieved.
	 * @return a list of all the codes which have the specified code category.
	 * @throws DAOException
	 *             It throws exception if no code is found for the specified
	 *             code category.
	 */
	List<CodeTransfer> getCodeSet(String codeCategory) throws DAOException;

	/**
	 * This method is used to retrieve all the code sets from the table
	 * CODE in the database.
	 * 
	 * @return a list of Code set Transfer objects.
	 * @throws DAOException
	 */
	List<CodeTransfer> getAllCodeSets() throws DAOException;
}
