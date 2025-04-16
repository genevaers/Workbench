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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ui.IWorkbenchPartSite;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.constants.OutputPhase;
import com.ibm.safr.we.constants.SAFRCompilerErrorType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.UserExitRoutineTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.ModelUtilities;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.SAFRValidator;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.base.SAFRActivatedComponent;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.utilities.importer.ModelTransferProvider;
import com.ibm.safr.we.utilities.SAFRLogger;


public class View extends SAFRActivatedComponent {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.View");

    static private final String LINEBREAK = System
    .getProperty("line.separator");

    private Code statusCode; // Active,Inactive VIEWSTATUSCD
    private Code typeCode; // Metadata list, VIEWTYPECD
    private Integer extractWorkFileNo; // Extr Phase, EXTRACTFILEPARTNBR
    private Code outputFormatCode; // General, OUTPUTMEDIACD
    private Integer outputLRId; // General (new view), OUTPUTLRID
    private Integer extractFileAssociationId; // Extr Output File, LFPFASSOCID
    private FileAssociation extractFileAssociation;
    private Integer linesPerPage; // General, PAGESIZE
    private Integer reportWidth; // General, LINESIZE
    private Boolean suppressZeroRecords; // Format Phase, ZEROSUPPRESSIND
    private Boolean headerRow; //Delimiter Phase, DELIMHEADERROWIND
    private Integer extractMaxRecords; // Extract Output, EXTRACTMAXRECCNT
    private Boolean aggregateBySortKey; // Extract Aggr, EXTRACTSUMMARYIND
    private Integer aggregateBufferSize; // Extract Aggr, EXTRACTSUMMARYBUF
    private Integer outputMaxRecCount; // Format Output, OUTPUTMAXRECCNT
    private Integer controlRecordId; // General, CONTROLID
    private ControlRecord controlRecord;
    private Integer writeExitId; // Extract Phase, WRITEEXITID
    private UserExitRoutine writeExit;
    private String writeExitParams; // Extract Phase, WRITEEXITSTARTUP
    private Integer formatExitId; // Format Phase, FORMATEXITID
    private UserExitRoutine formatExit;
    private String formatExitParams; // Format Phase, FORMATEXITSTARTUP
    private Code fileFieldDelimiterCode; // Format Phase, FILEFLDDELIMCD
    private Code fileStringDelimiterCode; // Format Phase, FILESTRDELIMCD
    private Boolean isFormatPhaseRecordAggregationOn;
    private OutputPhase outputPhase;
    private OutputFormat outputFormat;
    private Boolean extractPhaseOutputLimit;
    private Boolean formatPhaseOutputLimit;
    private Date effectiveDate;

    private String compilerVersion;
	private String formatRecordFilter; // Will be loaded using SAFRBlobs.dll
	// taken from compiler during view activation and will be stored in DB after
	// converting to blobs using SAFRBlobs.dll
	private byte[] compiledFormatRecordFilter;

    private final SAFRList<ViewSource> viewSources = new SAFRList<ViewSource>();
    private final SAFRList<ViewColumn> viewColumns = new SAFRList<ViewColumn>();
    private final SAFRList<ViewColumnSource> viewColumnSources = new SAFRList<ViewColumnSource>();
    private final SAFRList<ViewSortKey> viewSortKeys = new SAFRList<ViewSortKey>();

    private View.HeaderFooterItems header;
    private View.HeaderFooterItems footer;

    private List<ViewLogicDependency> viewLogicDependencies;

    private SAFRAssociationList<ViewFolderViewAssociation> vfAssociations = new SAFRAssociationList<ViewFolderViewAssociation>();
    
    private Integer dummyColumnId = 1;
    private Integer dummyColumnSrcId = 1;
    private Integer dummySrcId = 1;
    private Integer dummyKeyId = 1;
    
    private boolean writeExitUerState;
    private boolean formatExitUerState;

    private List<String> loadWarnings;

    private ModelTransferProvider provider;

    private boolean batchActivated = false;
    
    private boolean migrateRelatedComponents = false;
    

    /**
     * This constructor is used when defining a new View in the application. It
     * will initialize the View ID to zero and EnvironmentId to specified
     * Environment Id to which View belongs to. The View ID will be reset
     * automatically to a unique value when the View object is persisted via its
     * <code>store()</code> method.
     * 
     * @throws SAFRException
     */
    public View(Integer environmentId) {
        super(environmentId);
        this.statusCode = SAFRApplication.getSAFRFactory().getCodeSet(
            CodeCategories.VIEWSTATUS).getCode(Codes.INACTIVE);
        this.setOutputFormat(OutputFormat.Format_Fixed_Width_Fields);
        this.isFormatPhaseRecordAggregationOn = false;
        this.linesPerPage = 66;
        this.reportWidth = 250;
        this.controlRecordId = null;
        this.extractPhaseOutputLimit = false;
        this.formatPhaseOutputLimit = false;
    }

    /**
     * Create a View object containing the data in the specified transfer
     * object. Used to instantiate existing View objects.
     * 
     * @param trans
     *            the ViewTransfer object
     * @throws SAFRException
     *             In case there are inactive dependencies and the view cannot
     *             be loaded, a sub-type {@link SAFRDependencyException} will be
     *             thrown with a list of inactive components.
     */
   
    
    /**
     * Create a View object containing the data in the specified transfer
     * object. Used to instantiate existing View objects.
     * 
     * @param trans
     *            the ViewTransfer object
     * @throws SAFRException
     *             In case there are inactive dependencies and the view cannot
     *             be loaded, a sub-type {@link SAFRDependencyException} will be
     *             thrown with a list of inactive components.
     */
    public View(ViewTransfer trans) throws DAOException, SAFRException {
        super(trans);
        initFromTransfer(trans);
    }

    private void initFromTransfer(ViewTransfer trans) throws DAOException, SAFRException {
        
        if (!isForImport() && !isForMigration()) {
            checkForInactiveDependencies();

            // init viewColumns and viewSources before viewColumnSources
            this.viewSources.addAll(ViewFactory.getViewSources(this));
            this.viewColumns.addAll(ViewFactory.getViewColumns(this));
            this.viewColumnSources.addAll(ViewFactory.getViewColumnSources(this));
            this.viewSortKeys.addAll(ViewFactory.getViewSortKeys(this));

            // append the load warnings from the individual view columns, view
            // column sources and view sort keys to the loadWarnings list of the
            // View class
            for (ViewColumn viewCol : this.viewColumns.getActiveItems()) {
                if (!viewCol.getLoadWarnings().isEmpty()) {
                    this.loadWarnings.addAll(viewCol.getLoadWarnings());
                }
            }
            for (ViewColumnSource viewColSource : this.viewColumnSources
                    .getActiveItems()) {
                if (!viewColSource.getLoadWarnings().isEmpty()) {
                    for (String msg : viewColSource.getLoadWarnings()) {
                        String modifiedMsg = "View Column Source of column "
                                + viewColSource.getViewColumn().getColumnNo()
                                + msg;
                        this.loadWarnings.add(modifiedMsg);
                    }
                }
            }
            for (ViewSortKey sortKey : this.viewSortKeys.getActiveItems()) {
                if (!sortKey.getLoadWarnings().isEmpty()) {
                    this.loadWarnings.addAll(sortKey.getLoadWarnings());
                }
            }

            if (this.outputFormat == OutputFormat.Format_Report) {
                // load header footer items from DB.
                ViewFactory.loadHeaderFooterItems(this);

                // CQ 8768. Nikita. 21/10/2010
                // append the load warnings from the headers and footers to the
                // loadWarnings list of the View class
                if (this.header != null) {
                    for (HeaderFooterItem headerItem : this.header.items) {
                        if (!headerItem.getLoadWarnings().isEmpty()) {
                            for (String msg : headerItem.getLoadWarnings()) {
                                this.loadWarnings.add("This View's header "
                                        + msg);
                            }
                        }
                    }
                }
                if (this.footer != null) {
                    for (HeaderFooterItem footerItem : this.footer.items) {
                        if (!footerItem.getLoadWarnings().isEmpty()) {
                            for (String msg : footerItem.getLoadWarnings()) {
                                this.loadWarnings.add("This View's footer "
                                        + msg);
                            }
                        }
                    }
                }
            }

        }
        
        if (!trans.isForImport()) {
            // load only if already stored in DB.
            vfAssociations = SAFRAssociationFactory.getViewToViewFolderAssociations(this,false);
        }
        
                
    }

    public void checkForInactiveDependencies() {
        Map<ComponentType, List<DependentComponentTransfer>> dependencies = DAOFactoryHolder
                .getDAOFactory().getViewDAO()
                .getInactiveDependenciesOfView(getEnvironmentId(), getId());
        if (dependencies != null && !dependencies.isEmpty()) {
            throw new SAFRDependencyException(dependencies);
        }
    }

    @Override
    protected void setObjectData(SAFRTransfer safrTrans) {
        super.setObjectData(safrTrans);
        ViewTransfer trans = (ViewTransfer) safrTrans;

        loadWarnings = new ArrayList<String>();

        try {
            this.statusCode = SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.VIEWSTATUS)
                    .getCode(trans.getStatusCode());
        } catch (IllegalArgumentException iae) {
            loadWarnings.add("This View does not have a valid Status. " + SAFRUtilities.LINEBREAK + "Status will be set if the View is saved.");
        }
        try {
            this.typeCode = SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.VIEWTYPE)
                    .getCode(trans.getTypeCode());
            this.outputFormatCode = SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.OUTPUTMED)
                    .getCode(trans.getOutputFormatCode());
        } catch (IllegalArgumentException iae) {
            loadWarnings
                    .add("The Output Format is incomplete. " + SAFRUtilities.LINEBREAK + "A valid Output Format must be specified before saving.");
            this.outputFormatCode = null;
        }
        if(outputFormatCode != null && typeCode != null){
            this.convertTypeCodeToOutputFormat(typeCode.getGeneralId().intValue(),
                    outputFormatCode.getGeneralId().intValue());
        } else {
            outputPhase = OutputPhase.Format;
            isFormatPhaseRecordAggregationOn = false;
        }
        this.extractWorkFileNo = trans.getWorkFileNumber();
        this.outputLRId = trans.getOutputLRId();
        this.extractFileAssociationId = trans.getExtractFileAssocId();
        if (extractFileAssociation != null
                && extractFileAssociation.getAssociationId() != trans
                        .getExtractFileAssocId()) {
            this.extractFileAssociation = null;
        }
        this.linesPerPage = trans.getPageSize();
        this.reportWidth = trans.getLineSize();
        this.suppressZeroRecords = trans.isSuppressZeroRecords();
        this.headerRow = trans.isHeaderRow();
        this.extractMaxRecords = trans.getExtractMaxRecCount();
        this.aggregateBySortKey = trans.isAggregateBySortKey();
        this.aggregateBufferSize = trans.getExtractSummaryBuffer();
        this.outputMaxRecCount = trans.getOutputMaxRecCount();
        this.controlRecordId = trans.getControlRecId();
        if (controlRecord != null
                && controlRecord.getId() != trans.getControlRecId()) {
            this.controlRecord = null;
        }
        this.writeExitId = trans.getWriteExitId();
        if (writeExit != null && writeExit.getId() != trans.getWriteExitId()) {
            this.writeExit = null;
        }
        this.writeExitParams = trans.getWriteExitParams();
        this.formatExitId = trans.getFormatExitId();
        if (formatExit != null && formatExit.getId() != trans.getFormatExitId()) {
            this.formatExit = null;
        }
        this.formatExitParams = trans.getFormatExitParams();
        try {
            this.fileFieldDelimiterCode = ModelUtilities.getCodeFromKey(
                    CodeCategories.FLDDELIM, trans.getFieldDelimCode());
        } catch (IllegalArgumentException iae) {
            loadWarnings.add("This View does not have a valid field delimiter. Please select a valid field delimiter, if required, before saving.");
            this.fileFieldDelimiterCode = null;
        }
        try {
            this.fileStringDelimiterCode = ModelUtilities.getCodeFromKey(
                    CodeCategories.STRDELIM, trans.getStringDelimCode());
        } catch (IllegalArgumentException iae) {
            loadWarnings.add("This View does not have a valid string delimiter. Please select a valid string delimiter, if required, before saving.");
            this.fileStringDelimiterCode = null;
        }
        if (this.extractMaxRecords > 0) {
            this.extractPhaseOutputLimit = true;
        } else {
            this.extractPhaseOutputLimit = false;
        }
        if (this.outputMaxRecCount > 0) {
            this.formatPhaseOutputLimit = true;
        } else {
            this.formatPhaseOutputLimit = false;
        }
        this.effectiveDate = trans.getEffectiveDate();
        this.formatRecordFilter = trans.getFormatFilterlogic();
        this.compilerVersion = trans.getCompilerVersion();
    }

    @Override
    protected void setTransferData(SAFRTransfer safrTrans) {
        super.setTransferData(safrTrans);
        ViewTransfer trans = (ViewTransfer) safrTrans;
        trans.setStatusCode(statusCode.getKey()); // non-null
        this.convertOutputFormatToTypeCode(trans);
        trans.setWorkFileNumber(extractWorkFileNo != null ? extractWorkFileNo
                : 0);
        trans.setOutputLRId(outputLRId);
        trans.setExtractFileAssocId(extractFileAssociationId);
        trans.setPageSize(linesPerPage != null ? linesPerPage : 0);
        trans.setLineSize(reportWidth != null ? reportWidth : 0);
        trans.setZeroSuppressInd(suppressZeroRecords != null ? suppressZeroRecords
                : false);
        trans.setHeaderRow(headerRow != null ? headerRow : false);
        trans.setExtractMaxRecCount(extractMaxRecords != null ? extractMaxRecords
                : 0);
        trans.setExtractSummaryIndicator(aggregateBySortKey != null ? aggregateBySortKey
                : false);
        trans.setExtractSummaryBuffer(aggregateBufferSize != null ? aggregateBufferSize
                : 0);
        trans.setOutputMaxRecCount(outputMaxRecCount != null ? outputMaxRecCount
                : 0);
        trans.setControlRecId(controlRecordId);
        trans.setWriteExitId(writeExitId);
        trans.setWriteExitParams(writeExitParams);
        trans.setFormatExitId(formatExitId);
        trans.setFormatExitParams(formatExitParams);
        trans.setFieldDelimCode(fileFieldDelimiterCode == null ? null
                : fileFieldDelimiterCode.getKey());
        trans.setStringDelimCode(fileStringDelimiterCode == null ? null
                : fileStringDelimiterCode.getKey());
        trans.setEffectiveDate(this.effectiveDate);
        trans.setFormatFilterlogic(formatRecordFilter);
        trans.setCompilerVersion(compilerVersion);
    }

    /**
     * @return the extractPhaseOutputLimit
     */
    public Boolean hasExtractPhaseOutputLimit() {
        if (extractPhaseOutputLimit == null) {
            return false;
        } else {
            return extractPhaseOutputLimit;
        }
    }

    /**
     * @param extractPhaseOutputLimit
     *            the extractPhaseOutputLimit to set
     */
    public void setExtractPhaseOutputLimit(Boolean extractPhaseOutputLimit) {
        if (extractPhaseOutputLimit) {
            if (this.extractMaxRecords == null || this.extractMaxRecords <= 0) {
                setExtractMaxRecords(100);
            }
        } else {
            setExtractMaxRecords(0);
        }
        this.extractPhaseOutputLimit = extractPhaseOutputLimit;
        makeViewInactive();
        markModified();
    }

    /**
     * @return formatPhaseOutputLimit
     */
    public Boolean hasFormatPhaseOutputLimit() {
        if (formatPhaseOutputLimit == null) {
            return false;
        } else {
            return formatPhaseOutputLimit;
        }
    }

    /**
     * @param formatPhaseOutputLimit
     *            the formatPhaseOutputLimit to set
     */
    public void setFormatPhaseOutputLimit(Boolean formatPhaseOutputLimit) {
        if (formatPhaseOutputLimit) {
            if (this.outputMaxRecCount == null || this.outputMaxRecCount <= 0) {
                setOutputMaxRecCount(100);
            }
        } else {
            setOutputMaxRecCount(0);
        }
        this.formatPhaseOutputLimit = formatPhaseOutputLimit;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the statusCode
     */
    public Code getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the status of the View to active or inactive.
     * 
     * @param statusCode
     *            the statusCode to set
     * @throws NullPointerException
     *             if the parameter is null
     */
    public void setStatusCode(Code statusCode) {
        if (statusCode == null) {
            throw new NullPointerException(
                    "The View status code cannot be null.");
        }
        if (!this.statusCode.getGeneralId().equals(statusCode.getGeneralId())) {
            markActivated();            
        }
        this.statusCode = statusCode;
    }

    /**
     * @return the extractWorkFileQty
     */
    public Integer getExtractWorkFileNo() {
        return extractWorkFileNo;
    }

    /**
     * @param extractWorkFileNo
     *            the extractWorkFileQty to set
     */
    public void setExtractWorkFileNo(Integer extractWorkFileNo) {
        this.extractWorkFileNo = extractWorkFileNo;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the outputFormatCode
     */
    public Code getOutputFormatCode() {
        return outputFormatCode;
    }

    /**
     * @param outputLR - LR to create view columns from
     * @throws SAFRException
     */
    public void createViewColumns(LogicalRecord outputLR) throws SAFRException {

        if (outputLR != null) {
            createViewColsFromLRFields(outputLR);
        }

        if (outputLR == null) {
            this.outputLRId = 0;
        }
        makeViewInactive();
        markModified();
    }

    /**
     * @return the extractFileAssociation
     */
    public FileAssociation getExtractFileAssociation() throws DAOException,
            SAFRException {
        if (extractFileAssociation == null) {
            if (extractFileAssociationId != null) {
                // lazy initialize and cache the object
                this.extractFileAssociation = SAFRAssociationFactory
                        .getLogicalFileToPhysicalFileAssociation(
                                extractFileAssociationId, getEnvironmentId());
            }
        }
        return extractFileAssociation;
    }

    /**
     * @param extractFileAssociation
     *            the extractFileAssociation to set
     */
    public void setExtractFileAssociation(FileAssociation extractFileAssociation) {
        this.extractFileAssociation = extractFileAssociation;
        if (extractFileAssociation == null) {
            this.extractFileAssociationId = null;
        } else {
            this.extractFileAssociationId = extractFileAssociation
                    .getAssociationId();
        }
        makeViewInactive();
        markModified();
    }

    /**
     * @return the linesPerPage
     */
    public Integer getLinesPerPage() {
        return linesPerPage;
    }

    /**
     * @param linesPerPage
     *            the linesPerPage to set
     */
    public void setLinesPerPage(Integer linesPerPage) {
        this.linesPerPage = linesPerPage;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the reportWidth
     */
    public Integer getReportWidth() {
        return reportWidth;
    }

    /**
     * @param reportWidth
     *            the reportWidth to set
     */
    public void setReportWidth(Integer reportWidth) {
        this.reportWidth = reportWidth;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the suppressZeroRecords
     */
    public Boolean isSuppressZeroRecords() {
        if (suppressZeroRecords == null) {
            return false;
        } else {
            return suppressZeroRecords;
        }
    }

    /**
     * @param suppressZeroRecords
     *            the suppressZeroRecords to set
     */
    public void setSuppressZeroRecords(Boolean suppressZeroRecords) {
        this.suppressZeroRecords = suppressZeroRecords;
        makeViewInactive();
        markModified();
    }

    public Boolean isHeaderRow() {
        if (headerRow == null) {
            return false;
        } else {
            return headerRow;
        }
    }
    
    public void setHeaderRow(Boolean headerRow) {
        this.headerRow = headerRow;
        makeViewInactive();
        markModified();
    }
    /**
     * @return the extractMaxRecords
     */
    public Integer getExtractMaxRecords() {
        return extractMaxRecords;
    }

    /**
     * @param extractMaxRecords
     *            the extractMaxRecords to set
     */
    public void setExtractMaxRecords(Integer extractMaxRecords) {
        this.extractMaxRecords = extractMaxRecords;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the aggregateBySortKey
     */
    public Boolean isExtractAggregateBySortKey() {
        if (aggregateBySortKey == null) {
            return false;
        } else {
            return aggregateBySortKey;
        }
    }

    /**
     * @param aggregateBySortKey
     *            the aggregateBySortKey to set
     */
    public void setExtractAggregateBySortKey(Boolean aggregateBySortKey) {
        if (aggregateBySortKey) {
            if (this.aggregateBufferSize == null || this.aggregateBufferSize <= 0) {
                setExtractAggregateBufferSize(4000);
            }
        } else {
            setExtractAggregateBufferSize(0);
        }
        this.aggregateBySortKey = aggregateBySortKey;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the aggregateBufferSize
     */
    public Integer getExtractAggregateBufferSize() {
        return aggregateBufferSize;
    }

    /**
     * @param aggregateBufferSize
     *            the aggregateBufferSize to set
     */
    public void setExtractAggregateBufferSize(Integer aggregateBufferSize) {
        this.aggregateBufferSize = aggregateBufferSize;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the outputMaxRecCount
     */
    public Integer getOutputMaxRecCount() {
        return outputMaxRecCount;
    }

    /**
     * @param outputMaxRecCount
     *            the outputMaxRecCount to set
     */
    public void setOutputMaxRecCount(Integer outputMaxRecCount) {
        if (outputMaxRecCount != null && outputMaxRecCount > 0) {
            this.outputMaxRecCount = outputMaxRecCount;
        } else {
            this.outputMaxRecCount = 0;
        }
        makeViewInactive();
        markModified();
    }

    /**
     * Get the Control Record.
     * 
     * @return the Control Record.
     * @throws SAFRException
     * @throws SAFRNotFoundException
     *             If the Control Record specified is not found in the database
     */
    public ControlRecord getControlRecord() throws SAFRException {
        if (controlRecord == null) {
            if (controlRecordId != null && controlRecordId > 0) {
                // lazy initialize and cache the object
                this.controlRecord = SAFRApplication.getSAFRFactory()
                        .getControlRecord(controlRecordId, getEnvironmentId());
            }
        }
        return controlRecord;
    }

    /**
     * @param controlRecord
     *            the controlRecord to set
     */
    public void setControlRecord(ControlRecord controlRecord) {
        this.controlRecord = controlRecord;
        if (controlRecord == null) {
            this.controlRecordId = null;
        } else {
            this.controlRecordId = controlRecord.getId();
        }
        makeViewInactive();
        markModified();
    }

    /**
     * @return the writeExit
     */
    public UserExitRoutine getWriteExit() throws SAFRException {
        if (writeExit == null) {
            if (writeExitId != null && writeExitId > 0) {
                if (isForImport()) {
                    // this is called from import, use the model provider to get
                    // the UXR.
                    UserExitRoutineTransfer trans = (UserExitRoutineTransfer) provider
                            .get(UserExitRoutineTransfer.class, writeExitId);
                    this.writeExit = SAFRApplication.getSAFRFactory()
                            .initUserExitRoutine(trans);
                } else {
                    // lazy initialize and cache the object
                    this.writeExit = SAFRApplication.getSAFRFactory()
                            .getUserExitRoutine(writeExitId, this.getEnvironmentId());
                }
            }
        }
        return writeExit;
    }

    /**
     * @param writeExit
     *            the writeExit to set
     */
    public void setWriteExit(UserExitRoutine writeExit) {

        if (this.writeExit != null) {

            if ((writeExit != null)
                    && (!writeExit.getId().equals(this.writeExit.getId()))) {
                // different exit selected
                writeExitUerState = true;
            } else {
                writeExitUerState = false;
            }

        } else if (writeExit != null) {
            writeExitUerState = true;
        }

        if (writeExit == null) {
            this.writeExitId = null;
        } else {
            this.writeExitId = writeExit.getId();
        }
        this.writeExit = writeExit;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the writeExitParams
     */
    public String getWriteExitParams() {
        return writeExitParams;
    }

    /**
     * @param writeExitParams
     *            the writeExitParams to set
     */
    public void setWriteExitParams(String writeExitParams) {
        this.writeExitParams = writeExitParams;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the formatExit
     */
    public UserExitRoutine getFormatExit() throws SAFRException {
        if (formatExit == null) {
            if (formatExitId != null && formatExitId > 0) {
                if (isForImport()) {
                    // this is called from import, use the model provider to get
                    // the UXR.
                    UserExitRoutineTransfer trans = (UserExitRoutineTransfer) provider
                            .get(UserExitRoutineTransfer.class, formatExitId);
                    this.formatExit = SAFRApplication.getSAFRFactory()
                            .initUserExitRoutine(trans);
                } else {
                    // lazy initialize and cache the object
                    this.formatExit = SAFRApplication.getSAFRFactory()
                            .getUserExitRoutine(formatExitId, this.getEnvironmentId());
                }
            }
        }
        return formatExit;
    }

    /**
     * @param formatExit
     *            the formatExit to set
     */
    public void setFormatExit(UserExitRoutine formatExit) {

        if (this.formatExit != null) {

            if ((formatExit != null)
                    && (!formatExit.getId().equals(this.formatExit.getId()))) {
                // different exit selected
                formatExitUerState = true;

            } else {
                formatExitUerState = false;

            }

        } else if (formatExit != null) {
            // an exit selected
            formatExitUerState = true;
        }

        if (formatExit == null) {
            this.formatExitId = null;
        } else {
            this.formatExitId = formatExit.getId();
        }
        this.formatExit = formatExit;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the formatExitParams
     */
    public String getFormatExitParams() {
        return formatExitParams;
    }

    /**
     * @param formatExitParams
     *            the formatExitParams to set
     */
    public void setFormatExitParams(String formatExitParams) {
        this.formatExitParams = formatExitParams;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the fileFieldDelimiterCode
     */
    public Code getFileFieldDelimiterCode() {
        return fileFieldDelimiterCode;
    }

    /**
     * @param fileFieldDelimiterCode
     *            the fileFieldDelimiterCode to set
     */
    public void setFileFieldDelimiterCode(Code fileFieldDelimiterCode) {
        this.fileFieldDelimiterCode = fileFieldDelimiterCode;
        makeViewInactive();
        markModified();
    }

    /**
     * @return the fileStringDelimiterCode
     */
    public Code getFileStringDelimiterCode() {
        return fileStringDelimiterCode;
    }

    /**
     * @param fileStringDelimiterCode
     *            the fileStringDelimiterCode to set
     */
    public void setFileStringDelimiterCode(Code fileStringDelimiterCode) {
        this.fileStringDelimiterCode = fileStringDelimiterCode;
        makeViewInactive();
        markModified();
    }

    /**
     * Returns the logic text for the View's Format Record Filter.
     * 
     * @return a String of logic text
     */
    public String getFormatRecordFilter() {
        return formatRecordFilter;
    }

    /**
     * Sets the Format Record Filter of this view.
     * 
     * @param formatRecordFilter
     *            logic text string.
     */
    public void setFormatRecordFilter(String formatRecordFilter) {
        this.formatRecordFilter = formatRecordFilter;
        makeViewInactive();
        markModified();
    }

	public void setCompiledFormatRecordFilter(byte[] compiledFormatRecordFilter) {
		this.compiledFormatRecordFilter = compiledFormatRecordFilter;
	}
	
	public byte[] getCompiledFormatRecordFilter() {
		return compiledFormatRecordFilter;
	}

    /**
     * This method is used to check whether the format phase is in the use or
     * not.
     * 
     * @return true if the format phase is in use.
     */
    public Boolean isFormatPhaseInUse() {        
        if (outputPhase == OutputPhase.Format) {
            return true; 
        } else {
            return false;
        }
    }

    /**
     * This method is used to check whether aggregation is required for format
     * phase records.
     * 
     * @return true if aggregation is required.
     */
    public Boolean isFormatPhaseRecordAggregationOn() {
        if (isFormatPhaseRecordAggregationOn == null) {
            return false;
        } else {
            return isFormatPhaseRecordAggregationOn;
        }
    }

    /**
     * This method is used to set the aggregation of format phase records on.
     * 
     * @param isFormatPhaseRecordAggregationOn
     *            : true if it is to be aggregated.
     */
    public void setFormatPhaseRecordAggregationOn(Boolean isFormatPhaseRecordAggregationOn) {
        this.isFormatPhaseRecordAggregationOn = isFormatPhaseRecordAggregationOn;
        
        // set view column defaults.
        Code sum = (SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.RECORDAGGR).getCode("SUM"));
        for (ViewColumn col : viewColumns) {
            if (isFormatPhaseRecordAggregationOn) {
                if ((col.getDataTypeCode() != null && col.getDataTypeCode().getGeneralId() == Codes.ALPHANUMERIC) || 
                     col.isSortKey()) {
                    col.setRecordAggregationCode(null);                
                } else {
                    col.setRecordAggregationCode(sum);
                }
                
            } else {
                col.setRecordAggregationCode(null);                
            }
        }
        
        // set extract property defaults
        setExtractAggregateBySortKey(false);
        
        makeViewInactive();
        markModified();
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public OutputPhase getOutputPhase() {
        return outputPhase;
    }
    
    /**
     * This method is used to set the output format.
     * 
     * @param outputPhase
     *            : the Output Format which is to be set.
     * @throws SAFRException
     */
    private void setOutputPhase(OutputPhase outputPhase) throws SAFRException {
        this.outputPhase = outputPhase;
        if (outputPhase == OutputPhase.Extract) {
            
            setExtractWorkFileNo(1);
            
            // Clear relevant fields for output format Flat File.
            for (ViewColumn col : viewColumns.getActiveItems()) {
                if (col.isSortKey()) {
                    col.getViewSortKey().setDisplayModeCode(null);
                    col.setSortkeyFooterLabel(null);
                }
                col.setVisible(true);
                col.setSpacesBeforeColumn(0);
                col.setHeaderAlignmentCode(null);
                col.setGroupAggregationCode(null);
                col.setRecordAggregationCode(null);
            }

        } else {
            
            for (ViewColumn col : viewColumns.getActiveItems()) {
                // if record aggregation is ON and record aggregation code
                // is not SUM, set the same code in Group aggregation
                // funtion too.
                if (isFormatPhaseRecordAggregationOn) {
                    Code recordCode = col.getRecordAggregationCode();
                    if (recordCode == null) {
                        col.setGroupAggregationCode(null);
                    } else if (recordCode.getGeneralId() != Codes.SUM) {
                        col.setGroupAggregationCode(SAFRApplication.getSAFRFactory().getCodeSet(
                            CodeCategories.GROUPAGGR).getCode(recordCode.getKey()));
                    }
                }
                // Reset the display mode of sort key to default if output
                // format of View is changed.
                if (outputFormat == OutputFormat.Format_Report) {
                    if (col.isSortKey()) {
                        if (col.getViewSortKey().getDisplayModeCode() == null) {
                            col.getViewSortKey().setDisplayModeCode(SAFRApplication.getSAFRFactory().getCodeSet(
                                CodeCategories.SORTDSP).getCode((Codes.CATEGORIZE)));
                        }
                        if (col.getViewSortKey().getDisplayModeCode() != null && 
                            col.getViewSortKey().getDisplayModeCode().getGeneralId() == Codes.ASDATA) {
                            col.setSortkeyFooterLabel(null);
                        } else {
                            if (col.getSortkeyFooterLabel() == null) {
                                col.setSortkeyFooterLabel("Subtotal,");
                            }
                        }
                    }
                } else {
                    if (col.isSortKey()) {
                        col.getViewSortKey().setDisplayModeCode(null);
                        col.setSortkeyFooterLabel(null);
                    }
                    col.setSpacesBeforeColumn(0);
                    col.setHeaderAlignmentCode(null);
                    col.setGroupAggregationCode(null);
                }
            }
            
        }
        makeViewInactive();
        markModified();
    }

    /**
     * This method is used to set the type of output format.
     * 
     * @param outputFormat
     *            : the type of output format which is to be set.
     */
    public void setOutputFormat(OutputFormat outputFormat) {        
        if (outputFormat != null && outputFormat != this.outputFormat) {            
            this.outputFormat = outputFormat;
            switch (outputFormat) {
            case Extract_Fixed_Width_Fields:
                setOutputPhase(OutputPhase.Extract);
                setNonFormatReport();
                break;
            case Extract_Source_Record_Layout:
                setOutputPhase(OutputPhase.Extract);
                setExtractWorkFileNo(1);
                setFileFieldDelimiterCode(null);
                setFileStringDelimiterCode(null);
                for (ViewColumn col : viewColumns.getActiveItems()) {
                    // set effective date of all sources to Rundate
                    for (ViewColumnSource colSource : col
                            .getViewColumnSources().getActiveItems()) {
                        if (colSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                            colSource.setEffectiveDateTypeCode(SAFRApplication.getSAFRFactory().getCodeSet(
                                CodeCategories.RELPERIOD).getCode(Codes.RELPERIOD_RUNDATE));
                            colSource.setEffectiveDateValue("Runday()");
                        }
                    }
                    // set visible to true and aggregation to null
                    col.setVisible(true);
                    col.setRecordAggregationCode(null);
                    col.setGroupAggregationCode(null);
                }
                setNonFormatReport();
                break;
            case Format_Delimited_Fields:
                setOutputPhase(OutputPhase.Format);
                setFileFieldDelimiterCode(SAFRApplication.getSAFRFactory()
                        .getCodeSet(CodeCategories.FLDDELIM)
                        .getCode(Codes.BACK_SLASH));
                setFileStringDelimiterCode(SAFRApplication.getSAFRFactory()
                        .getCodeSet(CodeCategories.STRDELIM)
                        .getCode(Codes.DOUBLE_QUOTE));
                setHeaderRow(true);
                for (ViewColumn col : viewColumns.getActiveItems()) {
                    col.setGroupAggregationCode(null);
                    // Data Alignment should be cleared for Flat Files with
                    // Delimited Fields
                    col.setDataAlignmentCode(null);
                }
                setNonFormatReport();
                break;
            case Format_Fixed_Width_Fields:
                setOutputPhase(OutputPhase.Format);
                setFileFieldDelimiterCode(null);
                setFileStringDelimiterCode(null);
                setNonFormatReport();
                break;
            case Format_Report:
                setOutputPhase(OutputPhase.Format);
                setLinesPerPage(60);
                setReportWidth(132);
                break;
            }       
            setFormatPhaseRecordAggregationOn(false);            
        }
        makeViewInactive();
        markModified();
    }

    protected void setNonFormatReport() {
        setLinesPerPage(66);
        setReportWidth(250);
        
        // clear out sort key title settings
        for (ViewSortKey sortKey : getViewSortKeys()) {
            sortKey.setTitleField(null);
            sortKey.setTitleLength(null);
        }                
        for (ViewColumnSource columnSource : getViewColumnSources()) {
            columnSource.setSortKeyTitleLookupPathQueryBean(null);
            columnSource.setSortKeyTitleLRField(null);
        }
    }

    /**
     * Get Output LR id  
     * @return outputLRId
     */
    
    public Integer getOutputLRId() {
        return outputLRId;
    }
    
    /**
     * This method is to set the Output LR id.
     * 
     * @param outputLrId
     *            : the id to be set.
     * @throws SAFRException
     */
    void setOutputLRId(Integer outputLrId) {
        this.outputLRId = outputLrId;
    }

    /**
     * Get view dependencies from the view's logic text.
     * 
     * @return a list of the ViewlogicDependencies
     */
    public List<ViewLogicDependency> getViewLogicDependencies()
            throws SAFRException {
        if (viewLogicDependencies == null)
        {
            viewLogicDependencies = ViewFactory.getViewLogicDependencies(this);
        }
        return viewLogicDependencies;
    }
    
    public void setViewLogicDependencies(
        ArrayList<ViewLogicDependency> viewLogicDependencies) {
        this.viewLogicDependencies = viewLogicDependencies;        
    }
        
    /**
     * @return a list of the ViewSources
     */
    public SAFRList<ViewSource> getViewSources() {
        return viewSources;
    }

    /**
     * Create a new ViewSource, add it to this View and return the ViewSource
     * object.
     * 
     * @return the ViewSource
     * @throws SAFRException
     */
    public ViewSource addViewSource() throws SAFRException {
        ViewSource vs = new ViewSource(this);
        viewSources.add(vs);
        vs.setSequenceNo(this.viewSources.getActiveItems().size());
        vs.setId(dummySrcId++ * -1);        
        for (ViewColumn viewColumn : this.viewColumns.getActiveItems()) {
            addViewColumnSource(viewColumn, vs);
        }
        makeViewInactive();
        markModified();
        return vs;

    }

    /**
     * This method is used to remove view source.
     * 
     * @param viewSource
     *            : The view source which is to be removed.
     */
    public void removeViewSource(ViewSource viewSource) {
        // remove ViewColumnSources first. then remove view sources.
        for (ViewColumnSource src : viewSource.getViewColumnSources()
                .getActiveItems()) {
            removeViewColumnSource(src);
        }
        viewSources.remove(viewSource);
        for (int i = 0; i < this.viewSources.getActiveItems().size(); i++) {
            this.viewSources.getActiveItems().get(i).setSequenceNo(i + 1);
        }
        makeViewInactive();
        markModified();
    }

    /**
     * @return a list of the ViewColumns
     */
    public SAFRList<ViewColumn> getViewColumns() {
        return viewColumns;
    }

    /**
     * Create a new ViewColumn at the index specified.If the index is 0 or less
     * then the view column is added at the end of the list.Add it to this View
     * and return the ViewColumn object.
     * 
     * @param index
     *            : The index at which the view column is to be added. If the
     *            index is 0 or less then the view column is added at the end of
     *            the list.
     * @return the newly created view column.
     * @throws SAFRException
     */
    public ViewColumn addViewColumn(int index) throws SAFRException {
        // index is the actual column number. so deduct 1 from it to get the
        // list item number
        int num = index - 1;
        ViewColumn vc = new ViewColumn(this);
        if (num < 0) {
            viewColumns.add(vc);
            vc.setColumnNo(viewColumns.getActiveItems().size());
            calculateStartPosition();
        } else if (num >= 0) {
            viewColumns.add(num, vc);
            vc.setColumnNo(index);
            calculateStartPosition();
        }
        for (ViewSource viewSource : viewSources.getActiveItems()) {
            addViewColumnSource(vc, viewSource);
        }
        vc.setExtractAreaCode(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.EXTRACT)
                .getCode(Codes.DT_AREA));
        vc.setId(dummyColumnId++ * -1);
        makeViewInactive();
        markUpdated();
        return vc;

    }

    /**
     * This method is used to remove view column.
     * 
     * @param viewColumn
     *            : the view column which is to be removed.
     */
    public void removeViewColumn(ViewColumn viewColumn) {
        // remove the sort key.
        removeSortKey(viewColumn);
        this.viewColumns.remove(viewColumn);
        for (ViewColumnSource src : viewColumn.getViewColumnSources()) {
            removeViewColumnSource(src);
        }
        // calculate column numbers and start positions.
        calculateStartPosition();
        makeViewInactive();
        markUpdated();
    }

    /**
     * Returns a list of all of the ViewColumnSources for this View.
     * 
     * @return the ViewColumnSources
     */
    public SAFRList<ViewColumnSource> getViewColumnSources() {
        return viewColumnSources;
    }

    /**
     * Creates a new ViewColumnSource to associate the specified ViewColumn and
     * ViewSource, adds this new object to the View and returns it.
     * 
     * @param viewColumn
     *            one end of the ViewColumnSource association
     * @param viewSource
     *            the other end of the ViewColumnSource association
     * @return the ViewColumnSource
     * @throw IllegalArgumentException if a ViewColumnSource already exists for
     *        the specified ViewColumn and ViewSource
     */
    public ViewColumnSource addViewColumnSource(ViewColumn viewColumn,
            ViewSource viewSource) {
        for (ViewColumnSource vcs : viewColumnSources.getActiveItems()) {
            if (vcs.getViewColumn() == viewColumn
                    && vcs.getViewSource() == viewSource) {
                throw new IllegalArgumentException(
                        "A ViewColumnSource already exists for this ViewColumn and ViewSource.");
            }
        }
        ViewColumnSource vcSrc = new ViewColumnSource(viewColumn, viewSource);
        vcSrc.setId(dummyColumnSrcId++ * -1);
        vcSrc.setSourceType(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.COLSRCTYPE).getCode(Codes.CONSTANT));
        viewColumnSources.add(vcSrc);
        makeViewInactive();
        markUpdated();
        return vcSrc;
    }

    /**
     * This method is used to remove view column source.
     * 
     * @param viewColumnSource
     *            : the view column source which is to be removed.
     */
    private void removeViewColumnSource(ViewColumnSource viewColumnSource) {
        this.viewColumnSources.remove(viewColumnSource);
        makeViewInactive();
        markUpdated();
    }

    /**
     * @return a list of the ViewSortKeys
     */
    public SAFRList<ViewSortKey> getViewSortKeys() {
        return viewSortKeys;
    }

    /**
     * Returns the View header
     * 
     * @return {@link HeaderFooterItems} object representing the view header or
     *         null if the view is non-hardcopy view.
     */
    public HeaderFooterItems getHeader() {
        if (this.outputFormat != OutputFormat.Format_Report) {
            return null;
        }
        if (header == null) {
            header = new View.HeaderFooterItems(this, true);
        }
        return header;
    }

    /**
     * Return the View footer
     * 
     * @return {@link HeaderFooterItems} object representing the view footer or
     *         null if the view is non-hardcopy view.
     */
    public HeaderFooterItems getFooter() {
        if (this.outputFormat != OutputFormat.Format_Report) {
            return null;
        }
        if (footer == null) {
            footer = new View.HeaderFooterItems(this, false);
        }
        return footer;
    }

    /**
     * This class maintains a collection of Header/Footer Items.
     * 
     */
    public class HeaderFooterItems {

        private final boolean header;
        private final List<HeaderFooterItem> items = new ArrayList<HeaderFooterItem>();
        private final View view;

        /**
         * Use this constructor to create a new header or footer for a view.
         * 
         * @param view
         *            SAFR View for which a header or footer is to be created.
         * @param header
         *            boolean value indicating if a header or footer is to be
         *            created. TRUE will create a header while FALSE will create
         *            a footer.
         */
        HeaderFooterItems(View view, boolean header) {
            // Create new header footer instance
            this.header = header;
            this.view = view;
        }

        /**
         * Add a header/footer item.
         * 
         * @param funtionCode
         *            the function code to describe the item's content.
         * @param justifyCode
         *            the item's justify code specifying which area it belongs
         *            to (left/center/right).
         * @param row
         *            the row number of this item.
         * @param column
         *            the column number of this item.
         * @param itemText
         *            the user entered text. This is optional and is only used
         *            if the function code is 'TEXT'.
         * @return the {@link HeaderFooterItem} object representing the item
         *         added.
         * @throws IllegalArgumentException
         *             if any of the parameters other than itemText is null.
         */
        public HeaderFooterItem addItem(Code funtionCode, Code justifyCode,
                Integer row, Integer column, String itemText) {
            if (funtionCode == null || justifyCode == null || row == null
                    || column == null) {
                throw new IllegalArgumentException(
                        "A null value is passed for a non-null parameter.");
            }
            if (funtionCode.getGeneralId() != Codes.HF_TEXT) {
                itemText = null;
            }
            HeaderFooterItem item = new HeaderFooterItem(this, funtionCode,
                    justifyCode, row, column, itemText, view.getEnvironmentId());
            items.add(item);
            markModified();
            return item;
        }

        /**
         * Get a list of all items representing a header or footer.
         * 
         * @return a list of {@link HeaderFooterItem} objects.
         */
        public List<HeaderFooterItem> getItems() {
            List<HeaderFooterItem> itemList = new ArrayList<HeaderFooterItem>();
            itemList.addAll(items);
            return itemList;
        }

        void addItemInit(HeaderFooterItem item) {
            items.add(item);
        }
        
        /**
         * Removes all the items from the header or footer.
         */
        public void clearItems() {
            items.clear();
            markModified();
        }

        /**
         * Get the view associated with this Header or Footer.
         * 
         * @return View object.
         */
        View getView() {
            return view;
        }

        /**
         * Checks if this is a header or footer.
         * 
         * @return True if header, else False.
         */
        boolean isHeader() {
            return header;
        }
    }

    /**
     * This method is used to convert the type code into an output format
     * depending on the view type code and the output format code.This method is
     * called from set object data method. <br>
     * <br>
     * The logic for this conversion is mentioned in the sheet 'View Types' in
     * CQ 1517.
     * 
     * @param typeCode
     *            : the general id of type code.
     * @param outputFormatCode
     *            : the general id of output format.
     */
    private void convertTypeCodeToOutputFormat(int typeCode, int outputFormatCode) {
        switch (typeCode) {
        case Codes.SUMMARY:
            outputPhase = OutputPhase.Format;
            switch (outputFormatCode) {
            case Codes.DELIMITED:
                outputFormat = OutputFormat.Format_Delimited_Fields;
                break;
            case Codes.FILE:
                outputFormat = OutputFormat.Format_Fixed_Width_Fields;
                break;
            case Codes.HARDCOPY:
                outputFormat = OutputFormat.Format_Report;
                break;
            }
            isFormatPhaseRecordAggregationOn = true;
            break;
        case Codes.DETAIL:
            outputPhase = OutputPhase.Format;
            switch (outputFormatCode) {
            case Codes.DELIMITED:
                outputFormat = OutputFormat.Format_Delimited_Fields;
                break;
            case Codes.FILE:
                outputFormat = OutputFormat.Format_Fixed_Width_Fields;
                break;
            case Codes.HARDCOPY:
                outputFormat = OutputFormat.Format_Report;
                break;
            }
            // For Detail view types, format phase is on but record aggregation
            // is off by default.
            isFormatPhaseRecordAggregationOn = false;
            break;
        case Codes.EXTRACT_ONLY:
            switch (outputFormatCode) {
            case Codes.FILE:
                outputPhase = OutputPhase.Extract;
                outputFormat = OutputFormat.Extract_Fixed_Width_Fields;
            }
            // Format phase and Record Aggregation are always off by default for
            // Extract Only View types.
            isFormatPhaseRecordAggregationOn = false;
            break;
        case Codes.COPY_INPUT:
            switch (outputFormatCode) {
            case Codes.FILE:
                outputPhase = OutputPhase.Extract;
                outputFormat = OutputFormat.Extract_Source_Record_Layout;
            }
            // Format phase and Record Aggregation are always off by default for
            // Copy Input View types.
            isFormatPhaseRecordAggregationOn = false;
            break;
        default:
            outputPhase = OutputPhase.Extract;
            outputFormat = OutputFormat.Extract_Fixed_Width_Fields;            
            isFormatPhaseRecordAggregationOn = false;
        }
    }

    
    /**
     * This method is used to convert output format to codes that can be set in
     * transfer object.This method is called from setTransferData method. <br>
     * <br>
     * The logic for this conversion is mentioned in the sheet 'View Types' in
     * CQ 1517.
     * 
     * @param trans
     *            : the transfer object in which the values are to be set.
     */
    private void convertOutputFormatToTypeCode(ViewTransfer trans) {
        switch (outputFormat) {
        case Extract_Fixed_Width_Fields:
            trans.setOutputFormatCode("FILE");
            trans.setTypeCode("EXTR");
            break;
        case Extract_Source_Record_Layout:
            trans.setOutputFormatCode("FILE");
            trans.setTypeCode("COPY");
            break;
        case Format_Delimited_Fields:
            trans.setOutputFormatCode("DELIM");
            if (isFormatPhaseRecordAggregationOn) {
                trans.setTypeCode("SUMRY");
            } else {
                trans.setTypeCode("DETL");
            }
            break;
        case Format_Fixed_Width_Fields:
            trans.setOutputFormatCode("FILE");
            if (isFormatPhaseRecordAggregationOn) {
                trans.setTypeCode("SUMRY");
            } else {
                trans.setTypeCode("DETL");
            }
            break;
        case Format_Report:
            trans.setOutputFormatCode("HCOPY");
            if (isFormatPhaseRecordAggregationOn) {
                trans.setTypeCode("SUMRY");
            } else {
                trans.setTypeCode("DETL");
            }
            break;
        }        
    }

    /**
     * This method is used to change the sequence of a sort key column.
     * 
     * @param from
     *            : The position of the sort key column whose sequence is to be
     *            changed.
     * @param to
     *            : The position at which the sort key column is to be placed.
     */
    void changeSortKeySequence(int from, int to) {
        int listFromPosition = from - 1;// sort key sequence number is relative
        // to 1.
        int listToPosition = to - 1;
        if (this.viewSortKeys.getActiveItems().isEmpty()) {
            return;
        }
        if (from <= 0 || from > this.viewSortKeys.getActiveItems().size()
                || to <= 0 || to > this.viewSortKeys.getActiveItems().size()
                || from == to) {
            return;
        }
        if (from < to) {
            this.viewSortKeys.add(to, viewSortKeys.get(listFromPosition));
            this.viewSortKeys.remove(listFromPosition);
        } else {
            ViewSortKey removedSortKey = this.viewSortKeys
                    .remove(listFromPosition);
            this.viewSortKeys.add(listToPosition, removedSortKey);
        }
        markModified();
    }

    /**
     * This method is used to remove the sort key.After removing it from the
     * list,the sequence number of rest of the sort keys is calculated again.
     * 
     * @param viewColumn
     *            : The sort key column from which the sort key is to be
     *            removed.
     * @return The View sort key which is being removed or null if nothing was
     *         removed.
     */
    public ViewSortKey removeSortKey(ViewColumn viewColumn) {
        if (this.viewSortKeys.getActiveItems().isEmpty()) {
            return null;
        }
        ViewSortKey removedSortKey = null;
        if (viewColumn.isSortKey()) {
            removedSortKey = viewColumn.getViewSortKey();
            removedSortKey.removeSortKeyTitleField();
            this.viewSortKeys.remove(removedSortKey);
            calculateStartPosition();
        }
        markModified();
        return removedSortKey;
    }

    /**
     * Adds a new sort key on a column.
     * 
     * @param viewColumn
     *            The column to add the sort key on.
     * @return The newly added sort key or null if the column already has a sort
     *         key.
     * @throws SAFRException
     */
    public ViewSortKey addSortKey(ViewColumn viewColumn) throws SAFRException {
        if (viewColumn.isSortKey()) {
            return null;
        }
        ViewSortKey viewSortKey = new ViewSortKey(viewColumn);
        viewSortKey.setId(dummyKeyId++ * -1);
        // the new sort key inherits certain properties from view column. Also,
        // some view column properties change too.
        // Jaydeep CQ 6015:Sort Properties > DataType: Restrict the datatypes
        // available Date : Sept 16,2010.
        switch (viewColumn.getDataTypeCode().getGeneralId().intValue()) {
        case Codes.ALPHANUMERIC:
            viewSortKey.setDataTypeCode(viewColumn.getDataTypeCode());
            break;

        case Codes.BINARY:
        case Codes.BINARY_CODED_DECIMAL:
        case Codes.BINARY_SORTABLE:
            Code code = SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.DATATYPE)
                    .getCode(Codes.BINARY_SORTABLE);
            viewSortKey.setDataTypeCode(code);
            break;

        default:
            Code defCode = SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.DATATYPE)
                    .getCode(Codes.PACKED_SORTABLE);
            viewSortKey.setDataTypeCode(defCode);
            break;
        }
        viewSortKey.setDateTimeFormatCode(viewColumn.getDateTimeFormatCode());
        viewSortKey.setLength(viewColumn.getLength().intValue());
        viewSortKey.setDecimalPlaces(viewColumn.getDecimals().intValue());
        viewSortKey.setSigned(viewColumn.isSigned());
        // some items are to be set only if output format is Hardcopy
        if (outputFormat == OutputFormat.Format_Report) {
            viewSortKey.setSortkeyLabel(viewColumn.getInitialSortKeyLabel());

            // Changed by Shruti(20/04/10) for CQ 7150.
            if (viewColumn.getSortkeyFooterLabel() == null) {
                viewColumn.setSortkeyFooterLabel("Subtotal,");
            } else {
                viewColumn.setSortkeyFooterLabel(viewColumn
                        .getSortkeyFooterLabel());
            }
            viewSortKey.setDisplayModeCode(SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.SORTDSP)
                    .getCode((Codes.CATEGORIZE)));
        }
        viewSortKey.setFooterOption(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.SORTBRKFTR).getCode((Codes.PRINT)));
        viewSortKey.setHeaderOption(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.SORTBRKHDR).getCode((Codes.PSAME)));
        viewSortKey.setSortSequenceCode(SAFRApplication.getSAFRFactory()
                .getCodeSet(CodeCategories.SORTSEQ).getCode((Codes.ASCENDING)));
        viewSortKey.setKeySequenceNumber(this.viewSortKeys.getActiveItems()
                .size() + 1);
        viewSortKeys.add(viewSortKey);

        viewColumn.setRecordAggregationCode(null);
        viewColumn.setGroupAggregationCode(null);

        calculateStartPosition();
        markModified();
        return viewSortKey;
    }

    /**
     * This method is used to recalculate the sequence number of all the sort
     * keys.
     */
    void reCalcSortKeySequence() {
        for (int i = 0; i < this.viewSortKeys.getActiveItems().size(); i++) {
            this.viewSortKeys.getActiveItems().get(i)
                    .setKeySequenceNumber(i + 1);
        }
    }

    /**
     * This method is used to calculate the start position of view columns and
     * sort keys.The start position of a view column depends on the output
     * format of the view, the visibility of the column and whether the column
     * is a sort key column or not.
     */
    public void calculateStartPosition() {
        List<ViewColumn> activeViewColumns = this.viewColumns.getActiveItems();
        if (outputFormat == OutputFormat.Format_Fixed_Width_Fields) {
            int prevStartPosition = 1;
            int prevLength = 0;
            for (int i = 0; i < activeViewColumns.size(); i++) {
                ViewColumn currentViewColumn = activeViewColumns.get(i);
                currentViewColumn.setColumnNo(i + 1);
                if (currentViewColumn.isVisible()) {
                    if (currentViewColumn.getSpacesBeforeColumn() == null) {
                        currentViewColumn.setSpacesBeforeColumn(0);
                    }
                    int currentStartPos = currentViewColumn
                            .getSpacesBeforeColumn()
                            + prevStartPosition
                            + prevLength;
                    currentViewColumn.setStartPosition(currentStartPos);
                    prevStartPosition = currentStartPos;
                    prevLength = currentViewColumn.getLength().intValue();

                }// if column is not visible
                else {
                    currentViewColumn.setStartPosition(0);
                }    
            }
        } else if (outputFormat == OutputFormat.Extract_Fixed_Width_Fields) {
            int prevStartPosition = 1;
            int prevLength = 0;
            for (int i = 0; i < activeViewColumns.size(); i++) {
                ViewColumn currentViewColumn = activeViewColumns.get(i);
                currentViewColumn.setColumnNo(i + 1);
                if (!currentViewColumn.isSortKey()) {
                    int currentStartPos = prevStartPosition + prevLength;
                    currentViewColumn.setStartPosition(currentStartPos);
                    prevStartPosition = currentStartPos;
                    prevLength = currentViewColumn.getLength().intValue();
                }// if column is sort key
                else {
                    currentViewColumn.setStartPosition(0);
                }
            }
        } else {
            for (int i = 0; i < activeViewColumns.size(); i++) {
                ViewColumn currentViewColumn = activeViewColumns.get(i);
                currentViewColumn.setColumnNo(i + 1);
            }            
        }
        
        // Calculate sort key start position.
        int prevStartPosition = 1;
        int prevLength = 0;
        for (int i = 0; i < this.viewSortKeys.getActiveItems().size(); i++) {
            ViewSortKey currentSortKey = this.viewSortKeys.getActiveItems().get(i);
            currentSortKey.setKeySequenceNumber(i + 1);
            int currentSortKeyStartPos = prevLength + prevStartPosition;
            currentSortKey.setStartPosition(currentSortKeyStartPos);
            prevStartPosition = currentSortKeyStartPos;
            prevLength = currentSortKey.getLength().intValue();
        }
        
        // recalc ordinal positions
        calculateOrdinalPositions();        
    }

    /**
     * Calculates View length based on the last column
     * @return view length
     */
    public int getViewLength() {
        List<ViewColumn> activeViewColumns = viewColumns.getActiveItems();
        int visIdx = activeViewColumns.size();
        if (visIdx > 0) {
            // work our way backwards until we find a visible column
            ViewColumn col = activeViewColumns.get(visIdx-1);   
            while (!col.isVisible() && visIdx > 0) {
                visIdx--;
                col = activeViewColumns.get(visIdx);
            }
            if (col.isVisible() && col.getStartPosition() != null && col.getLength() != null) {
                return col.getStartPosition()+col.getLength()-1;
            }
            else {
                return 0;
            }
        }
        else {
            return 0;
        }
    }
    
    /**
     * Sets the ordinal positions of all the visible columns. Invisible columns
     * will have ordinal position 0.
     */
    private void calculateOrdinalPositions() {
        int pos = 1; // start with 1
        for (ViewColumn col : viewColumns.getActiveItems()) {
            if (col.isVisible()) {
                col.setOrdinalPosition(pos++);
            } else {
                col.setOrdinalPosition(0);
            }
        }
    }

    /**
     * This method is used to move a column to one position right.
     * 
     * @param viewColumn
     *            : The column which is to be moved to one position right.
     */
    public void moveColumnRight(ViewColumn viewColumn) {
        List<ViewColumn> activeItems = this.viewColumns.getActiveItems();
        int index = activeItems.indexOf(viewColumn);
        if (activeItems.isEmpty()) {
            return;
        }
        if (index > activeItems.size() || index < 0) {
            return;
        }
        ViewColumn tmpCol = activeItems.get(index + 1);
        ViewColumn item1 = activeItems.get(index);
        int moveToIndex = viewColumns.indexOf(tmpCol);
        int moveFromIndex = viewColumns.indexOf(item1);
        viewColumns.remove(moveToIndex);
        viewColumns.add(moveFromIndex, tmpCol);
        calculateStartPosition();
        makeViewInactive();
        markModified();
    }

    /**
     * This method is used to move a column to one position left.
     * 
     * @param viewColumn
     *            : The column which is to be moved to one position left.
     */
    public void moveColumnLeft(ViewColumn viewColumn) {
        List<ViewColumn> activeItems = this.viewColumns.getActiveItems();
        int index = activeItems.indexOf(viewColumn);
        if (activeItems.isEmpty()) {
            return;
        }
        if (index > (activeItems.size() - 1) || index < 1) {
            return;
        }
        ViewColumn tmpCol = activeItems.get(index - 1);
        ViewColumn item1 = activeItems.get(index);
        int moveToIndex = viewColumns.indexOf(tmpCol);
        int moveFromIndex = viewColumns.indexOf(item1);
        viewColumns.remove(moveToIndex);
        viewColumns.add(moveFromIndex, tmpCol);
        calculateStartPosition();
        makeViewInactive();
        markModified();
    }

    /**
     * Validate format record filter using this View.
     * 
     * @param logicText
     *            The format record filter logic text to be validated.
     * @throws DAOException
     * @throws SAFRException
     *             SAFRValidation exception will be thrown with a list of
     *             validation errors.
     */
    public void validateFormatRecordFilter(String logicText) {
        LogicTextSyntaxChecker.checkSyntaxFormatFilter(logicText, this);
    }

    /**
     * Call this method instead of the activate method if the View will be
     * activated in batch. A batch activated view cannot be modified from its
     * persistent state prior to batch activation, so because only minimal view
     * data is changed by activation, storing can be done more efficiently than
     * if it was modified then activated. This method sets a property indicating
     * the view has been batch activated, then calls the normal activate method.
     */
    public void batchActivate() throws DAOException, SAFRException {
        this.batchActivated = true;
        ViewActivator activator = new ViewActivator(this);
        ViewActivator.setSite(null);
        activator.batchActivate();
    }

    /**
     * Invoke activation 
     * @param iWorkbenchPartSite 
     * 
     * @throws DAOException, SAFRException
     */ 
    public void activate(IWorkbenchPartSite iWorkbenchPartSite) throws DAOException, SAFRException {
        ViewActivator activator = new ViewActivator(this);
        ViewActivator.setSite(iWorkbenchPartSite);
        activator.activate();
    }
    
    private void checkStoreNormalUser() throws SAFRException, DAOException {
        // CQ 8184 Santhosh K Bhukya 14/07/2010 UI Changes for General user.

        // If user is not a System Admin or Environment Admin then check edit
        // rights on the components which are used in the View.

        if (!isForMigration()) {
            String msgString = "The user does not have necessary edit rights on the following component(s):" + SAFRUtilities.LINEBREAK;
            String compListString = "";
            int countViewSrcLR = 1;
            int countViewSrcLF = 1;
            int countColSrcLR = 1;

            // check for View Sources LR.
            List<ViewSource> viewSrcs = this.getViewSources()
                    .getActiveItems();
            for (ViewSource viewSource : viewSrcs) {
                if (viewSource.getPersistence().equals(SAFRPersistence.NEW)) {
                    if (SAFRApplication.getUserSession().getEditRights(ComponentType.LogicalRecord,
                        viewSource.getLrFileAssociation().getAssociatingComponentId(),
                        getEnvironmentId()) == EditRights.None) {

                        if (countViewSrcLR == 1) {
                            compListString += SAFRUtilities.LINEBREAK + "View Source Logical Record(s):" + SAFRUtilities.LINEBREAK;
                        }
                        countViewSrcLR++;

                        compListString += ""
                                + viewSource.getLrFileAssociation()
                                        .getAssociatingComponentName()
                                + " ["
                                + viewSource.getLrFileAssociation()
                                        .getAssociatingComponentId()
                                + "]" + SAFRUtilities.LINEBREAK;
                    }
                }
            }

            // check for View Sources LF.
            for (ViewSource viewSource : viewSrcs) {
                if (viewSource.getPersistence().equals(SAFRPersistence.NEW)) {
                    if (SAFRApplication.getUserSession().getEditRights(ComponentType.LogicalFile,
                        viewSource.getLrFileAssociation().getAssociatedComponentIdNum(),
                        getEnvironmentId()) == EditRights.None) {

                        if (countViewSrcLF == 1) {
                            compListString += SAFRUtilities.LINEBREAK + "View Source Logical File(s):" + SAFRUtilities.LINEBREAK;
                        }
                        countViewSrcLF++;

                        compListString += ""
                                + viewSource.getLrFileAssociation()
                                        .getAssociatedComponentName()
                                + " ["
                                + viewSource.getLrFileAssociation()
                                        .getAssociatedComponentIdNum()
                                + "]" + SAFRUtilities.LINEBREAK;
                    }
                }
            }

            // Check for view column sources LR.

            List<ViewColumnSource> viewColSrcs = this
                    .getViewColumnSources().getActiveItems();

            for (ViewColumnSource viewColumnSource : viewColSrcs) {
                if (viewColumnSource.getPersistence().equals(
                        SAFRPersistence.NEW)) {
                    if (viewColumnSource.getLogicalRecordQueryBean() != null) {
                        if (SAFRApplication.getUserSession().getEditRights(ComponentType.LogicalRecord,
                            viewColumnSource.getLogicalRecordQueryBean().getId(), 
                            this.getEnvironmentId()) == EditRights.None) {                           

                            if (countColSrcLR == 1) {
                                compListString += SAFRUtilities.LINEBREAK + "Column Source Logical Record(s):" + SAFRUtilities.LINEBREAK;
                            }
                            countColSrcLR++;

                            compListString += ""

                                    + viewColumnSource
                                            .getLogicalRecordQueryBean()
                                            .getName()
                                    + " ["
                                    + viewColumnSource
                                            .getLogicalRecordQueryBean()
                                            .getId() + "]" + SAFRUtilities.LINEBREAK;
                        }
                    }
                }
            }

            // Check for Extract or Format LF
            if (this.getExtractFileAssociation() != null
                        && this.getExtractFileAssociation().getAssociationId() != null 
                        && this.getExtractFileAssociation().getAssociationId() > 0) {
                if (SAFRApplication.getUserSession().getEditRights(ComponentType.LogicalFile,
                    this.getExtractFileAssociation().getAssociatingComponentId(),
                    getEnvironmentId()) == EditRights.None) {
                    compListString += SAFRUtilities.LINEBREAK + "Output Logical File: " + SAFRUtilities.LINEBREAK
                            + this.getExtractFileAssociation()
                                    .getAssociatingComponentName()
                            + " ["
                            + this.getExtractFileAssociation()
                                    .getAssociatingComponentId() + "]" + SAFRUtilities.LINEBREAK;
                }
                // Check for Extract or Format PF
                if (SAFRApplication.getUserSession().getEditRights(ComponentType.PhysicalFile,
                    this.getExtractFileAssociation().getAssociatedComponentIdNum(),
                    this.getEnvironmentId()) == EditRights.None) {
                    compListString += SAFRUtilities.LINEBREAK + "Output Physical File: " + SAFRUtilities.LINEBREAK
                            + this.getExtractFileAssociation()
                                    .getAssociatedComponentName()
                            + " ["
                            + this.getExtractFileAssociation()
                                    .getAssociatedComponentIdNum() + "]" + SAFRUtilities.LINEBREAK;
                }
            }

            // Check for Write Exit
            if (writeExitUerState && this.getWriteExit() != null) {

                if (SAFRApplication.getUserSession().getEditRights(ComponentType.UserExitRoutine,
                    this.getWriteExit().getId(),this.getEnvironmentId()) == EditRights.None) {
                    compListString += SAFRUtilities.LINEBREAK + "Output User-Exit Routine: " + SAFRUtilities.LINEBREAK
                            + this.getWriteExit().getName() + " ["
                            + this.getWriteExit().getId() + "]" + SAFRUtilities.LINEBREAK;
                }
            }

            // Check for Format Exit
            if (formatExitUerState && this.getFormatExit() != null) {

                if (SAFRApplication.getUserSession().getEditRights(ComponentType.UserExitRoutine,
                    this.getFormatExit().getId(),this.getEnvironmentId()) == EditRights.None) {
                    compListString += SAFRUtilities.LINEBREAK + "Format User-Exit Routine: " + SAFRUtilities.LINEBREAK
                            + this.getFormatExit().getName() + " ["
                            + this.getFormatExit().getId() + "]" + SAFRUtilities.LINEBREAK;
                }
            }

            if (!compListString.equals("")) {
                // no rights on associated LR, LF, PF, UER.
                throw new SAFRException(msgString + compListString);
            }
        } // end of edit rights check
    }
    
    @Override
    public void store() throws SAFRException, DAOException {
        if (isForMigration()) {
            if (!SAFRApplication.getUserSession().isAdminOrMigrateInUser(this.getEnvironmentId())) {
                String msg = "The user is not authorized to migrate into Environment "
                    + getEnvironmentId();               
                throw new SAFRException(msg);
            }
        // check if we are a normal user
        } else {
       
            if (this.id == 0) {
                if (!hasCreatePermission()) {
                    throw new SAFRException("The user is not authorized to create a new view.");
                }
            } else {
                if (!hasUpdateRights() && !isBatchActivated()) {
                    throw new SAFRException("The user is not authorized to update this view.");
                }
            }
            checkStoreNormalUser();
        }

        
        List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();

        ViewTransfer viewTrans = new ViewTransfer();
        setTransferData(viewTrans);
        // CQ 7329 Kanchan Rauthan 04/03/2010 To show error if view is
        // already deleted from database and user still tries to save it.
        boolean success = false;
        boolean isImportOrMigrate = isForImport() || isForMigration() ? true : false;
        try {

            while (!success) {
                try {
                    // Begin Transaction
                    DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
                    viewTrans = DAOFactoryHolder.getDAOFactory().getViewDAO().persistView(viewTrans);
                    viewTrans.setFormatFilterlogic(formatRecordFilter);
                    viewTrans.setForImport(isForImport()); // retain import flag
                    setObjectData(viewTrans);
                    savedObjs.add(this);
                        
                    if (!isBatchActivated() && !isForMigration() && !SAFRApplication.getUserSession().isSystemAdministrator()) {
                        SAFRApplication.getUserSession().getGroup().assignComponentFullRights(
                            this, ComponentType.View);
                    }
                                        
					
                    // If view batch activated, ViewSources haven't changed so don't save them
                    if (!batchActivated) {
                        // save the ViewSource contents
                        List<ViewSourceTransfer> transList = new ArrayList<ViewSourceTransfer>();
                        HashMap<ViewSourceTransfer, ViewSource> viewSourceMap = new HashMap<ViewSourceTransfer, ViewSource>();
                        List<Integer> deletedIds = new ArrayList<Integer>();
                        for (ViewSource viewSource : viewSources) {
                            if (viewSource.getPersistence() == SAFRPersistence.DELETED) {
                                deletedIds.add(viewSource.getId());
                            } else if (viewSource.isForImport()
                                    || viewSource.isForMigration()
                                    || viewSource.getPersistence() != SAFRPersistence.OLD) {
                                ViewSourceTransfer viewSourceTransfer = new ViewSourceTransfer();
                                viewSource.setTransferData(viewSourceTransfer);
                                transList.add(viewSourceTransfer);
                                viewSourceMap.put(viewSourceTransfer,viewSource);
                            }
                        }
                        if (transList.size() > 0) {
                            DAOFactoryHolder.getDAOFactory().getViewSourceDAO()
                                    .persistViewSources(transList);
                            for (int i = 0; i < transList.size(); i++) {
                                ViewSourceTransfer viewSourceTrans = transList.get(i);
                                ViewSource tmpSrc = viewSourceMap.get(viewSourceTrans);
                                tmpSrc.setObjectData(viewSourceTrans);
                                savedObjs.add(tmpSrc);
                            }
                        }
                        if (deletedIds.size() > 0) {
                            DAOFactoryHolder.getDAOFactory().getViewSourceDAO().removeViewSources(
                                deletedIds,getEnvironmentId());
                            viewSources.flushDeletedItems();
                        }
                    }
                    
					
                    // TC18596 removed the conditional check !batchActivated
                    // because activation can modify a View Column
                    
                    // save ViewColumn Contents
                    List<ViewColumnTransfer> colTransList = new ArrayList<ViewColumnTransfer>();
                    HashMap<ViewColumnTransfer, ViewColumn> viewColumnMap = new HashMap<ViewColumnTransfer, ViewColumn>();
                    List<Integer> deletedColIds = new ArrayList<Integer>();
                    for (ViewColumn viewColumn : viewColumns) {

                        if (viewColumn.getPersistence() == SAFRPersistence.DELETED) {
                            deletedColIds.add(viewColumn.getId());
                        } else if (viewColumn.isForImport()
                                || viewColumn.isForMigration()
                                || viewColumn.getPersistence() != SAFRPersistence.OLD) {
                            ViewColumnTransfer viewColumnTransfer = new ViewColumnTransfer();
                            viewColumn.setTransferData(viewColumnTransfer);
                            colTransList.add(viewColumnTransfer);
                            viewColumnMap.put(viewColumnTransfer, viewColumn);
                        }
                    }
                    if (deletedColIds.size() > 0) {
                        DAOFactoryHolder.getDAOFactory().getViewColumnDAO().removeViewColumns(
                            deletedColIds,getEnvironmentId());
                        viewColumns.flushDeletedItems();
                    }
                    
                    if (colTransList.size() > 0) {
                        DAOFactoryHolder.getDAOFactory().getViewColumnDAO().persistViewColumns(colTransList);
                        for (int i = 0; i < colTransList.size(); i++) {
                            ViewColumnTransfer viewColumnTrans = colTransList.get(i);
							ViewColumn tmpCol = viewColumnMap.get(viewColumnTrans);
							tmpCol.setObjectData(viewColumnTrans);
							savedObjs.add(tmpCol);
						}
					}
					
					// The ViewColumn is saved and its id is generated. Save LTs for ViewColumn.
                    
                    // TC18596 removed the conditional check !batchActivated
                    // because activation can modify a View Col Src ECA
                    
                    // save viewColumnSource contents
                    List<ViewColumnSourceTransfer> colSourceTransList = new ArrayList<ViewColumnSourceTransfer>();
                    HashMap<ViewColumnSourceTransfer, ViewColumnSource> viewColumnSourceMap = new HashMap<ViewColumnSourceTransfer, ViewColumnSource>();
                    List<Integer> deletedColSourceIds = new ArrayList<Integer>();
                    for (ViewColumnSource viewColumnSource : viewColumnSources) {

                        if (viewColumnSource.getPersistence() == SAFRPersistence.DELETED) {
                            deletedColSourceIds.add(viewColumnSource.getId());
                        } else if (viewColumnSource.isForImport()
                                || viewColumnSource.isForMigration()
                                || viewColumnSource.getPersistence() != SAFRPersistence.OLD) {
                            ViewColumnSourceTransfer viewColumnSourceTransfer = new ViewColumnSourceTransfer();
                            viewColumnSource.setTransferData(viewColumnSourceTransfer);
                            colSourceTransList.add(viewColumnSourceTransfer);
                            viewColumnSourceMap.put(viewColumnSourceTransfer,viewColumnSource);
                        }
                    }
                    if (deletedColSourceIds.size() > 0) {
                        DAOFactoryHolder.getDAOFactory().getViewColumnSourceDAO().removeViewColumnSources(
                            deletedColSourceIds,getEnvironmentId());
                        viewColumnSources.flushDeletedItems();

                    }                    
                    if (colSourceTransList.size() > 0) {
                        DAOFactoryHolder.getDAOFactory().getViewColumnSourceDAO()
                        .persistViewColumnSources(colSourceTransList);
                        for (int i = 0; i < colSourceTransList.size(); i++) {
                            ViewColumnSourceTransfer viewColumnSourceTrans = colSourceTransList.get(i);
                            ViewColumnSource tmpViewCS = viewColumnSourceMap.get(viewColumnSourceTrans);
                            tmpViewCS.setObjectData(viewColumnSourceTrans);
                            savedObjs.add(tmpViewCS);
                        }
                    }
                    
                    // The ViewColumnSource is saved and its id is
                    // generated. Save LTs for ViewColumnSource.
                    for (ViewColumnSource viewColumnSource : viewColumnSources) {
                        // set extract column assignment text and the
                        // compiled version if this view is active.
                        if (!isForImport() && !isForMigration() && 
                            viewColumnSource.getSourceType().getGeneralId() == Codes.FORMULA) {
                            if (viewColumnSource.getExtractColumnAssignment() == null || 
                                    viewColumnSource.getExtractColumnAssignment().isEmpty()) {
                                logger.warning("No logic text for View Column Source "
                                        + viewColumnSource.getId());
                                if (!getConfirmWarningStrategy().confirmWarning("Saving View",
                                    "Extract Column Assignment in Column: "+ 
                                    viewColumnSource.getViewColumn().getColumnNo()
                                    + ", View Source: "+ viewColumnSource.getViewSource().getSequenceNo()
                                    + " contains no logic text. Continue saving?")) {
                                    SAFRCancelException svce = new SAFRCancelException();
                                    throw svce;
                                }
                            }
                        }
                    }
                    
                    // TC18596 removed the conditional check !batchActivated
                    // because activation can modify a View Sort Key
                    
                    // save view sort key contents.
                    List<ViewSortKeyTransfer> sortKeyTransList = new ArrayList<ViewSortKeyTransfer>();
                    HashMap<ViewSortKeyTransfer, ViewSortKey> viewSortKeyMap = new HashMap<ViewSortKeyTransfer, ViewSortKey>();
                    List<Integer> deletedSortKeyIds = new ArrayList<Integer>();
                    for (ViewSortKey viewSortKey : viewSortKeys) {
                        if (viewSortKey.getPersistence() == SAFRPersistence.DELETED) {
                            deletedSortKeyIds.add(viewSortKey.getId());
                        } else if (viewSortKey.isForImport()
                                || viewSortKey.isForMigration()
                                || viewSortKey.getPersistence() != SAFRPersistence.OLD) {
                            ViewSortKeyTransfer viewSortKeyTransfer = new ViewSortKeyTransfer();
                            viewSortKey.setTransferData(viewSortKeyTransfer);
                            sortKeyTransList.add(viewSortKeyTransfer);
                            viewSortKeyMap
                                    .put(viewSortKeyTransfer, viewSortKey);
                        }
                    }
                    if (sortKeyTransList.size() > 0) {
                        DAOFactoryHolder.getDAOFactory().getViewSortKeyDAO()
                                .persistViewSortKeys(sortKeyTransList);
                        for (int i = 0; i < sortKeyTransList.size(); i++) {
                            ViewSortKeyTransfer viewSortKeyTrans = sortKeyTransList
                                    .get(i);
                            ViewSortKey tmpSK = viewSortKeyMap
                                    .get(viewSortKeyTrans);
                            tmpSK.setObjectData(viewSortKeyTrans);
                            savedObjs.add(tmpSK);
                        }
                    }
                    if (deletedSortKeyIds.size() > 0) {
                        DAOFactoryHolder
                                .getDAOFactory()
                                .getViewSortKeyDAO()
                                .removeViewSortKeys(deletedSortKeyIds,
                                        getEnvironmentId());
                        viewSortKeys.flushDeletedItems();
                    }
                                        
					
                    // If view batch activated, Headers/Footers haven't changed so don't save them
                    if (!batchActivated) {
                        if (header != null || footer != null) {
                            // save view header footer for view of type hardcopy
                            List<HeaderFooterItemTransfer> viewHeaderFooterItemsTransfers = new ArrayList<HeaderFooterItemTransfer>();
                            if (this.outputFormat == OutputFormat.Format_Report) {
                                // merging the list of HeaderFooterItems for Header
                                // and
                                // Footer
                                if (header != null) {
                                    for (HeaderFooterItem HFitem : this.header
                                            .getItems()) {
                                        HeaderFooterItemTransfer hfItemTransfer = new HeaderFooterItemTransfer();
                                        HFitem.setTransferData(hfItemTransfer);
                                        viewHeaderFooterItemsTransfers
                                                .add(hfItemTransfer);

                                    }
                                }
                                if (footer != null) {
                                    for (HeaderFooterItem HFitem : this.footer
                                            .getItems()) {
                                        HeaderFooterItemTransfer hfItemTransfer = new HeaderFooterItemTransfer();
                                        HFitem.setTransferData(hfItemTransfer);
                                        viewHeaderFooterItemsTransfers
                                                .add(hfItemTransfer);

                                    }
                                }
                            }
                            // Persist the list of items.
                            DAOFactoryHolder
                                    .getDAOFactory()
                                    .getHeaderFooterDAO()
                                    .persistHeaderFooter(
                                            viewHeaderFooterItemsTransfers,
                                            this.getId(),
                                            this.getEnvironmentId());
                        }
                    }
                    
                    // save view dependencies
                    if (this.statusCode.getGeneralId() == Codes.INACTIVE || isImportOrMigrate){
                        viewLogicDependencies = null; // dependency not applicable
                    }
                    List<ViewLogicDependencyTransfer> logicDependenciesTransfers = new ArrayList<ViewLogicDependencyTransfer>();
                    if (viewLogicDependencies != null) {
                        for (ViewLogicDependency viewLogicDepend : viewLogicDependencies) {
                            ViewLogicDependencyTransfer trans = new ViewLogicDependencyTransfer();
                            viewLogicDepend.setTransferData(trans);
                            logicDependenciesTransfers.add(trans);
                        }
                    }
                    DAOFactoryHolder.getDAOFactory().getViewLogicDependencyDAO()
                        .persistViewLogicDependencies(logicDependenciesTransfers, getId(),getEnvironmentId());
                    
                    if (!existsAllViewsAssociation()) {
                        DAOFactoryHolder.getDAOFactory().getViewFolderDAO().
                            addAllViewsAssociation(getId(),getEnvironmentId());
                        vfAssociations = SAFRAssociationFactory.getViewToViewFolderAssociations(this,false);
                    }
                    success = true;
                    
                    SAFRApplication.getModelCount().incCount(this.getClass(), 1);       
                    SAFRApplication.getModelCount().incCount(ViewSource.class, viewSources.size());       
                    SAFRApplication.getModelCount().incCount(ViewColumn.class, viewColumns.size());       
                    SAFRApplication.getModelCount().incCount(ViewColumnSource.class, viewColumnSources.size());       
                    SAFRApplication.getModelCount().incCount(ViewSortKey.class, viewSortKeys.size()); 
                    if (viewLogicDependencies != null) {
                        SAFRApplication.getModelCount().incCount(ViewLogicDependency.class, viewLogicDependencies.size());                         
                    }
                    if (header != null) {
                        SAFRApplication.getModelCount().incCount(HeaderFooterItem.class, header.getItems().size());                               
                    }
                    if (footer != null) {
                        SAFRApplication.getModelCount().incCount(HeaderFooterItem.class, footer.getItems().size());                               
                    }
                                        
                } catch (DAOUOWInterruptedException e) {
                    // UOW interrupted so retry it
                    continue;
                }

            } // end while(!success)

        } catch (SAFRNotFoundException snfe) {
            throw new SAFRException(
                    "The view with id "
                            + this.getId()
                            + " cannot be updated as its already been deleted from the database.",
                    snfe);
        } finally {

            if (success) {
                // End Transaction.
                DAOFactoryHolder.getDAOFactory().getDAOUOW().end();

            } else {
                // Rollback the transaction.
                DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
                // reset the object state
                for (SAFRPersistentObject obj : savedObjs) {
                    obj.undo();
                }
            }

        }
    }

    private boolean existsAllViewsAssociation() {
        SAFRAssociationList<ViewFolderViewAssociation> vfAssociations = 
            SAFRAssociationFactory.getViewToViewFolderAssociations(this, true);
        boolean exists = false;
        for (ViewFolderViewAssociation assoc : vfAssociations) {
            if (assoc.getAssociatedComponentIdNum().equals(0)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    /**
     * This enum maintains the properties of a view.
     * 
     */
    public enum Property {
        NAME, VIEW_FOLDER, CONTROL_RECORD, LINES_PER_PAGE, TITLE_FIELD, VIEW_SOURCE, WRITE_USER_EXIT_ROTUINE, FORMAT_USER_EXIT_ROTUINE, SORT_KEY, COLUMN_SOURCE,
        OUTPUT_FORMAT
    }

    /**
     * This method is used to validate a view object.If any validation condition
     * is not met then this method throws a list of all the error messages.
     * 
     * @throws SAFRValidationException
     *             : This method will set all the error messages along with the
     *             key, which is a property of the view, and throws
     *             SAFRValidationException when any validation condition is not
     *             met.
     * @throws SAFRException
     * @throws DAOException
     */
    public void validate() throws SAFRValidationException, DAOException,
            SAFRException, SAFRValidationException {
        SAFRValidationException safrValidationException = new SAFRValidationException();
        SAFRValidator safrValidator = new SAFRValidator();

        if (getName() == null || getName() == "") {
            safrValidationException.setErrorMessage(Property.NAME,
                    "View name cannot be empty");
        } else {
            if (getName().length() > ModelUtilities.MAX_NAME_LENGTH) {
                safrValidationException.setErrorMessage(Property.NAME,
                        "The length of view name '" + getName()
                                + "' cannot exceed 48 characters.");
            }
            else if (this.isDuplicate()) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The view name '"
                                        + getName()
                                        + "' already exists. Please specify a different name.");
            }
            if (!safrValidator.isNameValid(getName())) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The view name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),(isForImport() || isForMigration()))
                                        + "should begin "
                                        + "with a letter and should comprise of letters"
                                        + ", numbers, pound sign (#) and underscores only.");
            }
        }

        if (controlRecordId == null || controlRecordId == 0) {
            safrValidationException.setErrorMessage(Property.CONTROL_RECORD,
                    "Control Record cannot be empty.");
        }

        if (outputFormat != null) {
            if (outputFormat.equals(OutputFormat.Format_Report) && 
                linesPerPage < 54) {
                safrValidationException.setErrorMessage(
                        Property.LINES_PER_PAGE,
                        "Lines per page must be greater than or equal to 54.");
            }
        } else {
            safrValidationException
                    .setErrorMessage(
                            Property.OUTPUT_FORMAT,
                            "The Output Format is incomplete. " + SAFRUtilities.LINEBREAK + "Please select a valid Output Format before saving.");
        }

        // CQ8589 Kanchan Rauthan 15/09/2010. Validation on type of User Exit
        // Routine used in Extract and format phase.
        try {
            if (this.getWriteExit() != null
                    && this.getWriteExit().getTypeCode().getGeneralId() != Codes.WRITE) {
                safrValidationException
                        .setErrorMessage(
                                Property.WRITE_USER_EXIT_ROTUINE,
                                "The user exit routine '"
                                        + this.writeExit.getName()
                                        + "["
                                        + this.writeExit.getId()
                                        + "]' is not of type 'Write'. Please select a valid user exit routine.");
            }
        } catch (SAFRNotFoundException snfe) {
            // CQ 8596. Nikita. 11/10/2010
            // Don't allow user to save if the UXR is invalid
            safrValidationException
                    .setErrorMessage(
                            Property.WRITE_USER_EXIT_ROTUINE,
                            "The user exit routine with id ["
                                    + snfe.getComponentId()
                                    + "] does not exist. Please select a valid user exit routine.");
        }

        try {
            if (this.getFormatExit() != null
                    && this.getFormatExit().getTypeCode().getGeneralId() != Codes.FORMAT) {
                safrValidationException
                        .setErrorMessage(
                                Property.FORMAT_USER_EXIT_ROTUINE,
                                "The user exit routine '"
                                        + this.formatExit.getName()
                                        + "["
                                        + this.formatExit.getId()
                                        + "]' is not of type 'Format'. Please select a valid user exit routine.");
            }
        } catch (SAFRNotFoundException snfe) {
            // CQ 8596. Nikita. 11/10/2010
            // Don't allow user to save if the UXR is invalid
            safrValidationException
                    .setErrorMessage(
                            Property.FORMAT_USER_EXIT_ROTUINE,
                            "The user exit routine with id ["
                                    + snfe.getComponentId()
                                    + "] does not exist. Please select a valid user exit routine.");
        }

        // CQ 8983 Kanchan Rauthan 13/1/2011 Validate for column Source
        for (ViewColumnSource vwColSrc : this.getViewColumnSources()
                .getActiveItems()) {
            try {
                vwColSrc.validate();
            } catch (SAFRValidationException sve) {
                safrValidationException.setErrorMessages(
                        Property.COLUMN_SOURCE, sve.getErrorMessages());
            }
        }

        for (ViewSortKey viewSortKey : this.getViewSortKeys().getActiveItems()) {
            try {
                viewSortKey.validateTitleField();
            } catch (SAFRValidationException sve) {
                safrValidationException.setErrorMessages(Property.TITLE_FIELD,
                        sve.getErrorMessages());
            }

            try {
                viewSortKey.validate();
            } catch (SAFRValidationException sve1) {
                safrValidationException.setErrorMessages(Property.SORT_KEY,
                        sve1.getErrorMessages());
            }
        }

        if (!safrValidationException.getErrorMessages().isEmpty()) {
            throw safrValidationException;
        }

    }

    private boolean isDuplicate() throws DAOException {
        ViewTransfer viewTransfer = null;
        viewTransfer = DAOFactoryHolder.getDAOFactory().getViewDAO()
                .getDuplicateView(getName(), getId(), getEnvironmentId());

        if (viewTransfer == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method is used to make a view inactive.
     */
    public void makeViewInactive() {
        Code inactive = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.VIEWSTATUS).getCode(Codes.INACTIVE);
        if (!statusCode.getGeneralId().equals(inactive.getGeneralId())) {
            SAFRLogger.logAllSeparator(logger, Level.INFO, "De-Activating View " + getDescriptor());
            this.statusCode = inactive;            
            this.setActivated(false);
            SAFRLogger.logEnd(logger);
        }
    }

    @Override
    public SAFRComponent saveAs(String newName) throws SAFRValidationException,
            SAFRException {
        
        if (hasCreatePermission()) {
            
            View viewCopy = SAFRApplication.getSAFRFactory().createView();
            
            viewCopy.setConfirmWarningStrategy(this.getConfirmWarningStrategy());

            // copy the general properties...
            viewCopy.setName(newName);
            viewCopy.setStatusCode(SAFRApplication.getSAFRFactory()
                    .getCodeSet(CodeCategories.VIEWSTATUS)
                    .getCode(Codes.INACTIVE));

            viewCopy.setOutputFormat(this.getOutputFormat());
            viewCopy.setExtractAggregateBufferSize(this.getExtractAggregateBufferSize());
            viewCopy.setExtractAggregateBySortKey(this.isExtractAggregateBySortKey());
            viewCopy.setComment(this.getComment());
            viewCopy.setControlRecord(this.getControlRecord());
            viewCopy.setExtractFileAssociation(this.getExtractFileAssociation());
            viewCopy.setExtractMaxRecords(this.getExtractMaxRecords());
            viewCopy.setExtractPhaseOutputLimit(this.extractPhaseOutputLimit);
            viewCopy.setExtractWorkFileNo(this.getExtractWorkFileNo());
            viewCopy.setFileFieldDelimiterCode(this.getFileFieldDelimiterCode());
            viewCopy.setFileStringDelimiterCode(this
                    .getFileStringDelimiterCode());
            // Jaydeep 18th June 2010: CQ 8118 : added setFormatPhaseUsage which
            // was missing.
            viewCopy.setFormatExit(this.getFormatExit());
            viewCopy.setFormatExitParams(this.getFormatExitParams());
            viewCopy.setFormatPhaseOutputLimit(this.formatPhaseOutputLimit);
            viewCopy.setFormatPhaseRecordAggregationOn(this
                    .isFormatPhaseRecordAggregationOn());
            viewCopy.setFormatRecordFilter(this.getFormatRecordFilter());
            viewCopy.setLinesPerPage(this.getLinesPerPage());
            viewCopy.setOutputMaxRecCount(this.getOutputMaxRecCount());
            viewCopy.setReportWidth(this.getReportWidth());
            viewCopy.setSuppressZeroRecords(this.suppressZeroRecords);
            viewCopy.setHeaderRow(this.headerRow);
            viewCopy.setWriteExit(this.getWriteExit());
            viewCopy.setWriteExitParams(this.getWriteExitParams());
            if (this.outputLRId != null && this.outputLRId > 0) {
                viewCopy.setOutputLRId(this.outputLRId);
            }

            // copy View Sources...
            for (ViewSource viewSource : this.getViewSources().getActiveItems()) {
                ViewSource vwSource = viewCopy.addViewSource();
                vwSource.setSequenceNo(viewSource.getSequenceNo());
                vwSource.setLrFileAssociation(viewSource.getLrFileAssociation());
                vwSource.setExtractRecordFilter(viewSource.getExtractRecordFilter());
                vwSource.setExtractFileAssociation(viewSource.getExtractFileAssociation());
                vwSource.setWriteExit(viewSource.getWriteExit());
                vwSource.setWriteExitParams(viewSource.getWriteExitParams());
                vwSource.setExtractOutputOverride(viewSource.isExtractOutputOverriden());
                vwSource.setExtractRecordOutput(viewSource.getExtractRecordOutput());
            }
            // copy ViewColumn Contents
            for (ViewColumn viewColumn : this.getViewColumns().getActiveItems()) {
                ViewColumn vwColumn = viewCopy.addViewColumn(0);

                // copy View Column sources.
                List<ViewColumnSource> oldViewColumnSources = viewColumn
                        .getViewColumnSources().getActiveItems();

                for (int i = 0; i < vwColumn.getViewColumnSources().size(); i++) {
                    ViewColumnSource viewColumnSource = oldViewColumnSources
                            .get(i);
                    ViewColumnSource newViewColumnSource = vwColumn
                            .getViewColumnSources().get(i);
                    // CQ 8056. Nikita. 22/06/2010.
                    // Source type should be set first to avoid resetting of
                    // other fields.
                    newViewColumnSource.setSourceType(viewColumnSource
                            .getSourceType());
                    if (viewColumnSource.getSourceType().getGeneralId() == Codes.SOURCE_FILE_FIELD) {
                        newViewColumnSource.setLRFieldColumn(viewColumnSource
                                .getLRField());
                    } else if (viewColumnSource.getSourceType().getGeneralId() == Codes.LOOKUP_FIELD) {
                        newViewColumnSource.setLRFieldColumn(viewColumnSource
                                .getLRField());
                    }
                    newViewColumnSource.setSourceValue(viewColumnSource
                            .getSourceValue());
                    if(viewColumnSource.getEffectiveDateLRField() != null) {
                    	newViewColumnSource.setEffectiveDateLRField(viewColumnSource.getEffectiveDateLRField());
                    }
                    newViewColumnSource
                            .setEffectiveDateTypeCode(viewColumnSource
                                    .getEffectiveDateTypeCode());
                    newViewColumnSource.setEffectiveDateValue(viewColumnSource
                            .getEffectiveDateValue());
                    newViewColumnSource
                            .setExtractColumnAssignment(viewColumnSource
                                    .getExtractColumnAssignment());
                    newViewColumnSource
                            .setLogicalRecordQueryBean(viewColumnSource
                                    .getLogicalRecordQueryBean());
                    newViewColumnSource.setLookupQueryBean(viewColumnSource
                            .getLookupQueryBean());
                    newViewColumnSource
                            .setSortKeyTitleLogicalRecordQueryBean(viewColumnSource
                                    .getSortKeyTitleLogicalRecordQueryBean());
                    LookupQueryBean lqb =viewColumnSource.getSortKeyTitleLookupPathQueryBean();
                    if(lqb != null) {
                    	newViewColumnSource.setSortKeyTitleLookupPathQueryBean(lqb);
                    }
                    if(viewColumnSource.getSortKeyTitleLRField() != null) {
                        newViewColumnSource.setSortKeyTitleLRField(viewColumnSource.getSortKeyTitleLRField());
                    }

                }

                vwColumn.setColumnNo(viewColumn.getColumnNo());
                vwColumn.setDataAlignmentCode(viewColumn.getDataAlignmentCode());
                vwColumn.setDataTypeCode(viewColumn.getDataTypeCode());
                vwColumn.setDateTimeFormatCode(viewColumn
                        .getDateTimeFormatCode());
                vwColumn.setDecimals(viewColumn.getDecimals());
                vwColumn.setDefaultValue(viewColumn.getDefaultValue());
                vwColumn.setExtractAreaCode(viewColumn.getExtractAreaCode());
                vwColumn.setExtractAreaPosition(viewColumn
                        .getExtractAreaPosition());
                vwColumn.setFormatColumnCalculation(viewColumn
                        .getFormatColumnCalculation());
                vwColumn.setGroupAggregationCode(viewColumn
                        .getGroupAggregationCode());
                vwColumn.setHeaderAlignmentCode(viewColumn
                        .getHeaderAlignmentCode());
                vwColumn.setHeading1(viewColumn.getHeading1());
                vwColumn.setHeading2(viewColumn.getHeading2());
                vwColumn.setHeading3(viewColumn.getHeading3());
                vwColumn.setLength(viewColumn.getLength());
                vwColumn.setName(viewColumn.getName());
                vwColumn.setNumericMaskCode(viewColumn.getNumericMaskCode());
                vwColumn.setOrdinalPosition(viewColumn.getOrdinalPosition());
                vwColumn.setRecordAggregationCode(viewColumn
                        .getRecordAggregationCode());
                vwColumn.setScaling(viewColumn.getScaling());
                vwColumn.setSigned(viewColumn.isSigned());
                vwColumn.setSortkeyFooterLabel(viewColumn
                        .getSortkeyFooterLabel());
                vwColumn.setSortKeyLabel(viewColumn.getSortKeyLabel());
                vwColumn.setSpacesBeforeColumn(viewColumn
                        .getSpacesBeforeColumn());
                vwColumn.setStartPosition(viewColumn.getStartPosition());
                vwColumn.setSubtotalLabel(viewColumn.getSubtotalLabel());
                vwColumn.setVisible(viewColumn.isVisible());

                // if View column is a sort key column then copy sort key
                // properties.
                if (viewColumn.isSortKey()) {
                    ViewSortKey viewSortKey = viewColumn.getViewSortKey();
                    ViewSortKey vwSortKey = viewCopy.addSortKey(vwColumn);
                    vwSortKey.setDataTypeCode(viewSortKey.getDataTypeCode());
                    vwSortKey.setDateTimeFormatCode(viewSortKey
                            .getDateTimeFormatCode());
                    vwSortKey.setDecimalPlaces(viewSortKey.getDecimalPlaces());
                    vwSortKey.setDisplayModeCode(viewSortKey
                            .getDisplayModeCode());
                    vwSortKey
                            .setFooterOption(viewSortKey.getFooterOptionCode());
                    vwSortKey
                            .setHeaderOption(viewSortKey.getHeaderOptionCode());
                    vwSortKey.setKeySequenceNo(viewSortKey.getKeySequenceNo());
                    vwSortKey.setLength(viewSortKey.getLength());
                    vwSortKey.setSigned(viewSortKey.isSigned());
                    vwSortKey.setSortkeyLabel(viewSortKey.getSortkeyLabel());
                    vwSortKey.setSortSequenceCode(viewSortKey
                            .getSortSequenceCode());
                    vwSortKey.setStartPosition(viewSortKey.getStartPosition());
                    vwSortKey.setTitleField(viewSortKey.getTitleField());
                    vwSortKey.setTitleLength(viewSortKey.getTitleLength());
                    vwSortKey.setView(viewCopy);

                }
            }

            // copy view header footer
            if (viewCopy.getOutputFormat() != null && 
                viewCopy.getOutputFormat().equals(OutputFormat.Format_Report)) {
                if (this.header != null || this.footer != null) {
                    if (header != null) {
                        for (HeaderFooterItem HFitem : this.header.getItems()) {

                            HeaderFooterItem item = new HeaderFooterItem(
                                    viewCopy.getHeader(),
                                    HFitem.getFunctionCode(),
                                    HFitem.getJustifyCode(), HFitem.getRow(),
                                    HFitem.getColumn(), HFitem.getItemText(),
                                    HFitem.getEnvironment().getId());

                            viewCopy.getHeader().addItemInit(item);

                        }
                    }
                    if (footer != null) {
                        for (HeaderFooterItem HFitem : this.footer.getItems()) {
                            HeaderFooterItem item = new HeaderFooterItem(
                                    viewCopy.getFooter(),
                                    HFitem.getFunctionCode(),
                                    HFitem.getJustifyCode(), HFitem.getRow(),
                                    HFitem.getColumn(), HFitem.getItemText(),
                                    HFitem.getEnvironment().getId());
                            viewCopy.footer.addItemInit(item);

                        }
                    }
                }
            }

            viewCopy.validate();
            viewCopy.store();
            return viewCopy;
        } else {
            throw new SAFRException(
                    "The user is not authorized to create a View.");
        }

    }

    // Creates view columns based on the LRFields.
    private void createViewColsFromLRFields(LogicalRecord outputLR)
            throws SAFRException {
        // check whether the output lr is same as the previous output lr and
        // proceed if not.
        if (outputLR != null && outputLR.getId() != this.outputLRId) {

            // if there are columns in the view then remove all the columns.
            if (!viewColumns.getActiveItems().isEmpty()) {
                for (ViewColumn viewColumn : viewColumns.getActiveItems()) {
                    removeViewColumn(viewColumn);
                }
            }

            // get all the LRFields of the outputLR specified.
            List<LRField> lrFields = SAFRApplication.getSAFRFactory()
                    .getLRFields(outputLR);
            if (!lrFields.isEmpty()) {
                addFieldsAsColumns(lrFields, 0);
            }

        }

    }

    public int genFieldsAsColumns(List<LRField> lrFields, int startPos, ViewSource viewSrc) {
        int i = 0;
        for (; i < lrFields.size(); i++) {
            LRField lrField = lrFields.get(i);
            int colNum = startPos + i + 1;
            ViewColumn vc = addViewColumn(colNum);
            setColumnAsField(lrField, vc);
            setColumnSourceField(vc, lrField, viewSrc);
        }      
        return startPos+i;
    }
    
    public void genLPFieldAsColumn(LRField field, int position, ViewSource viewSrc,
        LogicalRecordQueryBean lrBean, LookupQueryBean lpBean) {
        ViewColumn vc = addViewColumn(position+1);
        setColumnAsField(field, vc);
        setColumnLookupField(vc, field, viewSrc, lrBean, lpBean);
    }
    
    public void addFieldsAsColumns(List<LRField> lrFields, int startPos) {
        // loop through all the lrFields of the output LR and create a
        // new view Column for each lrField and set the properties of
        // the newly created column as that of the LRField.
        for (int i = 0; i < lrFields.size(); i++) {
            LRField lrField = lrFields.get(i);
            int colNum = startPos + i + 1;
            ViewColumn vc = addViewColumn(colNum);
            setColumnAsField(lrField, vc);
        }
    }

    public int overAllFieldsAsColumns(List<LRField> lrFields, int startPos, ViewSource viewSrc) {
        int i = 0;
        for (; i < lrFields.size(); i++) {
            LRField lrField = lrFields.get(i);
            ViewColumn vc = viewColumns.get(startPos+i);
            setColumnAsField(lrField, vc);
            setColumnSourceField(vc, lrField, viewSrc);
        }      
        return startPos+i;
    }
    
    public void overAllLPFieldAsColumn(LRField field, int position, ViewSource viewSrc,
        LogicalRecordQueryBean lrBean, LookupQueryBean lpBean) {
        ViewColumn vc = viewColumns.get(position);
        setColumnAsField(field, vc);
        setColumnLookupField(vc, field, viewSrc, lrBean, lpBean);
    }
    
    public int overSourceFieldsAsColumns(List<LRField> lrFields, int startPos, ViewSource viewSrc, Object[] checkedElements) {
        int i = 0;
        for (; i < lrFields.size(); i++) {
            LRField lrField = lrFields.get(i);
            ViewColumn vc = (ViewColumn) checkedElements[startPos+i];
            setColumnSourceField(vc, lrField, viewSrc);
        }
        return startPos+i;
    }
    
    public void overSourceLPFieldAsColumn(LRField field, int position, ViewSource viewSrc,
        LogicalRecordQueryBean lrBean, LookupQueryBean lpBean, Object[] checkedElements) {
        ViewColumn vc = (ViewColumn) checkedElements[position];
        setColumnLookupField(vc, field, viewSrc, lrBean, lpBean);
    }

    public void overAllAsConstant(int position, ViewSource viewSrc) {
        ViewColumn col = viewColumns.get(position);
        
        ViewColumnSource colSrc = col.getViewColumnSources().get(viewSrc.getSequenceNo()-1);
        colSrc.setSourceType(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.COLSRCTYPE).getCode(Codes.CONSTANT));
        colSrc.setSourceValue(" ");
        
        resetColumn(col);  
        
        setPersistence(SAFRPersistence.MODIFIED);
    }
    
    public void overSourceAsConstant(int position, ViewSource viewSrc) {
        ViewColumn col = viewColumns.get(position);
        
        ViewColumnSource colSrc = col.getViewColumnSources().get(viewSrc.getSequenceNo()-1);
        colSrc.setSourceType(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.COLSRCTYPE).getCode(Codes.CONSTANT));
        colSrc.setSourceValue(" ");        
    }
    

    protected void resetColumn(ViewColumn col) {
        col.setHeading1("");
        col.setHeading2("");
        col.setHeading3("");
        col.setLength(1);
        if (getOutputFormat() == OutputFormat.Format_Report) {
            col.setSpacesBeforeColumn(2);
        } else {
            col.setSpacesBeforeColumn(0);
        }
        col.setVisible(true);
        Code code = SAFRApplication.getSAFRFactory().getCodeSet(
            CodeCategories.DATATYPE).getCode(Codes.ALPHANUMERIC);
        col.setDataTypeCode(code);
        col.setRecordAggregationCode(null);
        col.setGroupAggregationCode(null);
    }
    
    protected void setColumnAsField(LRField lrField, ViewColumn vc) {
        vc.setDateTimeFormatCode(lrField.getDateTimeFormatCode());
        vc.setDecimals(lrField.getDecimals());
        vc.setDefaultValue(lrField.getDefaultValue() == null ? null
                : lrField.getDefaultValue().trim());
        vc.setHeaderAlignmentCode(lrField.getHeaderAlignmentCode());
        if (lrField.getHeading1() != null && !(lrField.getHeading1().equals(""))) {
            vc.setHeading1(lrField.getHeading1());
        } else {
            vc.setHeading1(lrField.getName());
        }
        vc.setHeading2(lrField.getHeading2());
        vc.setHeading3(lrField.getHeading3());
        vc.setLength(lrField.getLength());
        vc.setNumericMaskCode(lrField.getNumericMaskCode());
        vc.setScaling(lrField.getScaling());
        vc.setSigned(lrField.isSigned());
        vc.setSortKeyLabel(lrField.getSortKeyLabel());
        vc.setSubtotalLabel(lrField.getSubtotalLabel());
        vc.setDataTypeCode(lrField.getDataTypeCode());
    }

    private void setColumnSourceField(ViewColumn col, LRField field, ViewSource viewSrc) {
        ViewColumnSource colSrc = col.getViewColumnSources().get(viewSrc.getSequenceNo()-1);
        colSrc.setSourceType(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.COLSRCTYPE).getCode(Codes.SOURCE_FILE_FIELD));
        colSrc.setLRFieldPaste(field);        
    }
    
    private void setColumnLookupField(ViewColumn col, LRField field, ViewSource viewSrc,
        LogicalRecordQueryBean lrBean, LookupQueryBean lpBean) {
        ViewColumnSource colSrc = col.getViewColumnSources().get(viewSrc.getSequenceNo()-1);
        colSrc.setSourceType(SAFRApplication.getSAFRFactory().
            getCodeSet(CodeCategories.COLSRCTYPE).getCode(Codes.LOOKUP_FIELD));
        colSrc.setLogicalRecordQueryBean(lrBean);
        colSrc.setLookupQueryBean(lpBean);
        colSrc.setLRFieldPaste(field);        
    }
    
    public void addConstant(int position) {
        // TODO Auto-generated method stub
        
    }
    
    public List<String> getLoadWarnings() {
        return loadWarnings;
    }

    public void setModelTransferProvider(ModelTransferProvider provider) {
        this.provider = provider;
    }
    
    /**
     * Returns true if the View has been batch activated,
     * otherwise false;
     * 
     * @return boolean batch activated flag
     */
    public boolean isBatchActivated() {
        return batchActivated;
    }

    /**
     * @return the migrateRelatedComponents
     */
    public boolean isMigrateRelatedComponents() {
        return migrateRelatedComponents;
    }

    /**
     * @param migrateRelatedComponents the migrateRelatedComponents to set
     */
    public void setMigrateRelatedComponents(boolean migrateRelatedComponents) {
        this.migrateRelatedComponents = migrateRelatedComponents;
    }
    
    public ViewColumn findColumn(Integer colId) {
        for (ViewColumn col : getViewColumns().getActiveItems()) {
            if (col.getId().equals(colId)) {
                return col;
            }
        }
        return null;
    }

    public ViewColumnSource findColumnSource(Integer colSrcId) {
        for (ViewColumnSource colSrc : getViewColumnSources()) {
            if (colSrc.getId().equals(colSrcId)) {
                return colSrc;
            }
        }
        return null;
    }

    public ViewSource findSource(Integer srcId) {
        for (ViewSource src : getViewSources()) {
            if (src.getId().equals(srcId)) {
                return src;
            }
        }
        return null;
    }

    public String getCompilerVersion() {
        return compilerVersion;
    }

    public void setCompilerVersion(String compilerVersion) {
        this.compilerVersion = compilerVersion;
    }

    public SAFRImmutableList<ViewFolderViewAssociation> getViewFolderAssociations() {
        return new SAFRImmutableList<ViewFolderViewAssociation>(vfAssociations);
    }
    
    public Code getTypeCode() {
        return typeCode;
    }

    public String getAggrLevel() {
        if (aggregateBySortKey == null) {
            return "Detail";
        } else {
            if (aggregateBySortKey) {
                return "Summary";                
            } else {
                return "Detail";                
            }
        }
    }

	public boolean hasFormatExit() {	
		return (formatExitId != null);
	}



}
