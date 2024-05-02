package com.ibm.safr.we.preferences;

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


import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.exceptions.SAFRFatalException;

public class SortOrderPrefs {

    public enum Order {
        ASCENDING("Ascending"), DESCENDING("Descending");
        
        private String name;
        
        private Order(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public static Order getByName(String name) {
            Order res = null;
            for (Order ord : Order.values()) {
                if (ord.getName().equals(name)) {
                    res = ord;
                    break;
                }
            }
            return res;
        }
    }
    
    private String category;
    private String tableName;
    private Integer column;
    private Order order;
    
    // constructor for storage
    public SortOrderPrefs(String category, String tableName, Integer column, Order order) {
        super();
        this.category = category;
        this.tableName = tableName;
        this.column = column;
        this.order = order;
    }

    // constructor for loading
    public SortOrderPrefs(String category, String tableName) {
        super();
        this.category = category;
        this.tableName = tableName;
    }    

    public void store() {
        try {
            Preferences sortOrder = SAFRPreferences.getSAFRPreferences().node(UserPreferencesNodes.SORT);
            Preferences catpref = sortOrder.node(category);
            
            // get node for this table        
            Preferences tableNode = catpref.node(tableName);
            tableNode.put(UserPreferencesNodes.SORT_COLUMN, column.toString());
            tableNode.put(UserPreferencesNodes.SORT_ORDER, order.getName());
            
            SAFRPreferences.getSAFRPreferences().flush();
            SAFRPreferences.getSAFRPreferences().sync();
            
        } catch (BackingStoreException e) {
            throw new SAFRFatalException("Unable to Store Preferences " + e.getMessage());
        }        
    }
    
    public boolean load() {
        Preferences sortOrder = SAFRPreferences.getSAFRPreferences().node(UserPreferencesNodes.SORT);
        Preferences catpref = sortOrder.node(category);
        
        // get node for this table        
        Preferences tableNode = catpref.node(tableName);
        
        String colStr = tableNode.get(UserPreferencesNodes.SORT_COLUMN, null);
        if (colStr == null) {
            column = null;
        }
        else {
            column = Integer.valueOf(colStr);            
        }
        String ordStr = tableNode.get(UserPreferencesNodes.SORT_ORDER, null);
        if (ordStr == null) {
            order = null;
        }
        else {
            order = Order.getByName(ordStr);
        }
        
        if (column == null || order  == null) {
            return false;
        }
        else {
            return true;
        }
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    
}
