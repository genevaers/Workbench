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

public class TestLogicTextParser extends TestCase {

    public void testEmpty()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse("");
        assertTrue(parser.getTokenMap().isEmpty());
    }

    public void testLineEnding()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(SAFRUtilities.LINEBREAK);
        assertEquals(parser.getTokenMap().size(),1);
        assertTrue(parser.getTokenMap().get(0).isEmpty());
    }

    public void testComment()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "'First comment",            
            "'Second comment" // line offset
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 1);
        LogicToken firstComment = parser.getTokenMap().get(0).get(0);
        assertEquals(firstComment.getType(),LogicTokenType.COMMENT);
        assertEquals(firstComment.getString(), "'First comment");
        assertEquals(parser.getTokenMap().get(16).size(), 1);
        LogicToken secondComment = parser.getTokenMap().get(16).get(0);
        assertEquals(secondComment.getType(),LogicTokenType.COMMENT);
        assertEquals(secondComment.getString(), "'Second comment");
    }

    public void testString()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "\"First string\"",            
            "\"Second string\"" // line offset
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 1);
        LogicToken firstString = parser.getTokenMap().get(0).get(0);
        assertEquals(firstString.getType(),LogicTokenType.STRING);
        assertEquals(firstString.getString(), "\"First string\"");
        assertEquals(parser.getTokenMap().get(16).size(), 1);
        LogicToken secondString = parser.getTokenMap().get(16).get(0);
        assertEquals(secondString.getType(),LogicTokenType.STRING);
        assertEquals(secondString.getString(), "\"Second string\"");
    }

    public void testNumber()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "199",            
            "2707" // line offset
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 1);
        LogicToken firstNum = parser.getTokenMap().get(0).get(0);
        assertEquals(firstNum.getType(),LogicTokenType.NUMBER);
        assertEquals(firstNum.getString(), "199");
        assertEquals(parser.getTokenMap().get(5).size(), 1);
        LogicToken secondNum = parser.getTokenMap().get(5).get(0);
        assertEquals(secondNum.getType(),LogicTokenType.NUMBER);
        assertEquals(secondNum.getString(), "2707");
    }
    
    public void testWord()
    {
        LogicTextParser parser = new LogicTextParserExtractCalc();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "FISCALDAY",            
            "FESCALDAY" // line offset
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 1);
        LogicToken firstNum = parser.getTokenMap().get(0).get(0);
        assertEquals(firstNum.getType(),LogicTokenType.KEY);
        assertEquals(firstNum.getString(), "FISCALDAY");
        assertEquals(parser.getTokenMap().get(11).size(), 1);
        LogicToken secondNum = parser.getTokenMap().get(11).get(0);
        assertEquals(secondNum.getType(),LogicTokenType.WORD);
        assertEquals(secondNum.getString(), "FESCALDAY");
    }

    public void testWhiteSpace()
    {
        LogicTextParser parser = new LogicTextParser();
        parser.parse(String.join(SAFRUtilities.LINEBREAK,
            "     \t",            
            "\t     " // line offset
        ));
        assertEquals(parser.getTokenMap().size(),2);
        assertEquals(parser.getTokenMap().get(0).size(), 1);
        LogicToken firstNum = parser.getTokenMap().get(0).get(0);
        assertEquals(firstNum.getType(),LogicTokenType.WHITE);
        assertEquals(firstNum.getString(), "     \t");
        assertEquals(parser.getTokenMap().get(8).size(), 1);
        LogicToken secondNum = parser.getTokenMap().get(8).get(0);
        assertEquals(secondNum.getType(),LogicTokenType.WHITE);
        assertEquals(secondNum.getString(), "\t     ");
    }
    
}
