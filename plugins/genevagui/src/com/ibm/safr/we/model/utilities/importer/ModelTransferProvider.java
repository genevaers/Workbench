package com.ibm.safr.we.model.utilities.importer;

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


import com.ibm.safr.we.data.transfer.SAFRTransfer;

/**
 * This class acts as a bridge between the import utility and the WE model
 * classes. The WE model classes sometime require the actual model object to
 * validate its state or perform some activity. During import, these model
 * objects are not available, so the WE model classes can use this provider
 * class to reach the model objects parsed from the XML.
 * 
 */
public class ModelTransferProvider {
	ComponentImporter importer;

	public ModelTransferProvider(ComponentImporter importer) {
		this.importer = importer;
	}

	/**
	 * Retrieves transfer object of a WE model class from a list of component
	 * transfers parsed from the XML.
	 * 
	 * @param className
	 *            The class name of the transfer object to be retrieved
	 * @param id
	 *            The Id of the transfer object to be retrieved.
	 * @return The transfer object.
	 */
	public SAFRTransfer get(Class<? extends SAFRTransfer> className, Integer id) {
		return importer.records.get(className).get(id);
	}

	/**
	 * Returns the component importer used by this provider.
	 * 
	 * @return a subclass of component importer.
	 */
	public ComponentImporter getImporter() {
		return importer;
	}
}
