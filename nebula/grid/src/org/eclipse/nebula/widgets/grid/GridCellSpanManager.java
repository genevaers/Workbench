/*******************************************************************************
 * Copyright (c) 2009 Claes Rosell
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Claes Rosell<claes.rosell@solme.se>    - initial API and implementation
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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Rectangle;

class GridCellSpanManager {
	List listOfCellSpanRectangles = new ArrayList();
	Rectangle lastUsedCellSpanRectangle = null;

	protected void addCellSpanInfo(int colIndex, int rowIndex, int colSpan,
			int rowSpan) {
		Rectangle rect = new Rectangle(colIndex, rowIndex, colSpan + 1,
				rowSpan + 1);
		this.listOfCellSpanRectangles.add(rect);
	}

	private Rectangle findSpanRectangle(int columnIndex, int rowIndex) {
		Iterator iter = listOfCellSpanRectangles.iterator();
		while (iter.hasNext()) {
			Rectangle cellSpanRectangle = (Rectangle) iter.next();
			if (cellSpanRectangle.contains(columnIndex, rowIndex)) {
				return cellSpanRectangle;
			}
		}
		return null;
	}

	protected boolean skipCell(int columnIndex, int rowIndex) {
		this.lastUsedCellSpanRectangle = this.findSpanRectangle(columnIndex,
				rowIndex);
		return this.lastUsedCellSpanRectangle != null;
	}

	protected void consumeCell(int columnIndex, int rowIndex) {
		Rectangle rectangleToConsume = null;

		if (this.lastUsedCellSpanRectangle != null
				&& this.lastUsedCellSpanRectangle.contains(columnIndex,
						rowIndex)) {
			rectangleToConsume = this.lastUsedCellSpanRectangle;
		} else {
			rectangleToConsume = this.findSpanRectangle(columnIndex, rowIndex);
		}

		if (rectangleToConsume != null) {
			if (columnIndex >= rectangleToConsume.x
					+ (rectangleToConsume.width - 1)
					&& rowIndex >= (rectangleToConsume.y
							+ rectangleToConsume.height - 1)) {
				this.listOfCellSpanRectangles.remove(rectangleToConsume);
			}
		}
	}
}
