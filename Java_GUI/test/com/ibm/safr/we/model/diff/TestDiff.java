package com.ibm.safr.we.model.diff;

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


import junit.framework.TestCase;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.view.View;

public class TestDiff extends TestCase {
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void setUp() {
		helper.initDataLayer();
	}
	
	public void tearDown() {
		helper.closeDataLayer();
	}
	
	public void testControlRecord() {
		ControlRecord lcr = null;
		ControlRecord rcr = null;
		try {
			lcr = SAFRApplication.getSAFRFactory().getControlRecord(74);
			rcr = SAFRApplication.getSAFRFactory().getControlRecord(78);
            DiffNode.initGenerated();
			DiffControlRecord rec = new DiffControlRecord(lcr, rcr);
			DiffNode node = rec.generateWholeTree();
			String str1 = node.dumper();
			System.out.print(str1);
			System.out.println("-------------------------------------------------------------------------" + SAFRUtilities.LINEBREAK);
			DiffControlRecord rec2 = new DiffControlRecord(lcr, lcr);
			DiffNode node2 = rec2.generateWholeTree();
			String str2=node2.dumper();
			System.out.print(str2);
			
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}
	
	}
	
	public void testUserExitRoutine() {
		UserExitRoutine lcr = null;
		UserExitRoutine rcr = null;
		try {
			lcr = SAFRApplication.getSAFRFactory().getUserExitRoutine(344, 124);
			rcr = SAFRApplication.getSAFRFactory().getUserExitRoutine(344, 125);
            DiffNode.initGenerated();
			DiffUserExitRoutine rec = new DiffUserExitRoutine(lcr, rcr);
			DiffNode node = rec.generateWholeTree();
			String str1 = node.dumper();
			System.out.print(str1);
			System.out.println("-------------------------------------------------------------------------\n");
			DiffUserExitRoutine rec2 = new DiffUserExitRoutine(lcr, lcr);
			DiffNode node2 = rec2.generateWholeTree();
			String str2=node2.dumper();
			System.out.print(str2);
			
		} catch (SAFRException e1) {
			e1.printStackTrace();
			assertFalse(true);
		}
	
	}

    public void testPhysicalFile() {
        PhysicalFile lpf = null;
        PhysicalFile rpf = null;
        try {
            // test where both pf and exit have changed
            lpf = SAFRApplication.getSAFRFactory().getPhysicalFile(8686,124);
            rpf = SAFRApplication.getSAFRFactory().getPhysicalFile(8686,125);
            DiffPhysicalFile rec = new DiffPhysicalFile(lpf, rpf);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
            // test where referenced has changed to a added exit
            lpf = SAFRApplication.getSAFRFactory().getPhysicalFile(8686,124);
            rpf = SAFRApplication.getSAFRFactory().getPhysicalFile(8686,126);
            DiffNode.initGenerated();
            DiffPhysicalFile rec2 = new DiffPhysicalFile(lpf, rpf);
            DiffNode node2 = rec2.generateWholeTree();
            String str2 = node2.dumper();
            System.out.print(str2);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }

    public void testLogicalFile() {
        LogicalFile lpf = null;
        LogicalFile rpf = null;
        try {
            lpf = SAFRApplication.getSAFRFactory().getLogicalFile(1600,124);
            rpf = SAFRApplication.getSAFRFactory().getLogicalFile(1600,125);
            DiffNode.initGenerated();
            DiffLogicalFile rec = new DiffLogicalFile(lpf, rpf);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }

    public void testLogicalRecord() {
        LogicalRecord llr = null;
        LogicalRecord rlr = null;
        try {
            llr = SAFRApplication.getSAFRFactory().getLogicalRecord(1493,124);
            rlr = SAFRApplication.getSAFRFactory().getLogicalRecord(1493,125);
            DiffNode.initGenerated();
            DiffLogicalRecord rec = new DiffLogicalRecord(llr, rlr);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }

    public void testLookupPath() {
        LookupPath llr = null;
        LookupPath rlr = null;
        try {
            llr = SAFRApplication.getSAFRFactory().getLookupPath(2114,124);
            rlr = SAFRApplication.getSAFRFactory().getLookupPath(2114,125);
            DiffNode.initGenerated();
            DiffLookupPath rec = new DiffLookupPath(llr, rlr);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }

    public void testView() {
        View llr = null;
        View rlr = null;
        try {
            llr = SAFRApplication.getSAFRFactory().getView(8719,124);
            rlr = SAFRApplication.getSAFRFactory().getView(8719,125);
            DiffNode.initGenerated();
            DiffView rec = new DiffView(llr, rlr);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }

    public void testViewRelated() {
        View llr = null;
        View rlr = null;
        try {
            llr = SAFRApplication.getSAFRFactory().getView(8719,124);
            rlr = SAFRApplication.getSAFRFactory().getView(8719,125);
            DiffNode.initGenerated();
            DiffNode.setRelated(false);
            DiffView rec = new DiffView(llr, rlr);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }

    public void testViewWEField() {
        View llr = null;
        View rlr = null;
        try {
            llr = SAFRApplication.getSAFRFactory().getView(8719,124);
            rlr = SAFRApplication.getSAFRFactory().getView(8719,125);
            DiffNode.initGenerated();
            DiffNode.setWEFields(false);
            DiffView rec = new DiffView(llr, rlr);
            DiffNode node = rec.generateWholeTree();
            String str1 = node.dumper();
            System.out.print(str1);
            
        } catch (SAFRException e1) {
            e1.printStackTrace();
            assertFalse(true);
        }
    
    }
    
}
