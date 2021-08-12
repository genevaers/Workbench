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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * This class represents a list of SAFRPersistentObjects. This is an extension
 * of java.util.ArrayList<T> so it supports the interface java.util.List<T>.
 * Instances of this class are created by substituting template symbol 'T' with
 * a subtype of SAFRPersistentObject. The class overrides the behaviour of
 * certain List methods with behaviour required for managing lists of persistent
 * objects within the SAFR application. For example, if such an object is to be
 * removed from a list, it should also be deleted from the database, so this
 * class overrides the behaviour of List<T>.remove to mark an object that is
 * already persistent for deletion, rather than physically removing it from the
 * list. This is necessary to that the data layer can erase the objects
 * persistent state when the object is stored.
 * 
 * @param <T>
 *            SAFRPersistentObject or one of its subclasses
 */
public class SAFRList<T extends SAFRPersistentObject> extends ArrayList<T>
		implements List<T> {

	private static final long serialVersionUID = 1; 

    public void removeAll() {
        T thatItem;
        Iterator<T> i = this.iterator();
        List<T> tmpList = new ArrayList<T>();
        while (i.hasNext()) {
            thatItem = i.next();
            if (thatItem.getPersistence() == SAFRPersistence.NEW) {
                // object is not yet persistent so remove it from list
                tmpList.add(thatItem);
            } else {
                thatItem.markDeleted();
            }
        }
        this.removeAll(tmpList);
    }
	
	/**
	 * Remove the specified object from the list. If it is found in the list and
	 * it is not yet persistent (it's a new object) it will be physically
	 * removed from the list. If it is already persistent it will remain in the
	 * list but it will be marked as deleted and the data layer will handle the
	 * actual deletion when the object is stored. In this case it is still
	 * logically considered 'removed', so true will be returned.
	 * 
	 * @param thisItem
	 *            the SAFRPersistentObject to be removed from the list
	 * @return true if the object is found in the list and removed, otherwise
	 *         false
	 */
	public boolean remove(SAFRPersistentObject thisItem) {
		SAFRPersistentObject thatItem;
		boolean found = false;
		boolean result = false;
		Iterator<T> i = this.iterator();
		while (!found && i.hasNext()) {
			thatItem = (SAFRPersistentObject) i.next();
			if (thisItem.equals(thatItem)) {
				if (thatItem.getPersistence() == SAFRPersistence.NEW) {
					// object is not yet persistent so remove it from list
					result = super.remove(thatItem);
					found = true;
				} else {
					thatItem.markDeleted();
					found = true;
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * Returns only the SAFRPersistentObjects that are not marked for deletion.
	 * That is, those that are new, old or modified.
	 * 
	 * @return a List of SAFRPersistentObjects
	 */
	public List<T> getActiveItems() {
		List<T> activeItems = new ArrayList<T>();
		T item;
		Iterator<T> i = this.iterator();
		while (i.hasNext()) {
			item = (T) i.next();
			if (item.getPersistence() != SAFRPersistence.DELETED) {
				activeItems.add(item);
			}
		}
		return activeItems;
	}

	public void flushDeletedItems() {
		List<T> tempList = new ArrayList<T>();
		for (T item : this) {
			if (item.getPersistence() == SAFRPersistence.DELETED) {
				tempList.add(item);
			}
		}
		this.removeAll(tempList);
	}
	
	// CQ10756 method added to resolve a problem in migration.
	/**
	 * Remove the specified item from the list without impacting its persistency
	 * in the database. That is, without setting its Persistence flag to DELETE
	 * like the remove method. Intended for use only by utilities like Migration
	 * which need to manipulate model content in-memory.
	 */
	public void flush(SAFRPersistentObject thisItem) {
		if (this.contains(thisItem)) {
			super.remove(thisItem);
		}
	}

}
