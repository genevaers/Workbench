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


import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.view.View;

/**
 * This abstract class represents all SAFR metadata components. The common
 * characteristics of these components which differentiate them from other
 * persistent SAFR objects is that they have a numeric ID and a string name.
 */

abstract public class SAFRComponent extends SAFRPersistentObject {

	protected Integer id; // Should be private. Pending clone behaviour in
    // factory.
	
	private String name;
	
	private ComponentType componentType;

    private static final String ID = "ID";

	/**
	 * The default ctor is called by subclasses to instantiate a new SAFR
	 * component which may be persisted to the database. Its ID is initialized
	 * to zero (indicating it is not yet persistent) and its name is initialized
	 * to null. A unique ID value will be generated the first time the object is
	 * persisted.
	 */
	protected SAFRComponent() {
		super();
		this.id = new Integer(0);
		this.name = null;
		this.componentType = calcComponentType();
	}

	/**
	 * This ctor is called by subclasses to instantiate an existing SAFR
	 * component from the database. The object's state is obtained from the
	 * specified component transfer object.
	 * 
	 * @param trans
	 *            the SAFRComponentTransfer object. If the ID in the
	 *            SAFRComponentTransfer object i.e, the ID of the component is
	 *            null then it throws a Null pointer Exception.
	 */
	protected SAFRComponent(SAFRComponentTransfer trans) {
		super(trans);
		if (trans.getId() == null) {
			throw new NullPointerException("Component ID is null in database");
		}
        this.componentType = calcComponentType();
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		// before setting the properties, cache the old values so that we can
		// undo
		addToUndoMap(ID, id);

		SAFRComponentTransfer trans = (SAFRComponentTransfer) safrTrans;
		this.id = trans.getId();
		this.name = trans.getName();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		SAFRComponentTransfer trans = (SAFRComponentTransfer) safrTrans;
		trans.setId(id);
		trans.setName(name);
	}

	/**
	 * @return the id of the SAFR component.
	 */
	public Integer getId() {
		return id;
	}

    public void setId(Integer id) {
        this.id = id;
    }

	/**
	 * @return the name of the SAFR component.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this component to the specified String.
	 * 
	 * @param name
	 *            the component name
	 * @see #validate() validate
	 */
	public void setName(String name) {
		this.name = name;
		markModified();
	}

    public ComponentType getComponentType() {
        return componentType;
    }
	
	@Override
	public void undo() {
		super.undo();
		this.id = (Integer) getFromUndoMap(ID);
	}

	/**
	 * 
	 * Concrete subclasses must implement this abstract method with the
	 * necessary behaviour to saveAs an existing component.
	 * 
	 * @param newName
	 *            newName of the component to be savedas.
	 * @return SAFRComponent
	 * @throws SAFRValidationException
	 * @throws SAFRException
	 */
	abstract public SAFRComponent saveAs(String newName)
			throws SAFRValidationException, SAFRException;

	/**
	 * This method is used to validate objects against the SAFR data rules. The
	 * default implementation provided here is empty. Subclasses should override
	 * this method if SAFR data rules apply to their objects.
	 * 
	 * @throws SAFRValidationException
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		// default is no op
	}

	/**
	 * Call this method to re-invoke validation with a token that indicates a
	 * warning has been accepted. The default implementation is empty.
	 * Subclasses should override this method if they issue warnings during
	 * validation. If they don't issue validation warnings, this method can be
	 * ignored.
	 * <p>
	 * Validation is invoked initially either by calling the the no args
	 * validation() method or by calling this method with a null token argument.
	 * Then if a warning is returned via a SAFRValidationException and it is
	 * accepted, re-invoke validation by calling this method with the token that
	 * was return in that exception.
	 * 
	 * @param token
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate(SAFRValidationToken token) throws SAFRException,
	DAOException {
		// default is no op
	}
	
	/**
	 * @return a String concatenation of component name and id in the format Name[Id].
	 */
	public String getDescriptor() {
		if (name == null && id == null) {
			return "";
		}
		String n = (name != null ? name : "");
		String i = (id != null ? id.toString() : "");
		return n + "[" + i + "]";
	}

    private ComponentType calcComponentType() {
        if (this instanceof ControlRecord) {
            return ComponentType.ControlRecord;
        }
        else if (this instanceof Environment) {
            return ComponentType.Environment;
        }
        else if (this instanceof Group) {
            return ComponentType.Group;
        }
        else if (this instanceof LogicalFile) {
            return ComponentType.LogicalFile;
        }
        else if (this instanceof LogicalRecord) {
            return ComponentType.LogicalRecord;
        }
        else if (this instanceof LRField) {
            return ComponentType.LogicalRecordField;
        }
        else if (this instanceof LookupPath) {
            return ComponentType.LookupPath;
        }
        else if (this instanceof PhysicalFile) {
            return ComponentType.PhysicalFile;
        }
        else if (this instanceof UserExitRoutine) {
            return ComponentType.UserExitRoutine;
        }
        else if (this instanceof ViewFolder) {
            return ComponentType.ViewFolder;
        }
        else if (this instanceof View) {
            return ComponentType.View;
        }
        else {
            return null;
        }
    }
	
}
