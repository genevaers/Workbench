package com.ibm.safr.we.ui.editors;

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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.SAFRQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.MetadataListDialog;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationContentProvider;
import com.ibm.safr.we.ui.views.metadatatable.ComponentAssociationLabelProvider;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ViewFolderEditor extends SAFREditorPart {

	public static String ID = "SAFRWE.ViewFolderEditor";
	private static final int MAX_NAME_LENGTH = 32;

	private Section sectionNewViewFolder;
    private Text textID;
    private Text textName;
    private Text textComments;
    private Label labelCreatedValue;
    private Label labelModifiedValue;
	private String defaultCreated = "-";
	private String defaultModified = "-";

    private Section sectionViews;
    private Table tableViews;
    private CheckboxTableViewer tableViewerViews;
    private int prevSelection;
    private Button buttonAdd;
    private Button buttonRemove;

	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;
	private ViewFolderEditorInput viewFolderInput;
	private ViewFolder viewFolder;	

	public ViewFolderEditor() {
	}

	@Override
	public boolean isSaveAsAllowed() {
        boolean retval = false;        
        if(viewFolder.getId() > 0) {
            retval = isSaveAsAllowed(Permissions.CreateViewFolder);
        }
        return retval;
	}

	@Override
	public void createPartControl(Composite parent) {
		viewFolderInput = (ViewFolderEditorInput) getEditorInput();
		viewFolder = viewFolderInput.getViewFolder();

		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit
				.setReadOnly(viewFolderInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);

		form.getBody().setLayout(UIUtilities.createTableLayout(2, false));
		createGroupNewViewFolder(form.getBody());
	    createAssociatedViews(form.getBody());

		refreshControls();
		setDirty(false);
		// Used to load the context sensitive help
		if (viewFolder.getId() > 0) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.ViewFolderEditor");
		} else {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
					"com.ibm.safr.we.help.NewViewFolder");
		}
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

	private void createGroupNewViewFolder(Composite compositeValues) {

		sectionNewViewFolder = safrGuiToolkit.createSection(compositeValues,
				Section.TITLE_BAR, "General Information");
		sectionNewViewFolder.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB));

		Composite groupNewViewFolder = safrGuiToolkit.createComposite(
				sectionNewViewFolder, SWT.NONE);
		groupNewViewFolder.setLayout(UIUtilities.createTableLayout(3, true));

		safrGuiToolkit.createLabel(groupNewViewFolder,SWT.NONE, "ID:");

		textID = safrGuiToolkit.createTextBox(groupNewViewFolder, SWT.NONE);
		textID.setEnabled(false);
		textID.setLayoutData(UIUtilities.textTableData(1));

		Label filler1 = safrGuiToolkit.createLabel(groupNewViewFolder,
				SWT.NONE, "");
		filler1.setLayoutData(UIUtilities.textTableData(1));

		safrGuiToolkit.createLabel(groupNewViewFolder,SWT.NONE, "&Name:");

		textName = safrGuiToolkit.createNameTextBox(groupNewViewFolder, SWT.NONE);
        textName.setData(SAFRLogger.USER, "Name");                                                		
		textName.setLayoutData(UIUtilities.textTableData(2));
		textName.setTextLimit(MAX_NAME_LENGTH);
		textName.addModifyListener(this);

		safrGuiToolkit.createLabel(groupNewViewFolder,SWT.NONE, "C&omments:");

		textComments = safrGuiToolkit.createCommentsTextBox(groupNewViewFolder);
		textComments.setData(SAFRLogger.USER, "Comments");                                                        		
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 2,
				textComments.getLineHeight() * 10));
		textComments.addModifyListener(this);

		safrGuiToolkit.createLabel(groupNewViewFolder,SWT.NONE, "Created:");

		labelCreatedValue = safrGuiToolkit.createLabel(groupNewViewFolder,
				SWT.NONE, defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(2));

		safrGuiToolkit.createLabel(groupNewViewFolder,SWT.NONE, "Last Modified:");

		labelModifiedValue = safrGuiToolkit.createLabel(groupNewViewFolder,
				SWT.NONE, defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(2));

		sectionNewViewFolder.setClient(groupNewViewFolder);
	}

    private void createAssociatedViews(Composite body) {
        sectionViews = safrGuiToolkit.createSection(body,
                Section.TITLE_BAR, "Associated Views");
        sectionViews.setLayoutData(new TableWrapData(
                TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
        
        Composite compositeViews = safrGuiToolkit.createComposite(
            sectionViews, SWT.NONE);
        FormLayout layoutViews = new FormLayout();
        layoutViews.marginTop = 5;
        layoutViews.marginBottom = 5;
        layoutViews.marginLeft = 5;
        layoutViews.marginRight = 5;
        compositeViews.setLayout(layoutViews);
    
        createViewsTable(compositeViews);
        createButtons(compositeViews);
        createOpenEditorMenu();     
        
        sectionViews.setClient(compositeViews);        
    }

    protected void createViewsTable(Composite compositeViews) {
        tableViews = new Table(compositeViews, SWT.CHECK
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        tableViews.setData(SAFRLogger.USER, "Associated Views");      
        tableViewerViews = new CheckboxTableViewer(tableViews);
    
        FormData formDataTableView = new FormData();
        formDataTableView.left = new FormAttachment(0, 0);
        formDataTableView.top = new FormAttachment(0, 10);
        formDataTableView.bottom = new FormAttachment(100, 0);
        formDataTableView.height = 100;
        tableViews.setLayoutData(formDataTableView);
        toolkit.adapt(tableViews, false, false);
        
        UIUtilities.prepareTableViewerForShortList(tableViewerViews, ComponentType.View,450);
        tableViewerViews.setContentProvider(new ComponentAssociationContentProvider());
        tableViewerViews.setLabelProvider(new ComponentAssociationLabelProvider());
        tableViewerViews.setInput(viewFolder.getViewAssociations());
        
        tableViewerViews.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                if (tableViewerViews.getCheckedElements().length > 0 && 
                    viewFolderInput.getEditRights() != EditRights.Read) {
                    buttonRemove.setEnabled(true);
                } else {
                    buttonRemove.setEnabled(false);
                }
            }
        });
        
        tableViews.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
              
                if (event.detail == SWT.CHECK) {                  
                    int selRow = tableViews.indexOf((TableItem)event.item);  
                    int stateMask=event.stateMask;                  
                    if ((stateMask & SWT.SHIFT)==SWT.SHIFT) {
                        int prevRow = prevSelection;
                      
                        if ((stateMask & SWT.CTRL)!=SWT.CTRL) {
                            tableViewerViews.setAllChecked(false);
                        }
                        if (prevRow > selRow) {
                            for (int i=selRow ; i<=prevRow ; i++) {
                                Object element = tableViewerViews.getElementAt(i);
                                tableViewerViews.setChecked(element, true);
                            }
                        }
                        else {
                            for (int i=prevRow ; i<=selRow ; i++) {
                                Object element = tableViewerViews.getElementAt(i);
                                tableViewerViews.setChecked(element, true);
                            }                            
                        }
                    }   
                    else {
                        Object element = tableViewerViews.getElementAt(selRow);
                        if (tableViewerViews.getChecked(element)) {
                            prevSelection = tableViews.indexOf((TableItem)event.item);
                        }
                        else {
                            prevSelection = 0;
                        }
                    }
                }                  
            }
        });
    }

    protected void createButtons(Composite compositeViews) {
        Composite buttonBar = safrGuiToolkit.createComposite(
            compositeViews, SWT.NONE);
        buttonBar.setLayout(new FormLayout());
    
        FormData dataButtonBar = new FormData();
        dataButtonBar.left = new FormAttachment(tableViews, 10);
        dataButtonBar.right = new FormAttachment(100, 0);
        dataButtonBar.top = new FormAttachment(0, 10);
        dataButtonBar.bottom = new FormAttachment(100, 0);
        buttonBar.setLayoutData(dataButtonBar);
    
        buttonAdd = safrGuiToolkit.createButton(buttonBar, SWT.PUSH, "A&dd");
        buttonAdd.setData(SAFRLogger.USER, "Add Views");      
        
        FormData dataButtonAdd = new FormData();
        dataButtonAdd.left = new FormAttachment(0, 5);
        dataButtonAdd.width = 70;
        buttonAdd.setLayoutData(dataButtonAdd);

        buttonAdd.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                ApplicationMediator.getAppMediator().waitCursor();
                MetadataListDialog dialog = new MetadataListDialog(getSite()
                        .getShell(), TreeItemId.VIEWFOLDERCHILD, viewFolder,
                        UIUtilities.getCurrentEnvironmentID());
                dialog.create();
                dialog.open();
                List<SAFRQueryBean> returnList;
                if (dialog.getReturnCode() == Window.OK) {
                    returnList = dialog.getReturnList();
                    for (SAFRQueryBean b : returnList) {
                        ViewQueryBean bean = (ViewQueryBean) b;
                        try {
                            ViewFolderViewAssociation association = new ViewFolderViewAssociation(
                                viewFolder, bean.getId(), bean.getName(),bean.getRights());
                            viewFolder.addAssociatedView(association);
                        } catch (SAFRException se) {
                            UIUtilities.handleWEExceptions(se);
                        }
                    }
                    tableViewerViews.refresh();
                    if (returnList.size() > 0) {
                        setDirty(true);                                            
                    }
                }
                ApplicationMediator.getAppMediator().normalCursor();
            }
        });
        
        buttonRemove = safrGuiToolkit.createButton(buttonBar, SWT.PUSH, "Re&move");
        buttonRemove.setData(SAFRLogger.USER, "Remove Views");        
        FormData dataButtonRemove = new FormData();
        dataButtonRemove.left = new FormAttachment(0, 5);
        dataButtonRemove.top = new FormAttachment(buttonAdd, 5);
        dataButtonRemove.width = 70;
        buttonRemove.setLayoutData(dataButtonRemove);
        buttonRemove.setEnabled(false);
        buttonRemove.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                ApplicationMediator.getAppMediator().waitCursor();                
                int length = tableViewerViews.getCheckedElements().length;
                for (Object obj : tableViewerViews.getCheckedElements()) {
                    ViewFolderViewAssociation association = (ViewFolderViewAssociation) obj;
                    viewFolder.removeViewAssociation(association);
                    buttonRemove.setEnabled(false);
                }
                tableViewerViews.refresh();
                if (length > 0) {
                    setDirty(true);
                }
                ApplicationMediator.getAppMediator().normalCursor();
            }
        });
    }

    protected void createOpenEditorMenu() {
        tableViews.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                setPopupEnabled(true);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
            
        });
        
        tableViews.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent arg0) {
                ComponentAssociation ass = (ComponentAssociation) ((StructuredSelection)tableViewerViews.
                        getSelection()).getFirstElement();
                if (ass == null) {
                    setPopupEnabled(false);                    
                }
                else {
                    setPopupEnabled(true);
                }
            }

            public void focusLost(FocusEvent arg0) {
                setPopupEnabled(false);                                    
            }
            
        });
        
        // Code for Context menu
        // First we create a menu Manager
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViews);
        // Set the MenuManager
        tableViews.setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewerViews);        
        setPopupEnabled(false);
    }
	
    private void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.VIEWFOLDER);
        service.setViewFolder(enabled);
    }

    @Override
	public void setFocus() {
	    super.setFocus();
		textName.setFocus();
	}

	@Override
	public void doRefreshControls() {
		UIUtilities.checkNullText(textID, Integer.toString(viewFolder.getId()));
		if (viewFolder.getId() > 0) {
			UIUtilities.checkNullText(textName, viewFolder.getName());
			UIUtilities.checkNullText(textComments, viewFolder.getComment());
			labelCreatedValue.setText(viewFolder.getCreateBy() + " on " + viewFolder.getCreateTimeString());
			labelModifiedValue.setText(viewFolder.getModifyBy() + " on " + viewFolder.getModifyTimeString());
		}
		if (viewFolder.getName() == null) {
            form.setText("");		    
		} else {
            form.setText(viewFolder.getName());		    
		}
	}

	@Override
	public void refreshModel() {
		viewFolder.setName(textName.getText());
		viewFolder.setComment(textComments.getText());
	}

	@Override
	public String getModelName() {
		return "View Folder";
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		viewFolder.store();
        ApplicationMediator.getAppMediator().refreshNavigator();
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		viewFolder.validate();
		getMsgManager().removeAllMessages();
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == ViewFolder.Property.NAME) {
			return textName;
		} else if (property == ViewFolder.Property.COMMENT) {
			return textComments;
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.ViewFolder;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return viewFolder;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return textName.getText();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}

    /**
     * This method is to call refresh the metadata view when a component is
     * saved.
     */
    public void refreshMetadataView() {
        ApplicationMediator.getAppMediator().refreshMetadataView(getEditorCompType(), viewFolder.getId());
        ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.View, viewFolder.getId());
    }

    public ViewFolderViewAssociation getCurrentView() {
        return (ViewFolderViewAssociation)((StructuredSelection)tableViewerViews.getSelection()).getFirstElement();
    }
	
}
