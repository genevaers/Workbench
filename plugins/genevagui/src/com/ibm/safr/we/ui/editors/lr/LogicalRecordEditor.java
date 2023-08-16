package com.ibm.safr.we.ui.editors.lr;

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


import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRValidationToken;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

public class LogicalRecordEditor extends SAFREditorPart implements
		IPartListener2, ISearchablePart {

	public static String ID = "SAFRWE.LogicalRecordEditor";
		
	private FormToolkit toolkit;
    private ScrolledForm form;
	private CTabFolder tabFolder;
	private CTabItem tabLRProperties;
	private CTabItem tabLRFields;
	private CTabItem tabAssociatedLF;

	LogicalRecord logicalRecord = null;
	LogicalRecordEditorInput logicalRecordInput = null;
    private SAFRGUIToolkit safrGuiToolkit = null;
	private IToolBarManager formToolbar;
	private Boolean forSaveAs = false;
	private Boolean warningsShown = false;

    private LogicalRecordMediator mediator = new LogicalRecordMediator();
    
    private boolean validationWarning;    
    
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(logicalRecord.getId() > 0) {
			retval = isSaveAsAllowed(Permissions.CreateLogicalRecord);
		}
		return retval;
	}

	@Override
	public void refreshModelForSaveAs() {
		// refreshModel() works differently for Save and Save As.
		forSaveAs = true;
		refreshModel();
		forSaveAs = false;
	}

	@Override
	public void createPartControl(Composite parent) {
        mediator.setLogicalRecordEditor(this);	    
		try {
			logicalRecordInput = (LogicalRecordEditorInput) getEditorInput();
			logicalRecord = logicalRecordInput.getLogicalRecord();
			toolkit = new FormToolkit(parent.getDisplay());
			safrGuiToolkit = new SAFRGUIToolkit(toolkit);
			getSafrGuiToolkit().setReadOnly(logicalRecordInput.getEditRights() == EditRights.Read);
			form = toolkit.createScrolledForm(parent);
			form.getBody().setLayout(new FillLayout());
			createTabFolder(form.getBody());
			
	        // Code to manage the form toolbar.
	        formToolbar = (ToolBarManager) form.getToolBarManager();
	        IMenuService ms = (IMenuService) mediator.getSite().getService(IMenuService.class);
	        ms.populateContributionManager((ContributionManager) formToolbar,
	            "toolbar:SAFRWE.com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor");
	        formToolbar.update(true);
	        final ToolBar toolbar = ((ToolBarManager) formToolbar).getControl();
	        toolbar.setData(SAFRLogger.USER, "Toolbar");
	        toolbar.addMouseListener(new MouseAdapter() {

	            public void mouseDown(MouseEvent e) {
	                ToolItem item = toolbar.getItem(new Point(e.x, e.y));
	                if (item == null) {
	                    toolbar.setData(SAFRLogger.USER, "Toolbar");
	                } else {
	                    toolbar.setData(SAFRLogger.USER, "Toolbar " + item.getToolTipText());
	                }
	            }

	        });
			
			refreshControls();
			
			if (logicalRecord.getId() > 0) {
				// If editing open on LR field tab
				tabFolder.setSelection(1);
			}
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se);
		}
		setDirty(false);
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
		getSite().getPage().addPartListener(this);
	}

	/**
	 * This function creates a tab folder containing 3 tabs - one for each
	 * section : 'LR Properties', 'LR Fields' and 'Associated Logical Files'
	 * 
	 * @param body
	 *            the composite on which the tab folder is to be created.
	 * @throws SAFRException
	 */
	private void createTabFolder(Composite body) throws SAFRException {
		tabFolder = new CTabFolder(body, SWT.TOP);
		tabFolder.setLayout(UIUtilities.createTableLayout(1, false));
		toolkit.adapt(tabFolder, true, true);

		createCompositeLRGeneral();
		tabLRProperties = new CTabItem(tabFolder, SWT.NONE);
		tabLRProperties.setText("LR &Properties");
		tabLRProperties.setControl(mediator.getCompositeGeneral());
		tabFolder.setSelection(tabLRProperties);
		createCompositeLRFields();
		tabLRFields = new CTabItem(tabFolder, SWT.NONE);
		tabLRFields.setText("LR F&ields");
		tabLRFields.setControl(mediator.getCompositeLRFields());
		createCompositeAssociatedLF();
		tabAssociatedLF = new CTabItem(tabFolder, SWT.NONE);
		tabAssociatedLF.setText("Associated &Logical Files");
		tabAssociatedLF.setControl(mediator.getCompositeLogFile());
		tabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (e.item == tabLRFields) {
				    mediator.setLRFieldsFocus();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	/**
	 * This function creates the composite and its child elements for displaying
	 * the LR Properties section
	 * 
	 * @throws SAFRException
	 */
	private void createCompositeLRGeneral() throws SAFRException {
        LogicalRecordGeneralEditor generalEditor = new LogicalRecordGeneralEditor(mediator, tabFolder, logicalRecord);
        mediator.setLogicalRecordGeneralEditor(generalEditor);                     
        generalEditor.create();       	    
	}

	/**
	 * This function creates the composite and its child elements for displaying
	 * the LR Fields section.
	 */
	private void createCompositeLRFields() {
        LogicalRecordFieldEditor fieldEditor = new LogicalRecordFieldEditor(mediator, tabFolder, logicalRecord);
        mediator.setLogicalRecordFieldEditor(fieldEditor);                     
        fieldEditor.create();	    
	}

	/**
	 * This function creates the composite and its child elements for displaying
	 * the Associated Logical Files section.
	 */
	private void createCompositeAssociatedLF() {
	    
        LogicalRecordLogFileEditor logFileEditor = new LogicalRecordLogFileEditor(mediator, tabFolder, logicalRecord);
        mediator.setLogicalRecordLogFileEditor(logFileEditor);                     
        logFileEditor.create();       
	}

    public ComponentAssociation getCurrentLF() {
        return mediator.getCurrentLF();
    }
    
	@Override
	public void setFocus() {
		super.setFocus();
		mediator.setNameFocus();
	}

	@Override
	public String getModelName() {
		return "Logical Record";
	}

	@Override
	public void doRefreshControls() throws SAFRException {
	    mediator.refreshControlGeneral();
	    
		String status;
		if (logicalRecord.isActive()) {
			status = "[Active] ";
		} else {
			status = "[Inactive] ";
		}
		
		if (logicalRecord.getId() > 0) {
			mediator.refreshLRFields();
		}
		if (logicalRecord.getName() == null) {
	        form.setText(status);		    
		} else {
	        form.setText(status + logicalRecord.getName());		    
		}
	}

	@Override
	public void refreshModel() {
	    mediator.refreshModelGeneral();
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
	    // prompt for activate
	    if (!validationWarning && !logicalRecord.isActive()) {
	        MessageDialog dialog = new MessageDialog(
	            null, "Activate the Logical Record", null, 
	            "The logical record has passed validation without warnings do you wish to activate it?",
	            MessageDialog.QUESTION, new String[] {"Yes", "No"}, 0);
	        if (dialog.open() == 0) {
	            logicalRecord.setActive(true);
	        }	        
	    }
		logicalRecord.store();
        ApplicationMediator.getAppMediator().refreshDeactiveLookups(logicalRecord.getDeactivatedLookups());     
        ApplicationMediator.getAppMediator().refreshDeactiveViews(logicalRecord.getDeactivatedViews());		
	}

	public void validate() throws DAOException, SAFRException {
		boolean done = false;
		boolean flag = false;
		String messageTitle = "";
		SAFRValidationToken token = null;
		validationWarning = false;
		while (!done) {
			try {
				logicalRecord.validate(token);
				if ( token == null || 
				    (token != null && 
				     !token.getValidationFailureType().equals(SAFRValidationType.WARNING) && 
				     warningsShown == false) ) {
					getMsgManager().removeAllMessages();
				}
				warningsShown = false;
				done = true;
			} catch (SAFRValidationException sve) {
				SAFRValidationType failureType = sve.getSafrValidationToken().getValidationFailureType();
				
				if (failureType == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR) {
					// process deleted field dependency errors
					DependencyMessageDialog.openDependencyDialog(getSite().getShell(),"Field dependencies",
					    "These LR Fields cannot be deleted because they are used in a Lookup Path or View, so they have been restored.",
						sve.getMessageString(),MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL },0);
					mediator.refreshLRFields();
					throw new SAFRValidationException(); 
				} else if (failureType == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR) {
					// process deleted associated logical files dependency errors
					DependencyMessageDialog.openDependencyDialog(getSite().getShell(),"Logical Record/Logical File Dependencies",
					    "A new Lookup Path and/or View dependency has been created on a deleted Logical File association since the last warning issued about this deletion. These deleted LF associations have now been restored. Please review the LR and save again.",
						sve.getMessageString(),MessageDialog.ERROR,new String[] { IDialogConstants.OK_LABEL },0);
					mediator.refreshLogFiles();
					throw new SAFRValidationException(); // this will stop the
					// save process
				} else if (failureType == SAFRValidationType.ERROR) {
					// process LR and LR field errors. Throw back to the calling function.
					// Errors are handled in doSave.
					throw sve;
				} else if (failureType == SAFRValidationType.WARNING) {
					// process LR and LR field warnings. Ask for user confirmation to continue.
					// If the user confirms, Validate again to continue from where it was left.
				    validationWarning = true;
					int confirm = 0;
					decorateEditor(sve);
					confirm = DependencyMessageDialog.openDependencyDialog(getSite().getShell(),"Validations",
					    "This Logical Record will be saved as 'Inactive' until these errors are fixed." + SAFRUtilities.LINEBREAK,
					    sve.getMessageString(),MessageDialog.WARNING, new String[] {"&OK", "&Cancel" }, 0);
					if (confirm == 1) {
						// Stop save.
						throw new SAFRValidationException();
					} else {
						// get token from Exception to pass to validate again.
						token = sve.getSafrValidationToken();
						logicalRecord.setActive(false);
					}
				} else if (failureType == SAFRValidationType.DEPENDENCY_LR_WARNING) {
					// process LR dependencies. Ask user for confirmation to
					// continue.
					// If the user confirms, continue to SAVE else stop the
					// SAVE process.
					int confirm1 = 0;
					if (flag == false) {
						messageTitle = "The following Lookup Paths and/or Views are dependent on this logical record. If they are Active, saving this Logical Record will make these components inactive.";
					} else {
						messageTitle = "A new Lookup Path or View dependency has been created on this Logical Record since the last warning was issued. Saving this Logical Record will make this component(s) inactive too.";
					}
					confirm1 = DependencyMessageDialog.openDependencyDialog(
							getSite().getShell(),
							"Logical Record dependencies", messageTitle, 
							sve.getMessageString(), MessageDialog.WARNING,
							new String[] { "&OK", "&Cancel" }, 0);
					flag = true;
					// get token from Exception to pass to validate again.
					token = sve.getSafrValidationToken();
					if (confirm1 == 1) {
						throw new SAFRValidationException();
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (toolkit != null) {
			toolkit.dispose();
		}
		getSite().getPage().removePartListener(this);
		LogicalRecordFieldEditor lfe = mediator.getLogicalRecordFieldEditor();
		if(lfe != null) {
			lfe.dispose();
		}
	}

	protected Control getControlFromProperty(Object property) {	    
		return mediator.getControlFromProperty(property);
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.LogicalRecord;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return logicalRecord;
	}

	private void refreshEditRights() {
		SourceProvider sourceProvider = UIUtilities.getSourceProvider();
		if (logicalRecordInput.getEditRights() == EditRights.Read) {
			sourceProvider.setAllowEdit(false);
		} else {
			sourceProvider.setAllowEdit(true);
		}
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false).equals(this)) {
			refreshEditRights();
			// CQ 7615 :Jaydeep 13th April 10 ,implemented source provider.
			mediator.refreshToolbar();
			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setEditorFocusLR(true);
			sourceProvider.setEditorFocusLookupPath(false);
			sourceProvider.setEditorFocusView(false);
			sourceProvider.setEditorFocusEnv(false);
			mediator.setLRFieldsFocus();
		}
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	public void partClosed(IWorkbenchPartReference partRef) {
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (getSite().getWorkbenchWindow().getActivePage()
				.getEditorReferences() != null) {
			IEditorReference[] openEditors = getSite().getWorkbenchWindow()
					.getActivePage().getEditorReferences();

			if (partRef.getPart(false).equals(this)) {
				SourceProvider sourceProvider = UIUtilities.getSourceProvider();
				sourceProvider.setEditorFocusLR(false);
				if (openEditors.length == 0) {
					sourceProvider.setEditorFocusView(false);
					sourceProvider.setEditorFocusLookupPath(false);
					sourceProvider.setEditorFocusEnv(false);
				}
			}
		}

	}

	public void partHidden(IWorkbenchPartReference partRef) {
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	public void partOpened(IWorkbenchPartReference partRef) {
	}

	public void partVisible(IWorkbenchPartReference partRef) {
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
	    mediator.searchLFAssociation(searchCriteria, searchText);	    
	}

	@Override
	public String getComponentNameForSaveAs() {
		return mediator.getComponentNameForSaveAs();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(
				com.ibm.safr.we.model.LogicalRecord.Property.LR_NAME)) {
			return true;
		}
		return false;
	}

    public SAFRGUIToolkit getSafrGuiToolkit() {
        return safrGuiToolkit;
    }
    public FormToolkit getToolkit() {
        return toolkit;
    }

    public void setTabLRFields() {
        tabFolder.setSelection(tabLRFields);        
    }

    public void addRow() {
        mediator.addRow();        
    }

    public void editCalculateAllRows() {
        mediator.editCalculateAllRows();
    }

    public void editCalculateFromHighlightedRows() {
        mediator.editCalculateFromHighlightedRows();
    }

    public void editCalculateOnlyHighlightedRows() {
        mediator.editCalculateOnlyHighlightedRows();
    }

    public void editCalculateWithinHighlightedRow() {
        mediator.editCalculateWithinHighlightedRow();
    }
    
    public void editCalculateRedefines() {
        mediator.editCalculateRedefines();      
    }
    
    public void editCopyRow() {
        mediator.editCopyRow();
    }

    public void editFindField() {
        mediator.editFindField();
    }

    public void editInsertFieldBefore() {
        mediator.editInsertFieldBefore();
    }

    public void editInsertFieldAfter() {
        mediator.editInsertFieldAfter();
    }
    
    public void editPasteRowAboveSelected() {
        mediator.editPasteRowAboveSelected();
    }
    
    public void editPasteRowBelowSelected() {
        mediator.editPasteRowBelowSelected();
    }

    public void editRemoveRow() {
        mediator.editRemoveRow();
    }

    public void moveUpRow() {
        mediator.moveUpRow();
    }

    public void moveDownRow() {
        mediator.moveDownRow();
    }

    public void selectRowAtField(Integer id) {
        mediator.selectRowAtField(id);      
    }

    public void expandAll() {
        mediator.expandAll();        
    }

    public void collapseAll() {
        mediator.collapseAll();        
    }

    public LRField getCurrentLRField() {
        return mediator.getCurrentLRField();
    }

    public boolean isSaveAs() {
        return forSaveAs;
    }

    @Override
    public ComponentType getComponentType() {
        return mediator.getComponentType();
    }
}
