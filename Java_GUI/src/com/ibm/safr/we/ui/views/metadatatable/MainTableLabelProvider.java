package com.ibm.safr.we.ui.views.metadatatable;

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


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;

/**
 * Generic label provider for all metadata components. This class internally
 * calls the specific label providers for the corresponding metadata component.
 * 
 */
public class MainTableLabelProvider implements ITableLabelProvider,
		ITableColorProvider {

	private ITableLabelProvider labelProvider;
	private ComponentType componentType;

	public MainTableLabelProvider() {
		super();
	}

	/**
	 * This constructor is used when the label provider is used depending on the
	 * component type selected by the user.
	 * 
	 * @param componentType
	 */
	public MainTableLabelProvider(ComponentType componentType) {
		super();
		this.componentType = componentType;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String returnValue;
		returnValue = labelProvider.getColumnText(element, columnIndex);
		if (returnValue == null) {
			return UIUtilities.BLANK_VALUE;
		} else {
			return returnValue;
		}
	}

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

	/**
	 * Method to create instances of the appropriate label provider depending on
	 * the metadata chosen in the navigator.
	 */
	public void setInput(TreeItemId ID) {
	    if (ID == TreeItemId.VIEWFOLDERCHILD) {
			labelProvider = new ViewTableLabelProvider();
		} else if (ID == TreeItemId.ENV) {
			labelProvider = new EnvironmentTableLabelProvider();
		} else if (ID == TreeItemId.CONTROL) {
			labelProvider = new ControlRecordTableLabelProvider();
		} else if (ID == TreeItemId.PHYSICALFILE) {
			labelProvider = new PhysicalFileTableLabelProvider();
		} else if (ID == TreeItemId.USER) {
			labelProvider = new UserTableLabelProvider();
		} else if (ID == TreeItemId.VIEWFOLDER) {
			labelProvider = new ViewFolderTableLabelProvider();
		} else if (ID == TreeItemId.GROUP) {
			labelProvider = new GroupTableLabelProvider();
		} else if (ID == TreeItemId.USEREXITROUTINE) {
			labelProvider = new UserExitRoutineTableLabelProvider();
		} else if (ID == TreeItemId.LOGICALRECORD) {
			labelProvider = new LogicalRecordTableLabelProvider();
		} else if (ID == TreeItemId.LOOKUP) {
			labelProvider = new LookupTableLabelProvider();
		} else if (ID == TreeItemId.LOGICALFILE) {
			labelProvider = new LogicalFileTableLabelProvider();
		} else if (ID == TreeItemId.GROUPUSER) {
			labelProvider = new UserTableLabelProvider();
        } else if (ID == TreeItemId.USERGROUP) {
            labelProvider = new GroupTableLabelProvider();
		} else if (ID == TreeItemId.GROUPENV) {
			labelProvider = new EnvironmentTableLabelProvider();
		}else if (ID == TreeItemId.ENVGROUP) {
			labelProvider = new GroupTableLabelProvider();
		}// depending on the component type selected, the respective Label
		// provider is used.
		else if (ID == TreeItemId.GROUPCOMPONENT) {
			if (componentType.equals(ComponentType.LogicalFile)) {
				labelProvider = new LogicalFileTableLabelProvider();
			} else if (componentType.equals(ComponentType.LogicalRecord)) {
				labelProvider = new LogicalRecordTableLabelProvider();
			} else if (componentType.equals(ComponentType.PhysicalFile)) {
				labelProvider = new PhysicalFileTableLabelProvider();
			} else if (componentType.equals(ComponentType.UserExitRoutine)) {
				labelProvider = new UserExitRoutineTableLabelProvider();
			} else if (componentType.equals(ComponentType.ViewFolder)) {
				labelProvider = new ViewFolderTableLabelProvider();
			}
		}

	}

	public void setInput() {
		if (componentType.equals(ComponentType.View)) {
			labelProvider = new ViewTableLabelProvider();
		} else if (componentType.equals(ComponentType.Environment)) {
			labelProvider = new EnvironmentTableLabelProvider();
		} else if (componentType.equals(ComponentType.ControlRecord)) {
			labelProvider = new ControlRecordTableLabelProvider();
		} else if (componentType.equals(ComponentType.PhysicalFile)) {
			labelProvider = new PhysicalFileTableLabelProvider();
		} else if (componentType.equals(ComponentType.ViewFolder)) {
			labelProvider = new ViewFolderTableLabelProvider();
		} else if (componentType.equals(ComponentType.Group)) {
			labelProvider = new GroupTableLabelProvider();
		} else if (componentType.equals(ComponentType.UserExitRoutine)) {
			labelProvider = new UserExitRoutineTableLabelProvider();
		} else if (componentType.equals(ComponentType.LogicalRecord)) {
			labelProvider = new LogicalRecordTableLabelProvider();
		} else if (componentType.equals(ComponentType.LookupPath)) {
			labelProvider = new LookupTableLabelProvider();
		} else if (componentType.equals(ComponentType.LogicalFile)) {
			labelProvider = new LogicalFileTableLabelProvider();
		} else if (componentType.equals(ComponentType.LogicalRecordField)) {
			labelProvider = new LRFieldTableLableProvider();
		}
	}

    @Override
    public Color getForeground(Object element, int columnIndex) {
        return null;
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            MetadataView metadataview = (MetadataView) (window.getActivePage().findView(MetadataView.ID));
            if (metadataview != null && metadataview.isFiltered()) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
            }
        }
        return null;
    }
}
