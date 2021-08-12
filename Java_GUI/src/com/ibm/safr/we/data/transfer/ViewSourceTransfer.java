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



public class ViewSourceTransfer extends SAFREnvironmentalComponentTransfer {

	private Integer viewId;
	private Integer sourceSeqNo;
	private Integer lrFileAssocId;
	private String extractFilterLogic;
    private Integer extractFileAssociationId; // Extr Output File, OUTLFPFASSOCID
    private Integer writeExitId; // Extract Phase, WRITEEXITID
    private String writeExitParams; // Extract Phase, WRITEEXITPARAM
    private boolean extractOutputOverride; // EXTRACTOUTPUTIND
    private String extractRecordOutput;

    public Integer getViewId() {
		return viewId;
	}

    public void setViewId(Integer viewId) {
		this.viewId = viewId;
	}

	public Integer getSourceSeqNo() {
		return sourceSeqNo;
	}

	public void setSourceSeqNo(Integer sourceSeqNo) {
		this.sourceSeqNo = sourceSeqNo;
	}

	public Integer getLRFileAssocId() {
		return lrFileAssocId;
	}

	public void setLRFileAssocId(Integer lrFileAssocId) {
		this.lrFileAssocId = lrFileAssocId;
	}

	public String getExtractFilterLogic() {
		return extractFilterLogic;
	}

	public void setExtractFilterLogic(String extractFilterLogic) {
		this.extractFilterLogic = extractFilterLogic;
	}

    public Integer getExtractFileAssociationId() {
        return extractFileAssociationId;
    }

    public void setExtractFileAssociationId(Integer extractFileAssociationId) {
        this.extractFileAssociationId = extractFileAssociationId;
    }

    public Integer getWriteExitId() {
        return writeExitId;
    }

    public void setWriteExitId(Integer writeExitId) {
        this.writeExitId = writeExitId;
    }

    public String getWriteExitParams() {
        return writeExitParams;
    }

    public void setWriteExitParams(String writeExitParams) {
        this.writeExitParams = writeExitParams;
    }

    public boolean isExtractOutputOverride() {
        return extractOutputOverride;
    }

    public void setExtractOutputOverride(boolean extractOutputOverride) {
        this.extractOutputOverride = extractOutputOverride;
    }

    public String getExtractRecordOutput() {
        return extractRecordOutput;
    }

    public void setExtractRecordOutput(String extractRecordOutput) {
        this.extractRecordOutput = extractRecordOutput;
    }	
}
