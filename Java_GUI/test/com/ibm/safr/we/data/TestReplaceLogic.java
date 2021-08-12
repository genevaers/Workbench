package com.ibm.safr.we.data;

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


import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.transfer.FindTransfer;

public class TestReplaceLogic {

    TestDataLayerHelper helper = new TestDataLayerHelper();
    Integer nextId = null;
    
    
    @Test
    public void testReplaceLogic() {
        try {
            helper.initDataLayer(1);
        } catch (DAOException e) {
            assertFalse(true);
        }
        List<FindTransfer> replacements = new ArrayList<FindTransfer>();        
        FindTransfer trans = new FindTransfer();
        trans.setViewId(2041);
        trans.setLogicTextType(LogicTextType.Format_Record_Filter);
        trans.setCellId(null);
        trans.setLogicText("This is my test now && <\\ Format filter");
        replacements.add(trans);
        FindTransfer trans2 = new FindTransfer();
        trans2.setViewId(2041);
        trans2.setLogicTextType(LogicTextType.Extract_Record_Filter);
        trans2.setCellId(2270);
        trans2.setLogicText("This is my test now && <\\ Extract filter");
        replacements.add(trans2);
        FindTransfer trans3 = new FindTransfer();
        trans3.setViewId(2041);
        trans3.setLogicTextType(LogicTextType.Format_Column_Calculation);
        trans3.setCellId(16377);
        trans3.setLogicText("Column Calc 2");
        replacements.add(trans3);
        FindTransfer trans4 = new FindTransfer();
        trans4.setViewId(2041);
        trans4.setLogicTextType(LogicTextType.Extract_Column_Assignment);
        trans4.setCellId(38920);
        trans4.setLogicText("Extract Column Calc");
        replacements.add(trans4);
        
        DAOFactoryHolder.getDAOFactory().getViewDAO().replaceLogicText(1, replacements);

    }

}
