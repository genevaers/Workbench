package com.ibm.safr.we.ui.editors.find;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
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
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.model.utilities.FindReplaceText;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class FindReplaceCriteria {

    // mediator to talk to the rest of the Find/Replace Logic Text interface
    private FindReplaceMediator extMediator;
    
    // internal mediator for the criteria section 
    private FindReplaceCriteriaMediator intMediator;
    
    // Data
    FindReplaceText findReplaceText;
    
    // UI
    private SAFRGUIToolkit safrGuiToolkit;    
    private Section sectionSearchElement;
    private TableComboViewer comboEnvironmentViewer;
    private TableCombo comboEnvironment;
    private List<EnvironmentQueryBean> envList;
    private String selectedEnvironment = "";
    private EnvironmentQueryBean curEnv = null;    
    private Button buttonSearch;
    
    private MenuItem envOpenEditorItem = null;  
    
    protected FindReplaceCriteria() {
    }

    public void setMediator(FindReplaceMediator mediator) {
        this.extMediator = mediator;
        this.safrGuiToolkit = mediator.getToolkit();
    }
    
    protected void create(Composite parent) {
        sectionSearchElement = safrGuiToolkit.createSection(parent,
                Section.TITLE_BAR, "Search Criteria");
        FormData dataSectionSearchElement = new FormData();
        dataSectionSearchElement.top = new FormAttachment(0, 20);
        dataSectionSearchElement.left = new FormAttachment(0, 5);

        sectionSearchElement.setLayoutData(dataSectionSearchElement);

        Composite compositeSearchElement = safrGuiToolkit.createComposite(
                sectionSearchElement, SWT.NONE);
        compositeSearchElement.setLayout(new FormLayout());

        Control labelEnvironments = createEnvCombo(compositeSearchElement);

        // create findby and viewsin sections
        FindReplaceCriteriaFindBy findBy = new FindReplaceCriteriaFindBy();        
        FindReplaceCriteriaViewsIn viewsIn = new FindReplaceCriteriaViewsIn();        
        intMediator = new FindReplaceCriteriaMediator(
            extMediator, this, findBy, viewsIn);        
        findBy.setMediator(intMediator);      
        viewsIn.setMediator(intMediator);
        intMediator.create(compositeSearchElement, labelEnvironments);                
        
        buttonSearch = safrGuiToolkit.createButton(compositeSearchElement,
                SWT.PUSH, "&Search");
        buttonSearch.setData(SAFRLogger.USER, "Search");                                          
        FormData dataButtonSearch = new FormData();
        dataButtonSearch.top = new FormAttachment(intMediator.getBottomViewsIn(), 10);
        dataButtonSearch.left = new FormAttachment(0, 5);
        dataButtonSearch.width = 70;
        buttonSearch.setLayoutData(dataButtonSearch);
        buttonSearch.setEnabled(false);
        buttonSearch.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                onSearchButton();
            }
        });
        
        sectionSearchElement.setClient(compositeSearchElement);
    }
    

    private Control createEnvCombo(Composite parent) {
        Label labelEnvironments = safrGuiToolkit.createLabel(
            parent, SWT.NONE, "&Environment: ");
    
        FormData dataLabelEnvironments = new FormData();
        dataLabelEnvironments.top = new FormAttachment(0, 10);
        dataLabelEnvironments.left = new FormAttachment(0, 5);
        labelEnvironments.setLayoutData(dataLabelEnvironments);
    
        comboEnvironmentViewer = safrGuiToolkit.createTableComboForComponents(
                parent, ComponentType.Environment);
        comboEnvironment = comboEnvironmentViewer.getTableCombo();
        comboEnvironment.setData(SAFRLogger.USER, "Environment");                                     
    
        FormData dataComboEnvironments = new FormData();
        dataComboEnvironments.top = new FormAttachment(0, 10);
        dataComboEnvironments.left = new FormAttachment(labelEnvironments, 23);
        dataComboEnvironments.width = 400;
        comboEnvironment.setLayoutData(dataComboEnvironments);
    
        addEnvOpenEditorMenu();
        
        // load the data
        try {
            envList = SAFRQuery.queryEnvironmentsForLoggedInUser(
                    SortType.SORT_BY_NAME, false);    
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1,
                "Error occurred while retrieving environments for logged in user.",UIUtilities.titleStringDbException);
        }
        Integer count = 0;
        if (envList != null) {
            for (EnvironmentQueryBean enviromentQuerybean : envList) {
                comboEnvironment.setData(Integer.toString(count),
                        enviromentQuerybean);
                count++;
            }
        }
    
        comboEnvironment.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                    if (!comboEnvironment.getText().equals(selectedEnvironment)) {
                        // clear the search results and disable the replace, and
                        // selectall and deselectall
                        // buttons
                        findReplaceText = null;
                        intMediator.clearViewsList();
                        extMediator.clearResults();
                        extMediator.clearReplace();
                        intMediator.updateLabels();
                        extMediator.disableResults();
                        curEnv = (EnvironmentQueryBean) comboEnvironment
                                .getTable().getSelection()[0].getData();
                        extMediator.waitCursor();                        
                        setSearchButtonState(intMediator.getSearchButtonState());
                        selectedEnvironment = comboEnvironment.getText();
                    }
                } finally {
                    extMediator.normalCursor();
                }
            }
        });
        comboEnvironmentViewer.setInput(envList);
        Environment environment = SAFRApplication.getUserSession()
                .getEnvironment();
        String currentEnv = environment.getName();
        comboEnvironment.select(comboEnvironment.indexOf(UIUtilities
                .getComboString(currentEnv, environment.getId())));
     
        return labelEnvironments;
    }
    
    protected void setSearchButtonState(boolean state) {
        buttonSearch.setEnabled(state);
    }
    
    private void onSearchButton() {
        try {
            extMediator.waitCursor();
            findReplaceText = null;            
            extMediator.clearResults();
            generateSearch();
            List<FindReplaceComponent> compList = findReplaceText.findViews();
            // if is a field filer out anything not this LR related
            if (!intMediator.isFindByText() && intMediator.getComponentType() == ComponentType.LogicalRecord) {
                List<FindReplaceComponent> filtCompList = onSearchLRField(compList);
                extMediator.setResults(filtCompList);
            }
            else {
                extMediator.setResults(compList);
            }
            extMediator.clearMessage();
        } catch (SAFRValidationException e1) {
            if (!(e1.getMessageString().equals(""))) {
                extMediator.setMessage(e1);
                MessageDialog.openError(extMediator.getSite().getShell(),
                        "Validation error", e1.getMessageString());
            }
        } catch (SAFRException e1) {
            UIUtilities.handleWEExceptions(e1);
        } finally {
            extMediator.normalCursor();
        }

        extMediator.refreshResults();                    
    }
    
    private List<FindReplaceComponent> onSearchLRField(List<FindReplaceComponent> compList) throws DAOException {
        List<FindReplaceComponent> filtCompList = new ArrayList<FindReplaceComponent>();
        for (FindReplaceComponent comp : compList) {
            // check for source field pattern 
            String chksrc = "{" + intMediator.getCurField().getName() + "}";
            if (comp.getLogicText().contains(chksrc)) {
                // so confirm that the source equals the selected LR
                int selLRid = intMediator.getCurComp().getId();
                // check logic text type
                if (comp.getLogicTextType() == LogicTextType.Extract_Record_Filter ||
                    comp.getLogicTextType() == LogicTextType.Extract_Record_Output) {
                    int viewSourceId = comp.getParentId();   
                    int fndLRid = DAOFactoryHolder.getDAOFactory().getViewSourceDAO().
                        getViewSourceLrId(viewSourceId, curEnv.getId());
                    if (selLRid == fndLRid) {
                        filtCompList.add(comp);
                        continue;
                    }
                }
                else if (comp.getLogicTextType() == LogicTextType.Extract_Column_Assignment) {
                    int viewColSourceId = comp.getParentId();                            
                    int fndLRid = DAOFactoryHolder.getDAOFactory().getViewColumnSourceDAO().
                        getViewColumnSourceLrId(viewColSourceId, curEnv.getId());
                    if (selLRid == fndLRid) {
                        filtCompList.add(comp);
                        continue;
                    }                            
                }
            }
            
            // check for lookup pattern
            String chklpr = "\\{(\\w[\\d\\w#_]*)[\\.,;]" + intMediator.getCurField().getName() + "[\\},;]";
            Pattern pattern = Pattern.compile(chklpr);
            Matcher matcher = pattern.matcher(comp.getLogicText());
            while (matcher.find()) {
                String lpName = matcher.group(1);
                if (lpName != null && lpName.length() > 0) {
                    int fndLRid = DAOFactoryHolder.getDAOFactory().getLookupPathStepDAO().
                        getTargetLookUpPathLrId(curEnv.getId(), lpName);
                    int selLRid = intMediator.getCurComp().getId();
                    if (selLRid == fndLRid) {
                        filtCompList.add(comp);
                        break;
                    }                            
                }
            }
        }
        return filtCompList;
    }
    
    
    protected void generateSearch() {
        findReplaceText = new FindReplaceText();
        findReplaceText.setEnvironmentId(curEnv.getId());
        findReplaceText.setSearchViewsIn(intMediator.getSearchViewsIn());
        if (intMediator.isRefineSearchViews()) {
            findReplaceText.setSearchCriteria(intMediator.getSearchCriteria());
            findReplaceText.setSearchPeriod(intMediator.getSearchPeriod());
        } else {
            findReplaceText.setSearchCriteria(SearchCriteria.None);
            findReplaceText.setSearchPeriod(SearchPeriod.None);
        }
        findReplaceText.setDateToSearch(intMediator.getDateToSearch());
        if (intMediator.isFindByText()) {
            findReplaceText.setSearchText(intMediator.getFindText());
        }
        else {  
            findReplaceText.setComponent(true);
            ComponentType componentType = intMediator.getComponentType();
            if (componentType == ComponentType.LogicalRecord) {
                String regExp = "([\\{\\.,;])(" + intMediator.getCurField().getName() + ")([\\},;])"; 
                findReplaceText.setSearchText(regExp);                
            }
            else if (componentType == ComponentType.LogicalFile) {
                String regExp = "(\\{)(" + intMediator.getCurComp().getName() + ")([\\}\\.])"; 
                findReplaceText.setSearchText(regExp);                
            }
            else if (componentType == ComponentType.PhysicalFile) {
                String regExp = "(\\.)(" + intMediator.getCurComp().getName() + ")(\\})"; 
                findReplaceText.setSearchText(regExp);                
            }
            else if (componentType == ComponentType.UserExitRoutine) {
                String regExp = "(\\{)(" + intMediator.getCurComp().getName() + ")(\\})"; 
                findReplaceText.setSearchText(regExp);                
            }
            else if (componentType == ComponentType.LookupPath) {
                String regExp = "(\\{)(" + intMediator.getCurComp().getName() + ")([\\}\\.,;])"; 
                findReplaceText.setSearchText(regExp);                
            }
        }
        findReplaceText.setCaseSensitive(intMediator.isMatchingCase());
        if (intMediator.isFindByText()) {
            findReplaceText.setUsePatternMatching(intMediator.isPatternMatch());
        }
        else {
            findReplaceText.setUsePatternMatching(true);
        }
        if (intMediator.getSearchViewsIn() == SearchViewsIn.SearchAllViews) {
            findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
        } else {
            findReplaceText.setSearchRangeList(intMediator.getViewsList());
        }
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
    
    /**
     * This method is used to get the widget based on the property passed.
     * 
     * @param property
     * @return the widget.
     */
    protected Control getControlFromProperty(Object property) {
        Control con = intMediator.getControlFromFindBy(property);
        if (con == null) {
            con = intMediator.getControlFromViewsIn(property);            
            if (con == null) {
                if (property == FindReplaceText.Property.ENVIRONMENTID) {
                    return comboEnvironment;
                }
                return null;
            }
            else {
                return con;                
            }
        }
        else {
            return con;
        }        
    }
    
    protected void setFocusOnEnv() {
        comboEnvironment.setFocus();
    }
    
    protected EnvironmentQueryBean getCurrentEnv() {
        return curEnv;
    }    
    
    protected EnvironmentQueryBean getComboEnv() {
        EnvironmentQueryBean ebean = (EnvironmentQueryBean)((StructuredSelection) comboEnvironmentViewer
            .getSelection()).getFirstElement();                 
        return ebean;
    }
    
    protected Section getCriteriaSection() {
        return sectionSearchElement;
    }
    
    protected String getFindText() {
        return intMediator.getFindText();        
    }

    public FindReplaceText getSearch() {
        return findReplaceText;
    }

    public void clearFind() {
        findReplaceText = null;        
    }    
    
}
