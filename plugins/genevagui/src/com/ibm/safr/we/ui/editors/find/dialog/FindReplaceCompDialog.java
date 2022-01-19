package com.ibm.safr.we.ui.editors.find.dialog;

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


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class FindReplaceCompDialog extends TitleAreaDialog {

    protected FindReplaceCompMediator mediator;
	private SAFRGUIToolkit safrGUIToolkit;
	private Combo comboComponentType;
	private Composite compositeComponent;

	private int selectedComponentType = -1;
	protected ComponentType componentType;
	private Integer environmentId;

	public FindReplaceCompDialog(Shell parentShell, Integer environmentId) {
		super(parentShell);
		this.environmentId = environmentId;
		safrGUIToolkit = new SAFRGUIToolkit();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// To fix display issue when system font is increased.
		UIUtilities.setDialogBounds(getShell().getBounds().width, getShell()
				.getBounds().height, getShell());

		getShell().setText("Select component");
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	    
	    // setup mediator
	    FindReplaceCompComp compComp = new FindReplaceCompComp();
        FindReplaceCompLRField compLRField = new FindReplaceCompLRField();
	    mediator = new FindReplaceCompMediator(this, compComp, compLRField);
        compComp.setMediator(mediator);
        compLRField.setMediator(mediator);
	    
		createComposite(parent);
		
		mediator.create();
		
		return parent;
	}

	private void createComposite(Composite parent) {
		compositeComponent = safrGUIToolkit.createComposite(parent, SWT.NONE);
		compositeComponent.setLayout(new GridLayout(2, false));
		compositeComponent.setLayoutData(new GridData(GridData.FILL_BOTH));
				
		safrGUIToolkit.createLabel(compositeComponent, SWT.NONE, "Component &Type:");
		comboComponentType = safrGUIToolkit.createComboBox(compositeComponent,
				SWT.DROP_DOWN | SWT.READ_ONLY, "");
		GridData gridc1 = new GridData(GridData.FILL_HORIZONTAL);
		gridc1.horizontalSpan = 1;
		comboComponentType.setLayoutData(gridc1);

		int i = 0;
		comboComponentType.add("Lookup Path");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LookupPath);
		comboComponentType.add("Logical File");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LogicalFile);
		comboComponentType.add("Logical Record Field");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.LogicalRecord);
		comboComponentType.add("Physical File");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.PhysicalFile);
		comboComponentType.add("User-Exit Routine");
		comboComponentType.setData(String.valueOf(i++),
				ComponentType.UserExitRoutine);

		comboComponentType.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
                refreshComponent();
			}

			@Override
			public void focusGained(FocusEvent e) {
				setMessage("Select component type.",
						IMessageProvider.INFORMATION);
			}
		});
		
		comboComponentType.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                refreshComponent();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
		    
		});
	}
	
	private void refreshComponent() {
        try {
            if (comboComponentType.getSelectionIndex() != selectedComponentType) {
                componentType = (ComponentType) comboComponentType
                        .getData(String.valueOf(comboComponentType
                                .getSelectionIndex()));
                selectedComponentType = comboComponentType.getSelectionIndex();
                
                if (componentType.equals(ComponentType.LogicalRecord)) {
                    mediator.hideComponent();
                    
                    mediator.clearLRField();

                    getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

                    if (environmentId > 0l) {
                        mediator.populateLRList();;
                    }                            
                    mediator.showLRField();
                }
                else {
                    mediator.hideLRField();
                    
                    mediator.clearComponent();
                    
                    getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

                    if (environmentId > 0l) {
                        mediator.populateComponentList();
                    }
                    mediator.showComponent();
                }
            }
        } finally {
            getShell().setCursor(null);
        }	    
	}

    @Override
	protected void okPressed() {
		if ((mediator.getSelectedComponentName() == null) || 
		    (mediator.getSelectedComponentName().equals(""))) {
			setErrorMessage("Please select a component to continue.");
		} else {
			super.okPressed();
		}
	}
    
    public String getSelectedComponentName() {
        return mediator.getSelectedComponentName().substring(0, mediator.getSelectedComponentName().indexOf(" ["));
    }    

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", false);
	}

    protected Composite getComposite() {
        return compositeComponent;
    }

    protected boolean isComponentTypeSelected() {
        return (comboComponentType.getSelectionIndex() == -1);
    }

    protected ComponentType getComponentType() {
        return componentType;
    }

    protected int getEnvironmentId() {
        return environmentId;
    }
    
}
