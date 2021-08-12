package com.ibm.safr.we.model.logic;

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

public class TestLogicTextParserFind extends TestCase {
    
    public void testFindBegin()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "COLUMN = {LONG_DESC}",            
            "IF {FISCAL_YR_CONTENT} = RUNYEAR() THEN",
            "WRITE(SOURCE=DATA,DESTINATION=EXTRACT=30)"
        ));
        assertEquals(parser.getTokenMap().size(), 3);
        
        LogicToken token = parser.findToken(0, 0);
        assertNull(token);
    }

    public void testFindEnd()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "COLUMN = {LONG_DESC}",            
            "IF {FISCAL_YR_CONTENT} = RUNYEAR() THEN",
            "WRITE(SOURCE=DATA,DESTINATION=EXTRACT=30)"
        ));
        assertEquals(parser.getTokenMap().size(), 3);
        
        LogicToken token = parser.findToken(41, 41);
        assertNull(token);
    }
    
    public void testFindSource()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "COLUMN = {LONG_DESC}",            
            "IF {FISCAL_YR_CONTENT} = RUNYEAR() THEN",
            "WRITE(SOURCE=DATA,DESTINATION=EXTRACT=30)"
        ));
        assertEquals(parser.getTokenMap().size(), 3);
        
        LogicToken token = parser.findToken(63+6, 63+12);
        assertNotNull(token);
        assertEquals(token.getString(), "SOURCE");
    }
    
    public void testFindBracket()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "COLUMN = {LONG_DESC}",            
            "IF {FISCAL_YR_CONTENT} = RUNYEAR() THEN",
            "WRITE(SOURCE=DATA,DESTINATION=EXTRACT=30)"
        ));
        assertEquals(parser.getTokenMap().size(), 3);
        
        LogicToken token = parser.findToken(22+3, 22+4);
        assertNotNull(token);
        assertEquals(token.getString(), "{");
        assertEquals(token.getGroup().size(),2);
       
    }
    
}
