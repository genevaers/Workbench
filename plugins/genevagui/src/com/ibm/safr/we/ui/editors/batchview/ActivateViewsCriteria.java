package com.ibm.safr.we.ui.editors.batchview;

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


import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ActivateViewsCriteria {

    static final Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.batchview.ActivateViewsCriteria");
    
    private ActivateViewsMediator mediator;

    // section errors
    private Composite parent;
    private Section sectionCriteria;
    private Composite compositeCriteria;
    
    // environment
    private TableCombo comboEnvironment;
    private String selectedEnvironmentIndex = "";
    private TableComboViewer comboEnvironmentViewer;
    private Environment currentEnvironment = null;
    private List<EnvironmentQueryBean> envList;
    private MenuItem envOpenEditorItem = null;
    
    // folder
    private TableCombo comboFolder;
    private String selectedFolderIndex = "";
    private TableComboViewer comboFolderViewer;
    private ViewFolder currentViewFolder = null;
    private List<ViewFolderQueryBean> vfList;
    private MenuItem folOpenEditorItem = null;
    
    
    public ActivateViewsCriteria(ActivateViewsMediator mediator, Composite parent) {
        this.mediator = mediator;
        this.parent = parent;
    }
    
        
    protected void create() {
        sectionCriteria = mediator.getGUIToolKit().createSection(parent,
                Section.TITLE_BAR, "Environments");

        FormData dataSectionEnv = new FormData();
        dataSectionEnv.top = new FormAttachment(0, 10);
        dataSectionEnv.left = new FormAttachment(0, 5);
        dataSectionEnv.right = new FormAttachment(100, -5);
        sectionCriteria.setLayoutData(dataSectionEnv);

        compositeCriteria = mediator.getGUIToolKit().createComposite(
                sectionCriteria, SWT.NONE);
        compositeCriteria.setLayout(new FormLayout());

        Label labelEnvironments = mediator.getGUIToolKit().createLabel(compositeCriteria,
                SWT.NONE, "&Environment: ");

        FormData dataLabelEnvironments = new FormData();
        dataLabelEnvironments.top = new FormAttachment(0, 10);
        dataLabelEnvironments.left = new FormAttachment(0, 5);
        labelEnvironments.setLayoutData(dataLabelEnvironments);
    
        comboEnvironmentViewer = mediator.getGUIToolKit().createTableComboForComponents(
                compositeCriteria, ComponentType.Environment);
        comboEnvironment = comboEnvironmentViewer.getTableCombo();
        comboEnvironment.setData(SAFRLogger.USER, "Environment");

        FormData dataComboEnvironments = new FormData();
        dataComboEnvironments.top = new FormAttachment(0, 10);
        dataComboEnvironments.left = new FormAttachment(labelEnvironments, 10);
        dataComboEnvironments.width = 422;
        comboEnvironment.setLayoutData(dataComboEnvironments);

        addEnvOpenEditorMenu();
        
        comboEnvironment.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                try {
                    if (selectedEnvironmentIndex.equals(comboEnvironment.getText())) {
                        return;
                    }
                    ApplicationMediator.getAppMediator().waitCursor();
                    
                    EnvironmentQueryBean environmetQueryBean = (EnvironmentQueryBean) comboEnvironment
                        .getTable().getSelection()[0].getData();
                    mediator.clearErrors();

                    try {
                        currentEnvironment = SAFRApplication.getSAFRFactory()
                            .getEnvironment(environmetQueryBean.getId());

                    } catch (SAFRException e1) {
                        UIUtilities.handleWEExceptions(e1,"Error occurred while retrieving the enviornment.",null);
                    }

                    refreshFolders();
                    selectedFolderIndex = "";
                    currentViewFolder = null;
                    mediator.loadData();
                    mediator.refreshViews();

                } finally {
                    ApplicationMediator.getAppMediator().normalCursor();
                }
                selectedEnvironmentIndex = comboEnvironment.getText();
            }

        });

        // load the data
        try {
            envList = SAFRQuery.queryEnvironmentsForBAV(SortType.SORT_BY_NAME);

        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                    "Error occurred while retrieving all environments.",
                    UIUtilities.titleStringDbException);
        }
        Integer count = 0;
        if (envList != null) {
            for (EnvironmentQueryBean enviromentQuerybean : envList) {
                comboEnvironment.setData(Integer.toString(count),enviromentQuerybean);
                count++;
            }
        }
        comboEnvironmentViewer.setInput(envList);
        
        Label labelFolders = mediator.getGUIToolKit().createLabel(compositeCriteria,
            SWT.NONE, "View &Folder: ");
        FormData dataLabelFolders = new FormData();
        dataLabelFolders.top = new FormAttachment(labelEnvironments, 10);
        dataLabelFolders.left = new FormAttachment(0, 5);
        labelFolders.setLayoutData(dataLabelFolders);
        
        comboFolderViewer = mediator.getGUIToolKit().createTableComboForComponents(
            compositeCriteria, ComponentType.ViewFolder);
        comboFolder = comboFolderViewer.getTableCombo();
        comboFolder.setData(SAFRLogger.USER, "View Folder");        
        FormData dataComboFolders = new FormData();
        dataComboFolders.top = new FormAttachment(labelEnvironments, 10);
        dataComboFolders.left = new FormAttachment(labelEnvironments, 10);
        dataComboFolders.width = 422;
        comboFolder.setLayoutData(dataComboFolders);    
        comboFolder.setEnabled(false);
        
        addFolOpenEditorMenu();
        
        comboFolder.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent e) {
                try {
                    if (selectedFolderIndex.equals(comboFolder.getText())) {
                        return;
                    }

                    ApplicationMediator.getAppMediator().waitCursor();
                    
                    ViewFolderQueryBean folderQueryBean = (ViewFolderQueryBean) comboFolder
                        .getTable().getSelection()[0].getData();
                    mediator.clearErrors();
                    
                    if (folderQueryBean.getId() == 0) {
                        currentViewFolder = null;
                    } else {
                        try {
                            currentViewFolder = SAFRApplication.getSAFRFactory()
                                .getViewFolder(folderQueryBean.getId(), currentEnvironment.getId());
    
                        } catch (SAFRException e1) {
                            UIUtilities.handleWEExceptions(
                                e1,"Error occurred while retrieving the view folder.",null);
                        }
                    }
                    mediator.loadData();
                    mediator.refreshViews();

                } finally {
                    ApplicationMediator.getAppMediator().normalCursor();
                }
                selectedFolderIndex = comboFolder.getText();
                
            }
        });
        
        sectionCriteria.setClient(compositeCriteria);
    }

    private void addFolOpenEditorMenu() {
        Text text = comboFolder.getTextControl();
        Menu menu = text.getMenu();
        folOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        folOpenEditorItem.setText("Open Editor");
        folOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                ViewFolderQueryBean bean = (ViewFolderQueryBean)((StructuredSelection) comboFolderViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.ViewFolder);                        
                }                
            }
        });
        
        comboFolder.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    ViewFolderQueryBean bean = (ViewFolderQueryBean)((StructuredSelection) comboFolderViewer
                        .getSelection()).getFirstElement();
                    if (currentEnvironment.getId().equals(SAFRApplication.getUserSession().getEnvironment().getId()) && 
                        bean != null && bean.getId() != 0) {   
                        folOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        folOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }


    private void addEnvOpenEditorMenu()
    {
        Text text = comboEnvironment.getTextControl();
        Menu menu = text.getMenu();
        envOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        envOpenEditorItem.setText("Open Editor");
        envOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboEnvironmentViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.Environment);                        
                }                
            }
        });
        
        comboEnvironment.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboEnvironmentViewer
                            .getSelection()).getFirstElement();
                    if (bean != null) {   
                        envOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        envOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }

    private void refreshFolders() {
        comboFolder.setEnabled(true);        
        vfList = SAFRQuery.queryAllViewFolders(currentEnvironment.getId(), SortType.SORT_BY_NAME);
        Integer count = 0;
        if (vfList != null) {
            for (ViewFolderQueryBean vfQuerybean : vfList) {
                if (vfQuerybean.getId() == 0) {
                    vfList.remove(vfQuerybean);
                    break;
                }
            }
            ViewFolderQueryBean emptyBean = 
                new ViewFolderQueryBean(
                    null, 
                    0, "", null, null, null, null, null);
            vfList.add(0, emptyBean);
            for (ViewFolderQueryBean vfQuerybean : vfList) {
                comboFolder.setData(Integer.toString(count),vfQuerybean);
                count++;
            }
        }
        comboFolderViewer.setInput(vfList);        
    }

    public Control getEnvironmentSection() {
        return sectionCriteria;
    }

    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }       

    public ViewFolder getCurrentViewFolder() {
        return currentViewFolder;
    }       
    
    
}
