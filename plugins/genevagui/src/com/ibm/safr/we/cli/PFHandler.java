package com.ibm.safr.we.cli;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.base.SAFRComponent;

public class PFHandler extends SetHandler{

    private static String name;
    private static String value;
    private static InputDataset pfInput;
    private static OutputDataset pfOutput;

    // need to parse the field name and call the appropriate function
    // ... so SetHandler per Component? - derived from this
    public static void set(SAFRComponent comp, String[] words) {
        PhysicalFile pf = (PhysicalFile)comp;
        //if(words[1].startsWith("\"")) {
            int nextWord = 1;
            if(words[nextWord].equalsIgnoreCase("comments")) {
                name = words[nextWord++];
                StringBuilder cmnts = new StringBuilder();
                cmnts.append(words[nextWord++]);
                while(nextWord < words.length) {
                    cmnts.append(" " + words[nextWord++]);
                }
                value = cmnts.toString();
            } else {
            if(words.length == 3) {
                name = words[nextWord++];
            } else if(words.length == 4) {
                name = words[nextWord++] + " " + words[nextWord++];
            } else {
                name = words[nextWord++] + " " + words[nextWord++] + " " + words[nextWord++];
            }
            }
        //}
            
        //Verify Set - no already done
        //get name and value
        //name = words[1];
        //value = words[2];  //Can be "w w w w"?
        switch(name.toLowerCase()) {
        case "file type":
            CodeSet fileTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FILETYPE);
            pf.setFileTypeCode(fileTypeSet.getCode(words[nextWord++]));
            break;
        case "access method":
            CodeSet accessMethodSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD);
            pf.setAccessMethod(accessMethodSet.getCode(words[nextWord++]));
            break;
//                public void setUserExitRoutine(UserExitRoutine userExitRoutine) {
//                    public void setUserExitRoutineParams(String readExitParams) {
        //    break;
        case "data set name":
            pfInput.setDatasetName(words[nextWord++]);
            break;
        case "input dd name":
            pfInput.setInputDDName(words[nextWord++]);
            break;
        case "min record length":
            pfInput.setMinRecordLen(Integer.valueOf(words[nextWord]));
            break;
        case "max record length":
            pfInput.setMaxRecordLen(Integer.valueOf(words[nextWord]));
            break;
        case "output dd name":
            pfOutput.setOutputDDName(words[nextWord++]);
            break;
        case "recfm":
            CodeSet recfmSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.RECFM);
            pfOutput.setRecfm(recfmSet.getCode(words[nextWord++]));
            break;
        case "lrecl":
            pfOutput.setLrecl(Integer.valueOf(words[nextWord]));
            break;
        case "db2 subsystem":
            break;
        case "sql":
            break;
    }
    }

    public static void setPfInput(InputDataset pfInput) {
        if(PFHandler.pfInput == null) {
            PFHandler.pfInput = pfInput;
        }
    }

    public static void setPfOutput(OutputDataset pfOutput) {
        if(PFHandler.pfOutput == null) {
            PFHandler.pfOutput = pfOutput;
        }
    }
    
    
}
