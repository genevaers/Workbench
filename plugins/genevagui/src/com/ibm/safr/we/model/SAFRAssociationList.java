package com.ibm.safr.we.model;

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


import java.util.Iterator;

import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.model.associations.Association;

/**
 * This class represents a list of Associations. It specializes the SAFRList
 * class by overriding the add method of SAFRList to implement the behaviour
 * required for lists of Association objects (see add method comments).
 * <p>
 * This class is intended to be used within the SAFR object model
 * implementation. It is not intended to be specified on the SAFR model API.
 * That is, it should not be specified as an argument type or a return value on
 * any of the public methods of the SAFR model classes. Only its superclass,
 * SAFRList, should be specified on the SAFR object model API.
 * 
 * @param <T>
 *            Association or one of its subclasses
 */
public class SAFRAssociationList<T extends Association> extends SAFRList<T> {

	private static final long serialVersionUID = 1; // JAK: TODO proper value?

	/**
	 * Adds the specified NEW item to the list. If an equivalent object already
	 * exists in the list and its persistency is DELETED, that object's
	 * persistency will be changed to OLD and the NEW object specified as the
	 * method argument will be discarded.
	 */
	public boolean add(T thisItem) {
		// first search if this association is already present in the list as
		// DELETED.
		T thatItem;
		boolean found = false;
		boolean result = false;
		Iterator<T> i = this.iterator();
		while (!found && i.hasNext()) {
			thatItem = i.next();
			if (thisItem.equals(thatItem)) {
				found = true;
				result = true;
				// if item to be added is NEW and the item already found in
				// DELETED, then change the state of that item to OLD.
				if (thisItem.getPersistence() == SAFRPersistence.NEW
						&& thatItem.getPersistence() == SAFRPersistence.DELETED) {
					thatItem.setPersistence(SAFRPersistence.OLD);
				}
			}
		}
		if (!found) {
			result = super.add(thisItem);
		}
		return result;
	}

    public T get(T thisItem) {
        T thatItem;
        Iterator<T> i = this.iterator();
        while (i.hasNext()) {
            thatItem = i.next();
            if (thisItem.equals(thatItem)) {
                return thatItem;
            }
        }
        return null;
    }
	
}
