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


import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.Section;

import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.utilities.FindReplaceComponent;
import com.ibm.safr.we.model.utilities.FindReplaceText;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;

public class FindReplaceMediator {
    private FindReplaceTextEditor editor;
    private FindReplaceCriteria criteria;
    private FindReplaceReplace replace;
    private FindReplaceResults results;
    
    public FindReplaceMediator(
        FindReplaceTextEditor editor,
        FindReplaceCriteria criteria,
        FindReplaceReplace replace,
        FindReplaceResults results) {
        this.editor = editor;
        this.criteria = criteria;
        this.replace = replace;
        this.results = results;
    }
    
    // calls on editor
    protected IWorkbenchPartSite getSite() {
        return editor.getSite();
    }

    protected void waitCursor() {
        getSite().getShell().setCursor(
            getSite().getShell().getDisplay()
                    .getSystemCursor(SWT.CURSOR_WAIT));
    }

    protected void normalCursor() {
        getSite().getShell().setCursor(null);        
    }

    protected void clearMessage() {
        editor.getMsgManager().removeAllMessages();
    }

    protected void setMessage(SAFRValidationException e) {
        editor.decorateEditor(e);
    }
    
    protected SAFRGUIToolkit getToolkit() {
        return editor.getToolkit();
    }
    
    protected void setPopupEnabled(boolean enabled) {
        editor.setPopupEnabled(enabled);
    }
    
    // calls on all
    protected void create(Composite parent) {
        criteria.create(parent);
        replace.create(parent);
        results.create(parent);
    }
    
    // calls on criteria
    protected void setFocusOnEnv() {
        criteria.setFocusOnEnv();
    }
    
    protected Section getCriteriaSection() {
        return criteria.getCriteriaSection();
    }


    protected EnvironmentQueryBean getCurrentEnv() {
        return criteria.getCurrentEnv();
    }

    protected EnvironmentQueryBean getComboEnv() {
        return criteria.getComboEnv();
    }
    
    protected String getFindText() {
        return criteria.getFindText();
    }
    
    public FindReplaceText getSearch() {
        return criteria.getSearch();
    }    
    
    protected Control getControlFromCriteria(Object property) {
        return criteria.getControlFromProperty(property);
    }

    // calls on replace
    protected void clearReplace() {
        replace.clearReplace();
        replace.disableReplace();
    }
    
    protected void disableReplace() {
        replace.disableReplace();
    }

    protected void enableReplace() {
        replace.enableReplace();
    }
    
    protected boolean isReplaceTextEmpty() {
        return replace.isReplaceTextEmpty();
    }
    
    protected Control getControlFromReplace(Object property) {
        return criteria.getControlFromProperty(property);
    }
        
    // calls on results
    public void clearResults() {
        results.clearResults();
        clearReplace();
    }
    
    protected void disableResults() {
        results.disableResults();
    }
    
    protected void setResults(List<FindReplaceComponent> res) {
        results.setResults(res);
    }
    
    protected void refreshResults() {
        results.refreshResults();
    }
    
    protected boolean isResultsSelected() {
        return results.isResultsSelected();
    }
    
    protected Object[] getCheckedResults() {
        return results.getCheckedResults();
    }
    
    protected Control getControlFromResult(Object property) {
        return results.getControlFromProperty(property);
    }
    
    protected FindReplaceComponent getCurrentSelection() {
        return results.getCurrentSelection();
    }

    
}
