package com.ibm.safr.we.internal.data;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.sql.Connection;
import java.sql.SQLException;

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOW;
import com.ibm.safr.we.data.DataUtilities;

/**
 * A DB2 specific implementation of DAOUOW, based on the singleton pattern.
 * 
 */
public class DB2DAOUOW extends DAOUOW {

	// Package private ctor to restrict instantiation to dao factory.
	DB2DAOUOW() {
	}

	protected void doBegin() throws DAOException {
		// JDBC begin transaction...
		Connection con = DAOFactoryHolder.getDAOFactory().getConnection();
		try {
			con.setAutoCommit(false);
		} catch (SQLException e) {
			DataUtilities.createDAOException(
					"A database error occurred while begining a transaction.",
					e);
		}
	}

	protected void doEnd() throws DAOException {
		// JDBC commit transaction...
		Connection con = DAOFactoryHolder.getDAOFactory().getConnection();
		try {
			con.commit();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			DataUtilities
					.createDAOException(
							"A database error occurred while committing the transaction.",
							e);
		}
	}

	protected void doFail() throws DAOException {
		// JDBC rollback transaction...
		Connection con = DAOFactoryHolder.getDAOFactory().getConnection();
		try {
			con.rollback();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			DataUtilities
					.createDAOException(
							"A database error occurred while performing rollback on the transaction.",
							e);
		}
	}

}
