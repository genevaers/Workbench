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
import java.util.List;

import org.genevaers.wbscript.grammar.WBScriptBaseListener;
import org.genevaers.wbscript.grammar.WBScriptParser.Begin_periodContext;
import org.genevaers.wbscript.grammar.WBScriptParser.CrContext;
import org.genevaers.wbscript.grammar.WBScriptParser.End_periodContext;
import org.genevaers.wbscript.grammar.WBScriptParser.First_fiscalContext;
import org.genevaers.wbscript.grammar.WBScriptParser.SavestmentContext;

import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.base.SAFRComponent;

public class CLIListener extends WBScriptBaseListener {

	private List<String> errors = new ArrayList<>();
    private static SAFRComponent currentComponent = null;
	
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
            currentComponent =  createControlRecord(name);
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
            // TODO Auto-generated method stub
            super.enterEnd_period(ctx);
            ControlRecord cr = (ControlRecord)currentComponent;
            cr.setEndPeriod(Integer.parseInt(ctx.NUM().getText()));
        }

    @Override
    public void enterFirst_fiscal(First_fiscalContext ctx) {
        // TODO Auto-generated method stub
        super.enterFirst_fiscal(ctx);
        ControlRecord cr = (ControlRecord)currentComponent;
        cr.setFirstFiscalMonth(Integer.parseInt(ctx.NUM().getText()));
    }
    
    @Override
    public void enterBegin_period(Begin_periodContext ctx) {
        // TODO Auto-generated method stub
        super.enterBegin_period(ctx);
        ControlRecord cr = (ControlRecord)currentComponent;
        cr.setBeginPeriod(Integer.parseInt(ctx.NUM().getText()));
   }
    
    @Override
    public void enterSavestment(SavestmentContext ctx) {
        super.enterSavestment(ctx);
        currentComponent.validate();
        currentComponent.store();
    }

}
