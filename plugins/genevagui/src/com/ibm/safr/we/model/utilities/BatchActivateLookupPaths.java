package com.ibm.safr.we.model.utilities;

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
import java.util.List;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.LookupQueryBean;

/**
 * This class is used to activate selected lookup paths in the Batch Activate
 * lookup paths editor.
 */
public class BatchActivateLookupPaths {

	private static List<Integer> makeActiveList;
	private static List<Integer> makeInActiveList;

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.utilities.BatchActivateLookupPaths");
	
	/**
	 * This static method is used to activate the lookup paths that are
	 * selected. Validates all the steps of a lookup path and adds the errors to
	 * the list if any. If a lookup path has any inactive LR's while activating,
	 * then those are added to the loaderrors list.
	 * 
	 * @param envId
	 * @param batchComponent
	 * @throws DAOException
	 */
	public static void activate(int envId, List<BatchComponent> batchComponent)
			throws DAOException, SAFRException {
		makeActiveList = new ArrayList<Integer>();
		makeInActiveList = new ArrayList<Integer>();
		
		// This loop is used to take the list of batchcomponent object and
		// activate the lookup paths by checking
		// the steps, If the length of the source and target matches it gets
		// added to the valid list and stored to the database else is added to
		// the invalid list and stored to the database.
        Integer max = batchComponent.size();
        Integer j=1;
		for (BatchComponent component : batchComponent) {
            logger.info("Batch Activating " + j++ + " of " + max + " : "+ component.getComponent().getIdLabel());

			LookupQueryBean lookup = (LookupQueryBean) component.getComponent();
			
			if (envId != lookup.getEnvironmentId()) {
			    throw new SAFRException("Lookup environment " + lookup.getEnvironmentId() 
			            + " does not match input environment " + envId);
			}
			
			// This previousState represents the state of the lookup path
			// before activation.
			try {

				LookupPath lookupPath = SAFRApplication.getSAFRFactory()
						.getLookupPath(lookup.getId(),
								lookup.getEnvironmentId());
								
				int steps = lookupPath.getLookupPathSteps().size();

				// To check whether all the steps of a Lookup path is
				// valid or not
				for (int i = 0; i < steps; i++) {
					try {
						lookupPath.getLookupPathSteps().get(i).checkValid();
					} catch (SAFRValidationException sve) {
		                component.setResult(ActivityResult.FAIL);
					    component.setException(sve);
					    break;
					}
				}

			}
			catch (SAFRDependencyException sde) {
                component.setResult(ActivityResult.LOADERRORS);
			    component.setException(sde);
			}
			// If errors are empty the result will be shown as pass and is
			// set as active, the lookup id is added to the valid list.
			if (component.getException() == null) {
				component.setResult(ActivityResult.PASS);
				component.setActive(true);

			} 
			// If the lookup path previous state is false and becomes true
			// after batch activation the valid list gets stored into the
			// database else if the previous state is true and becomes false
			// after batch activation the invalid list is stored to the
			// database.
			if (component.isActive() == true) {
				makeActiveList.add(lookup.getId());				
			} else if (component.isActive() == false) {
				makeInActiveList.add(lookup.getId());
			}
		}
		if (!makeActiveList.isEmpty())
		{
			DAOFactoryHolder.getDAOFactory().getLookupDAO()
			.makeLookupPathsActive(makeActiveList,envId);			
		}
		if (!makeInActiveList.isEmpty())
		{
			DAOFactoryHolder.getDAOFactory().getLookupDAO()
			.makeLookupPathsInactive(makeInActiveList,envId);
		}
	}

}
