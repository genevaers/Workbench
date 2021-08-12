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


import java.util.Date;

/**
 * An abstract class providing a common type reference for all data transfer
 * objects.
 */
public abstract class SAFRTransfer {

	// indicates whether the object is already persistent
	private boolean persistent=true;

	// common fields set by the user
	private String comments;

	// common history fields set by the application, not by the user
	private Date createTime;
	private String createBy;
	private Date modifyTime;
	private String modifyBy;

	//indicates whether the object is used for Import.
	private boolean forImport=false; 
	
	//CQ9682 indicates the object is to be migrated
	private boolean forMigration = false;

	public Boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(Boolean persistent) {
		this.persistent = persistent;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getModifyBy() {
		return modifyBy;
	}

	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}

	public boolean isForImport() {
		return forImport;
	}

	public void setForImport(boolean forImport) {
		this.forImport = forImport;
	}

	public boolean isForMigration() {
		return forMigration;
	}

	public void setForMigration(boolean forMigration) {
		this.forMigration = forMigration;
	}
	
	public boolean isForImportOrMigration() {
		return isForImport() || isForMigration();
	}

}
