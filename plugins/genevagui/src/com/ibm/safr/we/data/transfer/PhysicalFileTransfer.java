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



/**
 * Data transfer object for PhysicalFile.
 * 
 */
public class PhysicalFileTransfer extends SAFREnvironmentalComponentTransfer {

	// Common fields
	private String fileTypeCode;
    private String diskFileTypeCode;
	private String accessMethodCode;
	private Integer readExitId;
	private String readExitParams;

	// SQL fields
    private String subSystem;
    private String sqlStatement;
	private String tableName;
	private String rowFormatCode;
	private boolean includeNullIndicators;

	// Dataset fields
    private String inputDDName;
	private String datasetName;
	private int minRecordLen;
	private int maxRecordLen;
	private String outputDDName;
	private String recfm;
	private int lrecl;

	public String getSubSystem() {
        return subSystem;
    }

    public void setSubSystem(String subSystem) {
        this.subSystem = subSystem;
    }

    public int getMinRecordLen() {
        return minRecordLen;
    }

    public void setMinRecordLen(int minRecordLen) {
        this.minRecordLen = minRecordLen;
    }

    public int getMaxRecordLen() {
        return maxRecordLen;
    }

    public void setMaxRecordLen(int maxRecordLen) {
        this.maxRecordLen = maxRecordLen;
    }

    public String getRecfm() {
        return recfm;
    }

    public void setRecfm(String recfm) {
        this.recfm = recfm;
    }

    public int getLrecl() {
        return lrecl;
    }

    public void setLrecl(int lrecl) {
        this.lrecl = lrecl;
    }

    public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getInputDDName() {
		return inputDDName;
	}

	public void setInputDDName(String inputDDName) {
		this.inputDDName = inputDDName;
	}

	public String getOutputDDName() {
		return outputDDName;
	}

	public void setOutputDDName(String outputDDName) {
		this.outputDDName = outputDDName;
	}

	public Integer getPartitionId() {
		return getId();
	}

	public void setPartitionId(Integer partitionId) {
		setId(partitionId);
	}

	public String getPartitionName() {
		return getName();
	}

	public void setPartitionName(String partitionName) {
		setName(partitionName);
	}

	public String getFileTypeCode() {
		return fileTypeCode;
	}

	public void setFileTypeCode(String fileTypeCode) {
		this.fileTypeCode = fileTypeCode;
	}

    public String getDiskFileTypeCode() {
        return diskFileTypeCode;
    }
	
    public void setDiskFileTypeCode(String diskFileTypeCode) {
        this.diskFileTypeCode = diskFileTypeCode;
    }
    
	public String getAccessMethodCode() {
		return accessMethodCode;
	}

	public void setAccessMethodCode(String accessMethodCode) {
		this.accessMethodCode = accessMethodCode;
	}

	public Integer getReadExitId() {
		return readExitId;
	}

	public void setReadExitId(Integer readExitId) {
		this.readExitId = readExitId;
	}

	public String getReadExitParams() {
		return readExitParams;
	}

	public void setReadExitParams(String readExitParams) {
		this.readExitParams = readExitParams;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getRowFormatCode() {
		return rowFormatCode;
	}

	public void setRowFormatCode(String rowFormatCode) {
		this.rowFormatCode = rowFormatCode;
	}

	public boolean isIncludeNullIndicators() {
		return includeNullIndicators;
	}

	public void setIncludeNullIndicators(boolean includeNullIndicators) {
		this.includeNullIndicators = includeNullIndicators;
	}

	public String getSqlStatement() {
		return sqlStatement;
	}

	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

}
