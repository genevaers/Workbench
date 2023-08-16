package com.ibm.safr.we.ui.editors.pf;

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


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

/**
 * This class creates the editor area and also the controls for a new/existing
 * physical file.
 * 
 */
public class PhysicalFileEditor extends SAFREditorPart implements
		ISearchablePart {

	public static String ID = "SAFRWE.PhysicalFileEditor";

    private PhysicalFileMediator mediator = new PhysicalFileMediator();

	private ScrolledForm form;
	FormToolkit toolkit;

	private CTabFolder tabFolder;
    private CTabItem tabGeneral;
    private CTabItem tabDataset;
    private Composite compDataset;
    private CTabItem tabDatabase;
    private Composite compDatabase;
	
	PhysicalFile physicalFile = null;
	PhysicalFileEditorInput physicalFileInput = null;
    SAFRGUIToolkit safrToolkit;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		physicalFileInput = (PhysicalFileEditorInput) getEditorInput();
		physicalFile = physicalFileInput.getPhysicalFile();
	}

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		if(physicalFile.getId() > 0) {
			retval = isSaveAsAllowed(Permissions.CreatePhysicalFile);
		}
		return retval;
	}

	@Override
	public void createPartControl(Composite parent) {

        mediator.setPhysicalFileEditor(this);
	    
		toolkit = new FormToolkit(parent.getDisplay());
        safrToolkit = new SAFRGUIToolkit(toolkit);
        safrToolkit.setReadOnly(physicalFileInput.getEditRights() == EditRights.Read);
        
		form = toolkit.createScrolledForm(parent);
        form.getBody().setLayout(new FormLayout());
        
        createTabFolder(form.getBody());
                
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

    protected PhysicalFileEditorInput getPhysicalFileInput() {
        return physicalFileInput;
    }
	
    protected void createTabFolder(Composite body) {
        
        tabFolder = new CTabFolder(body, SWT.TOP);
        tabFolder.setLayout(new FormLayout());
        toolkit.adapt(tabFolder, true, true);

        FormData folderData = new FormData();
        folderData.left = new FormAttachment(0, 20);
        folderData.right = new FormAttachment(95, 0);
        folderData.top = new FormAttachment(0, 0);
        folderData.bottom = new FormAttachment(95, 0);
        tabFolder.setLayoutData(folderData);
        
        toolkit.adapt(tabFolder, true, true);
        PhysicalFileGeneralEditor genEditor = new PhysicalFileGeneralEditor(mediator, tabFolder, physicalFile);
        mediator.setPhysicalFileGeneralEditor(genEditor);
        Composite compositeGeneral = genEditor.create();
        tabGeneral = new CTabItem(tabFolder, SWT.NONE);
        tabGeneral.setText("&General");
        tabGeneral.setControl(compositeGeneral);        
        tabFolder.setSelection(tabGeneral);                
        
        PhysicalFileDatabaseEditor databaseEditor = new PhysicalFileDatabaseEditor(mediator, tabFolder, physicalFile);
        mediator.setPhysicalFileDatabaseEditor(databaseEditor);
        compDatabase = databaseEditor.create();  

        PhysicalFileDatasetEditor datasetEditor = new PhysicalFileDatasetEditor(mediator, tabFolder, physicalFile);
        mediator.setPhysicalFileDatasetEditor(datasetEditor);
        compDataset = datasetEditor.create();    
     
        doRefreshControls();
    }

    public void enableDatasetTab() {
        if (tabDatabase !=null && !tabDatabase.isDisposed()) {
            tabDatabase.dispose();
            tabDatabase = null;
        }
        if (tabDataset == null || tabDataset.isDisposed()) {
            createDatasetTab();
        }
        
    }    

    public void enableDatabaseTab() {
        if (tabDataset !=null && !tabDataset.isDisposed()) {
            tabDataset.dispose();
            tabDataset = null;
        }
        if (tabDatabase == null || tabDatabase.isDisposed()) {
            createDatabaseTab();
        }
        
    }    
    
    protected void createDatabaseTab() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {        
                tabDatabase = new CTabItem(tabFolder, SWT.NONE, 1);
                tabDatabase.setText("&Database Info");
                tabDatabase.setControl(compDatabase);
            }
        });
    }

    protected void createDatasetTab() {
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {        
                tabDataset = new CTabItem(tabFolder, SWT.NONE, 1);
                tabDataset.setText("&File Info");
                tabDataset.setControl(compDataset);
            }
        });
    }
    
    
	public void setFocus() {
		super.setFocus();
		tabFolder.setSelection(tabGeneral);
		mediator.setNameFocus(); 
	}

	@Override
	public void refreshModel() {
	}

	@Override
	public String getModelName() {
		return "Physical File";
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		physicalFile.store();
		PhysicalFileEditorInput pfInput = new PhysicalFileEditorInput(
				physicalFile, physicalFileInput.getEditRights());
		this.setInput(pfInput);
		firePropertyChange(IEditorPart.PROP_INPUT);
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		physicalFile.validate();
		getMsgManager().removeAllMessages();
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
	    return mediator.getControlFromProperty(property);
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.PhysicalFile;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return physicalFile;
	}

	public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
	    mediator.searchComponent(searchCriteria, searchText);
	}

	public ComponentType getComponentType() {	    
		return mediator.getComponentType();
	}
	
	public ComponentAssociation getCurrentLF() {
	    return mediator.getCurrentLF(); 
	}

	@Override
	public String getComponentNameForSaveAs() {
		return mediator.getComponentNameForSaveAs();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(com.ibm.safr.we.model.PhysicalFile.Property.NAME)) {
			return true;
		}
		return false;
	}

    @Override
    public void doRefreshControls() throws SAFRException {
        mediator.doRefreshControls();
        if (physicalFile.getName() == null) {
            form.setText("");            
        } else {
            form.setText(physicalFile.getName());
        }
    }

}
