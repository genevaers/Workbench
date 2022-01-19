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


import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * An implementation of AbstractPreferences for storing preferences 
 * in two files one base and one override. Any settings in the 
 * override file will override settings in the base file. 
 * Modification of settings will only affect the override file.
 * Implements the standard Java Preferences API.
 * 
 * To use
 * 
 * System.setProperty("java.util.prefs.PreferencesFactory", 
 *                    OverridePreferencesFactory.class.getName());
 * System.setProperty(OverridePreferencesFactory.BASE_PROP, 
 *                    <base file location>); 
 * System.setProperty(OverridePreferencesFactory.OVER_PROP, 
 *                    <override file location>);
 * 
 * Will make Java use OverridePreferences implementation for Preferences
 * 
 * Then 
 *         Preferences p = Preferences.userRoot();
 *         
 * To access preferences.
 * 
 */
public class OverridePreferences extends AbstractPreferences {

    // assert one of these is always not null
    File baseFile;
    File overFile;
    FilePreferences base;
    FilePreferences over;
    FilePreferences overroot;
    
    private Map<String, OverridePreferences> children;    
    
    public OverridePreferences(
            File baseFile,
            File overFile) {
        super(null, "");
        this.baseFile = baseFile;
        this.overFile = overFile;
        base = new FilePreferences(null,"",baseFile);
        over = new FilePreferences(null,"",overFile);
        overroot = over;
        children = new TreeMap<String, OverridePreferences>();
    }

    protected OverridePreferences(
            AbstractPreferences parent, 
            String name, 
            FilePreferences base,
            FilePreferences over,
            FilePreferences overroot) {
        super(parent, name);
        this.base = base;
        this.over = over;
        this.overroot = overroot;
        children = new TreeMap<String, OverridePreferences>();
    }
    
    public Preferences getBasePrefs()  {
        return base;
    }
    
    public Preferences getOverPrefs()  {
        return over;
    }
    
    public boolean isNodeinBase(String path)  {
        if (base == null) {
            return false;
        }
        try {
            return base.nodeExists(path);
        } catch (BackingStoreException e) {
            // Never occurs all load occurs in sync
            return false;
        }
    }
    
    public boolean isNodeinOver(String path)  {
        if (over == null) {
            return false;
        }        
        try {
            return over.nodeExists(path);
        } catch (BackingStoreException e) {
            // Never occurs all load occurs in sync
            return false;
        }
    }
    
    @Override
    protected void putSpi(String key, String value) {
        // only put values in the override preferences
        if (over == null) {
            // generate a node in over
            over = (FilePreferences)overroot.node(this.absolutePath());
        }
        over.put(key, value);            
    }

    @Override
    protected String getSpi(String key) {
        // return overidden value if exists
        if (over == null || over.getSpi(key) == null) {
            return base.getSpi(key);
        }
        else
        {
            return over.getSpi(key);
        }
    }

    @Override
    protected void removeSpi(String key) {
        if (over != null) {
            over.removeSpi(key);            
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        if (over != null) {
            ((OverridePreferences)parent()).children.remove(this.name()); 
            over.removeNode();                
            over = null;
        }
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        Set<String> keys = new HashSet<String>();
        if (base != null) {
            keys.addAll(Arrays.asList(base.keysSpi()));            
        }
        if (over != null) {
            keys.addAll(Arrays.asList(over.keysSpi()));            
        }
        return keys.toArray(new String[keys.size()]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        Set<String> children = new HashSet<String>();
        if (base != null) {
            children.addAll(Arrays.asList(base.childrenNamesSpi()));            
        }
        if (over != null) {
            children.addAll(Arrays.asList(over.childrenNamesSpi()));            
        }
        return children.toArray(new String[children.size()]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        OverridePreferences child = children.get(name);
        if (child == null || child.isRemoved()) {
           child = new OverridePreferences(this, name, null, null, overroot);
            children.put(name, child);
        }
        return child;
    }

    @Override
    public void sync() throws BackingStoreException {
        
        clear();
        for (String subNodeName : childrenNames()) {
            OverridePreferences subnode = (OverridePreferences)this.getChild(subNodeName);
            subnode.removeNode();
        }
        
		// sync will only ever be called from the root node '/'
        // so base and over are set        
        base = new FilePreferences(null,"",baseFile);
        over = new FilePreferences(null,"",overFile);
        overroot = over;
        children = new TreeMap<String, OverridePreferences>();
        
        base.sync();
        syncBaseR(base);
        over.sync();
        syncOverR(over);        
    }
    
    private OverridePreferences childSpiSync(String name) {
        OverridePreferences child = children.get(name);
        if (child == null) {
            child = new OverridePreferences(this, name, (FilePreferences)null, (FilePreferences)null, overroot);
            children.put(name, child);
        }
        return child;
    }
    
    private void syncBaseR(FilePreferences base) throws BackingStoreException {
        
        this.base = base;
        
        for (String name : base.childrenNames()) {
            OverridePreferences subnode = childSpiSync(name);
            FilePreferences subbase = (FilePreferences)base.node(name);
            subnode.syncBaseR(subbase);
        }
    }
    
    private void syncOverR(FilePreferences over) throws BackingStoreException {
        
        this.over = over;
        
        for (String name : over.childrenNames()) {
            OverridePreferences subnode = childSpiSync(name);
            FilePreferences subover = (FilePreferences)over.node(name);
            subnode.syncOverR(subover);
        }
    }
    
    public void removeAll(String key) {
        this.over.remove(key);
        this.base.remove(key);
    }
    
    @Override
    public void flush() throws BackingStoreException {
        over.flush();
    }
    
    @Override
    protected void syncSpi() throws BackingStoreException {
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
    }    
}
