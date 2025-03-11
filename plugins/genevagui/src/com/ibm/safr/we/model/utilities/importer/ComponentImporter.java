package com.ibm.safr.we.model.utilities.importer;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.dao.NextKeyDAO;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathStepTransfer;
import com.ibm.safr.we.data.transfer.LookupPathTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LRIndexQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.ConfirmWarningStrategy;
import com.ibm.safr.we.model.utilities.NameConfirmWarningStrategy;


public abstract class ComponentImporter {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.utilities.importer.ComponentImporter");
    
	private ImportUtility importUtility;
	protected Map<Class<? extends SAFRTransfer>, Map<Integer, SAFRTransfer>> records = new HashMap<Class<? extends SAFRTransfer>, Map<Integer, SAFRTransfer>>();

	// Records existing components with IDs matching the import data
	protected Map<Class<? extends SAFRTransfer>, List<Integer>> duplicateIdMap = new HashMap<Class<? extends SAFRTransfer>, List<Integer>>();

	// Contains the next sequential IDs by component type
	protected Map<Class<? extends SAFRTransfer>, Integer> nextKeyIdMap = new HashMap<Class<? extends SAFRTransfer>, Integer>();
	
	// Used to store query beans representing components that exist in
	// the target environment, if needed for further checks.
	protected Map<Class<? extends EnvironmentalQueryBean>, Map<Integer, EnvironmentalQueryBean>> existingComponents = new HashMap<Class<? extends EnvironmentalQueryBean>, Map<Integer, EnvironmentalQueryBean>>();

	// Used to store associations that exist in
	// the target environment, if needed for further checks.
	protected Map<Class<? extends ComponentAssociation>, Map<Integer, ComponentAssociation>> existingAssociations = new HashMap<Class<? extends ComponentAssociation>, Map<Integer, ComponentAssociation>>();
	
	// Used to store transfer objects representing components that exist in
	// the target environment, if needed for further checks.
	protected Map<Class<? extends SAFREnvironmentalComponentTransfer>, Map<Integer, SAFREnvironmentalComponentTransfer>> existingComponentTransfers = new HashMap<Class<? extends SAFREnvironmentalComponentTransfer>, Map<Integer, SAFREnvironmentalComponentTransfer>>();

	/**
	 * Keep track of decisions made so that subsequent XML import files do not raise the same user questions 
	 */
	protected Map<Class<? extends SAFRTransfer>, Map<String, DependentComponentNode>> decidedComponents = new HashMap<Class<? extends SAFRTransfer>, Map<String, DependentComponentNode>>();
	
	//What we all want the good root
	protected DependentComponentNode dcnRoot = null;
	
	//Map per component type of the name clashes already decided
	//Use the DuplicateNameComponent to track old and new names
	protected Map<Class<? extends SAFRTransfer>, List<DependentComponentNode>> duplicateComponentsMap = null;
	
	
	public ComponentImporter(ImportUtility importUtility) {
		this.importUtility = importUtility;
		
		clearMaps();
		
        NextKeyDAO nextKeyDao = DAOFactoryHolder.getDAOFactory().getNextKeyDAO();
        nextKeyIdMap = nextKeyDao.getNextKeyIds();
        for(Entry<Class<? extends SAFRTransfer>, Integer> e :  nextKeyIdMap.entrySet())
        {
        	System.out.println("Key " + e.getKey().toString() + " Value " + e.getValue());
        }
	}
	
	public final void doImportMetadata() throws SAFRException,
			XPathExpressionException {
		
		// Do common import logic here.
		// Call this method for type-specific import logic
		doImport();
	}

	public void clearMaps() {
		records.clear();
		duplicateIdMap.clear();
		existingComponents.clear();
		existingAssociations.clear();
		existingComponentTransfers.clear();
	}

	abstract protected void doImport() throws SAFRException,
			XPathExpressionException;

	// helper getter methods for ImportUtility data

	protected Integer getTargetEnvironmentId() {
		return importUtility.getTargetEnvironment().getId();
	}

	protected ComponentType getComponentType() {
		return importUtility.getComponentType();
	}

	protected List<ImportFile> getFiles() {
		return importUtility.getFiles();
	}

	protected ConfirmWarningStrategy getConfirmWarningStrategy() {
		return importUtility.getConfirmWarningStrategy();
	}

	protected XPath getXPath() {
		return importUtility.getXPath();
	}

	protected Document getDocument() {
		return importUtility.getDocument();
	}

	protected ImportFile getCurrentFile() {
		return importUtility.getCurrentFile();
	}

	
	/**
	 * Check that the component type indicated by the name of the document
	 * element matches the component type specified for this import. For
	 * example, if the import component type is Physical File, the document
	 * element name should be in the form PhysicalFile-nnn, where nnn is the
	 * component ID, so this method will check that the text to the left of the
	 * hyphen is actually 'PhysicalFile'.
	 * 
	 * @param expectedText
	 *            string representing the text expected in the document element
	 *            name
	 * @throws SAFRValidationException
	 *             if the actual text does not match the expected text
	 */
	protected void checkDocumentElement(String expectedText)
			throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		Element rootElement = getDocument().getDocumentElement();
		String rootName = rootElement.getTagName();
		String actualText = null;
		int hyphenIndex = rootName.indexOf('-');
		if (hyphenIndex > -1) {
			actualText = rootName.substring(0, hyphenIndex);
		}
		if (!expectedText.equals(actualText)) {
			sve.setErrorMessage(getCurrentFile().getName(),
					"The document element is <" + rootName + "> "
							+ "but it should be of the form <" + expectedText
							+ "-[id]>.");
			throw sve;
		}
	}

	/**
	 * Checks if any imported components already exist in the target environment
	 * by looking for duplicate IDs. If any found, the existing components will
	 * be updated with the imported details subject to confirmation of this
	 * warning via the ConfirmWarningStrategy call back object.
	 * 
	 * @throws SAFRException
	 */
	protected void checkDuplicateIds() throws SAFRException {

		switch (getComponentType()) {
		case ViewFolder:
            checkDuplicateIds(ViewFolderTransfer.class, SAFRQuery.queryAllViewFolders(
                getTargetEnvironmentId(), SortType.SORT_BY_ID));
		    
            List<ViewFolderViewAssociation> vfvAssocs = SAFRAssociationFactory.getViewFolderToViewAssociations(getTargetEnvironmentId());
            storeExistingAssociations(ViewFolderViewAssociation.class, vfvAssocs);
            checkDuplicateAssociationIds(ViewFolderViewAssociationTransfer.class,vfvAssocs);
		    
		case View:
			checkDuplicateIds(ViewTransfer.class, SAFRQuery.queryAllViews(
					getTargetEnvironmentId(), SortType.SORT_BY_ID));
			checkDuplicateIds(ControlRecordTransfer.class, SAFRQuery
					.queryAllControlRecords(getTargetEnvironmentId(),
							SortType.SORT_BY_ID));

			// check if imported view sources duplicate existing view sources
			List<ViewSourceTransfer> existingViewSources = DAOFactoryHolder
					.getDAOFactory().getViewSourceDAO().getViewSources(0,
							getTargetEnvironmentId());
			storeExistingComponentTransfers(ViewSourceTransfer.class,
					existingViewSources);
			checkDuplicateTransferIds(ViewSourceTransfer.class,
					existingViewSources, duplicateIdMap);

			// check if imported view column sources duplicate existing view
			// column sources
			List<ViewColumnSourceTransfer> existingViewColumnSources = DAOFactoryHolder
					.getDAOFactory().getViewColumnSourceDAO()
					.getViewColumnSources(0, getTargetEnvironmentId());
			storeExistingComponentTransfers(ViewColumnSourceTransfer.class,
					existingViewColumnSources);
			checkDuplicateTransferIds(ViewColumnSourceTransfer.class,
					existingViewColumnSources, duplicateIdMap);

			// check if imported view columns duplicate existing view
			// columns
			List<ViewColumnTransfer> existingViewColumns = DAOFactoryHolder
					.getDAOFactory().getViewColumnDAO().getViewColumns(0,
							getTargetEnvironmentId());
			storeExistingComponentTransfers(ViewColumnTransfer.class,
					existingViewColumns);
			checkDuplicateTransferIds(ViewColumnTransfer.class,
					existingViewColumns, duplicateIdMap);

			// check if imported view sort keys duplicate existing view
			// sort keys
			List<ViewSortKeyTransfer> existingViewSortKeys = DAOFactoryHolder
					.getDAOFactory().getViewSortKeyDAO().getViewSortKeys(0,
							getTargetEnvironmentId());
			storeExistingComponentTransfers(ViewSortKeyTransfer.class,
					existingViewSortKeys);
			checkDuplicateTransferIds(ViewSortKeyTransfer.class,
					existingViewSortKeys, duplicateIdMap);

			// check if imported view header-footer duplicate existing view
			// header-footer
			List<HeaderFooterItemTransfer> existingViewHeaderFooters = DAOFactoryHolder
					.getDAOFactory().getHeaderFooterDAO()
					.getAllHeaderFooterItems(0, getTargetEnvironmentId());
			storeExistingComponentTransfers(HeaderFooterItemTransfer.class,
					existingViewHeaderFooters);
			checkDuplicateTransferIds(HeaderFooterItemTransfer.class,
					existingViewHeaderFooters, duplicateIdMap);

			// No break, continue.
		case LookupPath:
			checkDuplicateIds(LookupPathTransfer.class, SAFRQuery
					.queryAllLookups(getTargetEnvironmentId(),
							SortType.SORT_BY_ID));

			// check duplicate sub components

			// Steps
			List<LookupPathStepTransfer> existingSteps = DAOFactoryHolder
					.getDAOFactory().getLookupPathStepDAO()
					.getAllLookUpPathSteps(getTargetEnvironmentId(), 0);
			storeExistingComponentTransfers(LookupPathStepTransfer.class,
					existingSteps);
			checkDuplicateTransferIds(LookupPathStepTransfer.class,
					existingSteps, duplicateIdMap);

			// Source fields not needed right now, uncomment below code if
			// needed.
			List<LookupPathSourceFieldTransfer> existingSourceFields = DAOFactoryHolder
					.getDAOFactory().getLookupPathStepDAO()
					.getLookUpPathStepSourceFields(getTargetEnvironmentId(), 0);
			storeExistingComponentTransfers(
					LookupPathSourceFieldTransfer.class, existingSourceFields);
			checkDuplicateTransferIds(LookupPathSourceFieldTransfer.class,
					existingSourceFields, duplicateIdMap);
			// No break, continue.
		case LogicalRecord:
			checkDuplicateIds(LogicalRecordTransfer.class, SAFRQuery
					.queryAllLogicalRecords(getTargetEnvironmentId(),
							SortType.SORT_BY_ID));

			// check if imported LRFields duplicate existing LRFields
			List<LogicalRecordFieldQueryBean> existingLRFields = SAFRQuery
					.queryLRFields(getTargetEnvironmentId(),
							SortType.SORT_BY_ID);
			storeExistingComponents(LogicalRecordFieldQueryBean.class,
					existingLRFields);
			checkDuplicateIds(LRFieldTransfer.class, existingLRFields);

			// check if imported LRIndexes duplicate existing LRIndexes
			List<LRIndexQueryBean> existingLRIndexes = SAFRQuery
					.queryLRIndexes(getTargetEnvironmentId());
			storeExistingComponents(LRIndexQueryBean.class, existingLRIndexes);
			checkDuplicateIds(LRIndexTransfer.class, existingLRIndexes);

			// check if imported LRLF assocs duplicate existing assocs
			List<ComponentAssociation> lrlfAssocs = SAFRAssociationFactory
					.getLogicalRecordToLogicalFileAssociations(getTargetEnvironmentId());
			storeExistingAssociations(ComponentAssociation.class, lrlfAssocs);
			checkDuplicateAssociationIds(ComponentAssociationTransfer.class,
					lrlfAssocs);

			// No break, continue.
		case LogicalFile:
			checkDuplicateIds(LogicalFileTransfer.class, SAFRQuery
					.queryAllLogicalFiles(getTargetEnvironmentId(),
							SortType.SORT_BY_ID));

			// check if imported LFPF assocs duplicate existing assocs
			List<FileAssociation> lfpfAssocs = SAFRAssociationFactory
					.getLogicalFileToPhysicalFileAssociations(getTargetEnvironmentId());
			storeExistingAssociations(FileAssociation.class, lfpfAssocs);
			checkDuplicateAssociationIds(FileAssociationTransfer.class,
					lfpfAssocs);

			// No break, continue.
		default:
			// PhysicalFile
			checkDuplicateIds(PhysicalFileTransfer.class, SAFRQuery
					.queryAllPhysicalFiles(getTargetEnvironmentId(),
							SortType.SORT_BY_ID));

			checkDuplicateIds(UserExitRoutineTransfer.class, SAFRQuery
					.queryAllUserExitRoutines(getTargetEnvironmentId(),
							SortType.SORT_BY_ID));
		}
	}

	private void checkDuplicateIds(Class<? extends SAFRTransfer> transferClass,
			List<? extends EnvironmentalQueryBean> existingQueryBeans) {

		// existingComponents.
		List<Integer> existingIds = new ArrayList<Integer>();
		for (EnvironmentalQueryBean qb : existingQueryBeans) {
			existingIds.add(qb.getId());
		}
		List<Integer> duplicateIds = new ArrayList<Integer>();
		if (records.containsKey(transferClass)) {
			for (Integer id : records.get(transferClass).keySet()) {
				if (existingIds.contains(id)) {
					duplicateIds.add(id);
					// set the persistent boolean in transfer to true so that
					// DATA layer can update it
					records.get(transferClass).get(id).setPersistent(true);
				}
			}
		}
		if (duplicateIds.size() > 0) {
			duplicateIdMap.put(transferClass, duplicateIds);
		}
	}

	private void checkDuplicateAssociationIds(
			Class<? extends ComponentAssociationTransfer> transferClass,
			List<? extends ComponentAssociation> existingAssociations) {
		List<Integer> existingIds = new ArrayList<Integer>();
		for (ComponentAssociation assoc : existingAssociations) {
			existingIds.add(assoc.getAssociationId());
		}
		List<Integer> duplicateIds = new ArrayList<Integer>();
		if (records.containsKey(transferClass)) {
			for (Integer id : records.get(transferClass).keySet()) {
				if (existingIds.contains(id)) {
					duplicateIds.add(id);
					// set the persistent boolean in transfer to true so that
					// DATA layer can update it
					records.get(transferClass).get(id).setPersistent(true);
				}
			}
		}
		if (duplicateIds.size() > 0) {
			duplicateIdMap.put(transferClass, duplicateIds);
		}
	}

	protected void checkDuplicateTransferIds(
			Class<? extends SAFRTransfer> transferClass,
			List<? extends SAFREnvironmentalComponentTransfer> existingTrans,
			Map<Class<? extends SAFRTransfer>, List<Integer>> duplicateIdMap) {

		List<Integer> existingIds = new ArrayList<Integer>();
		for (SAFREnvironmentalComponentTransfer trans : existingTrans) {
			existingIds.add(trans.getId());
		}
		List<Integer> duplicateIds = new ArrayList<Integer>();
		if (records.containsKey(transferClass)) {
			for (Integer id : records.get(transferClass).keySet()) {
				if (existingIds.contains(id)) {
					duplicateIds.add(id);
					// set the persistent boolean in transfer to true so that
					// DATA layer can update it
					records.get(transferClass).get(id).setPersistent(true);
				}
			}
		}
		if (duplicateIds.size() > 0) {
			duplicateIdMap.put(transferClass, duplicateIds);
		}
	}
	
	protected List<String> prepareDuplicateIdsMessage(
			Map<Class<? extends SAFRTransfer>, List<Integer>> duplicateIdMap) {
		StringBuffer buffer = new StringBuffer();
		List<String> stringList = new ArrayList<String>();
		String shortMsg;
		shortMsg = "File "
				+ getCurrentFile().getName()
				+ " contains the following component IDs which already exist in the target environment. The existing components will be replaced by the imported components if you proceed.";

		stringList.add(0, shortMsg);
		for (Class<? extends SAFRTransfer> tfrClass : duplicateIdMap.keySet()) {

			// ignore association id's
			if (tfrClass.equals(FileAssociationTransfer.class) || 
				tfrClass.equals(ComponentAssociationTransfer.class)) {
				continue;
			}
				
			String tfrClassName = tfrClass.getSimpleName();
			int i = tfrClassName.indexOf("Transfer");
			String compName = tfrClassName.substring(0, i);
			buffer.append(compName + " IDs: ");
			for (Integer id : duplicateIdMap.get(tfrClass)) {
				buffer.append(getId(tfrClass, id) + " ");
			}
			buffer.append(SAFRUtilities.LINEBREAK);
		}
		stringList.add(1, buffer.toString());
		return stringList;
	}
	
	protected String getId(Class<? extends SAFRTransfer> tfrClass, Integer id) {
		// For non-composite keys just return the id.
		// Override this method for composite keys.
		return id.toString();
	}

	protected void checkOutOfRangeIds(Class<? extends SAFRTransfer> transferClass) throws SAFRException {

		Map<Integer, SAFRTransfer> tfrMap = null;
        NextKeyDAO nextKeyDao = DAOFactoryHolder.getDAOFactory().getNextKeyDAO();

		if (records.containsKey(transferClass)) {
			tfrMap = records.get(transferClass);
			Integer nextKeyId = nextKeyIdMap.get(transferClass);
			boolean changed = false; 
			for (Integer tfrId : tfrMap.keySet()) {
				if (nextKeyId != null && tfrId >= nextKeyId) {
				    changed = true;
				    nextKeyId = tfrId + 1;
					nextKeyIdMap.put(transferClass, nextKeyId);
				}
			}
			if (changed) {
                nextKeyDao.setNextKeyId(transferClass, nextKeyId);			    
			}
		}
	}

	protected void issueDuplicateIdsWarning() throws SAFRException {
	List<String> duplicateIdsMsg = prepareDuplicateIdsMessage(duplicateIdMap);
		// Issue warning and get response
		if (!getConfirmWarningStrategy().confirmWarning(
				"Importing existing components", duplicateIdsMsg.get(0), duplicateIdsMsg.get(1))) {
			SAFRValidationException sve1 = new SAFRValidationException();
			sve1.setErrorMessage(getCurrentFile().getName(),
					"Import cancelled on warning about existing components in target Environment.");
			throw sve1; // stop importing this file
		}
	}

	protected void issueNewNameWarning(DependentComponentNode dcnRoot) 
	throws SAFRException {
		String newNameMsg = prepareNewNameMessage();
		// Issue warning and get response
		if (!((NameConfirmWarningStrategy)getConfirmWarningStrategy()).correctNamesWarning(
				"Imported Name Duplication", newNameMsg, dcnRoot)) {
			SAFRValidationException sve = new SAFRValidationException();
			sve.setErrorMessage(getCurrentFile().getName(),
			"Import cancelled on warning about duplicate name.");
			throw sve; // stop importing this file
		}
	}
	
	private String prepareNewNameMessage() {
		StringBuffer buffer = new StringBuffer();
		buffer
				.append("File "
						+ getCurrentFile().getName()
						+ " contains names which duplicate one or more of those already in "
						+ " the target database."
						+ " Please edit or accept the suggested replacements below.");
		return buffer.toString();
	}

	private void storeExistingComponents(
			Class<? extends EnvironmentalQueryBean> compClass,
			List<? extends EnvironmentalQueryBean> comps) {
		Map<Integer, EnvironmentalQueryBean> compMap = new HashMap<Integer, EnvironmentalQueryBean>();
		for (EnvironmentalQueryBean comp : comps) {
			compMap.put(comp.getId(), comp);
		}
		existingComponents.put(compClass, compMap);

	}

	private void storeExistingAssociations(
			Class<? extends ComponentAssociation> assocClass,
			List<? extends ComponentAssociation> assocs) {
		Map<Integer, ComponentAssociation> assocMap = new HashMap<Integer, ComponentAssociation>();
		for (ComponentAssociation assoc : assocs) {
			assocMap.put(assoc.getAssociationId(), assoc);
		}
		existingAssociations.put(assocClass, assocMap);

	}

	private void storeExistingComponentTransfers(
			Class<? extends SAFREnvironmentalComponentTransfer> transClass,
			List<? extends SAFREnvironmentalComponentTransfer> transfers) {
		Map<Integer, SAFREnvironmentalComponentTransfer> transMap = new HashMap<Integer, SAFREnvironmentalComponentTransfer>();
		for (SAFREnvironmentalComponentTransfer trans : transfers) {
			transMap.put(ImportUtility.getId(trans), trans);
		}
		existingComponentTransfers.put(transClass, transMap);

	}

	public String splitMessage(String message) {
		return ImportUtility.splitMessage(message);
	}
	public String splitMessage(String message, int lineLength) {
		return ImportUtility.splitMessage(message, lineLength);
	}
	
}
