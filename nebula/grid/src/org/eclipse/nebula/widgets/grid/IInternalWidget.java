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
package org.eclipse.nebula.widgets.grid;

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


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * <p>
 * NOTE:  THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.  THIS IS A PRE-RELEASE ALPHA 
 * VERSION.  USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p> 
 * 
 * TODO fill in.
 * 
 * @author chris.gross@us.ibm.com
 */
public interface IInternalWidget extends IRenderer
{
    // CSOFF: Magic Number

    // Event type constants
    /** Hover State. */
    int MouseMove = SWT.MouseMove;

    /** Mouse down state. */
    int LeftMouseButtonDown = SWT.MouseDown;

    /**
     * Mechanism used to notify the light weight widgets that an event occurred
     * that it might be interested in.
     * 
     * @param event Event type.
     * @param point Location of event.
     * @param value New value.
     * @return widget handled the event.
     */
    boolean notify(int event, Point point, Object value);

    /**
     * Returns the hover detail object. This detail is used by the renderer to
     * determine which part or piece of the rendered image is hovered over.
     * 
     * @return string identifying which part of the image is being hovered over.
     */
    String getHoverDetail();

    /**
     * Sets a string object that represents which part of the rendered image is currently under the
     * mouse pointer.
     * 
     * @param detail identifying string.
     */
    void setHoverDetail(String detail);
}
