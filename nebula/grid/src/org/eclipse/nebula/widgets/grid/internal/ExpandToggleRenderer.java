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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class ExpandToggleRenderer extends AbstractRenderer
{

    /**
     * 
     */
    public ExpandToggleRenderer()
    {
        super();
        setSize(11, 9);
    }

    /**
     * {@inheritDoc}
     */
    public void paint(GC gc, Object value)
    {
        Color innerColor = null;
        Color outerColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);

        if (isHover())
        {
            innerColor = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        }
        else
        {
            innerColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        }

        if (isExpanded())
        {
            drawLeftPointingLine(gc, innerColor, outerColor, 0);
            drawLeftPointingLine(gc, innerColor, outerColor, 5);
        }
        else
        {
            drawRightPointingLine(gc, innerColor, outerColor, 0);
            drawRightPointingLine(gc, innerColor, outerColor, 5);
        }

    }

    private void drawRightPointingLine(GC gc, Color innerColor, Color outerColor, int xOffset)
    {
        gc.setForeground(outerColor);
        gc.drawLine(getBounds().x + 1 + xOffset, getBounds().y, getBounds().x + 5 + xOffset,
                    getBounds().y + 4);
        gc.drawLine(getBounds().x + 4 + xOffset, getBounds().y + 5, getBounds().x + 1 + xOffset,
                    getBounds().y + 8);
        gc.drawPoint(getBounds().x + xOffset, getBounds().y + 7);
        gc.drawLine(getBounds().x + xOffset, getBounds().y + 6, getBounds().x + 2 + xOffset,
                    getBounds().y + 4);
        gc.drawLine(getBounds().x + 1 + xOffset, getBounds().y + 3, getBounds().x + xOffset,
                    getBounds().y + 2);
        gc.drawPoint(getBounds().x + xOffset, getBounds().y + 1);

        gc.setForeground(innerColor);
        gc.drawLine(getBounds().x + 1 + xOffset, getBounds().y + 1, getBounds().x + 4 + xOffset,
                    getBounds().y + 4);
        gc.drawLine(getBounds().x + 1 + xOffset, getBounds().y + 2, getBounds().x + 3 + xOffset,
                    getBounds().y + 4);
        gc.drawLine(getBounds().x + 3 + xOffset, getBounds().y + 5, getBounds().x + 1 + xOffset,
                    getBounds().y + 7);
        gc.drawLine(getBounds().x + 2 + xOffset, getBounds().y + 5, getBounds().x + 1 + xOffset,
                    getBounds().y + 6);
    }

    private void drawLeftPointingLine(GC gc, Color innerColor, Color outerColor, int xOffset)
    {
        gc.setForeground(outerColor);
        gc.drawLine(getBounds().x + xOffset, getBounds().y + 4, getBounds().x + 4 + xOffset,
                    getBounds().y);
        gc.drawPoint(getBounds().x + 5 + xOffset, getBounds().y + 1);
        gc.drawLine(getBounds().x + 5 + xOffset, getBounds().y + 2, getBounds().x + 3 + xOffset,
                    getBounds().y + 4);
        gc.drawPoint(getBounds().x + 4 + xOffset, getBounds().y + 5);
        gc.drawLine(getBounds().x + 5 + xOffset, getBounds().y + 6, getBounds().x + 5 + xOffset,
                    getBounds().y + 7);
        gc.drawLine(getBounds().x + 4 + xOffset, getBounds().y + 8, getBounds().x + 1 + xOffset,
                    getBounds().y + 5);

        gc.setForeground(innerColor);
        gc.drawLine(getBounds().x + 1 + xOffset, getBounds().y + 4, getBounds().x + 4 + xOffset,
                    getBounds().y + 1);
        gc.drawLine(getBounds().x + 2 + xOffset, getBounds().y + 4, getBounds().x + 4 + xOffset,
                    getBounds().y + 2);
        gc.drawLine(getBounds().x + 2 + xOffset, getBounds().y + 5, getBounds().x + 4 + xOffset,
                    getBounds().y + 7);
        gc.drawLine(getBounds().x + 2 + xOffset, getBounds().y + 4, getBounds().x + 4 + xOffset,
                    getBounds().y + 6);
    }

    /**
     * {@inheritDoc}
     */
    public Point computeSize(GC gc, int wHint, int hHint, Object value)
    {
        return new Point(11, 9);
    }

}
