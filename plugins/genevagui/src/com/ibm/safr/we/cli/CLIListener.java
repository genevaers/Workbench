package com.ibm.safr.we.cli;

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
import java.util.Iterator;
import java.util.List;

import org.genevaers.wbscript.grammar.WBScriptBaseListener;
import org.genevaers.wbscript.grammar.WBScriptParser.Begin_periodContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Col_fld_dataContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Col_lengthContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ColumnContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Column_sourceContext;
import org.genevaers.wbscript.grammar.WBScriptParser.CrContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_constContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_fieldContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_formulaContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Cs_setContext;
import org.genevaers.wbscript.grammar.WBScriptParser.End_periodContext;
import org.genevaers.wbscript.grammar.WBScriptParser.First_fiscalContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LfContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Lf_pfContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LrContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LraddifeldContext;
import org.genevaers.wbscript.grammar.WBScriptParser.LraddlfContext;
import org.genevaers.wbscript.grammar.WBScriptParser.PfContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Pf_access_methodContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Pf_file_typeContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Pf_input_ddContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Pf_min_rec_lenContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Pf_output_ddContext;
import org.genevaers.wbscript.grammar.WBScriptParser.SavestmentContext;
import org.genevaers.wbscript.grammar.WBScriptParser.ViewContext;
import org.genevaers.wbscript.grammar.WBScriptParser.View_sourceContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Vs_log_fileContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Vs_log_recContext;
import org.genevaers.wbscript.grammar.WBScriptParser.Vw_add_crContext;

import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.model.CodeSet;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.utilities.UIUtilities;

public class CLIListener extends WBScriptBaseListener {

    private List<String> errors = new ArrayList<>();
    private ViewColumn currentColumn;
    private ViewSource currentViewSource;
    private ViewColumnSource currentViewColumnSource;
    private int columnIndex;
    private String vsRecord;
    private static SAFRComponent currentComponent = null;
    private static InputDataset pfInput;
    private static OutputDataset pfOutput;

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public void enterCr(CrContext ctx) {
        super.enterCr(ctx);
        String name = ctx.META_REF().getText();
        System.out.println("Need to create CR " + name);
        currentComponent = createControlRecord(name);
    }

    private SAFRComponent createControlRecord(String name) {
        ControlRecord cr = new ControlRecord(SAFRApplication.getUserSession().getEnvironment().getId());
        cr.setName(name);
        cr.setFirstFiscalMonth(1);
        cr.setBeginPeriod(1);
        cr.setEndPeriod(12);
        cr.setComment("Script Created");
        return cr;
    }

    @Override
    public void enterEnd_period(End_periodContext ctx) {
        super.enterEnd_period(ctx);
        ControlRecord cr = (ControlRecord) currentComponent;
        cr.setEndPeriod(Integer.parseInt(ctx.NUM().getText()));
    }

    @Override
    public void enterFirst_fiscal(First_fiscalContext ctx) {
        super.enterFirst_fiscal(ctx);
        ControlRecord cr = (ControlRecord) currentComponent;
        cr.setFirstFiscalMonth(Integer.parseInt(ctx.NUM().getText()));
    }

    @Override
    public void enterBegin_period(Begin_periodContext ctx) {
        super.enterBegin_period(ctx);
        ControlRecord cr = (ControlRecord) currentComponent;
        cr.setBeginPeriod(Integer.parseInt(ctx.NUM().getText()));
    }

    @Override
    public void enterPf(PfContext ctx) {
        super.enterPf(ctx);
        String name = ctx.META_REF().getText();
        System.out.println("Create PF " + name);
        PhysicalFile pf = new PhysicalFile(SAFRApplication.getUserSession().getEnvironment().getId());
        pfInput = pf.new InputDataset();
        pfOutput = pf.new OutputDataset();
        pf.setName(name);
        currentComponent = pf;
    }

    @Override
    public void enterPf_file_type(Pf_file_typeContext ctx) {
        super.enterPf_file_type(ctx);
        CodeSet fileTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FILETYPE);
        PhysicalFile pf = (PhysicalFile) currentComponent;
        pf.setFileTypeCode(fileTypeSet.getCode(ctx.META_REF().getText()));
    }

    @Override
    public void enterPf_access_method(Pf_access_methodContext ctx) {
        super.enterPf_access_method(ctx);
        CodeSet accessMethodSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD);
        PhysicalFile pf = (PhysicalFile) currentComponent;
        pf.setAccessMethod(accessMethodSet.getCode(ctx.META_REF().getText()));
    }

    @Override
    public void enterPf_input_dd(Pf_input_ddContext ctx) {
        super.enterPf_input_dd(ctx);
        pfInput.setInputDDName(ctx.META_REF().getText());
    }

    @Override
    public void enterPf_output_dd(Pf_output_ddContext ctx) {
        super.enterPf_output_dd(ctx);
        pfOutput.setOutputDDName(ctx.META_REF().getText());
    }

    @Override
    public void enterPf_min_rec_len(Pf_min_rec_lenContext ctx) {
        super.enterPf_min_rec_len(ctx);
        pfInput.setMinRecordLen(Integer.parseInt(ctx.NUM().getText()));
    }

    @Override
    public void enterLf(LfContext ctx) {
        super.enterLf(ctx);
        LogicalFile lf = new LogicalFile(SAFRApplication.getUserSession().getEnvironment().getId());
        lf.setName(ctx.META_REF().getText());
        currentComponent = lf;
    }

    @Override
    public void enterLf_pf(Lf_pfContext ctx) {
        super.enterLf_pf(ctx);
        LogicalFile lf = (LogicalFile) currentComponent;
        PhysicalFile pf = SAFRApplication.getSAFRFactory().getPhysicalFile(ctx.META_REF().getText());
        if (pf != null) {
            FileAssociation association = new FileAssociation(lf, pf.getId(), pf.getName(), null);
            lf.addAssociatedPhysicalFile(association);
        }
    }
    
    @Override
        public void enterLr(LrContext ctx) {
            // TODO Auto-generated method stub
            super.enterLr(ctx);
            LogicalRecord lr = new LogicalRecord(SAFRApplication.getUserSession().getEnvironment().getId());
            lr.setName(ctx.META_REF().getText());
            CodeSet lrTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.LRTYPE);
            lr.setLRTypeCode(lrTypeSet.getCode("FILE"));
            lr.setActive(true);
            currentComponent = lr;
        }
    
    @Override
    public void enterLraddifeld(LraddifeldContext ctx) {
        super.enterLraddifeld(ctx);
        LogicalRecord lr = (LogicalRecord)currentComponent;
        LRField f = lr.addField();
        f.setName(ctx.META_REF().getText());
        CodeSet dataTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE);
        f.setDataTypeCode(dataTypeSet.getCode("ALNUM"));
        f.setLength(1);
    }
    
    @Override
    public void enterLraddlf(LraddlfContext ctx) {
        // TODO Auto-generated method stub
        super.enterLraddlf(ctx);
        LogicalRecord lr = (LogicalRecord)currentComponent;
        LogicalFile lf = SAFRApplication.getSAFRFactory().getLogicalFile(ctx.META_REF().getText(), UIUtilities.getCurrentEnvironmentID());
        ComponentAssociation association = new ComponentAssociation(lr, lf.getId(),lf.getName(), null);
        lr.addAssociatedLogicalFile(association);
    }
    
    @Override
    public void enterView(ViewContext ctx) {
        super.enterView(ctx);
        View vw = new View(SAFRApplication.getUserSession().getEnvironment().getId());
        vw.setName(ctx.META_REF().getText());
        vw.setOutputFormat(OutputFormat.Extract_Fixed_Width_Fields);
        currentComponent = vw;
    }
    
    @Override
    public void enterVw_add_cr(Vw_add_crContext ctx) {
        super.enterVw_add_cr(ctx);
        View vw = (View)currentComponent;
        ControlRecord cr = SAFRApplication.getSAFRFactory().getControlRecord(ctx.META_REF().getText(), UIUtilities.getCurrentEnvironmentID());
        vw.setControlRecord(cr);
    }
    
    @Override
    public void enterColumn(ColumnContext ctx) {
        super.enterColumn(ctx);
        View vw = (View)currentComponent;
        currentColumn = vw.addViewColumn(vw.getViewColumns().size()+1);
    }
    
    @Override
    public void enterCol_fld_data(Col_fld_dataContext ctx) {
        super.enterCol_fld_data(ctx);
        CodeSet dataTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.DATATYPE);
        currentColumn.setDataTypeCode(dataTypeSet.getCode(ctx.META_REF().getText()));
    }
    
    @Override
    public void enterCol_length(Col_lengthContext ctx) {
        super.enterCol_length(ctx);
        currentColumn.setLength(Integer.valueOf(Integer.parseInt(ctx.NUM().getText())));
    }
    
    

    @Override
    public void enterSavestment(SavestmentContext ctx) {
        super.enterSavestment(ctx);
        currentComponent.validate();
        currentComponent.store();
    }
    
    @Override
    public void enterView_source(View_sourceContext ctx) {
        super.enterView_source(ctx);
        View vw = (View)currentComponent;
        currentViewSource = vw.addViewSource();
        currentColumn = null;
        currentViewColumnSource = null;
        columnIndex = 0;
    }
    
    @Override
    public void enterVs_log_rec(Vs_log_recContext ctx) {
        super.enterVs_log_rec(ctx);
        vsRecord = ctx.META_REF().getText();
    }
    
    @Override
    public void enterVs_log_file(Vs_log_fileContext ctx) {
        super.enterVs_log_file(ctx);
        View vw = (View)currentComponent;
        setViewSource(vw, vsRecord, ctx.META_REF().getText());
    }

    private void setViewSource(View vw, String vsRecord, String lf) {
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
    
    @Override
    public void enterCs_set(Cs_setContext ctx) {
        super.enterCs_set(ctx);
        View vw = (View)currentComponent;
        currentViewColumnSource = vw.getViewColumnSources().get(columnIndex);
        columnIndex++;
    }
    
    @Override
    public void enterCs_const(Cs_constContext ctx) {
        super.enterCs_const(ctx);
        if(currentViewColumnSource != null) {
            CodeSet sourceTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.COLSRCTYPE);
            currentViewColumnSource.setSourceType(sourceTypeSet.getCode("FRMLA"));
            currentViewColumnSource.setExtractColumnAssignmentBasic("COLUMN = " + ctx.META_REF().getText() + "");
        } else {
            //Yikes
        }
    }
    
    @Override
    public void enterCs_field(Cs_fieldContext ctx) {
        super.enterCs_field(ctx);
        if(currentViewColumnSource != null) {
            CodeSet sourceTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.COLSRCTYPE);
            currentViewColumnSource.setSourceType(sourceTypeSet.getCode("FRMLA"));
            currentViewColumnSource.setExtractColumnAssignmentBasic("COLUMN = {" + ctx.META_REF().getText() + "}");
        } else {
            //Yikes
        }
    }
    
}
