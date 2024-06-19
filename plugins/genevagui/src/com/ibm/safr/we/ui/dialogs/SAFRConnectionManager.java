package com.ibm.safr.we.ui.dialogs;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.preferences.OverridePreferences;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class creates the dialog and also the controls for setting database
 * connection.
 * 
 */
public class SAFRConnectionManager extends TitleAreaDialog implements Listener {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.dialogs.SAFRConnectionManager");
    
	private SAFRGUIToolkit safrGuiToolkit;
	private static final int SERVER_INPUT_LENGTH = 60;
	private static final int TEXT_INPUT_LENGTH = 30;

	Preferences currentConnection;

	private Combo connectionName;
	private String connectionName1;
	
	private Combo databaseType;
	String databaseType1;

	private Text databaseName;
	String databaseName1;

	private Text server;
	String serverText;

	private Text port;
	String portText;

	private Text schema;
	String schemaText;

	private Text userid;
	String userID;

	private Text pswd;
	String password;

	private Text conURL;
	String connectionURL;
	
	private Composite composite;
	private Button newButton;
    private Button copyButton;
	private Button removeButton;
	private boolean newed = false;

	public String getpassword() {
		return password;
	}

	public String getuserid() {
		return userID;
	}

	public String getDatabasename() {
		return databaseName1;
	}

	public String getUrl() {
		return serverText;
	}

	public String getSchema() {
		return schemaText;
	}

	public String getDatabasetype() {
		return databaseType1;
	}

	public SAFRConnectionManager(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		safrGuiToolkit = new SAFRGUIToolkit();
	}

	protected Control createDialogArea(Composite parent) {

        composite = safrGuiToolkit.createComposite(parent,SWT.BORDER);
        GridLayout grid1 = new GridLayout();
        grid1.numColumns = 4;
        grid1.makeColumnsEqualWidth = false;
        composite.setLayout(grid1);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    
        composite.setLayoutData(data);
        
        Label connection = safrGuiToolkit.createLabel(composite, SWT.NONE,"Co&nnection Name:");
        connection.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));      
        connectionName = safrGuiToolkit.createComboBox(composite, SWT.DROP_DOWN, "");
        GridData gridConnCombo = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridConnCombo.horizontalSpan = 3;
        connectionName.setLayoutData(gridConnCombo);
        connectionName.setData(SAFRLogger.USER, "Select the database connection");
        connectionName.setToolTipText("Characters space, =, :, \\ and / are not allowed");
        connectionName.addListener(SWT.FocusIn, this);
        connectionName.addListener(SWT.Modify, this);
    
        connectionName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
    
                if (currentConnection == null || !(connectionName.getText().equals(currentConnection.name()))) {
                    changeConnectionTo(connectionName.getText());
                    loadConnectionParameters(currentConnection);
                    setButtonState();
                }
    
            }
    
        });     
    
        connectionName.addKeyListener(new KeyListener() { 
            protected void spaceToUnderscore(Combo textName, KeyEvent e) {
                Point pt = textName.getSelection();
                String sData = textName.getText();
                sData = sData.replace(' ', '_');
                textName.setText(sData);
                textName.setSelection(pt);
            }
    
            public void keyPressed(KeyEvent e) {
            }
            
            public void keyReleased(KeyEvent e) {
                if(e.keyCode == ' ') { //space
                    spaceToUnderscore(connectionName, e);
                }
            }
        });
        
        Label databaseType1 = safrGuiToolkit.createLabel(composite, SWT.NONE,"Database &Type:");
        databaseType1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,false));
        databaseType = safrGuiToolkit.createComboBox(composite, SWT.DROP_DOWN| SWT.READ_ONLY, "");
        databaseType.add("Db2");
        databaseType.setText("Db2");
        databaseType.add("PostgresQL");
        databaseType.setText("PostgresQL");
    
        GridData gridCombo = new GridData(SWT.FILL, SWT.FILL, true, true);
        databaseType.setLayoutData(gridCombo);
        databaseType.setData(SAFRLogger.USER, "Select the database type");
        databaseType.addListener(SWT.FocusIn, this);
        databaseType.addListener(SWT.Modify, this);
    
        Label databaseName1 = safrGuiToolkit.createLabel(composite, SWT.LEFT,"&Database Name:");
        GridData griddb = new GridData(SWT.END, SWT.FILL, false, false);
        databaseName1.setLayoutData(griddb);
    
        databaseName = safrGuiToolkit.createTextBox(composite, SWT.BORDER);
        GridData griddb1 = new GridData(SWT.FILL, SWT.FILL, true, false);
        griddb1.widthHint = SWT.DEFAULT;
        databaseName.setLayoutData(griddb1);
        databaseName.setData(SAFRLogger.USER, "Enter the database name");
        databaseName.setTextLimit(TEXT_INPUT_LENGTH);
        databaseName.addListener(SWT.FocusIn, this);
        databaseName.addListener(SWT.Modify, this);
    
        Label serverLabel = safrGuiToolkit.createLabel(composite, SWT.NONE,"S&erver:");
        serverLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        server = safrGuiToolkit.createTextBox(composite, SWT.BORDER);
        GridData gridsvr = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridsvr.horizontalSpan = 3;
        server.setLayoutData(gridsvr);
        server.setData(SAFRLogger.USER, "Enter server");
        server.setTextLimit(SERVER_INPUT_LENGTH);
        server.addListener(SWT.FocusIn, this);
        server.addListener(SWT.Modify, this);
        UIUtilities.replaceMenuText(server);
    
        Label portLabel = safrGuiToolkit.createLabel(composite, SWT.NONE,"P&ort:");
        portLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        port = safrGuiToolkit.createIntegerTextBox(composite, SWT.BORDER,false);
        GridData gridPort = new GridData(SWT.FILL, SWT.FILL, true, true);
        port.setLayoutData(gridPort);
        port.setData(SAFRLogger.USER, "Enter the port number");
        port.setTextLimit(TEXT_INPUT_LENGTH);
        port.addListener(SWT.FocusIn, this);
        port.addListener(SWT.Modify, this);
    
        Label schemaLabel = safrGuiToolkit.createLabel(composite, SWT.LEFT,"Sc&hema Name:");
        GridData gridsc = new GridData(SWT.END, SWT.FILL, false, false);
        schemaLabel.setLayoutData(gridsc);
    
        schema = safrGuiToolkit.createTextBox(composite, SWT.BORDER);
        GridData gridsc1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        schema.setLayoutData(gridsc1);
        schema.setData(SAFRLogger.USER, "Enter schema");
        schema.setTextLimit(TEXT_INPUT_LENGTH);
        schema.addListener(SWT.FocusIn, this);
        schema.addListener(SWT.Modify, this);
    
        Label connUrl = safrGuiToolkit.createLabel(composite, SWT.NONE,"Connection URL:");
        connUrl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        conURL = safrGuiToolkit.createTextBox(composite, SWT.BORDER| SWT.READ_ONLY);
        GridData gridurl = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridurl.horizontalSpan = 3;
        conURL.setLayoutData(gridurl);
        conURL.setEnabled(false);
        UIUtilities.replaceMenuText(conURL);
    
        Label userId = safrGuiToolkit.createLabel(composite, SWT.NONE,"&User ID:");
        userId.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        userid = safrGuiToolkit.createTextBox(composite, SWT.BORDER);
        GridData gridUser = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridUser.horizontalSpan = 2;
        userid.setLayoutData(gridUser);
        userid.setData(SAFRLogger.USER, "Enter user ID");
        userid.setTextLimit(TEXT_INPUT_LENGTH);
        userid.addListener(SWT.FocusIn, this);
        userid.addListener(SWT.Modify, this);
        
        safrGuiToolkit.createLabel(composite, SWT.NONE, ""); // col spacer
    
        Label password = safrGuiToolkit.createLabel(composite, SWT.NONE,"&Password:");
        password.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        pswd = safrGuiToolkit.createTextBox(composite, SWT.BORDER);
        GridData gridPswd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridPswd.horizontalSpan = 2;
        pswd.setLayoutData(gridPswd);
        pswd.setData(SAFRLogger.USER, "Password");
        pswd.setTextLimit(TEXT_INPUT_LENGTH);
        pswd.addListener(SWT.FocusIn, this);
        pswd.addListener(SWT.Modify, this);
        pswd.setEchoChar('*');
    
        // populate Connection combo with the connection names
        for (String connName : SAFRPreferences.getConnectionNames()) {
            connectionName.add(connName);
        }
        
        // load the default connection values
        currentConnection = SAFRPreferences.getDefaultConnectionPreferences();
        if (currentConnection !=null) {
            connectionName.setText(currentConnection.name());
            loadConnectionParameters(currentConnection);
        } else {
            newed = true;
            clearUI();
        }

		return parent;
	}

	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		setTitle("GenevaERS Connection Manager");

		setMessage("Please Enter the Following Details :",
				IMessageProvider.INFORMATION);
		getShell().setText("GenevaERS Connection Manager");

		return contents;
	}

	private void loadConnectionParameters(Preferences connection) {

		databaseType.setText(connection.get(UserPreferencesNodes.DATABASETYPE,""));
		databaseName.setText(connection.get(UserPreferencesNodes.DATABASENAME,""));
		server.setText(connection.get(UserPreferencesNodes.SERVER, ""));
		port.setText(connection.get(UserPreferencesNodes.PORT, ""));
		schema.setText(connection.get(UserPreferencesNodes.SCHEMA, ""));
		userid.setText(connection.get(UserPreferencesNodes.USERID, ""));
		pswd.setText(SAFRUtilities.decrypt(connection.get(UserPreferencesNodes.PD, "")));

	}

	private void changeConnectionTo(String connName) {
		currentConnection = SAFRPreferences.getConnectionPreferences(connName);
		SAFRPreferences.getSAFRPreferences().put(UserPreferencesNodes.LAST_CONNECTION, connName);
	}

	protected void createButtonsForButtonBar(Composite parent) {

		newButton = createButton(parent,
				IDialogConstants.DETAILS_ID, "Ne&w", true);

		newButton.addListener(SWT.FocusIn, this);

		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newed = true;
				clearUI();
				setButtonState();				
			}
		});

        copyButton = createButton(parent,
            IDialogConstants.DETAILS_ID, "&Copy", true);
        copyButton.addListener(SWT.FocusIn, this);    
        copyButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                newed = true;
                copyUI();
                setButtonState();             
            }
        });
		
        // add a remove button as a Delete / Default toggle
        removeButton = createButton(parent, IDialogConstants.BACK_ID,
                "TBD", false);
        removeButton.addListener(SWT.FocusIn, this);
        
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                
				if (newed) {
					newed = false;
					clearUI();
				} else {
					String path = UserPreferencesNodes.SAVED_CONNECTION + "/"
							+ currentConnection.name();
					try {
						SAFRPreferences.getSAFRPreferences().node(path)
								.removeNode();
					} catch (BackingStoreException e) {
					    logger.log(Level.SEVERE, "Failed to save preferences", e);
						throw new SAFRFatalException("Failed to save preferences" + e.getMessage());
					}
					
					if (removeButton.getText().equals("Default")) {
						// load system prefs
						Preferences basePrefs = ((OverridePreferences) SAFRPreferences
								.getSAFRPreferences()).getBasePrefs();
						Preferences baseConn = basePrefs.node(path);
						loadConnectionParameters(baseConn);
						currentConnection = baseConn;
					} else {
						// remove button says "Delete"
						connectionName.remove(currentConnection.name());
						clearUI();
						SAFRPreferences.getSAFRPreferences().remove(UserPreferencesNodes.LAST_CONNECTION);
					}
				}
				composite.redraw();
				setButtonState();
			}
        });
        setButtonState();
        
		final Button save = createButton(parent, IDialogConstants.CLOSE_ID,
				"&Save", true);

		save.addListener(SWT.FocusIn, this);

		save.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (validate()) {
					saveAndClose(IDialogConstants.CLOSE_ID);
				}
			}
		});
		
		final Button cancel = createButton(parent, IDialogConstants.CANCEL_ID,
				"&Cancel", false);
		cancel.addListener(SWT.FocusIn, this);

	}

	protected boolean validate() {
		if (connectionName.getText() == "") {
			connectionName.setFocus();
			setErrorMessage("Please enter connection name");
			return false;
		} else if (Pattern.matches(".*[ =:/\\\\].*", connectionName.getText())) {
			// conn name contains an invalid char ' ', =, :, / or \
			connectionName.setFocus();
			setErrorMessage("Connection name cannot contain the characters Space, =, :, / or \\.");
			return false;
		} else if (databaseType.getText() == "") {
			databaseType.setFocus();
			setErrorMessage("Please enter database type");
			return false;
		} else if (databaseName.getText() == "") {
			databaseName.setFocus();
			setErrorMessage("Please enter database name");
			return false;
		} else if (server.getText() == "") {
			server.setFocus();
			setErrorMessage("Please enter server");
			return false;
		} else if (port.getText() == "") {
			port.setFocus();
			setErrorMessage("Please enter port");
			return false;
		} else if (schema.getText() == "") {
			schema.setFocus();
			setErrorMessage("Please enter schema");
			return false;
		} else if (userid.getText() == "") {
			userid.setFocus();
			setErrorMessage("Please enter userID");
			return false;
		} else if (pswd.getText() == "") {
			pswd.setFocus();
			setErrorMessage("please enter password");
			return false;
		}

		return true;
	}
	
	protected void saveAndClose(int returnId) {
		
		connectionName1 = connectionName.getText();
		databaseType1 = databaseType.getText();
		databaseName1 = databaseName.getText();
		serverText = server.getText();
		portText = port.getText();
		schemaText = schema.getText();
		userID = userid.getText();
		password = SAFRUtilities.encrypt(pswd.getText());

		boolean saveIt = false;
		if (currentConnection == null
				|| !connectionName1.equals(currentConnection.name())
				|| !databaseType1.equals(currentConnection.get(
						UserPreferencesNodes.DATABASETYPE, ""))
				|| !databaseName1.equals(currentConnection.get(
						UserPreferencesNodes.DATABASENAME, ""))
				|| !serverText.equals(currentConnection.get(
						UserPreferencesNodes.SERVER, ""))
				|| !portText.equals(currentConnection.get(
						UserPreferencesNodes.PORT, ""))
				|| !schemaText.equals(currentConnection.get(
						UserPreferencesNodes.SCHEMA, ""))
				|| !userID.equals(currentConnection.get(
						UserPreferencesNodes.USERID, ""))
				|| !password.equals(currentConnection.get(
						UserPreferencesNodes.PD, ""))) {
			saveIt = true;
		}

		try {
			if (saveIt) {
				saveConnectionParameters();
			}

			SAFRPreferences.getSAFRPreferences()
					.put(UserPreferencesNodes.LAST_CONNECTION,
							currentConnection.name());
			SAFRPreferences.getSAFRPreferences().put(UserPreferencesNodes.HELP_URL,SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.HELP_URL, ""));
			
			SAFRPreferences.getSAFRPreferences().flush();
			SAFRPreferences.getSAFRPreferences().sync();

		} catch (BackingStoreException e) {
            logger.log(Level.SEVERE, "Failed to save preferences", e);
			throw new SAFRFatalException("Failed to save preferences" + e.getMessage());
		}

		setReturnCode(returnId);
		close();
	}

	
	private void saveConnectionParameters() throws BackingStoreException {
		
		if (currentConnection == null) {
			// no connection prefs exist yet so create a new prefs node
			Preferences savedConns = SAFRPreferences.getSAFRPreferences().node(UserPreferencesNodes.SAVED_CONNECTION);
			currentConnection = savedConns.node(connectionName1);
			
		} else if (!connectionName1.equals(currentConnection.name())) {
			// The connection name has been modified or a new connection has
			// been created, so create a new preferences node with the new
			// connection name and remove the node with the old name if this
			// is a modification.
			Preferences parentNode = currentConnection.parent();
			if (!newed) {
				// remove the old connection
				currentConnection.removeNode();
			} else {
				// reset the flag
				newed = false;
			}
			currentConnection = parentNode.node(connectionName1);
		}

		currentConnection.put(UserPreferencesNodes.DATABASENAME, databaseName1);
		currentConnection.put(UserPreferencesNodes.DATABASETYPE, databaseType1);
		currentConnection.put(UserPreferencesNodes.SERVER, serverText);
		currentConnection.put(UserPreferencesNodes.PORT, portText);
		currentConnection.put(UserPreferencesNodes.SCHEMA, schemaText);
		currentConnection.put(UserPreferencesNodes.USERID, userID);
		currentConnection.put(UserPreferencesNodes.PD, password);
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			if (event.widget instanceof Button) {
				setMessage(null);
			} else if (event.widget.getData() == null) {
				setMessage(null);
			} else {
				setMessage(event.widget.getData().toString(),
						IMessageProvider.INFORMATION);
			}
		} else if (event.type == SWT.Modify) {
		    if (removeButton != null && !removeButton.isEnabled() && connectionName != event.widget) {
		        removeButton.setEnabled(true);
		    }
			setConnectionUrlDetails();
		}

	}

	private void setConnectionUrlDetails() {
		connectionURL = databaseType.getText().toLowerCase();
		connectionURL = "jdbc:" + connectionURL + "://" + server.getText()
				+ ":" + port.getText() + "/" + databaseName.getText();
		conURL.setText(connectionURL);

	}
	
	private void setButtonState() {
	    if (newed) {
	        newButton.setEnabled(false);
            copyButton.setEnabled(false);
	    }
	    else {
            newButton.setEnabled(true);
            copyButton.setEnabled(true);	        
	    }
		if (currentConnection == null) {
			// no user or system connections exist
			removeButton.setText("De&lete");
			removeButton.setEnabled(false);
		} else {
			String path = UserPreferencesNodes.SAVED_CONNECTION + "/" + currentConnection.name();
			if (SAFRPreferences.isNodeUser(path) && SAFRPreferences.isNodeSystem(path)) {
				// system conn is overridden by a user conn
				removeButton.setText("Defau&lt");
				removeButton.setEnabled(true);
			} else if (!SAFRPreferences.isNodeUser(path) && SAFRPreferences.isNodeSystem(path)) {
				// system conn is not overridden by a user conn
				removeButton.setText("Defau&lt");
				removeButton.setEnabled(false);
			} else if (SAFRPreferences.isNodeUser(path) && !SAFRPreferences.isNodeSystem(path)) {
				// user conn is not overriding a system conn
				removeButton.setText("De&lete");
				removeButton.setEnabled(true);
			}
		}
	}
	
	private void clearUI() {
		connectionName.setText("");
		databaseType.setText("");
		databaseName.setText("");
		server.setText("");
		port.setText("5000");
		schema.setText("");
		userid.setText("");
		pswd.setText("");
		currentConnection = null;
	}

    private void copyUI() {
        connectionName.setText("");
        currentConnection = null;
    }
	
}
