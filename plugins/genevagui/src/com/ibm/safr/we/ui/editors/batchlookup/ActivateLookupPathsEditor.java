package com.ibm.safr.we.ui.editors.batchlookup;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.MetadataSearchCriteria;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.BatchComponent;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.ISearchablePart;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class represents the UI controls of the Batch Activate Lookup paths
 * screen.
 */
public class ActivateLookupPathsEditor extends SAFREditorPart implements
		ISearchablePart {

	public static String ID = "SAFRWE.ActivateLookupPathsEditor";
    
	private TableCombo comboEnvironment;
	private Environment currentEnvironment;
	private ScrolledForm form;
	private FormToolkit toolkit;
	public SAFRGUIToolkit safrGuiToolkit;
	private Section sectionEnvironment;
	private String selectedEnvironmentIndex = "";
	private TableComboViewer comboEnvironmentViewer;

	private List<EnvironmentQueryBean> envList;

    private MenuItem envOpenEditorItem = null;
	
    private ActivateLookupPathsMediator mediator;
    
	@Override
	public void createPartControl(Composite parent) {
	    mediator = new ActivateLookupPathsMediator();
	    mediator.setActivateLookupPathsEditor(this);
	    
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FormLayout());
		form.setText("Batch Activate Lookup Paths");
		createSectionEnvironment(form.getBody());
		createSectionLookupPaths(form.getBody());
		createSectionErrors(form.getBody());
		// Used to load the context sensitive help
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(form.getBody(),
						"com.ibm.safr.we.help.BatchActivateLookupPathsEditor");

	}

	private void createSectionLookupPaths(Composite body) {
        ActivateLookupPathsLookups lookups = new ActivateLookupPathsLookups(mediator, body);
        mediator.setActivateLookupPathsLookups(lookups);
        lookups.create();
    }

    private void createSectionErrors(Composite body) {
        ActivateLookupPathsError errors = new ActivateLookupPathsError(mediator, body);
        mediator.setActivateLookupPathsError(errors);
        errors.create();
    }

    // Section to display the list of environment combo box.
	private void createSectionEnvironment(Composite body) {
		sectionEnvironment = safrGuiToolkit.createSection(body,
				Section.TITLE_BAR, "Environments");

		FormData dataSectionEnv = new FormData();
		dataSectionEnv.top = new FormAttachment(0, 10);
		dataSectionEnv.left = new FormAttachment(0, 5);
		dataSectionEnv.right = new FormAttachment(100, -5);
		sectionEnvironment.setLayoutData(dataSectionEnv);

		Composite composite = safrGuiToolkit.createComposite(
				sectionEnvironment, SWT.NONE);
		composite.setLayout(new FormLayout());

		Label labelEnvironments = safrGuiToolkit.createLabel(composite,
				SWT.NONE, "&Environment: ");

		FormData dataLabelEnvironments = new FormData();
		dataLabelEnvironments.top = new FormAttachment(0, 10);
		dataLabelEnvironments.left = new FormAttachment(0, 5);
		labelEnvironments.setLayoutData(dataLabelEnvironments);
	
		comboEnvironmentViewer = safrGuiToolkit.createTableComboForComponents(
				composite, ComponentType.Environment);
		comboEnvironment = comboEnvironmentViewer.getTableCombo();
		comboEnvironment.setData(SAFRLogger.USER, "Environment");

		FormData dataComboEnvironments = new FormData();
		dataComboEnvironments.top = new FormAttachment(0, 10);
		dataComboEnvironments.left = new FormAttachment(labelEnvironments, 10);
		dataComboEnvironments.width = 422;
		comboEnvironment.setLayoutData(dataComboEnvironments);

		addEnvOpenEditorMenu();
		
		comboEnvironment.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				try {
					if (selectedEnvironmentIndex.equals(comboEnvironment
							.getText())) {
						return;
					}
					getSite().getShell().setCursor(
							getSite().getShell().getDisplay()
									.getSystemCursor(SWT.CURSOR_WAIT));
					EnvironmentQueryBean environmetQueryBean = (EnvironmentQueryBean) comboEnvironment
							.getTable().getSelection()[0].getData();
					mediator.clearErrors();

					try {
						currentEnvironment = SAFRApplication.getSAFRFactory()
								.getEnvironment(environmetQueryBean.getId());

					} catch (SAFRException e1) {
						UIUtilities.handleWEExceptions(e1,
						    "Error occurred while retrieving the enviornment.",null);
					}

					if (currentEnvironment != null) {
						mediator.loadData();
					}

				} finally {
					getSite().getShell().setCursor(null);
				}
				selectedEnvironmentIndex = comboEnvironment.getText();
			}

		});

		// load the data
		try {
			envList = SAFRQuery.queryEnvironmentsForBAL(SortType.SORT_BY_NAME);

		} catch (DAOException e1) {
			UIUtilities.handleWEExceptions(e1,
			    "Error occurred while retrieving all environments.",UIUtilities.titleStringDbException);
		}
		Integer count = 0;
		if (envList != null) {
			for (EnvironmentQueryBean enviromentQuerybean : envList) {
				comboEnvironment.setData(Integer.toString(count),
						enviromentQuerybean);
				count++;
			}
		}

		comboEnvironmentViewer.setInput(envList);

		sectionEnvironment.setClient(composite);

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

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		comboEnvironment.setFocus();

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

	/**
	 * Private inner class for displaying data in the Lookup paths table.
	 */
	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.LookupPath;

	}

	@Override
	public SAFRPersistentObject getModel() {
		// TODO Auto-generated method stub
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
	
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }

    public Control getSectionEnvironment() {
        return sectionEnvironment;
    }

    public EnvironmentQueryBean getCurrentEnvironmentBean() {
        return (EnvironmentQueryBean)((StructuredSelection) comboEnvironmentViewer
            .getSelection()).getFirstElement();
    }

    @Override
    public void searchComponent(MetadataSearchCriteria searchCriteria, String searchText) {
        mediator.searchComponent(searchCriteria, searchText);
    }

    @Override
    public ComponentType getComponentType() {
        return mediator.getComponentType();
    }

    public BatchComponent getLookupSelection() {
        return mediator.getLookupSelection();
    }

}
