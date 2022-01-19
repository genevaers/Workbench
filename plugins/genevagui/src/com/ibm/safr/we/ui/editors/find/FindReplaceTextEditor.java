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


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.services.ISourceProviderService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.ui.editors.OpenEditorPopupState;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class FindReplaceTextEditor extends SAFREditorPart {

	public static String ID = "SAFRWE.FindReplaceTextEditor";
    private static String headerNote = "Use this utility to find and replace keywords, component name or expressions in the logic text of one or more views.";

	private FindReplaceMediator mediator = null;

    // UI
	private ScrolledForm form;
	private FormToolkit toolkit;
	private SAFRGUIToolkit safrGuiToolkit;

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Find/Replace Logic Text");
        form.getBody().setSize(new Point(100,100));		

		Label labelHeaderNote = safrGuiToolkit.createLabel(form.getBody(),
				SWT.NONE, headerNote);
		labelHeaderNote.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		FormData dataLabelHeaderNote = new FormData();
		dataLabelHeaderNote.top = new FormAttachment(0, 0);
		dataLabelHeaderNote.left = new FormAttachment(0, 5);
		labelHeaderNote.setLayoutData(dataLabelHeaderNote);

		FindReplaceCriteria criteria = new FindReplaceCriteria();
        FindReplaceReplace replace = new FindReplaceReplace();
        FindReplaceResults results = new FindReplaceResults();
        
		mediator = new FindReplaceMediator(this,criteria,replace,results);
		
		criteria.setMediator(mediator);
		replace.setMediator(mediator);
		results.setMediator(mediator);
		
		mediator.create(form.getBody());
		
		// Used to load the context sensitive help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(form.getBody(),
				"com.ibm.safr.we.help.FindReplaceLTEditor");
		form.reflow(true);
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

    protected void setPopupEnabled(boolean enabled) {
        ISourceProviderService sourceProviderService = (ISourceProviderService) PlatformUI
        .getWorkbench().getService(ISourceProviderService.class);
        OpenEditorPopupState service = (OpenEditorPopupState) sourceProviderService
                .getSourceProvider(OpenEditorPopupState.FINDREP);
        service.setFindRep(enabled);
    }

    protected SAFRGUIToolkit getToolkit() {
        return safrGuiToolkit;
    }
    
	@Override
	public void doRefreshControls() throws SAFRException {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public ComponentType getEditorCompType() {
		return null;
	}

	@Override
	public String getModelName() {
		return null;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void refreshModel() {
	}

	@Override
	public void setFocus() {
		mediator.setFocusOnEnv();
	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
	}

	@Override
	public void validate() throws DAOException, SAFRException {
	}

	@Override
	public SAFRPersistentObject getModel() {
		return null;
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
	    Control con = mediator.getControlFromCriteria(property);
	    if (con == null) {
	        con = mediator.getControlFromReplace(property);
	        if (con == null) {
	            return  mediator.getControlFromReplace(property);
	        }
	        else {
	            return con;
	        }
	    }
	    else {
	        return con;
	    }
	}

	@Override
	public String getComponentNameForSaveAs() {
		return null;
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		return null;
	}

    public FindReplaceComponent getCurrentSelection() {
        return mediator.getCurrentSelection();
    }    
	
}
