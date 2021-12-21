package com.ibm.safr.we.ui.dialogs;

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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.ConnectionParameters;
import com.ibm.safr.we.data.DAOAuthorizationException;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.GroupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.view.ViewActivator;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.security.UserSession;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates the dialog and also the controls for login
 * 
 * 
 */
public class SAFRLogin extends TitleAreaDialog {

	private SAFRPreferences preferences = null;
	private SAFRGUIToolkit safrGuiToolkit;
	static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.dialogs.SAFRLogin");

	public SAFRLogin(Shell parentShell) {
		super(parentShell);
		preferences = new SAFRPreferences();
		safrGuiToolkit = new SAFRGUIToolkit();
	}

	public SAFRLogin(String user, String pass, String env, String group) {
		super(null);
		preferences = new SAFRPreferences();
		userId = user;
		password = pass;
		this.env = env;
		grp = group;		
	}
	
	Label labelUserId;
	Label labelPassword;
	Label environmentLabel;
	Label groupLabel;
	Label connectionLab;
	private String currentConnectionName;
	private Combo comboConnection;	
	private Text userID;
	String userId;
	private Text pswd;
	String password;

	private TableCombo comboEnvironment;
	private Map<String, Integer> envMap = new HashMap<String, Integer>();
	private Map<String, Integer> grpMap = new HashMap<String, Integer>();
	String env;
	private TableCombo comboGroup;
	String grp;
	private Button checkEnv;
	Boolean chckEnv;
	private Button checkGrp;
	Boolean chckGrp;
	private Group connectionGroup;
	private Group loginGroup;
    private Group envGroup;
	private Button manageConnections;
	private Button login;
    private Button reset;
	private Button ok;
    @SuppressWarnings("unused")
    private Button cancel;

	private User currentUser = null;

	private TableComboViewer comboEnvViewer;

	private TableComboViewer comboGroupViewer;

	private String prevEnv = "";

	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		setTitle("GenevaERS Login");
		getShell().setText("GenevaERS Login");
		return contents;
	}

	protected Control createDialogArea(Composite parent) {

        Composite topLevelComp = safrGuiToolkit.createComposite(parent,SWT.BORDER);
        topLevelComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));        
        GridLayout Grid = new GridLayout();
        Grid.numColumns = 1;
        Grid.makeColumnsEqualWidth = true;
        topLevelComp.setLayout(Grid);
        
        connectionGroup = new Group(topLevelComp, SWT.NONE);
        connectionGroup.setText("Database Connection");
        GridLayout conLayout = new GridLayout();
        conLayout.numColumns = 3;
        conLayout.makeColumnsEqualWidth = true;
        conLayout.horizontalSpacing = 10;
        connectionGroup.setLayout(conLayout);

        connectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        connectionLab = safrGuiToolkit.createLabel(connectionGroup, SWT.NONE,"   &Name:");
        connectionLab.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        comboConnection = safrGuiToolkit.createComboBox(connectionGroup,SWT.DROP_DOWN, "");
        GridData gridConnCombo = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridConnCombo.horizontalSpan = 2;
        comboConnection.setLayoutData(gridConnCombo);
        comboConnection.setData(SAFRLogger.USER, "Select the database connection");
        comboConnection.setToolTipText("Select the database connection to login with.");
                
        // populate Connection combo with the connection names
        populateConnections();

        comboConnection.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (!(currentConnectionName.equals(comboConnection.getText()))) {
                    currentConnectionName = comboConnection.getText();
                }
            }
        });
        
        manageConnections = createButton(connectionGroup, IDialogConstants.OPEN_ID,
            "&Manage...", true);
        manageConnections.setSize(50, 50);
        manageConnections.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                manageConnections();
            }
        });
        
        loginGroup = new Group(topLevelComp, SWT.NONE);
        loginGroup.setText("Workbench Login");
        GridLayout loginLayout = new GridLayout();
        loginLayout.numColumns = 4;
        loginLayout.makeColumnsEqualWidth = true;
        loginGroup.setLayout(loginLayout);

        loginGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelUserId = safrGuiToolkit.createLabel(loginGroup, SWT.NONE,
                "   &User ID:");
        userID = safrGuiToolkit.createTextBox(loginGroup, SWT.BORDER);
        GridData grid1 = new GridData(GridData.FILL_HORIZONTAL);
        grid1.horizontalSpan = 2;
        userID.setLayoutData(grid1);
        userID.setData(SAFRLogger.USER, "Enter your user ID");
        userID.setTextLimit(8);
        userID.setText(preferences.getLastUser());

        safrGuiToolkit.createLabel(loginGroup, SWT.NONE, ""); // filler

        labelPassword = safrGuiToolkit.createLabel(loginGroup, SWT.NONE,"   &Password:");
        pswd = safrGuiToolkit.createTextBox(loginGroup, SWT.BORDER);
        GridData grid2 = new GridData(GridData.FILL_HORIZONTAL);
        grid2.horizontalSpan = 2;
        pswd.setLayoutData(grid1);
        pswd.setEchoChar('*');
        pswd.setTextLimit(20);
        pswd.setData(SAFRLogger.USER, "Password");     
        
        // filler
        safrGuiToolkit.createLabel(loginGroup, SWT.NONE, "");

        envGroup = new Group(topLevelComp, SWT.NONE);     
        envGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        envGroup.setText("Environment Selection");
        GridLayout envLayout = new GridLayout();
        envLayout.numColumns = 4;
        envLayout.makeColumnsEqualWidth = true;
        envLayout.marginBottom = 20;
        envGroup.setLayout(envLayout);
        
        environmentLabel = safrGuiToolkit.createLabel(envGroup, SWT.NONE, "   &Environment:");
        environmentLabel.setEnabled(false);
        comboEnvViewer = safrGuiToolkit.createTableComboForComponents(
            envGroup, ComponentType.Environment);
        comboEnvironment = comboEnvViewer.getTableCombo();
        GridData gridc1 = new GridData(GridData.FILL_HORIZONTAL);
        gridc1.horizontalSpan = 2;
        comboEnvironment.setLayoutData(gridc1);
        comboEnvironment.setData(SAFRLogger.USER, "Select your environment");
        comboEnvironment.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                
            }

            public void focusLost(FocusEvent e) {
                if ((prevEnv != null) && !(prevEnv.equals(comboEnvironment.getText()))) {
                    if (comboGroup.isEnabled()) {
                        try {
                            comboGroup.select(-1);
                            checkGrp.setEnabled(false);
                            checkGrp.setSelection(false);
                            populateGroup();
                        } catch (DAOException de) {
                            UIUtilities.handleWEExceptions(
                                de,"Unexpected Database error occurred while populating Group list.",
                                UIUtilities.titleStringDbException);
                        } catch (SAFRException se) {
                            UIUtilities.handleWEExceptions(se,"Error populating the default Group list.",null);
                        }

                        prevEnv = comboEnvironment.getText();
                    }
                }                            
            }
            
        });
        comboEnvironment.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                try {
                    if ((currentUser.getDefaultEnvironment() != null) && 
                        (envMap.get(comboEnvironment.getText()).equals(currentUser.getDefaultEnvironment().getId()))) {
                        checkEnv.setSelection(true);
                    } else {
                        checkEnv.setSelection(false);
                    }
                } catch (SAFRException e1) {
                    UIUtilities.handleWEExceptions(e1,"Error getting environment", null);                    
                }
                if (!(prevEnv.equals(comboEnvironment.getText()))) {
                    comboGroup.select(-1);
                    checkGrp.setEnabled(false);
                    checkGrp.setSelection(false);
                }                
            }
            
        });

        checkEnv = safrGuiToolkit.createCheckBox(envGroup,"&Set as Default");
        checkEnv.setData(SAFRLogger.USER, "Set selected environment as default");
        checkEnv.setToolTipText("Set selected environment as default");
        checkEnv.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (checkEnv.getSelection() && comboGroup.getSelectionIndex() != -1) {
                    checkGrp.setEnabled(true);
                } else {
                    checkGrp.setSelection(false);
                    checkGrp.setEnabled(false);
                }

            }

        });

        groupLabel = safrGuiToolkit.createLabel(envGroup, SWT.NONE, "   &Group:");
        groupLabel.setEnabled(false);
        comboGroupViewer = safrGuiToolkit.createTableComboForComponents(
            envGroup, ComponentType.Group);
        comboGroup = comboGroupViewer.getTableCombo();
        GridData gridc2 = new GridData(GridData.FILL_HORIZONTAL);
        gridc2.horizontalSpan = 2;
        comboGroup.setLayoutData(gridc2);
        comboGroup.setData(SAFRLogger.USER, "Select your Group");
        comboGroup.addSelectionListener(new SelectionAdapter() {
            
            public void widgetSelected(SelectionEvent e) {
                if (checkEnv.getSelection()) {
                    checkGrp.setEnabled(true);
                }
                Integer currentEnvId = envMap.get(comboEnvironment.getText());
                if (!currentUser.isSystemAdmin()) {
                    try {
                        if ((currentUser.getDefaultGroup() != null) && 
                            (currentEnvId.equals(currentUser.getDefaultEnvironment().getId())) && 
                            (grpMap.get(comboGroup.getText()).equals(currentUser.getDefaultGroup().getId()))) 
                        {
                            checkGrp.setSelection(true);
                        } 
                        else 
                        {
                            checkGrp.setSelection(false);
                        }
                    } catch (SAFRException e1) {
                        UIUtilities.handleWEExceptions(e1,"Error getting group", null);                                        
                    }
                }
            }
            
        });

        checkGrp = safrGuiToolkit.createButton(envGroup, SWT.CHECK, "");
        checkGrp.setText("Set as De&fault");
        checkGrp.setData(SAFRLogger.USER, "Set selected group as default");
        checkGrp.setToolTipText("Set selected group as default");
        
        enableDefaults(false);
        
        if (preferences.getLastUser() != null && preferences.getLastUser().length()>0) {
            pswd.setFocus();
        }
        
		return parent;
	}

	protected void enableDefaults(boolean enabled) {
        comboEnvironment.setEnabled(enabled);
        if (currentUser == null || currentUser.isSystemAdmin()) {
            comboGroup.setEnabled(false);
            groupLabel.setEnabled(false);
        } else {
            comboGroup.setEnabled(true);
            groupLabel.setEnabled(true);
        }
        
        checkEnv.setEnabled(enabled);
        if (currentUser == null || currentUser.isSystemAdmin()) {
            checkGrp.setEnabled(false);                        
        }
        else {
            if (checkEnv.getSelection() && comboGroup.getSelectionIndex() != -1) {
                checkGrp.setEnabled(true);
            } else {
                checkGrp.setSelection(false);
                checkGrp.setEnabled(false);
            }
        }
	}
	
	@Override
	protected Control createButtonBar(final Composite parent)
	{
        Composite buttonBar= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 0;   // create
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        buttonBar.setLayout(layout);
        buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        login = createButton(buttonBar, IDialogConstants.PROCEED_ID, "&Login", true);
        
        reset = createButton(buttonBar, IDialogConstants.RETRY_ID, "&Reset", true);
        reset.setEnabled(false);        

        Label filler= new Label(buttonBar, SWT.NONE);
        filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        layout.numColumns++;
                
        login.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (currentConnectionName == null) {
                    return;
                }
                if (!currentConnectionName.equals(SAFRPreferences.getSAFRPreferences().get(
                    UserPreferencesNodes.LAST_CONNECTION, ""))) {
                    try {
                        SAFRPreferences.getSAFRPreferences().put(
                            UserPreferencesNodes.LAST_CONNECTION,currentConnectionName);
                        SAFRPreferences.getSAFRPreferences().flush();
                        SAFRPreferences.getSAFRPreferences().sync();
                    } catch (BackingStoreException e1) {
                        logger.log(Level.SEVERE, "Failed to save preferences", e1);
                        throw new SAFRFatalException(e1);
                    }
                }
                ApplicationMediator.getAppMediator().waitCursor();
                if (checkUser()) {
                    login.setEnabled(false);
                    ok.setEnabled(true);
                    labelUserId.setEnabled(false);
                    userID.setEnabled(false);
                    pswd.setEnabled(false);
                    labelPassword.setEnabled(false);
                    comboConnection.setEnabled(false);  
                    connectionLab.setEnabled(false);
                    environmentLabel.setEnabled(true);
                    enableDefaults(true);
                    reset.setEnabled(true);
                    manageConnections.setEnabled(false);                    

                    try {
                        populateEnvironment();
                        
                        // Store the current user id to preferences
                        preferences.setLastUser(userID.getText());
                        comboEnvironment.setFocus();
                    } catch (DAOException de) {
                        UIUtilities.handleWEExceptions(
                            de,"Unexpected database error while populating Environment list.",UIUtilities.titleStringDbException);
                        setErrorMessage(de.getMessage());
                    } catch (SAFRException se) {
                        UIUtilities.handleWEExceptions(
                            se,"Unexpected error populating the list of Environments.",null);
                        setErrorMessage(se.getMessage());
                    }
                    
                    ok.setFocus();
                }
                ApplicationMediator.getAppMediator().normalCursor();                
            }
        });
        
        reset.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                labelUserId.setEnabled(true);
                labelPassword.setEnabled(true);
                userID.setEnabled(true);
                pswd.setEnabled(true);
                comboConnection.setEnabled(true);
                connectionLab.setEnabled(true);
                manageConnections.setEnabled(true);                
                login.setEnabled(true);
                ok.setEnabled(false);
                pswd.setText("");
                comboEnvironment.select(-1);
                comboEnvironment.getTable().removeAll();
                checkEnv.setSelection(false);
                loginGroup.setEnabled(true);
                comboEnvironment.setEnabled(false);
                checkEnv.setEnabled(false);
                comboGroup.select(-1);
                comboGroup.getTable().removeAll();
                checkGrp.setSelection(false);

                environmentLabel.setEnabled(false);
                groupLabel.setEnabled(false);
                comboGroup.setEnabled(false);
                checkGrp.setEnabled(false);
                reset.setEnabled(false);
                pswd.setFocus();
                prevEnv = "";
            }
        });
        
        ok = createButton(buttonBar, IDialogConstants.PROCEED_ID,"&OK", false);
        ok.setEnabled(false);        
        ok.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (validate()) {
                    saveAndClose(IDialogConstants.PROCEED_ID);
                }
            }
        });
        
        cancel = createButton(buttonBar, IDialogConstants.CANCEL_ID,"&Cancel", false);

        this.getShell().setDefaultButton(login);
        
        return buttonBar;
	}

	protected boolean validate() {
		if (comboEnvironment.getSelectionIndex() == -1) {
			setErrorMessage("Please select an Environment.");
			comboEnvironment.setFocus();
			return false;
		}
		if (comboGroup.isEnabled() && comboGroup.getSelectionIndex() == -1) {
			setErrorMessage("Please select a Group.");
			comboGroup.setFocus();
			return false;
		}
		return true;
	}
	
	private void populateConnections() {
		comboConnection.removeAll();
		for (String connName : SAFRPreferences.getConnectionNames()) {
			comboConnection.add(connName);
		}

		currentConnectionName = SAFRPreferences.getDefaultConnectionName();
		if (currentConnectionName != null) {
		    comboConnection.setText(currentConnectionName);
		}
	}

	protected void populateEnvironment() throws DAOException, SAFRException {
		// Populate the combo with a list of all environments.
		// Currently implemented for System admin only.
		// SAFRFactory factory = SAFRApplication.getSAFRFactory();

		List<EnvironmentQueryBean> envList = new ArrayList<EnvironmentQueryBean>();
		envList = SAFRQuery.queryAllEnvironments(SortType.SORT_BY_NAME,
				currentUser);
		EnvironmentQueryBean curEnv;

		User user = currentUser;
		Integer defaultenv = 0;
		try {
			if (user.getDefaultEnvironment() != null) {
				defaultenv = user.getDefaultEnvironment().getId();
			}
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,
					"Error populating the default Environment.", null);
		}
		ListIterator<EnvironmentQueryBean> iterator = envList.listIterator();
		comboEnvViewer.setInput(envList);
		comboEnvViewer.refresh();		
		while (iterator.hasNext()) {
			curEnv = (EnvironmentQueryBean) iterator.next();
			String envstr = curEnv.getName();
			envMap.put(UIUtilities.getComboString(envstr, curEnv.getId()),curEnv.getId());

			if (curEnv.getId().equals(defaultenv)) {
				comboEnvironment.select(comboEnvironment.indexOf(
				    UIUtilities.getComboString(envstr, curEnv.getId())));
				checkEnv.setSelection(true);
			}
		}
	}

	protected void populateGroup() throws DAOException, SAFRException {
		// Populate the combo with a list of all Groups.
		if (!currentUser.isSystemAdmin()) {
			// clear any previous Group entries from the combo box
			comboGroup.getTable().removeAll();
			grpMap.clear();
			checkGrp.setSelection(false);

			if (comboEnvironment.getSelectionIndex() < 0) {
				// return if environment is not selected.
				return;
			}
			List<GroupQueryBean> grpList = new ArrayList<GroupQueryBean>();
			grpList = SAFRQuery.queryGroups(userID.getText(), envMap.get(comboEnvironment.getText()));
			GroupQueryBean groupBean;

			Integer defaultGrp = 0;
			try {
				Integer currentEnvId = envMap.get(comboEnvironment.getText());
				if (currentUser.getDefaultGroup() != null && 
				    (currentEnvId.equals(currentUser.getDefaultEnvironment().getId()))) 
				{
					defaultGrp = currentUser.getDefaultGroup().getId();
				} else {
					defaultGrp = 0;
				}
			} catch (SAFRException se) {
				UIUtilities.handleWEExceptions(se,"Error populating the default Group list.", null);
			}
			comboGroupViewer.setInput(grpList);
			comboGroupViewer.refresh();
			ListIterator<GroupQueryBean> iterator = grpList.listIterator();
			while (iterator.hasNext()) {
				groupBean = (GroupQueryBean) iterator.next();
				String grpstr = groupBean.getName();
				grpMap.put(UIUtilities.getComboString(grpstr, groupBean.getId()), groupBean.getId());

				if (defaultGrp != 0 && groupBean.getId().equals(defaultGrp)) {
					checkGrp.setEnabled(true);
					checkGrp.setSelection(true);
					comboGroup.select(comboGroup.indexOf(UIUtilities.getComboString(grpstr, groupBean.getId())));
				}
			}
		}
	}

	protected void saveAndClose(int returnId) {
		userId = userID.getText();
		password = pswd.getText();
		env = comboEnvironment.getText();
		grp = comboGroup.getText();
		chckEnv = checkEnv.getSelection();
		chckGrp = checkGrp.getSelection();

		Integer selectedGroupId = null;
		com.ibm.safr.we.model.Group currentGroup = null;

		try {
			// to avoid null pointer exception when group not selected.
			if (grp != "") {
				selectedGroupId = grpMap.get(grp);
				currentGroup = SAFRApplication.getSAFRFactory().getGroup(
						selectedGroupId);
			}

			Environment currentEnv = SAFRApplication.getSAFRFactory()
					.getEnvironment(envMap.get(env));

			SAFRApplication.setUserSession(new UserSession(currentUser,
					currentEnv, currentGroup));
			
			// log the SAFR user, Environment and Group
			StringBuffer buffer = new StringBuffer();
			buffer.append("SAFR Login Details:");
			buffer.append(SAFRUtilities.LINEBREAK + "SAFR Userid  "
					+ currentUser.getUserid());
			buffer.append(SAFRUtilities.LINEBREAK + "Environment  "
					+ currentEnv.getDescriptor());
			if (currentUser.isSystemAdmin()) {
				buffer.append(SAFRUtilities.LINEBREAK+ "Group        [not applicable]");
			} else {
				buffer.append(SAFRUtilities.LINEBREAK + "Group        "+ currentGroup.getDescriptor());
			}
			if (currentUser.isSystemAdmin()) {
				buffer.append(SAFRUtilities.LINEBREAK + "Authority    System administrator");
			} else if (SAFRApplication.getUserSession().isEnvironmentAdministrator()) {
				buffer.append(SAFRUtilities.LINEBREAK + "Authority    Environment administrator");
			} else {
				buffer.append(SAFRUtilities.LINEBREAK + "Authority    Normal user");
			}
			SAFRLogger.logAll(logger, Level.INFO, buffer.toString());

			Integer currEnvId = new Integer(0);
			Integer defEnvId = new Integer(0);
			Integer curGrpId = new Integer(0);
			Integer defGrpId = new Integer(0);

			if (envMap.get(env) != null) {
				currEnvId = envMap.get(env);
			}

			if (currentUser.getDefaultEnvironment() != null) {
				defEnvId = currentUser.getDefaultEnvironment().getId();
			}
			Integer newDefEnvId;
			Integer newDefGroupId;

			newDefEnvId = (chckEnv) ? currEnvId : 0;

			if (!currentUser.isSystemAdmin()) {
				if (grpMap.get(grp) != null) {
					curGrpId = grpMap.get(grp);
				}
				if (currentUser.getDefaultGroup() != null) {
					defGrpId = currentUser.getDefaultGroup().getId();
				}

				newDefGroupId = (chckGrp) ? curGrpId : 0;

				if ((chckEnv && !defEnvId.equals(newDefEnvId))
						|| (chckGrp && !defGrpId.equals(newDefGroupId))) {
					currentUser.setUserPreferences(newDefEnvId, newDefGroupId);
				}
			} else {
				if (chckEnv && !defEnvId.equals(newDefEnvId)) {
					currentUser.setUserPreferences(newDefEnvId, new Integer(0));
				}
			}

		} catch (Exception e) {
		    logger.log(Level.SEVERE, "Failure during login", e);
			setErrorMessage(e.getMessage());
			return;
		} 

	    SAFRLogger.logAll(logger, Level.INFO, "Compiler version is Sycada");// + ViewActivator.getCompilerVersion());
		SAFRLogger.logEnd(logger);

		setReturnCode(returnId);
		close();
	}

	private Boolean checkUser() {
		userID.setText(userID.getText().trim());
		if (userID.getText() == "") {
			setErrorMessage("UserId Not Entered");
			return false;
		}
		// Check if the supplied user exists and the password is correct
		try {
			ConnectionParameters conParms = DAOFactoryHolder.getDAOFactory().getConnectionParameters();
			conParms.setUserName(userID.getText());
			conParms.setPassWord(pswd.getText());
			currentUser = SAFRApplication.getSAFRFactory().getUser(userID.getText());
		} catch (SAFRException se) {
			setErrorMessage(se.getMessage());
			showConnectionFailureMesssage(se);
			SAFRLogger.logAllStamp(logger, Level.SEVERE, "Failed to connect to the database", se);
			SAFRLogger.getUserLogger().severe("Please check that the database server is available and not blocked by a firewall.");
			return false;
		}
		// sets the error message to null when it is rectified.
		setErrorMessage(null);
		comboEnvironment.setEnabled(true);
		comboGroup.setEnabled(true);

		// load all codes from codetable
		try {
			SAFRApplication.getSAFRFactory().getAllCodeSets();
		} catch (SAFRNotFoundException snfe) {
			UIUtilities.handleWEExceptions(snfe,
					"Unable to get the codesets from the database.", null);
			System.exit(0);
		} catch (SAFRException se) {
			UIUtilities.handleWEExceptions(se,
					"Error getting all code sets from the database.", null);
			System.exit(0);
		}		

		// log stored procedure version
		try {
		    SAFRLogger.logAll(logger, Level.INFO, "Stored Procedure Version is " + SAFRApplication.getStoredProcedureVersion());
        } catch (SAFRException e) {
            logger.log(Level.SEVERE, "Stored Procedure Version is unavailable", e);
        }
		
		return true;
	}

	private void showConnectionFailureMesssage(SAFRException se) {

		String logFailMsg = "There was an error while logging in." + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK
				+ "Possible causes are:" + SAFRUtilities.LINEBREAK
				+ "\t - User ID or Password are incorrect" + SAFRUtilities.LINEBREAK
				+ "\t - The password has expired" + SAFRUtilities.LINEBREAK
				+ "\t - The account has been locked" + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK;

		// if no embedded exception is not a connection failure
		if (!(se instanceof DAOException)) {
			return;
		}

		String message;
		String text;
		if (se instanceof DAOAuthorizationException) {
			text = "Login Failure";
			message = logFailMsg
					+ "Do you want to change the connection manager settings?";
		} else {
			DAOException de = (DAOException) se;
			text = "Connection Failure";
			String emsg = de.getMessage();
			// remove [..][..] junk from start of emsg
			int li = emsg.lastIndexOf(']');
			if (li != -1 && li + 2 < emsg.length())
				emsg = emsg.substring(li + 2);
			message = "There was an error while connecting to the database." + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK
					+ "Please check that the database server is available and not blocked by a firewall." + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK
					+ "Do you want to change the connection manager settings?";
		}

		// error message for SAFR login process.
		MessageBox errorMsg = new MessageBox(getShell(), SWT.ERROR
				| SWT.YES | SWT.NO);

		errorMsg.setMessage(message);
		errorMsg.setText(text);
		int result = errorMsg.open();
		if (result == SWT.YES) {
			manageConnections();
		}
	}

	private void manageConnections() {
		// store the last-connection name if it's changed
		if (currentConnectionName != null &&
		    !currentConnectionName.equals(SAFRPreferences
				.getDefaultConnectionName())) {
			try {
				SAFRPreferences.getSAFRPreferences().put(
						UserPreferencesNodes.LAST_CONNECTION,
						currentConnectionName);
				SAFRPreferences.getSAFRPreferences().flush();
				SAFRPreferences.getSAFRPreferences().sync();
			} catch (BackingStoreException e1) {
	            logger.log(Level.SEVERE, "Failure to save preferences", e1);
				throw new SAFRFatalException(e1);
			}
		}
		
		// open the connection mgr dialog
		SAFRConnectionManager connManager = new SAFRConnectionManager(
				getShell());
		connManager.open();
		// close the active connection if user saved the settings.
		if (connManager.getReturnCode() == IDialogConstants.CLOSE_ID) {
			try {
				SAFRApplication.getSAFRFactory().closeDatabaseConnection();
			} catch (SAFRException se) {
				UIUtilities.handleWEExceptions(se,
						"Database connection could not be closed.", null);
			}
		}
		
		// set the connection list to the new default connection
		populateConnections();
	}
	
}
