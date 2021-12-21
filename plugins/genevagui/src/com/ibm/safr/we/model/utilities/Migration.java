package com.ibm.safr.we.model.utilities;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.TimeReporter;
import com.ibm.safr.we.TimeStamper;
import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRFactory;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.HeaderFooterItem;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.View.HeaderFooterItems;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewLogicDependency;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.utilities.SAFRLogger;


public class Migration extends Observable {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.model.utilities.Migration");
	
	/**
	 * To be used by subclasses as the line break character instead of explicit
	 * platform-dependent escaped characters like '\r' and '\n'.
	 */
	transient protected static final String LINEBREAK = System
			.getProperty("line.separator");
	
	// Migration params
	private EnvironmentQueryBean sourceEnv;
	private EnvironmentQueryBean targetEnv;
	private ComponentType componentType;
	private MigrationComponent migComponent; // CQ10477
	private EnvironmentalQueryBean component; // keep for backwards compat
	private Boolean migrateRelated;
	
	// components to be migrated - base and related
	private Map<ComponentType, Set<SAFREnvironmentalComponent>> compMap = new LinkedHashMap<ComponentType, Set<SAFREnvironmentalComponent>>();
	
	// related component IDs
	private Map<ComponentType, Set<Integer>> relatedIdMap = new HashMap<ComponentType, Set<Integer>>();
	
	// migrated LRLF pairs that already exist in the target env
	private Map<Integer, ComponentAssociation> existingLRLFAssocs = new HashMap<Integer, ComponentAssociation>();	
	
	// time monitoring
	private TimeReporter reporter = new TimeReporter("");
	private TimeStamper stamper = new TimeStamper(TimeStamper.NONE, reporter);

	private ConfirmWarningStrategy confirmWarningStrategy;
	
	private SAFRFactory factory;
	
	private final String SOURCE = "source";
	private final String TARGET = "target";
	private final String ACTIVE = "ACTVE";
	private final String NOT_SPECIFIED = "[not specified]";
	
	// CQ10221
	private Map<String, List<String>> deletedPFAssociationMsgs = new HashMap<String, List<String>>();
	private Map<String, List<DependentComponentTransfer>> userExitViewDependencies = new HashMap<String, List<DependentComponentTransfer>>();

	// CQ10518
	private MessageRecorder msgRecorder = new MessageRecorder();
	private List<SAFRValidationException> recordedSVEs = new ArrayList<SAFRValidationException>();
	
	public Migration(EnvironmentQueryBean sourceEnv,
			EnvironmentQueryBean targetEnv, ComponentType componentType,
			MigrationComponent component,
			Boolean migrateRelatedComponents) {
		this.sourceEnv = sourceEnv;
		this.targetEnv = targetEnv;
		this.componentType = componentType;
		this.migComponent = component;
		this.component = migComponent.getComponent(); // for backwards compatibility
		this.migrateRelated = migrateRelatedComponents;
		
		this.factory = SAFRApplication.getSAFRFactory();
	}

    public Migration(EnvironmentQueryBean sourceEnv,
            EnvironmentQueryBean targetEnv, ComponentType componentType,
            EnvironmentalQueryBean component,
            Boolean migrateRelatedComponents) {
        this.sourceEnv = sourceEnv;
        this.targetEnv = targetEnv;
        this.componentType = componentType;
        this.component = component;
        this.migComponent = new MigrationComponent(component);
        this.migrateRelated = migrateRelatedComponents;
        
        this.factory = SAFRApplication.getSAFRFactory();
    }
    
	public EnvironmentQueryBean getSourceEnvironment() {
		return sourceEnv;
	}

	public EnvironmentQueryBean getTargetEnvironment() {
		return targetEnv;
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public EnvironmentalQueryBean getComponent() {
		return component;
	}

	public boolean isMigrateRelatedComponents() {
		return migrateRelated;
	}

	public void setMigrateRelatedComponents(Boolean migrateRelated) {
		this.migrateRelated = migrateRelated;
	}
	

	public enum Property {
		RELATED_COMPONENTS, SOURCE_ENVIRONMENT, TARGET_ENVIRONMENT, COMPONENT_TYPE, COMPONENT, TARGET_VIEW_FOLDER, MIGRATE_RELATED
	}

	private void showParams() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Migrating");
		buffer.append(LINEBREAK + "Source Environment = " + (sourceEnv != null ? getDescriptor(sourceEnv) : NOT_SPECIFIED));
		buffer.append(LINEBREAK + "Target Environment = " + (targetEnv != null ? getDescriptor(targetEnv) : NOT_SPECIFIED));
		buffer.append(LINEBREAK + "Component Type     = " + (componentType != null ? componentType.getLabel() : NOT_SPECIFIED));
		EnvironmentalQueryBean component = migComponent.getComponent();
		buffer.append(LINEBREAK + "Component          = " + (component != null ? getDescriptor(component) : NOT_SPECIFIED));
		buffer.append(LINEBREAK + "Migrate Related    = " + (migrateRelated ? "yes" : "no"));
		SAFRLogger.logAllSeparator(logger, Level.INFO, buffer.toString());
	}

	private void validateParams() throws SAFRValidationException {
		SAFRValidationException sve = new SAFRValidationException();
		sve.setSafrValidationType(SAFRValidationType.PARAMETER_ERROR);

		if (this.sourceEnv == null) {
			sve.setErrorMessage(Property.SOURCE_ENVIRONMENT,
					"Specify a source environment.");
		}

		if (this.targetEnv == null) {
			sve.setErrorMessage(Property.TARGET_ENVIRONMENT,
					"Specify a target environment.");
		}

		if (this.componentType == null) {
			sve.setErrorMessage(Property.COMPONENT_TYPE,
					"Specify a component type.");
		} else if (this.componentType != ComponentType.ControlRecord
				&& this.componentType != ComponentType.UserExitRoutine
				&& this.componentType != ComponentType.PhysicalFile
				&& this.componentType != ComponentType.LogicalFile
				&& this.componentType != ComponentType.LogicalRecord
				&& this.componentType != ComponentType.LookupPath
				&& this.componentType != ComponentType.View
				&& this.componentType != ComponentType.ViewFolder) {
			sve.setErrorMessage(
					Property.COMPONENT_TYPE,
					"The only component types that can be migrated are "
							+ "Control Record, User Exit Routine, "
							+ "Physical File, Logical File, Logical Record, "
							+ "Lookup Path, View and View Folder.");
		}
		
		if (this.migComponent == null || this.component == null) {
			sve.setErrorMessage(Property.TARGET_ENVIRONMENT,
					"Specify a component.");
		}
		
		if (this.migrateRelated == null) {
			sve.setErrorMessage(Property.MIGRATE_RELATED,
					"Specify whether to migrate related components.");
		}

		if (sve.getErrorMessages().size() > 0) {
			msgRecorder
					.recordError("Migration Parameters",
							"Invalid migration parameters",
							sve.getMessageString(), sve);
			throw sve;
		}
	}

	public void setConfirmWarningStrategy(ConfirmWarningStrategy strategy) {
		this.confirmWarningStrategy = strategy;
	}
	
	private void initMaps() {
		compMap.clear();
		compMap.put(ComponentType.ControlRecord, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.UserExitRoutine, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.PhysicalFile, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.LogicalFile, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.LogicalRecord, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.LookupPath, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.View, new HashSet<SAFREnvironmentalComponent>());
		compMap.put(ComponentType.ViewFolder, new HashSet<SAFREnvironmentalComponent>());
		
		relatedIdMap.clear();
		relatedIdMap.put(ComponentType.ControlRecord, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.UserExitRoutine, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.PhysicalFile, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.LogicalFile, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.LogicalRecord, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.LookupPath, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.View, new HashSet<Integer>());
		relatedIdMap.put(ComponentType.ViewFolder, new HashSet<Integer>());
	}
	
	private void doValidate() throws SAFRException {
		// create a map to record Inactive components
		Map<ComponentType, List<Integer>> inactiveComps = new HashMap<ComponentType, List<Integer>>();
		inactiveComps.put(ComponentType.LookupPath, new ArrayList<Integer>());
		inactiveComps.put(ComponentType.View, new ArrayList<Integer>());
		userExitViewDependencies.clear();
		
		// validate each component to be migrated
		for (ComponentType compType : compMap.keySet()) {
			Set<SAFREnvironmentalComponent> comps = compMap.get(compType);
			for (SAFREnvironmentalComponent comp : comps) {
			    genDependencyExceptions(comp);
			    
				SAFRValidationToken token = null;
				boolean flag = false;
				boolean done = false;
				
				while (!done) {
					String progress = "Validating " + compType.toString() + " ";
					progress += getDescriptor(comp);
					this.setChanged();
					this.notifyObservers(progress);
					try {						
						if (token == null) {
						    SAFRLogger.logAll(logger, Level.INFO, "Validate " + getDescriptor(comp)); // temporary
							comp.validate();
						} else {
						    SAFRLogger.logAll(logger, Level.INFO, "Re-validate " + getDescriptor(comp)); // temporary
							comp.validate(token); // for LR and LK warnings
						}
						done = true;
					} catch (SAFRValidationException sve) {
						SAFRValidationType type = null;
						if (sve.getSafrValidationToken() != null) {
							token = sve.getSafrValidationToken();
							type = token.getValidationFailureType();
						} else {
							type = sve.getSafrValidationType();
						}
						if (type == SAFRValidationType.ERROR) {
							// identify the component in error msg and re-throw
							sve.setContextMessage(compType.getLabel() + " "
									+ getDescriptor(comp) + LINEBREAK + LINEBREAK);
							msgRecorder.recordError("Validation error.",
									sve.getContextMessage(),
									sve.getMessageString(), sve);
							setResults("Validation error.",
									sve.getContextMessage(),
									sve.getMessageString(),
									ActivityResult.FAIL, sve);
							throw sve;
						}
						String contextMsg = "";
						if (type == SAFRValidationType.WARNING) {
							// CQ10221 The LR and LK data states that trigger
							// this type of validation warning would have to
							// apply to the source LR or LK which means it
							// must already be Inactive, so it would not be
							// eligible for Migration. Therefore this branch
							// should never execute in Migration (the warning
							// should only apply to Create/Edit LR or LK).
							// Also, Migration is not creating the warning
							// state - it is just copying existing state from
							// source to target environment.
							// Therefore this branch is redundant in migration
							// and the warning message code has been removed.
							// The IF statement remains for documentation only.
							continue;
						} else if (type == SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_ERROR) {
							contextMsg = "A new View dependency has been created on a deleted Physical File association for Logical File "
									+ getDescriptor(comp)
									+ " since the last deletion warning was issued. Migration cancelled. Please review and migrate again.";
							sve.setErrorMessage(
									LogicalFile.Property.PF_ASSOCIATION_DEP_IMPORT,
									contextMsg);
							msgRecorder
									.recordError(
											"Physical File association dependency",
											sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP_IMPORT),
											sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP),
											sve);
	                        setResults("Physical File association dependency",
                                    sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP_IMPORT),
                                    sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP),
	                                ActivityResult.FAIL, sve);
							throw sve;
						} else if (type == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR) {
							// Issued by LR validation, but extremely unlikely
							// this will occur in migration due to the minimal
							// elapsed time between removing the LF association
							// and validating the LR.
							contextMsg = "A new Lookup Path or View dependency has been created on a Logical File associated with Logical Record "
									+ getDescriptor(comp)
									+ " after the file association was deleted by migration. Migration cancelled.";
							sve.setErrorMessage(
									LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT,
									contextMsg);
							msgRecorder
									.recordError(
											"Logical File association dependency",
											sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT),
											sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP),
											sve);
                            setResults("Logical File association dependency",
                                    sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT),
                                    sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP),
                                    ActivityResult.FAIL, sve);                          							
							throw sve;
						} else if (type == SAFRValidationType.DEPENDENCY_LR_WARNING) {
							boolean dependencyChange = false;
							
							// CQ10221 Do not warn about Lookups or Views
							// already inactive
							for (Integer id : inactiveComps
									.get(ComponentType.LookupPath)) {
								// exclude Lookups inactivated previously in
								// this migration
								if (sve.hasDependency(ComponentType.LookupPath,	id)) {
									sve.removeDependency(ComponentType.LookupPath, id);
									dependencyChange = true;
								}
							}
							for (Integer id : inactiveComps
									.get(ComponentType.View)) {
								// exclude Views inactivated previously in this
								// migration
								if (sve.hasDependency(ComponentType.View, id)) {
									sve.removeDependency(ComponentType.View, id);
									dependencyChange = true;
								}
							}
							
							List<DependentComponentTransfer> deps = null;
							List<DependentComponentTransfer> depsCopy = null;
							
							// exclude Lookups which are inactive in the
							// target env
							deps = sve.getDependencies()
									.get(ComponentType.LookupPath);
							if (deps != null && deps.size() > 0) {
								depsCopy = Arrays
										.asList(deps
												.toArray(new DependentComponentTransfer[0]));
								for (DependentComponentTransfer dep : depsCopy) {
									if (sve.hasDependency(ComponentType.LookupPath,
											dep.getId()) && !dep.isActive()) {
										inactiveComps.get(ComponentType.LookupPath)
												.add(dep.getId());
										sve.removeDependency(
												ComponentType.LookupPath, dep.getId());
										dependencyChange = true;
									}
								}
							}
							
							// exclude Views which are inactive in the
							// target env
							deps = sve.getDependencies()
									.get(ComponentType.View);
							if (deps != null && deps.size() > 0) {
								depsCopy = Arrays
										.asList(deps
												.toArray(new DependentComponentTransfer[0]));
								for (DependentComponentTransfer dep : depsCopy) {
									if (sve.hasDependency(ComponentType.View,
											dep.getId()) && !dep.isActive()) {
										inactiveComps.get(ComponentType.View)
												.add(dep.getId());
										sve.removeDependency(
												ComponentType.View, dep.getId());
										dependencyChange = true;
									}
								}
							}
							
							// CQ10067 The warning msg about dependent Lookups
							// and Views which will be inactivated by migrating
							// this LR should only specify target Lookups and
							// Views which are NOT being migrated.
							// A migrated View will always be made inactive
							// and a migrated Lookup will not be inactivated
							// unless there is a problem with it, in which 
							// case a separate warning msg will be issued.
							
							if (componentType == ComponentType.View) {
								// exclude the migrated view from the dependency msg
								Integer viewId = component.getId();
								if (sve.hasDependency(ComponentType.View, viewId)) {
									sve.removeDependency(ComponentType.View, viewId);
									dependencyChange = true;
								}
							}
							if (componentType == ComponentType.View
									|| componentType == ComponentType.LookupPath) {
								// exclude any migrated lookups from the dep msg
								Set<SAFREnvironmentalComponent> lookups = compMap
										.get(ComponentType.LookupPath);
								if (lookups != null) {
									for (SAFREnvironmentalComponent lookup : lookups) {
										if (sve.hasDependency(
												ComponentType.LookupPath,
												lookup.getId())) {
											sve.removeDependency(
													ComponentType.LookupPath,
													lookup.getId());
											dependencyChange = true;
										}
									}
								}
							}
							if (dependencyChange) {
								if ((sve.getDependencies(ComponentType.View) == null || sve
										.getDependencies(ComponentType.View)
										.size() == 0)
										&& (sve.getDependencies(ComponentType.LookupPath) == null || sve
												.getDependencies(
														ComponentType.LookupPath)
												.size() == 0)) {
									// No dependencies remain so no warning msg
									continue;
								} else {
									// recreate the warning msg with any
									// remaining Active dependencies
									sve.createDependencyErrorMessage(LogicalRecord.Property.VIEW_LOOKUP_DEP);
								}
							}
							
							// this type is issued by LR validation
							if (flag == false) {
								contextMsg = "The following Lookup Paths or Views in the target environment are dependent on Logical Record "
										+ getDescriptor(comp)
										+ ". Migrating this Logical Record will make them Inactive.";
							} else {
								contextMsg = "A new Lookup Path or View dependency has been created in the target environment on Logical Record \""
										+ getDescriptor(comp)
										+ " since the last warning was issued. Migrating this Logical Record will make this component(s) Inactive too.";
							}
							
							if (!confirmWarning(
									"Logical Record dependency warning",
									contextMsg, sve.getMessageString())) {
								setResults(
										"Migration cancelled on Logical Record dependency warning.",
										contextMsg, sve.getMessageString(),
										ActivityResult.CANCEL, null);
								throw new SAFRCancelException(
										"Migration cancelled on warning about Lookup Paths or Views dependent on Logical Record.");
							} else {
								flag = true;
								// add inactivated Lookups to the inactive map
								deps = sve.getDependencies().get(
										ComponentType.LookupPath);
								if (deps != null) {
									List<Integer> inactiveLookups = inactiveComps
											.get(ComponentType.LookupPath);
									for (DependentComponentTransfer dep : deps) {
										if (!inactiveLookups.contains(dep.getId())) {
											inactiveLookups.add(dep.getId());
										}
									}
								}
								// add inactivated Views to the inactive map
								deps = sve.getDependencies().get(
										ComponentType.View);
								if (deps != null) {
									List<Integer> inactiveViews = inactiveComps
											.get(ComponentType.View);
									for (DependentComponentTransfer dep : deps) {
										if (!inactiveViews.contains(dep.getId())) {
											inactiveViews.add(dep.getId());
										}
									}
								}
							}
							
						} else if (type == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR) {
							// issued by LR on remove field and validation
							contextMsg = "Migration cancelled due to dependency errors in the target Environment."
									+ LINEBREAK + LINEBREAK
									+ "Logical Record "
									+ getDescriptor(comp)
									+ "exists in the source and target environments, so the target LR "
									+ "should be replaced by the source LR and any LR Fields "
									+ "which appear in the target LR but not in the source LR should be "
									+ "removed from the target environment. "
									+ "However, some of these LR Fields are already used "
									+ "by existing Lookup Paths or Views in the target environment."
									+ LINEBREAK + LINEBREAK
									+ "These dependencies (shown below) must be removed before this LR can be migrated.";
							sve.setErrorMessage(
									LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT,
									contextMsg);
							msgRecorder
									.recordError(
											"LR Field Dependencies",
											sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT),
											sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP),
											sve);
                            setResults("LR Field Dependencies",
                                    sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT),
                                    sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP),
                                    ActivityResult.FAIL, sve);                                                      							
							throw sve;
							
						} else if (type == SAFRValidationType.DEPENDENCY_LOOKUP_WARNING) {
							boolean dependencyChange = false;

							// CQ10221 Do not warn about Views already inactive
							for (Integer id : inactiveComps
									.get(ComponentType.View)) {
								// exclude Views inactivated previously in this
								// migration
								if (sve.hasDependency(ComponentType.View, id)) {
									sve.removeDependency(ComponentType.View, id);
									dependencyChange = true;
								}
							}
							// exclude Views which are inactive in the
							// target env
							List<DependentComponentTransfer> depViews = sve
									.getDependencies().get(ComponentType.View);
							if (depViews != null && depViews.size() > 0) {
								List<DependentComponentTransfer> depViewsCopy = Arrays
										.asList(depViews
												.toArray(new DependentComponentTransfer[0]));
								for (DependentComponentTransfer dep : depViewsCopy) {
									if (sve.hasDependency(ComponentType.View,
											dep.getId()) && !dep.isActive()) {
										inactiveComps.get(ComponentType.View)
												.add(dep.getId());
										sve.removeDependency(
												ComponentType.View, dep.getId());
										dependencyChange = true;
									}
								}
							}
							
							// CQ10067 If the lookup is part of a view migration,
							// don't include the view in the inactivated dependency
							// warning message as it will be inactive anyway after
							// the migration.
							if (componentType == ComponentType.View) {
								Integer viewId = component.getId();
								if (sve.hasDependency(ComponentType.View,
										viewId)) {
									sve.removeDependency(ComponentType.View,
											viewId);
									dependencyChange = true;
								}
							}

							if (dependencyChange) {
								if ((sve.getDependencies(ComponentType.View) == null || sve
										.getDependencies(ComponentType.View)
										.size() == 0)) {
									// No dependencies remain so no warning msg
									continue;
								} else {
									// recreate the warning msg with any
									// remaining Active views
									sve.createDependencyErrorMessage(LookupPath.Property.VIEW_DEP);
								}
							}
							
							// this type is issued by LK validation
							if (flag == false) {
								contextMsg = "The following Views in the target environment are dependent on Lookup Path "
										+ getDescriptor(comp)
										+ ". Migrating this Lookup Path will make them Inactive.";
							} else {
								contextMsg = "A new View dependency has been created in the target environment on Lookup Path \""
										+ getDescriptor(comp)
										+ " since the last warning was issued. Migrating this Lookup Path will make this component(s) Inactive too.";
							}
							
							if (!confirmWarning(
									"Lookup Path dependency warning",
									contextMsg, sve.getMessage())) {
								setResults(
										"Migration cancelled on Lookup Path dependency warning.",
										contextMsg, sve.getMessageString(),
										ActivityResult.CANCEL, null);
								throw new SAFRCancelException(
										"Migration cancelled on warning about Views dependent on Lookup Path.");
							} else {
								flag = true;
								// add inactivated views to the inactive map
								depViews = sve.getDependencies().get(
										ComponentType.View);
								if (depViews != null) {
									List<Integer> inactiveViews = inactiveComps.get(ComponentType.View);
									for (DependentComponentTransfer dep : depViews) {
										if (!inactiveViews.contains(dep.getId())) {
											inactiveViews.add(dep.getId());
										}
									}
								}
							}
						} // end elseif DEPENDENCY_LOOKUP_WARNING
					} // end catch block
				} // end while loop
				clearDependencyExceptions(comp);
			} // end for comp loop

			if (compType == ComponentType.UserExitRoutine
					&& userExitViewDependencies.size() > 0) {
				// Warn about any Write exit View logic text dependencies.
				// These are accumulated during user exit validation via a
				// migration callback.
				List<Integer> inactiveViews = null;
				List<DependentComponentTransfer> activeViews = new ArrayList<DependentComponentTransfer>();
				for (Entry<String, List<DependentComponentTransfer>> exitViews : userExitViewDependencies
						.entrySet()) {
					activeViews.clear();
					inactiveViews = inactiveComps.get(ComponentType.View);
					String exitDescriptor = exitViews.getKey();
					List<Integer> uniqueIds = new ArrayList<Integer>();
					Integer viewId = null;
					for (DependentComponentTransfer view : exitViews.getValue()) {
						viewId = view.getId();
						if (inactiveViews.contains(viewId)) {
							// view inactivated previously, ignore it
						} else if (!view.isActive()) {
							// view is inactive in target env so add it to
							// inactive list
							inactiveViews.add(viewId);
						} else {
							if (!uniqueIds.contains(viewId)) {
								activeViews.add(view);
								uniqueIds.add(viewId); // avoid duplicates
							}
						}
					}
					if (activeViews.size() > 0) {
						// create the detail msg
						String dependencies = "Views:" + LINEBREAK;
						for (DependentComponentTransfer view : activeViews) {
							dependencies += "    " + view.getName() + " ["
									+ view.getId() + "]" + LINEBREAK;
						}
						
						//create the context msg
						String contextMsg = "The following Views have a logic text dependency on User Exit Routine '"
								+ exitDescriptor
								+ "'. Migrating this User Exit Routine will make them Inactive.";
						
						if (!confirmWarning(
								"User Exit Routine dependency warning",
								contextMsg, dependencies)) {
							setResults(
									"Migration cancelled on User Exit dependency warning.",
									contextMsg, dependencies,
									ActivityResult.CANCEL, null);
							throw new SAFRCancelException(
									"Migration cancelled on warning about View logic text dependency on User Exit Routine.");
						} else {
							// add views to the inactive map
							for (DependentComponentTransfer view : activeViews) {
								if (!inactiveViews.contains(view.getId())) {
									inactiveViews.add(view.getId());
								}
							}
						}
					}
				}
			} // end if user exit deps
			
		} // end for compType loop
	}
	
    private void genDependencyExceptions(SAFREnvironmentalComponent comp) {
	    if (comp instanceof LogicalRecord) {
	        LogicalRecord lr = (LogicalRecord)comp;
	        Set<SAFREnvironmentalComponent> exComps = compMap.get(ComponentType.LookupPath);
	        if (exComps != null) {
    	        for (SAFREnvironmentalComponent exComp : exComps ) {
    	            lr.getMigExLookupList().add(exComp.getId());
    	        }
	        }
            exComps = compMap.get(ComponentType.View);
            if (exComps != null) {
                for (SAFREnvironmentalComponent exComp : exComps ) {
                    lr.getMigExViewList().add(exComp.getId());
                }
            }
	    } else  if (comp instanceof LookupPath) {
	        LookupPath lp = (LookupPath)comp;
            Set<SAFREnvironmentalComponent> exComps = compMap.get(ComponentType.View);
            if (exComps != null) {
                for (SAFREnvironmentalComponent exComp : exComps ) {
                    lp.getMigExViewList().add(exComp.getId());
                }
            }
        }
    }

    private void clearDependencyExceptions(SAFREnvironmentalComponent comp) {
        if (comp instanceof LogicalRecord) {
            LogicalRecord lr = (LogicalRecord)comp;
            lr.getMigExLookupList().clear();
            lr.getMigExViewList().clear();
        } else  if (comp instanceof LookupPath) {
            LookupPath lp = (LookupPath)comp;
            lp.getMigExViewList().clear();
        }
    }

    private void doStore() throws SAFRException {

		// store model objects
		boolean success = false;
		while (!success) {
			try {
				// Begin Transaction
			    Map<Class<? extends SAFREnvironmentalComponent>, TimeStamper> timeMap = 
			        new HashMap<Class<? extends SAFREnvironmentalComponent>, TimeStamper>();
				DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
				DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOn();

				SAFRApplication.getModelCount().restartCount();
				SAFRApplication.getTimingMap().startRecording();
				
                TimeReporter reporter = new TimeReporter("Store: ");				
				for (Set<SAFREnvironmentalComponent> comps : compMap.values()) {
					for (SAFREnvironmentalComponent comp : comps) {
					    if (!timeMap.containsKey(comp.getClass())) {
					        timeMap.put(comp.getClass(), new TimeStamper(TimeStamper.NONE, reporter));
					    }
					    timeMap.get(comp.getClass()).startStamp();
						String progress = "Storing : " + getDescriptor(comp);
						SAFRLogger.logAll(logger, Level.INFO, progress);
						this.setChanged();
				        notifyObservers( progress );
                        comp.store();
                        timeMap.get(comp.getClass()).stopStamp();                        
					}
				}
				
				// report timing of all Store methods
				String tmReport = "Component Store Timing" + LINEBREAK;
				for (Map.Entry<Class<? extends SAFREnvironmentalComponent>, TimeStamper> ent : timeMap.entrySet()) {
				    tmReport += reporter.report(ent.getValue(), ent.getKey().getName()) + LINEBREAK;
				}
				SAFRLogger.logAll(logger, Level.INFO, tmReport);
                
				// log migration audit details
				DAOFactoryHolder.getDAOFactory().getMigrateDAO().logMigration(sourceEnv.getId(), 
						targetEnv.getId(), componentType, component.getId(), component.getName());

				success = true;
			} catch (DAOUOWInterruptedException e) {
				// UOW interrupted so retry it
				continue;
			} finally {
				DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOff();
				if (success) {
					// Complete the transaction.
					DAOFactoryHolder.getDAOFactory().getDAOUOW().end();

				} else {
					// Fail the transaction.
					DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
				}
				SAFRApplication.getTimingMap().report("DAO Method timings");
                SAFRApplication.getTimingMap().stopRecording();
                SAFRApplication.getModelCount().report();               
			}
		}

	}
	
	/**
	 * This method drives the migration activity. It is the method called by 
	 * clients to start the migration process after the migration criteria
	 * have been chosen (source / target environment, component type,
	 * component, possible view folder).
	 * @param monitor 
	 * 
	 * @throws SAFRException
	 */
	public void migrate() throws SAFRException {
		
		try {
			showParams();
			validateParams();
			initMaps();
			checkEditRightsRelatedComponents();
			
			migComponent.setResult(ActivityResult.FAIL);

			switch (componentType) {
			case ControlRecord:
				migrateControlRecord();
				break;
			case UserExitRoutine:
				migrateUserExitRoutine();
				break;
			case PhysicalFile:
				migratePhysicalFile();
				break;
			case LogicalFile:
				migrateLogicalFile();
				break;
			case LogicalRecord:
				migrateLogicalRecord();
				break;
			case LookupPath:
				migrateLookupPath();
				break;
			case View:
				migrateView();
				break;
			case ViewFolder:
				migrateViewFolder();
			default:
				break;
			}
			
			migComponent.setResult(ActivityResult.PASS);
		
		} catch (SAFRValidationException sve) {
			if (!recordedSVEs.contains(sve)) {
				msgRecorder.recordError("Migration component error.",
						sve.getContextMessage(), sve.getMessageString());
				setResults("Migration component error.",
						sve.getContextMessage(), sve.getMessageString(),
						ActivityResult.FAIL, sve);
			}
			throw sve;
		} catch (SAFRCancelException ce) {
			msgRecorder.recordCancelled(ce.getMessage());
			migComponent.setException(ce);
			throw ce;
		} catch (SAFRDependencyException sde) {
			msgRecorder.recordError("Migration dependency error.",
					sde.getContextMessage(), sde.getDependencyString());
			setResults("Migration dependency error.", sde.getContextMessage(),
					sde.getDependencyString(), ActivityResult.FAIL, sde);
			throw sde;
		} catch (SAFRException se) {
            logger.log(Level.SEVERE, "Error migrating" , se);		    
			msgRecorder.recordError("Migration error.", se.getMessage(),"");
			setResults("Migration error.",se.getMessage(), "", ActivityResult.SYSTEMERROR, se);
			throw se;
		} catch (RuntimeException e) {
			migComponent.setResult(ActivityResult.SYSTEMERROR);
			SAFRLogger.logAllStamp(logger, Level.SEVERE, "Migration unexpected error.", e);
			throw e;
		} finally {
			msgRecorder.writeMessages(); // write messages to log file
		}
		SAFRLogger.logEnd(logger);
	}

	private void migrateControlRecord() throws SAFRException {
		ControlRecord crSource = factory.getControlRecord(component.getId(),
				sourceEnv.getId());

		String progress = "Control Record : " + crSource.getName();
		this.setChanged();
        notifyObservers( progress );

		try {
			factory.getControlRecord(component.getId(), targetEnv.getId());
			crSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			crSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		}
		compMap.get(ComponentType.ControlRecord).add(crSource);
		
		confirmMigration();
		doValidate();
		doStore();
	}

	private void migrateUserExitRoutine() throws SAFRException {
		UserExitRoutine uxSource = factory.getUserExitRoutine(
				component.getId(), sourceEnv.getId());
        uxSource.setConfirmWarningStrategy(confirmWarningStrategy);
        uxSource.setMigrationCallback(this); // CQ10221
		
		String progress = "User Exit : " + uxSource.getName();
		this.setChanged();
        notifyObservers( progress );

		try {
			factory.getUserExitRoutine(uxSource.getId(), targetEnv.getId());
			uxSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			uxSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		}
		compMap.get(ComponentType.UserExitRoutine).add(uxSource);

		confirmMigration();
		doValidate();
		doStore();
	}
	
	private void migratePhysicalFile() throws SAFRException {
		PhysicalFile pfSource = factory.getPhysicalFile(component.getId(),
				sourceEnv.getId());

		String progress = "Physical File : " + pfSource.getName();
		this.setChanged();
        notifyObservers( progress );
		
		// read exit - do this here before we change the EnvID
		UserExitRoutine uxSource = pfSource.getUserExitRoutine();

		try {
			factory.getPhysicalFile(pfSource.getId(), targetEnv.getId());
			pfSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			pfSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		}
		compMap.get(ComponentType.PhysicalFile).add(pfSource);
		
		if (uxSource != null) {
			checkRelatedUserExitRoutine(uxSource);
		}
		
		confirmMigration();
		doValidate();
		doStore();
	}
	
	private void migrateLogicalFile() throws SAFRException {
		LogicalFile lfSource = factory.getLogicalFile(component.getId(),
				sourceEnv.getId());

		String progress = "Logical File : " + lfSource.getName();
		this.setChanged();
        notifyObservers( progress );

		LogicalFile lfTarget = null;
		try {
			lfTarget = factory.getLogicalFile(lfSource.getId(),
					targetEnv.getId());
			lfSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			lfSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		}
		compMap.get(ComponentType.LogicalFile).add(lfSource);
		
		checkLogicalFileDetails(lfSource, lfTarget);

		confirmMigration();
		showPreValidationWarnings();
		doValidate();
		doStore();
	}
	
	private void migrateLogicalRecord() throws SAFRException {
		String timeReport = new String();
		stamper.startStamp();
		
		LogicalRecord lrSource = factory.getLogicalRecord(component.getId(),
				sourceEnv.getId());

		if( !lrSource.isActive() ) {
			String msg = "Logical Record " + lrSource.getName() + " is not Active and cannot be migrated.";
			throw new SAFRValidationException(
				ComponentType.LogicalRecord, msg);
		}

		String progress = "Logical Record : " + lrSource.getName();
		this.setChanged();
        notifyObservers( progress );

		lrSource.getLookupExitRoutine(); // init before chg envid

		LogicalRecord lrTarget = null;
		try {
			lrTarget = factory.getLogicalRecord(lrSource.getId(),
					targetEnv.getId());
			lrSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			lrSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		}
		compMap.get(ComponentType.LogicalRecord).add(lrSource);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Retrieve Logical Record:") + LINEBREAK;
		
		reporter.reset();
		stamper.startStamp();						
		checkLogicalRecordDetails(lrSource, lrTarget);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Check Lookup Dependencies:") + LINEBREAK;

		confirmMigration();
		showPreValidationWarnings();

		reporter.reset();
		stamper.startStamp();						
		doValidate();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Validate Migration:") + LINEBREAK;

		reporter.reset();
		stamper.startStamp();						
		doStore();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Store migrated Logical Record:") + LINEBREAK;
		
		SAFRLogger.logAll(logger, Level.INFO, "Migrate Logical Record " + lrSource.getId() + " from Env " + sourceEnv.getId() + " to Env " +
				targetEnv.getId() + LINEBREAK + timeReport);				
		
	}
	
	private void migrateLookupPath() throws SAFRException {
		
		String timeReport = new String();
		stamper.startStamp();
		
		LookupPath lkSource;
		try {
			lkSource = factory.getLookupPath(component.getId(),
					sourceEnv.getId());
		} catch (SAFRDependencyException sde) {
			String ctxMsg = getInactiveContextMsg(ComponentType.LookupPath,
					getDescriptor(component), SOURCE);
			sde.setContextMessage(ctxMsg);
			throw sde;
		}
		
		if( !lkSource.isValid() ) {
			String msg = "Lookup Path " + lkSource.getName() + " is not Active and cannot be migrated.";
			throw new SAFRValidationException(
				ComponentType.LookupPath, msg);
		}

		String progress = "Lookup Path : " + lkSource.getName();
		this.setChanged();
        notifyObservers( progress );

		lkSource.getTargetLrFileAssociation(); // lazy init
        
		LookupPath lkTarget = null;
		try {
			lkTarget = factory.getLookupPath(lkSource.getId(),
					targetEnv.getId());
			lkSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			lkSource.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		} catch (SAFRDependencyException sde) {
			String ctxMsg = getInactiveContextMsg(ComponentType.LookupPath,
					getDescriptor(component), TARGET);
			sde.setContextMessage(ctxMsg);
			throw sde;
		}
		compMap.get(ComponentType.LookupPath).add(lkSource);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Retrieve Lookup:") + LINEBREAK;
		
		reporter.reset();
		stamper.startStamp();				
		checkLookupPathDetails(lkSource, lkTarget);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Check Lookup Dependencies:") + LINEBREAK;

		confirmMigration();
		showPreValidationWarnings();
		
		reporter.reset();
		stamper.startStamp();				
		doValidate();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Validate Migration:") + LINEBREAK;
		
		reporter.reset();
		stamper.startStamp();				
		doStore();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Store Migrated Lookup:") + LINEBREAK;
		
		SAFRLogger.logAll(logger, Level.INFO, "Migrate Lookup " + lkSource.getId() + " from Env " + sourceEnv.getId() + " to Env " +
				targetEnv.getId() + LINEBREAK + timeReport);				
	}
	
	private void migrateView() throws SAFRException {
		
		String timeReport = new String();
		stamper.startStamp();
		View srcView;
		try {
			srcView = factory.getView(component.getId(),
					sourceEnv.getId());
		} catch (SAFRDependencyException sde) {
			String ctxMsg = getInactiveContextMsg(ComponentType.View,
					getDescriptor(component), SOURCE);
			sde.setContextMessage(ctxMsg);
			throw sde;
		}

		if(srcView.getStatusCode() != SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE)) {
			String msg = "View " + srcView.getName() + " is not Active and cannot be migrated.";
			throw new SAFRValidationException(
				ComponentType.View, msg);
		}
		
		String progress = "View : " + srcView.getName();
		this.setChanged();
        notifyObservers( progress );

		srcView.getExtractFileAssociation(); //Lazy init
		srcView.getControlRecord(); //Lazy Init
		srcView.getViewLogicDependencies(); //Lazy init
		srcView.getFormatExit(); //Lazy init
		srcView.getWriteExit(); //Lazy init
		if (srcView.getViewColumnSources() != null)
		{
			for (ViewColumnSource src : srcView.getViewColumnSources())
			{
				src.getSortKeyTitleLRField(); //Lazy init
			}
		}
				
		View tgtView = null;
		try {
			tgtView = factory.getView(srcView.getId(),
					targetEnv.getId());
			srcView.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.OLD);
			
		}  catch (SAFRNotFoundException e) {
			srcView.migrateToEnvironment(targetEnv.getId(),
					SAFRPersistence.NEW);
		} catch (SAFRDependencyException sde) {
			String ctxMsg = getInactiveContextMsg(ComponentType.View,
					getDescriptor(component), TARGET);
			sde.setContextMessage(ctxMsg);
			throw sde;
		}
		compMap.get(ComponentType.View).add(srcView);
		
		// CQ9830 these view properties needed by ViewSortKey title validation
		srcView.setMigrateRelatedComponents(migrateRelated);
		
		srcView.makeViewInactive();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Retrieve View:") + LINEBREAK;
		
		reporter.reset();
		stamper.startStamp();		
		checkViewDetails(srcView, tgtView);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Check View Dependencies:") + LINEBREAK;

		confirmMigration();
		showPreValidationWarnings();
		
		reporter.reset();
		stamper.startStamp();		
		doValidate();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Validate Migration:") + LINEBREAK;
		
		reporter.reset();		
		stamper.startStamp();		
		doStore();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Store migrated View:") + LINEBREAK;
		
		SAFRLogger.logAll(logger, Level.INFO, "Migrate View " + srcView.getId() + " from Env " + sourceEnv.getId() + " to Env " +
				targetEnv.getId() + LINEBREAK + timeReport);		
	}
	
	private void migrateViewFolder() throws SAFRException {
		
		String timeReport = new String();
		stamper.startStamp();
		
		ViewFolder srcViewFolder = factory.getViewFolder(component.getId(),
				sourceEnv.getId());
		
		String progress = "View Folder : " + srcViewFolder.getName();
		this.setChanged();
        notifyObservers( progress );
		
		try {
			factory.getViewFolder(srcViewFolder.getId(), targetEnv.getId());
			srcViewFolder.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
		} catch (SAFRNotFoundException e) {
			srcViewFolder.migrateToEnvironment(targetEnv.getId(),SAFRPersistence.NEW);
		}
		compMap.get(ComponentType.ViewFolder).add(srcViewFolder);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Retrieve View Folder:") + LINEBREAK;
		
		reporter.reset();
		stamper.startStamp();			
		checkViewFolderDetails(srcViewFolder);
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Check View Folder Dependencies:") + LINEBREAK;

		confirmMigration();
		showPreValidationWarnings();

		reporter.reset();
		stamper.startStamp();				
		doValidate();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Validate Migration:") + LINEBREAK;
		
		reporter.reset();
		stamper.startStamp();				
		doStore();
		stamper.stopStamp();
		timeReport += reporter.report(stamper, "Store Migrated View Folder:") + LINEBREAK;
		
		SAFRLogger.logAll(logger, Level.INFO, "Migrate View Folder " + srcViewFolder.getId() + " from Env " + sourceEnv.getId() + 
				" to Env " + targetEnv.getId() + LINEBREAK + timeReport);				
	}

	private void checkRelatedControlRecord(ControlRecord cr) throws SAFRException {
		String progress = "Check Related Control Record : " + cr.getName();
		this.setChanged();
        notifyObservers( progress );

		if (relatedIdMap.get(ComponentType.ControlRecord).contains(cr.getId())) {
			// already checked
			return;
		} else {
			// check it
			relatedIdMap.get(ComponentType.ControlRecord).add(cr.getId());
		}
	
		boolean existsInTarget;
		try {
			factory.getControlRecord(cr.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, "Control Record exists in Env " + targetEnv.getId() + " with ID : " + cr.getId());			
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		}
		
		if (migrateRelated
		    && SAFRApplication.getUserSession().isAdminOrMigrateInUser(targetEnv.getId())) {
			if (existsInTarget) {
				// replace it
				cr.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				cr.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.ControlRecord).add(cr);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.ControlRecord,
						getDescriptor(cr));
				throw new SAFRValidationException(ComponentType.ControlRecord,
						msg);
			}
		}
	}

	private void checkRelatedUserExitRoutine(UserExitRoutine uxr)
			throws SAFRException {
		String progress = "Check Related Exit Routine : " + uxr.getName();
		this.setChanged();
        notifyObservers( progress );
		
		if (relatedIdMap.get(ComponentType.UserExitRoutine).contains(uxr.getId())) {
			// already checked
			return;
		} else {
			// check it
			relatedIdMap.get(ComponentType.UserExitRoutine).add(uxr.getId());
		}
		
		uxr.setConfirmWarningStrategy(confirmWarningStrategy);
		uxr.setMigrationCallback(this); // CQ10221
		
		boolean existsInTarget;
		try {
			factory.getUserExitRoutine(uxr.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, 
			    "User Exit Routine exists in Env " + targetEnv.getId() + " with ID : " + uxr.getId());
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		}
		
		if (migrateRelated) {
			if (existsInTarget) {
				// replace it
				uxr.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				uxr.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.UserExitRoutine).add(uxr);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.UserExitRoutine,
						getDescriptor(uxr));
				throw new SAFRValidationException(
						ComponentType.UserExitRoutine, msg);
			}
		}
	}
	
	private void checkRelatedPhysicalFile(PhysicalFile pf) throws SAFRException {
		String progress = "Check Related Physical File : " + pf.getName();
		this.setChanged();
        notifyObservers( progress );
				
		// get read exit - here before we change the envID
		UserExitRoutine uxSource = pf.getUserExitRoutine();

		boolean existsInTarget;
		try {
			factory.getPhysicalFile(pf.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, 
			    "Physical File exists in Env " + targetEnv.getId() + " with ID : " + pf.getId());
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		}
		
		if (migrateRelated) {
			if (existsInTarget) {
				// replace it
				pf.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				pf.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.PhysicalFile).add(pf);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.PhysicalFile,
						getDescriptor(pf));
				throw new SAFRValidationException(ComponentType.PhysicalFile,
						msg);
			}
		}
		if (uxSource != null) {
			checkRelatedUserExitRoutine(uxSource);
		}
	}
	
	private void checkRelatedLogicalFile(LogicalFile lf) throws SAFRException {
		String progress = "Check Related Logical File : " + lf.getName();
		this.setChanged();
        notifyObservers( progress );

		boolean existsInTarget;
		LogicalFile lfTarget = null;
		try {
			lfTarget = factory.getLogicalFile(lf.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, 
			    "Logical File exists in Env " + targetEnv.getId() + " with ID : " + lf.getId());			
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		}

		if (migrateRelated) {
			if (existsInTarget) {
				// replace it
				lf.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				lf.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.LogicalFile).add(lf);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.LogicalFile, getDescriptor(lf));
				throw new SAFRValidationException(ComponentType.LogicalFile, msg);
			}
		}
		
		checkLogicalFileDetails(lf, lfTarget);
	}
	
	private void checkRelatedLogicalRecord(LogicalRecord lr) throws SAFRException {
		String progress = "Check Related Logical Record : " + lr.getName();
		this.setChanged();
        notifyObservers( progress );

		boolean existsInTarget;
		LogicalRecord lrTarget = null;
		try {
			lrTarget = factory.getLogicalRecord(lr.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, 
			    "Logical Record exists in Env " + targetEnv.getId() + " with ID : " + lr.getId());
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		}
			
		lr.getLookupExitRoutine(); // init before chg envid

		if (migrateRelated) {
			if (existsInTarget) {
				// replace it
				lr.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				lr.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.LogicalRecord).add(lr);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.LogicalRecord,
						getDescriptor(lr));
				throw new SAFRValidationException(ComponentType.LogicalRecord,
						msg);
			}
		}
		
		checkLogicalRecordDetails(lr, lrTarget);
	}
	
	private void checkRelatedLookupPath(LookupPath lu) throws SAFRException {
		String progress = "Check Related Lookup Path : " + lu.getName();
		this.setChanged();
        notifyObservers( progress );
		
        lu.getTargetLrFileAssociation(); //lazy init
        
		boolean existsInTarget;
		LookupPath luTarget = null;
		try {
			luTarget = factory.getLookupPath(lu.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, 
			    "Lookup Path exists in Env " + targetEnv.getId() + " with ID : " + lu.getId());
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		} catch (SAFRDependencyException sde) {
			String ctxMsg = getInactiveContextMsg(ComponentType.LookupPath,
					getDescriptor(lu), TARGET);
			sde.setContextMessage(ctxMsg);
			throw sde;
		}

		if (migrateRelated
		    && SAFRApplication.getUserSession().isAdminOrMigrateInUser(targetEnv.getId())) {
			if (existsInTarget) {
				// replace it
				lu.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				lu.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.LookupPath).add(lu);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.LookupPath,
						getDescriptor(lu));
				throw new SAFRValidationException(ComponentType.LookupPath,
						msg);
			}
		}
		
		checkLookupPathDetails(lu, luTarget);
	}
	
	private void checkRelatedView(View view, ViewFolder srcViewFolder) throws SAFRException{
		String progress = "Check Related View : " + view.getName();
		this.setChanged();
        notifyObservers( progress );
		
		// Only 'active' views are passed in so no need to check status.
        
		if (relatedIdMap.get(ComponentType.View).contains(view.getId())) {
			// already checked
			return;
		} else {
			// check it
			relatedIdMap.get(ComponentType.View).add(view.getId());
		}
		
		view.getExtractFileAssociation(); //Lazy init
		view.getControlRecord(); //Lazy Init
		view.getViewLogicDependencies(); //Lazy init
		view.getFormatExit(); //Lazy init
		view.getWriteExit(); //Lazy init
		if (view.getViewColumnSources() != null)
		{
			for (ViewColumnSource src : view.getViewColumnSources())
			{
				src.getSortKeyTitleLRField(); //Lazy init
			}
		}
		
		boolean existsInTarget;
		View tgtView = null;
		try {
			tgtView = factory.getView(view.getId(), targetEnv.getId());
			SAFRLogger.logAll(logger, Level.INFO, 
			    "View exists in Env " + targetEnv.getId() + " with ID : " + view.getId());
			existsInTarget = true;
		} catch (SAFRNotFoundException e) {
			existsInTarget = false;
		} catch (SAFRDependencyException sde) {
			String ctxMsg = getInactiveContextMsg(ComponentType.View,
					getDescriptor(view), TARGET);
			sde.setContextMessage(ctxMsg);
			throw sde;
		}
		
        // TC18418 these view properties needed by ViewSortKey title validation
        view.setMigrateRelatedComponents(migrateRelated);
		
		if (migrateRelated) {
			if (existsInTarget) {
				// replace it
				view.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.OLD);
			} else {
				// create it
				view.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
			}
			compMap.get(ComponentType.View).add(view);
		} else {
			if (existsInTarget) {
				// re-use it, no further action needed
				return;
			} else {
				// error, related component not in target env
				String msg = getMissingCompMsg(ComponentType.View,
						getDescriptor(view));
				throw new SAFRValidationException(
						ComponentType.View, msg);
			}
		}
		
		view.makeViewInactive();		
		
		checkViewDetails(view, tgtView);
	}

	private void checkLogicalFileDetails(LogicalFile lfSource,
			LogicalFile lfTarget) throws SAFRException {

		String progress = "Check Logical File Details : " + lfSource.getName();
		this.setChanged();
        notifyObservers( progress );

		SAFRList<FileAssociation> srcAssocs = lfSource
				.getPhysicalFileAssociations();

		if (lfTarget == null) {
			// All source PF assocs will be NEW in target env
			for (FileAssociation srcAssoc : srcAssocs) {
				srcAssoc.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
		}

		if (lfTarget != null) {
			List<String> dependencyMsgs = new ArrayList<String>();
			SAFRList<FileAssociation> tgtAssocs = lfTarget
					.getPhysicalFileAssociations();
			
			// If a source PF assoc already exists in the target env use the
			// OLD target assoc, else use the NEW source assoc.
			List<FileAssociation> flushed = new ArrayList<FileAssociation>();
			List<FileAssociation> added = new ArrayList<FileAssociation>();
			for (FileAssociation srcAssoc : srcAssocs) {
				boolean found = false;
				for (FileAssociation tgtAssoc : tgtAssocs) {
					if (srcAssoc.getAssociatedComponentIdNum().equals(
							tgtAssoc.getAssociatedComponentIdNum())) {
						flushed.add(srcAssoc);
						added.add(tgtAssoc);
						found = true;
						break;
					}
				}
				if (!found) {
					srcAssoc.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			// Replace src assocs with any existing trg assocs
			for (FileAssociation assoc : flushed) {
				srcAssocs.flush(assoc);
			}
			for (FileAssociation assoc : added) {
				srcAssocs.add(assoc);
			}
			
			// Delete target PF assocs that don't exist in source LF.
			for (FileAssociation tgtAssoc : tgtAssocs) {
				boolean found = false;
				for (FileAssociation srcAssoc : srcAssocs) {
					if (tgtAssoc.getAssociatedComponentIdNum().equals(
							srcAssoc.getAssociatedComponentIdNum())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// The target PF assoc does not exist in the source LF, so
					// add it to that LF and then remove it so that it will be
					// flagged for deletion when the LF is stored in target env.
					lfSource.addAssociatedPhysicalFile(tgtAssoc);
					try {
						lfSource.removeAssociatedPhysicalFile(tgtAssoc, null);
					} catch (SAFRValidationException sve) {
						SAFRValidationToken token = sve
								.getSafrValidationToken();
						if (token != null
								&& token.getValidationFailureType() == SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_WARNING) {
							// CQ10221 Assume PF dependency warning will be
							// accepted.
							// Remove the PF associations now and store the
							// warning details to be displayed later after
							// the Migration Confirmation message along with
							// the standard validation warnings. If the
							// user cancels at that stage, these changes
							// can just be discarded as they are not persistent
							// yet.
							lfSource.removeAssociatedPhysicalFile(tgtAssoc,
									sve.getSafrValidationToken());
							dependencyMsgs.add(sve.getMessageString());

						} else {
							throw sve;
						}
					}
				} // end if not found
			} // end for loop trgAssocs
			
			if (!dependencyMsgs.isEmpty()) {
				// store deleted PFs dependency msgs
				String lfDescriptor = getDescriptor(lfTarget);
				deletedPFAssociationMsgs.put(lfDescriptor, dependencyMsgs);
			}
			
		} // end if lfTarget not null

		// Check each associated PF
		List<FileAssociation> pfAssocs = lfSource.getPhysicalFileAssociations()
				.getActiveItems();
		for (FileAssociation pfAssoc : pfAssocs) {
			if (relatedIdMap.get(ComponentType.PhysicalFile).contains(pfAssoc.getAssociatedComponentIdNum())) {
				logger.fine("Physical File already checked " + pfAssoc.getAssociatedComponentIdNum());
				// already checked
				continue;
			} else {
				// check it
				relatedIdMap.get(ComponentType.PhysicalFile).add(pfAssoc.getAssociatedComponentIdNum());
			}
			
			PhysicalFile pf = factory.getPhysicalFile(
					pfAssoc.getAssociatedComponentIdNum(), sourceEnv.getId());
			checkRelatedPhysicalFile(pf);
		}
	}
	
	private void checkLogicalRecordDetails(LogicalRecord lrSource,
			LogicalRecord lrTarget) throws SAFRException {

		String progress = "Check Logical Record Details : " + lrSource.getName();
		this.setChanged();
        notifyObservers( progress );
		// ------------ check LR fields ------------------

		SAFRList<LRField> srcFields = lrSource.getLRFields();

		if (lrTarget == null) {
			// All source LR fields will be NEW in target env
			for (LRField srcField : srcFields) {
				srcField.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
		}
		
		if (lrTarget != null) {
			SAFRList<LRField> tgtFields = lrTarget.getLRFields();

			// Set persistency of source LR fields based on whether or not they
			// exist in the target env.
			for (LRField srcField : srcFields) {
				boolean found = false;
				for (LRField tgtField : tgtFields) {
					if (srcField.getId().equals(tgtField.getId())) {
						found = true;
						srcField.migrateToEnvironment(targetEnv.getId(),
								SAFRPersistence.OLD);
						break;
					}
				}
				if (!found) {
					srcField.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			
			// Delete any target LR fields that don't exist in source LR.
			List<LRField> deletedFields = new ArrayList<LRField>();
			for (LRField tgtField : tgtFields) {
				boolean found = false;
				for (LRField srcField : srcFields) {
					if (tgtField.getId().equals(srcField.getId())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// The target LR field does not exist in the source LR, so
					// add it to that LR and to the deleted fields list
					// so that it will be flagged for deletion when the LR is 
					// stored in the target env.
					lrSource.getLRFields().add(tgtField);
					deletedFields.add(tgtField);
				}
			}
			try {
				lrSource.removeFields(deletedFields);
			} catch (SAFRValidationException e1) {
				SAFRValidationType failureType = e1.getSafrValidationToken()
						.getValidationFailureType();
				if (failureType == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR) {
					// Set the context message then re-throw
					String msg = "Migration cancelled due to dependency errors in the target Environment."
							+ LINEBREAK + LINEBREAK
							+ "Logical Record "
							+ getDescriptor(lrSource)
							+ "exists in the source and target environments, so the target LR "
							+ "should be replaced by the source LR and any LR Fields "
							+ "which appear in the target LR but not in the source LR should be "
							+ "removed from the target environment. "
							+ "However, some of these LR Fields are already used "
							+ "by existing Lookup Paths or Views in the target environment."
							+ LINEBREAK + LINEBREAK
							+ "These dependencies (shown below) must be removed before this LR can be migrated.";
					e1.setErrorMessage(
							LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT, msg);
					msgRecorder
							.recordError(
									"LR Field Dependencies",
									e1.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT),
									e1.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP),
									e1);
					setResults(
							"LR Field Dependencies",
							msg,
							e1.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP),
							ActivityResult.FAIL, e1);
				}
				throw e1;
			}
		}
		
		//------------ end of check LR fields ------------------
		

		//------------ check LRLF associations ------------------
		
		SAFRList<ComponentAssociation> srcAssocs = lrSource
				.getLogicalFileAssociations();

		if (lrTarget == null) {
			// All source LF assocs will be NEW in target env
			for (ComponentAssociation srcAssoc : srcAssocs) {
				srcAssoc.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
		}

		if (lrTarget != null) {
			SAFRList<ComponentAssociation> tgtAssocs = lrTarget
					.getLogicalFileAssociations();

			// Set persistency of source LF assocs based on whether or not they
			// exist in the target env.
			for (ComponentAssociation srcAssoc : srcAssocs) {
				boolean found = false;
				for (ComponentAssociation tgtAssoc : tgtAssocs) {
					if (srcAssoc.getAssociatedComponentIdNum().equals(
							tgtAssoc.getAssociatedComponentIdNum())) {
						found = true;
						srcAssoc.migrateToEnvironment(targetEnv.getId(),
								SAFRPersistence.OLD);
						if (srcAssoc.getAssociationId().intValue() != tgtAssoc.getAssociationId().intValue())
						{
							existingLRLFAssocs.put(srcAssoc.getAssociationId(), tgtAssoc);
						}
						break;
					}
				}
				if (!found) {
					srcAssoc.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			
			// Delete any target LF assocs that don't exist in source LR.
			List<ComponentAssociation> deletedAssocs = new ArrayList<ComponentAssociation>();
			for (ComponentAssociation tgtAssoc : tgtAssocs) {
				boolean found = false;
				for (ComponentAssociation srcAssoc : srcAssocs) {
					if (tgtAssoc.getAssociatedComponentIdNum().equals(
							srcAssoc.getAssociatedComponentIdNum())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// The target LF assoc does not exist in the source LR, so
					// add it to that LR and to the deleted associations list
					// so that it will be flagged for deletion when the LR is 
					// stored in target env.
					lrSource.addAssociatedLogicalFile(tgtAssoc);
					deletedAssocs.add(tgtAssoc);
				}
			}
			try {
				lrSource.removeAssociatedLogicalFiles(deletedAssocs);
			} catch (SAFRValidationException e2) {
				SAFRValidationType failureType = e2.getSafrValidationToken()
						.getValidationFailureType();
				if (failureType == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR) {
					// Set the context message then re-throw
					String msg = "Migration cancelled due to dependencies in the target Environment."
							+ LINEBREAK + LINEBREAK
							+ "Logical Record "
							+ getDescriptor(lrSource)
							+ "exists in the target environment so it will be "
							+ "replaced by the source LR and any Logical File associations "
							+ "which appear in the target LR but not in the source LR should be "
							+ "removed from the target environment. "
							+ "However, some of these Logical File associations are already used "
							+ "by existing Lookup Paths or Views in the target environment."
							+ LINEBREAK + LINEBREAK
							+ "These dependencies (shown below) must be removed before this LR can be migrated.";
					e2.setErrorMessage(
							LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT,
							msg);
					msgRecorder
							.recordError(
									"Logical File Association Dependency",
									e2.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT),
									e2.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP),
									e2);
					setResults(
							"Logical File Association Dependency",
							msg,
							e2.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP),
							ActivityResult.FAIL, e2);
				}
				throw e2;
			}
		}
		//------------ end of check LRLF associations ------------------
		
		// Check each associated LF
		List<ComponentAssociation> lfAssocs = lrSource
				.getLogicalFileAssociations().getActiveItems();
		for (ComponentAssociation lfAssoc : lfAssocs) {
			
			if (relatedIdMap.get(ComponentType.LogicalFile).contains(lfAssoc.getAssociatedComponentIdNum())) {
				logger.fine("Logical File already checked " + lfAssoc.getAssociatedComponentIdNum());			
				// already checked
				continue;
			} else {
				// check it
				relatedIdMap.get(ComponentType.LogicalFile).add(lfAssoc.getAssociatedComponentIdNum());
			}
			
			LogicalFile lf = factory.getLogicalFile(
					lfAssoc.getAssociatedComponentIdNum(), sourceEnv.getId());
			checkRelatedLogicalFile(lf);
		}
		
		// Check the related Lookup exit
		UserExitRoutine uxSource = lrSource.getLookupExitRoutine();
		if (uxSource != null) {
			checkRelatedUserExitRoutine(uxSource);
		}
		
		if (lrTarget != null) {
			lrSource.setCheckLookupDependencies(true);
			lrSource.setCheckViewDependencies(true);
		}
		
	}
	
	private void checkLookupPathDetails(LookupPath lkSource,
			LookupPath lkTarget) throws SAFRException {
		
		String progress = "Check Lookup Path Details : " + lkSource.getName();
		this.setChanged();
        notifyObservers( progress );

		SAFRList<LookupPathStep> srcSteps = lkSource.getLookupPathSteps();

		// init the LRs in each step before changing the step environment
		for (LookupPathStep srcStep : srcSteps) {
			srcStep.getSourceLR(); // lazy init
			srcStep.getTargetLR(); // lazy init
		}
		
		
		// ------------ check LookupPathSteps ------------------

		if (lkTarget == null) {
			// All source LK steps will be NEW in target env
			for (LookupPathStep srcStep : srcSteps) {
				srcStep.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
				// migrate the step source fields too
				for (LookupPathSourceField srcField : srcStep.getSourceFields()) {
					srcField.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
		}
		
		if (lkTarget != null) {
			SAFRList<LookupPathStep> tgtSteps = lkTarget.getLookupPathSteps();

			// Set persistency of source LK steps based on whether or not they
			// exist in the target env.
			for (LookupPathStep srcStep : srcSteps) {
				boolean found = false;
				for (LookupPathStep tgtStep : tgtSteps) {
					if (srcStep.getId().equals(tgtStep.getId())) {
						found = true;
						srcStep.migrateToEnvironment(targetEnv.getId(),
								SAFRPersistence.OLD);
						break;
					}
				}
				if (!found) {
					srcStep.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
				// migrate the step source fields too
				// Persistence doesn't matter as always deleted/inserted, never
				// updated.
				for (LookupPathSourceField srcField : srcStep.getSourceFields()) {
					srcField.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			
			// Delete any target LK steps that don't exist in source LK.
			for (LookupPathStep tgtStep : tgtSteps) {
				boolean found = false;
				for (LookupPathStep srcStep : srcSteps) {
					if (tgtStep.getId().equals(srcStep.getId())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// The target LK step does not exist in the source LK, so
					// add it to that LK and mark it deleted
					// so that it will be deleted when the LK is
					// stored in the target env.
					lkSource.getLookupPathSteps().add(tgtStep);
					tgtStep.markDeleted();
				}
			}
		}
		
		// Check all related LRs. Note that src LR of each step is trg LR of
		// prev step. So can just check src LRs for each step plus the
		// trg LR of last step.
		
		// check the related sourceLRs in each step
		boolean firstStep = true;
        Map<Integer,ComponentAssociation> previousAss = new HashMap<Integer,ComponentAssociation>();
		for (LookupPathStep srcStep : srcSteps.getActiveItems()) {
			
            ComponentAssociation compAss = srcStep.getTargetLRLFAssociation();            
            
            if (compAss != null) {
                
                // check first source
                if (firstStep) {
                    firstStep = false;
                    LogicalRecord lr = srcStep.getSourceLR();
                    checkRelatedLogicalRecord(lr);             
                    relatedIdMap.get(ComponentType.LogicalRecord).add(lr.getId());
                }
                
                // check LR
                if (relatedIdMap.get(ComponentType.LogicalRecord).contains(compAss.getAssociatingComponentId())) {
                    logger.fine("Logical record already checked " + compAss.getAssociatingComponentId());
                } else {
                    // check it
                    LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(
                        compAss.getAssociatingComponentId(), sourceEnv.getId());
                    checkRelatedLogicalRecord(lr);             
                    relatedIdMap.get(ComponentType.LogicalRecord).add(lr.getId());
                }
    
                // check LF
                if (relatedIdMap.get(ComponentType.LogicalFile).contains(compAss.getAssociatedComponentIdNum())) {
                    logger.fine("Logical record already checked " + compAss.getAssociatedComponentIdNum());
                } else {
                    // check it
                    LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(
                        compAss.getAssociatedComponentIdNum(), sourceEnv.getId());
                    checkRelatedLogicalFile(lf);             
                    relatedIdMap.get(ComponentType.LogicalRecord).add(lf.getId());
                }
    			
    			checkAssociations(lkSource, srcStep, previousAss);
            }
		}
		// change lookup target is needed
        ComponentAssociation lpAss = lkSource.getTargetLrFileAssociation();
        if (previousAss.containsKey(lpAss.getAssociationId())) {
            lkSource.setTargetLRLFAssociation(previousAss.get(lpAss.getAssociationId()));
        }
	}

    protected void checkAssociations(LookupPath lkSource, LookupPathStep srcStep, 
        Map<Integer, ComponentAssociation> previousAss) {
        // check associations
        ComponentAssociation compAss = srcStep.getTargetLRLFAssociation();
        ComponentAssociation trgFileAssoc = SAFRAssociationFactory.getLogicalRecordToLogicalFileAssociation(
            compAss.getAssociatingComponentId(),
            compAss.getAssociatedComponentIdNum(), targetEnv.getId());
        if (trgFileAssoc != null) {
            int trgFileAssocId = trgFileAssoc.getAssociationId();    
            if (compAss.getAssociationId() != trgFileAssocId) {
                previousAss.put(compAss.getAssociationId(), trgFileAssoc);
                
                // migrate the step source fields too
                for (LookupPathSourceField srcField : srcStep.getSourceFields()) {
                    ComponentAssociation fldAss = srcField.getSourceFieldLRLFAssociation();
                    if (fldAss != null && previousAss.containsKey(fldAss.getAssociationId())) {
                        srcField.setSourceFieldLRLFAssociation(previousAss.get(fldAss.getAssociationId()));
                    }
                }
                srcStep.setTargetLRLFAssociation(trgFileAssoc);                                                
            }
        } else if (!migrateRelated) {
            String msg = getMissingAssMsg("LR-LF Association", "(" + 
                compAss.getAssociatingComponentId() + "," +
                compAss.getAssociatedComponentIdNum() + ")");
            throw new SAFRValidationException(ComponentType.LookupPath, msg);
        }
    }

	private void checkViewDetails(View srcView, View tgtView) throws SAFRException {
		String progress = "Check View Details : " + srcView.getName();
		this.setChanged();
        notifyObservers( progress );
        
        srcView.setConfirmWarningStrategy(confirmWarningStrategy);

		//need an init section like in Lookups
		SAFRList<ViewSource> viewSources = srcView.getViewSources();
		SAFRList<ViewColumn> viewColumns = srcView.getViewColumns();
		SAFRList<ViewColumnSource> viewColumnSources = srcView.getViewColumnSources();
		SAFRList<ViewSortKey> viewSortKeys = srcView.getViewSortKeys();
		HeaderFooterItems headerFooters = srcView.getHeader();
		
		ControlRecord cr = srcView.getControlRecord();
		checkRelatedControlRecord(cr);
		
		UserExitRoutine fuer = srcView.getFormatExit();
		if (fuer != null) {
			checkRelatedUserExitRoutine(fuer);				
		}
		
		UserExitRoutine wuer = srcView.getWriteExit();
		if (wuer != null) {
			checkRelatedUserExitRoutine(wuer);				
		}
		
		// ------------ check View Sources ------------------
		for(ViewSource viewSrc : viewSources)
		{			
            ComponentAssociation compAss = viewSrc.getLrFileAssociation();
            
			// check LR
            if (relatedIdMap.get(ComponentType.LogicalRecord).contains(compAss.getAssociatingComponentId())) {
				logger.fine("Logical record already checked " + compAss.getAssociatingComponentId());
			} else {
				// check it
	            LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(
	                compAss.getAssociatingComponentId(), sourceEnv.getId());
	            checkRelatedLogicalRecord(lr);             
				relatedIdMap.get(ComponentType.LogicalRecord).add(lr.getId());
			}

            // check LF
            if (relatedIdMap.get(ComponentType.LogicalFile).contains(compAss.getAssociatedComponentIdNum())) {
                logger.fine("Logical record already checked " + compAss.getAssociatedComponentIdNum());
            } else {
                // check it
                LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(
                    compAss.getAssociatedComponentIdNum(), sourceEnv.getId());
                checkRelatedLogicalFile(lf);             
                relatedIdMap.get(ComponentType.LogicalRecord).add(lf.getId());
            }
            
			// check the association
			ComponentAssociation trgFileAssoc = SAFRAssociationFactory.getLogicalRecordToLogicalFileAssociation(
                compAss.getAssociatingComponentId(),
                compAss.getAssociatedComponentIdNum(), targetEnv.getId());
            if (trgFileAssoc != null) {
                int trgFileAssocId = trgFileAssoc.getAssociationId();    
                if (compAss.getAssociationId() != trgFileAssocId) {
                    viewSrc.setLrFileAssociation(trgFileAssoc);
                }
            } else if (!migrateRelated) {
                String msg = getMissingAssMsg("LR-LF Association", "(" + 
                    compAss.getAssociatingComponentId() + "," +
                    compAss.getAssociatedComponentIdNum() + ")");
                throw new SAFRValidationException(ComponentType.LogicalRecord, msg);
            }						
		}
		
		// ---------- check View Column Sources -------------
		for(ViewColumnSource viewColSrc : viewColumnSources)
		{
			viewColSrc.getViewSource(); //lazy init
			if ( viewColSrc.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
				if (relatedIdMap.get(ComponentType.LookupPath).contains(viewColSrc.getLookupPathId())) {
					// already checked
					logger.fine("Lookup Path already checked " + viewColSrc.getLookupPathId());
					continue;
				} else {
					// check it
					relatedIdMap.get(ComponentType.LookupPath).add(viewColSrc.getLookupPathId());
				}

				LookupPath lu;
				try {
					lu = SAFRApplication.getSAFRFactory().getLookupPath(
							viewColSrc.getLookupPathId(), sourceEnv.getId());
				} catch (SAFRDependencyException sde) {
					EnvironmentalQueryBean lkBean = SAFRQuery.queryLookupPath(
							viewColSrc.getLookupPathId(), sourceEnv.getId());
					String ctxMsg = getInactiveContextMsg(
							ComponentType.LookupPath, getDescriptor(lkBean),
							SOURCE);
					sde.setContextMessage(ctxMsg);
					throw sde;
				}

				checkRelatedLookupPath(lu);
			}
			
            // Grab sort title lookup path dependencies
            Integer sktLkup = viewColSrc.getSortKeyTitleLookupPathId();              
            if (sktLkup != null && sktLkup > 0) {
                if (relatedIdMap.get(ComponentType.LookupPath).contains(sktLkup)) {
                    // already checked
                    logger.fine("Lookup Path already checked " + sktLkup);
                    continue;
                } else {
                    // check it
                    relatedIdMap.get(ComponentType.LookupPath).add(sktLkup);
                }
                LookupPath lkpf;
                try {
                    lkpf = factory.getLookupPath(sktLkup, sourceEnv.getId());
                } catch (SAFRDependencyException sde) {
                    EnvironmentalQueryBean lkBean = SAFRQuery.queryLookupPath(sktLkup, sourceEnv.getId());
                    String ctxMsg = getInactiveContextMsg(
                        ComponentType.LookupPath, getDescriptor(lkBean),SOURCE);
                    sde.setContextMessage(ctxMsg);
                    throw sde;
                }
                checkRelatedLookupPath(lkpf);               
            }
			
		}

		// Check extract file association (Output LFPF)
		FileAssociation srcFileAssoc = srcView.getExtractFileAssociation();
		if (srcFileAssoc != null) {
			int srcFileAssocId = srcFileAssoc.getAssociationId();
			if (srcFileAssocId > 0) {
				// check lf
				if (relatedIdMap.get(ComponentType.LogicalFile).contains(srcFileAssoc.getAssociatingComponentId())) {
					logger.fine("Logical File already checked " + srcFileAssoc.getAssociatingComponentId());
				} else {
					LogicalFile lf = SAFRApplication.getSAFRFactory()
							.getLogicalFile(srcFileAssoc.getAssociatingComponentId(), sourceEnv.getId());
					checkRelatedLogicalFile(lf);
				}

				// check pf
				if (relatedIdMap.get(ComponentType.PhysicalFile).contains(srcFileAssoc.getAssociatedComponentIdNum())) {
					logger.fine("Physical File already checked " + srcFileAssoc.getAssociatedComponentIdNum());
				} else {
					PhysicalFile pf = SAFRApplication.getSAFRFactory()
							.getPhysicalFile(srcFileAssoc.getAssociatedComponentIdNum(), sourceEnv.getId());
					checkRelatedPhysicalFile(pf);
				}

				// check association
				FileAssociation trgFileAssoc = SAFRAssociationFactory.getLogicalFileToPhysicalFileAssociation(
						srcFileAssoc.getAssociatingComponentId(), srcFileAssoc.getAssociatedComponentIdNum(),
						targetEnv.getId());
				if (trgFileAssoc != null) {
					int trgFileAssocId = trgFileAssoc.getAssociationId();

					// If LFPF associated in target env with a different assoc
					// ID, use the trg assoc for the src view's extract file.
					if (srcFileAssocId != trgFileAssocId) {
						srcView.setExtractFileAssociation(trgFileAssoc);
					}
				} else if (!migrateRelated) {
					// we cannot migrate the source association so we have no way of saving the view
					String msg = getMissingAssMsg("LF-PF Association", "(" + srcFileAssoc.getAssociatingComponentId()
							+ "," + srcFileAssoc.getAssociatedComponentIdNum() + ")");
					throw new SAFRValidationException(ComponentType.LogicalFile, msg);
				}
			}
		}
		
		//Make sure we drag along the items from the view dependencies
		List<ViewLogicDependency> vlds = srcView.getViewLogicDependencies();
		for(ViewLogicDependency vld : vlds){
			//need to work out what the dependency is
			//Then check the underlying component
			Integer lkup = vld.getLookupPathId();
			if (lkup != null && lkup > 0) {
				if (relatedIdMap.get(ComponentType.LookupPath).contains(lkup)) {
					// already checked
					logger.fine("Lookup Path already checked " + lkup);
					continue;
				} else {
					// check it
					relatedIdMap.get(ComponentType.LookupPath).add(lkup);
				}
				
				LookupPath lkpf;
				try {
					lkpf = factory.getLookupPath(
							lkup, sourceEnv.getId());
				} catch (SAFRDependencyException sde) {
					EnvironmentalQueryBean lkBean = SAFRQuery.queryLookupPath(
							lkup, sourceEnv.getId());
					String ctxMsg = getInactiveContextMsg(
							ComponentType.LookupPath, getDescriptor(lkBean),
							SOURCE);
					sde.setContextMessage(ctxMsg);
					throw sde;
				}
				checkRelatedLookupPath(lkpf);				
			}
				
			Integer exitId = vld.getUserExitRoutineId();
			if (exitId != null && exitId > 0) {
				UserExitRoutine uer = factory.getUserExitRoutine(exitId, sourceEnv.getId());
				checkRelatedUserExitRoutine(uer);				
			}
				
			Integer fileAssId = vld.getFileAssociationId();
			if (fileAssId != null && fileAssId > 0) {
				FileAssociation fileAss = SAFRAssociationFactory
						.getLogicalFileToPhysicalFileAssociation(fileAssId,
								sourceEnv.getId());
				
				if (relatedIdMap.get(ComponentType.LogicalFile).contains(fileAss.getAssociatingComponentId())) {
					logger.fine("Logical File already checked " + fileAss.getAssociatingComponentId());			
					// already checked
				} else {
					// check it
					relatedIdMap.get(ComponentType.LogicalFile).add(fileAss.getAssociatingComponentId());
					LogicalFile logf = factory.getLogicalFile(
							fileAss.getAssociatingComponentId(), sourceEnv.getId());
					checkRelatedLogicalFile(logf);
				}				
			}
		}
		
		
		if (tgtView == null){
			// All source LK steps will be NEW in target env
			for(ViewSource viewSrc : viewSources) {
				viewSrc.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
			for(ViewColumn viewCol : viewColumns) {
				viewCol.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
			for(ViewColumnSource viewColSrc : viewColumnSources) {
				viewColSrc.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
			for(ViewSortKey viewSK : viewSortKeys) {
				viewSK.migrateToEnvironment(targetEnv.getId(),
						SAFRPersistence.NEW);
			}
		}
		else {
			SAFRList<ViewSource> tgtViewSources = tgtView.getViewSources();

			// View Sources
			
			// Set persistency of source ViewSources based on whether or not they
			// exist in the target env.
			for(ViewSource srcViewSrc : viewSources) {
				//is the source in the target env?
				boolean found = false;
				for(ViewSource tgtViewSrc : tgtViewSources) {
					if (srcViewSrc.getId().equals(tgtViewSrc.getId())) {
						found = true;
						srcViewSrc.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.OLD);
						break;
					}
				}
				if (found == false)
				{
					srcViewSrc.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			// Delete any target Sources that don't exist in source View.
			for(ViewSource tgtViewSrc : tgtViewSources) {
				//is the source in the target env?
				boolean found = false;
				for(ViewSource srcViewSrc : viewSources) {
					if (srcViewSrc.getId().equals(tgtViewSrc.getId())) {
						found = true;
						break;
					}
				}
				if (found == false)
				{
					srcView.getViewSources().add(tgtViewSrc);
					tgtViewSrc.markDeleted();
				}
			}
			
			// View Columns
			SAFRList<ViewColumn> tgtViewColumns = tgtView.getViewColumns();
			
			// Set persistency of source ViewSources based on whether or not they
			// exist in the target env.
			for(ViewColumn srcViewCol : viewColumns) {
				//is the source in the target env?
				boolean found = false;
				for(ViewColumn tgtViewCol : tgtViewColumns) {
					if (srcViewCol.getId().equals(tgtViewCol.getId())) {
						found = true;
						srcViewCol.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.OLD);
						break;
					}
				}
				if (found == false)
				{
					srcViewCol.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			// Delete any target Sources that don't exist in source View.
			for(ViewColumn tgtViewCol : tgtViewColumns) {
				//is the source in the target env?
				boolean found = false;
				for(ViewColumn srcViewSrc : viewColumns) {
					if (srcViewSrc.getId().equals(tgtViewCol.getId())) {
						found = true;
						break;
					}
				}
				if (found == false)
				{
					srcView.getViewColumns().add(tgtViewCol);
					tgtViewCol.markDeleted();
				}
			}
			
			// View Column Sources
			SAFRList<ViewColumnSource> tgtViewColSources = tgtView.getViewColumnSources();
			
			// Set persistency of source ViewSources based on whether or not they
			// exist in the target env.
			for(ViewColumnSource srcViewColSrc : viewColumnSources) {
				//is the source in the target env?
				boolean found = false;
				for(ViewColumnSource tgtViewColSrc : tgtViewColSources) {
					if ( srcViewColSrc.getId().equals(tgtViewColSrc.getId()) ) {
						found = true;
						srcViewColSrc.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.OLD);
						break;
					}
				}
				if (found == false)
				{
					srcViewColSrc.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
				
			}
			// Delete any target Sources that don't exist in source View.
			for(ViewColumnSource tgtViewColSrc : tgtViewColSources) {
				//is the source in the target env?
				boolean found = false;
				for(ViewColumnSource srcViewColSrc : viewColumnSources) {
					if (srcViewColSrc.getId().equals(tgtViewColSrc.getId())) {
						found = true;
						break;
					}
				}
				if (found == false)
				{
					srcView.getViewColumnSources().add(tgtViewColSrc);
					tgtViewColSrc.markDeleted();
				}
			}
			
			
			// View Sort Keys
			SAFRList<ViewSortKey> tgtViewSortKeys = tgtView.getViewSortKeys();
			
			// Set persistency of source keys based on whether or not they
			// exist in the target env.
			for(ViewSortKey srcViewSK : viewSortKeys) {
				//is the source in the target env?
				boolean found = false;
				for(ViewSortKey tgtViewSK : tgtViewSortKeys) {
					if ( srcViewSK.getId().equals(tgtViewSK.getId()) ) {
						found = true;
						srcViewSK.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.OLD);
						break;
					}
				}
				if (found == false)
				{
					srcViewSK.migrateToEnvironment(targetEnv.getId(),
							SAFRPersistence.NEW);
				}
			}
			// Delete any target keys that don't exist in source View.
			for(ViewSortKey tgtViewSK : tgtViewSortKeys) {
				//is the source in the target env?
				boolean found = false;
				for(ViewSortKey srcViewSK : viewSortKeys) {
					if (srcViewSK.getId().equals(tgtViewSK.getId())) {
						found = true;
						break;
					}
				}
				if (found == false)
				{
					srcView.getViewSortKeys().add(tgtViewSK);
					tgtViewSK.markDeleted();
				}
			}
		}
		
		// Header footers are simply replaced
		if (headerFooters != null) {
            for(HeaderFooterItem hf : headerFooters.getItems()) {
                hf.migrateToEnvironment(targetEnv.getId(),
                        SAFRPersistence.NEW);
            }           
		}
	}
	
	private void checkViewFolderDetails(ViewFolder srcViewFolder) throws SAFRException {
		String progress = "Check View Folder Details : " + srcViewFolder.getName();
		this.setChanged();
        notifyObservers( progress );
        
        // deal with associations
        List<ViewQueryBean> srcViewBeans = SAFRQuery.queryAllViews(sourceEnv.getId(),
            srcViewFolder.getId(), SortType.SORT_BY_NAME);

        List<ViewQueryBean> activeViews = new ArrayList<ViewQueryBean>();
        List<ViewQueryBean> inactiveViews = new ArrayList<ViewQueryBean>();
        List<Integer> srcViewIDs = new ArrayList<Integer>();
        Set<Integer> actViewIDs = new HashSet<Integer>();
        for (ViewQueryBean viewBean : srcViewBeans) {
            if (ACTIVE.equals(viewBean.getStatus())) {
                activeViews.add(viewBean);
                actViewIDs.add(viewBean.getId());
            } else {
                inactiveViews.add(viewBean);
            }
            srcViewIDs.add(viewBean.getId());
        }
                
        //find associated source Views that exist in the target env
        Set<Integer> targExistViews = new HashSet<Integer>();       
        for(ViewQueryBean viewBean: srcViewBeans) {
            ViewQueryBean bean = SAFRQuery.queryView(targetEnv.getId(), viewBean.getId());
            if (bean != null) {
                targExistViews.add(bean.getId());
            }
            else if (migrateRelated && actViewIDs.contains(viewBean.getId())) {
                targExistViews.add(viewBean.getId());
            }
        }       
        checkRelatedVFVAssoc(srcViewFolder, targExistViews);
                
        if (!migrateRelated) {
        	// views in VF will not be migrated
        	return;
        }
		
		if (!inactiveViews.isEmpty()) {
			// warn that inactive views will not be migrated
			List<DependencyData> inactVws = new ArrayList<DependencyData>();
			DependencyData dd = new DependencyData("View");
			for (ViewQueryBean vqbean : inactiveViews) {
				ComponentData cd = new ComponentData();
				cd.setComponentName(getDescriptor(vqbean));
				cd.setOverwrite(false);
				dd.addComponent(cd);
			}
			inactVws.add(dd);

			if (!confirmWarning(
							"View migration warning",
							"These inactive Views in the source View Folder will not be migrated:",
							inactVws)) {
				setResults(
						"Migration cancelled on inactive Views warning.",
						"These inactive Views in the source View Folder will not be migrated:",
						formatDependencies(inactVws),
						ActivityResult.CANCEL, null);
				throw new SAFRCancelException(
						"Migration cancelled on warning about inactive Views not migrated.");
			}
		}

		//check the active views to be migrated
		for(ViewQueryBean viewBean: activeViews) {
			View view;
			try {
				view = factory.getView(viewBean.getId(), sourceEnv.getId());
			} catch (SAFRDependencyException sde) {
				String ctxMsg = getInactiveContextMsg(ComponentType.View,
						getDescriptor(viewBean), SOURCE);
				sde.setContextMessage(ctxMsg);
				throw sde;
			}
			checkRelatedView(view, srcViewFolder);
		}
		
	}

	private void checkRelatedVFVAssoc(ViewFolder srcViewFolder, Set<Integer> targExistViews) {
	    
        ViewFolder tgtViewFolder = null;
        try {
            tgtViewFolder = factory.getViewFolder(srcViewFolder.getId(), targetEnv.getId());
        } catch (SAFRException e) {
        } 
	    
        SAFRList<ViewFolderViewAssociation> srcAssocs = srcViewFolder.getViewAssociations();

        // if view folder is "ALL_VIEWS" then we don't have to worry about associations        
        if (srcViewFolder.getId() == 0) {
            @SuppressWarnings("unchecked")
            ArrayList<ViewFolderViewAssociation> csrcAssocs = 
                (ArrayList<ViewFolderViewAssociation>) srcAssocs.clone();
            for (ViewFolderViewAssociation assoc : csrcAssocs) {
                srcAssocs.flush(assoc);
            }
        } else if (tgtViewFolder == null) {
            // All source View assocs will be NEW in target env
            for (ViewFolderViewAssociation srcAssoc : srcAssocs) {
                if (targExistViews.contains(srcAssoc.getAssociatedComponentIdNum())) {
                    srcAssoc.migrateToEnvironment(targetEnv.getId(), SAFRPersistence.NEW);
                }
            }
        }
        else {
            SAFRList<ViewFolderViewAssociation> tgtAssocs = tgtViewFolder.getViewAssociations();
            
            // If a source View assoc already exists in the target env use the
            // OLD target assoc, else use the NEW source assoc.
            List<ViewFolderViewAssociation> flushed = new ArrayList<ViewFolderViewAssociation>();
            List<ViewFolderViewAssociation> added = new ArrayList<ViewFolderViewAssociation>();
            for (ViewFolderViewAssociation srcAssoc : srcAssocs) {
                boolean found = false;
                for (ViewFolderViewAssociation tgtAssoc : tgtAssocs) {
                    if (srcAssoc.getAssociatedComponentIdNum().equals(
                            tgtAssoc.getAssociatedComponentIdNum())) {
                        flushed.add(srcAssoc);
                        added.add(tgtAssoc);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (targExistViews.contains(srcAssoc.getAssociatedComponentIdNum())) {
                        srcAssoc.migrateToEnvironment(targetEnv.getId(),SAFRPersistence.NEW);
                    }
                }
            }
            // Replace src assocs with any existing trg assocs
            for (ViewFolderViewAssociation assoc : flushed) {
                srcAssocs.flush(assoc);
            }
            for (ViewFolderViewAssociation assoc : added) {
                srcAssocs.add(assoc);
            }
            
            // Delete target View assocs that don't exist in source VF.
            for (ViewFolderViewAssociation tgtAssoc : tgtAssocs) {
                boolean found = false;
                for (ViewFolderViewAssociation srcAssoc : srcAssocs) {
                    if (tgtAssoc.getAssociatedComponentIdNum().equals(
                            srcAssoc.getAssociatedComponentIdNum())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    srcViewFolder.addAssociatedView(tgtAssoc);
                    srcViewFolder.removeViewAssociation(tgtAssoc);
                } 
            }             
        } 
	}
	
	private void confirmMigration() throws SAFRCancelException {
		StringBuffer header = new StringBuffer();
		header.append("The following component(s) will be migrated. ");
		header.append("Red items marked with '*' exist in the target environment and will be overwritten.");
		
		Set<ComponentType> keySet = compMap.keySet();
		Integer size = keySet.size();
		ComponentType[] keys = keySet.toArray(new ComponentType[size]);
		
		// print comp types in reverse of map order
		List<DependencyData> details = new ArrayList<DependencyData>();
		
		for (int i =  size-1; i>=0; i--) {
			ComponentType compType = keys[i];
			if (!compMap.get(compType).isEmpty()) {
				DependencyData dd = new DependencyData(compType.getLabel());
				for (SAFREnvironmentalComponent comp : compMap.get(compType)) {
					ComponentData cd = new ComponentData();
					cd.setComponentName(getDescriptor(comp)
							+ (comp.isPersistent() ? "*" : ""));
					cd.setOverwrite(comp.isPersistent());
					dd.addComponent(cd);
				}
				details.add(dd);
			}
		}

		if (!confirmWarning("Confirm Migration",
				header.toString(), details)) {
			setResults("Migration cancelled at confirmation.",
					header.toString(), formatDependencies(details),
					ActivityResult.CANCEL, null);
			throw new SAFRCancelException(
					"Migration cancelled on warning to confirm the migration.");
		}
	}
	
	/*
	 * Concatentate the name and ID in square brackets.
	 */
	private String getDescriptor(SAFRComponent comp) {
		return comp.getName() + "[" + comp.getId() + "]";
	}
	
	private String getDescriptor(NumericIdQueryBean comp) {
		return comp.getName() + "[" + comp.getId() + "]";
	}
	
	private String getMissingCompMsg(ComponentType compType, String compDesc) throws SAFRException {
		StringBuffer msg = new StringBuffer();
		msg.append("Related components that are not migrated must exist in the target Environment ");
		msg.append("but the related ");
		msg.append(compType.getLabel());
		msg.append(" ");
		msg.append(compDesc);
		msg.append(" does not exist there.");

		if (migrateRelated && !SAFRApplication.getUserSession().isAdminOrMigrateInUser(targetEnv.getId())) {
			if (compType == ComponentType.ControlRecord
					|| compType == ComponentType.LookupPath) {
				msg.append(LINEBREAK + LINEBREAK + "It will not be migrated with the selected View as the login Group");
				msg.append(" does not have Admin rights or Migrate-In permission in the target Environment.");
			}
		}

		return msg.toString();
	}

    private String getMissingAssMsg(String assType, String compDesc) throws SAFRException {
        StringBuffer msg = new StringBuffer();
        msg.append("Related associations that are not migrated must exist in the target Environment ");
        msg.append("but the related ");
        msg.append(assType);
        msg.append(" ");
        msg.append(compDesc);
        msg.append(" does not exist there.");
        return msg.toString();
    }
	
	private String getInactiveContextMsg(ComponentType compType, String compDesc, String environRole) {
		StringBuffer msg = new StringBuffer();
		msg.append("The ");
		msg.append(compType.getLabel());
		msg.append(" ");
		msg.append(compDesc);
		msg.append(" in the ");
		msg.append(environRole);
		msg.append(" Environment cannot be loaded because of Inactive components shown below.");
		msg.append(" Activate these components first");
		if(environRole.equals(SOURCE)) {
			msg.append(".");
		} else {
			msg.append(" or delete the ");
			msg.append(compType.getLabel());
			msg.append(" from the target Environment.");
		}
		
		return msg.toString();
	}

	/*
	 * This method checks that ordinary users have edit rights on all related
	 * components. It reuses the technique used in the export utility, which is
	 * to retrieve transfer objects for all related components then check the
	 * edit rights for each one.
	 * 
	 * Throws SAFRDependencyException if user has no edit rights on related
	 * components.
	 */
	private void checkEditRightsRelatedComponents() throws SAFRException {
		
		if (!migrateRelated || SAFRApplication.getUserSession().isSystemAdministrator()
				|| componentType == ComponentType.ControlRecord
				|| componentType == ComponentType.UserExitRoutine) {
			// nothing to check
			return;
		}

		String progress = "Check edit rights on related components";
		this.setChanged();
        notifyObservers( progress );
        
		Map<ComponentType, List<DependentComponentTransfer>> depComponentTransferMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();
		try {
			if (componentType != ComponentType.ViewFolder) {
				// retrieve related components
				depComponentTransferMap = DAOFactoryHolder
						.getDAOFactory()
						.getExportDAO()
						.getComponentDependencies(this.componentType,
								this.component.getId(), this.sourceEnv.getId());
			} else {
				// retrieve related components of Views in the VF
				depComponentTransferMap = checkEditRightsFolderRelatedViews();
			}
		} catch (DAOException e1) {
			String msg = "Database error during migration while checking user edit rights on the related components of "
					+ this.componentType.getLabel()
					+ " "
					+ getDescriptor(component) 
					+ ". See log file.";
			logger.log(Level.SEVERE, msg, e1);
			throw new SAFRException(msg, e1);
		}

		Map<ComponentType, List<DependentComponentTransfer>> noRightsMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();

		// loop through the map
		for (ComponentType compType : depComponentTransferMap.keySet()) {
			if (compType == ComponentType.ControlRecord || 
			    compType == ComponentType.ViewFolder) {
				// edit rights don't apply to Server, CR and GF
				// VF is never a related component
				continue;
			}
			List<DependentComponentTransfer> depCompTransList = depComponentTransferMap
					.get(compType);
			for (DependentComponentTransfer depCompTransfer : depCompTransList) {
				// add component to temp map if user has no edit rights
				if (depCompTransfer.getEditRights() == EditRights.None) {
					if (noRightsMap.containsKey(compType)) {
						noRightsMap.get(compType).add(depCompTransfer);
					} else {
						List<DependentComponentTransfer> depCompList = new ArrayList<DependentComponentTransfer>();
						depCompList.add(depCompTransfer);
						noRightsMap.put(compType, depCompList);
					}
				}
			}
		}

		if (!noRightsMap.isEmpty()) {
			SAFRDependencyException sde = new SAFRDependencyException(
					noRightsMap);
			String msg = "The login Group must have at least Read rights on all related components"
					+ " but has no edit rights on the following components:";
			sde.setContextMessage(msg);
			throw sde;
		}
	}
	
	private Map<ComponentType, List<DependentComponentTransfer>> checkEditRightsFolderRelatedViews()
			throws DAOException {

		Map<ComponentType, List<DependentComponentTransfer>> oneViewCompMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();
		Map<ComponentType, Map<Integer, DependentComponentTransfer>> allViewsCompMap = new LinkedHashMap<ComponentType, Map<Integer, DependentComponentTransfer>>();
		Map<ComponentType, List<DependentComponentTransfer>> depCompMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();

		// check all Views in the VF
		List<ViewQueryBean> vBeans = SAFRQuery.queryAllViews(sourceEnv.getId(),
				component.getId(), SortType.SORT_BY_ID);

		for (ViewQueryBean vBean : vBeans) {
			// retrieve related components for the view
			oneViewCompMap = DAOFactoryHolder
					.getDAOFactory()
					.getExportDAO()
					.getComponentDependencies(ComponentType.View,
							vBean.getId(), sourceEnv.getId());

			// accumulate the comps, eliminating duplicates
			for (ComponentType compType : oneViewCompMap.keySet()) {
				List<DependentComponentTransfer> relatedComps = oneViewCompMap
						.get(compType);
				for (DependentComponentTransfer relatedComp : relatedComps) {
					if (relatedComp.getEditRights() == EditRights.None) {
						if (allViewsCompMap.containsKey(compType)) {
							allViewsCompMap.get(compType).put(
									relatedComp.getId(), relatedComp);
						} else {
							Map<Integer, DependentComponentTransfer> compMap = new LinkedHashMap<Integer, DependentComponentTransfer>();
							compMap.put(relatedComp.getId(), relatedComp);
							allViewsCompMap.put(compType, compMap);
						}
					}
				}
			}
		}

		// return comps in the required Map format
		for (Entry<ComponentType, Map<Integer, DependentComponentTransfer>> entry : allViewsCompMap
				.entrySet()) {
			depCompMap.put(entry.getKey(),
					new ArrayList<DependentComponentTransfer>(entry.getValue()
							.values()));
		}
		return depCompMap;
	}
	
	private void showPreValidationWarnings() throws SAFRCancelException {
		// Show warnings about PF associations to be deleted in the target env
		for (Entry<String, List<String>> entry : deletedPFAssociationMsgs.entrySet()) {
			String lfDescriptor = entry.getKey();
			List<String> pfDependencyMsgs = entry.getValue();
			String dependencyMsg = "";
			String lineFeed = "";
			for (String depMsg : pfDependencyMsgs) {
				dependencyMsg += lineFeed;
				dependencyMsg += depMsg;
				lineFeed = LINEBREAK;
			}
			// PF dependency warning
			String contextMsg = "When Logical File '" + lfDescriptor
					+ "' is overwritten in the target environment "
					+ "the following Physical Files will no longer "
					+ "be associated with it because they were not "
					+ "associated with the file in the source "
					+ "environment and Views which use these for "
					+ "Output will be made Inactive.";
			if (!confirmWarning("Physical File dependency warning", contextMsg,
					dependencyMsg)) {
				setResults(
						"Migration cancelled on Physical File dependency warning.",
						contextMsg, dependencyMsg, ActivityResult.CANCEL, null);
				throw new SAFRCancelException(
						"Migration cancelled on warning about Views dependent on Physical File");
			}
			
		}
		
	}
	
	public void addUserExitViewDependency(String exitDescriptor, DependentComponentTransfer viewDependency) {
		if (userExitViewDependencies.containsKey(exitDescriptor)) {
			userExitViewDependencies.get(exitDescriptor).add(viewDependency);
		} else {
			List<DependentComponentTransfer> viewDeps = new ArrayList<DependentComponentTransfer>();
			viewDeps.add(viewDependency);
			userExitViewDependencies.put(exitDescriptor, viewDeps);
		}
	}
	
	private boolean confirmWarning(String topic, String contextMsg,
			String detailMsg) {

		msgRecorder.recordWarning(topic, contextMsg, detailMsg);

		return confirmWarningStrategy.confirmWarning(topic, contextMsg,
				detailMsg);
	}

	private boolean confirmWarning(String topic, String contextMsg,
			List<DependencyData> dependencyList) {

		String detailMsg = formatDependencies(dependencyList);
		SAFRLogger.logAll(logger, Level.INFO, "Dependencies " + SAFRUtilities.LINEBREAK + detailMsg);
		msgRecorder.recordWarning(topic, contextMsg, detailMsg);

		// use the following dep list signature, not the msg string signature,
		// to ensure overwritten components are highlighted correctly
		return confirmWarningStrategy.confirmWarning(topic, contextMsg,
				dependencyList);
	}
	
	private String formatDependencies(List<DependencyData> dependencyList) {
		String detailMsg = "";
		for (DependencyData dat : dependencyList) {
			detailMsg += dat.getComponentTypeName() + LINEBREAK;
			for (ComponentData cdat : dat.getComponentDataList()) {
				detailMsg += "\t" + cdat.getComponentName() + LINEBREAK;
			}
		}
		return detailMsg;
	}
	
	private void setResults(String topic, String shortMsg,
			String dependencyMsg, ActivityResult result, SAFRException se) {
		migComponent.setMsgTopic(topic);
		migComponent.setMainMsg(shortMsg);
		migComponent.setDependencyMsg(dependencyMsg);
		if (result != null) {
			migComponent.setResult(result); // defaults to FAIL
		}
		if (se != null) {
			migComponent.setException(se);
		}
	}
		
	
	private class MessageRecorder {
		
		final private String warningBanner =    LINEBREAK + "Migration Warnings" + LINEBREAK;
		final private String errorBanner =      LINEBREAK + "Migration stopped on error.";
		final private String cancelBanner =     LINEBREAK + "Migration cancelled on warning.";
		final private String completionBanner = "Migration completed.";
		private List<String> warnings = new ArrayList<String>();
		private List<String> errors = new ArrayList<String>();
		boolean migrationCompleted = true;
		
		private void recordWarning(String topic, String contextMsg, String detailMsg) {
			warnings.add(topic);
			if (contextMsg != null && contextMsg != "") {
				warnings.add(LINEBREAK + contextMsg);
			}
			if (detailMsg != null && detailMsg != "") {
				warnings.add(LINEBREAK + detailMsg);
			}
			warnings.add(LINEBREAK);
		}
		
		private void recordCancelled(String message) {
			migrationCompleted = false;
			warnings.add(cancelBanner);
			if (message != null && message != "") {
				warnings.add(message);
			}
			warnings.add(LINEBREAK);
		}
		
		private void recordError(String topic, String contextMsg, String detailMsg) {
			recordError(topic, contextMsg, detailMsg, null);
		}
		
		private void recordError(String topic, String contextMsg,
				String detailMsg, SAFRValidationException sve) {
			errors.add(topic);
			if (contextMsg != null && contextMsg != "") {
				errors.add(LINEBREAK + contextMsg);
			}
			if (detailMsg != null && detailMsg != "") {
				errors.add(LINEBREAK + detailMsg);
			}
			errors.add(LINEBREAK);
			if (sve != null) {
				recordedSVEs.add(sve);
			}
		}
		
		private void writeMessages() {
			if (!warnings.isEmpty()) {
				StringBuffer buffer = new StringBuffer();
				for (String line : warnings) {
					buffer.append(line);
				}
				SAFRLogger.logAll(logger, Level.WARNING, warningBanner.substring(2) + buffer.toString());
			}
			
			if (!errors.isEmpty()) {
				migrationCompleted = false;
				StringBuffer buffer = new StringBuffer();
				for (String line : errors) {
					buffer.append(line);
				}
				SAFRLogger.logAll(logger, Level.SEVERE, errorBanner.substring(2) + buffer.toString());
			}
			
			if (migrationCompleted) {
			    SAFRLogger.logAll(logger, Level.INFO, completionBanner);				
			}
		}
	}
	
}
