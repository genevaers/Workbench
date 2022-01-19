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

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.HeaderFooterItemTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

/**
 * This class represents items containing a view header or footer information.
 * 
 */
public class HeaderFooterItem extends SAFREnvironmentalComponent {

	private View.HeaderFooterItems headerFooter;
	private Code functionCode;
	private Code justifyCode;
	private Integer row;
	private Integer column;
	private Integer length;
	private String itemText;
	private List<String> loadWarnings;

	/**
	 * Use this constructor to create a new {@link HeaderFooterItem} object.
	 * 
	 * @param headerFooter
	 *            the parent {@link HeaderFooter} reference.
	 * @param funtionCode
	 *            the function code to describe the item's content.
	 * @param justifyCode
	 *            the item's justify code specifying which area it belongs to
	 *            (left/center/right).
	 * @param row
	 *            the row number of this item.
	 * @param column
	 *            the column number of this item.
	 * @param itemText
	 *            the user entered text. This is optional and is only used if
	 *            the function code is 'TEXT'.
	 * @param environmentId
	 *            the id of the environment.
	 */
	HeaderFooterItem(View.HeaderFooterItems headerFooter, Code funtionCode,
			Code justifyCode, Integer row, Integer column, String itemText,
			Integer environmentId) {
		super(environmentId);
		this.headerFooter = headerFooter;
		this.functionCode = funtionCode;
		this.justifyCode = justifyCode;
		this.row = row;
		this.column = column;
		this.itemText = itemText;
		// CQ 7915. Nikita. 05/05/2010. Length should be 0 for all header/footer
		// items unless it is user-text.
		this.length = itemText == null ? 0 : itemText.length();
	}

	/**
	 * Use this constructor to load an existing {@link HeaderFooterItem}.
	 * 
	 * @param headerFooter
	 *            the parent {@link HeaderFooter} reference.
	 * @param trans
	 *            the {@link HeaderFooterItemTransfer} object containing
	 *            {@link HeaderFooterItem} information to load.
	 */
	HeaderFooterItem(View.HeaderFooterItems headerFooter,
			HeaderFooterItemTransfer trans) {
		super(trans);
		this.headerFooter = headerFooter;
	}

	@Override
	protected void setObjectData(SAFRTransfer trans) {
		super.setObjectData(trans);

		loadWarnings = new ArrayList<String>();
		// TODO copy from transfer to object
		HeaderFooterItemTransfer itemTrans = (HeaderFooterItemTransfer) trans;
		this.justifyCode = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.JUSTIFY).getCode(itemTrans.getJustifyCode());

		// CQ 8768. Nikita. 21/10/2010
		// Handle invalid function code
		try {
			this.functionCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.FUNCTION).getCode(
					itemTrans.getStdFuctionCode());
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			String justify = "(" + this.justifyCode.getDescription()
					+ " Selection)";
			loadWarnings
					.add(justify
							+ " contains an invalid function (&["
							+ itemTrans.getStdFuctionCode()
							+ "]). Please select a valid function, if required, before saving.");
			this.functionCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.FUNCTION).getCode(Codes.HF_TEXT);

		}

		this.row = itemTrans.getRowNumber();
		this.column = itemTrans.getColNumber();
		this.itemText = itemTrans.getItemText();
		this.length = itemTrans.getLength();
	}

	@Override
	protected void setTransferData(SAFRTransfer trans) {
		super.setTransferData(trans);
		HeaderFooterItemTransfer itemTrans = (HeaderFooterItemTransfer) trans;
		itemTrans.setViewId(headerFooter.getView().getId());
		itemTrans.setStdFuctionCode(functionCode.getKey());
		itemTrans.setJustifyCode(justifyCode.getKey());
		itemTrans.setRowNumber(row);
		itemTrans.setColNumber(column);
		itemTrans.setItemText(itemText);
		itemTrans.setLength(length);
		itemTrans.setHeader(headerFooter.isHeader());
	}

	/*
	 * (non-Javadoc) This is a redundant method.
	 */
	@Override
	public void store() throws SAFRException, DAOException {
		// Nothing needed here. data is stored in HeaderFooter class.

	}

	public Code getFunctionCode() {
		return functionCode;
	}

	public Code getJustifyCode() {
		return justifyCode;
	}

	public Integer getRow() {
		return row;
	}

	public Integer getColumn() {
		return column;
	}

	public String getItemText() {
		return itemText;
	}

	/*
	 * (non-Javadoc) This is a redundant method and will have no effect on
	 * retrieving of header footer item.
	 */
	@Override
	public String getName() {
		return super.getName();
	}

	/*
	 * (non-Javadoc) This is a redundant method and will have no effect on
	 * setting of header footer item.
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
	}

	/*
	 * (non-Javadoc) This is a redundant method and will have no effect on
	 * retrieving or setting of header footer item.
	 */
	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		return null;
	}

	List<String> getLoadWarnings() {
		return loadWarnings;
	}

}
