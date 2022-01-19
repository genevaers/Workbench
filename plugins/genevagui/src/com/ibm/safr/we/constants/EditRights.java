package com.ibm.safr.we.constants;

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


public enum EditRights {

	Read(1,"Read"), ReadModify(2,"Modify"), ReadModifyDelete(3,"Full"), None(4,"None");

    private int code;
	private String desc;
	
	private EditRights(int code, String desc) {
	    this.code = code;
	    this.desc = desc;
	}
	
	/**
	 * Helper method to convert an edit rights integer value from the database
	 * into its equivalent EditRights enum. Returns null if the integer value is
	 * not recognized.
	 */
	public static EditRights intToEnum(int editRightsInt) {
		if (editRightsInt == 1) {
			return EditRights.Read;
		} else if (editRightsInt == 2) {
			return EditRights.ReadModify;
		} else if (editRightsInt == 3) {
			return EditRights.ReadModifyDelete;
        } else if (editRightsInt == 4) {
            return EditRights.None;
		} else {
			return null;
		}
	}

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    
}
