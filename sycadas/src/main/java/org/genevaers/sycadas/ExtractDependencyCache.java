package org.genevaers.sycadas;

import java.util.Iterator;

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


import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.genevaers.sycadas.dataprovider.SycadaDataProvider;


public class ExtractDependencyCache {

	private static Map<String, Integer> pfsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, Integer> exitsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, Integer> procsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, Integer> fieldsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String, LookupRef> lookupsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static SycadaDataProvider dataProvider;
	private static int currentLrId;

	public static void clear() {
		pfsByName.clear();
		exitsByName.clear();
		procsByName.clear();
		fieldsByName.clear();
		lookupsByName.clear();
		currentLrId = 0;
	}

	public static Integer getLookup(String name) {
		LookupRef localLkref = lookupsByName.computeIfAbsent(name, s -> getLookupRefByName(s));
		return localLkref != null ? localLkref.getId() : null;
	}

	private static LookupRef getLookupRefByName(String name) {
		LookupRef lkref = null;
		//Get all of the lookup target fields here
		Map<String, Integer> lookfieldsAndIds = dataProvider.getLookupTargetFields(name);
		if(lookfieldsAndIds.size() > 0 ) {
				lkref = new LookupRef();
				lkref.setId(lookfieldsAndIds.get("Lookup_ID"));
				lkref.setName(name);
				Iterator<Entry<String, Integer>> lkfsi = lookfieldsAndIds.entrySet().iterator();
				while(lkfsi.hasNext()) {
					Entry<String, Integer> lkf = lkfsi.next();
					if(!lkf.getKey().equals("Lookup_ID")) {
						lkref.getLookupFieldsByName().put(lkf.getKey(), lkf.getValue());
					}
				}
		}
		// System.out.println("get lkref for lookup field " + name);
		// if(lkref != null) {
		// 	System.out.println("Id " + lkref.getId());
		// 	lkref.getLookFieldsStream().forEach(f -> System.out.println(f.getKey() + " " + f.getValue() + "\n"));
		// }
		return lkref;
	}

	public static Integer getNamedField(String name) {
		return fieldsByName.computeIfAbsent(name, s -> getField(s));

	}

	private static Integer getField(String fieldName) {
		return null;
	}

	public static Integer getNamedLookupField(String lkname, String name){
		LookupRef localLkref = lookupsByName.computeIfAbsent(lkname, s -> getLookupRefByName(s));
		Integer r = localLkref.getLookupFieldsByName().computeIfAbsent(name, s -> getLkField(localLkref, s));
		return r;
	}

	private static Integer getLkField(LookupRef lkref, String fieldName) {
		return null;
	}


	//Need to manage the overlap... name and procdure to same exit
	public static Integer getWriteExit(String name) {
		return exitsByName.computeIfAbsent(name, s -> getExitID(s));
	}
	
	private static Integer getExitID(String name) {
		return dataProvider.findExitID(name, false);
	}

	public static Integer getProcedure(String name) {
		return procsByName.computeIfAbsent(name, s -> getProcedureID(s));
	}
	
	private static Integer getProcedureID(String name) {
		return dataProvider.findExitID(name, true);
	}

	public static Integer getNamedLfPfAssoc(String fullName) { 
		return pfsByName.computeIfAbsent(fullName, s -> getPfAssocID(s));
	}

	private static Integer getPfAssocID(String fullName) {
		String[] parts = fullName.split("\\.");
		return dataProvider.findPFAssocID(parts[0], parts[1]);
	}

	public static void setDataProvider(SycadaDataProvider dataFromHere) {
		if(dataProvider == null) {
			dataProvider = dataFromHere;	
		} else if(dataProvider.getEnvironmentID() != dataFromHere.getEnvironmentID() 
			|| dataProvider.getLogicalRecordID() != dataFromHere.getLogicalRecordID() ) {
			dataProvider = dataFromHere;	
			clear();
		} 
	}

	public static void setLRFieldNameIds(Map<String, Integer> fieldsFromLr) {
		fieldsByName = fieldsFromLr;
	}

	public static boolean needsFieldNames() {
		return fieldsByName.isEmpty();
	}

	public static void preloadCacheFromLR(int id) { 
		if(ExtractDependencyCache.needsFieldNames() || currentLrId != id) {
			currentLrId = id;
			ExtractDependencyCache.setLRFieldNameIds(dataProvider.getFieldsFromLr(id));
		}
	}


}
