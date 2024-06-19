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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.genevaers.runcontrolgenerator.workbenchinterface.WorkbenchCompiler;

import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * This class is used to batch activate the Views.
 */
public class BatchActivateViews {

    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.model.utilities.BatchActivateViews");

    private static View view;
    private static ViewQueryBean viewBean;
    private static final String ACTIVE = "ACTVE";
    private static boolean allActive;

    /**
     * This static method is used to activate the views that are selected.
     * Validates the view and adds the errors to the list if any. If a view has
     * any inactive components while activating, then those are added to the
     * loaderrors list.
     * 
     * @param batchViewComponent
     * @throws SAFRException
     */
    public static void activate(Collection<BatchComponent> batchViewComponents, ConfirmWarningStrategy warningStrategy)
            throws SAFRException {

        // This loop is used to take the list of batchViewcomponent objects and
        // activate the views, adds to the valid list and stored to the database
        // else is added to
        // the invalid list and stored to the database.
        allActive = true;
        Integer max = batchViewComponents.size();
        Integer i = 1;
        for (BatchComponent component : batchViewComponents) {
            SAFRLogger.logAllStamp(logger, Level.INFO,
                    "Batch Activating " + i++ + " of " + max + " : " + component.getComponent().getIdLabel());
            component.setException(null);
            viewBean = (ViewQueryBean) component.getComponent();
            String status = viewBean.getStatus();

            try {
                view = SAFRApplication.getSAFRFactory().getView(viewBean.getId(), viewBean.getEnvironmentId());
            } catch (SAFRDependencyException sde) {
                component.setException(sde);
                component.setResult(ActivityResult.LOADERRORS);
                continue;
            }
            SAFRViewActivationException actExp = null;
            try {
                // CQ9748 ensure already Active views are re-compiled if
                // selected.
                // If it's currently active, make it inactive first.
                if (status.equalsIgnoreCase(ACTIVE)) {
                    view.makeViewInactive();
                }
                view.batchActivate();
                view.setConfirmWarningStrategy(warningStrategy);
                view.store();
            } catch (SAFRViewActivationException svae) {
                // set the SAFRViewActivationException in the model to use
                // it in the UI which is later passed to the
                // SAFRViewActivation Error.
                actExp = svae;
                component.setException(svae);
                // if warning store it anyway
                if (!svae.hasErrorOccured()) {
                    view.setConfirmWarningStrategy(warningStrategy);
                    view.store();
                }
            }
            if (actExp == null) {
                if (WorkbenchCompiler.hasWarnings()) {
                    component.setResult(ActivityResult.WARNING);
                } else {
                    component.setResult(ActivityResult.PASS);
                }
                component.setActive(true);
            } else if (actExp.hasErrorOccured()) {
                component.setResult(ActivityResult.FAIL);
                component.setActive(false);
                allActive = false;
            } else {
                component.setResult(ActivityResult.PASS);
                component.setActive(true);
            }
        }
    }
    
    public static boolean isAllActive() {
        return allActive;
    }
}
