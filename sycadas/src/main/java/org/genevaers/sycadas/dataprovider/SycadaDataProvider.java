package org.genevaers.sycadas.dataprovider;

import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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


public interface SycadaDataProvider {

	public class LookupTargetField {
		public String lookupname;
		public int lookupid;
		public String fieldname;
		public int fieldid;
	}

	public Integer findExitID(String string, boolean procedure);
	public Integer findPFAssocID(String lfName, String pfName);
	public Map<String, Integer> getFieldsFromLr(int id);
	public Map<String, Integer> getLookupTargetFields(String name);

	public void setEnvironmentID(int environmentId);
	public int getEnvironmentID();

	public void setLogicalRecordID(int lrid);
	public int getLogicalRecordID();


}
