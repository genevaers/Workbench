package com.ibm.safr.we.model.query;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


public class ViewMappingsReportQueryBean {

		private String viewid;
		private String viewcolumnid;
		private String columnnumber;
		private String srcseqnbr;
		private int sourcetypeid;
		private String constval;
		private String lookupid;
		private String lkName;
		private String lrfieldid;
		private String fieldName;
		private String extractcalclogic;
		private String spacesbeforecolumn;
		private String startposition;
		private String fldfmtcd;
		private String fldcontentcd;
		private String maxlen;
		private String justifycd;
		private String decimalcnt;
		private String rounding;
		private String signedind;
		private String visible;
		private String formatcalclogic;
		private ViewSortKeyReportQueryBean sortkey;
		
		public ViewMappingsReportQueryBean(String viewid, String viewcolumnid, String columnnumber, String srcseqnbr, int srcType,
				String constval, String lookupid, String lkName, String lrfieldid, String fieldName, String extractcalclogic,
				String spacesbeforecolumn, String startposition, String fldfmtcd, String fldcontentcd, String maxlen, String justifycd,
				String decimalcnt, String rounding, String signedind, String visible, String formatcalclogic) {
			super();
			this.viewid = viewid;
			this.viewcolumnid = viewcolumnid;
			this.columnnumber = columnnumber;
			this.srcseqnbr = srcseqnbr;
			this.sourcetypeid = srcType;
			this.constval = constval;
			this.lookupid = lookupid;
			this.lkName = lkName;
			this.lrfieldid = lrfieldid;
			this.fieldName = fieldName;
			this.extractcalclogic = extractcalclogic;
			this.spacesbeforecolumn = spacesbeforecolumn;
			this.startposition = startposition;
			this.fldfmtcd = fldfmtcd;
			this.fldcontentcd = fldcontentcd;
			this.maxlen = maxlen;
			this.justifycd = justifycd;
			this.decimalcnt = decimalcnt;
			this.rounding = rounding;
			this.signedind = signedind;
			this.visible = visible;
			this.formatcalclogic = formatcalclogic;
		}

		public String getViewid() {
			return viewid;
		}

		public String getViewcolumnid() {
			return viewcolumnid;
		}

		public String getColumnnumber() {
			return columnnumber;
		}

		public String getSrcseqnbr() {
			return srcseqnbr;
		}

		public Integer getSourcetypeid() {
			return sourcetypeid;
		}

		public String getConstval() {
			return constval;
		}

		public String getLookupid() {
			return lookupid;
		}

		public String getLkName() {
			return lkName;
		}

		public String getLrfieldid() {
			return lrfieldid;
		}

		public String getFieldName() {
			return fieldName;
		}

		public String getExtractcalclogic() {
			return extractcalclogic;
		}

		public String getSpacesbeforecolumn() {
			return spacesbeforecolumn;
		}

		public String getStartposition() {
			return startposition;
		}

		public String getFldfmtcd() {
			return fldfmtcd;
		}

		public String getFldcontentcd() {
			return fldcontentcd;
		}

		public String getMaxlen() {
			return maxlen;
		}

		public String getJustifycd() {
			return justifycd;
		}

		public String getDecimalcnt() {
			return decimalcnt;
		}

		public String getRounding() {
			return rounding;
		}

		public String getSignedind() {
			return signedind;
		}

		public String getVisible() {
			return visible;
		}

		public String getFormatcalclogic() {
			return formatcalclogic == null ? "" : formatcalclogic;
		}

		public void setSortKey(ViewSortKeyReportQueryBean sk) {
			sortkey = sk;
		}
		
		public ViewSortKeyReportQueryBean getSortkey() {
			return sortkey;
		}
		
}
