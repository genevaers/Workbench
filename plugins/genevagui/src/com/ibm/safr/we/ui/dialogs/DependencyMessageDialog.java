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


import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ibm.safr.we.model.utilities.ComponentData;
import com.ibm.safr.we.model.utilities.DependencyData;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class DependencyMessageDialog extends MessageDialog {
	private String dependencyString;
	private List<DependencyData> dependencies;
	Text messageText;
	Table table;
	private SAFRGUIToolkit guiToolkit = new SAFRGUIToolkit();
	boolean buttonEnabled[] = null;

	public DependencyMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage,
			String dependencyList, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		dependencyString = dependencyList;
		
		Integer currentStyle = this.getShellStyle();
		this.setShellStyle(currentStyle|SWT.RESIZE);
	}

    public DependencyMessageDialog(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage,
            String dependencyList, int dialogImageType,
            String[] dialogButtonLabels, boolean[] buttonEnabled, int defaultIndex) {
        this(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dependencyList,
                dialogImageType, dialogButtonLabels, defaultIndex);
        this.buttonEnabled = buttonEnabled;
    }
	
	public DependencyMessageDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage,
			List<DependencyData> dependencyList, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		this.dependencies = dependencyList;
		
		Integer currentStyle = this.getShellStyle();
		this.setShellStyle(currentStyle|SWT.RESIZE);
	}
	
	@Override
	protected Control createCustomArea(Composite parent) {
		createComposite(parent);
		getShell()
				.setBounds(
						(getShell().getParent().getBounds().width / 2) - 250,
						(getShell().getParent().getBounds().height / 2) - 150,
						500, 300);
		return parent;
	}

	private void createComposite(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite composite = guiToolkit.createComposite(parent, SWT.BORDER);
		composite.setLayout(new FillLayout());
		
		if(dependencies == null) {
			messageText = new Text(composite, SWT.V_SCROLL | SWT.H_SCROLL
					| SWT.BORDER);
			messageText.setEditable(false);
			messageText.setText(dependencyString);
		}
		else {
		    table = new Table(composite, SWT.BORDER | SWT.V_SCROLL
		            | SWT.H_SCROLL);
		        String[] titles = { "Component Type", "name"};
		        
		    table.setBackground(composite.getBackground());
		        
	        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
	            new TableColumn(table, SWT.NULL);
	        }
	        
	    	TableItem item = null;
	    	for(DependencyData dd : dependencies) {
	    		item = new TableItem(table, SWT.NULL);
	    		item.setText(0, dd.getComponentTypeName());
	    		item.setText(1,"");
	    		List<ComponentData> cdl = dd.getComponentDataList();
	    		
	    		for(ComponentData cd : cdl){
	        		item = new TableItem(table, SWT.NULL);
	        		item.setText(0, "");
		       		item.setText(1, cd.getComponentName());
	        		if(cd.getOverwrite()) {
		            	item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
	        		} else {
		            	item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));           			
	        		}       			
	    		}
	    	}
	    	
	        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
	            table.getColumn(loopIndex).pack();
	        }
		}
	}
	
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        if (buttonEnabled != null) {
            for (int i=0; i<buttonEnabled.length ; i++) {
                getButton(i).setEnabled(buttonEnabled[i]);                
            }            
        }
    }	
	
	public static int openDependencyDialog(Shell parentShell,
			String dialogTitle, String dialogMessage, String dependencyList,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		DependencyMessageDialog dialog = new DependencyMessageDialog(
				parentShell, dialogTitle, null, dialogMessage, dependencyList,
				dialogImageType, dialogButtonLabels, defaultIndex);

		int value = dialog.open();
		if (value == 1 || value == -1) {
			return 1;
		} else {
			return value;
		}

	}

    public static int openDependencyDialog(Shell parentShell,
            String dialogTitle, String dialogMessage, String dependencyList,
            int dialogImageType, String[] dialogButtonLabels, boolean[] buttonEnabled, int defaultIndex) {
        DependencyMessageDialog dialog = new DependencyMessageDialog(
                parentShell, dialogTitle, null, dialogMessage, dependencyList,
                dialogImageType, dialogButtonLabels, buttonEnabled, defaultIndex);

        int value = dialog.open();
        if (value == 1 || value == -1) {
            return 1;
        } else {
            return value;
        }

    }
	
	public static int openDependencyDialog(Shell parentShell,
			String dialogTitle, String dialogMessage, List<DependencyData> dependencyList,
			int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		DependencyMessageDialog dialog = new DependencyMessageDialog(
				parentShell, dialogTitle, null, dialogMessage, dependencyList,
				dialogImageType, dialogButtonLabels, defaultIndex);

		int value = dialog.open();
		if (value == 1 || value == -1) {
			return 1;
		} else {
			return value;
		}

	}

}
