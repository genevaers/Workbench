package org.genevaers.sycadas;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
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




public class DependencyEntry {

	private int sourceType;
	private int parentId;
	private Integer fieldID;
	private Integer lookupId;
	
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public Integer getFieldID() {
		return fieldID;
	}
	public void setFieldID(Integer fieldID) {
		this.fieldID = fieldID;
	}
	public Integer getLookupId() {
		return lookupId;
	}
	public void setLookupId(Integer lookupId) {
		this.lookupId = lookupId;
	}
	public Integer getLfpfAssocId() {
		return lfpfAssocId;
	}
	public void setLfpfAssocId(Integer lfpfAssocId) {
		this.lfpfAssocId = lfpfAssocId;
	}
	public Integer getExitId() {
		return exitId;
	}
	public void setExitId(Integer exitId) {
		this.exitId = exitId;
	}
	private Integer lfpfAssocId;
	private Integer exitId;

}
