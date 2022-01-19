package com.ibm.safr.we.model.query;

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

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.ModelUtilities;

public abstract class SAFRQueryBean implements Comparable<SAFRQueryBean> {

    private String name;
	private Date createTime;
	private String createBy;
	private Date modifyTime;
	private String modifyBy;
	private ComponentType componentType;

	/**
	 * Parameterized constructor to initialize values.
	 */
	public SAFRQueryBean(String name, Date createTime, String createBy,
			Date modifyTime, String modifyBy) {
		this.name = name;
		this.createTime = createTime;
		this.createBy = createBy;
		this.modifyTime = modifyTime;
		this.modifyBy = modifyBy;
		this.componentType = calcComponentType();
	}

	public ComponentType getComponentType() {
        return componentType;
    }

    public abstract String getIdLabel();

	public String getName() {
		return name;
	}

	public String getNameLabel() {
		return name != null ? name : "";
	}

	public Date getCreateTime() {
		return createTime;
	}

	public String getCreateTimeLabel() {
		return createTime != null ? ModelUtilities.formatDate(createTime) : "";
	}

	public String getCreateBy() {
		return createBy;
	}

	public String getCreateByLabel() {
		return createBy != null ? createBy : "";
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public String getModifyTimeLabel() {
		return modifyTime != null ? ModelUtilities.formatDate(modifyTime) : "";
	}

	public String getModifyBy() {
		return modifyBy;
	}

	public String getModifyByLabel() {
		return modifyBy != null ? modifyBy : "";
	}
	
	/**
	 * Returns a String of properties which identify the component the query
	 * bean represents. For most metadata components this is typically the
	 * concatentation of name and id.
	 */
	public abstract String getDescriptor();

    public int compareTo(SAFRQueryBean o) {
        return name.compareTo(o.name);
    }

    private ComponentType calcComponentType() {
        if (this instanceof ControlRecordQueryBean) {
            return ComponentType.ControlRecord;
        }
        else if (this instanceof EnvironmentQueryBean) {
            return ComponentType.Environment;
        }
        else if (this instanceof GroupQueryBean) {
            return ComponentType.Group;
        }
        else if (this instanceof LogicalFileQueryBean) {
            return ComponentType.LogicalFile;
        }
        else if (this instanceof LogicalRecordQueryBean) {
            return ComponentType.LogicalRecord;
        }
        else if (this instanceof LogicalRecordFieldQueryBean) {
            return ComponentType.LogicalRecordField;
        }
        else if (this instanceof LookupQueryBean) {
            return ComponentType.LookupPath;
        }
        else if (this instanceof PhysicalFileQueryBean) {
            return ComponentType.PhysicalFile;
        }
        else if (this instanceof UserExitRoutineQueryBean) {
            return ComponentType.UserExitRoutine;
        }
        else if (this instanceof UserQueryBean) {
            return ComponentType.User;
        }
        else if (this instanceof ViewFolderQueryBean) {
            return ComponentType.ViewFolder;
        }
        else if (this instanceof ViewQueryBean) {
            return ComponentType.View;
        }
        else {
            return null;
        }
    }
	
}
