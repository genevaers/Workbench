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


import java.util.logging.Logger;

import junit.framework.TestCase;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.view.View.HeaderFooterItems;

public class TestHeaderFooterItem extends TestCase {
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.TestViewColumn");

	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void setUp() {
	}
	
	public void tearDown() {
	    helper.closeDataLayer();
	}

	public void dbStartup() {
		try {
			helper.initDataLayer();
		} catch (DAOException e) {
			assertFalse(true);
		}
	}

	public void testGetHeaderFooterItem() throws SAFRException {
		dbStartup();
		View view = SAFRApplication.getSAFRFactory().getView(8518, 1);
		CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.FUNCTION);
		Code functionCode = codeSet.getCode(Codes.HF_FISCALDATE);
		CodeSet codeSet1 = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.JUSTIFY);
		Code justifyCode = codeSet1.getCode(Codes.RIGHT);
		View.HeaderFooterItems hf = view.new HeaderFooterItems(view, true);
		HeaderFooterItem hfItem = new HeaderFooterItem(hf, functionCode,
				justifyCode, 1, 1, "test", 1);

		assertEquals(hfItem.getFunctionCode(), functionCode);
		assertEquals(justifyCode, hfItem.getJustifyCode());
		assertEquals(new Integer(1), hfItem.getRow());
		assertEquals(new Integer(1), hfItem.getColumn());
		assertEquals(new String("test"), hfItem.getItemText());

	}
	public void testGetFunctionCode() throws SAFRException{
		dbStartup();
		
		CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FUNCTION);
		Code functionCode = codeSet.getCode(Codes.HF_FISCALDATE);
		
		HeaderFooterItem hf = new HeaderFooterItem(null, functionCode, null, 1, 1, "NA", 1);
		
		assertEquals(hf.getFunctionCode(), functionCode);
		
		
		/* test from a saved view */
		View view = SAFRApplication.getSAFRFactory().getView(8552, 1);
		HeaderFooterItems header = view.getHeader();
		
		HeaderFooterItem header_1 = header.getItems().get(0);
	
		Code functionCode_1 = codeSet.getCode(Codes.HF_COMPANYNAME);
		
		assertEquals(header_1.getFunctionCode(), functionCode_1);
		
		
	}
	
	public void testGetItemText() throws SAFRException{
		dbStartup();
		View view = SAFRApplication.getSAFRFactory().getView(8552, 1);
		HeaderFooterItems footer = view.getFooter();
		
		assertEquals("testing testing 123",footer.getItems().get(1).getItemText());

	}
	
	public void testGetJustifyCode() throws SAFRException{
		dbStartup();
		
		CodeSet codeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.JUSTIFY);
		Code justifyCode = codeSet.getCode(Codes.RIGHT);
		
		HeaderFooterItem hf = new HeaderFooterItem(null, null, justifyCode, 1, 1, "NA", 1);
		assertEquals(hf.getJustifyCode(), justifyCode);
		
		View view = SAFRApplication.getSAFRFactory().getView(8552, 1);
		HeaderFooterItems footer = view.getFooter();
		
		assertEquals(justifyCode,footer.getItems().get(1).getJustifyCode());
		
	}
	
	public void testgetColumn() throws SAFRException{
		dbStartup();
		View view = SAFRApplication.getSAFRFactory().getView(8552, 1);
		HeaderFooterItems header = view.getHeader();
		
		HeaderFooterItem header_1 = header.getItems().get(0);
		HeaderFooterItem header_2 = header.getItems().get(1);
		HeaderFooterItem header_3 = header.getItems().get(2);
		
		assertEquals(header_1.getColumn(),new Integer(1));
		assertEquals(header_2.getColumn(),new Integer(2));
		assertEquals(header_3.getColumn(),new Integer(3));
	
	}
	
	public void testGetRow() throws SAFRException{
		
		dbStartup();
		View view = SAFRApplication.getSAFRFactory().getView(8552, 1);
		HeaderFooterItems header = view.getHeader();
		HeaderFooterItems footer = view.getFooter();
		footer.getItems().get(3).getRow();
		
		assertEquals(header.getItems().get(0).getRow(),new Integer(1));
		assertEquals(footer.getItems().get(3).getRow(), new Integer(3));		
	}
}
