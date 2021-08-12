package com.ibm.safr.we.data.transfer;

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

public class ViewLogicDependencyTransfer extends
		SAFREnvironmentalComponentTransfer {

	private Integer viewId; // VIEWID
	private LogicTextType logicTextType; // PARENTTYPECD
	private Integer parentId; // PARENTID
	private Integer sequenceNo; // DEPENDID
	private Integer lookupPathId; // LOOKUPID
	private Integer lrFieldId; // LRFIELDID
	private Integer userExitRoutineId; // EXITID
	private Integer fileAssociationId; // LFPFASSOCID

	public ViewLogicDependencyTransfer() {
		super();
	}

	public Integer getViewId() {
		return viewId;
	}

	public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public LogicTextType getLogicTextType() {
		return logicTextType;
	}

	public void setLogicTextType(LogicTextType logicTextType) {
		this.logicTextType = logicTextType;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public Integer getLookupPathId() {
		return lookupPathId;
	}

	public void setLookupPathId(Integer lookupPathId) {
		this.lookupPathId = lookupPathId;
	}

	public Integer getLrFieldId() {
		return lrFieldId;
	}

	public void setLrFieldId(Integer lrFieldId) {
		this.lrFieldId = lrFieldId;
	}

	public Integer getUserExitRoutineId() {
		return userExitRoutineId;
	}

	public void setUserExitRoutineId(Integer userExitRoutineId) {
		this.userExitRoutineId = userExitRoutineId;
	}

	public Integer getFileAssociationId() {
		return fileAssociationId;
	}

	public void setFileAssociationId(Integer fileAssociationId) {
		this.fileAssociationId = fileAssociationId;
	}

}
