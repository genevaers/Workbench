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
import java.util.Date;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.model.utilities.FindReplaceText;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class FindReplaceCriteriaViewsIn {
    
    private final static String COUNT_NOTE = " selected";
        
    private FindReplaceCriteriaMediator mediator;

    // Data
    
    // UI
    private SAFRGUIToolkit safrGuiToolkit;   
    
    // views to search in
    private Section sectionViewToSearchIn;
    private Label labelSelectedViewFolders;
    private Label labelSelectedViews;    
    private Button radioSearchAllViews;   
    private Button radioSearchSpecificViews;
    private Button radioSearchTheseViewFolders;
    private Button buttonSearchSpecificViews;
    private Button buttonSearchViewFolders;    
    private Button checkRefineSearchViews;
    private Group groupRefineViewSearch;    
    private Button radioCreated;
    private Button radioModified;
    private Combo comboRefineViewSearch;
    private DateTime dateViewCreatedModified;    
    private List<EnvironmentalQueryBean> viewsList = new ArrayList<EnvironmentalQueryBean>();
    private SearchViewsIn searchViewsIn = SearchViewsIn.SearchAllViews;
    private SearchCriteria searchCriteria = SearchCriteria.SearchCreated;
    private SearchPeriod searchPeriod = SearchPeriod.After;
    
    protected FindReplaceCriteriaViewsIn() {
        
    }

    protected void setMediator(FindReplaceCriteriaMediator mediator) {
        this.mediator = mediator;
        this.safrGuiToolkit = mediator.getToolkit();                
    }
    
    protected void create(Composite body, Control top) {
        sectionViewToSearchIn = safrGuiToolkit.createSection(body,
                Section.TITLE_BAR | Section.TWISTIE, "Views to Search In");
        FormData dataSectionSearchElement = new FormData();
        dataSectionSearchElement.top = new FormAttachment(top, 10);
        dataSectionSearchElement.left = new FormAttachment(0, 5);
        dataSectionSearchElement.right = new FormAttachment(100, -5);
        sectionViewToSearchIn.setLayoutData(dataSectionSearchElement);
        sectionViewToSearchIn.setExpanded(true);
        Composite compositeViewToSearchIn = safrGuiToolkit.createComposite(
                sectionViewToSearchIn, SWT.NONE);
        compositeViewToSearchIn.setLayout(new FormLayout());

        String searchAllViews = "Search a&ll views in the specified environment";

        radioSearchAllViews = safrGuiToolkit.createButton(
                compositeViewToSearchIn, SWT.RADIO, searchAllViews);
        radioSearchAllViews.setData(SAFRLogger.USER, "Search all views");                                                         
        FormData dataRadioSearchAllViews = new FormData();
        dataRadioSearchAllViews.top = new FormAttachment(0, 10);
        dataRadioSearchAllViews.left = new FormAttachment(0, 5);
        radioSearchAllViews.setLayoutData(dataRadioSearchAllViews);
        radioSearchAllViews.setSelection(true);
        radioSearchAllViews.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                buttonSearchSpecificViews.setEnabled(false);
                buttonSearchViewFolders.setEnabled(false);
                if (searchViewsIn != SearchViewsIn.SearchAllViews) {
                    searchViewsIn = SearchViewsIn.SearchAllViews;                   
                    mediator.clearResults();                 
                }
                viewsList.clear();
                mediator.clearMessage();
                updateLabels();
            }
        });

        radioSearchSpecificViews = safrGuiToolkit.createButton(
                compositeViewToSearchIn, SWT.RADIO,
                "Search these specific vie&w(s):");
        radioSearchSpecificViews.setData(SAFRLogger.USER, "Search specific views");                                                               
        FormData dataRadioSearchSpecificViews = new FormData();
        dataRadioSearchSpecificViews.top = new FormAttachment(
                radioSearchAllViews, 12);
        dataRadioSearchSpecificViews.left = new FormAttachment(0, 5);
        radioSearchSpecificViews.setLayoutData(dataRadioSearchSpecificViews);
        radioSearchSpecificViews.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                buttonSearchSpecificViews.setEnabled(true);
                buttonSearchViewFolders.setEnabled(false);
                if (searchViewsIn != SearchViewsIn.SearchInSpecificViews) {
                    searchViewsIn = SearchViewsIn.SearchInSpecificViews;                   
                    mediator.clearResults();                 
                }
                viewsList.clear();
                mediator.clearMessage();
                updateLabels();
            }

        });

        buttonSearchSpecificViews = safrGuiToolkit.createButton(
                compositeViewToSearchIn, SWT.PUSH, "...");
        buttonSearchSpecificViews.setData(SAFRLogger.USER, "Search specific views button");                                                                       
        FormData dataButtonSearchSpecificViews = new FormData();
        dataButtonSearchSpecificViews.top = new FormAttachment(
                radioSearchAllViews, 8);
        buttonSearchSpecificViews.setLayoutData(dataButtonSearchSpecificViews);

        buttonSearchSpecificViews.setEnabled(false);
        buttonSearchSpecificViews.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                try {
                    mediator.waitCursor();
                    MetadataListDialog dialog = new MetadataListDialog(
                            mediator.getSite().getShell(), TreeItemId.VIEWFOLDERCHILD,
                            null, mediator.getCurentEnv().getId());
                    dialog.create();
                    dialog.getShell().setText("Select View(s) to search in");
                    dialog.setCheckedElements(viewsList.toArray(new SAFRQueryBean[viewsList.size()]));
                    dialog.open();
                    List<SAFRQueryBean> returnList = new ArrayList<SAFRQueryBean>();                    
                    if (dialog.getReturnCode() == Window.OK) {
                        if (dialog.getReturnList() != null) {
                            returnList = dialog.getReturnList();
                        }

                    } else {
                        returnList.clear();
                        return;
                    }
                    viewsList.clear();
                    if (returnList.size() > 0) {
                        for (int i = 0; i < returnList.size(); i++) {
                            EnvironmentalQueryBean bean = (EnvironmentalQueryBean) returnList
                                    .get(i);
                            viewsList.add(bean);
                        }
                    }
                    updateLabels();
                } finally {
                    mediator.normalCursor();
                }
            }

        });

        labelSelectedViews = safrGuiToolkit.createLabel(
                compositeViewToSearchIn, SWT.NONE, "0" + COUNT_NOTE);
        FormData dataLabelSelectedViews = new FormData();
        dataLabelSelectedViews.top = new FormAttachment(
                buttonSearchSpecificViews, 0, SWT.CENTER);
        dataLabelSelectedViews.left = new FormAttachment(
                buttonSearchSpecificViews, 5);
        labelSelectedViews.setLayoutData(dataLabelSelectedViews);
        labelSelectedViews.setVisible(false);

        radioSearchTheseViewFolders = safrGuiToolkit.createButton(
                compositeViewToSearchIn, SWT.RADIO,
                "Search views in these view f&older(s):");
        radioSearchTheseViewFolders.setData(SAFRLogger.USER, "Search views in view folders");                                                                       
        
        FormData dataRadioSearchTheseViewFolders = new FormData();
        dataRadioSearchTheseViewFolders.top = new FormAttachment(
                radioSearchSpecificViews, 12);
        dataRadioSearchTheseViewFolders.left = new FormAttachment(0, 5);
        radioSearchTheseViewFolders
                .setLayoutData(dataRadioSearchTheseViewFolders);
        radioSearchTheseViewFolders.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                buttonSearchSpecificViews.setEnabled(false);
                buttonSearchViewFolders.setEnabled(true);
                if (searchViewsIn != SearchViewsIn.SearchInViewFolders) {
                    searchViewsIn = SearchViewsIn.SearchInViewFolders;                   
                    mediator.clearResults();                 
                }
                viewsList.clear();
                mediator.clearMessage();
                updateLabels();
            }

        });

        buttonSearchViewFolders = safrGuiToolkit.createButton(
                compositeViewToSearchIn, SWT.PUSH, "...");
        buttonSearchViewFolders.setData(SAFRLogger.USER, "Search views in view folders button");                                                                              
        FormData dataButtonSearchViewFolders = new FormData();
        dataButtonSearchViewFolders.top = new FormAttachment(
                buttonSearchSpecificViews, 5);
        dataButtonSearchViewFolders.left = new FormAttachment(
                radioSearchTheseViewFolders, 5);
        buttonSearchViewFolders.setLayoutData(dataButtonSearchViewFolders);

        // for alignment purpose while changing font.
        dataButtonSearchSpecificViews.left = new FormAttachment(
                buttonSearchViewFolders, 0, SWT.LEFT);

        buttonSearchViewFolders.setEnabled(false);
        buttonSearchViewFolders.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                try {
                    mediator.waitCursor();
                    MetadataListDialog dialog = new MetadataListDialog(
                            mediator.getSite().getShell(), TreeItemId.VIEWFOLDER, null,
                            mediator.getCurentEnv().getId());
                    dialog.create();
                    dialog.getShell().setText(
                            "Select View Folder(s) to search in");
                    dialog.setCheckedElements(viewsList
                            .toArray(new SAFRQueryBean[viewsList.size()]));
                    dialog.open();

                    List<SAFRQueryBean> returnList = new ArrayList<SAFRQueryBean>();                                        
                    if (dialog.getReturnCode() == Window.OK) {
                        if (dialog.getReturnList() != null) {
                            returnList = dialog.getReturnList();
                        }
                    } else {
                        returnList.clear();
                        return;
                    }
                    viewsList.clear();
                    if (returnList.size() > 0) {
                        for (int i = 0; i < returnList.size(); i++) {
                            viewsList.add((EnvironmentalQueryBean) returnList
                                    .get(i));
                        }
                    }
                    updateLabels();
                } finally {
                    mediator.normalCursor();
                }
            }

        });

        labelSelectedViewFolders = safrGuiToolkit.createLabel(
                compositeViewToSearchIn, SWT.NONE, "0" + COUNT_NOTE);
        FormData dataLabelSelectedViewFolders = new FormData();
        dataLabelSelectedViewFolders.top = new FormAttachment(
                buttonSearchViewFolders, 0, SWT.CENTER);
        dataLabelSelectedViewFolders.left = new FormAttachment(
                buttonSearchViewFolders, 5);
        labelSelectedViewFolders.setLayoutData(dataLabelSelectedViewFolders);
        labelSelectedViewFolders.setVisible(false);

        checkRefineSearchViews = safrGuiToolkit.createButton(
                compositeViewToSearchIn, SWT.CHECK,
                "Refine Search to &View(s):");
        checkRefineSearchViews.setData(SAFRLogger.USER, "Refine Search to Views");                                                                                    
        FormData dataCheckRefineViewSearch = new FormData();
        dataCheckRefineViewSearch.top = new FormAttachment(
                radioSearchTheseViewFolders, 25);
        dataCheckRefineViewSearch.left = new FormAttachment(0, 5);
        checkRefineSearchViews.setLayoutData(dataCheckRefineViewSearch);
        checkRefineSearchViews.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (checkRefineSearchViews.getSelection()) {
                    if (searchCriteria == SearchCriteria.SearchCreated) {
                        radioCreated.setSelection(true);
                    } else {
                        radioModified.setSelection(true);
                    }
                    radioCreated.setEnabled(true);
                    radioModified.setEnabled(true);
                    comboRefineViewSearch.setEnabled(true);
                    dateViewCreatedModified.setEnabled(true);
                } else {
                    radioCreated.setEnabled(false);
                    radioModified.setEnabled(false);
                    comboRefineViewSearch.setEnabled(false);
                    dateViewCreatedModified.setEnabled(false);
                }
                mediator.clearResults();
            }
        });

        groupRefineViewSearch = safrGuiToolkit.createGroup(
                compositeViewToSearchIn, SWT.NONE, "");
        FormData dataGroupRefineViewSearch = new FormData();
        dataGroupRefineViewSearch.top = new FormAttachment(
                checkRefineSearchViews, 0);
        dataGroupRefineViewSearch.left = new FormAttachment(0, 12);
        groupRefineViewSearch.setLayoutData(dataGroupRefineViewSearch);
        groupRefineViewSearch.setLayout(new FormLayout());

        radioCreated = safrGuiToolkit.createButton(groupRefineViewSearch,
                SWT.RADIO, "Create&d");
        radioCreated.setData(SAFRLogger.USER, "Created");                                                                                         
        FormData dataRadioCreated = new FormData();
        dataRadioCreated.top = new FormAttachment(0, 0);
        dataRadioCreated.left = new FormAttachment(0, 10);
        radioCreated.setLayoutData(dataRadioCreated);
        radioCreated.setSelection(true); // default selected
        radioCreated.setEnabled(false);
        radioCreated.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                if (searchCriteria != SearchCriteria.SearchCreated) {
                    searchCriteria = SearchCriteria.SearchCreated;
                    mediator.clearResults();
                }               
            }

        });

        radioModified = safrGuiToolkit.createButton(groupRefineViewSearch,
                SWT.RADIO, "&Modified");
        radioModified.setData(SAFRLogger.USER, "Modified");                                                                                               
        FormData dataRadioModified = new FormData();
        dataRadioModified.top = new FormAttachment(0, 0);
        dataRadioModified.left = new FormAttachment(radioCreated, 10);
        dataRadioModified.right = new FormAttachment(100, -10);
        radioModified.setLayoutData(dataRadioModified);
        radioModified.setEnabled(false);
        radioModified.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                if (searchCriteria != SearchCriteria.SearchModified) {
                    searchCriteria = SearchCriteria.SearchModified;
                    mediator.clearResults();
                }               
            }

        });

        comboRefineViewSearch = safrGuiToolkit.createComboBox(
                groupRefineViewSearch, SWT.READ_ONLY, "");
        comboRefineViewSearch.setData(SAFRLogger.USER, "Date Modifier");                                                                                                      
        FormData dataComboRefineViewSearch = new FormData();
        dataComboRefineViewSearch.top = new FormAttachment(radioCreated, 10);
        dataComboRefineViewSearch.left = new FormAttachment(0, 10);
        comboRefineViewSearch.setLayoutData(dataComboRefineViewSearch);
        comboRefineViewSearch.add("After");
        comboRefineViewSearch.add("Before");
        comboRefineViewSearch.add("On");
        comboRefineViewSearch.select(0);
        comboRefineViewSearch.setEnabled(false);
        comboRefineViewSearch.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (comboRefineViewSearch.getText().equals("After")) {
                    if (searchPeriod != SearchPeriod.After) {
                        searchPeriod = SearchPeriod.After;
                        mediator.clearResults();
                    }
                } else if (comboRefineViewSearch.getText().equals("Before")) {
                    if (searchPeriod != SearchPeriod.Before) {
                        searchPeriod = SearchPeriod.Before;  
                        mediator.clearResults();                        
                    }
                } else if (comboRefineViewSearch.getText().equals("On")) {
                    if (searchPeriod != SearchPeriod.On) {
                        searchPeriod = SearchPeriod.On;                        
                        mediator.clearResults();                        
                    }
                }
            }
        });

        dateViewCreatedModified = safrGuiToolkit.createDateTime(
                groupRefineViewSearch, SWT.DATE | SWT.MEDIUM | SWT.BORDER);
        dateViewCreatedModified.setData(SAFRLogger.USER, "Date");                                                                                                     
        FormData dataDateViewCreatedModified = new FormData();
        dataDateViewCreatedModified.top = new FormAttachment(radioCreated, 10);
        dataDateViewCreatedModified.left = new FormAttachment(
                comboRefineViewSearch, 10);
        dataDateViewCreatedModified.right = new FormAttachment(100, -10);
        dataDateViewCreatedModified.bottom = new FormAttachment(100, -10);
        dateViewCreatedModified.setLayoutData(dataDateViewCreatedModified);
        dateViewCreatedModified.setEnabled(false);
        dateViewCreatedModified.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                mediator.clearResults();
            }
        });
        
        sectionViewToSearchIn.setClient(compositeViewToSearchIn);

    }
    
    protected void updateLabels() {
        if (radioSearchSpecificViews.getSelection()) {
            labelSelectedViews.setText(viewsList.size() + COUNT_NOTE);
            labelSelectedViews.setVisible(true);
            labelSelectedViews.pack(true);
        } else {
            labelSelectedViews.setVisible(false);
        }
        if (radioSearchTheseViewFolders.getSelection()) {
            labelSelectedViewFolders.setText(viewsList.size() + COUNT_NOTE);
            labelSelectedViewFolders.setVisible(true);
            labelSelectedViewFolders.pack(true);
        } else {
            labelSelectedViewFolders.setVisible(false);
        }

    }

    protected Control getBottomViewsIn() {
        return sectionViewToSearchIn;
    }

    protected void clearViewsList() {
        viewsList.clear();
    }

    public List<EnvironmentalQueryBean> getViewsList() {
        return viewsList;
    }
    
    protected boolean isRefineSearchViews() {
        return checkRefineSearchViews.getSelection();
    }

    protected SearchViewsIn getSearchViewsIn() {
        return searchViewsIn;
    }

    protected SearchCriteria getSearchCriteria() {
        return searchCriteria;
    }

    protected SearchPeriod getSearchPeriod() {
        return searchPeriod;
    }

    protected Date getDateToSearch() {
        return UIUtilities.dateFromPicker(dateViewCreatedModified);
    }

    protected Control getControlFromProperty(Object property) {
        if (property == FindReplaceText.Property.SEARCHVIEWSIN) {
            return sectionViewToSearchIn;
        }
        else if (property == FindReplaceText.Property.SEARCHINVIEWFOLDERS) {
            return radioSearchTheseViewFolders;
        }
        else if (property == FindReplaceText.Property.SEARCHSPECIFICVIEWS) {
            return radioSearchSpecificViews;
        }
        else if (property == FindReplaceText.Property.SEARCHCRITERIA) {
            return groupRefineViewSearch;
        }
        else if (property == FindReplaceText.Property.SEARCHPERIOD) {
            return comboRefineViewSearch;
        }
        else if (property == FindReplaceText.Property.DATETOSEARCH) {
            return dateViewCreatedModified;
        }
        else {
            return null;
        }        
    }
    
    
}
