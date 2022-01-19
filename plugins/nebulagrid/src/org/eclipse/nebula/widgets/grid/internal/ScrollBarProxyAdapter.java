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


import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Adapts a normal scrollbar to the IScrollBar proxy.
 *
 * @author chris.gross@us.ibm.com
 * @since 2.0.0
 */
public class ScrollBarProxyAdapter implements IScrollBarProxy
{
    /**
     * Delegates to this scrollbar.
     */
    private ScrollBar scrollBar;

    /**
     * Contructs this adapter by delegating to the given scroll bar.
     * 
     * @param scrollBar delegate
     */
    public ScrollBarProxyAdapter(ScrollBar scrollBar)
    {
        super();
        this.scrollBar = scrollBar;
    }

    /** 
     * {@inheritDoc}
     */
    public int getIncrement()
    {
        return scrollBar.getIncrement();
    }

    /** 
     * {@inheritDoc}
     */
    public int getMaximum()
    {
        return scrollBar.getMaximum();
    }

    /** 
     * {@inheritDoc}
     */
    public int getMinimum()
    {
        return scrollBar.getMinimum();
    }

    /** 
     * {@inheritDoc}
     */
    public int getPageIncrement()
    {
        return scrollBar.getPageIncrement();
    }

    /** 
     * {@inheritDoc}
     */
    public int getSelection()
    {
        return scrollBar.getSelection();
    }

    /** 
     * {@inheritDoc}
     */
    public int getThumb()
    {
        return scrollBar.getThumb();
    }

    /** 
     * {@inheritDoc}
     */
    public boolean getVisible()
    {
        return scrollBar.getVisible();
    }

    /** 
     * {@inheritDoc}
     */
    public void setIncrement(int value)
    {
        scrollBar.setIncrement(value);
    }

    /** 
     * {@inheritDoc}
     */
    public void setMaximum(int value)
    {
        scrollBar.setMaximum(value);
    }

    /** 
     * {@inheritDoc}
     */
    public void setMinimum(int value)
    {
        scrollBar.setMinimum(value);
    }

    /** 
     * {@inheritDoc}
     */
    public void setPageIncrement(int value)
    {
        scrollBar.setPageIncrement(value);
    }

    /** 
     * {@inheritDoc}
     */
    public void setSelection(int selection)
    {
        scrollBar.setSelection(selection);
    }

    /** 
     * {@inheritDoc}
     */
    public void setThumb(int value)
    {
        scrollBar.setThumb(value);
    }

    /** 
     * {@inheritDoc}
     */
    public void setValues(int selection, int minimum, int maximum, int thumb, int increment, 
                          int pageIncrement)
    {
        scrollBar.setValues(selection, minimum, maximum, thumb, increment, pageIncrement);
    }

    /** 
     * {@inheritDoc}
     */
    public void setVisible(boolean visible)
    {
        scrollBar.setVisible(visible);
    }

    /** 
     * {@inheritDoc}
     */
    public void handleMouseWheel(Event e)
    {
        //do nothing        
    }

    /** 
     * {@inheritDoc}
     */
    public void addSelectionListener(SelectionListener listener)
    {
        scrollBar.addSelectionListener(listener);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeSelectionListener(SelectionListener listener)
    {
        scrollBar.removeSelectionListener(listener);
    }
}
