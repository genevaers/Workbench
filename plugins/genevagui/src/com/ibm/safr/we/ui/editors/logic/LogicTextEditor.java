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
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
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
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewNew;
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewOld;

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
		safrGuiToolkit
				.setReadOnly(viewInput.getEditRights() == EditRights.Read);
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		form.getBody().setLayoutData(new GridData());
		text = new StyledText(form.getBody(), SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		
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
                String fldlu = line.substring(left, right);                
                LogicTextView lTView = (LogicTextView) getSite()
                .getPage().findView(LogicTextView.ID);
                LogicTextType logicTextType = viewInput.getLogicTextType();
                if (lTView != null) {
                    lTView.setFocusOn(fldlu, logicTextType);
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
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(LogicTextView.ID);
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
		LogicTextType logicTextType = viewInput.getLogicTextType();
		String logicText = "";
		String formHeader = "";
		if (logicTextType == LogicTextType.Extract_Record_Filter) {
			logicText = viewInput.getViewSource().getExtractRecordFilter();
			formHeader = "Extract-Phase Record Filter";
		} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			logicText = viewInput.getViewColumnSource()
					.getExtractColumnAssignment();
			formHeader = "Extract-Phase Column Logic";
		} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			logicText = viewInput.getViewColumn().getFormatColumnCalculation();
			formHeader = "Format-Phase Column Logic";
		} else if (logicTextType == LogicTextType.Format_Record_Filter) {
			logicText = view.getFormatRecordFilter();
			formHeader = "Format-Phase Record Filter";
        } else if (logicTextType == LogicTextType.Extract_Record_Output) {
            logicText = viewInput.getViewSource().getExtractRecordOutput();
            formHeader = "Extract-Phase Record Logic";
        }

		recording = false;
		if ((null != logicText) && (logicText != "")) {
			text.setText(logicText);
		} else {
			text.setText("");
		}
		recording = true;
		form.setText(formHeader);
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
		LogicTextEditorInput logicTextEditorInput = (LogicTextEditorInput) this
				.getEditorInput();
		LogicTextType logicTextType = logicTextEditorInput.getLogicTextType();
		if (logicTextType == LogicTextType.Extract_Record_Filter) {
			logicTextEditorInput.getViewSource().setExtractRecordFilter(
					text.getText());
		} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			logicTextEditorInput.getViewColumnSource()
					.setExtractColumnAssignment(text.getText());
			// update view editor .
			logicTextEditorInput.getViewEditor().updateFormulaRows();
		} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			logicTextEditorInput.getViewColumn().setFormatColumnCalculation(
					text.getText());
			// logicTextEditorInput.getViewEditor().updateFormatPhaseCalRows();
			logicTextEditorInput.getViewEditor().updateElement(
					ViewColumnEditor.FORMATPHASECALCULATION);
		} else if (logicTextType == LogicTextType.Format_Record_Filter) {
			view.setFormatRecordFilter(text.getText());
			// update the button caption in view properties.
			logicTextEditorInput.getViewEditor().updateFRFButtonState();
        } else if (logicTextType == LogicTextType.Extract_Record_Output) {
            logicTextEditorInput.getViewSource().setExtractRecordOutput(text.getText());
        }
		// update logic text cell in View Grid.
		if (logicTextEditorInput.getLogicTextDialogCellEditor() != null && 
		    !logicTextEditorInput.getLogicTextDialogCellEditor().getControl().isDisposed()) {
			// set the logic text value in the logic Text dialog cell editor.
			logicTextEditorInput.getLogicTextDialogCellEditor().setValue(
					text.getText());
			logicTextEditorInput.getLogicTextDialogCellEditor().update(
					text.getText());
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

		LogicTextEditorInput logicTextEditorInput = (LogicTextEditorInput) this
				.getEditorInput();
		LogicTextType logicTextType = logicTextEditorInput.getLogicTextType();
		String logicTextName = logicTextEditorInput.getView().getName();
		if (logicTextName == null) {
			logicTextName = "";
		}
		if (logicTextType == LogicTextType.Extract_Record_Filter ||
		    logicTextType == LogicTextType.Extract_Record_Output) {
			return "View: "
					+ logicTextName
					+ "["
					+ logicTextEditorInput.getView().getId()
					+ "] - Source: "
					+ logicTextEditorInput.getViewSource()
							.getLrFileAssociation()
							.getAssociatingComponentName()
					+ "."
					+ logicTextEditorInput.getViewSource()
							.getLrFileAssociation()
							.getAssociatedComponentName() + " ("
					+ logicTextEditorInput.getViewSource().getSequenceNo()
					+ ")";
		} else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			return "View: "
					+ logicTextName
					+ "["
					+ logicTextEditorInput.getView().getId()
					+ "] - Column: "
					+ logicTextEditorInput.getViewColumnSource()
							.getViewColumn().getColumnNo()

					+ " Source: "
					+ logicTextEditorInput.getViewColumnSource()
							.getViewSource().getLrFileAssociation()
							.getAssociatingComponentName()
					+ "."
					+ logicTextEditorInput.getViewColumnSource()
							.getViewSource().getLrFileAssociation()
							.getAssociatedComponentName()
					+ " ("
					+ logicTextEditorInput.getViewColumnSource()
							.getViewSource().getSequenceNo() + ")";
		} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			return "View: " + logicTextName + "["
					+ logicTextEditorInput.getView().getId() + "] - Column: "
					+ logicTextEditorInput.getViewColumn().getColumnNo();

		} else if (logicTextType == LogicTextType.Format_Record_Filter) {
			return "View: " + logicTextName + " ["
					+ logicTextEditorInput.getView().getId() + "]";
		}
		return "";
	}

	/**
	 * Validates the Logic text in this editor using SAFRCompiler.
	 */
	public void validateLogicText() {
		try {
			validateErrors = null;
			if (!text.getText().trim().equals("")) {
				if (viewInput.getLogicTextType() == LogicTextType.Format_Record_Filter) {
					validateFRF();
				} else if (viewInput.getLogicTextType() == LogicTextType.Extract_Record_Filter) {
					validateERF();
				} else if (viewInput.getLogicTextType() == LogicTextType.Extract_Column_Assignment) {
					validateECA();
				} else if (viewInput.getLogicTextType() == LogicTextType.Format_Column_Calculation) {
					validateFCC();
                } else if (viewInput.getLogicTextType() == LogicTextType.Extract_Record_Output) {
                    validateERO();
                }
				MessageDialog.openInformation(getSite().getShell(),
						"Logic Text", "The Logic Text is Valid.");
			}	
			closeValidationLog();
		} catch (SAFRViewActivationException sva) {
			// Validation error. store in local variable so that the
			// View
			// error table can use it. Also open the error RCP view.
			validateErrors = sva;			
			openValidationLog();
		} catch (SAFRException e) {
			// show user message.
			UIUtilities.handleWEExceptions(e,"Unexpected error occurred while validating the Logic Text.",null);
		}
	}

	public void openValidationLog() {
	    if (validateErrors != null && validateErrors.hasErrorOrWarningOccured()) {
	        if (!validateErrors.getActivationLogOld().isEmpty()) {
	            try {
	                ActivationLogViewOld eView = (ActivationLogViewOld) getSite()
	                        .getPage().showView(ActivationLogViewOld.ID);
                    eView.setViewEditor(false);
	                eView.showGridForCurrentEditor(this);
	                eView.setExpands(expandsOld);  
	            } catch (PartInitException e1) {
	                UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while opening validation errors view.",null);
	            }       
	        }
	        if (!validateErrors.getActivationLogNew().isEmpty()) {
	            try {
	                ActivationLogViewNew eView = (ActivationLogViewNew) getSite()
	                        .getPage().showView(ActivationLogViewNew.ID);
                    eView.setViewEditor(false);
	                eView.showGridForCurrentEditor(this);
	                eView.setExpands(expandsNew);              
	            } catch (PartInitException e1) {
	                UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while opening validation errors view.",null);
	            }       
	        }	        
	    }
	    else {
	        ViewEditor editor = viewInput.getViewEditor();
	        SAFRViewActivationException aex = editor.getViewActivationException();
	        if (aex != null && aex.hasErrorOrWarningOccured()) {
	            if (!aex.getActivationLogOld().isEmpty()) {
	                try {
	                    ActivationLogViewOld eView = (ActivationLogViewOld) getSite()
	                            .getPage().showView(ActivationLogViewOld.ID);
	                    eView.setViewEditor(false);
	                    eView.showGridForCurrentEditor(editor);
	                } catch (PartInitException e1) {
	                    UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while opening validation errors view.",null);
	                }       
	            }
	            if (!aex.getActivationLogNew().isEmpty()) {
	                try {
	                    ActivationLogViewNew eView = (ActivationLogViewNew) getSite()
	                            .getPage().showView(ActivationLogViewNew.ID);
	                    eView.setViewEditor(false);
	                    eView.showGridForCurrentEditor(editor);
	                } catch (PartInitException e1) {
	                    UIUtilities.handleWEExceptions(e1,"Unexpected error occurred while opening validation errors view.",null);
	                }       
	            }           	            
	        }
	    }
	}
	
	public void closeValidationLog() {
        ActivationLogViewOld logViewOld = (ActivationLogViewOld)getSite().getPage().findView(ActivationLogViewOld.ID);
        if (logViewOld != null && !logViewOld.isViewEditor()) {
            if (validateErrors != null && !validateErrors.getActivationLogOld().isEmpty()) {
                expandsOld = logViewOld.getExpands();
            }
            else {
                expandsOld = null;
            }
            getSite().getPage().hideView(logViewOld);
        }       
	    
		ActivationLogViewNew logViewNew = (ActivationLogViewNew)getSite().getPage().findView(ActivationLogViewNew.ID);
		if (logViewNew != null && !logViewNew.isViewEditor()) {
			if (validateErrors != null && !validateErrors.getActivationLogNew().isEmpty()) {
				expandsNew = logViewNew.getExpands();
			}
			else {
				expandsNew = null;
			}
			getSite().getPage().hideView(logViewNew);
		}		
		
	}
	
	/**
	 * Validates the format column calculation in this editor.
	 * 
	 * @throws SAFRException
	 */
	private void validateFCC() throws SAFRException {
		((LogicTextEditorInput) getEditorInput()).getViewColumn()
				.validateFormatColumnCalculation(text.getText());
	}

	/**
	 * Validates the Extract column assignment in this editor.
	 * 
	 * @throws SAFRException
	 */
	private void validateECA() throws SAFRException {
		((LogicTextEditorInput) getEditorInput()).getViewColumnSource()
				.validateExtractColumnAssignment(text.getText());
	}

	/**
	 * Validates the extract record filter in this editor.
	 * 
	 * @throws SAFRException
	 */
	private void validateERF() throws SAFRException {
		((LogicTextEditorInput) getEditorInput()).getViewSource()
				.validateExtractRecordFilter(text.getText());
	}

    private void validateERO() throws SAFRException {
        ((LogicTextEditorInput) getEditorInput()).getViewSource()
                .validateExtractRecordOutput(text.getText());
    }
	
	/**
	 * Validates the format record filter in this editor.
	 * 
	 * @throws SAFRException
	 */
	private void validateFRF() throws SAFRException {
		view.validateFormatRecordFilter(text.getText());
	}

	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		viewInput.getViewEditor().setDirty(true);
	}

	public boolean isLogicTextValidationMessageExistsNew() {
		return (validateErrors != null && 
		        !validateErrors.getActivationLogNew().isEmpty());
	}

    public boolean isLogicTextValidationMessageExistsOld() {
        return (validateErrors != null && 
                !validateErrors.getActivationLogOld().isEmpty());
    }
	
	public SAFRViewActivationException getLogicTextValidationErrors() {
		return validateErrors;
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

    public void partActivated(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
            ApplicationMediator.getAppMediator().updateStatusContribution(
                ApplicationMediator.STATUSBARVIEW, "", true);                          
        }        
    }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    public void partClosed(IWorkbenchPartReference partRef) {
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
            ApplicationMediator.getAppMediator().updateStatusContribution(
                ApplicationMediator.STATUSBARVIEW, "", false);                        
        }        
    }

    public void partOpened(IWorkbenchPartReference partRef) {
    }

    public void partHidden(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {
        	Display.getCurrent().asyncExec(new Runnable() {
				public void run() {			
					if(getSite().getPage() != null) {
			            IViewPart logicView = getSite().getPage().findView(LogicTextView.ID);
			            getSite().getPage().hideView(logicView);
		                closeValidationLog();
					}
				}
        	});        	
        }        
    }

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
		        	LogicTextView logicView = (LogicTextView)getSite().getPage().findView(LogicTextView.ID);
		        	if (logicView != null) {
		        		logicView.showContentsForCurrentEditor(editor);
		                openValidationLog();		        	
		        	}
				}
        	});
        }    	
    }

    public void partInputChanged(IWorkbenchPartReference partRef) {
    }	
}
