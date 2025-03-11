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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IColorProvider;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.menus.IMenuService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.ui.dialogs.SaveAsDialog;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.ViewFolderTableLabelProvider;
import com.ibm.safr.we.utilities.SAFRLogger;

public class UserEditor extends SAFREditorPart {
	public static String ID = "SAFRWE.UserEditor";
	private Text userId;
	private Text password;
	private Button chckSysAdmin;
	private Text textComments;
	private Label labelCreatedValue;
	private Label labelModifiedValue;
	private String defaultCreated = "-";
	private String defaultModified = "-";

	private Text firstName;
	private Text middleInitial;
	private Text lastName;
	private Text emailAddress;
	private Combo logLevel;
	private Text maxCompilationError;

	private TableCombo comboDefaultEnvironment;
	private TableCombo comboDefaultGroup;
	private TableCombo comboDefaultViewFolder;

	ScrolledForm form;
	FormToolkit toolkit;

	Section sectionLogin;
	Section sectionDefaults;
	Section sectionDetails;

	UserEditorInput userInput = null;
	User user = null;
	SAFRGUIToolkit safrToolkit;

	private static final int MAXUSERID = 8;
	private static final int MAXPASSWORD = 20;
	private static final int MAXNAME = 50;
	private static final int MAXMIDDLEINTIAL = 1;
	private static final int MAXCOMPILATIONERROR = 4;
	private static final int MAXCOMMENT = 200;
	private static final int MAXEMAIL = 50;
	private static final String DEFAULTCOMPILATIONERROR = "20";

	Environment previousEnv = null;
	Group previousGroup = null;
	String copyName = null;
	private TableComboViewer comboEnvironmentViewer;
	private List<EnvironmentQueryBean> envList;
	private TableComboViewer comboDefaultGroupViewer;
	private TableComboViewer comboDefaultViewFolderViewer;

	protected EnvironmentalQueryBean prevDefVF;
	private String selectedLogLevel;

    private MenuItem envOpenEditorItem = null;	
    private MenuItem grpOpenEditorItem = null;	
    private MenuItem folOpenEditorItem = null;	
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		userInput = (UserEditorInput) getEditorInput();
		user = userInput.getUser();
	}

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
		//if not dealing with a new component 
		//check with parent based on permissions
		if(user.getUserid() != "") {
			retval = SAFRApplication.getUserSession().isSystemAdministrator() && saveAsEnabled;
		}
		return retval;
	}

	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(UIUtilities.createTableLayout(1, false));
		safrToolkit = new SAFRGUIToolkit(toolkit);

		createLogin(form.getBody());
		createDetails(form.getBody());
		createDefault(form.getBody());

		refreshControls();
		setDirty(false);
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
	}

	private void createLogin(Composite parent) {
		sectionLogin = safrToolkit.createSection(parent, Section.TITLE_BAR,
				"Login");
		TableWrapData newData = new TableWrapData(TableWrapData.FILL_GRAB);
		sectionLogin.setLayoutData(newData);

		Composite login = safrToolkit.createComposite(sectionLogin, SWT.NONE);
		login.setLayout(UIUtilities.createTableLayout(4, false));

		toolkit.createLabel(login, "&User ID:");
		userId = safrToolkit.createUpperCaseTextBox(login, SWT.NONE);
		userId.setLayoutData(UIUtilities.textTableData(1));
		userId.setTextLimit(MAXUSERID);
		userId.addModifyListener(this);
		
		Label filler1 = safrToolkit.createLabel(login, SWT.NONE, "");
		filler1.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(login, SWT.NONE, "&Password:");
		password = safrToolkit.createTextBox(login, SWT.NONE);
		password.setData(SAFRLogger.USER, "Password");																								
		password.setEchoChar('*');
		password.setTextLimit(MAXPASSWORD);
		password.setLayoutData(UIUtilities.textTableData(1));
		password.addModifyListener(this);

		Label filler2 = safrToolkit.createLabel(login, SWT.NONE, "");
		filler2.setLayoutData(UIUtilities.textTableData(2));
		safrToolkit.createLabel(login, SWT.NONE, "");

		chckSysAdmin = safrToolkit.createButton(login, SWT.CHECK,
				"&System Administrator");
		chckSysAdmin.setData(SAFRLogger.USER, "System Administrator");																								
		chckSysAdmin.setLayoutData(UIUtilities.textTableData(1));

		chckSysAdmin.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				EnvironmentQueryBean defaultEnv = null;
				if (comboDefaultEnvironment.getTable().getSelection().length > 0) {
					defaultEnv = (EnvironmentQueryBean) comboDefaultEnvironment
							.getTable().getSelection()[0].getData();
				}
				String defaultEnvString = "";
				if (defaultEnv != null) {
					defaultEnvString = defaultEnv.getName();
				}

				String defaultVFString = "";
				if (chckSysAdmin.getSelection()) {
					ViewFolderQueryBean defaultVF = null;
					if (comboDefaultViewFolder.getTable().getSelection().length > 0) {
						defaultVF = (ViewFolderQueryBean) comboDefaultViewFolder
								.getTable().getSelection()[0].getData();
					}
					if (defaultVF != null) {
						defaultVFString = defaultVF.getName();
					}

				}

				// save the user as admin or general user depending on checkbox
				// selection
				user.setSystemAdmin(chckSysAdmin.getSelection());
				// reset the default combos
				refreshDefaults();

				// reselect previous selections if they are present in the newly
				// populated combos for environment and also for view folder in
				// case of admin
				int envIndex = comboDefaultEnvironment
						.indexOf(defaultEnvString);
				if (envIndex >= 0) {
					comboDefaultEnvironment.select(envIndex);
				}
				try {
					if (defaultEnv != null) {
						enableDefaultsOnEnvSelection(defaultEnv.getId(), 0);
					}
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Error in getting defaults on environment selection.","");
				}

				if (chckSysAdmin.getSelection()) {
					if (defaultEnv != null) {
						int vfIndex = comboDefaultViewFolder
								.indexOf(defaultVFString);
						if (vfIndex >= 0) {
							comboDefaultViewFolder.select(vfIndex);
						}
					}
				}

			}
		});
		Label filler4 = safrToolkit.createLabel(login, SWT.NONE, "");
		filler4.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(login, SWT.NONE,"C&omments:");

		textComments = safrToolkit.createCommentsTextBox(login);
		textComments.setData(SAFRLogger.USER, "Comments");																										
		textComments.setLayoutData(UIUtilities.multiLineTextData(1, 1,
				textComments.getLineHeight() * 3));
		textComments.setTextLimit(MAXCOMMENT);
		textComments.addModifyListener(this);

		Label filler5 = safrToolkit.createLabel(login, SWT.NONE, "");
		filler5.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(login, SWT.NONE,"Created:");

		labelCreatedValue = safrToolkit.createLabel(login, SWT.NONE,
				defaultCreated);
		labelCreatedValue.setLayoutData(UIUtilities.textTableData(1));

		Label filler6 = safrToolkit.createLabel(login, SWT.NONE, "");
		filler6.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(login, SWT.NONE,"Last Modified:");

		labelModifiedValue = safrToolkit.createLabel(login, SWT.NONE,
				defaultModified);
		labelModifiedValue.setLayoutData(UIUtilities.textTableData(1));

		Label filler7 = safrToolkit.createLabel(login, SWT.NONE, "");
		filler7.setLayoutData(UIUtilities.textTableData(2));

		sectionLogin.setClient(login);

	}

	private void createDetails(Composite parent) {
		sectionDetails = safrToolkit.createSection(parent, Section.TITLE_BAR,
				"Details");
		TableWrapData newData = new TableWrapData(TableWrapData.FILL_GRAB);
		sectionDetails.setLayoutData(newData);

		Composite details = safrToolkit.createComposite(sectionDetails,
				SWT.NONE);
		details.setLayout(UIUtilities.createTableLayout(4, false));

		safrToolkit.createLabel(details, SWT.NONE,"First &Name:");
		firstName = safrToolkit.createTextBox(details, SWT.NONE);
		firstName.setData(SAFRLogger.USER, "First Name");                                                                                                        		
		firstName.setTextLimit(MAXNAME);
		firstName.setLayoutData(UIUtilities.textTableData(1));
		firstName.addModifyListener(this);

		Label filler1 = safrToolkit.createLabel(details, SWT.NONE, "");
		filler1.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(details, SWT.NONE,"&Middle Initial:");
		middleInitial = safrToolkit.createTextBox(details, SWT.NONE);
		middleInitial.setData(SAFRLogger.USER, "Middle Initial");                                                                                                             
		middleInitial.setTextLimit(MAXMIDDLEINTIAL);
		middleInitial.setLayoutData(UIUtilities.textTableData(1));
		middleInitial.addModifyListener(this);

		Label filler2 = safrToolkit.createLabel(details, SWT.NONE, "");
		filler2.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(details, SWT.NONE,"&Last Name:");
		lastName = safrToolkit.createTextBox(details, SWT.NONE);
		lastName.setData(SAFRLogger.USER, "Last Name");                                                                                                             		
		lastName.setLayoutData(UIUtilities.textTableData(1));
		lastName.setTextLimit(MAXNAME);
		lastName.addModifyListener(this);

		Label filler3 = safrToolkit.createLabel(details, SWT.NONE, "");
		filler3.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(details, SWT.NONE,"&Email Address:");
		emailAddress = toolkit.createText(details, "");
		emailAddress.setData(SAFRLogger.USER, "Email Address");                                                                                                                   		
		emailAddress.setLayoutData(UIUtilities.textTableData(1));
		emailAddress.setTextLimit(MAXEMAIL);
		emailAddress.addModifyListener(this);

		Label fill1 = safrToolkit.createLabel(details, SWT.NONE, "");
		fill1.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(details, SWT.NONE,"Log Le&vel:");
		logLevel = safrToolkit.createComboBox(details, SWT.READ_ONLY, "");
		logLevel.setData(SAFRLogger.USER, "Log Level");                                                                                                                           		
		logLevel.add("0 - Only Errors");
		logLevel.add("1 - High Level");
		logLevel.add("2 - Detail Level");
		logLevel.select(0);
		logLevel.setLayoutData(UIUtilities.textTableData(1));
		selectedLogLevel = logLevel.getText();

		logLevel.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				super.focusLost(e);
				if (!(logLevel.getText().equals(selectedLogLevel))) {
					setDirty(true);
					selectedLogLevel = logLevel.getText();
				}

			}

		});

		Label filler4 = safrToolkit.createLabel(details, SWT.NONE, "");
		filler4.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(details, SWT.NONE,"Ma&x Compilation Error:");
		maxCompilationError = safrToolkit.createIntegerTextBox(details,
				SWT.NONE, false, DEFAULTCOMPILATIONERROR);
		maxCompilationError.setData(SAFRLogger.USER, "Max Compilation Error");                                                                                                                                   		
		maxCompilationError.setTextLimit(MAXCOMPILATIONERROR);
		maxCompilationError.setLayoutData(UIUtilities.textTableData(1));
		maxCompilationError.addModifyListener(this);
		maxCompilationError.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {

				super.focusGained(e);

			}

			@Override
			public void focusLost(FocusEvent e) {

				super.focusLost(e);
				if (maxCompilationError.getText() == "") {
					maxCompilationError.setText(DEFAULTCOMPILATIONERROR);
				}
			}

		});

		Label filler5 = toolkit.createLabel(details, "");
		filler5.setLayoutData(UIUtilities.textTableData(2));

		sectionDetails.setClient(details);

	}

	private void createDefault(Composite parent) {
		sectionDefaults = safrToolkit.createSection(parent, Section.TITLE_BAR,
				"Defaults");
		sectionDefaults.setText("Defaults");
		TableWrapData newData = new TableWrapData(TableWrapData.FILL_GRAB);
		sectionDefaults.setLayoutData(newData);

		Composite defaults = safrToolkit.createComposite(sectionDefaults,
				SWT.NONE);
		defaults.setLayout(UIUtilities.createTableLayout(4, false));

		safrToolkit.createLabel(defaults, SWT.NONE,
				"Default Env&ironment:");

		comboEnvironmentViewer = safrToolkit.createTableComboForComponents(
				defaults, ComponentType.Environment);

		comboDefaultEnvironment = comboEnvironmentViewer.getTableCombo();
		addEnvOpenEditorMenu();
		comboDefaultEnvironment.setData(SAFRLogger.USER, "Default Environment");                                                                                                                                        		
		comboDefaultEnvironment.setLayoutData(UIUtilities.textTableData(1));
		comboDefaultEnvironment.setEnabled(false);
		comboDefaultEnvironment.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {

					if (previousEnv == null) {
						user.setDefaultEnvironment(null);
					}

					EnvironmentQueryBean selectedEnvBean = null;
					if (comboDefaultEnvironment.getTable().getSelection().length > 0) {
						selectedEnvBean = (EnvironmentQueryBean) comboDefaultEnvironment
								.getTable().getSelection()[0].getData();
					}

					Environment selectedEnv = null;
					if ((selectedEnvBean != null)
							&& (!(selectedEnvBean.getId() == 0))) {
						selectedEnv = SAFRApplication.getSAFRFactory()
								.getEnvironment(selectedEnvBean.getId());
					}
					if (!UIUtilities.isEqualSAFRComponent(previousEnv,
							selectedEnv)) {

						Integer selectedEnvId = 0;
						if (selectedEnvBean != null) {
							selectedEnvId = selectedEnvBean.getId();
						}
						// group id is needed only for populating View
						// Folders,
						// and group id is not applicable if user is a
						// sysAdmin
						enableDefaultsOnEnvSelection(selectedEnvId, 0);
						comboDefaultGroup.select(-1);
						comboDefaultViewFolder.select(-1);
						user.setDefaultEnvironment(selectedEnv);
						setDirty(true);
					}
					previousEnv = user.getDefaultEnvironment();

				} catch (SAFRNotFoundException sne) {
					UIUtilities.handleWEExceptions(sne, null,
							UIUtilities.titleStringNotFoundException);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,"Error in getting defaults on environment selection.",null);
				}
			}

		});

		Label fillr1 = safrToolkit.createLabel(defaults, SWT.NONE, "");
		fillr1.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(defaults, SWT.NONE,"Default &Group:");
		comboDefaultGroupViewer = safrToolkit.createTableComboForComponents(
				defaults, ComponentType.Group);		
		comboDefaultGroup = comboDefaultGroupViewer.getTableCombo();
		comboDefaultGroup.setData(SAFRLogger.USER, "Default Group");                                                                                                                                              
		
		addGrpOpenEditorMenu();		
		comboDefaultGroup.setLayoutData(UIUtilities.textTableData(1));
		comboDefaultGroup.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {

					if (previousGroup == null) {
						user.setDefaultGroup(null);
					}

					EnvironmentQueryBean selectedEnvBean = null;
					if (comboDefaultEnvironment.getTable().getSelection().length > 0) {
						selectedEnvBean = (EnvironmentQueryBean) comboDefaultEnvironment
								.getTable().getSelection()[0].getData();
					}
					Environment selectedEnv = null;
					if (selectedEnvBean != null) {
						selectedEnv = SAFRApplication.getSAFRFactory()
								.getEnvironment(selectedEnvBean.getId());
					}

					GroupQueryBean selectedGroupBean = null;
					if (comboDefaultGroup.getTable().getSelection().length > 0) {
						selectedGroupBean = (GroupQueryBean) comboDefaultGroup
								.getTable().getSelection()[0].getData();
					}
					Group selectedGroup = null;
					if (selectedGroupBean != null
							&& (!(selectedGroupBean.getId() == 0))) {
						selectedGroup = SAFRApplication.getSAFRFactory()
								.getGroup(selectedGroupBean.getId());
					}
					// if (selectedGroup != null) {
					if (!UIUtilities.isEqualSAFRComponent(previousEnv,
							selectedEnv)
							|| !UIUtilities.isEqualSAFRComponent(previousGroup,
									selectedGroup)) {

						Integer selectedEnvId = 0;
						if (selectedEnvBean != null) {
							selectedEnvId = selectedEnvBean.getId();
						}
						prevDefVF = null;
						Integer selectedGroupId = 0;
						if (selectedGroupBean != null) {
							selectedGroupId = selectedGroupBean.getId();
						}
						enableDefaultsOnGroupSelection(selectedEnvId,
								selectedGroupId);
						comboDefaultViewFolder.select(-1);
						user.setDefaultGroup(selectedGroup);
						setDirty(true);
					}
					previousGroup = user.getDefaultGroup();
				} catch (SAFRNotFoundException sne) {
					UIUtilities.handleWEExceptions(sne, null,
							UIUtilities.titleStringNotFoundException);
				} catch (SAFRException e1) {
					UIUtilities.handleWEExceptions(e1,
							"Error in getting defaults on group selection.",
							null);
				}

			}

		});
		comboDefaultGroup.setEnabled(false);

		Label fillr2 = safrToolkit.createLabel(defaults, SWT.NONE, "");
		fillr2.setLayoutData(UIUtilities.textTableData(2));

		safrToolkit.createLabel(defaults, SWT.NONE,"Default View Fol&der:");

		comboDefaultViewFolderViewer = safrToolkit.createTableComboForComponents(
		    defaults,ComponentType.ViewFolder);
		comboDefaultViewFolder = comboDefaultViewFolderViewer.getTableCombo();
		addFolOpenEditorMenu();
		comboDefaultViewFolder.setData(SAFRLogger.USER, "Default View Folder");                                                                                                                                              		
		comboDefaultViewFolder.setLayoutData(UIUtilities.textTableData(1));
		comboDefaultViewFolder.addModifyListener(this);
		comboDefaultViewFolder.setEnabled(false);
		comboDefaultViewFolderViewer
				.setLabelProvider(new DefaultViewFolderLabelProvider());
		comboDefaultViewFolder.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				EnvironmentalQueryBean bean = null;
				if (comboDefaultViewFolder.getTable().getSelectionCount() > 0) {
					bean = (EnvironmentalQueryBean) comboDefaultViewFolder
							.getTable().getSelection()[0].getData();
					prevDefVF = bean;
				} else {
					if (prevDefVF != null) {
						comboDefaultViewFolder.setText(
						    UIUtilities.getComboString(prevDefVF.getName(), prevDefVF.getId()));
					} else {
						comboDefaultViewFolder.setText("");
					}
				}
			}

		});

		Label fillr3 = safrToolkit.createLabel(defaults, SWT.NONE, "");
		fillr3.setLayoutData(UIUtilities.textTableData(2));

		// Code for section toolbar
		ToolBar toolbar = new ToolBar(sectionDefaults, SWT.FLAT);
		ToolBarManager toolManager = new ToolBarManager(toolbar);
		IMenuService m = (IMenuService) getSite()
				.getService(IMenuService.class);
		m
				.populateContributionManager(toolManager,
						"toolbar:SAFRWE.com.ibm.safr.we.ui.editors.UserEditor");
		toolManager.update(true);
		sectionDefaults.setTextClient(toolbar);

		sectionDefaults.setClient(defaults);

	}

    private void addEnvOpenEditorMenu()
    {
        Text text = comboDefaultEnvironment.getTextControl();
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
        
        comboDefaultEnvironment.addMouseListener(new MouseListener() {

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

    private void addGrpOpenEditorMenu()
    {
        Text text = comboDefaultGroup.getTextControl();
        Menu menu = text.getMenu();
        grpOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        grpOpenEditorItem.setText("Open Editor");
        grpOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	GroupQueryBean bean = (GroupQueryBean)((StructuredSelection) comboDefaultGroupViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.Group);                        
                }                
            }
        });
        
        comboDefaultGroup.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	GroupQueryBean bean = (GroupQueryBean)((StructuredSelection) comboDefaultGroupViewer
                            .getSelection()).getFirstElement();
                    if (bean != null) {   
                        grpOpenEditorItem.setEnabled(true);                            
                    }
                    else {
                        grpOpenEditorItem.setEnabled(false);
                    }                    
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       

    private void addFolOpenEditorMenu()
    {
        Text text = comboDefaultViewFolder.getTextControl();
        Menu menu = text.getMenu();
        folOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        folOpenEditorItem.setText("Open Editor");
        folOpenEditorItem.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
            	ViewFolderQueryBean bean = (ViewFolderQueryBean)((StructuredSelection) comboDefaultViewFolderViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {   
                    EditorOpener.open(bean.getId(), ComponentType.ViewFolder);                        
                }                
            }
        });
        
        comboDefaultViewFolder.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3)
                {
                	EnvironmentQueryBean ebean = (EnvironmentQueryBean)((StructuredSelection) comboEnvironmentViewer
                            .getSelection()).getFirstElement();                	
                	if (ebean.getId() == UIUtilities.getCurrentEnvironmentID()) {						                	
	                	ViewFolderQueryBean bean = (ViewFolderQueryBean)((StructuredSelection) comboDefaultViewFolderViewer
	                            .getSelection()).getFirstElement();
	                    if (bean != null) {   
                        	folOpenEditorItem.setEnabled(true);                            
	                    }
	                    else {
	                    	folOpenEditorItem.setEnabled(false);
	                    }                    
                	}
                	else {
                    	folOpenEditorItem.setEnabled(false);                		
                	}
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }
            
        });      
    }       
    
	protected void enableDefaultsOnEnvSelection(Integer selectedEnvId,
			Integer selectedGroupId) throws SAFRException {
		if (comboDefaultEnvironment.getText().length() > 0) {
			// default group is applicable only for general user
			if (!user.isSystemAdmin() || (!chckSysAdmin.getSelection())) {
				comboDefaultGroup.setEnabled(true);
				populateGroup(comboDefaultGroup);

				comboDefaultViewFolder.setEnabled(false);
				comboDefaultViewFolder.getTable().deselectAll();
			} else {
				comboDefaultGroup.setEnabled(false);
				comboDefaultGroup.getTable().deselectAll();

				comboDefaultViewFolder.setEnabled(true);
				populateViewFolder(comboDefaultViewFolder, selectedEnvId,
						selectedGroupId);
			}

		} else {
			comboDefaultGroup.setEnabled(false);
			comboDefaultGroup.getTable().deselectAll();
			comboDefaultViewFolder.setEnabled(false);
			comboDefaultViewFolder.getTable().deselectAll();
		}
	}

	/**
	 * Method to populate a combo box with the list of groups
	 */
	protected void populateGroup(TableCombo comboBox) {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));

			Integer counter = 0;
			try {
				comboBox.getTable().removeAll();

				EnvironmentQueryBean selectedEnv = null;
				if (comboDefaultEnvironment.getTable().getSelection().length > 0) {
					selectedEnv = (EnvironmentQueryBean) comboDefaultEnvironment
							.getTable().getSelection()[0].getData();
				}
				if (selectedEnv != null) {
					List<GroupQueryBean> groupList = SAFRQuery.queryGroups(user
							.getUserid(), selectedEnv.getId());

					// a dummy query bean is added to list to add a blank item
					// in combo box.
					GroupQueryBean blankGroupBean = new GroupQueryBean(0, "",
							null, null, null, null);
					groupList.add(0, blankGroupBean);
					comboDefaultGroupViewer.setInput(groupList);
					comboDefaultGroupViewer.refresh();
					for (GroupQueryBean group : groupList) {
						comboBox.setData(Integer.toString(counter++), group);
					}
				}
			} catch (DAOException e) {
				UIUtilities.handleWEExceptions(e);
			}

		} finally {
			getSite().getShell().setCursor(null);
		}

	}

	/**
	 * Method to populate a combo box with the list of view folders
	 * 
	 * @param comboBox
	 *            the {@link Combo} control to be populated
	 * @param allowBlank
	 *            a boolean parameter which determines whether the user can
	 *            select a blank value in the {@link Combo}
	 * @param selectedEnvId
	 *            the ID of the default Environment
	 * @param selectedGroupId
	 *            the ID of the default Group
	 */
	private void populateViewFolder(TableCombo comboBox, Integer selectedEnvId,
			Integer selectedGroupId) {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));

			Integer counter = 0;

			try {

				comboBox.getTable().removeAll();
				List<ViewFolderQueryBean> viewFolderList = SAFRQuery
						.queryViewFolders(selectedEnvId, selectedGroupId, user,
								SortType.SORT_BY_NAME);

				// a dummy query bean is added to list to add a blank item in
				// combo box.
				ViewFolderQueryBean blankVFBean = new ViewFolderQueryBean(null,
						0, "", EditRights.ReadModifyDelete, null, null, null, null);
				viewFolderList.add(0, blankVFBean);
				comboDefaultViewFolderViewer.setInput(viewFolderList);
				comboDefaultViewFolderViewer.refresh();

				for (ViewFolderQueryBean viewFolder : viewFolderList) {
					comboBox.setData(Integer.toString(counter++), viewFolder);
				}

			} catch (DAOException e) {
				UIUtilities.handleWEExceptions(e);
			}
		} finally {
			getSite().getShell().setCursor(null);
		}
	}

	protected void enableDefaultsOnGroupSelection(Integer selectedEnvId,
			Integer selectedGroupId) throws SAFRException {
		if (comboDefaultGroup.getText().length() > 0) {
			comboDefaultViewFolder.setEnabled(true);
			populateViewFolder(comboDefaultViewFolder, selectedEnvId,
					selectedGroupId);
		} else {
			comboDefaultViewFolder.setEnabled(false);
			comboDefaultViewFolder.getTable().deselectAll();
		}
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (user.getUserid().equals("")) {
			userId.setFocus();
		} else {
			password.setFocus();
		}
	}

	@Override
	public String getModelName() {
		return "User";
	}

	@Override
	public void doRefreshControls() throws SAFRException {

		if (user.getUserid() != null && !user.getUserid().equals("")) {
			// for existing users
			UIUtilities.checkNullText(userId, user.getUserid());
			userId.setEnabled(false); // ID is not editable once saved.
			UIUtilities.checkNullText(password, user.getPassword());
			UIUtilities.checkNullText(textComments, user.getComment());
			labelCreatedValue.setText(user.getCreateBy() + " on "
					+ user.getCreateTimeString());
			labelModifiedValue.setText(user.getModifyBy() + " on "
					+ user.getModifyTimeString());
			UIUtilities.checkNullText(firstName, user.getFirstName());
			UIUtilities.checkNullText(middleInitial, user.getMiddleInitial());
			UIUtilities.checkNullText(lastName, user.getLastName());
			UIUtilities.checkNullText(emailAddress, user.getEmail());
			logLevel.select(user.getLogLevel());
			selectedLogLevel = logLevel.getText();
			UIUtilities.checkNullText(maxCompilationError, Integer
					.toString(user.getMaxCompileErrors()));

			refreshDefaults();

			if (user.getDefaultEnvironment() != null) {
				// String envstr = UIUtilities.getComboString(user
				// .getDefaultEnvironment().getName(), user
				// .getDefaultEnvironment().getId());
				Environment env = user.getDefaultEnvironment();
				comboDefaultEnvironment.select(comboDefaultEnvironment
						.indexOf(UIUtilities.getComboString(env.getName(), env
								.getId())));

				previousEnv = user.getDefaultEnvironment();
			}

			Integer defaultGroupId = 0;
			Integer defaultEnvId = 0;
			if (user.getDefaultEnvironment() != null) {
				defaultEnvId = user.getDefaultEnvironment().getId();
			}
			// If User is a General User
			if (!user.isSystemAdmin()) {
				chckSysAdmin.setSelection(false);

				// group id is needed only for populating View Folders, and View
				// Folder is populated only after group is selected for a
				// General User, hence groupId is passed as 0
				enableDefaultsOnEnvSelection(defaultEnvId, defaultGroupId);

				if (user.getDefaultGroup() != null) {
					comboDefaultGroup.select(comboDefaultGroup
							.indexOf(UIUtilities.getComboString(user
									.getDefaultGroup().getName(), user
									.getDefaultGroup().getId())));

					previousGroup = user.getDefaultGroup();

					defaultGroupId = user.getDefaultGroup().getId();

				}
				enableDefaultsOnGroupSelection(defaultEnvId, defaultGroupId);
			} else {
				chckSysAdmin.setSelection(true);

				// group id is needed only for populating View Folders, and
				// group id is not applicable if user is a sysAdmin, hence
				// groupId is passed as 0
				enableDefaultsOnEnvSelection(defaultEnvId, defaultGroupId);

			}

			// Update User Profile - for General User, chckSysAdmin and logLevel
			// are not applicable
			User currentUser = SAFRApplication.getUserSession().getUser();
			if (!currentUser.isSystemAdmin()) {
				chckSysAdmin.setVisible(false);
				chckSysAdmin.setSelection(false);
				logLevel.setEnabled(false);
			} else {
				chckSysAdmin.setVisible(true);
				logLevel.setEnabled(true);
			}

			if (user.getDefaultViewFolder() != null) {
				comboDefaultViewFolder.select(comboDefaultViewFolder
						.indexOf(UIUtilities.getComboString(user
								.getDefaultViewFolder().getName(), user
								.getDefaultViewFolder().getId())));
			}
			if (comboDefaultViewFolder.getTable().getSelection().length > 0) {
				prevDefVF = (EnvironmentalQueryBean) comboDefaultViewFolder
						.getTable().getSelection()[0].getData();
			} else {
				prevDefVF = null;
			}
			form.setText(user.getUserid());
		} else {
			form.setText(user.getUserid());
		}

	}

	@Override
	public void refreshModel() {
		user.setSystemAdmin(chckSysAdmin.getSelection());
		user.setUserid(userId.getText());
		user.setPassword(password.getText());
		user.setComment(textComments.getText());
		user.setFirstName(firstName.getText());
		user.setMiddleInitial(middleInitial.getText());
		user.setLastName(lastName.getText());
		user.setEmail(emailAddress.getText());
		user.setLogLevel(logLevel.getSelectionIndex());
		if (maxCompilationError.getText() == "") {
			user.setMaxCompileErrors(UIUtilities
					.stringToInteger(DEFAULTCOMPILATIONERROR));
		} else {
			user.setMaxCompileErrors(UIUtilities
					.stringToInteger(maxCompilationError.getText()));
		}
		try {
			Environment env = null;
			EnvironmentQueryBean environment = null;
			if (comboDefaultEnvironment.getText().length() > 0) {
				environment = (EnvironmentQueryBean) comboDefaultEnvironment
						.getTable().getSelection()[0].getData();

				env = SAFRApplication.getSAFRFactory().getEnvironment(
						environment.getId());
			}
			user.setDefaultEnvironment(env);

			ViewFolder vf = null;
			if (comboDefaultViewFolder.getText().length() > 0) {
				ViewFolderQueryBean viewFolder = (ViewFolderQueryBean) comboDefaultViewFolder
						.getTable().getSelection()[0].getData();

				vf = SAFRApplication.getSAFRFactory().getViewFolder(
						viewFolder.getId(), environment.getId());
			}
			user.setDefaultViewFolder(vf);

			// If User is a General User
			if (!user.isSystemAdmin()) {
				Group grp = null;
				if (comboDefaultGroup.getText().length() > 0) {
					GroupQueryBean group = (GroupQueryBean) comboDefaultGroup
							.getTable().getSelection()[0].getData();

					grp = SAFRApplication.getSAFRFactory().getGroup(
							group.getId());
				}
				user.setDefaultGroup(grp);
			}
		} catch (SAFRNotFoundException sne) {
			UIUtilities.handleWEExceptions(sne, null,
					UIUtilities.titleStringNotFoundException);
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,"Error in getting environment or group or view folder based on ID.",null);

		}

	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		user.store();
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		user.validate();
		getMsgManager().removeAllMessages();
	}

	public void refreshDefaults() {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));

			// is not used for a new user
			if (user.isPersistent()) {
				comboDefaultEnvironment.setEnabled(true);
				// CombodefaultEnvironment.deselectAll();
				// reset previous Environment as the combo is reset
				previousEnv = null;
				prevDefVF = null;
				comboDefaultGroup.setText("");
				comboDefaultGroup.setEnabled(false);
				comboDefaultGroup.getTable().deselectAll();
				// reset previous Group as the combo is reset
				previousGroup = null;

				comboDefaultViewFolder.setText("");
				comboDefaultViewFolder.setEnabled(false);
				comboDefaultViewFolder.getTable().deselectAll();
				// UIUtilities
				// .populateEnvironment(CombodefaultEnvironment, false, user);

				// load the data
				try {

					envList = SAFRQuery.queryAllEnvironments(
							SortType.SORT_BY_NAME, user);
				} catch (DAOException e1) {
					UIUtilities.handleWEExceptions(e1,
					    "Unexpected error occurred while retrieving environments for currently edited user.",UIUtilities.titleStringDbException);
				}
				Integer count = 0;
				if (envList != null) {
					for (EnvironmentQueryBean enviromentQuerybean : envList) {
						comboDefaultEnvironment.setData(
								Integer.toString(count), enviromentQuerybean);
						count++;
					}
				}

				EnvironmentQueryBean blankEnvironmentBean = new EnvironmentQueryBean(
						0, "", false, null, null, null, null);
				envList.add(0, blankEnvironmentBean);
				comboEnvironmentViewer.setInput(envList);
			}
		} finally {
			getSite().getShell().setCursor(null);
		}
	}

	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
		if (property == User.Property.USERID) {
			return userId;
		} else if (property == User.Property.MAX_COMPILATION_ERROR) {
			return maxCompilationError;
		} else if (property == User.Property.PASSWORD) {
			return password;
		} else if (property == User.Property.FIRST_NAME) {
			return firstName;
		} else if (property == User.Property.LAST_NAME) {
			return lastName;
		} else if (property == User.Property.COMMENT) {
			return textComments;
		}
		return null;
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.User;
	}

	@Override
	public SAFRPersistentObject getModel() {
		return user;
	}

	@Override
	public SAFRPersistentObject saveComponentCopy()
			throws SAFRValidationException, SAFRException {
		SAFRPersistentObject copy = null;
		SaveAsDialog saveAsDialog;
		// open the save as dialog with the last name entered by the user.
		if (copyName == null) {
			saveAsDialog = new SaveAsDialog(this.getSite().getShell(),
					getComponentNameForSaveAs(), true);
		} else {
			saveAsDialog = new SaveAsDialog(this.getSite().getShell(),
					copyName, true);
		}
		int returnCode = saveAsDialog.open();
		if (returnCode == IDialogConstants.OK_ID) {
			copyName = saveAsDialog.getNewName();
			boolean storeAsociatedGroups = MessageDialog.openQuestion(Display
					.getCurrent().getActiveShell(), "Copy Group Membership",
					"Do you want the new user to belong to the same groups? ");
			try {
				getSite().getShell().setCursor(
						getSite().getShell().getDisplay().getSystemCursor(
								SWT.CURSOR_WAIT));
				copy = user.saveAs(copyName, storeAsociatedGroups);
			} finally {
				getSite().getShell().setCursor(null);
			}
		}
		// set the name as null once the save as is done.
		copyName = null;
		return copy;
	}

	@Override
	public String getComponentNameForSaveAs() {
		return userId.getText();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(
				com.ibm.safr.we.model.User.Property.USERID)) {
			return true;
		}
		return false;
	}

	class DefaultViewFolderLabelProvider extends ViewFolderTableLabelProvider
			implements IColorProvider {
		public String getColumnText(Object element, int columnIndex) {

			switch (columnIndex) {
			case 2:
				NumericIdQueryBean nBean = (NumericIdQueryBean) element;
				return UIUtilities.getComboString(nBean.getName(), nBean
						.getId());
			default:
				return super.getColumnText(element, columnIndex);
			}
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			return null;
		}

	}
}
