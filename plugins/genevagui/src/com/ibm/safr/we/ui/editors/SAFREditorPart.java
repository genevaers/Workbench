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


import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.part.EditorPart;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.dialogs.SaveAsDialog;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Abstract class for all SAFR WB Editors. This class provides an outline on how
 * Editors, in WE, should be written and enforces a standard flow. All WE
 * editors should extend this class and not IEditor part directly.
 * 
 */
public abstract class SAFREditorPart extends EditorPart implements
		ModifyListener, ISaveablePart2 {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.SAFREditorPart");
    
	boolean saveAsEnabled = true;
	
	boolean dirty = false;
	String copyName = null;
	/**
	 * This flag is used in refreshControls() method to check whether
	 * refreshControls() is called from createPartControl() or from doSave().It
	 * is set to false by default and once controls are created and
	 * refreshControls() is called it is set to true.
	 */
	private boolean initializing;
	private IMessageManager msgManager;
	
	public void setMsgManager(IMessageManager msgManager) {
		this.msgManager = msgManager;
	}

	public IMessageManager getMsgManager() {
		return msgManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor) This method provides an outline of save process which
	 * all WE editors should follow.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Saving " + getModelName(), 100);

		try {
			refreshModel();
			monitor.subTask("Validating");
			validate();
			monitor.worked(25);
			monitor.setTaskName("Saving");
			storeModel();
			refreshControls();
			setPartName(getEditorInput().getName());
			setDirty(false);
			refreshMetadataView();

		} catch (SAFRValidationException e) {
			if (!(e.getMessageString().equals(""))) {
				decorateEditor(e);
				String ctx = e.getContextMessage();
				ctx = (ctx != null ? ctx.replaceAll("&", "&&") : "");
				String msg = e.getMessageString().replaceAll("&", "&&");
				MessageDialog.openError(getSite().getShell(), "Error saving "
						+ getModelName(), ctx + msg);
			}
		} catch (SAFRCancelException e) {
			//no-op, just cancel this operation
		} catch (SAFRException e) {
            logger.log(Level.SEVERE, "Error saving " + getModelName(), e);
			String msg = e.getMessage().replaceAll("&", "&&");
			MessageDialog.openError(getSite().getShell(), "Error saving "
					+ getModelName(), msg);

		} 
		finally {
			monitor.worked(100);
			monitor.done();
		}

	}

	/**
	 * Store the Model object in DB.
	 * <p>
	 * Normally, calling the <code>Store()</code> method on SAFR Persistent
	 * object should suffice.
	 * </p>
	 * 
	 * @throws DAOException
	 *             if a data access error occurs.
	 * @throws SAFRException
	 *             if an application error occurs.
	 */
	public abstract void storeModel() throws DAOException, SAFRException;

	/**
	 * Validates the model object.
	 * <p>
	 * Normally, calling the <code>Validate()</code> method of SAFR metadata
	 * object should suffice.
	 * </p>
	 * 
	 * @throws DAOException
	 *             if a data access error occurs
	 * @throws SAFRException
	 *             if a validation fails
	 */
	public abstract void validate() throws DAOException, SAFRException;

	/**
	 * Return the current Model name (metadata name).
	 * <p>
	 * For example, this function should return "Physical File" for PF Editor.
	 * </p>
	 * This is useful while displaying messages to users.
	 * 
	 * @return String. The Model name.
	 */
	public abstract String getModelName();

	/**
	 * Refreshes the UI controls on Editor using the values in the model. Useful
	 * while loading the editor for editing a metadata and refreshing the editor
	 * with the saved metadata values.
	 * 
	 * @throws SAFRException
	 * 
	 */
	public abstract void doRefreshControls() throws SAFRException;

	/**
	 * This method handles the exceptions occurred in doRefreshControls()
	 * method.
	 */
	public final void refreshControls() {
		try {
			doRefreshControls();
			initializing = false;
		} catch (SAFRException e) {
            logger.log(Level.SEVERE, "Unexpected Workbench Error", e);
			if (initializing) {
				MessageDialog.openError(getSite().getShell(),
						"Unexpected Workbench Error",
						"An unexpected error occurred while opening this editor : "
								+ e.getMessage()
								+ SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + "This Editor will be closed.");
				getSite().getPage().closeEditor(this, false);
			} else {
				MessageDialog.openError(getSite().getShell(),
						"Unexpected Workbench Error",
						"An unexpected error occurred : " + e.getMessage());
			}
		}
	}

	/**
	 * Refreshes the Model using the values entered by the user in the UI
	 * controls. Should be called before validating and saving the metadata.
	 * 
	 */
	public abstract void refreshModel();

	/**
	 * This method is used to get the current model.
	 * 
	 * @return the component.
	 */
	public abstract SAFRPersistentObject getModel();

	/**
	 * This method will prompt the user with the Save As dialog and will save a
	 * copy of the model object.
	 * 
	 * @return copy of the component.
	 * @throws SAFRValidationException
	 * @throws SAFRException
	 */
	public SAFRPersistentObject saveComponentCopy()
			throws SAFRValidationException, SAFRException {
		SAFRPersistentObject copy = null;
		SaveAsDialog saveAsDialog;
		// open the save as dialog with the last name entered by the user.
		if (copyName == null) {
			saveAsDialog = new SaveAsDialog(this.getSite().getShell(),
					getComponentNameForSaveAs());
		} else {
			saveAsDialog = new SaveAsDialog(this.getSite().getShell(), copyName);
		}
		int returnCode = saveAsDialog.open();
		if (returnCode == IDialogConstants.OK_ID) {
			try {
				getSite().getShell().setCursor(
						getSite().getShell().getDisplay().getSystemCursor(
								SWT.CURSOR_WAIT));
				copyName = saveAsDialog.getNewName();
				copy = ((SAFRComponent) getModel()).saveAs(copyName);
				UIUtilities.enableDisableMenuAsPerUserRights();
			} finally {
				getSite().getShell().setCursor(null);
			}
		}
		// set the name as null once the save as is done.
		copyName = null;
		return copy;
	}

	/**
	 * Gets the component name from the editor. This is required for save as
	 * operation, the save as dialog prompts the user for a new name by
	 * pre-populating it with the current name in the editor text box. This
	 * method will return that value in the text box.
	 * 
	 * @return the component name as seen on the editor.
	 */
	public abstract String getComponentNameForSaveAs();

	/**
	 * Refreshes the model object for Save As method. The default implementation
	 * is to just call refreshModel() method, but child classes can override to
	 * provide custom behaviour.
	 */
	public void refreshModelForSaveAs() {
		refreshModel();
	}

	@Override
	public void doSaveAs() {
		boolean saveAsDone = false;

		while (!saveAsDone) {
			try {
				refreshModelForSaveAs();

				SAFRPersistentObject copy = saveComponentCopy();
				if (copy != null) {
					refreshMetadataView();
					UIUtilities.openEditor(copy, getEditorCompType());
	                if (copy instanceof ViewFolder) {
	                    ApplicationMediator.getAppMediator().refreshNavigator();
	                }
				}
				saveAsDone = true;

			} catch (SAFRValidationException e) {
				saveAsDone = true;
				logger.log(Level.SEVERE, "Error saving a copy of this " + getModelName(), e);
				if (!(e.getMessageString().equals(""))) {
					String ctx = e.getContextMessage();
					ctx = (ctx != null ? ctx.replaceAll("&", "&&") : "");
					String msg = e.getMessageString().replaceAll("&", "&&");
					MessageDialog.openError(getSite().getShell(),
							"Error saving a copy of this " + getModelName(),
							ctx + msg);
					if (retrySaveAs(e)) {
						saveAsDone = false;
					}
				}
			} catch (SAFRCancelException e) {
				//no-op, just cancel this operation
			} catch (SAFRException e) {
                logger.log(Level.SEVERE, "Error saving a copy of this " + getModelName(), e);
				String msg = e.getMessage().replaceAll("&", "&&");
				MessageDialog.openError(getSite().getShell(),
						"Error saving a copy of this " + getModelName(), msg);
				saveAsDone = true; // no need to retry for this error

			} catch (PartInitException e) {
                logger.log(Level.SEVERE, "Error saving a copy of this " + getModelName(), e);
				saveAsDone = true;// no need to retry for this error
			}
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(getEditorInput().getName());
		initializing = true;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public abstract boolean isSaveAsAllowed();
	
	protected boolean isSaveAsAllowed(Permissions permissionRequired) {
		boolean permitted = false;
		try {
		        if (permissionRequired == null) {
		            permitted = SAFRApplication.getUserSession().isSystemAdminOrEnvAdmin();
		        }
		        else if (SAFRApplication.getUserSession().hasPermission( permissionRequired )) {
			        permitted = true;
			    }
			
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,
				"Error occurred while getting permissions for user",
				UIUtilities.titleStringDbException);
		}
		return (saveAsEnabled && permitted);
	}
	
	public void disableSaveAs() {
		saveAsEnabled = false;
	}

	@Override
	public abstract void createPartControl(Composite parent) ;
	
	@Override
	public void setFocus() {
		saveAsEnabled = true;
	}

	// added for CQ 7095
	public abstract ComponentType getEditorCompType();

	public void modifyText(ModifyEvent e) {
		setDirty(true);
	}

	public int promptToSaveOnClose() {
		// Implemented to allow users to modify data and try re-save if an error
		// occurs while saving the dirty editor on close.
		MessageDialog dialog = new MessageDialog(getSite().getShell(),
				"Save Changes?", null, "Do you want to save changes made to "
						+ getPartName() + "?", MessageDialog.QUESTION,
				new String[] { "&Yes", "&No", "&Cancel" }, 0);
		int returnVal = dialog.open();
		// Display an hour glass till editor is closed.
		Display.getCurrent().getActiveShell().setCursor(
				Display.getCurrent().getActiveShell().getDisplay()
						.getSystemCursor(SWT.CURSOR_WAIT));
		if (returnVal == 0) {
			// 'Yes' pressed, try to validate and save
			try {
				refreshModel();
				validate();
				storeModel();
				setDirty(false);
				refreshMetadataView();
				returnVal = ISaveablePart2.NO; // already saved. return no to
				// continue with other editors.
			} catch (SAFRValidationException e) {
				if (!(e.getMessageString().equals(""))) {
					MessageDialog.openError(getSite().getShell(),
							"Error saving " + getModelName() + ".", e
									.getMessageString());
					setDirty(false);
				}
				returnVal = ISaveablePart2.CANCEL; // allow users to modify data
			} catch (SAFRCancelException e) {
				//no-op, just cancel this operation
			} catch (SAFRException e) {
				setDirty(false);
	            logger.log(Level.SEVERE, "Error saving " + getModelName(), e);
				MessageDialog.openError(getSite().getShell(), "Error saving "
						+ getModelName(), e.getMessage());
				returnVal = ISaveablePart2.CANCEL; // allow users to modify data
			}
		} else if (returnVal == 1) {
			// 'No' pressed, just return the code back.
			returnVal = ISaveablePart2.NO;
		} else {
			// 'Cancel' pressed, just return the code back.
			returnVal = ISaveablePart2.CANCEL;
		}
		// return cursor to normal
		Display.getCurrent().getActiveShell().setCursor(null);
		return returnVal;
	}

	/**
	 * Sets the dirty flag of the editor to true or false and fires the
	 * PROP_DIRTY event.
	 * 
	 * @param dirty
	 *            flag to be set.
	 */
	public void setDirty(Boolean dirty) {
		this.dirty = dirty;
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	/**
	 * Decorates the editor with the validation errors after showing those to
	 * the user.Subclasses provide an implementation.This may include setting
	 * focus to the first property having error and to decorate the eclipse form
	 * with markers.
	 * 
	 * @param e
	 *            the {@link SAFRValidationException} thrown by the model class.
	 */
	public void decorateEditor(SAFRValidationException e) {
		msgManager.removeAllMessages();
		for (Object property : e.getErrorMessageMap().keySet()) {
			for (String errorMesg : e.getErrorMessageMap().get(property)) {
				String msg = errorMesg.replace("&", "&&");
                Control ctrl = getControlFromProperty(property); 
                if (ctrl == null || ctrl.isDisposed()) { 
                        msgManager.addMessage(msg, msg, null, 
                                        IMessageProvider.ERROR); 
                } else { 
                        msgManager.addMessage(msg, msg, null, 
                                        IMessageProvider.ERROR, ctrl); 
                        msgManager.setDecorationPosition(SWT.TOP); 
                } 
            }
		}
	}
	
	/**
	 * Declared here to allow the base to call derived class methods
	 * in decorateEditor
	 * 
	 * Not not all derived classes currently implement this function
	 * hence it is not abstract.
	 * 
	 * @param property
	 * @return
	 */
	protected Control getControlFromProperty(Object property) {
		return null;
	}

	/**
	 * This method is to call refresh the metadata view when a component is
	 * saved.
	 */
	public void refreshMetadataView() {
		ApplicationMediator.getAppMediator().refreshMetadataView(getEditorCompType(), null);
	}

	public abstract Boolean retrySaveAs(SAFRValidationException sve);
	
	protected void spaceToUnderscore(Text textName, KeyEvent e) {
		int caret = textName.getCaretPosition();
		String sData = textName.getText();
		sData = sData.replace(' ', '_');
		textName.setText(sData);
		textName.setSelection(caret);
	}
	
	protected void closeEditor(){
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	IWorkbenchPage page = workbenchWindow.getActivePage();
    	page.closeEditor(this,true);
	}
	
}
