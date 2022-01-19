package com.ibm.safr.we.data;

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


import org.genevaers.sycadas.dataprovider.SycadaDataProvider;

import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;

public class WESycadaDataProvider implements SycadaDataProvider {

	private DAOFactory DAOFact;
	private Integer environID;
	private Integer sourceLRID;

	public WESycadaDataProvider() {
		DAOFact = DAOFactoryHolder.getDAOFactory();
	}
	
	@Override
	public Integer findExitID(String exitName) {
		return DAOFact.getUserExitRoutineDAO().getUserExitRoutine(exitName, environID);
	}

	@Override
	public Integer findLRFieldID(String fieldName) {
		return DAOFact.getLRFieldDAO().getLRField(environID, sourceLRID, fieldName);
	}

	@Override
	public Integer findLookupFieldID(int lookupid, String targetFieldName) {
		// TODO Auto-generated method stub
		return DAOFact.getLRFieldDAO().getLookupLRField(environID, lookupid, targetFieldName);
	}

	@Override
	public Integer findLookupID(String name) {
		return DAOFact.getLookupDAO().getLookupPath(name, environID);
	}

	@Override
	public Integer findPFAssocID(String lfName, String pfName) {
		return DAOFact.getLogicalFileDAO().getLFPFAssocID(environID, lfName, pfName);
	}

	public void setEnvironmentID(Integer environmentId) {
		environID = environmentId;		
	}

	public void setSourceLRID(Integer id) {
		sourceLRID = id;		
	}

}
