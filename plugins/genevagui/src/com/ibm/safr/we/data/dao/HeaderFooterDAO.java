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
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;

public interface HeaderFooterDAO {
	/**
	 * This method is responsible for storing view header/footer in database.
	 * 
	 * @param headerFooterItemTransfer
	 *            : A list of transfer objects.
	 * @param viewId
	 *            : The id of the view to which the header/footer is associated.
	 * @param environmentId
	 *            : the environment id to which header/footer belongs.
	 * @return a list of transfer objects whose value are set according to the
	 *         fields of created or updated header/footer in VIEWHEADERFOOTER
	 *         table.
	 * @throws DAOException
	 */
	List<HeaderFooterItemTransfer> persistHeaderFooter(
			List<HeaderFooterItemTransfer> headerFooterItemTransfer,
			Integer viewId, Integer environmentId) throws DAOException;

	/**
	 * This method is used to get all the Header/Footer(s) associated with the
	 * specified view if the View ID passed is greater than 0, else it will
	 * retrieve all the Header/Footer(s) in the specified environment..
	 * 
	 * @param viewId
	 *            : The id of the view for which all the associated
	 *            Header/Footer(s) are to be retrieved. If this is 0, this
	 *            method will retrieve all the Header/Footer(s) in the specified
	 *            environment.
	 * @param environmentId
	 *            : The environment id to which the view in which the
	 *            Header/Footer is present, belongs.
	 * 
	 * @return a list of transfer objects of the header/footer(s) associated
	 *         with the specified view.
	 * @throws DAOException
	 *             if a database error occurs while retrieving the
	 *             header/footer(s).
	 */
	List<HeaderFooterItemTransfer> getAllHeaderFooterItems(Integer viewId,
			Integer environmentId) throws DAOException;

	/**
	 * This method is used to delete a Header or a Footer from the
	 * VIEWHEADERFOOTER table.
	 * 
	 * @param viewId
	 *            : The Id of the view from which all the headers/footers are to
	 *            be deleted.
	 * @param environmentId
	 *            : The environment id to which the view in which the
	 *            header/footer is present, belongs.
	 * @throws DAOException
	 *             if a database error occurs while deleting the header/footer.
	 */
	void removeHeaderFooter(Integer viewId, Integer environmentId)
			throws DAOException;

}
