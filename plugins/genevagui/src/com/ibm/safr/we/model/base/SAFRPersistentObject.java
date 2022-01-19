package com.ibm.safr.we.model.base;

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


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.utilities.ConfirmWarningStrategy;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * An abstract supertype for all SAFR objects that are stored persistently in
 * the SAFR database. It defines the characteristics that are common to all
 * persistent SAFR objects.
 */
abstract public class SAFRPersistentObject extends SAFRObject {

	// Indicates the persistence status of this object
	private SAFRPersistence persistence;

	// Persistent state
	private String comment;
	private Date createTime;
	private String createBy;
	private Date modifyTime;
	private String modifyBy;

	private static final String CREATED_BY = "CB";
	private static final String CREATED_TIME = "CT";
	private static final String MODIFY_BY = "MB";
	private static final String MODIFY_TIME = "MT";
	private static final String PERSISTENCE = "P";

	// map to cache properties.
	private Map<String, Object> undoMap = new HashMap<String, Object>();

	// indicates where this object is for import.
	private boolean forImport;
	
	private ConfirmWarningStrategy confirmWarningStrategy = null;

	/**
	 * The default ctor is called by subclasses to instantiate a new SAFR object
	 * which may be persisted to the database. It will initialize the state of
	 * this object to NEW and its member variable to nulls.
	 */
	protected SAFRPersistentObject() {
		super();
		this.persistence = SAFRPersistence.NEW;
		this.comment = null;
		this.createTime = null;
		this.createBy = null;
		this.modifyTime = null;
		this.modifyBy = null;
	}

	/**
	 * This ctor is called by subclasses to instantiate an existing object from
	 * the SAFR database. The object's state is obtained from the specified
	 * transfer object
	 * 
	 * @param trans
	 *            the SAFRTransfer object
	 */
	protected SAFRPersistentObject(SAFRTransfer trans) {
		super();
		setObjectData(trans);
	}

	/**
	 * A protected helper method for use by subclasses to set this object's
	 * state from its transfer object.
	 * 
	 * @param trans
	 *            the SAFRTransfer object
	 * @see doSetData
	 */
	protected void setObjectData(SAFRTransfer trans) {
		// before setting the properties, cache the old values so that we can
		// undo
		addToUndoMap(CREATED_BY, createBy);
		addToUndoMap(CREATED_TIME, createTime);
		addToUndoMap(MODIFY_BY, modifyBy);
		addToUndoMap(MODIFY_TIME, modifyTime);
		addToUndoMap(PERSISTENCE, persistence);

		this.comment = trans.getComments();
		this.createTime = trans.getCreateTime();
		this.createBy = trans.getCreateBy();
		this.modifyTime = trans.getModifyTime();
		this.modifyBy = trans.getModifyBy();

		this.persistence = trans.isPersistent() ? SAFRPersistence.OLD
				: SAFRPersistence.NEW;
		this.forImport = trans.isForImport();
	}

	/**
	 * A protected helper method for use by subclasses to set the transfer
	 * object from this object's state.
	 * 
	 * @param trans
	 *            the SAFRTransfer object
	 * @see doSetData
	 */
	protected void setTransferData(SAFRTransfer trans) {

		trans.setPersistent(persistence == SAFRPersistence.NEW ? false : true);
		trans.setForImport(forImport);
		trans.setComments(comment);

		// the history fields are set in the data layer for inserts or updates
		// so just set them to null here.
		// correction: set these from transfer too, needed for import to work
		// properly.
		trans.setCreateTime(createTime);
		trans.setCreateBy(createBy);
		trans.setModifyTime(modifyTime);
		trans.setModifyBy(modifyBy);
	}

	/**
	 * Indicates the persistence status of this object (NEW, OLD, MODIFIED or
	 * DELETED).
	 * 
	 * @return a SAFRPersistence enum value
	 */
	public SAFRPersistence getPersistence() {
		return persistence;
	}

	// JAK: needed by associations - to be reviewed later.
	public void setPersistence(SAFRPersistence persistence) {
		this.persistence = persistence;
	}

	/**
	 * Marks this object as modified from its persistent state so that the data
	 * layer can determine if an existing object is to be udpated. It has no
	 * effect on a new object (one that is not yet persistent) or a deleted
	 * object (one that has been marked as deleted). This method should be
	 * called by all setter methods in this class or any subclasses which change
	 * the persistent attributes of the object.
	 * 
	 */
	public void markModified() {
		if (persistence == SAFRPersistence.OLD && !isForImport()) {
			this.persistence = SAFRPersistence.MODIFIED;
		}
	}

	/**
	 * Marks this object as deleted, indicating that its persistent state is to
	 * be erased. This method has no effect on a new object (one that is not yet
	 * persistent).
	 * 
	 */
	public void markDeleted() {
		if (persistence != SAFRPersistence.NEW) {
			this.persistence = SAFRPersistence.DELETED;
		}
	}

	/**
	 * Get the comment describing this component.
	 * 
	 * @return the comment string
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Set the comment describing this component to the specified String.
	 * 
	 * @param comment
	 *            a comment string
	 */
	public void setComment(String comment) {
		this.comment = comment;
		markModified();
	}

	/**
	 * This method is used to get the creation timestamp of a component.
	 * 
	 * @return the timestamp of creation.
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * 
	 * @return
	 */
    public String getCreateTimeString() {
        return UIUtilities.formatDate(createTime);
    }
	
	/**
	 * This method is used to get the id of the user who creates the component.
	 * 
	 * @return the id of the User.
	 */
	public String getCreateBy() {
		return createBy;
	}

	/**
	 * This method is used to get the timestamp at which the component was last
	 * modified.
	 * 
	 * @return the timestamp at which the component was modified.
	 */
	public Date getModifyTime() {
		return modifyTime;
	}
	
    public String getModifyTimeString() {
        return UIUtilities.formatDate(modifyTime);
    }
	
	/**
	 * This method is used to get the Id of the user who last modified the
	 * component.
	 * 
	 * @return the id of the user.
	 */
	public String getModifyBy() {
		return modifyBy;
	}

	protected void addToUndoMap(String property, Object value) {
		undoMap.put(property, value);
	}

	protected Object getFromUndoMap(String property) {
		return undoMap.get(property);
	}

	public void undo() {
		createBy = (String) undoMap.get(CREATED_BY);
		createTime = (Date) undoMap.get(CREATED_TIME);
		modifyBy = (String) undoMap.get(MODIFY_BY);
		modifyTime = (Date) undoMap.get(MODIFY_TIME);
		persistence = (SAFRPersistence) undoMap.get(PERSISTENCE);
	}

	/**
	 * Concrete subclasses must implement this abstract method with the
	 * behaviour necessary to persist the object's state.
	 * 
	 * @throws SAFRException
	 *             For errors persisting the object's state. May wrap other
	 *             checked exceptions like database errors.
	 */
	abstract public void store() throws SAFRException, DAOException;

	/**
	 * Specifies if this component is to be imported.
	 * 
	 * @return true if this component is to be imported.
	 */
	public boolean isForImport() {
		return forImport;
	}
	
	/**
	 * @return true if the object has been stored, otherwise false
	 */
	public Boolean isPersistent() {
		if (SAFRPersistence.NEW.equals(this.persistence)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @return warning strategy
	 */
    public ConfirmWarningStrategy getConfirmWarningStrategy() {
        return confirmWarningStrategy;
    }

    /**
     * Only set for objects that need a confirmation warning strategy
     * 
     * @param confirmWarningStrategy
     */
    public void setConfirmWarningStrategy(ConfirmWarningStrategy confirmWarningStrategy) {
        this.confirmWarningStrategy = confirmWarningStrategy;
    }

}
