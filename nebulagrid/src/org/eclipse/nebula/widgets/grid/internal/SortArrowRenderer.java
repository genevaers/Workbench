/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    chris.gross@us.ibm.com - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.grid.internal;

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


import org.eclipse.nebula.widgets.grid.AbstractRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * The column header sort arrow renderer.
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class SortArrowRenderer extends AbstractRenderer
{

    /**
     * Default constructor.
     */
    public SortArrowRenderer()
    {
        super();

        setSize(7, 4);
    }

    /** 
     * {@inheritDoc}
     */
    public void paint(GC gc, Object value)
    {
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        if (isSelected())
        {
            gc
                .drawLine(getBounds().x + 0, getBounds().y + 0, getBounds().x + 6,
                          getBounds().y + 00);
            gc.drawLine(getBounds().x + 1, getBounds().y + 01, getBounds().x + 5,
                        getBounds().y + 01);
            gc.drawLine(getBounds().x + 2, getBounds().y + 02, getBounds().x + 4,
                        getBounds().y + 02);
            gc.drawPoint(getBounds().x + 3, getBounds().y + 03);
        }
        else
        {
            gc.drawPoint(getBounds().x + 3, getBounds().y + 0);
            gc.drawLine(getBounds().x + 2, getBounds().y + 01, getBounds().x + 4,
                        getBounds().y + 01);
            gc.drawLine(getBounds().x + 1, getBounds().y + 02, getBounds().x + 5,
                        getBounds().y + 02);
            gc.drawLine(getBounds().x + 0, getBounds().y + 03, getBounds().x + 6,
                        getBounds().y + 03);
        }

    }

    /** 
     * {@inheritDoc}
     */
    public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        return new Point(7, 4);
    }

}
