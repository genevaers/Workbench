package com.ibm.safr.we;

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


import java.util.Collection;
import java.util.List;

import com.ibm.safr.we.model.SAFRList;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * This class represents an immutable list of SAFRPersistentObjects. It
 * specializes the SAFRList class by overriding all of the mutator methods
 * (add/remove/etc) to throw the UnsupportedOperationException. It also
 * overrides the methods which return sublists of this list so that they return
 * a SAFRImmutableList as the concrete List object.
 * 
 * @param <T>
 *            SAFRPersistentObject or one of its subclasses
 */
public class SAFRImmutableList<T extends SAFRPersistentObject> extends
		SAFRList<T> {

	private static final long serialVersionUID = 1; // JAK: TODO proper value?

	public SAFRImmutableList(Collection<T> c) {
		super.addAll(c);
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	};

	/**
	 * @throws UnsupportedOperationException
	 */
	public boolean add(T o) {
		throw new UnsupportedOperationException();
	};

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void flushDeletedItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Overrides the inherited behaviour to return a SAFRImmutableList object as
	 * the concrete List object. This list still contains only the
	 * SAFRPersistentObjects that are not marked for deletion (those that are
	 * new, old or modified), but the list cannot be modified.
	 */
	@Override
	public List<T> getActiveItems() {
		SAFRImmutableList<T> list = new SAFRImmutableList<T>(super
				.getActiveItems());
		return list;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean remove(SAFRPersistentObject thisItem) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	};

	/**
	 * Overrides the inherited behaviour to return a SAFRImmutableList as the
	 * concrete List object. This list cannot be modified
	 */
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		SAFRImmutableList<T> list = new SAFRImmutableList<T>(
				(Collection<T>) super.subList(fromIndex, toIndex));
		return list;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void trimToSize() {
		throw new UnsupportedOperationException();
	}

}
