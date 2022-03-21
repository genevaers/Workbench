package com.ibm.safr.we.model.logic;

import java.util.Arrays;

import com.ibm.safr.we.constants.LogicTextType;

public class LogicTextParserExtractCalc extends LogicTextParser {

    public LogicTextParserExtractCalc() {
        super();
        this.logicType = LogicTextType.Extract_Column_Assignment;
        initializeKeywordArray();
    }

    private void initializeKeywordArray() {
        String[] arr = new String[] { "IF", "THEN", "ELSE", "ENDIF",
            "ISFOUND", "COLUMN", "COL", "ALL", "TIMESTAMP", "BATCHDATE", "REPEAT","SUBSTR","LEFT","RIGHT",
            "FISCALYEAR", "FISCALMONTH", "FISCALDAY",
            "CURRENT", "PRIOR", "DATE", 
            "DAYSBETWEEN", "MONTHSBETWEEN", "YEARSBETWEEN",  
            "RUNYEAR", "RUNMONTH", "RUNDAY", 
            "ISFOUND", "ISNOTFOUND", "ISNULL", "ISNOTNULL", "ISNUMERIC",
            "ISNOTNUMERIC", "ISSPACES", "ISNOTSPACES", "WRITE", "SOURCE", "VIEW",
            "INPUT", "DATA", "DEST", "DESTINATION", "EXTRACT", "EXT",
            "PROCEDURE", "USEREXIT", "FILE" };
        keywords.addAll(Arrays.asList(arr));
    }
}
