package com.ibm.safr.we.model.view;



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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;
import org.eclipse.ui.IWorkbenchPartSite;


import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.ReportType;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRValidator;
import com.ibm.safr.we.ui.reports.ReportUtils;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewActivator {    
      
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.view.ViewActivator");
    
    private View view;
    private ArrayList<ViewLogicDependency> viewLogicDependencies;
    private SAFRViewActivationException vaException = new SAFRViewActivationException(view);

	private Set<Integer> CTCols;
	public ViewActivator(View view) {
        super();
        this.view = view;
    }
    
    protected SAFRViewActivationException getVaException() {
        return vaException;
    }    
    
    static public String getNewCompilerVersion() {
        return WorkbenchCompiler.getVersion();          
    }
    
    static public String getCompilerVersion() {
            return getNewCompilerVersion();
    }

	public static void setSite(IWorkbenchPartSite site) {
	}
    /**
     * Invoke activation 
     */ 
    public void activate() throws DAOException, SAFRException {
        executeTheActivation();
    	ReportUtils.openReportEditor(ReportType.ActivationReport);
        logActivationResult();
    }

    public void batchActivate() throws DAOException, SAFRException {
        executeTheActivation();
    	ReportUtils.generateOnly(ReportType.ActivationReport);
        logActivationResult();
    }

	private void logActivationResult() {
		if(WorkbenchCompiler.hasErrors()) {
            SAFRLogger.logAll(logger, Level.INFO, "Failed Activation");                
            SAFRLogger.logEnd(logger);
            vaException.setErrorOccured();
            throw vaException;
	    } else {
	        SAFRLogger.logAll(logger, Level.INFO, "Successful Activation");
	        SAFRLogger.logEnd(logger);
	    }
	}

	private void executeTheActivation() {
		SAFRLogger.logAllSeparator(logger, Level.INFO, "Activating View " + view.getDescriptor());
        
        vaException = new SAFRViewActivationException(view);   
        try {
        	activateTheWholeView();
        } catch (SAFRViewActivationException e) {
            logger.log(Level.INFO, "New Activation Exception");
        }
        
        view.setCompilerVersion(ViewActivator.getCompilerVersion());
	}

    /**
     * Activate
     * 
     */ 
    protected void activateTheWholeView() throws DAOException, SAFRException {

        initialization();                
        checkActivationShowStoppers();        

        compileFormatCalculation();
        compileFormatFilter();
        addSummaryColsToCTs();
        compileViewSources();
                
        processResult();        
    }

    private void addSummaryColsToCTs() {
        if (view.isFormatPhaseInUse()) {
            for (ViewColumn col : view.getViewColumns().getActiveItems()) {
                if (col.getSubtotalTypeCode() != null && col.getSubtotalTypeCode().getGeneralId() != Codes.GRPAGGR_NONE) {
                    CTCols.add(col.getColumnNo());
                }
            }
        }
    }

    protected void initialization() throws SAFRException, DAOException, SAFRViewActivationException {
        initViewDependencies();
        initializeViewColumns();
        WBCompilerDataStore.initializeWorkbenchCompiler(view);
    }

	protected void checkActivationShowStoppers()
        throws SAFRException, DAOException, SAFRViewActivationException {
        checkSavedView();
        checkFormatHasSortKey();
        checkHasViewSources();
        checkConsistentHardcopy();        
        checkDuplicateViewSource();        
        checkSortKeyTitles();
        checkRecordAggregation();
        checkCopyViewHasNoColumns();
        checkNormalViewHasColumns();
        if (WorkbenchCompiler.newErrorsDetected()) {
            throw vaException; // cannot continue.                
        }
    }

    private void checkNormalViewHasColumns() {
        if (! view.getOutputFormat().equals(OutputFormat.Extract_Source_Record_Layout)) {
        	if(view.getViewColumns().size() == 0) {
            	WorkbenchCompiler.addViewPropertiesErrorMessage("Only a Source Record Layout view can have no columns.");
        	}
        }		
	}

	private void checkCopyViewHasNoColumns() {
        if (view.getOutputFormat().equals(OutputFormat.Extract_Source_Record_Layout)) {
        	if(view.getViewColumns().size() > 0) {
            	WorkbenchCompiler.addViewPropertiesErrorMessage("A Source Record Layout view cannot have columns.");
        	}
        }		
	}

	private void checkRecordAggregation() {
        if (view.isFormatPhaseInUse() && view.isFormatPhaseRecordAggregationOn()) {
        	for(ViewColumn c : view.getViewColumns()) {
        		if(c.isSortKey() == false && c.isNumeric() && c.getRecordAggregationCode() == null) {
        			WorkbenchCompiler.setCurrentColumnNumber(c.getColumnNo());
                	WorkbenchCompiler.addViewPropertiesErrorMessage("Record Aggregation cannot be blank.");
        		}
        	}
        }		
	}

	protected void checkSavedView() throws SAFRException {
        if (view.getId() <= 0) {
            // if the view is not saved yet then throw error
            throw new SAFRException("A new View must be saved before activation.");
        }
    }
        
    protected void checkFormatHasSortKey() {
        if (view.isFormatPhaseInUse()) {
            if (view.getViewSortKeys().getActiveItems().isEmpty()) {
            	WorkbenchCompiler.setCurrentColumnNumber(0);
            	WorkbenchCompiler.addViewPropertiesErrorMessage("This View must have at least one Sort Key.");
            }
        }
    }
    
    protected void checkHasViewSources() {
        // if the view doesn't have any View sources, then throw activation
        // exception.
        if (view.getViewSources().getActiveItems().isEmpty()) {
        	WorkbenchCompiler.setCurrentColumnNumber(0);
        	WorkbenchCompiler.addViewPropertiesErrorMessage("This View must have at least one view source.");
        }
    }
    
    protected  void checkConsistentHardcopy() {
        // If the last sort key of a hard copy summary view has Display Mode
        // 'Categorize' (or blank), it must have a Sort Key Footer Option
        // of 'Print'. 
        if (isSummarizedHardCopyView()) {
            for (ViewSortKey key : view.getViewSortKeys().getActiveItems()) {
                if (isCategoryModeAndLast(key) && isSuppressPrint(key)) {
                	WorkbenchCompiler.setCurrentColumnNumber(key.getViewColumn().getColumnNo());
                	WorkbenchCompiler.addViewPropertiesErrorMessage("Sort Key Footer Option must be 'Print' for the last sort key of a hard copy summary view.");
                    break;
                }
            }
        } 
    }

    protected boolean isSummarizedHardCopyView() {
        return view.getOutputFormat() == OutputFormat.Format_Report && 
            view.isFormatPhaseRecordAggregationOn();
    }
    
    protected boolean isCategoryModeAndLast(ViewSortKey key) {
        return key.isLastSortKey() && 
             (key.getDisplayModeCode() == null || 
              key.getDisplayModeCode().getGeneralId() == Codes.CATEGORIZE);
    }

    protected boolean isSuppressPrint(ViewSortKey key) {
        return key.getFooterOptionCode() == null
                || key.getFooterOptionCode().getGeneralId() == Codes.SUPPRESS_PRINT;
    }
    
    protected  void checkDuplicateViewSource() {
        // A view should'nt have duplicate view source.
        List<ViewSource> sources = view.getViewSources().getActiveItems();
        for (ViewSource source : sources) {
            int found = 0;
            for (ViewSource source1 : sources) {
                if (source1.getLrFileAssociationId().equals(
                        source.getLrFileAssociationId())) {
                    found++;
                }
            }
            if (found > 1) {
            	WorkbenchCompiler.setCurrentColumnNumber(0);
            	WorkbenchCompiler.addViewPropertiesErrorMessage("Duplicate View sources are not allowed.");
                break;
            }
        }
    }

    protected void checkSortKeyTitles()
        throws SAFRException, DAOException {
        for (ViewSortKey key : view.getViewSortKeys().getActiveItems()) {
            try {
                SAFRValidator safrValidator = new SAFRValidator();
                safrValidator.verifySortKey(key);
                key.validateTitleField();
            } catch (SAFRValidationException sve) {
                for (String error : sve.getErrorMessages()) {
                    error = "Sort Key# " + key.getKeySequenceNo() + " " + error;
                    vaException.addActivationError(new ViewActivationError(null, null,
                            SAFRCompilerErrorType.VIEW_PROPERTIES, error));
                }
            }
        }
    }

    protected void initViewDependencies() {
        viewLogicDependencies = new ArrayList<ViewLogicDependency>();  
    }
        
    protected void initializeViewColumns() throws DAOException {
        // get column Ids for unsaved columns.
        getTempColumnIds();
        // calculate start positions of all the columns.
        view.calculateStartPosition();
        // calculate ordinal positions of all the columns.
        calculateOrdinalPositions();
    }
    
    /**
     * Checks for unsaved columns whose Ids are not yet assigned from DB. For
     * all such columns, this function will get DB Id and assign to id.
     * 
     * @throws DAOException
     */
    protected void getTempColumnIds() throws DAOException {
        int maxId = 1;
        List<ViewColumn> cols = new ArrayList<ViewColumn>();
        // calculate the number of Ids needed for NEW columns having non-DB Ids.
        // add all such columns to a temp list.
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            if (col.getPersistence() == SAFRPersistence.NEW && col.getId() <= 0) {
                cols.add(col);
            } else {
                if (col.getId() > maxId) {
                    maxId = col.getId();
                }
            }
        }
        Integer idToStart = maxId+1;
        for (ViewColumn col : cols) {
            col.setId(idToStart++);
        }
    }

    /**
     * Sets the ordinal positions of all the visible columns. Invisible columns
     * will have ordinal position 0.
     */
    protected void calculateOrdinalPositions() {
        int pos = 1; // start with 1
        for (ViewColumn col : view.getViewColumns().getActiveItems()) {
            if (col.isVisible()) {
                col.setOrdinalPosition(pos++);
            } else {
                col.setOrdinalPosition(0);
            }
        }
    }

    protected void compileFormatCalculation() {    
        ViewLogicFormatCalc logCompiler = new ViewLogicFormatCalc(view, vaException);
        logCompiler.compile();
        CTCols = logCompiler.getCTCols();
    }


    protected void compileViewSources() {

        WorkbenchCompiler.clearNewErrorsDetected();
        for (ViewSource source : view.getViewSources().getActiveItems()) {
            WorkbenchCompiler.addViewSource(WBCompilerDataStore.makeViewSource(source));
            WorkbenchCompiler.compileExtractFilter(view.getId(), source.getSequenceNo());
            compileExtractColumns(source, CTCols);
            WorkbenchCompiler.compileExtractOutput(view.getId(), source.getSequenceNo());
        }
        if (WorkbenchCompiler.hasNoNewErrors()) {
            WorkbenchCompiler.buildLogicTablesAndPerformWholeViewChecks();
        }
        WBCompilerDataStore.setLogicTableLog();
    }

    protected void compileExtractColumns(ViewSource source, Set<Integer> cTCols) {
    	ViewLogicExtractCalc columnsCompiler = new ViewLogicExtractCalc(view, viewLogicDependencies);
    	columnsCompiler.compile(source, cTCols);
    }

    protected void compileFormatFilter() throws SAFRException {
        // Format phase record filter compilation
        // ONLY if format phase is ON
        if (view.isFormatPhaseInUse()) {
            if (view.getFormatRecordFilter() != null && view.getFormatRecordFilter().length() > 0) {
                String cs =WorkbenchCompiler.compileFormatFilter(view.getId());
                WBCompilerDataStore.setFormatFilterCalculationStack(cs);
                Set<Integer> calcCols = WorkbenchCompiler.getColumnRefs();
                checkColumnDataTypes(calcCols);
                CTCols.addAll(calcCols);
            }
        }
    }

    private void checkColumnDataTypes(Set<Integer> calcCols) {
        // Columns here are not indexed
        // process the other way around - crazy slow ?
        for (ViewColumn c : view.getViewColumns()) {
            if (calcCols.contains(c.getColumnNo())) {
                if (c.isNumeric() == false) {
                    // The workbench disables this.... should not occur
                    WorkbenchCompiler.setCurrentColumnNumber(c.getColumnNo());
                    WorkbenchCompiler
                            .addFormatFilterErrorMessage("Column number " + c.getColumnNo() + " is alphanumeric");
                }
                if (c.isSortKey()) {
                    // This is disabled too!
                    vaException.addActivationError(
                            new ViewActivationError(null, null, SAFRCompilerErrorType.FORMAT_RECORD_FILTER,
                                    "Column number " + c.getColumnNo() + " a sort key column"));
                }
            }
        }
        for (Integer refCol : calcCols) {
            if (refCol == 0) {
                WorkbenchCompiler.setCurrentColumnNumber(refCol);
                WorkbenchCompiler.addFormatFilterErrorMessage("Column number must be greater than zero.");
            }
            if (refCol > view.getViewColumns().size()) {
                WorkbenchCompiler.setCurrentColumnNumber(refCol);
                WorkbenchCompiler.addFormatFilterErrorMessage("Column number " + refCol
                        + " is greater than the number of columns " + view.getViewColumns().size());
            }
        }

    }
	
    protected void processResult() {
        if (WorkbenchCompiler.hasErrors()) {
        	logger.log(Level.SEVERE, "Activation Errors found");
        } else {
        	extractDependencies();
            view.setViewLogicDependencies(viewLogicDependencies);
            WBCompilerDataStore.setLogicTableLog();
            view.setStatusCode(SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.ACTIVE));
        }
    }

    protected void extractDependencies() {
    	ViewLogicExtractor vle = new ViewLogicExtractor(view, viewLogicDependencies);
    	vle.extractDependencies();
	}
}
