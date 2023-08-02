package org.genevaers.sycadas;

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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.genevaers.grammar.GenevaERSBaseListener;
import org.genevaers.grammar.GenevaERSParser;
import org.genevaers.grammar.GenevaERSParser.LookupContext;
import org.genevaers.grammar.GenevaERSParser.LrFieldContext;
import org.genevaers.sycadas.dataprovider.SycadaDataProvider;


public class ExtractDependencyAnalyser extends GenevaERSBaseListener {

	private Map<String, Integer> pfsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Set<Integer> writeExits = new HashSet<>();
	private Map<String, Integer> exitsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	//Need to cater for the same name in different LRs?
	//No only the source LR is seached
	private Map<String, Integer> fieldsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, LookupRef> lookupsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private List<String> errors = new ArrayList<>();
	private boolean procedure = false;
	private SycadaType sycadaType;
	//private ExtractDependencyCache cache;

	@Override 
	public void enterColumnAssignment(GenevaERSParser.ColumnAssignmentContext ctx) { 
		if(sycadaType != SycadaType.EXTRACT_COLUMN) {
			errors.add("Column assignment cannot be used for this type of logic");	
		}
	}

	@Override 
	public void enterSelectIf(GenevaERSParser.SelectIfContext ctx) 
	{ 
		if(sycadaType != SycadaType.EXTRACT_FILTER) {
			errors.add("SELECTIF cannot be used for this type of logic");	
		}
	}

	@Override public void enterSkipIf(GenevaERSParser.SkipIfContext ctx) 
	{ 
		if(sycadaType != SycadaType.EXTRACT_FILTER) {
			errors.add("SKIPIF cannot be used for this type of logic");	
		}
	}

	@Override
	public void enterLookup(LookupContext ctx) {
		StringBuilder sb;
		if (ctx.getChildCount() > 1) {
		    sb = new StringBuilder(ctx.getChild(1).getText());
		} else {
			sb = new StringBuilder(ctx.getText());
   			sb.deleteCharAt(0);
			sb.deleteCharAt(sb.length()-1);
		}	
		String fullName = sb.toString();
		lookupsByName.computeIfAbsent(fullName, s -> getLookupID(s));
	}

	private LookupRef getLookupID(String name) {
		LookupRef lkref = null;

		//This is  wrong want to build the analysers picture from the cache
		//on an as referred too basis
		// so the dependecyA lookupsByName will grow as fields are analysed
		Integer lkid = ExtractDependencyCache.getLookup(name);
		if(lkid != null ) {
				lkref = new LookupRef();
				lkref.setId(lkid);
				lkref.setName(name);
		}
		if(lkref == null) {
			errors.add("Lookup " + name + " not found");			
		}
		return lkref;
	}

	@Override
	public void exitLrField(LrFieldContext ctx) {
		StringBuilder sb;
		if (ctx.getChildCount() > 1) {    
			sb = new StringBuilder(ctx.getChild(2).getText());
		} else {
			sb = new StringBuilder(ctx.getText()); 
		}
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		String fieldName = sb.toString();
		fieldsByName.computeIfAbsent(fieldName, s -> getField(s));

	}

	private Integer getField(String fieldName) {
		//The source LR used will be added by the data provider
		Integer fieldID = ExtractDependencyCache.getNamedField(fieldName);
		if(fieldID == null) {
			errors.add("Field " + fieldName + " not found");
		}
		return fieldID;
	}

	@Override
	public void exitLookupField(GenevaERSParser.LookupFieldContext ctx){
		StringBuilder sb = new StringBuilder(ctx.getChild(1).getText());
		String fullName = sb.toString();
		String[] parts = fullName.split("\\.");
		LookupRef lkref = lookupsByName.computeIfAbsent(parts[0], s -> getLookupID(s));
		if(lkref != null) {
			//Get the lkFields for this lookup	
			lkref.getLookupFieldsByName().computeIfAbsent(parts[1], name -> getLkField(lkref, parts[1]));
		} else {
			errors.add("Lookup " + parts[0] + " not found");			
		}
	}

	private Integer getLkField(LookupRef lkref, String fieldName) {
		Integer lkfield = ExtractDependencyCache.getNamedLookupField(lkref.getName(), fieldName);
		if(lkfield == null) {
			errors.add("Lookup " + lkref.getName() + " does not reference field " + fieldName);								
		}
		return lkfield;
	}

	@Override 
	public void enterProcedure(GenevaERSParser.ProcedureContext ctx) { 
		if(ctx.getChild(0).getText().equalsIgnoreCase("USEREXIT")) {
			procedure = false;
		} else {
			procedure = true;
		}
	}

	@Override
	public void exitWriteExit(GenevaERSParser.WriteExitContext ctx) {
		StringBuilder sb = new StringBuilder(ctx.getText());
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		Integer exitId = null;
		String name = sb.toString();
		if(procedure) {
			exitId = ExtractDependencyCache.getProcedure(name);
		} else {
			exitId = ExtractDependencyCache.getWriteExit(name);
		}
		if(exitId != null) {
			writeExits.add(exitId);
		} else {
			if(procedure) {
				errors.add("Procedure " + name + " not found");						
			} else {
				errors.add("Write Exit " + name + " not found");						
			}
		}
	}
	
	// An issue we have here is that the same exit can be referred to 
	// via its name and its procedure... avoid the duplicates
	@Override 
	public void exitFile(GenevaERSParser.FileContext ctx) { 
		StringBuilder sb = new StringBuilder(ctx.getText());
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		String fullName = sb.toString();
		pfsByName.computeIfAbsent(fullName, s -> getPfAssocID(s));
	}

	private Integer getPfAssocID(String fullName) {
		Integer pfID = ExtractDependencyCache.getNamedLfPfAssoc(fullName);
		if(pfID == null) {
			errors.add("Output File " + fullName + " not found");						
		}
		return pfID;
	}

	public void setDataProvider(SycadaDataProvider dataFromHere) {
		ExtractDependencyCache.setDataProvider(dataFromHere);
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	public Stream<Integer> getFieldIDs() {
		return fieldsByName.values().stream();
	}


	public Map<Integer, List<Integer>> getLookupIDs() {
		return null;
	}

	public  Stream<LookupRef> getLookupsStream() {
		return lookupsByName.values().stream();
	}

	public Set<Integer> getExitIDs() {		
		return writeExits;
	}

	public Stream<Integer> getLFPFAssocIDs() {
		return pfsByName.values().stream();
	}

	public void setSycadaType(SycadaType type) {
		sycadaType = type;
	}

	public String getDependenciesAsString() {
		StringBuilder sb = new StringBuilder("Dependencies");
		sb.append("\n============");
		sb.append("\n\nFields");
		sb.append("\n------");
		fieldsByName.entrySet().stream().forEach(e -> sb.append(getEntryString(e)));
		sb.append("\n\nLookups");
		sb.append("\n-------\n");
		if(lookupsByName.size() > 0) {
			lookupsByName.values().stream().forEach(lkref -> sb.append(lkref.toString()));
		} else {
			sb.append("None");
		}
		sb.append("\n\nLFPF Assocs");
		sb.append("\n-----------");
		pfsByName.entrySet().stream().forEach(e -> sb.append("\n" + e.getKey() + "[" + e.getValue() +"]"));
		sb.append("\n\nExits");
		sb.append("\n-----");
		return sb.toString();
	}

	private String getEntryString(Entry<String, Integer> e) {
		return "\n" + e.getKey() + "[" + e.getValue().toString() + "]";
	}

	// public void useCache(ExtractDependencyCache edc) {
	// 	cache = edc;
	// }

	//This can be called once the data provider is set
	public void preloadCacheFromLR(int id) { 
		ExtractDependencyCache.preloadCacheFromLR(id);
	}

	//Call this as soon as we get an Lookup name
	public void preloadCacheFromLookup(String name){

	}

	public void clear() {
		pfsByName.clear();
		fieldsByName.clear();
		lookupsByName.clear();
		writeExits.clear();
	}

}
