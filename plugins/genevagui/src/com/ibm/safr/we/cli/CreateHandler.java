package com.ibm.safr.we.cli;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRComponent;

public class CreateHandler {

    public static SAFRComponent  create(String[] words) {
        String name = "";//switch (cr) {
        //if(words[1].startsWith("\"")) {
            int nextWord = 1;
            if(words.length == 2) {
                name = words[nextWord++];
            } else {
                if(words[nextWord].equalsIgnoreCase("view")) {
                    name = words[nextWord++];
                } else {
                    name = words[nextWord++] + " " + words[nextWord++];
                }
            }
        //}
        if(ComponentType.ControlRecord.getLabel().equals(name)) {
            ControlRecord cr = new ControlRecord(SAFRApplication.getUserSession().getEnvironment().getId());
            cr.setName(words[nextWord]);
            cr.setFirstFiscalMonth(1);
            cr.setBeginPeriod(1);
            cr.setEndPeriod(12);
            cr.setComment("Script Created");
            return cr;
        } else if(ComponentType.PhysicalFile.getLabel().equals(name)) {
            PhysicalFile pf = new PhysicalFile(SAFRApplication.getUserSession().getEnvironment().getId());
            pf.setName(words[nextWord]);
            return pf;
        } else if(ComponentType.LogicalFile.getLabel().equals(name)) {
            LogicalFile lf = new LogicalFile(SAFRApplication.getUserSession().getEnvironment().getId());
            lf.setName(words[nextWord]);
            return lf;
        } else if(ComponentType.LogicalRecord.getLabel().equals(name)) {
            LogicalRecord lr = new LogicalRecord(SAFRApplication.getUserSession().getEnvironment().getId());
            lr.setName(words[nextWord]);
            CodeSet lrTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.LRTYPE);
            lr.setLRTypeCode(lrTypeSet.getCode("FILE"));
            lr.setActive(true);
            return lr;
        } else if(ComponentType.View.getLabel().equals(name)) {
            View vw = new View(SAFRApplication.getUserSession().getEnvironment().getId());
            vw.setName(words[nextWord]);
            vw.setOutputFormat(OutputFormat.Extract_Fixed_Width_Fields);
//            CodeSet lrTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.LRTYPE);
//            vw.setLRTypeCode(lrTypeSet.getCode("FILE"));
//            vw.setActive(true);
            return vw;
        } else {
            return null;
        }
    }
}
