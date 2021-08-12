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

public class TestLogicTextParserBracket extends TestCase {

    public void testCBracket()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "{inside.bracket1}",            
            "{  inside.bracket2}"            
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 3);    
        LogicToken lbracket = parser.getTokenMap().get(0).get(0);
        assertEquals(lbracket.getString(), "{");
        assertEquals(lbracket.getType(), LogicTokenType.LCBRACKET);        
        LogicToken ibracket = parser.getTokenMap().get(0).get(1);
        assertEquals(ibracket.getString(), "inside.bracket1");
        assertEquals(ibracket.getType(), LogicTokenType.INCBRACKET);        
        LogicToken rbracket = parser.getTokenMap().get(0).get(2);
        assertEquals(rbracket.getString(), "}");
        assertEquals(rbracket.getType(), LogicTokenType.RCBRACKET);                

        assertTrue(lbracket.getGroup() == ibracket.getGroup());
        assertTrue(ibracket.getGroup() == rbracket.getGroup());
        
        assertEquals(parser.getTokenMap().get(19).size(), 3);    
        LogicToken lbracket2 = parser.getTokenMap().get(19).get(0);
        assertEquals(lbracket2.getString(), "{");
        assertEquals(lbracket2.getType(), LogicTokenType.LCBRACKET);        
        LogicToken ibracket2 = parser.getTokenMap().get(19).get(1);
        assertEquals(ibracket2.getString(), "  inside.bracket2");
        assertEquals(ibracket2.getType(), LogicTokenType.INCBRACKET);        
        LogicToken rbracket2 = parser.getTokenMap().get(19).get(2);
        assertEquals(rbracket2.getString(), "}");
        assertEquals(rbracket2.getType(), LogicTokenType.RCBRACKET);                

        assertTrue(lbracket2.getGroup() == ibracket2.getGroup());
        assertTrue(ibracket2.getGroup() == rbracket2.getGroup());
    }
    
    public void testUnfinishedCBracket()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "{inside.bracket1",            
            "{  inside.bracket2"            
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 2);    
        LogicToken lbracket = parser.getTokenMap().get(0).get(0);
        assertEquals(lbracket.getString(), "{");
        assertEquals(lbracket.getType(), LogicTokenType.LCBRACKET);        
        LogicToken ibracket = parser.getTokenMap().get(0).get(1);
        assertEquals(ibracket.getString(), "inside.bracket1");
        assertEquals(ibracket.getType(), LogicTokenType.INCBRACKET);        
        assertTrue(lbracket.getGroup() == ibracket.getGroup());

        assertEquals(parser.getTokenMap().get(18).size(), 2);    
        LogicToken lbracket2 = parser.getTokenMap().get(18).get(0);
        assertEquals(lbracket2.getString(), "{");
        assertEquals(lbracket2.getType(), LogicTokenType.LCBRACKET);        
        LogicToken ibracket2 = parser.getTokenMap().get(18).get(1);
        assertEquals(ibracket2.getString(), "  inside.bracket2");
        assertEquals(ibracket2.getType(), LogicTokenType.INCBRACKET);        
        assertTrue(lbracket2.getGroup() == ibracket2.getGroup());
    }    

    public void testLotsCBrackets()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse("{{{{{{{}}}}}}}}}");
        assertEquals(parser.getTokenMap().size(),1);
        assertEquals(parser.getTokenMap().get(0).size(), 11);
        LogicToken lbracket = parser.getTokenMap().get(0).get(0);
        assertEquals(lbracket.getString(), "{");
        assertEquals(lbracket.getType(), LogicTokenType.LCBRACKET);        
        LogicToken ibracket = parser.getTokenMap().get(0).get(1);
        assertEquals(ibracket.getString(), "{{{{{{");
        assertEquals(ibracket.getType(), LogicTokenType.INCBRACKET);        
        LogicToken rbracket = parser.getTokenMap().get(0).get(2);
        assertEquals(rbracket.getString(), "}");
        assertEquals(rbracket.getType(), LogicTokenType.RCBRACKET);        
        LogicToken other = parser.getTokenMap().get(0).get(3);
        assertEquals(other.getString(), "}");
        assertEquals(other.getType(), LogicTokenType.OTHER);                
    }
    
    public void testBracket()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "(inside.bracket1)",            
            "(  inside.bracket2)"            
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 5);    
        LogicToken lbracket = parser.getTokenMap().get(0).get(0);
        assertEquals(lbracket.getString(), "(");
        assertEquals(lbracket.getType(), LogicTokenType.LBRACKET);        
        LogicToken word1 = parser.getTokenMap().get(0).get(1);
        assertEquals(word1.getString(), "inside");
        assertEquals(word1.getType(), LogicTokenType.WORD);        
        LogicToken other = parser.getTokenMap().get(0).get(2);
        assertEquals(other.getString(), ".");
        assertEquals(other.getType(), LogicTokenType.OTHER);        
        LogicToken word2 = parser.getTokenMap().get(0).get(3);
        assertEquals(word2.getString(), "bracket1");
        assertEquals(word2.getType(), LogicTokenType.WORD);        
        LogicToken rbracket = parser.getTokenMap().get(0).get(4);
        assertEquals(rbracket.getString(), ")");
        assertEquals(rbracket.getType(), LogicTokenType.RBRACKET);                

        assertTrue(lbracket.getGroup() == rbracket.getGroup());
        
        assertEquals(parser.getTokenMap().get(19).size(), 6);    
        lbracket = parser.getTokenMap().get(19).get(0);
        assertEquals(lbracket.getString(), "(");
        assertEquals(lbracket.getType(), LogicTokenType.LBRACKET);        
        LogicToken white = parser.getTokenMap().get(19).get(1);
        assertEquals(white.getString(), "  ");
        assertEquals(white.getType(), LogicTokenType.WHITE);        
        word1 = parser.getTokenMap().get(19).get(2);
        assertEquals(word1.getString(), "inside");
        assertEquals(word1.getType(), LogicTokenType.WORD);        
        other = parser.getTokenMap().get(19).get(3);
        assertEquals(other.getString(), ".");
        assertEquals(other.getType(), LogicTokenType.OTHER);        
        word2 = parser.getTokenMap().get(19).get(4);
        assertEquals(word2.getString(), "bracket2");
        assertEquals(word2.getType(), LogicTokenType.WORD);        
        rbracket = parser.getTokenMap().get(19).get(5);
        assertEquals(rbracket.getString(), ")");
        assertEquals(rbracket.getType(), LogicTokenType.RBRACKET);                

        assertTrue(lbracket.getGroup() == rbracket.getGroup());
    }
    
    public void testBracketNesting()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "((inside(bracket1",            
            "inside)bracket2))"            
        ));
        assertEquals(parser.getTokenMap().size(),2);
        LogicToken lbracket1 = parser.getTokenMap().get(0).get(0);
        LogicToken lbracket2 = parser.getTokenMap().get(0).get(1);
        LogicToken lbracket3 = parser.getTokenMap().get(0).get(3);
        
        LogicToken rbracket1 = parser.getTokenMap().get(19).get(1);
        LogicToken rbracket2 = parser.getTokenMap().get(19).get(3);
        LogicToken rbracket3 = parser.getTokenMap().get(19).get(4);
        
        assertEquals(lbracket1.getGroup().size(),2);
        assertEquals(lbracket2.getGroup().size(),2);
        assertEquals(lbracket3.getGroup().size(),2);

        assertEquals(lbracket1.getGroup(),rbracket3.getGroup());
        assertEquals(lbracket2.getGroup(),rbracket2.getGroup());
        assertEquals(lbracket3.getGroup(),rbracket1.getGroup());

    }
}
