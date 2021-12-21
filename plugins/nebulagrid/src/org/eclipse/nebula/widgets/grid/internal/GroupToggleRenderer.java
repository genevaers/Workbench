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
 * The renderer for the expand/collapse toggle on a column group header.
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class GroupToggleRenderer extends AbstractRenderer
{

    /** 
     * {@inheritDoc}
     */
    public void paint(GC gc, Object value)
    {

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        gc.fillArc(getBounds().x, getBounds().y, 8, getBounds().height + -1, 90, 180);
        gc.drawArc(getBounds().x, getBounds().y, 8, getBounds().height + -1, 90, 180);

        gc.fillRectangle(getBounds().x + 4, getBounds().y, getBounds().width - 4,
                         getBounds().height);

        int yMid = ((getBounds().height - 1) / 2);

        if (isHover())
        {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        }
        else
        {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        }

        if (isExpanded())
        {
            gc.drawLine(getBounds().x + 5, getBounds().y + yMid, getBounds().x + 8, getBounds().y
                                                                                    + yMid - 3);
            gc.drawLine(getBounds().x + 6, getBounds().y + yMid, getBounds().x + 8, getBounds().y
                                                                                    + yMid - 2);

            gc.drawLine(getBounds().x + 5, getBounds().y + yMid, getBounds().x + 8, getBounds().y
                                                                                    + yMid + 3);
            gc.drawLine(getBounds().x + 6, getBounds().y + yMid, getBounds().x + 8, getBounds().y
                                                                                    + yMid + 2);

            gc.drawLine(getBounds().x + 9, getBounds().y + yMid, getBounds().x + 12, getBounds().y
                                                                                     + yMid - 3);
            gc.drawLine(getBounds().x + 10, getBounds().y + yMid, getBounds().x + 12, getBounds().y
                                                                                      + yMid - 2);

            gc.drawLine(getBounds().x + 9, getBounds().y + yMid, getBounds().x + 12, getBounds().y
                                                                                     + yMid + 3);
            gc.drawLine(getBounds().x + 10, getBounds().y + yMid, getBounds().x + 12, getBounds().y
                                                                                      + yMid + 2);
        }
        else
        {
            gc.drawLine(getBounds().x + getBounds().width - 5, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 8, getBounds().y + yMid - 3);
            gc.drawLine(getBounds().x + getBounds().width - 6, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 8, getBounds().y + yMid - 2);

            gc.drawLine(getBounds().x + getBounds().width - 5, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 8, getBounds().y + yMid + 3);
            gc.drawLine(getBounds().x + getBounds().width - 6, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 8, getBounds().y + yMid + 2);

            gc.drawLine(getBounds().x + getBounds().width - 9, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 12, getBounds().y + yMid - 3);
            gc.drawLine(getBounds().x + getBounds().width - 10, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 12, getBounds().y + yMid - 2);

            gc.drawLine(getBounds().x + getBounds().width - 9, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 12, getBounds().y + yMid + 3);
            gc.drawLine(getBounds().x + getBounds().width - 10, getBounds().y + yMid,
                        getBounds().x + getBounds().width - 12, getBounds().y + yMid + 2);
        }

    }

    /** 
     * {@inheritDoc}
     */
    public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        return new Point(0, 0);
    }
}
