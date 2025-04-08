package com.ibm.safr.we.ui.editors.logic;

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


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRValidator;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.logic.LogicTextParser;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.editors.view.ViewColumnEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.logic.LogicTextView;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * An editor for writing Logic Text.
 * 
 */
public class LogicTextEditor extends SAFREditorPart implements IPartListener2 {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.ui.editors.LogicTextEditor");
	
	private static final int MAX_UNDO_SIZE = 25;	
	public static String ID = "SAFRWE.LogicTextEditor";
	
	// Support for Undo/Redo Operation classes
	abstract class Operation {
		abstract void redo(StyledText text);

		abstract void undo(StyledText text);
		
		void setCaretPos(StyledText widget, int pos) {
			widget.setCaretOffset(pos);
			if (!widget.getCaret().isVisible()) {
				widget.setTopIndex(widget.getLineAtOffset(pos));							
			}
		}
	}

	class ReplaceOperation extends Operation {

		private String oldText;
		private String newText;
		private int pos;

		public ReplaceOperation(String oldText, String newText, int pos) {
			this.oldText = oldText;
			this.newText = newText;
			this.pos = pos;
		}

		@Override
		public void redo(StyledText widget) {
			widget.replaceTextRange(pos, oldText.length(), newText);
			setCaretPos(widget, pos + newText.length());
		}

		@Override
		public void undo(StyledText widget) {
			widget.replaceTextRange(pos, newText.length(), oldText);
			setCaretPos(widget, pos);
		}		
	}
	
	class InsertOperation extends Operation {

		private String modText;
		private int pos;

		public InsertOperation(String modText, int pos) {
			this.modText = modText;
			this.pos = pos;
		}

		@Override
		public void redo(StyledText widget) {
			widget.replaceTextRange(pos, 0, modText);
			setCaretPos(widget, pos + modText.length());
		}

		@Override
		public void undo(StyledText widget) {
			try {
				widget.replaceTextRange(pos, modText.length(), "");
			} catch (Exception e) {
			    logger.log(Level.SEVERE,"Failure to undo",e);
			}
			setCaretPos(widget, pos);
		}		
	}

	class RemoveOperation extends Operation {

		private String modText;
		private int pos;

		public RemoveOperation(String modText, int pos) {
			this.modText = modText;
			this.pos = pos;
		}

		@Override
		public void redo(StyledText widget) {
			widget.replaceTextRange(pos, modText.length(), "");
			setCaretPos(widget, pos);
		}

		@Override
		public void undo(StyledText widget) {
			widget.replaceTextRange(pos, 0, modText);
			setCaretPos(widget, pos + modText.length());
		}

	}
	
	// main editor widget
	private StyledText text = null;
	
	private LogicTextLineStyler lineStyler = null;
	private IToolBarManager formToolbar = null;
	private FormToolkit toolkit = null;
	private SAFRGUIToolkit safrGuiToolkit = null;
	private ScrolledForm form = null;

	private LogicTextParser parser;
    private View view = null;
	private LogicTextEditorInput viewInput = null;
	private SAFRViewActivationException validateErrors;
	private Label labelHeadClient;

	// undo support
	private Stack<Operation> undoStack = new Stack<Operation>();;
	private Stack<Operation> redoStack = new Stack<Operation>();
	private boolean recording = false;
	
    // store the expand state of the related view activation log
    Object expandsOld[] = null;
    Object expandsNew[] = null;
	
	@Override
	public void createPartControl(Composite parent) {
	    
        disableCloseOthers(parent);
        
		toolkit = new FormToolkit(parent.getDisplay());
		safrGuiToolkit = new SAFRGUIToolkit(toolkit);
		safrGuiToolkit.setReadOnly(viewInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		form.getBody().setLayoutData(new GridData());
		text = new StyledText(form.getBody(), SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		parser = LogicTextParser.generateParser(
		    ((LogicTextEditorInput) getEditorInput()).getLogicTextType());		
		lineStyler = new LogicTextEditorLineStyler(this);
		GridData spec = new GridData();
		spec.horizontalAlignment = GridData.FILL;
		spec.heightHint = 1;
        spec.widthHint = 1;
		spec.grabExcessHorizontalSpace = true;
		spec.verticalAlignment = GridData.FILL;
		spec.grabExcessVerticalSpace = true;
		text.setLayoutData(spec);
		text.setEditable(!safrGuiToolkit.isReadOnly());
		text.addLineStyleListener(lineStyler);
		text.addExtendedModifyListener(lineStyler);
		text.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			    // highlight field or lookup by searching for {}

			    // get the current line
                int caretOffset = text.getCaretOffset();
                int lineIndex = text.getLineAtOffset(caretOffset);
                String line = text.getLine(lineIndex);
                int pos = caretOffset - text.getOffsetAtLine(lineIndex)-1;
                SAFRValidator validator = new SAFRValidator();

                // check the double clicked character 
                if (pos <= 0 || line.length() <= pos || !validator.isCharacterValid(line.charAt(pos))) {
                    return;
                }
                
                // search valid lookup/field name characters left for {
			    Integer left = null;
			    boolean period = false;
                for (int i=pos-1 ; i>=0 ; i--) {
                    char ch = line.charAt(i);
                    // check for {
                    if (ch == '{') {
                        left = i+1;
                        break;
                    }
                    // check for period
                    else if (period == false && ch == '.') {
                        period = true;   
                    }
                    // check for invalid character
                    else if (!validator.isCharacterValid(ch)) {
                        break;
                    }                    
                }
                
                if (left == null) {
                    return;
                }
                
                // search valid lookup/field name characters right for }
                Integer right = null;
                for (int i=pos+1 ; i<line.length() ; i++) {
                    char ch = line.charAt(i);
                    // check for {
                    if (ch == '}' || ch == ';' ||  ch == ',') {
                        right = i;
                        break;
                    }
                    // check for period
                    else if (period == false && ch == '.') {
                        period = true;   
                    }                    
                    // check for invalid character
                    else if (!validator.isCharacterValid(ch)) {
                        break;
                    }                    
                }
                
                if (right == null) {
                    return;
                }

                // have found the field so highlight it
                text.setSelection(text.getOffsetAtLine(lineIndex)+left, 
                        text.getOffsetAtLine(lineIndex)+right);
                String selectedField = line.substring(left, right);                
                LogicTextView lTView = (LogicTextView) getSite().getPage().findView(LogicTextView.ID);
                
                LogicTextType logicTextType = viewInput.getLogicTextType();
                if (lTView != null) {
                    lTView.setFocusOn(selectedField, logicTextType);
                } else {
                    try {
                    	LogicTextView lTView2 = (LogicTextView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(LogicTextView.ID);
                        lTView2.setFocusOn(selectedField, logicTextType);
					} catch (PartInitException e1) {
					    logger.log(Level.SEVERE, "PartInitException " + e1.getMessage());
					}
                }
			}

			public void mouseDown(MouseEvent e) {

			}

			public void mouseUp(MouseEvent e) {
			    updateLineStatus();
			}

		});
		text.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				
				// update line no and column no on key pressing event.
			    updateLineStatus();
			    
				// listen for undo and redo
				if (((e.stateMask & SWT.CTRL) == SWT.CTRL)) {
					switch (e.keyCode) {
					case 'z':
						recording = false;
						undo();
						recording = true;
						break;
					case 'y':
						recording = false;
						redo();
						recording = true;
						break;
                    case 'a':
                        if (((e.stateMask & SWT.SHIFT) == SWT.SHIFT)) {
                            text.setSelection(text.getCaretOffset(), text.getCaretOffset());                            
                        }
                        else {
                            text.selectAll();                            
                        }
                        break;
					default:
					}
				}
			}

			public void keyReleased(KeyEvent e) {

			}
		});
		text.addExtendedModifyListener(new ExtendedModifyListener() {

			public void modifyText(ExtendedModifyEvent event) {

				// update line no and column no on key pressing event.
				updateLineStatus();
				
				// check for undo-able operation
				if (recording) {
					String currText = text.getText();
					String newText = currText.substring(event.start, event.start
							+ event.length);
	
					if (newText != null && newText.length() > 0) {
						
						// if replace operation
						if (event.replacedText.length() > 0) {
							ReplaceOperation op = new ReplaceOperation(event.replacedText, newText, event.start);
							undoStack.push(op);						
							redoStack.clear();
						}
						// if insert operation
						else {
							InsertOperation op = new InsertOperation(newText, event.start);
							undoStack.push(op);						
							redoStack.clear();												
						}
					}				
					// else if remove operation
					else if (event.replacedText.length() > 0) {
						RemoveOperation op = new RemoveOperation(event.replacedText, event.start);
						undoStack.push(op);
						redoStack.clear();
					}
					if (undoStack.size() > MAX_UNDO_SIZE) {
						undoStack.remove(0);
					}
				}
			}
		});
		
		text.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                lineStyler.setSelection(e.x,e.y);
                text.redrawRange(0, text.getCharCount(), true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
		    
		});

		Color bg = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		text.setBackground(bg);

		// Code to manage the form toolbar.
		formToolbar = (ToolBarManager) form.getToolBarManager();
		IMenuService ms = (IMenuService) getSite().getService(
				IMenuService.class);
		ms.populateContributionManager((ContributionManager) formToolbar,
	        "toolbar:SAFRWE.com.ibm.safr.we.ui.editors.LogicTextEditor");
		formToolbar.update(true);
		labelHeadClient = safrGuiToolkit.createLabel(form.getForm().getHead(),
				SWT.LEFT, "");
		form.setHeadClient(labelHeadClient);
		refreshControls();
		text.addModifyListener(this);
		setDirty(false);
		
        getSite().getPage().addPartListener(this);
        		
	}

    public LogicTextParser getParser() {
        return parser;
    }
	
    protected void disableCloseOthers(Composite parent) {
        // get the tab folder 
        CTabFolder tabFolder = (CTabFolder) parent.getParent().getParent();
        tabFolder.addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(MenuDetectEvent e) {
                // disable menu item created in org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer from
                // org.eclipse.e4.ui.workbench.renderers.swt.source_0.12.2.v20150204-1353.jar
                Shell shell = getSite().getShell();
                Menu menu = (Menu)shell.getData("shell_close_editors_menu");
                Object part = (Object)menu.getData("stack_selected_part");
                Class<? extends Object> cl = part.getClass();
                try {
                    Field f = cl.getDeclaredField("label"); //NoSuchFieldException
                    f.setAccessible(true);
                    String label = (String) f.get(part); //IllegalAccessException
                    if (menu.getItemCount() > 1) {
                        MenuItem item = menu.getItem(1);
                        if (viewInput.getName().equals(label)) {
                            item.setEnabled(false);                        
                        }
                        else {
                            item.setEnabled(true);                                                
                        }
                    }
                } catch (Exception e1) {
                    UIUtilities.handleWEExceptions(e1,"Unexpected error on menu listener.",null);                    
                } 
            }
            
        });
    }

	/**
	 * Perform an undo from the undo stack
	 */
	private void undo() {
		if (undoStack.size() > 0) {
			Operation lastEdit = undoStack.pop();
			lastEdit.undo(text);
			redoStack.push(lastEdit);
		}
	}
	
	/**
	 * Perform a redo from the redo stack
	 */
	private void redo() {
		if (redoStack.size() > 0) {
			Operation edit = redoStack.pop();
			edit.redo(text);
			undoStack.push(edit);
		}
	}
	
	/**
	 * Safe Replace text
	 */
	
	
	/**
	 * A method for setting text in Logic Text Editor.
	 * 
	 * @param editorText
	 *            text to insert.
	 * @param revisedCaretPosition
	 *            new caret position after inserting text.
	 */
	public void setEditorText(String editorText, int revisedCaretPosition) {
        Point rng = text.getSelectionRange();
		if (editorText != null) {
		    // check first for special characters around selection
		    if (rng.x > 0 && 
		        text.getText(rng.x-1, rng.x-1).contains("{") && 
		        rng.y < text.getCharCount() && 
		        text.getText(rng.x+rng.y, rng.x+rng.y).contains("}")) {
	            rng.x--;
	            rng.y+= 2;
		    }
		    		    
			text.replaceTextRange(rng.x, rng.y, editorText);
			
	        if (revisedCaretPosition == 0) {
	            // if select length 0 caret doesn't move and 
	            // we want it at end of replacement string 
	            if (rng.y == 0) {
	                revisedCaretPosition = editorText.length();
	            }
	        }
	        else {
	            // if select length >0 caret does move so we need to adjust
	            // so that caret ends up at originally intended revisedCaretPosition
	            if (rng.y != 0) {
	                revisedCaretPosition -= editorText.length();
	            }
	        }
	        text.setCaretOffset(text.getCaretOffset() + revisedCaretPosition);

	        updateLineStatus();	        
		}

    }

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		viewInput = (LogicTextEditorInput) getEditorInput();
		view = viewInput.getView();
		if (viewInput.getEditRights() != EditRights.Read) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(LogicTextView.ID);
		}
	}

	private void updateLineStatus() {
        // update status line counter.
        int caretOffset = text.getCaretOffset();
        int lineIndex = text.getLineAtOffset(caretOffset);
        String lineCount = "Line No " + (lineIndex + 1) + " : Column No " + 
            (1 + (caretOffset - text.getOffsetAtLine(lineIndex))) + "     ";
        ApplicationMediator.getAppMediator().updateStatusContribution(
            ApplicationMediator.STATUSBARVIEW, lineCount, true);            
	}
	
	@Override
	public void setFocus() {
		text.setFocus();
		updateLineStatus();
	}

	@Override
	public String getModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doRefreshControls() throws SAFRException {
		String logicText = viewInput.getLogicText();
		recording = false;
		if (null != logicText) {
			text.setText(logicText);
		} else {
			text.setText("");
		}
		recording = true;
		form.setText(viewInput.getFormHeader());
		refreshEditorHeaders();
	}

	/**
	 * Refreshes editor title and form text.
	 * 
	 * @throws SAFRException
	 */
	public void refreshEditorHeaders() throws SAFRException {
		labelHeadClient.setText(getLogicTextFormTitle());
		// labelHeadClient.redraw();
		this.setPartName(viewInput.getName());
		// form.redraw();
	}

	@Override
	public void refreshModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void storeModel() throws DAOException, SAFRException {
		LogicTextEditorInput logicTextEditorInput = (LogicTextEditorInput) this.getEditorInput();
		logicTextEditorInput.saveLogicText(text.getText());
		LogicTextType logicTextType = logicTextEditorInput.getLogicTextType();
		if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			logicTextEditorInput.getViewEditor().updateFormulaRows();
		} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			logicTextEditorInput.getViewEditor().updateElement(
					ViewColumnEditor.FORMATPHASECALCULATION);
		} else if (logicTextType == LogicTextType.Format_Record_Filter) {
			logicTextEditorInput.getViewEditor().updateFRFButtonState();
        }
		// update logic text cell in View Grid.
		if (logicTextEditorInput.getLogicTextDialogCellEditor() != null && 
		    !logicTextEditorInput.getLogicTextDialogCellEditor().getControl().isDisposed()) {
			// set the logic text value in the logic Text dialog cell editor.
			logicTextEditorInput.getLogicTextDialogCellEditor().setValue(text.getText());
			logicTextEditorInput.getLogicTextDialogCellEditor().update(text.getText());
		}
		// as the LT is saved, make the view inactive.
		logicTextEditorInput.getViewEditor().setModified(true);
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private String getLogicTextFormTitle() throws DAOException, SAFRException {
		return ((LogicTextEditorInput) this.getEditorInput()).getLogicTextFormTitle();
	}

	/**
	 * Validates the Logic text in this editor using SAFRCompiler.
	 */
	public void validateLogicText() {
		try {
			validateErrors = null;
			if (text.getText().trim().length() > 0) {
				((LogicTextEditorInput) getEditorInput()).validateLogicText(text.getText());
			}	
		} catch (SAFRViewActivationException sva) {
			validateErrors = sva;			
		} catch (SAFRException e) {
			// show user message.
			UIUtilities.handleWEExceptions(e,"Unexpected error occurred while validating the Logic Text.",null);
		}
	}

	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		viewInput.getViewEditor().setDirty(true);
	}

	@Override
	public ComponentType getEditorCompType() {
		return null;
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
	
	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	@Override
    public void partActivated(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
            ApplicationMediator.getAppMediator().updateStatusContribution(
                ApplicationMediator.STATUSBARVIEW, "", true);                          
        }        
    }

	@Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

	@Override
    public void partClosed(IWorkbenchPartReference partRef) {
    }

	@Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
            ApplicationMediator.getAppMediator().updateStatusContribution(
                ApplicationMediator.STATUSBARVIEW, "", false);                        
        }        
    }

	@Override
    public void partOpened(IWorkbenchPartReference partRef) {
    }

	@Override
    public void partHidden(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
        	Display.getCurrent().asyncExec(new Runnable() {
				public void run() {			
					if(partRef.getPage() != null) {
			            IViewPart logicView = partRef.getPage().findView(LogicTextView.ID);
			            partRef.getPage().hideView(logicView);
					}
				}
        	});        	
        }        
    }

	@Override
    public void partVisible(final IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
        	Display.getCurrent().asyncExec(new Runnable() {
        		LogicTextEditor editor = (LogicTextEditor)partRef.getPart(false);
				public void run() {
		            if (viewInput.getEditRights() != EditRights.Read) {
		                try {
		                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		                            .getActivePage().showView(LogicTextView.ID);
		                } catch (PartInitException e) {
		                    UIUtilities.handleWEExceptions(e,"Failed to open logic text helper.", null);
		                }
		            }  
		            IWorkbenchPage p = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		            if( p != null) {
			        	IViewPart logicView = p.findView(LogicTextView.ID);
			        	if (logicView != null) {
			        		((LogicTextView) logicView).showContentsForCurrentEditor(editor);
			        	}
		            } else {
					    logger.log(Level.SEVERE,"Page NULL");
		            }
				}
        	});
        }    	
    }

	@Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }	
	
	@Override
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
			viewInput.getViewEditor().setDirty(false);;
			returnVal = ISaveablePart2.NO;
		} else {
			// 'Cancel' pressed, just return the code back.
			returnVal = ISaveablePart2.CANCEL;
		}
		// return cursor to normal
		Display.getCurrent().getActiveShell().setCursor(null);
		return returnVal;
	}
}
