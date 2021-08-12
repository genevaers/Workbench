package com.ibm.safr.we.ui.editors.find;

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


import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordFieldQueryBean;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class FindReplaceCriteriaMediator {

    private FindReplaceMediator extMediator;
    private FindReplaceCriteria criteria;
    private FindReplaceCriteriaFindBy findBy;
    private FindReplaceCriteriaViewsIn viewsIn;
    
    protected FindReplaceCriteriaMediator(
        FindReplaceMediator extMediator,
        FindReplaceCriteria criteria,
        FindReplaceCriteriaFindBy findBy,
        FindReplaceCriteriaViewsIn viewsIn) {
        this.extMediator = extMediator;
        this.criteria = criteria;
        this.findBy = findBy;
        this.viewsIn = viewsIn;        
    }

    // calls on external mediator
    protected SAFRGUIToolkit getToolkit() {
        return extMediator.getToolkit();
    }
    
    protected void clearResults() {
        criteria.clearFind();
        extMediator.clearResults();
    }

    protected IWorkbenchPartSite getSite() {
        return extMediator.getSite();
    }
    
    protected void waitCursor() {
        extMediator.waitCursor();
    }

    protected void normalCursor() {
        extMediator.normalCursor();
    }
    
    protected void clearMessage() {
        extMediator.clearMessage();        
    }
    
    // calls on criteria
    protected void setSearchButtonState(boolean state) {
        criteria.setSearchButtonState(state);
    }
    
    protected EnvironmentQueryBean getCurentEnv() {
        return criteria.getCurrentEnv();
    }
    
    // calls on find by
    protected Control getControlFromFindBy(Object property) {
        return findBy.getControlFromProperty(property);
    }
    
    protected boolean getSearchButtonState() {
        return findBy.getSearchButtonState(); 
    }
    
    protected boolean isFindByText() {
        return findBy.isFindByText();
    }
    
    protected ComponentType getComponentType() {
        return findBy.getComponentType();
    }

    protected EnvironmentalQueryBean getCurComp() {
        return findBy.getCurComp();
    }
    
    protected LogicalRecordFieldQueryBean getCurField() {
        return findBy.getCurField();
    }
    
    protected String getFindText() {
        return findBy.getFindText();        
    }
    
    protected boolean isMatchingCase() {
        return findBy.isMatchingCase();
    }

    protected boolean isPatternMatch() {
        return findBy.isPatternMatch();
    }
    
    protected Control getBottomFindBy() {
        return findBy.getBottomFindBy();
    }
    
    // calls on views in
    protected Control getControlFromViewsIn(Object property) {
        return viewsIn.getControlFromProperty(property);
    }
    
    protected Control getBottomViewsIn() {
        return viewsIn.getBottomViewsIn();
    }
    
    protected void updateLabels() {
        viewsIn.updateLabels();
    }
    
    protected void clearViewsList() {
        viewsIn.clearViewsList();
    }

    protected List<EnvironmentalQueryBean> getViewsList() {
        return viewsIn.getViewsList();
    }
    
    protected SearchViewsIn getSearchViewsIn() {
        return viewsIn.getSearchViewsIn();
    }

    protected SearchCriteria getSearchCriteria() {
        return viewsIn.getSearchCriteria();
    }

    protected SearchPeriod getSearchPeriod() {
        return viewsIn.getSearchPeriod();
    }
    
    protected Date getDateToSearch() {
        return viewsIn.getDateToSearch();
    }
    
    protected boolean isRefineSearchViews() {
        return viewsIn.isRefineSearchViews();
    }
    
    // misc
    protected void create(Composite body, Control top) {
        findBy.create(body, top);
        viewsIn.create(body, getBottomFindBy());
    }
    
}
