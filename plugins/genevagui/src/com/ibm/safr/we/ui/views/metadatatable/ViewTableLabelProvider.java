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
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBeanConv;
import com.ibm.safr.we.ui.utilities.UIUtilities;

/**
 * Label Provider for the View metadata. Contains method to provide the text for
 * each column of the table for the Environment metadata.
 * 
 */
public class ViewTableLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ViewQueryBeanConv) {
    		return getColumnTextConv(element, columnIndex);
		} else if (element instanceof ViewQueryBean) {
            return getColumnTextNormal(element, columnIndex);		    
		} else {
		    return null;
		}
	}

    protected String getColumnTextConv(Object element, int columnIndex) {
        ViewQueryBeanConv view = (ViewQueryBeanConv) element;
   
        switch (columnIndex) {
        case 0:
        	return Integer.toString(view.getId());
        case 1:
        	return view.getName();
        case 2:
        	String status = view.getStatus();
        	if (status == null) {
        		return "";
        	}
        	try {
        		return SAFRApplication.getSAFRFactory().getCodeSet(
        				CodeCategories.VIEWSTATUS).getCode(status).getDescription();
        	} catch (IllegalArgumentException e) {
        		//Absorb the message here and defer until the offending view is opened
        		return "";
        	}
        case 3:
            String phase = view.getPhase();
            if (phase == null) {
                return "";
            } else {
                return phase;
            }
        case 4:
        	String opFormat = view.getOutputFormat();
        	if (opFormat == null) {
        		return "";
        	} else {
        	    return opFormat;
        	}
        case 5:
        	String type = view.getAggrLevel();
        	if (type == null) {
        		return "";
        	} else {
        	    return type.equalsIgnoreCase("Summary") ? "Y" :  "N";
        	}
        case 6:
        	String createTime = UIUtilities.formatDate(view.getCreateTime());
        	return createTime;
        case 7:
        	return view.getCreateBy();
        case 8:
        	String modifyTime = UIUtilities.formatDate(view.getModifyTime());
        	return modifyTime;
        case 9:
        	return view.getModifyBy();
        case 10:
            String actTime = UIUtilities.formatDate(view.getActivatedTime());
            return actTime;
        case 11:
            return view.getActivatedBy();
        case 12:
            return view.getCompilerVersion();
        case 13:
            return view.getRights().getDesc();
        }
        return null;
    }

    private String getColumnTextNormal(Object element, int columnIndex) {
        ViewQueryBean view = (ViewQueryBean) element;
        
        switch (columnIndex) {
        case 0:
            return Integer.toString(view.getId());
        case 1:
            return view.getName();
        case 2:
            String status = view.getStatus();
            if (status == null) {
                return "";
            }
            try {
                return SAFRApplication.getSAFRFactory().getCodeSet(
                        CodeCategories.VIEWSTATUS).getCode(status).getDescription();
            } catch (IllegalArgumentException e) {
                //Absorb the message here and defer until the offending view is opened
                return "";
            }
        }
        return null;
    }

    
	public void addListener(ILabelProviderListener listener) {

	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {

	}

}
