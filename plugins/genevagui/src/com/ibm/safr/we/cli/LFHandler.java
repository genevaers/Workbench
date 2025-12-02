package com.ibm.safr.we.cli;

import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.base.SAFRComponent;

public class LFHandler extends SetHandler{

    private static String name;
    private static String value;

    // need to parse the field name and call the appropriate function
    // ... so SetHandler per Component? - derived from this
    public static void set(SAFRComponent comp, String[] words) {
        LogicalFile lf = (LogicalFile)comp;
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

    public static void add(SAFRComponent comp, String[] words) {
        LogicalFile lf = (LogicalFile)comp;
        name = words[1];
        PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(name);
        if(pf != null) {
            FileAssociation association = new FileAssociation(lf, pf.getId(), pf.getName(), null);
            lf.addAssociatedPhysicalFile(association);
        }
    }

}
