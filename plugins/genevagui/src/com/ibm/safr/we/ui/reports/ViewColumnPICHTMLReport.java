package com.ibm.safr.we.ui.reports;

import java.nio.file.Path;

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
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.ViewColumnPICQueryBean;
import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TrTag;

public class ViewColumnPICHTMLReport extends GenevaHTMLReport {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnPICReport");

    private Map<Integer, List<ViewColumnPICQueryBean>> viewPICs = new TreeMap<>();
	private List<String> errorMsgList = new ArrayList<String>();
	private List<Integer> viewIDs = new ArrayList<Integer>();

	private List<ViewColumnPICQueryBean> viewColumns;

	public ViewColumnPICHTMLReport() {
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return  div(h2("View Column PIC Report"),   
					viewPICTables());
	}
	
	private DomContent viewPICTables() {
		return  each(viewPICs.values(), v-> getViewSection(v));
	}

	private ContainerTag<DivTag> getViewSection(List<ViewColumnPICQueryBean> v) {
		ViewColumnPICQueryBean vcqb = v.get(0);
		return div(
				h3(vcqb.getViewname()+"[" + vcqb.getViewid() + "]"),
				table(
					tbody(
						getTableHeader(),
						each(v, c -> getPICRow(c))
					)
				).withClass("w3-table-all w3-striped w3-border")
			   ).withClass("w3-container  w3-twothird");
	}

	private ContainerTag<TrTag> getPICRow(ViewColumnPICQueryBean c) {
		return tr(
				td(c.getColumnnumber()).withClass("w3-border"),
				td(c.getHdrline1() == null ? "" : c.getHdrline1() ).withClass("w3-border"),
				td(getPictureClause(c)).withClass("w3-border")
				).withClass("w3-border");
	}

	private ContainerTag<TrTag> getTableHeader() {
		return tr(
				th("Column").withClass("w3-border"),
				th("Name").withClass("w3-border"),
				th("PIC").withClass("w3-border")
				);
	}

	public void addViewColumns(int id, List<ViewColumnPICQueryBean> viewColumns) {
		viewPICs.put(id, viewColumns);		
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : viewIDs) {
			outputFile += "_[" + id.toString() + "]";
		}
		outputFile += ".html";
		reportPath = htmlPath.resolve(outputFile);
	}


	public String getPictureClause(ViewColumnPICQueryBean c) {

		String picClause = "PIC ";
		Code dataType = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE).getCode(c.getFldfmtcd().trim());
		//Code dataType = code.getDescription();
		Integer decimalPlaces = c.getDecimalcnt();
		Integer length = c.getMaxlen();
		Boolean signed = c.getSignedind();

		switch (dataType.getGeneralId().intValue()) {
		case Codes.ALPHANUMERIC:
			picClause += "X" + "(" + length + ")";
			break;
		case Codes.PACKED:
		case Codes.PACKED_SORTABLE:
			// double the length as its packed.
			length *= 2;
			// reserve one nibble for sign for both signed and unsigned for
			// packed number.
			length -= 1;

			picClause += (signed ? "S" : "")
					+ adjustLengthAndDecimals(length, decimalPlaces)
					+ " COMP-3";
			break;
		case Codes.BINARY:
		case Codes.BINARY_SORTABLE:
			if (length == 0) {
				picClause = "Length is zero";
			} else if (length > 8) {
				picClause = "Length is greater than 8 bytes";
			} else {
				if (length >= 1 && length <= 2) {
					length = 4;
				} else if (length >= 3 && length <= 4) {
					length = 9;
				} else if (length >= 5 && length <= 8) {
					length = 18;
				}

				boolean decimals = false;
				if (decimalPlaces > 0) {
					if (decimalPlaces > length) {
						picClause = "Too many decimal places for length";
						break;
					} else {
						decimals = true;
						length -= decimalPlaces;
					}
				}

				picClause += (signed ? "S" : "") + "9(" + length + ")"
						+ (decimals ? "V9(" + decimalPlaces + ")" : "")
						+ " COMP";
			}
			break;
		default:
			picClause += (signed ? "S" : "")
					+ adjustLengthAndDecimals(length, decimalPlaces);
			break;
		}

		return picClause;

	}

	public String adjustLengthAndDecimals(Integer length, Integer decimals) {

		String str = "";
		if (length >= decimals) {
			str += "9" + "(" + (length - decimals) + ")";
		} else {
			str += "9" + "(" + (length) + ")";
		}
		if (decimals > 0) {
			str += "V9" + "(" + decimals + ")";
		}
		return str;
	}


}
