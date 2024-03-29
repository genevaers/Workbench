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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.ControlRecordTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFREnvironmentalComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewFactory;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;

public class ViewImporter extends LookupPathImporter implements
		LRInformationProvider {
	
	// CQ10049 refactored variables
	protected ViewRecordParser viewParser;
	protected ViewSourceRecordParser viewSrcParser;
	protected ViewColumnRecordParser viewColParser;
	protected ViewColumSourceRecordParser viewColSrcParser;
	protected ViewSortKeyRecordParser viewSortKeyParser;
	protected ViewHeaderFooterRecordParser viewHeaderFooterParser;
	protected CRRecordParser crParser;
	protected List<ControlRecord> crs;
    protected List<View> views;
	
	public ViewImporter(ImportUtility importUtility) {
		super(importUtility);
	}

	@Override
	protected void doImport() throws SAFRException, XPathExpressionException {
		SAFRValidationException sve = new SAFRValidationException();
		
		clearMaps();

		parseRecords();

		// Check that View(s) was found
		if (!records.containsKey(ViewTransfer.class)) {
			sve.setErrorMessage(getCurrentFile().getName(),
					"There are no <View> <Record> elements.");
			throw sve;
		}

		// Check for orphaned foreign keys and unreferenced primary keys
		checkReferentialIntegrity();

        // if generated by MR91 generate redefines
        GenerationTransfer genTran = (GenerationTransfer)records.get(GenerationTransfer.class).get(0);
        if (genTran.getProgram().equalsIgnoreCase("MR91")) {
            generateOrdPos();
            generateRedefine();
            generateSources();
        }
        		
		// Check if imported components already exist in DB
		checkDuplicateIds();

		// Check for import IDs > next key IDs (out of range)
		checkOutOfRangeIds();

		if (duplicateIdMap.size() > 0) {
			issueDuplicateIdsWarning();
		}

		checkAssociationsAndSubComponents();
		checkAssociationsWithDifferentId();
		
		// create and validate model objects.
		uxrs = createUserExitRoutines();
		pfs = createPhysicalFiles();
		lfs = createLogicalFiles();
		lrs = createLogicalRecords();
		lks = createLookupPaths();
        crs = createControlRecords();
		views = createViews();

        // check LR-LF associations with negative ID's. These come from MR91 generated WE xml 
        // and require us to generate new id's for them
        fixupNegativeIds();        
		
		// store all model objects within a DB transaction
		boolean success = false;
		while (!success) {
			try {
				// Begin Transaction
				DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
				DAOFactoryHolder.getDAOFactory().getDAOUOW().multiComponentScopeOn();
				SAFRApplication.getTimingMap().startRecording();	
                SAFRApplication.getModelCount().restartCount();
				
				for (ControlRecord cr : crs) {
					cr.store();
				}
				for (UserExitRoutine uxr : uxrs.values()) {
					uxr.store();
				}
				for (PhysicalFile pf : pfs) {
					pf.store();
				}
				for (LogicalFile lf : lfs) {
					lf.store();
				}
				for (LogicalRecord lr : lrs) {
					lr.store();
				}
				for (LookupPath lookup : lks) {
					lookup.store();
				}
				for (View view : views) {
					view.store();
				}
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

    protected void fixupNegativeIds() {
        super.fixupNegativeIds();
        
        for (View view : views) {
            
            // loop sources 
            for (ViewSource src : view.getViewSources()) {                
                if (lrlfFixup.containsKey(src.getLrFileAssociationId())) {
                    src.setLrFileAssociationId(
                        lrlfFixup.get(src.getLrFileAssociationId()));
                }
            }
        }
    }
        
    protected void generateSources() {
        for (SAFRTransfer trans : records.get(ViewSourceTransfer.class).values()) {
            convertViewSource((ViewSourceTransfer) trans);
        }	    
	}
	
    protected void convertViewSource(ViewSourceTransfer src) {
        
        // check if already converted
        if (!src.getExtractRecordOutput().isEmpty() || src.isExtractOutputOverride()) {
            return;
        }
        
        // check for existing WRITE
        Map<Integer, SAFRTransfer> colSrcs = records.get(ViewColumnSourceTransfer.class);
        
        boolean writeExists = false;
        if (colSrcs != null) {
            for (SAFRTransfer trans : colSrcs.values()) {
                ViewColumnSourceTransfer colSrc = (ViewColumnSourceTransfer)trans;
                if (colSrc.getViewId().equals(src.getViewId())) {
                    if (isWriteStatement(colSrc.getExtractColumnLogic())) {
                        writeExists = true;
                        break;
                    }
                }
            }
        }
        if (writeExists) {
            src.setExtractFileAssociationId(0);
            src.setWriteExitId(0);
            src.setWriteExitParams("");
            src.setExtractRecordOutput("");
            src.setExtractOutputOverride(true);
        }
        else {
            generateSourceOutputLogic(src);            
        }
        
    }
    
    protected boolean isWriteStatement(String logic) {
        String[] lines = logic.split("\\n");
        for (String line : lines) {
            line = removeComments(line);
            line = removeStrings(line);
            line = line.trim().toUpperCase();
            if (line.matches(".*WRITE\\s*\\(.*\\).*")) {
                return true;
            }
        }
        return false;
    }

    private String removeComments(String input) {
        Pattern commentPattern = Pattern.compile("(.*?)(\\\\|').*");
        Matcher nonComment = commentPattern.matcher(input);
        if (nonComment.find()) {
            return nonComment.group(1);            
        } 
        else {
            return input;
        }
    }
    
    private String removeStrings(String input) {
        String removed = input;
        Pattern stringPattern = Pattern.compile("(.*)\".*?\"(.*)");
        Matcher remStr = stringPattern.matcher(removed);
        while (remStr.find()) {
            removed =  remStr.group(1) + remStr.group(2);     
            remStr = stringPattern.matcher(removed);
        } 
        return removed;
    }
    
    protected void generateSourceOutputLogic(ViewSourceTransfer src) {
        
        ViewTransfer viewTrans = (ViewTransfer) (records.get(ViewTransfer.class)).get(src.getViewId());
        
        // calculate WRITE logic 
        String writeLogic = "";
        boolean isFormatPhase = !(viewTrans.getTypeCode().equals("EXTR") || viewTrans.getTypeCode().equals("COPY"));  
        if (isFormatPhase) {
            Integer workFileNo = viewTrans.getWorkFileNumber();
            writeLogic = "WRITE(SOURCE=VIEW,DEST=EXT=" + String.format("%03d",workFileNo) + ")";
        } else if (viewTrans.getTypeCode().equals("COPY")) {
            if (viewTrans.getExtractFileAssocId() == null || viewTrans.getExtractFileAssocId()==0) { 
                writeLogic = "WRITE(SOURCE=INPUT,DEST=DEFAULT)";
            } else {
                String writeParm = getWriteParm(viewTrans);                
                writeLogic = "WRITE(SOURCE=INPUT,"+ writeParm +")";
            }    
        } else if (viewTrans.getTypeCode().equals("EXTR")) {
            if (viewTrans.getExtractFileAssocId() == null || viewTrans.getExtractFileAssocId()==0) { 
                writeLogic = "WRITE(SOURCE=DATA,DEST=DEFAULT)";
            } else {
                String writeParm = getWriteParm(viewTrans);                
                writeLogic = "WRITE(SOURCE=DATA,"+ writeParm +")";
            }    
        }        
        src.setExtractFileAssociationId(viewTrans.getExtractFileAssocId());
        src.setWriteExitId(viewTrans.getWriteExitId());
        src.setWriteExitParams(viewTrans.getWriteExitParams());
        src.setExtractRecordOutput(writeLogic);
        src.setExtractOutputOverride(false);
    }

    protected String getWriteParm(ViewTransfer viewTrans) {
        
        FileAssociationTransfer assoc = (FileAssociationTransfer) 
            (records.get(FileAssociationTransfer.class)).get(viewTrans.getExtractFileAssocId());
        
        LogicalFileTransfer lfTrans = (LogicalFileTransfer) 
            (records.get(LogicalFileTransfer.class)).get(assoc.getAssociatingComponentId());

        PhysicalFileTransfer pfTrans = (PhysicalFileTransfer) 
            (records.get(PhysicalFileTransfer.class)).get(assoc.getAssociatedComponentId());
        
        String lfName = lfTrans.getName();
        String pfName = pfTrans.getName();
        String result = "DEST=FILE={" + lfName + "." + pfName + "}";
        
        if (viewTrans.getWriteExitId() != null && viewTrans.getWriteExitId() > 0) {
            UserExitRoutineTransfer userExittrans = DAOFactoryHolder.getDAOFactory().getUserExitRoutineDAO().getUserExitRoutine(
                viewTrans.getWriteExitId(), viewTrans.getEnvironmentId());
            String exitArg =  "{" + userExittrans.getName() + "}";            
            if (viewTrans.getWriteExitParams() != null && viewTrans.getWriteExitParams().length() > 0) {
                String parmArg = ",\"" + viewTrans.getWriteExitParams() + "\"";
                exitArg = ",USEREXIT=(" + exitArg + parmArg + ")";
            } else {
                exitArg = ",USEREXIT=" + exitArg;
            }            
            result += exitArg;
        }
        return result;
    }
	
    protected List<View> createViews() {
        Map<Integer, SAFRTransfer> map;

		// Create model objects for View subcomponents
		
		// Create a map of view sources from XML keyed on View id
		map = records.get(ViewSourceTransfer.class);
		Map<Integer, List<ViewSourceTransfer>> viewSourcesMap = new HashMap<Integer, List<ViewSourceTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				ViewSourceTransfer trans = (ViewSourceTransfer) tfr;
				if (trans.getId() > 0) {
					Integer viewId = trans.getViewId();
					// add View Source transfer to map
					if (viewSourcesMap.containsKey(viewId)) {
						viewSourcesMap.get(viewId).add(trans);
					} else {
						List<ViewSourceTransfer> viewSources = new ArrayList<ViewSourceTransfer>();
						viewSources.add(trans);
						viewSourcesMap.put(viewId, viewSources);
					}
				}
			}
		}

		// Create a map of view sources in the target env that will be
		// deleted because they do not exist in the XML
		Map<Integer, List<ViewSource>> deletedViewSources = new HashMap<Integer, List<ViewSource>>();
		if (map != null) {
			if (existingComponentTransfers
					.containsKey(ViewSourceTransfer.class)) {
				// get existing view sources for the view in the XML
				for (SAFREnvironmentalComponentTransfer tfr : existingComponentTransfers.get(
						ViewSourceTransfer.class).values()) {
					// proceed only if view source not present in the XML
					if (!map.containsKey(tfr.getId())) {
						// proceed only if View is present in XML
						ViewSourceTransfer viewSrcTrans = (ViewSourceTransfer) tfr;
						if (records.get(ViewTransfer.class).containsKey(
								viewSrcTrans.getViewId())) {
							// get model object to be deleted
							ViewSource tmpViewSrc = ViewFactory.initViewSource(null,viewSrcTrans);
							tmpViewSrc.markDeleted();
							Integer viewId = viewSrcTrans.getViewId();
							if (deletedViewSources.containsKey(viewId)) {
								// add to existing list
								deletedViewSources.get(viewId).add(tmpViewSrc);
							} else {
								List<ViewSource> tmpViewSrcs = new ArrayList<ViewSource>();
								tmpViewSrcs.add(tmpViewSrc);
								deletedViewSources.put(viewId, tmpViewSrcs);
							}
						}
					}
				}
			}
		}

		// Create a map of view columns from XML keyed on View id
		map = records.get(ViewColumnTransfer.class);
		Map<Integer, List<ViewColumnTransfer>> viewColumnsMap = new HashMap<Integer, List<ViewColumnTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				ViewColumnTransfer trans = (ViewColumnTransfer) tfr;
				if (trans.getId() > 0) {
					Integer viewId = trans.getViewId();
					// add View Column transfer to map
					if (viewColumnsMap.containsKey(viewId)) {
						viewColumnsMap.get(viewId).add(trans);
					} else {
						List<ViewColumnTransfer> columns = new ArrayList<ViewColumnTransfer>();
						columns.add(trans);
						viewColumnsMap.put(viewId, columns);
					}
				}
			}
		}

		// Create a map of view columns in the target env that will be
		// deleted because they do not exist in the XML
		Map<Integer, List<ViewColumn>> deletedViewColumns = new HashMap<Integer, List<ViewColumn>>();
		if (map != null) {
			if (existingComponentTransfers
					.containsKey(ViewColumnTransfer.class)) {
				// get existing view columns for the view in the XML
				for (SAFREnvironmentalComponentTransfer tfr : existingComponentTransfers.get(
						ViewColumnTransfer.class).values()) {
					// proceed only if view column not present in the XML
					if (!map.containsKey(tfr.getId())) {
						// proceed only if View is present in XML
						ViewColumnTransfer viewColTrans = (ViewColumnTransfer) tfr;
						if (records.get(ViewTransfer.class).containsKey(
								viewColTrans.getViewId())) {
							// get model object to be deleted
							viewColTrans.setForImport(true);
							ViewColumn tmpViewCol = ViewFactory.initViewColumn(null,viewColTrans);
							tmpViewCol.markDeleted();
							Integer viewId = viewColTrans.getViewId();
							if (deletedViewColumns.containsKey(viewId)) {
								// add to existing list
								deletedViewColumns.get(viewId).add(tmpViewCol);
							} else {
								List<ViewColumn> tmpViewCols = new ArrayList<ViewColumn>();
								tmpViewCols.add(tmpViewCol);
								deletedViewColumns.put(viewId, tmpViewCols);
							}
						}
					}
				}
			}
		}

		// Create a map of view column sources from XML keyed on View id
		map = records.get(ViewColumnSourceTransfer.class);
		Map<Integer, List<ViewColumnSourceTransfer>> viewColumnSourcesMap = new HashMap<Integer, List<ViewColumnSourceTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				ViewColumnSourceTransfer trans = (ViewColumnSourceTransfer) tfr;
				if (trans.getId() > 0) {
					Integer viewId = trans.getViewId();
					// add View Column Sources transfer to map
					if (viewColumnSourcesMap.containsKey(viewId)) {
						viewColumnSourcesMap.get(viewId).add(trans);
					} else {
						List<ViewColumnSourceTransfer> columnSources = new ArrayList<ViewColumnSourceTransfer>();
						columnSources.add(trans);
						viewColumnSourcesMap.put(viewId, columnSources);
					}
				}
			}
		}

		// Create a map of view column sources in the target env that will be
		// deleted because they do not exist in the XML
		Map<Integer, List<ViewColumnSource>> deletedViewColumnSources = new HashMap<Integer, List<ViewColumnSource>>();
		if (map != null) {
			if (existingComponentTransfers
					.containsKey(ViewColumnSourceTransfer.class)) {
				// get existing view column sources for the view in the XML
				for (SAFREnvironmentalComponentTransfer tfr : existingComponentTransfers.get(
						ViewColumnSourceTransfer.class).values()) {
					// proceed only if view column source not present in the XML
					if (!map.containsKey(tfr.getId())) {
						ViewColumnSourceTransfer viewColSrcTrans = (ViewColumnSourceTransfer) tfr;
						Integer viewId = viewColSrcTrans.getViewId();
						// proceed only if View is present in XML
						if (records.get(ViewTransfer.class).containsKey(viewId)) {
							// get model object to be deleted
							viewColSrcTrans.setForImport(true);
							ViewColumnSource tmpViewColSrc = ViewFactory.initViewColumnSource(null, viewColSrcTrans);
							tmpViewColSrc.markDeleted(); // redundant but clear
							if (deletedViewColumnSources.containsKey(viewId)) {
								deletedViewColumnSources.get(viewId).add(
										tmpViewColSrc);
							} else {
								List<ViewColumnSource> tmpViewColSrcs = new ArrayList<ViewColumnSource>();
								tmpViewColSrcs.add(tmpViewColSrc);
								deletedViewColumnSources.put(viewId,
										tmpViewColSrcs);
							}
						}
					}
				}
			}
		}

		// Create a map of view sort keys from XML keyed on View id
		map = records.get(ViewSortKeyTransfer.class);
		Map<Integer, List<ViewSortKeyTransfer>> viewSortKeyMap = new HashMap<Integer, List<ViewSortKeyTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				ViewSortKeyTransfer trans = (ViewSortKeyTransfer) tfr;
				if (trans.getId() > 0) {
					Integer viewId = trans.getViewId();
					// add View Sort Key transfer to map
					if (viewSortKeyMap.containsKey(viewId)) {
						viewSortKeyMap.get(viewId).add(trans);
					} else {
						List<ViewSortKeyTransfer> sortKeys = new ArrayList<ViewSortKeyTransfer>();
						sortKeys.add(trans);
						viewSortKeyMap.put(viewId, sortKeys);
					}
				}
			}
		}

		// Create a map of view sort keys in the target env that will be
		// deleted because they do not exist in the XML
		Map<Integer, List<ViewSortKey>> deletedViewSortKeys = new HashMap<Integer, List<ViewSortKey>>();
		if (map != null) {
			if (existingComponentTransfers
					.containsKey(ViewSortKeyTransfer.class)) {
				// get existing view sort keys for the view in the XML
				for (SAFREnvironmentalComponentTransfer tfr : existingComponentTransfers.get(
						ViewSortKeyTransfer.class).values()) {
					// proceed only if view sort key not present in the XML
					if (!map.containsKey(tfr.getId())) {
						// proceed only if View is present in XML
						ViewSortKeyTransfer viewSortKeyTrans = (ViewSortKeyTransfer) tfr;
						if (records.get(ViewTransfer.class).containsKey(
								viewSortKeyTrans.getViewId())) {
							// get model object to be deleted
							viewSortKeyTrans.setForImport(true);
							ViewSortKey tmpViewSortKey = ViewFactory.initViewSortKey(null,viewSortKeyTrans);
							tmpViewSortKey.markDeleted();
							Integer viewId = viewSortKeyTrans.getViewId();
							if (deletedViewSortKeys.containsKey(viewId)) {
								// add to existing list
								deletedViewSortKeys.get(viewId).add(
										tmpViewSortKey);
							} else {
								List<ViewSortKey> tmpViewSortKeys = new ArrayList<ViewSortKey>();
								tmpViewSortKeys.add(tmpViewSortKey);
								deletedViewSortKeys.put(viewId,
										tmpViewSortKeys);
							}
						}
					}
				}
			}
		}

		// Create a map of header footers from XML keyed on View id
		map = records.get(HeaderFooterItemTransfer.class);
		Map<Integer, List<HeaderFooterItemTransfer>> viewHFMap = new HashMap<Integer, List<HeaderFooterItemTransfer>>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				HeaderFooterItemTransfer trans = (HeaderFooterItemTransfer) tfr;
				if (trans.getId() > 0) {
					Integer viewId = trans.getViewId();
					// add header footer transfer to map
					if (viewHFMap.containsKey(viewId)) {
						viewHFMap.get(viewId).add(trans);
					} else {
						List<HeaderFooterItemTransfer> HFItems = new ArrayList<HeaderFooterItemTransfer>();
						HFItems.add(trans);
						viewHFMap.put(viewId, HFItems);
					}
				}
			}
		}

		// Create the View model object and attach the View sources, columns, 
		// column sources, sort keys, and header/footers.
		
		map = records.get(ViewTransfer.class);
		List<View> views = new ArrayList<View>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				ViewTransfer trans = (ViewTransfer) tfr;
				View view = SAFRApplication.getSAFRFactory().initView(trans);
				view.setModelTransferProvider(provider);

				if (trans.getWriteExitId() != null && trans.getWriteExitId() > 0) {
					// get the UXR and add to View model
					UserExitRoutine uxr = uxrs.get(trans.getWriteExitId());					
					view.setWriteExit(uxr);
				}
				if (trans.getFormatExitId() != null && trans.getFormatExitId() > 0) {
					// get the UXR and add to View model
					UserExitRoutine uxr = uxrs.get(trans.getFormatExitId());					
					view.setFormatExit(uxr);
				}

				// add View Sources to this View
				if (viewSourcesMap.containsKey(view.getId())) {
					List<ViewSource> vwSources = new ArrayList<ViewSource>();
					for (ViewSourceTransfer vwSrcTrans : viewSourcesMap
							.get(view.getId())) {
						ViewSource vwSrc = ViewFactory.initViewSource(view, vwSrcTrans);
						vwSources.add(vwSrc);
					}
					Collections.sort(vwSources, new Comparator<ViewSource>() {
						public int compare(ViewSource o1, ViewSource o2) {
							return o1.getSequenceNo().compareTo(
									o2.getSequenceNo());
						}
					});
					view.getViewSources().addAll(vwSources);
					// add the view sources to be deleted
					if (deletedViewSources.containsKey(view.getId())) {
						view.getViewSources().addAll(
								deletedViewSources.get(view.getId()));
					}
				}

				// add View Columns to this View
				if (viewColumnsMap.containsKey(view.getId())) {
					List<ViewColumn> vwColumns = new ArrayList<ViewColumn>();
					for (ViewColumnTransfer vwColTrans : viewColumnsMap
							.get(view.getId())) {
						ViewColumn vwCol = ViewFactory.initViewColumn(view, vwColTrans);
						vwColumns.add(vwCol);
					}
					Collections.sort(vwColumns, new Comparator<ViewColumn>() {
						public int compare(ViewColumn o1, ViewColumn o2) {
							return o1.getColumnNo().compareTo(o2.getColumnNo());
						}
					});
					view.getViewColumns().addAll(vwColumns);
					// add the view columns to be deleted
					if (deletedViewColumns.containsKey(view.getId())) {
						view.getViewColumns().addAll(
								deletedViewColumns.get(view.getId()));
					}
				}
				// add View Column Sources to this View
				if (viewColumnSourcesMap.containsKey(view.getId())) {
					List<ViewColumnSource> vwColumnSrcs = new ArrayList<ViewColumnSource>();
					for (ViewColumnSourceTransfer vwColSrcTrans : viewColumnSourcesMap
							.get(view.getId())) {
						ViewColumnSource vwColSrc = ViewFactory.initViewColumnSource(view,vwColSrcTrans);
						vwColSrc.setModelTransferProvider(provider);
						vwColumnSrcs.add(vwColSrc);
					}
					view.getViewColumnSources().addAll(vwColumnSrcs);
					// add the view column sources to be deleted
					if (deletedViewColumnSources.containsKey(view.getId())) {
						view.getViewColumnSources().addAll(
								deletedViewColumnSources.get(view.getId()));
					}
				}
				// add View Sort Keys to this View
				if (viewSortKeyMap.containsKey(view.getId())) {
					List<ViewSortKey> vwSortkeys = new ArrayList<ViewSortKey>();
					for (ViewSortKeyTransfer vwSortKeyTrans : viewSortKeyMap.get(view.getId())) {
						ViewSortKey vwSortKey = ViewFactory.initViewSortKey(view, vwSortKeyTrans);
						vwSortKey.setModelTransferProvider(provider);
						vwSortkeys.add(vwSortKey);
					}
					Collections.sort(vwSortkeys, new Comparator<ViewSortKey>() {
						public int compare(ViewSortKey o1, ViewSortKey o2) {
							return o1.getKeySequenceNo().compareTo(
									o2.getKeySequenceNo());
						}
					});
					view.getViewSortKeys().addAll(vwSortkeys);
					// add the sort keys to be deleted
					if (deletedViewSortKeys.containsKey(view.getId())) {
						view.getViewSortKeys().addAll(
								deletedViewSortKeys.get(view.getId()));
					}
				}

				// CQ10040 only import header/footers for hardcopy views
				if (view.getOutputFormat() == OutputFormat.Format_Report) {
					// add View Header Footer to this View
					if (viewHFMap.containsKey(view.getId())) {
						for (HeaderFooterItemTransfer vwHFTrans : viewHFMap
								.get(view.getId())) {
							ViewFactory.addHeaderFooterItem(view, vwHFTrans);

						}
					}
				}
				
				// don't forget the CR - there should be one an only one
				for (ControlRecord cr : crs) {
					view.setControlRecord(cr);
				}
				// view should be inactive when imported
				view.makeViewInactive();
				// validate view
				view.validate();
				// add in list for store
				views.add(view);
			}
		}
        return views;
    }

    protected List<ControlRecord> createControlRecords() {
        // create control records.
		Map<Integer, SAFRTransfer> map = records.get(ControlRecordTransfer.class);
		List<ControlRecord> crs = new ArrayList<ControlRecord>();
		if (map != null) {
			for (SAFRTransfer tfr : map.values()) {
				ControlRecordTransfer crTrans = (ControlRecordTransfer) tfr;
				ControlRecord cr = SAFRApplication.getSAFRFactory()
						.initControlRecord(crTrans);
				cr.validate();
				crs.add(cr);
			}
		}
        return crs;
    }
	
	// CQ10049 refactored code follows...
	
	protected void parseRecords() throws SAFRException,
			XPathExpressionException {
		super.parseRecords();
		viewParser = new ViewRecordParser(this);
		viewParser.parseRecords();
		viewSrcParser = new ViewSourceRecordParser(this);
		viewSrcParser.parseRecords();
		viewColParser = new ViewColumnRecordParser(this);
		viewColParser.parseRecords();
		viewColSrcParser = new ViewColumSourceRecordParser(this);
		viewColSrcParser.parseRecords();
		viewSortKeyParser = new ViewSortKeyRecordParser(this);
		viewSortKeyParser.parseRecords();
		viewHeaderFooterParser = new ViewHeaderFooterRecordParser(this);
		viewHeaderFooterParser.parseRecords();
		crParser = new CRRecordParser(this);
		crParser.parseRecords();
	}
	
	protected void checkReferentialIntegrity() throws SAFRValidationException {
		super.checkReferentialIntegrity();
		viewParser.checkReferentialIntegrity();
		viewSrcParser.checkReferentialIntegrity();
		viewColParser.checkReferentialIntegrity();
		viewColSrcParser.checkReferentialIntegrity();
		crParser.checkReferentialIntegrity();
        lfPfParser.checkReferentialIntegrity();		
	}
	
	protected void checkOutOfRangeIds() throws SAFRException {
		super.checkOutOfRangeIds();
		checkOutOfRangeIds(ViewTransfer.class);
		checkOutOfRangeIds(ViewSourceTransfer.class);
		checkOutOfRangeIds(ViewColumnTransfer.class);
		checkOutOfRangeIds(ViewColumnSourceTransfer.class);
		checkOutOfRangeIds(ViewSortKeyTransfer.class);
		checkOutOfRangeIds(HeaderFooterItemTransfer.class);
		checkOutOfRangeIds(ControlRecordTransfer.class);
	}
	
	/**
	 * @see com.ibm.safr.we.model.utilities.importer.LookupPathImporter#checkAssociationsAndSubComponents()
	 */
	protected void checkAssociationsAndSubComponents() throws SAFRException {
		super.checkAssociationsAndSubComponents();
		
		// Check that if any View subcomponents already exist in the DB,
		// they relate to the imported View not some other View.
		viewSrcParser.checkViewSources();
		viewColParser.checkViewColumns();
		viewColSrcParser.checkViewColumnSources();
		viewSortKeyParser.checkViewSortKeys();
		viewHeaderFooterParser.checkViewHeaderFooters();
	}
	
	/**
	 * @see com.ibm.safr.we.model.utilities.importer.LookupPathImporter#checkAssociationsWithDifferentId()
	 */
	protected void checkAssociationsWithDifferentId() throws SAFRException {
		super.checkAssociationsWithDifferentId();
		
		// replace association ids to match target database
		viewSrcParser.replaceAssociationIds();
		viewParser.replaceAssociationIds();
	}
	
}
