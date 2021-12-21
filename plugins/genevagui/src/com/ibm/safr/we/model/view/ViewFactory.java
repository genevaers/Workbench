package com.ibm.safr.we.model.view;

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

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnTransfer;
import com.ibm.safr.we.data.transfer.ViewLogicDependencyTransfer;
import com.ibm.safr.we.data.transfer.ViewSortKeyTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;

public class ViewFactory {

    /**
     * Initializes and returns a new header Footer Item object created using a
     * transfer object and add it to the view object passed to it.
     * 
     * @throws SAFRException
     * @throws DAOException
     */
    public static void addHeaderFooterItem(View view, HeaderFooterItemTransfer trans)
            throws DAOException, SAFRException {
        if (trans.isHeader()) {
            view.getHeader().addItemInit(
                    new HeaderFooterItem(view.getHeader(), trans));
        } else {
            view.getFooter().addItemInit(
                    new HeaderFooterItem(view.getFooter(), trans));
        }
    }
    
    /**
     * Loads the header footer items from database into the View object.
     * 
     * @param view
     *            the view to load the items with.
     * @throws SAFRException
     *             if an error occurs while loading header footer items.
     * @throws DAOException
     *             if a database error occurs while loading header footer items.
     */
    public static void loadHeaderFooterItems(View view) throws SAFRException {
        List<HeaderFooterItemTransfer> items = new ArrayList<HeaderFooterItemTransfer>();
        // call DAO and get items
        items = DAOFactoryHolder.getDAOFactory().getHeaderFooterDAO()
                .getAllHeaderFooterItems(view.getId(),
                        view.getEnvironmentId());
        for (HeaderFooterItemTransfer trans : items) {
            if (trans.isHeader()) {
                // add to view header
                view.getHeader().addItemInit(
                        new HeaderFooterItem(view.getHeader(), trans));
            } else {
                // add to view footer
                view.getFooter().addItemInit(
                        new HeaderFooterItem(view.getFooter(), trans));
            }
        }
    }

    
    /**
     * Initializes and returns a new View Sort Key object created using a
     * transfer object.
     * 
     * @return View Sort Key object.
     * @throws SAFRException
     * @throws DAOException
     */
    public static ViewSortKey initViewSortKey(View view, ViewSortKeyTransfer trans)
            throws DAOException, SAFRException {
        return new ViewSortKey(view, trans);
    }

    
    /**
     * Initializes and returns a new View Column object created using a transfer
     * object.
     * 
     * @return View Column object.
     * @throws SAFRException
     * @throws DAOException
     */
    public static ViewColumn initViewColumn(View view, ViewColumnTransfer trans)
            throws DAOException, SAFRException {
        return new ViewColumn(view, trans);
    }

    /**
     * This method is used to get the ViewColumns of the specified View.
     * 
     * @param view
     *            : the View whose ViewColumns are to be retrieved.
     * @return a list of ViewColumns.
     * @throws SAFRException
     */
    public static List<ViewColumn> getViewColumns(View view) throws SAFRException {
        List<ViewColumn> viewcols = new ArrayList<ViewColumn>();
        List<ViewColumnTransfer> vcts = DAOFactoryHolder.getDAOFactory()
                .getViewColumnDAO().getViewColumns(view.getId(),
                        view.getEnvironmentId());
        for (ViewColumnTransfer vct : vcts) {
            ViewColumn vc = new ViewColumn(view, vct);
            viewcols.add(vc);
        }
        return viewcols;
    }

    /**
     * Initializes and returns a new View Column Source object created using a
     * transfer object.
     * 
     * @return View Column Source object.
     * @throws SAFRException
     * @throws DAOException
     */
    public static ViewColumnSource initViewColumnSource(View view,
            ViewColumnSourceTransfer trans) throws DAOException, SAFRException {
        return new ViewColumnSource(view, trans);
    }

    /**
     * This method is used to get the ViewColumnSources for the specified View.
     * 
     * @param view
     *            the parent View
     * @return a list of ViewColumnSources
     * @throws SAFRException
     */
    public static List<ViewColumnSource> getViewColumnSources(View view)
            throws SAFRException {
        List<ViewColumnSource> viewcolsrcs = new ArrayList<ViewColumnSource>();
        List<ViewColumnSourceTransfer> vcsts = DAOFactoryHolder.getDAOFactory()
                .getViewColumnSourceDAO().getViewColumnSources(view.getId(),
                        view.getEnvironmentId());
        for (ViewColumnSourceTransfer vcst : vcsts) {
            ViewColumnSource vcs = new ViewColumnSource(view, vcst);
            viewcolsrcs.add(vcs);
        }
        return viewcolsrcs;
    }

    public static List<ViewLogicDependency> getViewLogicDependencies(View view) throws SAFRException {
        List<ViewLogicDependency> viewLogicDependencies = new ArrayList<ViewLogicDependency>();
        List<ViewLogicDependencyTransfer> vldts = DAOFactoryHolder.getDAOFactory()
                .getViewLogicDependencyDAO().getViewDependecies(view.getId(),
                        view.getEnvironmentId());
        
        for (ViewLogicDependencyTransfer vldt : vldts) {
            ViewLogicDependency vld = new ViewLogicDependency(view, vldt);
            viewLogicDependencies.add(vld);
        }

        return viewLogicDependencies;
    }

    /**
     * Initializes and returns a new View Source object created using a transfer
     * object.
     * 
     * @return View Source object.
     * @throws SAFRException
     * @throws DAOException
     */
    public static ViewSource initViewSource(View view, ViewSourceTransfer trans)
            throws DAOException, SAFRException {
        return new ViewSource(view, trans);
    }

    /**
     * This method is used to get the ViewSources of the specified View.
     * 
     * @param view
     *            : the View whose ViewSources are to be retrieved.
     * @return a list of ViewSources.
     * @throws SAFRException
     */
    public static List<ViewSource> getViewSources(View view) throws SAFRException {
        List<ViewSource> viewsrcs = new ArrayList<ViewSource>();
        List<ViewSourceTransfer> vsts = DAOFactoryHolder.getDAOFactory()
                .getViewSourceDAO().getViewSources(view.getId(),
                        view.getEnvironmentId());
        for (ViewSourceTransfer vst : vsts) {
            ViewSource vs = new ViewSource(view, vst);
            viewsrcs.add(vs);
        }
        return viewsrcs;
    }

    /**
     * This method is used to get the ViewSortKeys of the specified View.
     * 
     * @param view
     *            : the View whose ViewSortKeys are to be retrieved.
     * @return a list of ViewSortKeys.
     * @throws SAFRException
     */
    public static List<ViewSortKey> getViewSortKeys(View view) throws SAFRException {
        List<ViewSortKey> viewsortkeys = new ArrayList<ViewSortKey>();
        List<ViewSortKeyTransfer> vskts = DAOFactoryHolder.getDAOFactory()
                .getViewSortKeyDAO().getViewSortKeys(view.getId(),
                        view.getEnvironmentId());
        for (ViewSortKeyTransfer vskt : vskts) {
            ViewSortKey vsk = new ViewSortKey(view, vskt);
            viewsortkeys.add(vsk);
        }
        return viewsortkeys;
    }

    
    
}
