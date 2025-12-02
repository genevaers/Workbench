package com.ibm.safr.we.cli;

import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRField;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class LRHandler extends SetHandler{

    private static String name;
    private static String value;

    // need to parse the field name and call the appropriate function
    // ... so SetHandler per Component? - derived from this
    public static void set(SAFRComponent comp, String[] words) {
        LogicalRecord lf = (LogicalRecord)comp;
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
            
        switch(name.toLowerCase()) {
        case "comments":
            lf.setComment(value);
            break;
        }
    }

    public static SAFRField add(SAFRComponent comp, String[] words) {
        LogicalRecord lr = (LogicalRecord)comp;
        if(words[1].equalsIgnoreCase("field")) {
            LRField f = lr.addField();
            name = words[2];
            f.setName(name);
            CodeSet dataTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE);
            f.setDataTypeCode(dataTypeSet.getCode("ALNUM"));
            f.setLength(1);
            return f;
        } else {
            //Assume logical file
            LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(words[3], UIUtilities.getCurrentEnvironmentID());
            ComponentAssociation association = new ComponentAssociation(lr, lf.getId(),
                    lf.getName(), null);
            lr.addAssociatedLogicalFile(association);
            return null;
        }
        
    }

}
