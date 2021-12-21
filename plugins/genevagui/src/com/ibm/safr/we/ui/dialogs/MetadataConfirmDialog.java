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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.ibm.safr.we.model.query.SAFRQueryBean;

/*
 * Shows a list of components (SAFRQueryBean) with an option OK or Cancel.  
 */

public class MetadataConfirmDialog extends Dialog {

    // table content provider
    public class MetadataConfirmContent implements IStructuredContentProvider {

        private List<SAFRQueryBean> content;
        
        public void dispose() {
        }

        @SuppressWarnings("unchecked")
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            content = (List<SAFRQueryBean>)newInput;
        }

        public Object[] getElements(Object inputElement) {
            return content.toArray();
        }
    }
    
    public class MetadataConfirmLabel implements ITableLabelProvider {

        public void addListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub

        }

        public void dispose() {
            // TODO Auto-generated method stub

        }

        public boolean isLabelProperty(Object element, String property) {
            // TODO Auto-generated method stub
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
            // TODO Auto-generated method stub

        }

        public Image getColumnImage(Object element, int columnIndex) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            SAFRQueryBean bean = (SAFRQueryBean)element;
            switch (columnIndex) {
            case 0:
                return bean.getDescriptor();
            }
            return "";
        }
    }    

    // passed in information
    private String dialogTitle;
    private List<SAFRQueryBean> selected;

    // swt widgets that make up the dialog
    private Table metaTable;
    private TableViewer metaTableViewer;

    public MetadataConfirmDialog(
            Shell parentShell, 
            String dialogTitle,
            List<SAFRQueryBean> selected
        ) {
        
        super(parentShell);
        this.dialogTitle = dialogTitle;
        this.selected = selected;
        Integer currentStyle = this.getShellStyle();
        this.setShellStyle(currentStyle|SWT.RESIZE);        
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(dialogTitle);
        shell.setBounds(
                (shell.getParent().getBounds().width / 2) - 350,
                (shell.getParent().getBounds().height / 2) - 150,
                700, 300);
        
    }
     
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(layout);

        metaTable = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        metaTableViewer = new TableViewer(metaTable);
        metaTable.setLayout(new FillLayout());
        metaTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        metaTable.setLinesVisible(false);
        metaTable.setBackground(area.getBackground());
        

        TableViewerColumn column1 = new TableViewerColumn(metaTableViewer,SWT.NONE);
        column1.getColumn().setText("Name");
        column1.getColumn().setWidth(650);
        column1.getColumn().setResizable(true);
                
        metaTableViewer.setLabelProvider(new MetadataConfirmLabel());
        metaTableViewer.setContentProvider(new MetadataConfirmContent());
        metaTableViewer.setInput(selected);
        return area;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        return super.createButtonBar(parent);
    }

}
