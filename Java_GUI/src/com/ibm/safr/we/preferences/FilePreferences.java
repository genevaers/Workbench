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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import com.ibm.safr.we.SAFRUtilities;

/**
 * An implementation of AbstractPreferences for storing 
 * preferences in a file. Implements the standard Java Preferences API.
 */
public class FilePreferences extends AbstractPreferences {

    
    private Map<String, String> root;
    private Map<String, FilePreferences> children;
    private File file;

    public FilePreferences(AbstractPreferences parent, String name, File file) {
        super(parent, name);
        root = new TreeMap<String, String>();
        children = new TreeMap<String, FilePreferences>();
        this.file = file;
    }

    @Override
    protected void putSpi(String key, String value) {
        root.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return root.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        root.remove(key);
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        ((FilePreferences)parent()).children.remove(this.name());
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        return root.keySet().toArray(new String[root.keySet().size()]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        return children.keySet().toArray(new String[children.keySet().size()]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        FilePreferences child = children.get(name);
        if (child == null || child.isRemoved()) {
            child = new FilePreferences(this, name, file);
            children.put(name, child);
        }
        return child;
    }

    @Override
    public void sync() throws BackingStoreException {

        if (!file.exists()) {
            clearPrefsR(new HashSet<String>());
            return;
        }

        Properties p = new Properties();
                
        try {
            FileInputStream st = new FileInputStream(file);
            p.load(st);
            st.close();
            syncSpiR(p);
            
            // remove elements that no longer exist
            Set<String> keys = new HashSet<String>();            
            final Enumeration<?> pnen = p.propertyNames();
            while (pnen.hasMoreElements()) {
                String propKey = (String) pnen.nextElement();                
                keys.add(propKey);
            }
            clearPrefsR(keys);            
            
        } catch (IOException e) {
            throw new BackingStoreException(e);
        }
    }

    private void syncSpiR(Properties p) throws BackingStoreException {
        
        StringBuilder sb = new StringBuilder();
        getPath(sb);
        String path = sb.toString();

        // load elements from file
        final Enumeration<?> pnen = p.propertyNames();
        while (pnen.hasMoreElements()) {
            String propKey = (String) pnen.nextElement();
            if (propKey.startsWith(path)) {
                String subKey = propKey.substring(path.length());
                
                if (subKey.indexOf('/') == -1) {
                    root.put(subKey, p.getProperty(propKey).trim());
                }
                else {
                    String subNodeName = subKey.substring(0, subKey.indexOf("/"));
                    FilePreferences subNode = (FilePreferences)childSpi(subNodeName);
                    subNode.syncSpiR(p);
                }
            }
        }
        
    }
    
    private void clearPrefsR(Set<String> keys) throws BackingStoreException {
        
        // remove preference values
        StringBuilder sb = new StringBuilder();
        getPath(sb);
        String path = sb.toString();        
        for (String key : this.keys()) {
            if (!keys.contains(path + key)) {
                remove(key);
            }
        }
        
        // remove child nodes
        for (String nodeName : childrenNames()) {
            FilePreferences subnode = (FilePreferences)this.getChild(nodeName);
            String nodepath = subnode.absolutePath().substring(1);
            boolean found = false;
            for (String key : keys) {
                if (key.indexOf(nodepath) == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                subnode.removeNode();
            }
            else {
                subnode.clearPrefsR(keys);
            }
        }   
    }
    
    @Override
    public void flush() throws BackingStoreException {
        
        if (this.isRemoved())
            return;
        
        Map<String, String> p = new TreeMap<String, String>();  
        
        try {
            loadPropsR(p);
            FileWriter st = new FileWriter(file);
            for (Entry<String,String> ent : p.entrySet()) {
                // escape \ in values
                String value = ent.getValue().replace("\\", "\\\\");
                st.write(ent.getKey()+"="+value+SAFRUtilities.LINEBREAK);
            }
            st.close();
            
        } catch (IOException e) {
            throw new BackingStoreException(e);
        }
    }

    private void loadPropsR(Map<String, String> p) {
        
        if (this.isRemoved())
            return;

        StringBuilder sb = new StringBuilder();
        getPath(sb);
        String path = sb.toString();
        
        // add local values
        for (Entry<String, String> ent : root.entrySet()) {
            p.put(path + ent.getKey(), ent.getValue());
        }
        
        // add sub node values 
        for (Entry<String, FilePreferences> ent : children.entrySet()) {
            ent.getValue().loadPropsR(p);
        }        
    }
    
    protected void getPath(StringBuilder sb) {
        final FilePreferences parent = (FilePreferences) parent();
        if (parent == null)
            return;

        parent.getPath(sb);
        sb.append(name()).append('/');
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
    }
}
