package com.ibm.safr.we.ui.editors.permission;

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
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class EnvPermEnvironment {

    private EnvPermMediator mediator;
    
    // UI elements
    private Section section;
    private TableCombo comboEnv;
    private TableComboViewer comboEnvViewer;
    private MenuItem envOpenEditorItem = null;              
    
    // Model
    private Environment env = null;
    private List<EnvironmentQueryBean> envList;
    
    public EnvPermEnvironment() {
    }
    
    protected void setMediator(EnvPermMediator mediator) {
        this.mediator = mediator;
    }

    protected Section getSection() {
        return section;
    }

    protected Environment getCurrentEnvironment() {
        return env;
    }
    
    protected void create() {
        
        section = mediator.getFormToolkit().createSection(mediator.getForm().getBody(), 
            Section.TITLE_BAR|Section.EXPANDED);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0,0);
        formData.bottom = new FormAttachment(15,0);
        formData.left = new FormAttachment(0,10);
        formData.right = new FormAttachment(35,2);
        section.setLayoutData(formData);
        section.setText("Permissions By");
        
        Composite sectionClient = mediator.getFormToolkit().createComposite(section);
        sectionClient.setLayout(new FormLayout());
        
        Label groupLab = mediator.getSAFRGUIToolkit().createLabel(sectionClient, SWT.NONE, "&Environment: ");
        FormData labData = new FormData();
        labData.top = new FormAttachment(0,0);
        labData.left = new FormAttachment(0,10);
        groupLab.setLayoutData(labData);

        comboEnvViewer = mediator.getSAFRGUIToolkit().createTableComboForComponents(
            sectionClient, ComponentType.Environment);
        comboEnv = comboEnvViewer.getTableCombo();
        comboEnv.setData(SAFRLogger.USER,"Environment");
        addEnvOpenEditorMenu();
        FormData combData = new FormData();
        combData.top = new FormAttachment(0,0);
        combData.left = new FormAttachment(groupLab,10);
        comboEnv.setLayoutData(combData);

        comboEnv.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                EnvironmentQueryBean envQueryBean = null;    
                if (comboEnv.getTable().getSelection().length > 0) {
                    envQueryBean = (EnvironmentQueryBean) comboEnv.getTable().getSelection()[0].getData();
                    try {
                        env = SAFRApplication.getSAFRFactory().getEnvironment(envQueryBean.getId());
                        mediator.onEnvironmentSelected();                
                    } catch (SAFRException e1) {
                        UIUtilities.handleWEExceptions(e1,"Error occurred while retrieving the environment.",null);
                    }
                }
            }            
        });

        // load the data
        try {
            if (SAFRApplication.getUserSession().isSystemAdministrator()) {
                envList = SAFRQuery.queryAllEnvironments(SortType.SORT_BY_NAME);
            } else  {
                envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
                        SortType.SORT_BY_NAME, true);
            }

        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1);
        }
        Integer counter = 0;
        if (envList != null) {
            for (EnvironmentQueryBean envQueryBean : envList) {
                comboEnv.setData(Integer.toString(counter), envQueryBean);
                counter++;
            }
        }

        comboEnvViewer.setInput(envList);
        
        section.setClient(sectionClient);
        
    }
    
    private void addEnvOpenEditorMenu()
    {
        Text text = comboEnv.getTextControl();
        Menu menu = text.getMenu();
        envOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        envOpenEditorItem.setText("Open Editor");
        envOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboEnvViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.Environment);                        
                }                
            }
        });
        
        comboEnv.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                    EnvironmentQueryBean bean = (EnvironmentQueryBean)((StructuredSelection) comboEnvViewer
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
}
