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


import com.ibm.safr.we.data.DAOException;

/**
 * This is an interface for getting information about the SAFR
 * stored procedures. All these unimplemented methods are implemented in
 * DB2StoredProcedureDAO.
 * 
 */
public interface StoredProcedureDAO {

    /**
     * This method is used to get the version of SAFR stored procedures
     * installed in the metadata database
     * 
     * @return A string representing the stored procedure version of the form
     *         SD<version>.<major>.<minor>.<build>. e.g. SD4.13.002.73
     * @throws DAOException
     *             It throws an exception for database errors accessing the version.
     */
    public String getVersion() throws DAOException;
}
