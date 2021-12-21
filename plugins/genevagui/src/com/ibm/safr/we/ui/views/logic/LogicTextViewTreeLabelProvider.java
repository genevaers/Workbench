package com.ibm.safr.we.ui.views.logic;

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


import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;


public class LogicTextViewTreeLabelProvider extends ColumnLabelProvider {
	public String getText(Object element) {
		return ((LogicTextViewTreeNode) element).getTitleText();
	}

	public String getToolTipText(Object element) {
		return ((LogicTextViewTreeNode) element).getToolTipText();
	}

	public Point getToolTipShift(Object object) {
		return new Point(5, 5);
	}

	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

	public int getToolTipTimeDisplayed(Object object) {
		return 5000;
	}

	@Override
	public void update(ViewerCell cell) {
		cell.setText(((LogicTextViewTreeNode) cell.getElement()).getTitleText());
		cell.setForeground(this.getForeground(cell.getElement()));
	}

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		return null;
	}
}
