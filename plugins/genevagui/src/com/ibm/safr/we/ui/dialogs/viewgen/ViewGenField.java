package com.ibm.safr.we.ui.dialogs.viewgen;

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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.dialogs.viewgen.FieldTreeNode.FieldNodeType;
import com.ibm.safr.we.ui.dialogs.viewgen.ViewGenCriteria.EditMode;

public class ViewGenField {
    
    public class FieldColumnTypeProvider extends ColumnLabelProvider
    {

        @Override
        public Font getFont(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;
            if (node.getType().equals(FieldNodeType.LRFIELDHEADER) ||
                node.getType().equals(FieldNodeType.LPFIELDHEADER)) {
                return mediator.getBoldFont();                
            }            
            return mediator.getFont();
        }
        
        @Override
        public String getText(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;            
            if (node.getType().equals(FieldNodeType.LRFIELD) ||
                node.getType().equals(FieldNodeType.LPFIELD)) {
                FieldTreeNodeLeaf leaf = (FieldTreeNodeLeaf)node;
                return leaf.getField().getDataTypeCode().getDescription();
            } else if (node.getType().equals(FieldNodeType.LRFIELDHEADER) ||
                node.getType().equals(FieldNodeType.LPFIELDHEADER)) {
                return "Type";
            }
            return "";
        }
        
    }
    
    public class FieldColumnPositionProvider extends ColumnLabelProvider
    {

        @Override
        public Font getFont(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;
            if (node.getType().equals(FieldNodeType.LRFIELDHEADER) ||
                node.getType().equals(FieldNodeType.LPFIELDHEADER)) {
                return mediator.getBoldFont();                
            }            
            return mediator.getFont();
        }

        @Override
        public String getText(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;            
            if (node.getType().equals(FieldNodeType.LRFIELD) ||
                node.getType().equals(FieldNodeType.LPFIELD)) {
                FieldTreeNodeLeaf leaf = (FieldTreeNodeLeaf)node;
                return leaf.getField().getPosition().toString();
            } else if (node.getType().equals(FieldNodeType.LRFIELDHEADER) ||
                node.getType().equals(FieldNodeType.LPFIELDHEADER)) {
                return "Pos";
            }
            return "";
        }
        
    }
    
    public class FieldTreeLabelProvider extends ColumnLabelProvider  
    {

        @Override
        public Font getFont(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;
            if (node.getType().equals(FieldNodeType.LRFIELDHEADER) ||
                node.getType().equals(FieldNodeType.LPFIELDHEADER)) {
                return mediator.getBoldFont();                
            }            
            return mediator.getFont();
        }

        @Override
        public String getText(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;            
            return node.getName();
        }

    }
    
    public class FieldTreeContentProvider implements ITreeContentProvider {


        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            FieldTreeNode topNode = (FieldTreeNode)inputElement;
            return topNode.getChildren().toArray();
        }

        @Override
        public Object[] getChildren(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;
            if (node.getType().equals(FieldNodeType.LR) && node.getChildren().size() <= 1) {
                FieldTreeNodeLR nodeLR = (FieldTreeNodeLR)node;
                
                LogicalRecord logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(nodeLR.getLrBean().getId());
                for (LRField field : logicalRecord.getLRFields()) {
                    FieldTreeNodeLeaf fieldLeaf = new FieldTreeNodeLeaf(nodeLR,FieldNodeType.LPFIELD,field.getDescriptor(),field);
                    nodeLR.getChildren().add(fieldLeaf);
                }
            } else if (node.getType().equals(FieldNodeType.LP) && node.getChildren().size() <= 0) {
                FieldTreeNodeLP nodeLP = (FieldTreeNodeLP)node;
                LookupQueryBean lpBean = nodeLP.getLPBean();
                Integer targLRID = source.getTargetLR(lpBean.getId());
                LogicalRecordQueryBean lrBean = SAFRQuery.queryLogicalRecord(targLRID, source.getEnvironmentId());
                
                FieldTreeNodeLR lrNode = new FieldTreeNodeLR(nodeLP, FieldNodeType.LR, lrBean.getDescriptor(), lrBean);
                nodeLP.getChildren().add(lrNode);
                
                FieldTreeNode lpfieldHeader = new FieldTreeNode(lrNode,FieldNodeType.LPFIELDHEADER,"Name");
                lrNode.getChildren().add(lpfieldHeader);
                
            }
            return node.getChildren().toArray();
        }

        @Override
        public Object getParent(Object element) {
            FieldTreeNode node = (FieldTreeNode)element;            
            return node.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            FieldTreeNode node = (FieldTreeNode)element; 
            if (node.getType().equals(FieldNodeType.LP)) {
                return true;
            } else {
                return !node.getChildren().isEmpty();
            }
        }        
    }
    

    private ViewGenMediator mediator;    
    private ViewSource source;
    
    private Composite parent;
    private Composite compositeField;
    private TreeViewer fieldTreeViewer;
    private Button fieldAdd;
    
    public ViewGenField(
        ViewGenMediator mediator, 
        Composite parent,
        ViewSource source) {
        this.mediator = mediator;
        this.parent = parent;
        this.source = source;
    }

    public void create() {
        compositeField = mediator.getGUIToolKit().createComposite(parent,SWT.NONE);
        compositeField.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
        
        GridLayout layout = new GridLayout(2, false);
        compositeField.setLayout(layout);
        
        // field label
        Label fieldName = mediator.getGUIToolKit().createLabel(compositeField, SWT.NONE,"Field:");
        GridData fieldData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        fieldData.heightHint = 30;
        fieldData.minimumHeight = 30;
        fieldName.setLayoutData(fieldData);
        

        // filler label
        Label fillerName = mediator.getGUIToolKit().createLabel(compositeField, SWT.NONE,"");
        fillerName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
                
        createFieldTree();
        
        createAddButton();
    }

    protected void createFieldTree() {
        
        // field viewer
        fieldTreeViewer = mediator.getGUIToolKit().createTreeViewer(
            compositeField, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        GridData data = new GridData(SWT.LEFT, SWT.TOP, true, true);
        data.minimumWidth = 500;
        data.widthHint = 500;
        data.minimumHeight = 350;
        data.heightHint = 350;
        fieldTreeViewer.getTree().setLayoutData(data);
        fieldTreeViewer.setContentProvider(new FieldTreeContentProvider());
        
        TreeViewerColumn viewerColumn = new TreeViewerColumn(fieldTreeViewer, SWT.NONE);
        viewerColumn.getColumn().setWidth(370);
        viewerColumn.getColumn().setText("Names");
        viewerColumn.setLabelProvider(new FieldTreeLabelProvider());

        TreeViewerColumn viewerColumn2 = new TreeViewerColumn(fieldTreeViewer, SWT.NONE);
        viewerColumn2.getColumn().setWidth(90);
        viewerColumn2.getColumn().setText("Type");
        viewerColumn2.setLabelProvider(new FieldColumnTypeProvider());
        
        TreeViewerColumn viewerColumn3 = new TreeViewerColumn(fieldTreeViewer, SWT.NONE);
        viewerColumn3.getColumn().setWidth(40);
        viewerColumn3.getColumn().setText("Pos");
        viewerColumn3.setLabelProvider(new FieldColumnPositionProvider());
                
        fieldTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                refreshAddButtonState();
            }
            
        });
        fieldTreeViewer.setInput(generateData());        
    }

    protected void createAddButton() {
        // field add button
        fieldAdd = mediator.getGUIToolKit().createButton(compositeField, SWT.PUSH,"  >>  ");
        fieldAdd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        fieldAdd.setEnabled(false);
        
        fieldAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // get list of input fields
                @SuppressWarnings("unchecked")
                Iterator<ISelection> selections = ((TreeSelection) fieldTreeViewer.getSelection()).iterator();
                List<LRField> lrFields = new ArrayList<LRField>();
                List<FieldTreeNodeLeaf> lpFields = new ArrayList<FieldTreeNodeLeaf>();
                while (selections.hasNext()) {
                    FieldTreeNode node = (FieldTreeNode)selections.next();
                    if (node instanceof FieldTreeNodeLeaf) {
                        if (node.getType().equals(FieldNodeType.LRFIELD)) {
                            lrFields.add(((FieldTreeNodeLeaf)node).getField());
                        } else if (node.getType().equals(FieldNodeType.LPFIELD)) {
                            lpFields.add((FieldTreeNodeLeaf)node);                            
                        }
                    }
                }

                mediator.putFields(lrFields, lpFields);    
                refreshAddButtonState();
            }

        });
    }

    
    protected FieldTreeNode generateData() {
        FieldTreeNode top = new FieldTreeNode(null,FieldNodeType.TOP,null);
        
        FieldTreeNode lrfieldTop = new FieldTreeNode(top,FieldNodeType.LRFIELDTOP,"LR Fields");
        FieldTreeNode lpfieldTop = new FieldTreeNode(top,FieldNodeType.LPFIELDTOP,"Lookup Path Target Fields");
        top.getChildren().add(lrfieldTop);
        top.getChildren().add(lpfieldTop);
        
        FieldTreeNode lrfieldHeader = new FieldTreeNode(lrfieldTop,FieldNodeType.LRFIELDHEADER,"Name");
        lrfieldTop.getChildren().add(lrfieldHeader);
        
        if (source.getLrFileAssociation().getAssociatingComponentId() != null) {
            LogicalRecord logicalRecord = SAFRApplication.getSAFRFactory()
                    .getLogicalRecord(source.getLrFileAssociation().getAssociatingComponentId());
            
            for (LRField field : logicalRecord.getLRFields()) {
                FieldTreeNodeLeaf fieldLeaf = new FieldTreeNodeLeaf(lrfieldTop,FieldNodeType.LRFIELD,field.getDescriptor(),field);
                lrfieldTop.getChildren().add(fieldLeaf);
            }
        }
        
        for (LookupQueryBean lpBean : source.getAllLookupPaths()) {
            
            Integer targLRID = source.getTargetLR(lpBean.getId());
            if (targLRID != null) {                
                FieldTreeNodeLP lpNode = new FieldTreeNodeLP(lpfieldTop, FieldNodeType.LP, lpBean.getDescriptor(), lpBean);
                lpfieldTop.getChildren().add(lpNode);
            }
                    
        }
        return top;
    }

    public void refreshAddButtonState() {
        boolean leafSelected = false;
        // check that tree has at least one leaf selected
        @SuppressWarnings("unchecked")
        Iterator<ISelection> selections = ((TreeSelection) fieldTreeViewer.getSelection()).iterator();
        while (selections.hasNext()) {
            FieldTreeNode node = (FieldTreeNode)selections.next();
            if (node instanceof FieldTreeNodeLeaf) {
                leafSelected = true;
                break;
            }
        }
        
        // set message
        if ((mediator.getEditMode().equals(EditMode.INSERTBEFORE) ||
             mediator.getEditMode().equals(EditMode.INSERTAFTER))) {
            if (!leafSelected) {
                mediator.setMessage("Select field to insert as a column");
            } else if (!mediator.isColumnSelected() && !mediator.viewHasNoColumns()) {
                mediator.setMessage("Select position to insert in the columns table");            
            } else {
                fieldAdd.setEnabled(true);
                mediator.setMessage("");
                return;
            }
        } else if ((mediator.getEditMode().equals(EditMode.OVERALL) || 
                    mediator.getEditMode().equals(EditMode.OVERSOURCE))) {
            if (!leafSelected) {
                mediator.setMessage("Select field to overwrite as a column");
            } else if (!mediator.isColumnSelected() && !mediator.viewHasNoColumns()) {
                mediator.setMessage("Select position to overwrite in the columns table");            
            } else if (!enoughRoomForColumns()) {
                mediator.setMessage("Too many fields selected to overwrite columns");
            } else {
                mediator.setMessage("");
                fieldAdd.setEnabled(true);
                return;
            }
        }
        fieldAdd.setEnabled(false);        
    }
 
    private boolean enoughRoomForColumns() {
        @SuppressWarnings("unchecked")
        Iterator<ISelection> selections = ((TreeSelection) fieldTreeViewer.getSelection()).iterator();
        List<LRField> lrFields = new ArrayList<LRField>();
        List<FieldTreeNodeLeaf> lpFields = new ArrayList<FieldTreeNodeLeaf>();
        while (selections.hasNext()) {
            FieldTreeNode node = (FieldTreeNode)selections.next();
            if (node instanceof FieldTreeNodeLeaf) {
                if (node.getType().equals(FieldNodeType.LRFIELD)) {
                    lrFields.add(((FieldTreeNodeLeaf)node).getField());
                } else if (node.getType().equals(FieldNodeType.LPFIELD)) {
                    lpFields.add((FieldTreeNodeLeaf)node);                            
                }
            }
        }
        
        int numAvailColumns = mediator.numberColumnsFromSelection();
        
        if (numAvailColumns >= lrFields.size() + lpFields.size()) {
            return true;
        }
        else {
            return false;            
        }
    }
}
