package org.genevaers.sycadas;

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

public class DummyDataProvider implements SycadaDataProvider {

	@Override
	public Integer findLRFieldID(String fieldName) {
		if(fieldName.equals("FindMe")) {
			return 99;			
		} else {
			return null;
		}
	}

	@Override
	public Integer findLookupID(String lookupName) {
		if(lookupName.equals("FindLookup")) {
			return 123;			
		} else {
			return null;
		}
	}

	@Override
	public Integer findLookupFieldID(int lkid, String fieldName) {
		if(fieldName.equals("lkfield")) {
			return 321;			
		} else {
			return null;
		}
	}

	@Override
	public Integer findExitID(String exitName) {
		if(exitName.equals("wrexit")) {
			return 60;			
		} else {
			return null;
		}
	}

	@Override
	public Integer findPFAssocID(String lfName, String pfName) {
		if(lfName.equals("LFname") && pfName.equals("PFname")) {
			return 199;			
		} else {
			return null;
		}
	}

}
