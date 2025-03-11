package com.ibm.safr.we.data;

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


/**
 * This class is a DAO Unit of Work coordinator. It provides an abstract,
 * technology-independent interface to delimit the beginning and end of a set of
 * related persistency changes that comprise a single logical unit of work. It
 * will typically be used when multiple DAO objects are called in sequence to
 * persist some composite object.
 * <p>
 * Callers simply specify when a logical unit of work begins and when it either
 * ends or fails. They do not require any knowledge of the underlying
 * transaction control technology.
 * <p>
 * Concrete subclasses will provide an implementation of these 3 behaviours
 * (begin, end, fail) which is specific to the underlying technology.
 */
public abstract class DAOUOW {

	private boolean inProgress = false;
	private boolean multiComponentScope = false;

	/**
	 * Begin a logical unit of work. For example, if using a database, this
	 * typically means begin a database transaction. After this method is
	 * invoked, the inProgress() method will return true. If the UOW is being
	 * controlled by a meta data importer, calls to this method will be ignored.
	 * 
	 * @throws DAOException
	 */
	final public void begin() throws DAOException {
		if (multiComponentScope) {
			// UOW is not under control of single component so ignore this request
			return;
		}
		doBegin();
		this.inProgress = true;
	}

	abstract protected void doBegin() throws DAOException;

	/**
	 * Terminate a logical unit of work which has completed successfully. For
	 * example, if using a database, this typically means commit a database
	 * transaction. After this method is invoked, the inProgress() method will
	 * return false. If the UOW is being controlled by a meta data importer,
	 * calls to this method will be ignored.
	 * 
	 * @throws DAOException
	 */
	final public void end() throws DAOException {
		if (multiComponentScope) {
			// UOW is not under the control of a single component so ignore this request
			return;
		}
		this.inProgress = false;
		doEnd();
	}

	abstract protected void doEnd() throws DAOException;

	/**
	 * Terminate a logical unit of work which did not complete successfully. For
	 * example, if using a database, this typically means rollback a database
	 * transaction. After this method is invoked, the inProgress() method will
	 * return false.
	 * 
	 * @throws DAOException
	 */
	final public void fail() throws DAOException {
		this.inProgress = false;
		doFail();
	}

	abstract protected void doFail() throws DAOException;

	/**
	 * Indicates whether a unit of work is in-progress. A UOW is in-progress if
	 * the application has called 'begin' but not yet called 'end' or 'fail'.
	 * <p>
	 * 
	 * @return boolean true if UOW is in-progress, otherwise false
	 */
	public boolean inProgress() {
		return inProgress;
	}

	/**
	 * Resets the in-progress status of the unit of work. The inProgress()
	 * method will then return false. It will typically be called if an
	 * in-progress unit of work terminates unexpectedly. This method does NOT
	 * invoke any end() or fail() behaviour.
	 */
	public void stopProgress() {
		this.inProgress = false;
	}

	/**
	 * Indicates whether the UOW is scoped to a single component and its
	 * subcomponents or to multiple components. If the UOW was started by a
	 * metadata component the UOW is scoped to that component. If the UOW was
	 * started by a utility like Import or Migration, the scope includes all
	 * components stored by the utility action.
	 * <p>
	 * If the scope is multi-component, any calls to the begin() or end()
	 * methods will be ignored. Calls to fail() are not affected by this - any
	 * object can call fail() at any time.
	 * 
	 * @return true if the scope of the UOW includes multiple components.
	 */
	public boolean isMultiComponentScope() {
		return multiComponentScope;
	}
	
	/**
	 * Sets the scope of the UOW to include multiple components. If multiple
	 * component database changes are to be included in one UOW, call this
	 * method immediately after calling the begin() method.
	 */
	public void multiComponentScopeOn() {
		this.multiComponentScope = true;
	}

	/**
	 * Sets the scope of the UOW to a single component. If a UOW has been set to
	 * multiple component scope, call this method immediately before calling the
	 * end() or fail() methods to terminate the UOW.
	 */
	public void multiComponentScopeOff() {
		this.multiComponentScope = false;
	}
	
}
