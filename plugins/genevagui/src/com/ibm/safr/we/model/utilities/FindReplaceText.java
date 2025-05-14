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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.FindTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;

/**
 * This class represents find/replace utility. It can be used to find a
 * component name, keyword or expression in view's logic text. It can also be
 * used to replace a particular component name, keyword or expression in logic
 * text of views with a different text.
 */
public class FindReplaceText {
	private Integer environmentId;
	private SearchViewsIn searchViewsIn;
	private List<EnvironmentalQueryBean> searchRangeList;
	private SearchCriteria searchCriteria;
	private Date dateToSearch;
	private SearchPeriod searchPeriod;
	private String searchText;
	private boolean matchCase;
	private boolean usePatternMatching;
	private boolean component = false;

    private List<FindReplaceComponent> componentsToReplace;
	private String replaceText;
    Map<Integer, List<FindTransfer>> viewIdToFindTransListmap;	

	/**
	 * This enumeration maintains the properties of find replace utility.
	 * 
	 */
	public enum Property {
		FINDWHAT, SEARCHSPECIFICVIEWS, SEARCHINVIEWFOLDERS, ENVIRONMENTID, SEARCHVIEWSIN, DATETOSEARCH, SEARCHPERIOD, SEARCHCRITERIA, MATCHCASE, USEPATTERNMATCHING, COMPONENTNAMESTOREPLACE, REPLACE;
	}

	private static Pattern pattern;

	/**
	 * @return The current environment id.
	 */
	public Integer getEnvironmentId() {
		return environmentId;
	}

	/**
	 * 
	 * Sets the environment id which will be used to find or replace views.
	 * 
	 * @param environmentId
	 */
	public void setEnvironmentId(Integer environmentId) {
		this.environmentId = environmentId;
	}

	public SearchViewsIn getSearchViewsIn() {
		return searchViewsIn;
	}

	/**
	 * Sets the {@link SearchViewsIn} enumeration value, this describes the
	 * criteria for limiting the views to search in.
	 * 
	 * @param searchViewsIn
	 */
	public void setSearchViewsIn(SearchViewsIn searchViewsIn) {
		this.searchViewsIn = searchViewsIn;
	}

	public List<EnvironmentalQueryBean> getSearchRangeList() {
		return searchRangeList;
	}

	/**
	 * Set the search range {@link List}. The list will
	 * <ul>
	 * <li>Be empty if search will be done in all views.
	 * <li>contain view ids if search will be done in specific views.
	 * <li>contain view folder ids if search will be done in specific view
	 * folders.
	 * 
	 * @param searchRangeList
	 */
	public void setSearchRangeList(List<EnvironmentalQueryBean> searchRangeList) {
		this.searchRangeList = searchRangeList;
	}

	/**
	 * @return the search criteria.
	 */
	public SearchCriteria getSearchCriteria() {
		return searchCriteria;
	}

	/**
	 * Sets the {@link SearchCriteria} enumeration value, this is optional.
	 * Setting this value other then NULL will require setting of date and
	 * search period.
	 * 
	 * @param searchCriteria
	 *            NULL if no search criteria is required.
	 */
	public void setSearchCriteria(SearchCriteria searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	public Date getDateToSearch() {
		return dateToSearch;
	}

	/**
	 * Sets the date to search. Required only if the search criteria is not
	 * NULL.
	 * 
	 * @param dateToSearch
	 */
	public void setDateToSearch(Date dateToSearch) {
		this.dateToSearch = dateToSearch;
	}

	public SearchPeriod getSearchPeriod() {
		return searchPeriod;
	}

	/**
	 * 
	 * Sets the {@link SearchPeriod} enumeration value. Required only if the
	 * search criteria is not NULL.
	 * 
	 * @param searchPeriod
	 */
	public void setSearchPeriod(SearchPeriod searchPeriod) {
		this.searchPeriod = searchPeriod;
	}

	public String getSearchText() {
		return searchText;
	}

	/**
	 * Sets the component name, keyword or expression used to search the view's
	 * logic text.
	 * 
	 * @param searchText
	 */
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public boolean isCaseSensitive() {
		return matchCase;
	}

	/**
	 * Sets the whether the search will case sensitive.
	 * 
	 * @param matchCase
	 */
	public void setCaseSensitive(boolean matchCase) {
		this.matchCase = matchCase;
	}

	/**
	 * 
	 * @return boolean indicating if the search text is a regular expression
	 *         pattern.
	 */
	public boolean usePatternMatching() {
		return usePatternMatching;
	}

    public void setComponent(boolean component) {
        this.component = component;
    }
	
	/**
	 * sets whether the search text is a regular expression.
	 * 
	 * @param usePatternMatching
	 */
	public void setUsePatternMatching(boolean usePatternMatching) {
		this.usePatternMatching = usePatternMatching;
	}

	public List<FindReplaceComponent> getComponentsToReplace() {
		return componentsToReplace;
	}

	/**
	 * 
	 * Sets the list of {@link FindReplaceComponent} which should be used to
	 * replace the text in view's logic.
	 * 
	 * @param list
	 *            of {@link FindReplaceComponent}
	 */
	public void setComponentsToReplace(
			List<FindReplaceComponent> componentsToReplace) {
		this.componentsToReplace = componentsToReplace;
	}

	public String getReplaceText() {
		return replaceText;
	}

	/**
	 * Sets the replace text which will be used to replace the search text found
	 * in view's logic.
	 * 
	 * @param replaceText
	 */
	public void setReplaceText(String replaceText) {
		this.replaceText = replaceText;
	}

	/**
	 * Searches the logic text of all applicable views for the search text.
	 * Returns a list of {@link FindReplaceComponent} for all the places where
	 * the search text was found.
	 * 
	 * @return {@link List} of the {@link FindReplaceComponent} containing the
	 *         search result.
	 * @throws SAFRException
	 * @throws SAFRValidationException
	 *             if there are validation errors.
	 */
	public List<FindReplaceComponent> findViews() throws SAFRException {
		List<FindReplaceComponent> findReList = new ArrayList<FindReplaceComponent>();
		List<Integer> componentIdList = new ArrayList<Integer>();
		validateFind();
		for (EnvironmentalQueryBean environmentalQueryBean : searchRangeList) {
			componentIdList.add(environmentalQueryBean.getId());
		}
		// get the list of the transfer objects from database.
		List<FindTransfer> findTransferList = DAOFactoryHolder.getDAOFactory()
				.getViewDAO().searchViewsToReplaceLogicText(
						environmentId,
						searchViewsIn,
						componentIdList,
						searchCriteria == null ? SearchCriteria.None : searchCriteria, 
						dateToSearch, 
						searchPeriod);
		// process list of transfer objects list to form a map.In which view id
		// is used as key.use this map for grouping find transfer objects
		// according to view id.
		viewIdToFindTransListmap = new HashMap<Integer, List<FindTransfer>>();
		List<Integer> viewIdList = new ArrayList<Integer>();
		for (FindTransfer findTransfer : findTransferList) {
			Integer key = findTransfer.getViewId();
			if (viewIdToFindTransListmap.containsKey(key)) {
				viewIdToFindTransListmap.get(key).add(findTransfer);
			} else {
				List<FindTransfer> fTList = new ArrayList<FindTransfer>();
				fTList.add(findTransfer);
				viewIdToFindTransListmap.put(key, fTList);
				viewIdList.add(key);
			}
		}

		// process the map. For each key i.e.view id extract logic text once so
		// as to improve performance.
		Set<Integer> mapKeySet = viewIdToFindTransListmap.keySet();
		for (Integer viewId : mapKeySet) {

			List<FindTransfer> fTList = viewIdToFindTransListmap.get(viewId);
			// get the logic text in the string form.
			for (FindTransfer findTransfer : fTList) {
			    String logicText =findTransfer.getLogicText();
				// check whether the component name to find is used in the
				// logic text then create find replace component.
				if ((logicText != null) && (isComponentUsedInLT(logicText))) {
					FindReplaceComponent findReplaceComponent = new FindReplaceComponent(
							viewId, findTransfer.getViewName(),
							findTransfer.getLogicTextType(), logicText,
							findTransfer.getCellId(), findTransfer.getReferenceId(),
							findTransfer.getRights());
					findReList.add(findReplaceComponent);
				}
			}
		}
		return findReList;
	}

	/**
	 * Validates the input parameters for method findViewsToReplaceLogicText().
	 * 
	 * @throws SAFRValidationException
	 *             if any of the input parameters are invalid.
	 */
	private void validateFind() throws SAFRValidationException {
		SAFRValidationException sException = new SAFRValidationException();
		if (environmentId == null || environmentId <= 0) {
			sException.setErrorMessage(FindReplaceText.Property.ENVIRONMENTID,
					"Please select a valid environment.");
		}
		if ((searchText == null) || searchText.equals("")) {
			sException.setErrorMessage(FindReplaceText.Property.FINDWHAT,
					"Please specify a search text.");
		} else {
			try {
				pattern = getCurrentPattern();
			} catch (PatternSyntaxException e) {
				sException.setErrorMessage(FindReplaceText.Property.FINDWHAT,
				    "There was an error validating the pattern. Please enter the correct pattern to search views." + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK+ e.getMessage());
			}
		}
		if (searchViewsIn == null) {
			sException
					.setErrorMessage(FindReplaceText.Property.SEARCHVIEWSIN,
							"Please select an option to specify which views to search.");
		} else {
			if ((searchRangeList == null) || (searchRangeList.isEmpty())) {
				if ((searchViewsIn.equals(SearchViewsIn.SearchInSpecificViews))) {
					sException.setErrorMessage(
							FindReplaceText.Property.SEARCHSPECIFICVIEWS,
							"Please select specific View(s) to search in.");
				}

				if ((searchViewsIn.equals(SearchViewsIn.SearchInViewFolders))) {
					sException.setErrorMessage(
							FindReplaceText.Property.SEARCHINVIEWFOLDERS,
							"Please select View Folder(s) to search in.");
				}
			}
		}
		if (searchCriteria != null) {
			if (searchPeriod == null) {
				sException.setErrorMessage(
						FindReplaceText.Property.SEARCHPERIOD,
						"Please select the search period.");
			} else if (dateToSearch == null) {
				sException.setErrorMessage(
						FindReplaceText.Property.DATETOSEARCH,
						"Please select the date to search.");
			}
		}
		if (!sException.getErrorMessages().isEmpty()) {
			throw sException;
		}
	}

	private Pattern getCurrentPattern() {
		int flgs = 0;
		if (!usePatternMatching) {
			// ignore special characters
			flgs = Pattern.LITERAL;
		}
		if (!matchCase) {
			// set the case insensitive flag
			flgs = flgs | Pattern.CASE_INSENSITIVE;
		}
		// this will throw an exception if the pattern is not valid
		return Pattern.compile(searchText, flgs);
	}

	/**
	 * Validates the input parameters for method replaceLogicTextInViews()
	 * 
	 * @throws SAFRValidationException
	 *             if any of the input parameters are invalid.
	 */
	private void validateReplace() throws SAFRValidationException {
		SAFRValidationException sException = new SAFRValidationException();
		if (environmentId == null || environmentId <= 0) {
			sException.setErrorMessage(FindReplaceText.Property.ENVIRONMENTID,
					"Please select a valid environment.");
		}
		if ((searchText == null) || searchText.equals("")) {
			sException.setErrorMessage(FindReplaceText.Property.FINDWHAT,
					"Please specify a search text.");
		} else {
			try {
				pattern = getCurrentPattern();
			} catch (PatternSyntaxException e) {
				sException.setErrorMessage(FindReplaceText.Property.FINDWHAT,
				    "There was an error validating the pattern. Please enter the correct pattern to search views." + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK+ e.getMessage());
			}
		}
		if (componentsToReplace == null || componentsToReplace.isEmpty()) {
			sException.setErrorMessage(
							FindReplaceText.Property.COMPONENTNAMESTOREPLACE,
							"Please select a list of views to replace the logic text in.");
		}

		if ((replaceText == null) || replaceText == "") {
			sException.setErrorMessage(FindReplaceText.Property.REPLACE,
					"Please specify a replace text.");
		}
		if (!sException.getErrorMessages().isEmpty()) {
			throw sException;
		}
	}

	private boolean isComponentUsedInLT(String logicText) {
		Matcher match = pattern.matcher(logicText);
		return match.find();
	}

	/**
	 * This method replaces the search text by the replace text, in all the
	 * selected view's logic text and makes those views inactive.
	 * 
	 * @throws SAFRException
	 * @throws SAFRValidationException
	 *             if there are any validation errors.
	 */
	public void replaceViews() throws SAFRException {
	    
        validateReplace();

        // replace logic text in Views
        List<FindTransfer> replacements = new ArrayList<FindTransfer>();
		for (FindReplaceComponent findReplaceComponent : componentsToReplace) {
			// replace the text only if the LT in this component contains the search text.
			if (isComponentUsedInLT(findReplaceComponent.getLogicText())) {
                findReplaceComponent.setLogicText(replaceComponentInLogicText(findReplaceComponent.getLogicText()));
			    FindTransfer trans = new FindTransfer();
			    findReplaceComponent.setTransferData(trans);
			    replacements.add(trans);
			}
		}
		DAOFactoryHolder.getDAOFactory().getViewDAO().replaceLogicText(environmentId, replacements);

		// deactivate all changed Views
        List<Integer> mapKeySet = replacements.stream().map(rp -> rp.getViewId()).collect(Collectors.toList());        
        DAOFactoryHolder.getDAOFactory().getViewDAO().makeViewsInactive(
           mapKeySet, environmentId);				
	}

	private String replaceComponentInLogicText(String logicText) {
		// use pattern to replace text in logic text
		Matcher match = pattern.matcher(logicText);
		StringBuffer buffer = new StringBuffer();
		while (match.find()) {
		    if (component) {
                match.appendReplacement(buffer, match.group(1) + replaceText + match.group(3));		        
		    }
		    else {
		        match.appendReplacement(buffer, replaceText);
		    }
		}
		match.appendTail(buffer);
		return buffer.toString();
	}
}
