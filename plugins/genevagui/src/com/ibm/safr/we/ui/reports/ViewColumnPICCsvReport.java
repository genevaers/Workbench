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

public class ViewColumnPICCsvReport extends GenevaCSVReport {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.reports.ViewColumnPICReport");

    private Map<Integer, List<ViewColumnPICQueryBean>> viewPICs = new TreeMap<>();

	public ViewColumnPICCsvReport() {
	}


	public void addViewColumns(int id, List<ViewColumnPICQueryBean> viewColumns) {
		viewPICs.put(id, viewColumns);		
	}

	public void setFileName(Path path, String baseName, List<Integer> viewIDs) {
		Path csvPath = makeCsvDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId();
		for(Integer id : viewIDs) {
			outputFile += "_[" + id.toString() + "]";
		}
		outputFile += ".csv";
		reportPath = csvPath.resolve(outputFile);
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

	@Override
	protected List<String>  getHeaders() {
		headers.add("View");
		headers.add("Column");
		headers.add("Name");
		headers.add("PIC");
		return headers;
	}
	
	@Override
	protected List<List<String>> getRows() {
		for(Integer v : viewPICs.keySet()) {
			addPicData(v);
		}
		return allrows;
	}


	private void addPicData(Integer v) {
		List<ViewColumnPICQueryBean> pics = viewPICs.get(v);
			
		for ( ViewColumnPICQueryBean p : pics) {
			List<String> row = new ArrayList<>();
			row.add(Integer.toString(v));
			row.add(p.getColumnnumber());
			row.add(p.getHdrline1() == null ? "" : p.getHdrline1());
			row.add(getPictureClause(p));
			allrows.add(row);
		}
	}

}
