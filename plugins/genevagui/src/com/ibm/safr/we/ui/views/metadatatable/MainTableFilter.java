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


import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.ibm.safr.we.model.query.NumericIdQueryBean;
import com.ibm.safr.we.model.query.UserQueryBean;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;

public class MainTableFilter extends ViewerFilter {

    private boolean byName = true;
    private String searchText = null;
    
    public void setByName(boolean byName) {
        this.byName = byName;
    }

    public void setSearchText(String nameFilter) {
        this.searchText = nameFilter;
    }

    @Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

        // filter on name
        if (searchText != null && !searchText.trim().isEmpty()) {
            String upSearch = searchText.toUpperCase();
            if (byName) {
                if (element instanceof NumericIdQueryBean) {
                    String name = ((NumericIdQueryBean) element).getName().toUpperCase();
                    if (!name.contains(upSearch)) {
                        return false;
                    }
                }
                else if (element instanceof UserQueryBean) {
                    String name = ((UserQueryBean) element).getId().toUpperCase();
                    if (!name.contains(upSearch)) {
                        return false;
                    }
                }
            }
            else {
                if (element instanceof NumericIdQueryBean) {
                    if (!((NumericIdQueryBean) element).getId().toString().contains(searchText)) {
                        return false;
                    }
                }
                else if (element instanceof UserQueryBean) {
                    if (!((UserQueryBean) element).getId().contains(searchText)) {
                        return false;
                    }
                }                
            }
        }
        
        // filter out ALL_VIEWS
        if (element instanceof ViewFolderQueryBean && ((ViewFolderQueryBean) element).getId().equals(0)) {
            return false;            
        }
        
        
        return true;
	}

    public boolean isFiltered() {
        if (searchText == null || searchText.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

}
