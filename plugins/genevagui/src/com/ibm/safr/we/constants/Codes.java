package com.ibm.safr.we.constants;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.data.transfer.CodeTransfer;
import com.ibm.safr.we.model.CodeSet;

/**
 * A class containing static variables referring to the General ID of codes.
 * 
 */
public class Codes {
    public static final int FILE_DISK = 2;
    public static final int FILE_TAPE = 3;
    public static final int FILE_PIPE = 4;
    public static final int FILE_TOKEN = 5;
    public static final int FILE_DATAB = 6;
    public static final int FILE_REXIT = 7;    
    public static final int FILE_PEXIT = 8;    
    
	/** ACCMETHOD of type Sequential - Standard. */
	public static final int SEQUENTIAL_STANDARD = 1;
	/** ACCMETHOD of type VSAM - Ordered. */
	public static final int VSAM_ORDERED = 3;
	/** ACCMETHOD of type DB2 via SQL. */
	public static final int DB2VIASQL = 6;

	/** DATATYPE of type Alphanumeric. */
	public static final int ALPHANUMERIC = 1;
	/** DATATYPE of type Zoned Decimal. */
	public static final int ZONED_DECIMAL = 3;
	/** DATATYPE of type Packed. */
	public static final int PACKED = 4;
	/** DATATYPE of type Packed Sortable. */
	public static final int PACKED_SORTABLE = 5;
	/** DATATYPE of type Binary. */
	public static final int BINARY = 6;
	/** DATATYPE of type Binary Sortable. */
	public static final int BINARY_SORTABLE = 7;
	/** DATATYPE of type Binary Coded Decimal. */
	public static final int BINARY_CODED_DECIMAL = 8;
	/** DATATYPE of type Masked Numeric. */
	public static final int MASKED_NUMERIC = 9;
	/** DATATYPE of type Edited Numeric. */
	public static final int EDITED_NUMERIC = 10;

	/** EXITTYPE of type Read */
	public static final int READ = 1;
	/** EXITTYPE of type Write */
	public static final int WRITE = 2;
	/** EXITTYPE of type Lookup */
	public static final int LOOKUP = 3;
	/** EXITTYPE of type Format */
	public static final int FORMAT = 4;

	/** FLDCONTENT of type YYMMDD. */
	public static final int YYMMDD = 1;
	/** FLDCONTENT of type YY/MM/DD. */
	public static final int YY_MM_DD = 3;
	/** FLDCONTENT of type CCYY/MM/DD. */
	public static final int CCYY_MM_DD = 4;
	/** FLDCONTENT of type DDMMYY. */
	public static final int DDMMYY = 5;
	/** FLDCONTENT of type DD/MM/YY. */
	public static final int DD_MM_YY = 6;
	/** FLDCONTENT of type DDMMCCYY. */
	public static final int DDMMCCYY = 7;
	/** FLDCONTENT of type DD/MM/CCYY. */
	public static final int DD_MM_CCYY = 8;
	/** FLDCONTENT of type YYDDD. */
	public static final int YYDDD = 9;
	/** FLDCONTENT of type YY/DDD. */
	public static final int YY_DDD = 10;
	/** FLDCONTENT of type CCYYDDD. */
	public static final int CCYYDDD = 11;
	/** FLDCONTENT of type CCYY/DDD. */
	public static final int CCYY_DDD = 12;
	/** FLDCONTENT of type MMDD. */
	public static final int MMDD = 13;
	/** FLDCONTENT of type MM/DD. */
	public static final int MM_DD = 14;
	/** FLDCONTENT of type DD/MM. */
	public static final int DD_MM = 15;
	/** FLDCONTENT of type MM. */
	public static final int MM = 16;
	/** FLDCONTENT of type DD. */
	public static final int DD = 17;
	/** FLDCONTENT of type HHNNSSTT. */
	public static final int HHNNSSTT = 19;
	/** FLDCONTENT of type HH:NN:SS.TT. */
	public static final int HH_NN_SS_TT = 20;
	/** FLDCONTENT of type HHNNSS. */
	public static final int HHNNSS = 21;
	/** FLDCONTENT of type HH:NN:SS. */
	public static final int HH_NN_SS = 22;
	/** FLDCONTENT of type HHNN. */
	public static final int HHNN = 23;
	/** FLDCONTENT of type HH:NN. */
	public static final int HH_NN = 24;
	/** FLDCONTENT of type CCYYMMDDHHNNSS. */
	public static final int CCYYMMDDHHNNSS = 25;
	/** FLDCONTENT of type CCYYMM. */
	public static final int CCYYMM = 30;
	/** FLDCONTENT of type CCYY. */
	public static final int CCYY = 31;
	/** FLDCONTENT of type YY. */
	public static final int YY = 32;
	/** FLDCONTENT of type MMM DD CCYY HH:NN:SS.TT AM/PM. */
	public static final int MMMDDCCYYHH_NN_SS_TTAM_PM = 33;
	/** FLDCONTENT of type CCYY-MM-DD HH:NN:SS.TT. */
	public static final int CCYY_MM_DDHH_NN_SS_TT = 34;
	/** FLDCONTENT of type MMDDYY. */
	public static final int MMDDYY = 35;
	/** FLDCONTENT of type MMDDCCYY. */
	public static final int MMDDCCYY = 36;
	/** FLDCONTENT of type MM/DD/CCYY. */
	public static final int MM_DD_CCYY = 37;
	/** FLDCONTENT of type CCYY-DDD. */
	public static final int CCYY_DDD1 = 38;
	/** FLDCONTENT of type CCYY-MM. */
	public static final int CCYY_MM = 39;
	/** FLDCONTENT of type CCYY-MM-DD. */
	public static final int CCYY_MM_DD1 = 40;
	/** FLDCONTENT of type CCYY/MM. */
	public static final int CCYY_MM1 = 41;
	/** FLDCONTENT of type CCYY/MM/DD HH:NN:SS.TT. */
	public static final int CCYY_MM_DDHH_NN_SS_TT1 = 42;
	/** FLDCONTENT of type DD-MM. */
	public static final int DD_MM1 = 43;
	/** FLDCONTENT of type DD-MM-CCYY. */
	public static final int DD_MM_CCYY1 = 44;
	/** FLDCONTENT of type DD-MM-YY. */
	public static final int DD_MM_YY1 = 45;
	/** FLDCONTENT of type MM-DD. */
	public static final int MM_DD1 = 46;
	/** FLDCONTENT of type MM-DD-CCYY. */
	public static final int MM_DD_CCYY1 = 47;
	/** FLDCONTENT of type YY-DDD. */
	public static final int YY_DDD1 = 48;
	/** FLDCONTENT of type YY-MM-DD. */
	public static final int YY_MM_DD1 = 49;
	/** FLDCONTENT of type MMMM DD, CCYY. */
	public static final int MMMMDD_CCYY = 50;
	/** FLDCONTENT of type DD MMMM CCYY. */
	public static final int DDMMMMCCYY = 51;
	/** FLDCONTENT of type DD-MMM-CCYY. */
	public static final int DD_MMM_CCYY = 52;
	/** FLDCONTENT of type MMMM CCYY. */
	public static final int MMMMCCYY = 53;
	/** FLDCONTENT of type CCYYMMDDHHNNSSTT. */
	public static final int CCYYMMDDHHNNSSTT = 54;

	/** Default Numeric mask (LY12y) for all non alphanumeric columns */
	public static final int DEFAULTNUMERICMASK = 22;

	/** LRSTATUS of type Active */
	public static final int ACTIVE = 1;
	/** LRSTATUS of type Inactive */
	public static final int INACTIVE = 2;

	/** LRTYPE of type Logical File */
	public static final int LOGICAL_FILE = 1;
	/** LRTYPE of type View Specific Structure */
	public static final int VIEW_SPECIFIC_STRUCTURE = 2;

	/** VIEW TYPE */
	public static final int COPY_INPUT = 4;
	public static final int DETAIL = 2;
	public static final int SUMMARY_DT_MERGER = 3;
	public static final int EXTRACT_ONLY = 5;
	public static final int SUMMARY = 1;

	/** OUTPUT FORMAT TYPE */
	public static final int DELIMITED = 7;
	public static final int SPREADSHEET = 5;
	public static final int FILE = 3;
	public static final int HARDCOPY = 1;

	/** SUBTOTAL CODES */
	public static final int FIRST = 5;
	public static final int BREAK_CALCULATION = 8;
	public static final int DETAIL_CALCULATION = 7;
	public static final int DETAIL_FIRST = 12;
	public static final int DETAIL_LAST = 13;
	public static final int DETAIL_MAXIMUM = 10;
	public static final int DETAIL_MINIMUM = 11;
	public static final int LAST = 6;
	public static final int MAXIMUM = 3;
	public static final int MINIMUM = 4;

	/** RECORD AGGREGATION */
	public static final int GROUP_CALCULATION = 7;
	public static final int RECAGGR_FIRST = 5;
	public static final int RECAGGR_LAST = 6;
	public static final int RECAGGR_MAX = 4;
	public static final int RECAGGR_MIN = 3;
	public static final int SUM = 2;

	/** GROUP AGGREGATION */
	public static final int GRPAGGR_GROUP_CALCULATION = 7;
	public static final int GRPAGGR_MAX = 4;
	public static final int GRPAGGR_MIN = 3;
	public static final int GRPAGGR_SUM = 2;
	public static final int GRPAGGR_NONE = 1;

	/** SORT SEQUENCE CODE */
	public static final int ASCENDING = 1;
	public static final int DESCENDING = 2;

	/** COLUMN EXTRACT TYPE CODE */
	public static final int SORTKEY = 1;
	public static final int DT_AREA = 3;
	public static final int CT_AREA = 4;

	/** SORT KEY FOOTER OPTIONS */
	public static final int PRINT = 1;
	public static final int SUPPRESS_PRINT = 0;

	/** COLUMN SOURCE TYPE CODE **/
	public static final int CONSTANT = 1;
	public static final int LOOKUP_FIELD = 2;
	public static final int SOURCE_FILE_FIELD = 3;
	public static final int FORMULA = 4;

	/** EFFECTIVE DATE TYPE CODE **/
	public static final int RELPERIOD_RUNDATE = 1;
	public static final int RELPERIOD_SOURCE_FILE_FIELD = 2;
	public static final int RELPERIOD_CONSTANT = 3;

	/** DISPLAY MODE **/
	public static final int ASDATA = 2;
	public static final int CATEGORIZE = 1;

	/** FIELD DELIM **/
	public static final int BACK_SLASH = 7;
	public static final int COLON = 8;
	public static final int COMMA = 1;
	public static final int CTRL_A = 4;
	public static final int PIPE = 3;
	public static final int SEMICOLON = 9;
	public static final int SLASH = 6;
	public static final int TAB = 2;
	public static final int TILDE = 10;

	/** STRING DELIM **/
	public static final int DOUBLE_QUOTE = 2;
	public static final int NO_STRING_DELIM = 0;
	public static final int SINGLE_QUOTE = 1;

	/** SORT KEY HEADER OPTIONS */
	public static final int PSAME = 0;
	public static final int PNEW = 1;
	public static final int NOPRT = 2;

	/** Alignment Codes **/
	public static final int LEFT = 1;
	public static final int CENTER = 2;
	public static final int RIGHT = 3;

	/** Header Footer Function Codes **/
	public static final int HF_PROCESSDATE = 1;
	public static final int HF_PROCESSTIME = 2;
	public static final int HF_PAGENUMBER = 3;
	public static final int HF_VIEWID = 4;
	public static final int HF_TEXT = 5;
	public static final int HF_COMPANYNAME = 6;
	public static final int HF_VIEWNAME = 7;
	public static final int HF_OWNERUSERID = 8;
	public static final int HF_SORTKEY01LABEL = 11;
	public static final int HF_SORTKEY01TITLE = 13;
	public static final int HF_SORTKEY01VALUE = 12;
	public static final int HF_SORTKEY02LABEL = 21;
	public static final int HF_SORTKEY02TITLE = 23;
	public static final int HF_SORTKEY02VALUE = 22;
	public static final int HF_SORTKEY03LABEL = 31;
	public static final int HF_SORTKEY03TITLE = 33;
	public static final int HF_SORTKEY03VALUE = 32;
	public static final int HF_SORTKEY04LABEL = 41;
	public static final int HF_SORTKEY04TITLE = 43;
	public static final int HF_SORTKEY04VALUE = 42;
	public static final int HF_SORTKEY05LABEL = 51;
	public static final int HF_SORTKEY05TITLE = 53;
	public static final int HF_SORTKEY05VALUE = 52;
	public static final int HF_RUNNUMBER = 501;
	public static final int HF_RUNDATE = 502;
	public static final int HF_FISCALDATE = 503;

	/**
	 * A way to populate codes without needing the database. Only to be used for unit testing
	 * @return
	 */
    public static Map<String, CodeSet> getAllCodeSets() {
        Map<String, CodeSet> allCodeSets = new HashMap<String, CodeSet>();
                
        allCodeSets.put(CodeCategories.ACCMETHOD, getACCMETHODCodeSet());
        allCodeSets.put(CodeCategories.COLSRCTYPE, getCOLSRCTYPECodeSet());
        allCodeSets.put(CodeCategories.DATATYPE, getDATATYPECodeSet());
        allCodeSets.put(CodeCategories.EXTRACT, getEXTRACTCodeSet());
        allCodeSets.put(CodeCategories.FORMATMASK, getFORMATMMASKCodeSet());
        allCodeSets.put(CodeCategories.GROUPAGGR, getGROUPAGGRCodeSet());
        allCodeSets.put(CodeCategories.JUSTIFY, getJUSTIFYCodeSet());
        allCodeSets.put(CodeCategories.RECORDAGGR, getRECORDAGGRCodeSet());
        allCodeSets.put(CodeCategories.RELPERIOD, getRELPERIODCodeSet());
        allCodeSets.put(CodeCategories.SORTBRKFTR, getSORTBRKFTRCodeSet());
        allCodeSets.put(CodeCategories.SORTBRKHDR, getSORTBRKHDRCodeSet());
        allCodeSets.put(CodeCategories.SORTDSP, getSORTDSPCodeSet());
        allCodeSets.put(CodeCategories.SORTSEQ, getSORTSEQCodeSet());
        allCodeSets.put(CodeCategories.VIEWSTATUS, getVIEWSTATUSCodeSet());
        allCodeSets.put(CodeCategories.SUBTOT, getSUBTOTCodeSet());
        allCodeSets.put(CodeCategories.FLDDELIM, getFLDDELIMCodeSet());
        allCodeSets.put(CodeCategories.STRDELIM, getSTRDELIMCodeSet());
        
        return allCodeSets;
    }

    private static CodeSet getSTRDELIMCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer none = new CodeTransfer();        
        none.setGeneralId(0);
        none.setCodeDescription("No String Delimiter");
        none.setCodeValue("NONE");
        transfers.add(none);

        CodeTransfer sngle = new CodeTransfer();        
        sngle.setGeneralId(1);
        sngle.setCodeDescription("Single Quote");
        sngle.setCodeValue("SNGLE");
        transfers.add(sngle);

        CodeTransfer dblqt = new CodeTransfer();        
        dblqt.setGeneralId(2);
        dblqt.setCodeDescription("Double Quote");
        dblqt.setCodeValue("DBLQT");
        transfers.add(dblqt);
        
        return new CodeSet(CodeCategories.STRDELIM, transfers);                
    }

    private static CodeSet getFLDDELIMCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer comma = new CodeTransfer();        
        comma.setGeneralId(1);
        comma.setCodeDescription("Comma");
        comma.setCodeValue("COMMA");
        transfers.add(comma);

        CodeTransfer tab = new CodeTransfer();        
        tab.setGeneralId(2);
        tab.setCodeDescription("TAB");
        tab.setCodeValue("TAB");
        transfers.add(tab);

        CodeTransfer pipe = new CodeTransfer();        
        pipe.setGeneralId(3);
        pipe.setCodeDescription("Pipe");
        pipe.setCodeValue("PIPE");
        transfers.add(pipe);

        CodeTransfer ctrla = new CodeTransfer();        
        ctrla.setGeneralId(4);
        ctrla.setCodeDescription("Ctrl-A");
        ctrla.setCodeValue("CTRLA");
        transfers.add(ctrla);

        CodeTransfer slash = new CodeTransfer();        
        slash.setGeneralId(6);
        slash.setCodeDescription("Slash");
        slash.setCodeValue("SLASH");
        transfers.add(slash);

        CodeTransfer blash = new CodeTransfer();        
        blash.setGeneralId(7);
        blash.setCodeDescription("Back Slash");
        blash.setCodeValue("BLASH");
        transfers.add(blash);

        CodeTransfer colon = new CodeTransfer();        
        colon.setGeneralId(8);
        colon.setCodeDescription("Colon");
        colon.setCodeValue("COLON");
        transfers.add(colon);

        CodeTransfer scoln = new CodeTransfer();        
        scoln.setGeneralId(9);
        scoln.setCodeDescription("Semicolon");
        scoln.setCodeValue("SCOLN");
        transfers.add(scoln);

        CodeTransfer tilde = new CodeTransfer();        
        tilde.setGeneralId(10);
        tilde.setCodeDescription("Tilde");
        tilde.setCodeValue("TILDE");
        transfers.add(tilde);
        
        return new CodeSet(CodeCategories.FLDDELIM, transfers);                
    }

    private static CodeSet getSUBTOTCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer sum = new CodeTransfer();        
        sum.setGeneralId(SUM);
        sum.setCodeDescription("Sum");
        sum.setCodeValue("SUM");
        transfers.add(sum);
        
        CodeTransfer max = new CodeTransfer();        
        max.setGeneralId(MAXIMUM);
        max.setCodeDescription("Maximum");
        max.setCodeValue("MAX");
        transfers.add(max);

        CodeTransfer min = new CodeTransfer();        
        min.setGeneralId(MINIMUM);
        min.setCodeDescription("Minimum");
        min.setCodeValue("MIN");
        transfers.add(min);

        CodeTransfer first = new CodeTransfer();        
        first.setGeneralId(FIRST);
        first.setCodeDescription("First");
        first.setCodeValue("FIRST");
        transfers.add(first);

        CodeTransfer last = new CodeTransfer();        
        last.setGeneralId(LAST);
        last.setCodeDescription("Last");
        last.setCodeValue("LAST");
        transfers.add(last);

        CodeTransfer dcalc = new CodeTransfer();        
        dcalc.setGeneralId(DETAIL_CALCULATION);
        dcalc.setCodeDescription("Detail Calculation");
        dcalc.setCodeValue("DCALC");
        transfers.add(dcalc);

        CodeTransfer bcalc = new CodeTransfer();        
        bcalc.setGeneralId(BREAK_CALCULATION);
        bcalc.setCodeDescription("Break Calculation");
        bcalc.setCodeValue("BCALC");
        transfers.add(bcalc);

        CodeTransfer dmax = new CodeTransfer();        
        dmax.setGeneralId(DETAIL_MAXIMUM);
        dmax.setCodeDescription("Detail Maximum");
        dmax.setCodeValue("DMAX");
        transfers.add(dmax);

        CodeTransfer dmin = new CodeTransfer();        
        dmin.setGeneralId(DETAIL_MINIMUM);
        dmin.setCodeDescription("Detail Minimum");
        dmin.setCodeValue("DMIN");
        transfers.add(dmin);

        CodeTransfer dfirst = new CodeTransfer();        
        dfirst.setGeneralId(DETAIL_FIRST);
        dfirst.setCodeDescription("Detail First");
        dfirst.setCodeValue("DFRST");
        transfers.add(dfirst);

        CodeTransfer dlast = new CodeTransfer();        
        dlast.setGeneralId(DETAIL_LAST);
        dlast.setCodeDescription("Detail Last");
        dlast.setCodeValue("DLAST");
        transfers.add(dlast);
        
        return new CodeSet(CodeCategories.SUBTOT, transfers);        
    }

    private static CodeSet getACCMETHODCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer seqin = new CodeTransfer();        
        seqin.setGeneralId(SEQUENTIAL_STANDARD);
        seqin.setCodeDescription("Sequential");
        seqin.setCodeValue("SEQIN");
        transfers.add(seqin);


        CodeTransfer ksds = new CodeTransfer();
        ksds.setGeneralId(VSAM_ORDERED);
        ksds.setCodeDescription("VSAM - Ordered");
        ksds.setCodeValue("KSDS");
        transfers.add(ksds);

        CodeTransfer db2sq = new CodeTransfer();
        db2sq.setGeneralId(DB2VIASQL);
        db2sq.setCodeDescription("Db2 via SQL");
        db2sq.setCodeValue("DB2SQ");
        transfers.add(db2sq);
        
        return new CodeSet(CodeCategories.ACCMETHOD, transfers);        
    }
    
    private static CodeSet getCOLSRCTYPECodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer const1 = new CodeTransfer();        
        const1.setGeneralId(CONSTANT);
        const1.setCodeDescription("Constant");
        const1.setCodeValue("CONST");
        transfers.add(const1);

        CodeTransfer lkfld = new CodeTransfer();        
        lkfld.setGeneralId(LOOKUP_FIELD);
        lkfld.setCodeDescription("Lookup Field");
        lkfld.setCodeValue("LKFLD");
        transfers.add(lkfld);

        CodeTransfer srcff = new CodeTransfer();        
        srcff.setGeneralId(SOURCE_FILE_FIELD);
        srcff.setCodeDescription("Source File Field");
        srcff.setCodeValue("SRCFF");
        transfers.add(srcff);

        CodeTransfer frmla = new CodeTransfer();        
        frmla.setGeneralId(FORMULA);
        frmla.setCodeDescription("User-Defined Logic");
        frmla.setCodeValue("FRMLA");
        transfers.add(frmla);
        
        return new CodeSet(CodeCategories.COLSRCTYPE, transfers);                
    }
        
    
    private static CodeSet getDATATYPECodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer alnum = new CodeTransfer();        
        alnum.setGeneralId(ALPHANUMERIC);
        alnum.setCodeDescription("Alphanumeric");
        alnum.setCodeValue("ALNUM");
        transfers.add(alnum);

        CodeTransfer numer = new CodeTransfer();        
        numer.setGeneralId(ZONED_DECIMAL);
        numer.setCodeDescription("Zoned Decimal");
        numer.setCodeValue("NUMER");
        transfers.add(numer);

        CodeTransfer packd = new CodeTransfer();        
        packd.setGeneralId(PACKED);
        packd.setCodeDescription("Packed");
        packd.setCodeValue("PACKD");
        transfers.add(packd);
        
        CodeTransfer psort = new CodeTransfer();        
        psort.setGeneralId(PACKED_SORTABLE);
        psort.setCodeDescription("Packed Sortable");
        psort.setCodeValue("PSORT");
        transfers.add(psort);

        CodeTransfer binry = new CodeTransfer();        
        binry.setGeneralId(BINARY);
        binry.setCodeDescription("Binary");
        binry.setCodeValue("BINRY");
        transfers.add(binry);

        CodeTransfer bsort = new CodeTransfer();        
        bsort.setGeneralId(BINARY_SORTABLE);
        bsort.setCodeDescription("Binary Sortable");
        bsort.setCodeValue("BSORT");
        transfers.add(bsort);

        CodeTransfer bcd = new CodeTransfer();        
        bcd.setGeneralId(BINARY_CODED_DECIMAL);
        bcd.setCodeDescription("Binary Coded Decimal");
        bcd.setCodeValue("BCD");
        transfers.add(bcd);

        CodeTransfer msknm = new CodeTransfer();        
        msknm.setGeneralId(MASKED_NUMERIC);
        msknm.setCodeDescription("Masked Numeric");
        msknm.setCodeValue("MSKNUM");
        transfers.add(msknm);

        CodeTransfer ednum = new CodeTransfer();        
        ednum.setGeneralId(EDITED_NUMERIC);
        ednum.setCodeDescription("Edited Numeric");
        ednum.setCodeValue("EDNUM");
        transfers.add(ednum);
        
        return new CodeSet(CodeCategories.DATATYPE, transfers);        
    }
    
    private static CodeSet getEXTRACTCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();

        CodeTransfer sortk = new CodeTransfer();        
        sortk.setGeneralId(SORTKEY);
        sortk.setCodeDescription("Sort Key");
        sortk.setCodeValue("SORTK");
        transfers.add(sortk);

        CodeTransfer sttlk = new CodeTransfer();        
        sttlk.setGeneralId(2);
        sttlk.setCodeDescription("Sort Title Key");
        sttlk.setCodeValue("STTLK");
        transfers.add(sttlk);

        CodeTransfer aredt = new CodeTransfer();        
        aredt.setGeneralId(DT_AREA);
        aredt.setCodeDescription("DT Area");
        aredt.setCodeValue("AREDT");
        transfers.add(aredt);

        CodeTransfer arect = new CodeTransfer();        
        arect.setGeneralId(CT_AREA);
        arect.setCodeDescription("CT Area");
        arect.setCodeValue("ARECT");
        transfers.add(arect);
        
        return new CodeSet(CodeCategories.EXTRACT, transfers);                
    }
    
    private static CodeSet getFORMATMMASKCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer def = new CodeTransfer();        
        def.setGeneralId(DEFAULTNUMERICMASK);
        def.setCodeDescription("-Z,ZZ9.99");
        def.setCodeValue("LY12Y");
        transfers.add(def);
        
        return new CodeSet(CodeCategories.FORMATMASK, transfers);                
    }
    
    private static CodeSet getGROUPAGGRCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer sum = new CodeTransfer();        
        sum.setGeneralId(GRPAGGR_SUM);
        sum.setCodeDescription("Sum");
        sum.setCodeValue("SUM");
        transfers.add(sum);
        
        CodeTransfer min = new CodeTransfer();        
        min.setGeneralId(GRPAGGR_MIN);
        min.setCodeDescription("Minimum");
        min.setCodeValue("MIN");
        transfers.add(min);

        CodeTransfer max = new CodeTransfer();        
        max.setGeneralId(GRPAGGR_MAX);
        max.setCodeDescription("Maximum");
        max.setCodeValue("MAX");
        transfers.add(max);

        CodeTransfer first = new CodeTransfer();        
        first.setGeneralId(GRPAGGR_NONE);
        first.setCodeDescription("First");
        first.setCodeValue("FIRST");
        transfers.add(first);

        CodeTransfer calc = new CodeTransfer();        
        calc.setGeneralId(GRPAGGR_GROUP_CALCULATION);
        calc.setCodeDescription("Group Calculation");
        calc.setCodeValue("CALC");
        transfers.add(calc);

        return new CodeSet(CodeCategories.GROUPAGGR, transfers);        
    }
    
    private static CodeSet getJUSTIFYCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer left = new CodeTransfer();        
        left.setGeneralId(LEFT);
        left.setCodeDescription("Left");
        left.setCodeValue("LEFT");
        transfers.add(left);

        CodeTransfer cnter = new CodeTransfer();        
        cnter.setGeneralId(CENTER);
        cnter.setCodeDescription("Center");
        cnter.setCodeValue("CNTER");
        transfers.add(cnter);

        CodeTransfer right = new CodeTransfer();        
        right.setGeneralId(RIGHT);
        right.setCodeDescription("Right");
        right.setCodeValue("RIGHT");
        transfers.add(right);
        
        return new CodeSet(CodeCategories.JUSTIFY, transfers);        
        
    }

    
    private static CodeSet getRECORDAGGRCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer sum = new CodeTransfer();        
        sum.setGeneralId(SUM);
        sum.setCodeDescription("Sum");
        sum.setCodeValue("SUM");
        transfers.add(sum);
        
        CodeTransfer min = new CodeTransfer();        
        min.setGeneralId(RECAGGR_MIN);
        min.setCodeDescription("Minimum");
        min.setCodeValue("MIN");
        transfers.add(min);

        CodeTransfer max = new CodeTransfer();        
        max.setGeneralId(RECAGGR_MAX);
        max.setCodeDescription("Maximum");
        max.setCodeValue("MAX");
        transfers.add(max);

        CodeTransfer first = new CodeTransfer();        
        first.setGeneralId(RECAGGR_FIRST);
        first.setCodeDescription("First");
        first.setCodeValue("FIRST");
        transfers.add(first);

        CodeTransfer last = new CodeTransfer();        
        last.setGeneralId(RECAGGR_LAST);
        last.setCodeDescription("Last");
        last.setCodeValue("LAST");
        transfers.add(last);

        CodeTransfer calc = new CodeTransfer();        
        calc.setGeneralId(GROUP_CALCULATION);
        calc.setCodeDescription("Group Calculation");
        calc.setCodeValue("CALC");
        transfers.add(calc);
        
        return new CodeSet(CodeCategories.RECORDAGGR, transfers);        
    }

    private static CodeSet getRELPERIODCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer cdate = new CodeTransfer();        
        cdate.setGeneralId(RELPERIOD_RUNDATE);
        cdate.setCodeDescription("Run Date");
        cdate.setCodeValue("CDATE");
        transfers.add(cdate);

        CodeTransfer edate = new CodeTransfer();        
        edate.setGeneralId(RELPERIOD_SOURCE_FILE_FIELD);
        edate.setCodeDescription("Source File Field");
        edate.setCodeValue("EDATE");
        transfers.add(edate);
        
        CodeTransfer specd = new CodeTransfer();        
        specd.setGeneralId(RELPERIOD_CONSTANT);
        specd.setCodeDescription("Constant (in CCYYMMDD format)");
        specd.setCodeValue("SPECD");
        transfers.add(specd);
        
        return new CodeSet(CodeCategories.RELPERIOD, transfers);        
    }
    
    private static CodeSet getSORTBRKFTRCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer noprt = new CodeTransfer();        
        noprt.setGeneralId(SUPPRESS_PRINT);
        noprt.setCodeDescription("Suppress Print");
        noprt.setCodeValue("NOPRT");
        transfers.add(noprt);

        CodeTransfer print = new CodeTransfer();        
        print.setGeneralId(PRINT);
        print.setCodeDescription("Print");
        print.setCodeValue("PRINT");
        transfers.add(print);        
        
        return new CodeSet(CodeCategories.SORTBRKFTR, transfers);        
    }

    private static CodeSet getSORTBRKHDRCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();

        CodeTransfer psame = new CodeTransfer();        
        psame.setGeneralId(PSAME);
        psame.setCodeDescription("Print on Same Page");
        psame.setCodeValue("PSAME");
        transfers.add(psame);        
        
        CodeTransfer pnew = new CodeTransfer();        
        pnew.setGeneralId(PNEW);
        pnew.setCodeDescription("Print on New Page");
        pnew.setCodeValue("PNEW");
        transfers.add(pnew);        
        
        CodeTransfer noprt = new CodeTransfer();        
        noprt.setGeneralId(NOPRT);
        noprt.setCodeDescription("Suppress Print");
        noprt.setCodeValue("NOPRT");
        transfers.add(noprt);

        
        return new CodeSet(CodeCategories.SORTBRKHDR, transfers);        
    }
    
    private static CodeSet getSORTDSPCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer cat = new CodeTransfer();        
        cat.setGeneralId(CATEGORIZE);
        cat.setCodeDescription("Categorize");
        cat.setCodeValue("CAT");
        transfers.add(cat);

        CodeTransfer asdta = new CodeTransfer();        
        asdta.setGeneralId(ASDATA);
        asdta.setCodeDescription("As Data");
        asdta.setCodeValue("ASDTA");
        transfers.add(asdta);
        
        return new CodeSet(CodeCategories.SORTDSP, transfers);        
    }    
    
    private static CodeSet getSORTSEQCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer ascnd = new CodeTransfer();        
        ascnd.setGeneralId(ASCENDING);
        ascnd.setCodeDescription("Ascending");
        ascnd.setCodeValue("ASCND");
        transfers.add(ascnd);

        CodeTransfer dscnd = new CodeTransfer();        
        dscnd.setGeneralId(DESCENDING);
        dscnd.setCodeDescription("Descending");
        dscnd.setCodeValue("DSCND");
        transfers.add(dscnd);
        
        return new CodeSet(CodeCategories.SORTSEQ, transfers);        
    }
    
    private static CodeSet getVIEWSTATUSCodeSet() {
        List<CodeTransfer> transfers = new ArrayList<CodeTransfer>();
        
        CodeTransfer actve = new CodeTransfer();        
        actve.setGeneralId(ACTIVE);
        actve.setCodeDescription("Active");
        actve.setCodeValue("ACTVE");
        transfers.add(actve);

        CodeTransfer inact = new CodeTransfer();        
        inact.setGeneralId(INACTIVE);
        inact.setCodeDescription("Inactive");
        inact.setCodeValue("INACT");
        transfers.add(inact);
        
        return new CodeSet(CodeCategories.VIEWSTATUS, transfers);        
    }

    
    
}
