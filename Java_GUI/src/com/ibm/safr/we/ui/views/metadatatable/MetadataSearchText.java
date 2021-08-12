package com.ibm.safr.we.ui.views.metadatatable;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class MetadataSearchText extends WorkbenchWindowControlContribution {

    private MetadataView metadataTab = null;
    private Text searchText;
    private SAFRGUIToolkit safrGUIToolkit;
    private List<String> nameHistory = new ArrayList<String>();
    private Set<String> nameSet = new HashSet<String>();
    private int nameIdx = 0;
    private List<String> idHistory = new ArrayList<String>();
    private Set<String> idSet = new HashSet<String>();
    private int idIdx = 0;
    private String posHist = null;
    
    public MetadataSearchText() {
        safrGUIToolkit = new SAFRGUIToolkit();
        metadataTab = ApplicationMediator.getAppMediator().getMetaView();
    }

    public MetadataSearchText(String id) {
        super(id);
        safrGUIToolkit = new SAFRGUIToolkit();
        metadataTab = ApplicationMediator.getAppMediator().getMetaView();
    }

    @Override
    protected Control createControl(Composite parent) {
        RowLayout layout = new RowLayout();
        parent.setLayout(layout);
        Label label = new Label(parent, SWT.NONE);
        label.setText("Fi&lter");
        
        searchText = new Text(parent, SWT.NONE);
        searchText.setLayoutData(new RowData(200,30));
        searchText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                metadataTab.filterSearchText(searchText.getText().trim());
            }
            
        });
        UIUtilities.replaceMenuText(searchText);
        ApplicationMediator.getAppMediator().setMetadataFilterText(searchText);
        
        Button clear = safrGUIToolkit.createButton(parent, SWT.NONE, "x");
        clear.setLayoutData(new RowData(20,20));
        clear.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                searchText.setText("");
            }
            
        });
        
        Composite byGroup = safrGUIToolkit.createComposite(parent, SWT.None);
        RowLayout byGroupLayout = new RowLayout(); 
        byGroupLayout.marginTop = 0;
        byGroupLayout.spacing = 0;
        byGroup.setLayout(byGroupLayout);
        final Button byID = safrGUIToolkit.createRadioButton(byGroup, "by &ID");
        byID.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!byID.getSelection()) {
                    searchText.setText("");
                }
                metadataTab.filterByName(false);
                searchText.setFocus();
            }
            
        });
        final Button byName = safrGUIToolkit.createRadioButton(byGroup, "by &Name");
        byName.setSelection(true);
        byName.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                if (!byName.getSelection()) {
                    searchText.setText("");
                }
                metadataTab.filterByName(true);
                searchText.setFocus();
            }
            
        });

        searchText.addVerifyListener(new VerifyListener() {

            public void verifyText(VerifyEvent e) {
                if (byID.getSelection()) {
                    String txt = e.text;
                    if (txt.isEmpty()) {
                        return;
                    }
                    try {
                        Integer.parseInt(txt);
                    } catch (NumberFormatException ex) {
                        e.doit = false;
                    }                    
                }
            }
        });
        
        searchText.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                posHist = null;
                if (searchText.getText().length() > 0) {
                    // store string to history 
                    if (byID.getSelection()) {
                        if (!idSet.contains(searchText.getText())) {
                            if (idHistory.size() > 10) {
                                idSet.remove(idHistory.get(0));
                                idHistory.remove(0);
                            }
                            idHistory.add(searchText.getText()); 
                            idSet.add(searchText.getText());
                        }
                    }
                    else {
                        if (!nameSet.contains(searchText.getText())) {
                            if (nameHistory.size() > 10) {
                                nameSet.remove(nameHistory.get(0));
                                nameHistory.remove(0);
                            }                        
                            nameHistory.add(searchText.getText());
                            nameSet.add(searchText.getText());
                        }
                    }
                }
                if (byID.getSelection()) {
                    idIdx = idHistory.size();                     
                }
                else {
                    nameIdx = nameHistory.size(); 
                }
            }
            
        });
        
        searchText.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
                    if (posHist == null && searchText.getText().length() > 0) {
                        posHist = searchText.getText();
                    }
                }                
            }

            public void keyReleased(KeyEvent e) {                
                if (e.keyCode == SWT.ARROW_UP) {
                    if (byID.getSelection()) {
                        if (idIdx > 0) {
                            idIdx--;
                            if (idHistory.size() > 0) {
                                searchText.setText(idHistory.get(idIdx));
                            }
                        }
                    }
                    else {
                        if (nameIdx >0) {
                            nameIdx--;
                            if (nameHistory.size() > 0) {
                                searchText.setText(nameHistory.get(nameIdx));
                            }
                        }
                    }
                }
                else if (e.keyCode == SWT.ARROW_DOWN) {
                    if (byID.getSelection()) {
                        if (idIdx < idHistory.size() - 1) {
                            idIdx++;
                            if (idHistory.size() > 0) {
                                searchText.setText(idHistory.get(idIdx));
                            }
                        }
                    }
                    else {
                        if (nameIdx < nameHistory.size() - 1) {
                            nameIdx++;
                            if (nameHistory.size() > 0) {
                                searchText.setText(nameHistory.get(nameIdx));
                            }
                        }
                    }
                }  
                if (searchText.getText().length() == 0 && posHist != null) {
                    if (byID.getSelection()) {
                        if (!idSet.contains(posHist)) {
                            if (idHistory.size() > 10) {
                                idSet.remove(idHistory.get(0));
                                idHistory.remove(0);
                            }
                            idHistory.add(posHist);
                            idSet.add(posHist);
                            idIdx = idHistory.size();                                                 
                        }
                    }
                    else {
                        if (!nameSet.contains(posHist)) {
                            if (nameHistory.size() > 10) {
                                nameSet.remove(nameHistory.get(0));
                                nameHistory.remove(0);
                            }
                            nameHistory.add(posHist);
                            nameSet.add(posHist);
                            nameIdx = nameHistory.size();                             
                        }
                    }
                    posHist=null;
                }
            }            
        });        
        
        return null;
    }
    
}
