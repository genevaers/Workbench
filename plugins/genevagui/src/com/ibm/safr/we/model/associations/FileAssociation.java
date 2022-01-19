package com.ibm.safr.we.model.associations;

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


import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

//JAK: Use this for LF-PF associations

public class FileAssociation extends ComponentAssociation {

	private Integer sequenceNo;

	// Use this ctor for creating a new association (e.g. LF ADD PF)
	public FileAssociation(SAFREnvironmentalComponent associatingFile,
			Integer associatedFileId, String associatedFileName,
			EditRights associatedCompRights) throws SAFRException {
		super(associatingFile, associatedFileId, associatedFileName,
				associatedCompRights);
		this.sequenceNo = 0;
	}

	// Use this ctor for instantiating an existing association
	public FileAssociation(FileAssociationTransfer trans,
			SAFREnvironmentalComponent associatingFile) {
		super(trans, associatingFile);
	}

	// Use this ctor for instantiating an existing association
	public FileAssociation(FileAssociationTransfer trans) {
		super(trans);
	}

	public Integer getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
		markModified();
	}

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);
		FileAssociationTransfer fileAssociationTrans = (FileAssociationTransfer) trans;
		this.sequenceNo = fileAssociationTrans.getSequenceNo();
	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		FileAssociationTransfer fileAssociationTrans = (FileAssociationTransfer) trans;
		fileAssociationTrans.setSequenceNo(sequenceNo);
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO
	}

}
