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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
	
	private class FieldComparator implements Comparator<LRField> {

		@Override
		public int compare(LRField f1, LRField f2) {
			 
			return f1.getName().compareTo(f2.getName());
		}
		
	}
    
	private class FieldPositionComparator implements Comparator<LRField> {

		@Override
		public int compare(LRField f1, LRField f2) {
			 
			return f1.getPosition().compareTo(f2.getPosition());
		}
		
	}
    
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

    	Map<Integer, LogicalRecord> localLRsById = new HashMap<>();
        private boolean sortByName;

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
            if (node.getType().equals(FieldNodeType.LR)) { // && node.getChildren().size() <= 1) {
                populateLookupPathLRNodes(node);
            } else if (node.getType().equals(FieldNodeType.LP) && node.getChildren().size() <= 0) {
                FieldTreeNodeLP nodeLP = (FieldTreeNodeLP)node;
                LookupQueryBean lpBean = nodeLP.getLPBean();
                
                LogicalRecordQueryBean lrBean = nodeLP.getLrBean();
                if(lrBean == null) {                
	                Integer targLRID = source.getTargetLR(lpBean.getId());
	                lrBean = SAFRQuery.queryLogicalRecord(targLRID, source.getEnvironmentId());
	                nodeLP.addLrBean(lrBean);
	                
	                FieldTreeNodeLR lrNode = new FieldTreeNodeLR(nodeLP, FieldNodeType.LR, lrBean.getDescriptor(), lrBean);
	                nodeLP.getChildren().add(lrNode);
	                
	                FieldTreeNode lpfieldHeader = new FieldTreeNode(lrNode,FieldNodeType.LPFIELDHEADER,"Name");
	                lrNode.getChildren().add(lpfieldHeader);
                }
                
            }
            return node.getChildren().toArray();
        }

		private void populateLookupPathLRNodes(FieldTreeNode node) {
			FieldTreeNodeLR nodeLR = (FieldTreeNodeLR)node;
			LogicalRecord logicalRecord = getLRFromLocalCache(nodeLR);
			sortTheLR(logicalRecord);
			addNodesToTheTree(nodeLR, logicalRecord);
		}

		private void addNodesToTheTree(FieldTreeNodeLR nodeLR, LogicalRecord logicalRecord) {
			for (LRField field : logicalRecord.getLRFields()) {
			    FieldTreeNodeLeaf fieldLeaf = new FieldTreeNodeLeaf(nodeLR,FieldNodeType.LPFIELD,field.getDescriptor(),field);
			    nodeLR.getChildren().add(fieldLeaf);
			}
		}

		private void sortTheLR(LogicalRecord logicalRecord) {
			if(sortByName) {
				Collections.sort(logicalRecord.getLRFields(), new FieldComparator());
			} else {
				Collections.sort(logicalRecord.getLRFields(), new FieldPositionComparator());            	
			}
		}

		private LogicalRecord getLRFromLocalCache(FieldTreeNodeLR nodeLR) {
			LogicalRecord logicalRecord = localLRsById.get(nodeLR.getLrBean().getId());
			if(logicalRecord == null) {
				logicalRecord = SAFRApplication.getSAFRFactory().getLogicalRecord(nodeLR.getLrBean().getId());
				localLRsById.put(nodeLR.getLrBean().getId(), logicalRecord);
			}
			return logicalRecord;
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

		public void setSortByName(boolean sortByName) {
			this.sortByName = sortByName;
		}        
    }
    

    private ViewGenMediator mediator;    
    private ViewSource source;
    
    private Composite parent;
    private Composite compositeField;
    private TreeViewer fieldTreeViewer;
    private Button fieldAdd;
    private boolean sortByName = false;
    private LogicalRecord logicalRecord = null;
	private FieldTreeNode top;
	private FieldTreeContentProvider treeDataProvider;

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
        
        GridLayout layout = new GridLayout(3, false);
        compositeField.setLayout(layout);
        
        // field label
        Label fieldName = mediator.getGUIToolKit().createLabel(compositeField, SWT.NONE," ");
        GridData fieldData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        fieldData.heightHint = 30;
        fieldData.minimumHeight = 30;
        
        fieldName.setLayoutData(fieldData);
        
        Button sortchk = mediator.getGUIToolKit().createCheckBox(compositeField, "Fields: Sort By Name");
        GridData sortData = new GridData(SWT.LEFT, SWT.TOP, false, false);
        sortData.heightHint = 30;
        sortData.minimumHeight = 30;
        sortchk.setLayoutData(sortData);
        
        sortchk.addSelectionListener(new SelectionAdapter( ) {
            public void widgetSelected(SelectionEvent e) {
            	sortByName = sortchk.getSelection();
            	treeDataProvider.setSortByName(sortByName);
                fieldTreeViewer.setInput(generateData());        
                fieldTreeViewer.refresh();
            }
        });

        // filler label
        Label fillerName = mediator.getGUIToolKit().createLabel(compositeField, SWT.NONE,"");
        fillerName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
                
        createFieldTree();
        
        createAddButton();
        
        Label filler2Name = mediator.getGUIToolKit().createLabel(compositeField, SWT.NONE,"");
        filler2Name.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));    }

    protected void createFieldTree() {
        
        // field viewer
        fieldTreeViewer = mediator.getGUIToolKit().createTreeViewer(
            compositeField, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        GridData data = new GridData(SWT.LEFT, SWT.TOP, true, true);
        data.minimumWidth = 500;
        data.widthHint = 500;
        data.minimumHeight = 350;
        data.heightHint = 350;
        data.horizontalSpan=2;
        fieldTreeViewer.getTree().setLayoutData(data);
        treeDataProvider = new FieldTreeContentProvider();
        treeDataProvider.setSortByName(sortByName);
        fieldTreeViewer.setContentProvider(treeDataProvider);
        
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
    		top = new FieldTreeNode(null,FieldNodeType.TOP,null);
            addLRDataToTree(top);
            addLPDataToTree(top);
        return top;
    }

	private void addLPDataToTree(FieldTreeNode top) {
        FieldTreeNode lpfieldTop = new FieldTreeNode(top,FieldNodeType.LPFIELDTOP,"Lookup Path Target Fields");
        top.getChildren().add(lpfieldTop);
		for (LookupQueryBean lpBean : source.getAllLookupPaths()) {
            
            Integer targLRID = source.getTargetLR(lpBean.getId());
            if (targLRID != null) {                
                FieldTreeNodeLP lpNode = new FieldTreeNodeLP(lpfieldTop, FieldNodeType.LP, lpBean.getDescriptor(), lpBean);
                lpfieldTop.getChildren().add(lpNode);
            }
                    
        }
	}

	private void addLRDataToTree(FieldTreeNode top) {
		
		FieldTreeNode lrfieldTop = new FieldTreeNode(top,FieldNodeType.LRFIELDTOP,"LR Fields");
        top.getChildren().add(lrfieldTop);
        
        FieldTreeNode lrfieldHeader = new FieldTreeNode(lrfieldTop,FieldNodeType.LRFIELDHEADER,"Name");
        lrfieldTop.getChildren().add(lrfieldHeader);
        
        if (source.getLrFileAssociation().getAssociatingComponentId() != null) {
            if(logicalRecord == null) {
            	logicalRecord= SAFRApplication.getSAFRFactory().getLogicalRecord(source.getLrFileAssociation().getAssociatingComponentId());
            }
            if(sortByName) {
            	Collections.sort(logicalRecord.getLRFields(), new FieldComparator());
            } else {
            	Collections.sort(logicalRecord.getLRFields(), new FieldPositionComparator());            	
            }
            
            for (LRField field : logicalRecord.getLRFields()) {
                FieldTreeNodeLeaf fieldLeaf = new FieldTreeNodeLeaf(lrfieldTop,FieldNodeType.LRFIELD,field.getDescriptor(),field);
                lrfieldTop.getChildren().add(fieldLeaf);
            }
        }
	}

    public void refreshAddButtonState() {
        boolean enableAddition = false;
        boolean leafSelected = !fieldTreeViewer.getSelection().isEmpty(); 
        if (leafSelected ) {
            // set message
            if ((mediator.getEditMode().equals(EditMode.INSERTBEFORE) ||
                 mediator.getEditMode().equals(EditMode.INSERTAFTER))) {
                if (!mediator.isColumnSelected() && !mediator.viewHasNoColumns()) {
                    mediator.setErrorMessage("Select position to insert in the columns table");            
                } else {
                    enableAddition = true;
                    mediator.setInfoMessage("Add the selected fields");
                }
            } else if (mediator.getEditMode().equals(EditMode.OVERSOURCE)) {
                if (!leafSelected) {
                    mediator.setMessage("Select field to overwrite as a column");
                } else if (!mediator.isColumnSelected() && !mediator.viewHasNoColumns()) {
                    mediator.setMessage("Select position to overwrite in the columns table");            
                } else if (!enoughRoomForColumns()) {
                    mediator.setMessage("Too many fields selected to overwrite columns");
                } else {
                    mediator.setMessage("");
                    enableAddition = true;
                }
            }
        } else {
            mediator.setErrorMessage("Select fields to insert");            
        }
        fieldAdd.setEnabled(enableAddition);        
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
