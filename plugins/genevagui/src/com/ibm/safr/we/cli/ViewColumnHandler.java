package com.ibm.safr.we.cli;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRField;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.base.SAFRComponent;

public class ViewColumnHandler extends SetHandler{

    private static String name;
    private static String value;
    private static int columnIndex;

    // need to parse the field name and call the appropriate function
    // ... so SetHandler per Component? - derived from this
    public static void set(SAFRComponent comp, String[] words) {
        View vw = (View)comp;
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
            } else if(words[nextWord].equalsIgnoreCase("column")) {
                name = words[nextWord++];
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
        case "comments":
            vw.setComment(value);
            break;
        }
    }

    public static SAFRField add(SAFRComponent comp, String[] words) {
        View vw = (View)comp;
        if(words[1].equalsIgnoreCase("control")) {
            ControlRecord cr = SAFRApplication.getSAFRFactory().getControlRecord(words[3], UIUtilities.getCurrentEnvironmentID());
            vw.setControlRecord(cr);
            return null;
        } else if(words[1].equalsIgnoreCase("column")) {
            //Assume logical file
            return vw.addViewColumn(columnIndex++);
        } else {
            return null;            
        }
        
    }
    
    public static void setColumnIndex(int columnIndex) {
        ViewColumnHandler.columnIndex = columnIndex;
    }
}
