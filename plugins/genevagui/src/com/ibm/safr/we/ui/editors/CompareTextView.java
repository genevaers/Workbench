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


import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class CompareTextView extends ViewPart {

    public class CompareContentProvider implements IMergeViewerContentProvider {

        private String leftLabel;
        private String rightLabel;
        
        public CompareContentProvider(String leftLabel, String rightLabel) {
            super();
            this.leftLabel = leftLabel;
            this.rightLabel = rightLabel;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public String getAncestorLabel(Object input) {
            return null;
        }

        public Image getAncestorImage(Object input) {
            return null;
        }

        public Object getAncestorContent(Object input) {
            return null;
        }

        public boolean showAncestor(Object input) {
            return false;
        }

        public String getLeftLabel(Object input) {
            return leftLabel;
        }

        public Image getLeftImage(Object input) {
            return null;
        }

        public Object getLeftContent(Object input) {
            if (input instanceof DiffNode) {
                DiffNode node =(DiffNode)input;
                return ((TextContent)node.getLeft()).getContent();
            }
            return null;
        }

        public boolean isLeftEditable(Object input) {
            return false;
        }

        public void saveLeftContent(Object input, byte[] bytes) {
        }

        public String getRightLabel(Object input) {
            return rightLabel;
        }

        public Image getRightImage(Object input) {
            return null;
        }

        public Object getRightContent(Object input) {
            if (input instanceof DiffNode) {
                DiffNode node =(DiffNode)input;
                return ((TextContent)node.getRight()).getContent();
            }
            return null;
        }

        public boolean isRightEditable(Object input) {
            return false;
        }

        public void saveRightContent(Object input, byte[] bytes) {
        }

    }

    public class TextContent implements ITypedElement {
        private String name;
        private Document content;
        
            
        public TextContent(String name, String str) {
            super();
            this.name = name;
            this.content = new Document(str);
        }

        public Document getContent() {
            return content;
        }

        public String getName() {
            return name;
        }

        public Image getImage() {
            return null;
        }

        public String getType() {
            return ITypedElement.TEXT_TYPE;
        }
    }

    public static String ID = "SAFRWE.CompareTextView";
    
    private CompareViewerPane pane;
    private TextMergeViewer viewer;
    
    public CompareTextView() {
        
    }

    public void showCompareFor(String leftStr, String rightStr, String leftLabel, String rightLabel) {
        TextContent left = new TextContent("Left", leftStr);
        TextContent right = new TextContent("Right", rightStr);
        DiffNode diffNode = new DiffNode(left, right);
        viewer.setContentProvider(new CompareContentProvider(leftLabel, rightLabel));
        viewer.setInput(diffNode);
        viewer.refresh();
    }
    
    @Override
    public void createPartControl(Composite parent) {
        GridLayoutFactory.fillDefaults().applyTo(parent);
        pane=new CompareViewerPane(parent ,SWT.BORDER | SWT.FLAT);
        GridDataFactory.fillDefaults().align(SWT.FILL,SWT.FILL).grab(true,true).applyTo(pane);
        CompareConfiguration cc=new CompareConfiguration();
        cc.setLeftLabel("Left");
        cc.setRightLabel("Right");
        viewer = new TextMergeViewer(pane, cc);        
        pane.setContent(viewer.getControl());
        GridDataFactory.fillDefaults().align(SWT.FILL,SWT.FILL).grab(true,true).applyTo(viewer.getControl());        
        viewer.setInput(null);
    }
    
    @Override
    public void setFocus() {
    }

    protected CompareViewerPane getPane() {
        return pane;
    }
    
}
