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
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SearchCriteria;
import com.ibm.safr.we.constants.SearchPeriod;
import com.ibm.safr.we.constants.SearchViewsIn;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.view.View;

public class TestFindReplaceText extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.testFindReplaceText");

	TestDataLayerHelper helper = new TestDataLayerHelper();

	
	public void setUp() {
		helper.initDataLayer();
	}

	public void tearDown() {
		helper.closeDataLayer();
	}

	public void testFindViewsToReplaceLogicText() {
		
		FindReplaceText findReplaceText = new FindReplaceText();
		try {
			findReplaceText.setEnvironmentId(93);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
			findReplaceText
					.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(new Date());
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("test7322");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			// search all views.
			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			FindReplaceComponent fRcComponent = fRCList.get(0);
			assertTrue(fRcComponent.getLogicTextType() == LogicTextType.Format_Record_Filter);
			assertTrue(fRcComponent.getLogicText().equals("test7322"));
			assertTrue(fRcComponent.getViewId().equals(8521));
			assertTrue(fRcComponent.getViewName().equals("test_7322"));
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// search all views.
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			list.add(new ViewQueryBean(null, 8523, null, null, null, null,
					null, null, null, null, null, null, null, null));
			list.add(new ViewQueryBean(null, 8524, null, null, null, null,
					null, null, null, null, null, null, null, null));
			list.add(new ViewQueryBean(null, 8525, null, null, null, null,
					null, null, null, null, null, null, null, null));

			findReplaceText.setEnvironmentId(1);
			findReplaceText
					.setSearchViewsIn(SearchViewsIn.SearchInSpecificViews);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(new Date());
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("test7322");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			
			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			FindReplaceComponent fRcComponent = fRCList.get(0);
			assertTrue(fRcComponent.getLogicText().equals("test7322"));
			assertTrue(fRcComponent.getViewId().equals(8523));
			assertTrue(fRcComponent.getViewName().equals("test7322"));
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// search in views folders.
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			list.add(new ViewQueryBean(null, 1, null, null, null, null, null,
					null, null, null, null, null, null, null));
			list.add(new ViewQueryBean(null, 20, null, null, null, null, null,
					null, null, null, null, null, null, null));
			list.add(new ViewQueryBean(null, 51, null, null, null, null, null,
					null, null, null, null, null, null, null));

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchInViewFolders);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(new Date());
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("test7322");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			FindReplaceComponent fRcComponent = fRCList.get(0);
			assertTrue(fRcComponent.getLogicText().equals("test7322"));
			assertTrue(fRcComponent.getViewId().equals(8523));
			assertTrue(fRcComponent.getViewName().equals("test7322"));
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// refine search & search all views..
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			Date date = new Date();

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.SearchCreated);
			findReplaceText.setDateToSearch(date);
			findReplaceText.setSearchPeriod(SearchPeriod.Before);
			findReplaceText.setSearchText("test");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			FindReplaceComponent fRcComponent = fRCList.get(0);
			View view = SAFRApplication.getSAFRFactory().getView(
					fRcComponent.getViewId());
			assertTrue(view.getCreateTime().before(date));
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// refine search & search all views..test for search modified after.
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			View view = SAFRApplication.getSAFRFactory().getView(8519);
			Date date = view.getCreateTime();

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.SearchModified);
			findReplaceText.setDateToSearch(date);
			findReplaceText.setSearchPeriod(SearchPeriod.After);
			findReplaceText.setSearchText("test");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			assertTrue(view.getModifyTime().after(date));
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// refine search & search all views..test for created on date
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			View view = SAFRApplication.getSAFRFactory().getView(8519);
			Date date = view.getCreateTime();

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.SearchCreated);
			findReplaceText.setDateToSearch(date);
			findReplaceText.setSearchPeriod(SearchPeriod.On);
			findReplaceText.setSearchText("test");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			boolean check = false;
			int i = 0;
			for (FindReplaceComponent component : fRCList) {
				component = fRCList.get(i);
				if (view.getId().equals(component.getViewId())) {
					check = true;
				}
				i++;
			}
			assertTrue(check);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// search all views..test for match case
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			Date date = new Date();

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(date);
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("TEST");
			findReplaceText.setCaseSensitive(true);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			int i = 0;
			boolean check = false;
			for (FindReplaceComponent component : fRCList) {
				component = fRCList.get(i);
				if (component.getLogicText().matches("TEST")) {
					check = true;
				}
				i++;
			}
			assertTrue(check);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		try {
			// search all views..test for PATTERN MATCHING
			List<EnvironmentalQueryBean> list = new ArrayList<EnvironmentalQueryBean>();
			Date date = new Date();

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
			findReplaceText.setSearchRangeList(list);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(date);
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("(test){2}");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(true);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);
			FindReplaceComponent fRcComponent = fRCList.get(0);
			assertTrue(fRcComponent.getLogicText().matches("(test){2}"));
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReplaceLogicTextInViews() {
		
		FindReplaceText findReplaceText = new FindReplaceText();

		try {
			// search all views and replace
			String componentNameToFind = "replace";
			String componentNameToReplace = "find";
			// search all views.
			List<EnvironmentalQueryBean> envQBeanlist = new ArrayList<EnvironmentalQueryBean>();
			envQBeanlist.add(new ViewQueryBean(null, 8528, null, null, null,
					null, null, null, null, null, null, null, null, null));

			findReplaceText.setEnvironmentId(1);
			findReplaceText
					.setSearchViewsIn(SearchViewsIn.SearchInSpecificViews);
			findReplaceText.setSearchRangeList(envQBeanlist);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(new Date());
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText(componentNameToFind);
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> fRCList = findReplaceText.findViews();
			assertTrue(fRCList.size() > 0);

			// replace string "replace" by "find" and check whether view are
			// inactive.
			findReplaceText.setEnvironmentId(1);
			findReplaceText.setComponentsToReplace(fRCList);
			findReplaceText.setSearchText(componentNameToFind);
			findReplaceText.setReplaceText(componentNameToReplace);
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			findReplaceText.replaceViews();
			View view = SAFRApplication.getSAFRFactory().getView(
					fRCList.get(0).getViewId());

			assertTrue(view.getStatusCode().getGeneralId().equals(
					Codes.INACTIVE));
			// search DB to check whether component is replaced or not.

			findReplaceText.setEnvironmentId(1);
			findReplaceText
					.setSearchViewsIn(SearchViewsIn.SearchInSpecificViews);
			findReplaceText.setSearchRangeList(envQBeanlist);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(new Date());
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("replace");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> list = findReplaceText.findViews();
			assertTrue(list.isEmpty());

			// now replace find again by "replace" to make Junit run properly.

			findReplaceText.setEnvironmentId(1);
			findReplaceText
					.setSearchViewsIn(SearchViewsIn.SearchInSpecificViews);
			findReplaceText.setSearchRangeList(envQBeanlist);
			findReplaceText.setSearchCriteria(SearchCriteria.None);
			findReplaceText.setDateToSearch(new Date());
			findReplaceText.setSearchPeriod(SearchPeriod.None);
			findReplaceText.setSearchText("find");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);

			List<FindReplaceComponent> list1 = findReplaceText.findViews();

			findReplaceText.setEnvironmentId(1);
			findReplaceText.setComponentsToReplace(list1);
			findReplaceText.setSearchText("find");
			findReplaceText.setReplaceText("replace");
			findReplaceText.setCaseSensitive(false);
			findReplaceText.setUsePatternMatching(false);
			findReplaceText.replaceViews();

		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

	}
	public void testGetSetEnvironmentID() {
		
		Integer envid = 0;
		FindReplaceText findReplaceText = new FindReplaceText();
		findReplaceText.setEnvironmentId(envid);
		assertEquals(envid,findReplaceText.getEnvironmentId());
	}
	
	public void testGetSetSearchViewsIn() {

		SearchViewsIn Search = SearchViewsIn.SearchInSpecificViews;
		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchViewsIn(Search);
		assertEquals(Search,findReplaceText.getSearchViewsIn());
	}
	
	public void testGetSetSearchRangeList() {

		List<EnvironmentalQueryBean> envQBeanlist = new ArrayList<EnvironmentalQueryBean>();
		
		envQBeanlist.add(new ViewQueryBean(null, 8528, null, null, null,
				null, null, null, null, null, null, null, null, null));	
		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchRangeList(envQBeanlist);
		assertEquals(envQBeanlist,findReplaceText.getSearchRangeList());
	}
	
	public void testGetSetSearchCriteria() {

		SearchCriteria sc = SearchCriteria.None;
		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchCriteria(sc);
		assertEquals(sc,findReplaceText.getSearchCriteria());
	}
	
	public void testGetSetDateToSearch() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		Date dateToSearch = new Date();
		findReplaceText.setDateToSearch(dateToSearch);
		assertEquals(dateToSearch,findReplaceText.getDateToSearch());
	}
	
	public void testGetSetSearchPeriod() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		SearchPeriod sp = SearchPeriod.None;
		findReplaceText.setSearchPeriod(sp);
		assertEquals(sp,findReplaceText.getSearchPeriod());
	}
	
	public void testGetSetSearchText() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		String st = "test";
		findReplaceText.setSearchText(st);
		assertEquals(st,findReplaceText.getSearchText());
	}
	
	public void testIsSetCaseSensitive() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		boolean cs = true;
		findReplaceText.setCaseSensitive(cs);
		assertEquals(cs,findReplaceText.isCaseSensitive());
	}
	
	public void testGetUsePatternMatching() {

		FindReplaceText findReplaceText = new FindReplaceText();

		boolean usePatternMatching =true;
		findReplaceText.setUsePatternMatching(usePatternMatching);
		assertEquals(usePatternMatching,findReplaceText.usePatternMatching());
	}
	
	public void testGetSetReplaceText() {

		FindReplaceText findReplaceText = new FindReplaceText();

		String rtext = "test";
		findReplaceText.setReplaceText(rtext );
		assertEquals(rtext,findReplaceText.getReplaceText());
	}
	
	public void testGetSetComponentsToReplace() throws SAFRException {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setEnvironmentId(1);
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
		findReplaceText.setSearchPeriod(SearchPeriod.None);
		findReplaceText.setSearchText("Test");
		List<FindReplaceComponent> fRCList = findReplaceText.findViews();
		
		findReplaceText.setComponentsToReplace(fRCList);
		assertEquals(fRCList,findReplaceText.getComponentsToReplace());
	}
	
	public void testValidateFindError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		try {
	       findReplaceText.findViews();
        } catch (SAFRException e) {
	       String error = e.getMessage();
	        assertEquals(error,"Please specify a search text." + SAFRUtilities.LINEBREAK + "Please select a valid environment." + SAFRUtilities.LINEBREAK + "Please select an option to specify which views to search.");
        }
	}
	
	public void testValidateFindSearchViewError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchInSpecificViews);
		findReplaceText.setSearchText("Test");
		findReplaceText.setSearchPeriod(SearchPeriod.None);
		findReplaceText.setEnvironmentId(1);
		try {
	       findReplaceText.findViews();
        } catch (SAFRException e) {
	       String error = e.getMessage();
	        assertEquals(error,"Please select specific View(s) to search in.");
        }
	}
	
	public void testValidateFindSearchViewFolderError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchInViewFolders);
		findReplaceText.setSearchText("Test");
		findReplaceText.setSearchPeriod(SearchPeriod.None);
		findReplaceText.setEnvironmentId(1);
		try {
	       findReplaceText.findViews();
        } catch (SAFRException e) {
	       String error = e.getMessage();
	        assertEquals(error,"Please select View Folder(s) to search in.");
        }
	}
	
	public void testValidateFindSearchPeriodError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
		findReplaceText.setSearchText("Test");
		findReplaceText.setSearchCriteria(SearchCriteria.None);
		findReplaceText.setEnvironmentId(1);
		try {
	       findReplaceText.findViews();
        } catch (SAFRException e) {
	       String error = e.getMessage();
	        assertEquals(error,"Please select the search period.");
        }
	}
	
	public void testValidateFindSearchDateError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
		findReplaceText.setSearchText("Test");
		findReplaceText.setSearchCriteria(SearchCriteria.None);
		findReplaceText.setSearchPeriod(SearchPeriod.None);
		findReplaceText.setEnvironmentId(1);
		try {
	       findReplaceText.findViews();
        } catch (SAFRException e) {
	       String error = e.getMessage();
	        assertEquals(error,"Please select the date to search.");
        }
	}
	
	public void testValidateFindSearchPatternError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
		findReplaceText.setSearchText("[a-%xz]");
		findReplaceText.setUsePatternMatching(true);
		findReplaceText.setEnvironmentId(1);
		try {
	       findReplaceText.findViews();
        } catch (SAFRException e) {
	       String error = e.getMessage().substring(0, 92);
	        assertEquals(error,"There was an error validating the pattern. Please enter the correct pattern to search views.");
        }
	}
	
	public void testValidateReplaceError() {

		FindReplaceText findReplaceText = new FindReplaceText();
	
		try {
	       findReplaceText.replaceViews();
        } catch (SAFRException e) {
	       String error = e.getMessage();
	        assertEquals(error,"Please specify a search text." + SAFRUtilities.LINEBREAK + "Please select a valid environment." + SAFRUtilities.LINEBREAK + "Please select a list of views to replace the logic text in." + SAFRUtilities.LINEBREAK + "Please specify a replace text.");
        }
	}
	
	public void testValidateReplacePatternError() {

		FindReplaceText findReplaceText = new FindReplaceText();
		findReplaceText.setSearchRangeList(new ArrayList<EnvironmentalQueryBean>());
		findReplaceText.setSearchViewsIn(SearchViewsIn.SearchAllViews);
		findReplaceText.setSearchText("[a-%xz]");
		findReplaceText.setUsePatternMatching(true);
		findReplaceText.setEnvironmentId(99);
		try {
	       findReplaceText.replaceViews();
        } catch (SAFRException e) {
	       String error = e.getMessage().substring(0, 92);
	        assertEquals(error,"There was an error validating the pattern. Please enter the correct pattern to search views.");
        }
	}
}
