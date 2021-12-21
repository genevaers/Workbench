package com.ibm.safr.we.model.view;

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


import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

/**
 * This class represents a single component dependency within a body of logic
 * text. That is, a reference within the logic text to a lookup path, an LR
 * field, a user exit routine or a LF-PF file association. This dependency
 * information includes the View, the type of logic text, the ID of the parent
 * component that owns the logic text, the sequence number of the dependencies
 * within the logic text and the ID of the dependent component.
 * <p>
 * There are 4 types of logic text that are used in the following order:
 * <ol>
 * <li>Extract Record Filter (parent is ViewSource)
 * <li>Extract Column Assignment (parent is ViewColumnSource)
 * <li>Format Record Filter (parent is View)
 * <li>Format Column Calculation (parent is ViewColumn)
 * </ol>
 * There is no transfer object constructor for this class as the model layer
 * never needs to instantiate it from the database. It just stores the
 * dependencies returned by the View compiler.
 * 
 */
public class ViewLogicDependency extends SAFREnvironmentalComponent {

	/*
	 * Note, this class has no ID or name attributes, so those fields in the
	 * superclass hierarchy will have null values.
	 */

	private View view; // VIEWID
	private LogicTextType logicTextType; // LOGICTYPECD
	SAFRComponent parentComponent;
	private Integer sequenceNo; // DEPENDID
	private Integer lookupPathId; // LOOKUPID
	private Integer lrFieldId; // LRFIELDID
	private Integer userExitRoutineId; // EXITID
	private Integer fileAssociationId; // LFPFASSOCID

	ViewLogicDependency(View view, LogicTextType logicTextType,
			SAFRComponent parentComponent, Integer sequenceNo,
			Integer lookupPathId, Integer lrFieldId, Integer userExitRoutineId,
			Integer fileAssociationId) {
		super(view.getEnvironmentId());
		this.view = view;
		this.logicTextType = logicTextType;
		this.parentComponent = parentComponent;
		this.sequenceNo = sequenceNo;
		this.lookupPathId = lookupPathId;
		this.lrFieldId = lrFieldId;
		this.userExitRoutineId = userExitRoutineId;
		this.fileAssociationId = fileAssociationId;
	}
	
	ViewLogicDependency(View parentView, ViewLogicDependencyTransfer trans) throws DAOException {
		super(trans);
		this.view = parentView;
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		ViewLogicDependencyTransfer trans = (ViewLogicDependencyTransfer) safrTrans;
		this.logicTextType = trans.getLogicTextType();
		this.sequenceNo = trans.getSequenceNo();
		this.lookupPathId = trans.getLookupPathId();
		this.lrFieldId = trans.getLrFieldId();
		this.userExitRoutineId = trans.getUserExitRoutineId();
		this.fileAssociationId = trans.getFileAssociationId();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		ViewLogicDependencyTransfer trans = (ViewLogicDependencyTransfer) safrTrans;
		trans.setViewId(view.getId());
		trans.setLogicTextType(logicTextType);
		trans.setParentId(parentComponent.getId());
		trans.setSequenceNo(sequenceNo);
		trans.setLookupPathId(lookupPathId);
		trans.setLrFieldId(lrFieldId);
		trans.setUserExitRoutineId(userExitRoutineId);
		trans.setFileAssociationId(fileAssociationId);
	}

	/**
	 * @return the logicTextType
	 */
	public LogicTextType getLogicTextType() {
		return logicTextType;
	}

	/**
	 * @param logicTextType
	 *            the logicTextType to set
	 */
	public void setLogicTextType(LogicTextType logicTextType) {
		this.logicTextType = logicTextType;
		markModified();
	}

	/**
	 * @return the sequenceNo
	 */
	public Integer getSequenceNo() {
		return sequenceNo;
	}

	/**
	 * @param sequenceNo
	 *            the sequenceNo to set
	 */
	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
		markModified();
	}

	/**
	 * @return the lookupPathId
	 */
	public Integer getLookupPathId() {
		return lookupPathId;
	}

	/**
	 * @param lookupPathId
	 *            the lookupPathId to set
	 */
	public void setLookupPathId(Integer lookupPathId) {
		this.lookupPathId = lookupPathId;
		markModified();
	}

	/**
	 * @return the lrFieldId
	 */
	public Integer getLrFieldId() {
		return lrFieldId;
	}

	/**
	 * @param lrFieldId
	 *            the lrFieldId to set
	 */
	public void setLrFieldId(Integer lrFieldId) {
		this.lrFieldId = lrFieldId;
		markModified();
	}

	/**
	 * @return the userExitRoutineId
	 */
	public Integer getUserExitRoutineId() {
		return userExitRoutineId;
	}

	/**
	 * @param userExitRoutineId
	 *            the userExitRoutineId to set
	 */
	public void setUserExitRoutineId(Integer userExitRoutineId) {
		this.userExitRoutineId = userExitRoutineId;
		markModified();
	}

	/**
	 * @return the fileAssociationId
	 */
	public Integer getFileAssociationId() {
		return fileAssociationId;
	}

	/**
	 * @param fileAssociationId
	 *            the fileAssociationId to set
	 */
	public void setFileAssociationId(Integer fileAssociationId) {
		this.fileAssociationId = fileAssociationId;
		markModified();
	}

	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// ViewLogicDependencyTransfer trans = new
		// ViewLogicDependencyTransfer();
		// setTransferData(trans);
		// DAOFactoryHolder.getDAOFactory().getViewLogicDependencyDAO()
		// .persistViewLogicDependency(trans);
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		// TODO Auto-generated method stub
		return null;
	}

}
