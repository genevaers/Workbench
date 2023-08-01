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


import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

/**
 * 
 * This class creates the editor area and also the controls for Activate Views
 * Editor screen.
 */
public class ActivateViewsEditor extends SAFREditorPart implements
		ISearchablePart, IPartListener2 {

	public static String ID = "SAFRWE.ActivateViewsEditor";
	
	private ScrolledForm form;
	private FormToolkit toolkit;
	public SAFRGUIToolkit safrGuiToolkit;
	
    private ActivateViewsMediator mediator;

	public void createPartControl(Composite parent) {
	    
	    mediator = new ActivateViewsMediator();
	    mediator.setActivateViewsEditor(this);
	    
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Batch Activate Views");

		createSectionCriteria(form.getBody());
		createSectionViews(form.getBody());
		createSectionErrors(form.getBody());	
        getSite().getPage().addPartListener(this);
	}

    private void createSectionCriteria(Composite body) {
        
        ActivateViewsCriteria criteria = new ActivateViewsCriteria(mediator, body);
        mediator.setActivateViewsCriteria(criteria);
        criteria.create();
    }

    private void createSectionViews(Composite body) {

        ActivateViewsViews views = new ActivateViewsViews(mediator, body);
        mediator.setActivateViewsViews(views);
        views.create();
    }
    
	private void createSectionErrors(Composite body) {

	    ActivateViewsError errors = new ActivateViewsError(mediator, body);
	    mediator.setActivateViewsErrors(errors);
	    errors.create();
	}

	@Override
	public void setFocus() {
	    mediator.setFocusViews();
	}

	@Override
	public String getModelName() {
		return null;
	}

	@Override
	public void doRefreshControls() throws SAFRException {

	}

	@Override
	public void refreshModel() {

	}

	@Override
	public void storeModel() throws DAOException, SAFRException {

	}

	@Override
	public void validate() throws DAOException, SAFRException {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public ComponentType getEditorCompType() {
		return null;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return null;
	}


	@Override
	public String getComponentNameForSaveAs() {
		return null;
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}


	public void partActivated(IWorkbenchPartReference partRef) {
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	public void partClosed(IWorkbenchPartReference partRef) {
	}

	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	public void partOpened(IWorkbenchPartReference partRef) {
	}

	public void partHidden(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
        	Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					mediator.closeActivationLog();
				}
        	});
        }
	}

	public void partVisible(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
        	Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
				    mediator.showActivationLog();
				}
        	});
        }
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

    @Override
    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        mediator.searchComponent(searchCriteria, searchText);
    }

    @Override
    public ComponentType getComponentType() {
        return mediator.getComponentType();
    }

    public BatchComponent getSelectedView() {
        return mediator.getSelectedView();
    }

    public SAFRViewActivationException getViewActivationException() {
        return mediator.getViewActivationException();
    }

    public SAFRViewActivationException getViewActivationState(Integer id) {
        return mediator.getViewActivationState(id);
    }
   
}

	
