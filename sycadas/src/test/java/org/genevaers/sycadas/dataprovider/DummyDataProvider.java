package org.genevaers.sycadas.dataprovider;

import java.util.Map;
import java.util.TreeMap;

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


public class DummyDataProvider implements SycadaDataProvider {

	private int environmentId;
	private int logicalRecordId;

	@Override
	public Integer findExitID(String exitName, boolean procedure) {
		if (procedure) {
			if (exitName.equalsIgnoreCase("wrproc")) {
				return 33;
			} else {
				return null;
			}
		} else {
			if (exitName.equalsIgnoreCase("wrexit")) {
				return 60;
			} else {
				return null;
			}
		}
	}

	@Override
	public Integer findPFAssocID(String lfName, String pfName) {
		if(lfName.equalsIgnoreCase("LFname") && pfName.equals("PFname")) {
			return 199;			
		} else {
			return null;
		}
	}

	@Override
	public Map<String, Integer> getLookupTargetFields(String name) {
		// From here we can return the data to build the LKRef
		//can't really return the LK Ref directly?
		//Can't really get a Query been from the other side? Or can we - no this code should not be dependent on Provider
		// Want return in simple Java util form
		// Map<String, Integer> - have the lookup name as an entry?
		// Can a field name look like a lookupname? prepend with something like LK_ just too make sure?
		//
		// Then we can read the map and populate the cache
		Map<String, Integer> retval = new TreeMap<>();
		if(name.equalsIgnoreCase("FindLookup")) {
			retval.put("Lookup_ID", 123);
			retval.put("lkfield", 321);
		}  else if(name.equalsIgnoreCase("FDWF_EC959_Mrkt_Unit_Eff_DT_Lkup_From_LB587")){
			retval.put("Lookup_ID", 333);
			retval.put("MRKT_UNIT_BUSN_ID", 777);
		}
		return retval;
	}

	@Override
	public Map<String, Integer> getFieldsFromLr(int id) {
		Map<String, Integer> retval = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		retval.put("FindMe", 99);
		retval.put("Another", 25);
		return retval;
	}

	@Override
	public void setEnvironmentID(int environmentId) {
		this.environmentId = environmentId;
	}

	@Override
	public int getEnvironmentID() {
		return environmentId;
	}

	@Override
	public void setLogicalRecordID(int lrid) {
		logicalRecordId = lrid;
	}

	@Override
	public int getLogicalRecordID() {
		return logicalRecordId;
	}

}
