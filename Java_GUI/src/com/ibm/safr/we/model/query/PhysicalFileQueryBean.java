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

import com.ibm.safr.we.constants.EditRights;

/**
 * Query information for a physical file. Includes just the info displayed in a
 * list of physical files and the key info needed to instantiate a physical
 * file.
 */
public class PhysicalFileQueryBean extends EnvironmentalQueryBean {

    private String fileType;
    private String diskFileType;
    private String accessMethod;
    private String inputDD;
    private String inputDSN;
    private String outputDD;
    
	/**
	 *Parameterized constructor to initialize values
	 */
	public PhysicalFileQueryBean(Integer environmentId, Integer id,
			String name, String fileType, String diskFileType, String accessMethod, String inputDD, String inputDSN, String outputDD, 
			EditRights rights, Date createTime, String createBy, Date modifyTime, String modifyBy) {
		super(environmentId, id, name, rights, createTime,
				createBy, modifyTime, modifyBy);
		this.fileType = fileType;
        this.diskFileType = diskFileType;
		this.accessMethod = accessMethod;
		this.inputDD = inputDD;
		this.inputDSN = inputDSN;
		this.outputDD = outputDD;
	}

    public String getFileType() {
        return fileType;
    }

    public String getDiskFileType() {
        return diskFileType;
    }
    
    public String getAccessMethod() {
        return accessMethod;
    }

    public String getInputDD() {
        return inputDD;
    }

    public String getInputDSN() {
        return inputDSN;
    }

    public String getOutputDD() {
        return outputDD;
    }

}
