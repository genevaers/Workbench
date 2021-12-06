/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    chris.gross@us.ibm.com - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.nebula.jface.gridviewer;

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


import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

/**
 * .
 */
public abstract class CheckEditingSupport extends EditingSupport
{
    /**
     * Checkbox editing support.
     * 
     * @param viewer column to add check box support for.
     */
    public CheckEditingSupport(ColumnViewer viewer)
    {
        super(viewer);
    }

    /** {@inheritDoc} */
    protected boolean canEdit(Object element)
    {
        return false;
    }

    /** {@inheritDoc} */
    protected CellEditor getCellEditor(Object element)
    {
        return null;
    }

    /** {@inheritDoc} */
    protected Object getValue(Object element)
    {
        return null;
    }

    /** {@inheritDoc} */
    public abstract void setValue(Object element, Object value);
}
