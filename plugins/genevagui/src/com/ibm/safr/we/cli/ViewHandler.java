package com.ibm.safr.we.cli;

import java.util.Iterator;

import com.ibm.safr.we.cli.SetHandler;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class ViewHandler extends SetHandler{

    private static String name;
    private static String value;
    private static int columnIndex;
    private static ViewColumn currentColumn;
    private static ViewSource currentViewSource;
    private static String vsRecord;
    private static ViewColumnSource currentViewColumnSource;

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
//            } else if(words[nextWord].equalsIgnoreCase("column")) {
//                name = words[nextWord++];
//                setColumnValue(name, words[nextWord++]);
            } else if(words[nextWord].equalsIgnoreCase("logical")) {
                nextWord++;
                name = words[nextWord++];
                if(name.equalsIgnoreCase("record")){
                    vsRecord = words[nextWord++];
                } else if(name.equalsIgnoreCase("file")){
                    setViewSource(vw, vsRecord, words[nextWord++]);
                }
            } else {
                if(words.length == 3) {
                    name = words[nextWord++];
                } else if(words.length == 4) {
                    name = words[nextWord++] + " " + words[nextWord++];
                } else {
                    name = words[nextWord++] + " " + words[nextWord++] + " " + words[nextWord++];
                }
            }

            if(currentColumn != null) {
                setColumnValue(name, words[nextWord++]);
            }
            if(currentViewColumnSource != null) {
                setViewColumnSourceValue(name, words[words.length - 1]);
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

    //Little bit tricky here
    //Do we want to lookup fields by name
    //Or always set to a formula with the correct logic
    //Then don't need to lookup field name
    private static void setViewColumnSourceValue(String name, String value) {
        if(currentViewColumnSource != null) {
            CodeSet sourceTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.COLSRCTYPE);
            currentViewColumnSource.setSourceType(sourceTypeSet.getCode("FRMLA"));
            switch(name.toLowerCase()) {
            case "type source field":
                currentViewColumnSource.setExtractColumnAssignmentBasic("COLUMN = {" + value + "}");
                break;
            case "type constant":
                currentViewColumnSource.setExtractColumnAssignmentBasic("COLUMN = " + value + "");
                break;
            }
        } else {
            //Yikes
        }
    }

    private static void setViewSource(View vw, String vsRecord, String lf) {
        if(currentViewSource != null) {
            //Get the Logical File Associations for vsRecord
            LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(vsRecord);
            SAFRAssociationList<ComponentAssociation> logicalFileAssociations = SAFRAssociationFactory
                    .getLogicalRecordToLogicalFileAssociations(
                            lr.getId(), UIUtilities.getCurrentEnvironmentID());
            //Find the one that has lf -> In trouble if not found
            Iterator<ComponentAssociation> lfai = logicalFileAssociations.iterator();
            ComponentAssociation ourLrLfAssoc = null;
            while(lfai.hasNext() && ourLrLfAssoc == null) {
                ComponentAssociation ca = lfai.next();
                if(ca.getAssociatedComponentName().equalsIgnoreCase(lf)) {
                    ourLrLfAssoc = ca;
                }
            }
            //Add the association to the viewSource
            if(ourLrLfAssoc != null) {
                currentViewSource.setLrFileAssociation(ourLrLfAssoc);
                currentViewSource.setExtractRecordOutput("WRITE(SOURCE=DATA,DEST=DEFAULT)");
            } else {
                //In trouble again
                System.err.printf("Cannot set view source for view %s lr %s lf %s\n", vw.getName(), vsRecord, lf);
            }
        } else {
            //Yikes
        }
        
    }

    public static SAFRComponent add(SAFRComponent comp, String[] words) {
        View vw = (View)comp;
        if(words[1].equalsIgnoreCase("control")) {
            ControlRecord cr = SAFRApplication.getSAFRFactory().getControlRecord(words[3], UIUtilities.getCurrentEnvironmentID());
            vw.setControlRecord(cr);
            return null;
        } else if(words[1].equalsIgnoreCase("column")) {
            currentColumn = vw.addViewColumn(vw.getViewColumns().size()+1);
            return currentColumn;
        } else if(words[2].equalsIgnoreCase("source")) {
            currentViewSource = vw.addViewSource();
            currentColumn = null;
            currentViewColumnSource = null;
            columnIndex = 0;
            return currentViewSource;
        } else if(words[2].equalsIgnoreCase("column")) {
            currentViewColumnSource = vw.getViewColumnSources().get(columnIndex);
            columnIndex++;
            return currentViewColumnSource;
        } else {
            return null;            
        }
        
    }
    
    public static void setColumnIndex(int columnIndex) {
        ViewHandler.columnIndex = columnIndex;
    }

    private static void setColumnValue(String name,String value) {
        if(currentColumn != null) {
            switch(name.toLowerCase()) {
            case "data type":
                CodeSet dataTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE);
                currentColumn.setDataTypeCode(dataTypeSet.getCode(value));
                break;
            case "length":
                currentColumn.setLength(Integer.valueOf(value));
                break;
            }
        }
    }

}
