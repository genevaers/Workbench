package com.ibm.safr.we.cli;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRComponent;

public class FieldHandler extends SetHandler{

    private static String name;
    private static String value;

    // need to parse the field name and call the appropriate function
    // ... so SetHandler per Component? - derived from this
    public static void set(SAFRComponent comp, String[] words) {
        LRField fld = (LRField)comp;
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
        }
    }
}
