package com.ibm.safr.we.ui.editors;

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


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.model.Environment;

public class EnvironmentEditorInput implements IEditorInput {

	private Environment environment;
	private EditRights editRights;

	public EnvironmentEditorInput(Environment env, EditRights editRights) {
		environment = env;
		this.editRights = editRights;
	}

	public EditRights getEditRights() {
		return editRights;
	}

	public void setEditRights(EditRights editRights) {
		this.editRights = editRights;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "Environment[" + environment.getId() + "]";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Environment[" + environment.getId() + "]";
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		if (!(obj instanceof EnvironmentEditorInput))
			return false;
		EnvironmentEditorInput other = (EnvironmentEditorInput) obj;
		return (environment.getId().equals(other.getEnvironment().getId()));
	}

	public Environment getEnvironment() {
		return environment;
	}

}
