package com.ibm.safr.we.ui.editors.pf;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.swt.IFocusService;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.utilities.DepCheckOpener;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationContentProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationLabelProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationTableSorter;
import com.ibm.safr.we.utilities.SAFRLogger;

public class PhysicalFileGeneralEditor {

    static transient Logger logger = Logger
        .getLogger("com.ibm.safr.we.ui.editors.pf.PhysicalFileGeneralEditor");
    
    private static final int MAXUSEREXITPARAM = 32;
    
    private PhysicalFileMediator mediator;
    
    private Composite parent;        
    private Composite compositeGeneral;

    private Section sectionDetails;  
    private Composite compositeDetails;  
    private Text textGeneralID;
    private Text textGeneralName;                
    private Combo comboGeneralFileType;
    private Combo comboGeneralAccessMethod;
    private TableComboViewer comboGenUserExitViewer;
    private TableCombo comboGeneralUserExitRoutine;
    private Text textGeneralUserExitRoutineParam;
    private Text textGeneralComments;
    private Label labelGeneralCreatedValue;
    private Label labelGeneralModifiedValue;
    
    private Section sectionLogicalFiles;
    private Composite compositeLogicalFiles;
    private TableViewer lfTabViewer;
    private Table lfTable;
    
    private MenuItem ueOpenEditorItem = null;
    private MenuItem ueDepCheckItem = null;
        
    private String defaultCreated = "-";
    private String defaultModified = "-";

    private String selectedFileType = "";
    private String selectedAccessMethod = "";
    private String selectedUserExit = "";

    // model
    private PhysicalFile physicalFile;    
    private List<UserExitRoutineQueryBean> userExitRoutineList;

    public PhysicalFileGeneralEditor(PhysicalFileMediator mediator,
        Composite parent, PhysicalFile physicalFile) {
        super();
        this.mediator = mediator;
        this.parent = parent;
        this.physicalFile = physicalFile;
    }

    public Composite create() {
        compositeGeneral = mediator.getSAFRToolkit().createComposite(parent, SWT.NONE);            
        compositeGeneral.setLayout(new FormLayout());

        FormData dataComposite = new FormData();
        dataComposite.bottom = new FormAttachment(100, 0);
        compositeGeneral.setLayoutData(dataComposite);

        createSectionDetails();
        createSectionLogicalFiles();
        
        return compositeGeneral;
    }

    protected void createSectionDetails() {
        sectionDetails = mediator.getSAFRToolkit().createSection(compositeGeneral, Section.TITLE_BAR, "Details");
        sectionDetails.setLayout(new FormLayout());
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(0, 10);
        dataSectionDetails.bottom = new FormAttachment(100, 0);
        sectionDetails.setLayoutData(dataSectionDetails);        
        
        compositeDetails = mediator.getSAFRToolkit().createComposite(sectionDetails, SWT.NONE);
        compositeDetails.setLayout(new FormLayout());
        sectionDetails.setClient(compositeDetails);                
        
        Label idLabel = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, "ID:");
        FormData idLabelData = new FormData();
        idLabelData.top = new FormAttachment(0, 10);
        idLabelData.left = new FormAttachment(0, 20);
        idLabel.setLayoutData(idLabelData);
        
        textGeneralID = mediator.getSAFRToolkit().createTextBox(compositeDetails, SWT.NONE);
        textGeneralID.setEnabled(false);
        FormData idTextData = new FormData();
        idTextData.top = new FormAttachment(0, 10);
        idTextData.left = new FormAttachment(idLabel, 160);
        idTextData.width = 300;
        textGeneralID.setLayoutData(idTextData);
        
        Label labelGeneralName = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, "&Name:");
        FormData nameLabelData = new FormData();
        nameLabelData.top = new FormAttachment(idLabel, 15);
        nameLabelData.left = new FormAttachment(0, 20);
        labelGeneralName.setLayoutData(nameLabelData);        

        textGeneralName = mediator.getSAFRToolkit().createNameTextBox(compositeDetails, SWT.NONE);
        textGeneralName.setData(SAFRLogger.USER, "Name");
        FormData nameTextData = new FormData();
        nameTextData.top = new FormAttachment(idLabel, 15);
        nameTextData.left = new FormAttachment(idLabel, 160);
        nameTextData.width = 300;
        textGeneralName.setLayoutData(nameTextData);
        textGeneralName.setTextLimit(UIUtilities.MAXNAMECHAR);
        textGeneralName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(physicalFile.getName(), textGeneralName.getText())) {
                    mediator.setDirty(true);
                    physicalFile.setName(textGeneralName.getText());
                }
            }            
        });
        
        Label labelGeneralFileType = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, "File T&ype:");
        FormData fileTypeLabelData = new FormData();
        fileTypeLabelData.top = new FormAttachment(labelGeneralName, 15);
        fileTypeLabelData.left = new FormAttachment(0, 20);
        labelGeneralFileType.setLayoutData(fileTypeLabelData);
        
        comboGeneralFileType = mediator.getSAFRToolkit().createComboBox(compositeDetails,SWT.READ_ONLY, "");
        FormData fileTypeComboData = new FormData();
        fileTypeComboData.top = new FormAttachment(labelGeneralName, 15);
        fileTypeComboData.left = new FormAttachment(idLabel, 160);   
        fileTypeComboData.width = 285;
        comboGeneralFileType.setLayoutData(fileTypeComboData);
        comboGeneralFileType.setData(SAFRLogger.USER, "File Type");               
        UIUtilities.populateComboBox(comboGeneralFileType, CodeCategories.FILETYPE, -1, false);
        
        comboGeneralFileType.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!(comboGeneralFileType.getText().equals(selectedFileType))) {
                    mediator.setDirty(true);
                    selectedFileType = comboGeneralFileType.getText();
                    Code codeFileType = UIUtilities.getCodeFromCombo(comboGeneralFileType);
                    if (codeFileType != null) {
                        physicalFile.setFileTypeCode(codeFileType);
                        List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCodes();
                        if (codeFileType.getGeneralId().equals(Codes.FILE_DATAB)) {
                            physicalFile.setAccessMethod(codeList.get(0));
                        }
                        else {
                            physicalFile.setAccessMethod(codeList.get(2));                            
                        }
                        if (!codeFileType.getGeneralId().equals(Codes.FILE_REXIT)) {
                            physicalFile.setUserExitRoutine(null);
                            physicalFile.setUserExitRoutineParams("");
                        }
                        doRefreshControls();
                    }
                }
            }
        });
        
        Label labelGeneralAccessMethod = mediator.getSAFRToolkit().createLabel(
            compositeDetails, SWT.NONE, "Access &Method:");
        FormData labelAccessData = new FormData();
        labelAccessData.top = new FormAttachment(labelGeneralFileType, 15);
        labelAccessData.left = new FormAttachment(0, 20);   
        labelGeneralAccessMethod.setLayoutData(labelAccessData);
        
        comboGeneralAccessMethod = mediator.getSAFRToolkit().createComboBox(compositeDetails,SWT.READ_ONLY, "");
        FormData comboAccessData = new FormData();
        comboAccessData.top = new FormAttachment(labelGeneralFileType, 15);
        comboAccessData.left = new FormAttachment(idLabel, 160);   
        comboGeneralAccessMethod.setLayoutData(comboAccessData);
        comboGeneralAccessMethod.setData(SAFRLogger.USER, "Access Method");   
        comboAccessData.width=285;
        comboGeneralAccessMethod.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!(comboGeneralAccessMethod.getText().equals(selectedAccessMethod))) {
                    mediator.setDirty(true);
                    selectedAccessMethod = comboGeneralAccessMethod.getText();
                    Code codeAccess = UIUtilities.getCodeFromCombo(comboGeneralAccessMethod);
                    if (codeAccess != null) {
                        physicalFile.setAccessMethod(codeAccess);
                        doRefreshControls();
                    }
                }                
            }               
            
        });
        
        Label userExitLabel = mediator.getSAFRToolkit().createLabel(compositeDetails, 
            SWT.NONE, "Read User-E&xit Routine:");        
        FormData userExitLabelData = new FormData();
        userExitLabelData.top = new FormAttachment(labelGeneralAccessMethod, 15);
        userExitLabelData.left = new FormAttachment(0, 20); 
        userExitLabel.setLayoutData(userExitLabelData);
        
        comboGenUserExitViewer = mediator.getSAFRToolkit().createTableComboForExits(
            compositeDetails, ComponentType.UserExitRoutine);       
        comboGeneralUserExitRoutine = comboGenUserExitViewer.getTableCombo();
        FormData userExitComboData = new FormData();
        userExitComboData.top = new FormAttachment(labelGeneralAccessMethod, 15);
        userExitComboData.left = new FormAttachment(idLabel, 160);   
        userExitComboData.width = 310;
        comboGeneralUserExitRoutine.setLayoutData(userExitComboData);        
        comboGeneralUserExitRoutine.setData(SAFRLogger.USER, "User-Exit Routine");                        
            
        comboGeneralUserExitRoutine.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!(comboGeneralUserExitRoutine.getText().equals(selectedUserExit))) {
                    mediator.setDirty(true);
                    selectedUserExit = comboGeneralUserExitRoutine.getText();
                    try {
                        if (comboGeneralUserExitRoutine.getSelectionIndex() >= 0) {
                            UserExitRoutineQueryBean userExitRoutineBean = (UserExitRoutineQueryBean) comboGeneralUserExitRoutine
                                    .getTable().getSelection()[0].getData();
                            if (userExitRoutineBean != null && userExitRoutineBean.getId() > 0) {
                                UserExitRoutine userExitRoutine = SAFRApplication.getSAFRFactory()
                                    .getUserExitRoutine(userExitRoutineBean.getId());
                                physicalFile.setUserExitRoutine(userExitRoutine);
                            }
                        }
                        else {
                            physicalFile.setUserExitRoutine(null);
                        }
                    } catch (SAFRNotFoundException sne) {
                        UIUtilities.handleWEExceptions(sne, null, UIUtilities.titleStringNotFoundException);
                    } catch (SAFRException ex) {
                        UIUtilities.handleWEExceptions(ex, "Error in getting User Exit Routine", null);
                    }
                    
                }
            }
        });
        
        try {
            userExitRoutineList = SAFRQuery.queryUserExitRoutines(
                UIUtilities.getCurrentEnvironmentID(), "READ", SortType.SORT_BY_NAME);
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while retrieving User Exit Routines.",
                UIUtilities.titleStringDbException);
        }
        addUEOpenEditorMenu();

        
        Label userParamLabel = mediator.getSAFRToolkit().createLabel(
            compositeDetails, SWT.NONE, "&Parameters:");
        FormData userParamLabelData = new FormData();
        userParamLabelData.top = new FormAttachment(userExitLabel, 15);
        userParamLabelData.left = new FormAttachment(0, 50);   
        userParamLabel.setLayoutData(userParamLabelData);
        
        textGeneralUserExitRoutineParam = mediator.getSAFRToolkit().createTextBox(
            compositeDetails, SWT.NONE);
        FormData userParamTextData = new FormData();
        userParamTextData.top = new FormAttachment(userExitLabel, 15);
        userParamTextData.left = new FormAttachment(idLabel, 160);   
        userParamTextData.width = 300;
        textGeneralUserExitRoutineParam.setLayoutData(userParamTextData);                
        textGeneralUserExitRoutineParam.setData(SAFRLogger.USER, "User-Exit Param");                              
        textGeneralUserExitRoutineParam.setTextLimit(MAXUSEREXITPARAM);
        textGeneralUserExitRoutineParam.setEnabled(false);
        textGeneralUserExitRoutineParam.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(physicalFile.getUserExitRoutineParams(), 
                    textGeneralUserExitRoutineParam.getText())) {
                    mediator.setDirty(true);
                    physicalFile.setUserExitRoutineParams(textGeneralUserExitRoutineParam.getText());
                }
            }            
        });

        Label commentLabel = mediator.getSAFRToolkit().createLabel(compositeDetails, SWT.NONE, "C&omments:");
        FormData commentLabelData = new FormData();
        commentLabelData.top = new FormAttachment(userParamLabel, 15);
        commentLabelData.left = new FormAttachment(0, 20); 
        commentLabel.setLayoutData(commentLabelData);
        
        textGeneralComments = mediator.getSAFRToolkit().createCommentsTextBox(compositeDetails);
        textGeneralComments.setData(SAFRLogger.USER, "Comments");
        FormData commentTextData = new FormData();
        commentTextData.top = new FormAttachment(userParamLabel, 15);
        commentTextData.left = new FormAttachment(idLabel, 160); 
        commentTextData.width = 280;
        commentTextData.height = 60;
        textGeneralComments.setLayoutData(commentTextData);        
        textGeneralComments.setTextLimit(UIUtilities.MAXCOMMENTCHAR);
        textGeneralComments.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!UIUtilities.isEqualString(physicalFile.getComment(), textGeneralComments.getText())) {
                    mediator.setDirty(true);
                    physicalFile.setComment(textGeneralComments.getText());
                }
            }            
        });

        Label createdLabel = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, "Created:");
        FormData createdLabelData = new FormData();
        createdLabelData.top = new FormAttachment(textGeneralComments, 15);
        createdLabelData.left = new FormAttachment(0, 20); 
        createdLabel.setLayoutData(createdLabelData);
        
        labelGeneralCreatedValue = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, defaultCreated);
        FormData createdLabelValueData = new FormData();
        createdLabelValueData.top = new FormAttachment(textGeneralComments, 15);
        createdLabelValueData.left = new FormAttachment(idLabel, 160); 
        labelGeneralCreatedValue.setLayoutData(createdLabelValueData);

        Label modifiedLabel = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, "Last Modified:");
        FormData modifiedLabelData = new FormData();
        modifiedLabelData.top = new FormAttachment(createdLabel, 15);
        modifiedLabelData.left = new FormAttachment(0, 20); 
        modifiedLabel.setLayoutData(modifiedLabelData);
        
        labelGeneralModifiedValue = mediator.getSAFRToolkit().createLabel(compositeDetails,SWT.NONE, defaultModified);
        FormData modifiedLabelValueData = new FormData();
        modifiedLabelValueData.top = new FormAttachment(createdLabel, 15);
        modifiedLabelValueData.left = new FormAttachment(idLabel, 160); 
        labelGeneralModifiedValue.setLayoutData(modifiedLabelValueData);
    }

    protected void enableDisableBasedOnDetails() {
        Code fileType = UIUtilities.getCodeFromCombo(comboGeneralFileType);
        boolean readOnly = (mediator.getPhysicalFileInput().getEditRights() == EditRights.Read);
        if (fileType.getGeneralId().equals(Codes.FILE_DISK)) {
            if (!readOnly) {
                comboGeneralAccessMethod.setEnabled(true);
                comboGeneralAccessMethod.setData(Codes.SEQUENTIAL_STANDARD);
            }
            populateAccessMethodComboFile();
            comboGenUserExitViewer.setInput(null);            
            comboGeneralUserExitRoutine.setEnabled(false);
            textGeneralUserExitRoutineParam.setEnabled(false);
            textGeneralComments.setEnabled(true);
            
            
            mediator.enableDatasetTab();
            mediator.enableDatasetName();
            mediator.doRefreshDatasetTab();
            mediator.disableRecordSettings();
        }
        else if (fileType.getGeneralId().equals(Codes.FILE_DATAB)) {
            if (!readOnly) {
                comboGeneralAccessMethod.setEnabled(true);
            }
            mediator.enableDatabaseTab();  
            populateAccessMethodComboDatabase();          

            textGeneralComments.setEnabled(true);
            mediator.doRefreshDatabaseTab();
            
            Code accessMethod = physicalFile.getAccessMethodCode();
            if (!readOnly) {
                if (accessMethod.getGeneralId().equals(Codes.DB2VIASQL)) {
                    mediator.enableSQL();                
                }
            }
        }
        else if (fileType.getGeneralId().equals(Codes.FILE_REXIT)) {

            populateAccessMethodComboNA();
            comboGeneralAccessMethod.setEnabled(false);
            if (!readOnly) {
                comboGeneralUserExitRoutine.setEnabled(true);
                comboGenUserExitViewer.setInput(userExitRoutineList);
                textGeneralUserExitRoutineParam.setEnabled(true);
                textGeneralComments.setEnabled(true);
            }
            mediator.enableDatasetTab();                    
            mediator.doRefreshDatasetTab();
            mediator.disableDatasetName();
            mediator.disableRecordSettings();
        }
        else if (fileType.getGeneralId().equals(Codes.FILE_PEXIT)) {
            populateAccessMethodComboNA();
            comboGeneralAccessMethod.setEnabled(false);
            if (!readOnly) {
                comboGeneralUserExitRoutine.setEnabled(true);
                comboGenUserExitViewer.setInput(userExitRoutineList);
                textGeneralUserExitRoutineParam.setEnabled(true);
            }
            mediator.enableDatasetTab();
            mediator.doRefreshDatasetTab();
            mediator.disableDatasetName();
            mediator.disableRecordSettings();

        } else if (fileType.getGeneralId().equals(Codes.FILE_PIPE)) {
            populateAccessMethodComboNA();
            comboGeneralAccessMethod.setEnabled(false);
            comboGeneralAccessMethod.setData(Codes.SEQUENTIAL_STANDARD);
            comboGeneralUserExitRoutine.setEnabled(false);
            textGeneralComments.setEnabled(true);
            comboGenUserExitViewer.setInput(null);            
            textGeneralUserExitRoutineParam.setEnabled(false);
            mediator.enableDatasetTab();
            if (!readOnly) {
                mediator.enableRecordSettings();
            }
            mediator.doRefreshDatasetTab();
            mediator.disableDatasetName();
        } else {
            populateAccessMethodComboNA();
            comboGeneralAccessMethod.setData(Codes.SEQUENTIAL_STANDARD);
            comboGeneralAccessMethod.setEnabled(false);
            comboGeneralUserExitRoutine.setEnabled(false);
            comboGenUserExitViewer.setInput(null);            
            textGeneralUserExitRoutineParam.setEnabled(false);
            textGeneralComments.setEnabled(true);
            mediator.enableDatasetTab();                    
            mediator.doRefreshDatasetTab();
            mediator.disableRecordSettings();
            mediator.disableDatasetName();
        }
    }

    private void populateAccessMethodComboNA() {
        
        comboGeneralAccessMethod.removeAll();
        List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCodes();
        for (Code code : codeList) {
            if (code.getGeneralId().equals(Codes.SEQUENTIAL_STANDARD)) {
                comboGeneralAccessMethod.add(code.getDescription());
                comboGeneralAccessMethod.setData(Integer.toString(0), code);
            }
        }
        comboGeneralAccessMethod.select(0);
    }

    
    private void populateAccessMethodComboFile() {
        
        comboGeneralAccessMethod.removeAll();
        List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCodes();

        int selectIdx = 0;
        int idx = 0;
        for (Code code : codeList) {
            if (code.getGeneralId().equals(Codes.SEQUENTIAL_STANDARD) ||
                code.getGeneralId().equals(Codes.VSAM_ORDERED)) {
                comboGeneralAccessMethod.add(code.getDescription());
                comboGeneralAccessMethod.setData(Integer.toString(idx), code);
                if (code.equals(physicalFile.getAccessMethodCode())) {
                    selectIdx = idx;
                }
                idx++;
            }
        }
        // select based on current access method
        comboGeneralAccessMethod.select(selectIdx);
    }

    private void populateAccessMethodComboDatabase() {
        
        comboGeneralAccessMethod.removeAll();
        List<Code> codeList = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCodes();
        
        int selectIdx = 0;
        int idx = 0;
        for (Code code : codeList) {
            if (code.getGeneralId().equals(Codes.DB2VIASQL)) {
                comboGeneralAccessMethod.add(code.getDescription());
                comboGeneralAccessMethod.setData(Integer.toString(idx), code);
                if (code.equals(physicalFile.getAccessMethodCode())) {
                    selectIdx = idx;
                }
                idx++;
            }
        }
        comboGeneralAccessMethod.select(selectIdx);
    }
    
    
    private void createSectionLogicalFiles() {
        sectionLogicalFiles = mediator.getSAFRToolkit().createSection(compositeGeneral, Section.TITLE_BAR, "Associated Logical Files");
        sectionLogicalFiles.setLayout(new FormLayout());
        FormData dataSectionDetails = new FormData();
        dataSectionDetails.top = new FormAttachment(0, 10);
        dataSectionDetails.left =new FormAttachment(sectionDetails, 10);
        dataSectionDetails.bottom = new FormAttachment(70, 0);
        sectionLogicalFiles.setLayoutData(dataSectionDetails);        
        
        compositeLogicalFiles = mediator.getSAFRToolkit().createComposite(sectionLogicalFiles, SWT.NONE);
        compositeLogicalFiles.setLayout(new FormLayout());
        sectionLogicalFiles.setClient(compositeLogicalFiles);
        
        lfTabViewer = mediator.getSAFRToolkit().createTableViewer(compositeLogicalFiles, 
            SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION, false);
        lfTable = lfTabViewer.getTable();
        FormData dataTableLogicalFile = new FormData();
        dataTableLogicalFile.left = new FormAttachment(0, 0);
        dataTableLogicalFile.top = new FormAttachment(0, 10);
        dataTableLogicalFile.bottom = new FormAttachment(100, 0);
        dataTableLogicalFile.height = 100;
        lfTable.setLayoutData(dataTableLogicalFile);
        mediator.getFormToolkit().adapt(lfTable, false, false);

        UIUtilities.prepareTableViewerForShortList(lfTabViewer,ComponentType.LogicalFile);
        lfTabViewer.setContentProvider(new ComponentAssociationContentProvider());
        lfTabViewer.setLabelProvider(new ComponentAssociationLabelProvider());
        lfTabViewer.setInput(physicalFile.getLogicalFileAssociations());
        
        SortOrderPrefs prefs = new SortOrderPrefs(UIUtilities.SORT_CATEGORY, ComponentType.LogicalFile.name());
        if (prefs.load()) {
            lfTabViewer.getTable().setSortColumn(
                lfTabViewer.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                lfTabViewer.getTable().setSortDirection(SWT.UP);
                lfTabViewer.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.UP));
            }
            else {
                lfTabViewer.getTable().setSortDirection(SWT.DOWN);
                lfTabViewer.setSorter(new ComponentAssociationTableSorter(prefs.getColumn(), SWT.DOWN));
            }                   
        }
        else {
            lfTabViewer.getTable().setSortColumn(
                lfTabViewer.getTable().getColumn(1));
            lfTabViewer.getTable().setSortDirection(SWT.UP);
            lfTabViewer.setSorter(new ComponentAssociationTableSorter(1, SWT.UP));
        }       
        
        // Code for tracking the focus on the table
        IFocusService service = (IFocusService) mediator.getSite().getService(IFocusService.class);
        service.addFocusTracker(lfTable, "LFSearchableTable");        
        lfTabViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ComponentAssociation node = (ComponentAssociation)((StructuredSelection) event.getSelection()).getFirstElement();
                if (node == null) {                    
                    setPopupEnabled(false);
                }
                else {
                    setPopupEnabled(true);
                }
            }
            
        });
        
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(lfTable);
        // Set the MenuManager
        lfTable.setMenu(menu);
        mediator.getSite().registerContextMenu(menuManager, lfTabViewer);        
        setPopupEnabled(false);             
    }

    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.PHYSICALFILE);
        service.setPhysicalFile(enabled);
    }
    
    private void addUEOpenEditorMenu()
    {
        Text text = comboGeneralUserExitRoutine.getTextControl();
        Menu menu = text.getMenu();
        ueOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        ueOpenEditorItem.setText("Open Editor");
        ueOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboGenUserExitViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.UserExitRoutine);                        
                }                
            }
        });

        ueDepCheckItem = new MenuItem(menu, SWT.PUSH);
        ueDepCheckItem.setText("Dependency Checker");
        ueDepCheckItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboGenUserExitViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    DepCheckOpener.open(bean);                        
                }                
            }
        });
        
        comboGeneralUserExitRoutine.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    UserExitRoutineQueryBean bean = (UserExitRoutineQueryBean)((StructuredSelection) comboGenUserExitViewer
                            .getSelection()).getFirstElement();
                    if (bean != null && bean.getId() != 0) {   
                        ueOpenEditorItem.setEnabled(true);
                        ueDepCheckItem.setEnabled(true);                        
                    }
                    else {
                        ueOpenEditorItem.setEnabled(false);
                        ueDepCheckItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }

    public ComponentAssociation getCurrentLF() {
        return (ComponentAssociation) ((StructuredSelection)lfTabViewer.getSelection()).getFirstElement();
    }       
    
    public void doRefreshControls() throws SAFRException {
        
        UIUtilities.checkNullText(textGeneralID, Integer.toString(physicalFile.getId()));        
        UIUtilities.checkNullText(textGeneralName, physicalFile.getName());
        UIUtilities.checkNullCombo(comboGeneralFileType, physicalFile.getFileTypeCode());
        selectedFileType = comboGeneralFileType.getText();
        if (physicalFile.getAccessMethodCode() != null) {
            comboGeneralAccessMethod.setText(physicalFile.getAccessMethodCode().getDescription());
        } else {
            comboGeneralAccessMethod.setText("");
        }
        selectedAccessMethod = comboGeneralAccessMethod.getText();
        UIUtilities.checkNullText(textGeneralUserExitRoutineParam, physicalFile.getUserExitRoutineParams());
        UIUtilities.checkNullText(textGeneralComments, physicalFile.getComment());
        
        if (physicalFile.getCreateBy() != null) {
            labelGeneralCreatedValue.setText(physicalFile.getCreateBy()+ " on " + physicalFile.getCreateTimeString());            
        }

        if (physicalFile.getModifyBy() != null) {
            labelGeneralModifiedValue.setText(physicalFile.getModifyBy()+ " on " + physicalFile.getModifyTimeString());
        }

        enableDisableBasedOnDetails();  
        
        selectUserExit();                    
    }

    protected void selectUserExit() {
        Code fileType = UIUtilities.getCodeFromCombo(comboGeneralFileType);
        if (fileType.getGeneralId().equals(Codes.FILE_REXIT) ||
            fileType.getGeneralId().equals(Codes.FILE_PEXIT)) {
        	comboGeneralUserExitRoutine.setEnabled(true);
            try {
                //We want a non standard value now
                UserExitRoutine uer = physicalFile.getUserExitRoutine();
    			comboGeneralUserExitRoutine.setText(uer == null ? "" : uer.getComboString());
            } catch (SAFRNotFoundException snfe) {
                logger.log(Level.SEVERE, "Cannot select user exit", snfe);
                MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                    "Warning","The User Exit Routine with id ["+ snfe.getComponentId()+ 
                    "] referred to by this Physical File does not exist. Please select another User Exit Routine if required.");
                comboGeneralUserExitRoutine.setText("[" + snfe.getComponentId()+ "]");
            }
            selectedUserExit = comboGeneralUserExitRoutine.getText();
        } else {
        	comboGeneralUserExitRoutine.setEnabled(false);
        	comboGeneralUserExitRoutine.setText("");
        }
    }
    
    public void setNameFocus() {
        textGeneralName.setFocus();
    }

    public Control getControlFromProperty(Object property) {
        if (property == PhysicalFile.Property.NAME) {
            return textGeneralName;
        } else if (property == PhysicalFile.Property.ACCESS_METHOD) {
            return comboGeneralAccessMethod;
        } else if (property == PhysicalFile.Property.FILE_TYPE) {
            return comboGeneralFileType;
        } else if (property == PhysicalFile.Property.COMMENT) {
            return textGeneralComments;
        } else if (property == PhysicalFile.Property.USER_EXIT_ROUTINE) {
            return comboGeneralUserExitRoutine;
        }
        return null;
    }

    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        if (lfTable.isFocusControl()) {
            // if search criteria is id, then sort the list of components
            // according to id.
            if (searchCriteria == MetadataSearchCriteria.ID) {
                lfTabViewer.getTable().setSortColumn(
                    lfTabViewer.getTable().getColumn(0));
                lfTabViewer.getTable().setSortDirection(SWT.UP);
                lfTabViewer.setSorter(new ComponentAssociationTableSorter(0,SWT.UP));
            } else {
                // if search criteria is name, then sort the list of components
                // according to name.
                lfTabViewer.getTable().setSortColumn(
                    lfTabViewer.getTable().getColumn(1));
                lfTabViewer.getTable().setSortDirection(SWT.UP);
                lfTabViewer.setSorter(new ComponentAssociationTableSorter(1,
                        SWT.UP));
            }

            // get the items from the table.
            for (TableItem item : lfTabViewer.getTable().getItems()) {
                ComponentAssociation bean = (ComponentAssociation) item.getData();
                if (searchCriteria == MetadataSearchCriteria.ID) {
                    if (bean.getAssociatedComponentIdString().startsWith(searchText)) {
                        lfTabViewer.setSelection(new StructuredSelection(bean),true);
                        return;
                    }
                } else if (searchCriteria == MetadataSearchCriteria.NAME) {
                    if (bean.getAssociatedComponentName() != null && 
                        bean.getAssociatedComponentName().toLowerCase().startsWith(searchText.toLowerCase())) {
                        lfTabViewer.setSelection(new StructuredSelection(bean),true);
                        return;
                    }
                }
            }

            // if no component is found, then show the dialog box.
            MessageDialog.openInformation(mediator.getSite().getShell(),
                            "Component not found",
                            "The component you are trying to search is not found in the list.");

        }
    }

    public ComponentType getComponentType() {
      if (lfTable.isFocusControl()) {
        return ComponentType.LogicalFile;
      }
      else {
          return null;
      }
    }

    public String getComponentNameForSaveAs() {
        return textGeneralName.getText();
    }

}
