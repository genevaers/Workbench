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


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.LogicTextType;

public class LogicTextParser {
    
    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.logic.LogicTextParser");
    
    static final int EOF_KEY = -1;
    static final int CR_KEY = '\r';
    static final int LF_KEY = '\n';
    
    static public LogicTextParser generateParser(LogicTextType logicType) {
       switch (logicType) {
        case Extract_Record_Output :
        case Extract_Column_Assignment :
            return new LogicTextParserExtractCalc();
        case Extract_Record_Filter :
            return new LogicTextParserExtractFilter();
        case Format_Column_Calculation :
            return new LogicTextParserFormatCalc();
        case Format_Record_Filter :
            return new LogicTextParserFormatFilter();
        case Invalid :
            break;
        default :
            break;            
       }
       return null;
    }
    
    protected static String[] logicalOperatorKeywords = { "OR", "AND", "NOT" };
    
    protected LogicTextType logicType;
    
    protected Set<String> keywords = new HashSet<String>();

    protected String inStr;
    
    protected int curLineOff;
    protected int curLinePos;    
    
    protected Map<Integer,List<LogicToken>> tokenMap = new TreeMap<Integer,List<LogicToken>>();
    
    protected Set<LogicToken> curlBracketGroup = null;

    protected Deque<LogicToken> normBrackets = new ArrayDeque<LogicToken>();

    protected Deque<LogicToken> ifBlocks = new ArrayDeque<LogicToken>();
    
    public LogicTextParser() {
        keywords.addAll(Arrays.asList(logicalOperatorKeywords));        
    }
    
    public void parse(String newStr) {
        if (newStr.equals(inStr)) {
            return;
        }
        inStr = newStr;
        curLineOff = 0;
        curLinePos = 0;
        tokenMap.clear();
        normBrackets.clear();
        curlBracketGroup = null;
        List<LogicToken> lineTokens = new ArrayList<LogicToken>();
        
        LogicToken curToken = nextToken();
        while (!(curToken.getType() == LogicTokenType.EOF)) {
            if (curToken.getType() == LogicTokenType.EOL) {
                tokenMap.put(curLineOff, lineTokens);
                curLineOff += curLinePos;
                curLinePos = 0;
                lineTokens = new ArrayList<LogicToken>();
                curToken = nextToken();
            } else if (curToken.getType() == LogicTokenType.LCBRACKET) {
                lineTokens.add(curToken);                
                curToken = processInsideCurlBracket();
            } else if (curToken.getType() == LogicTokenType.INCBRACKET) {
                lineTokens.add(curToken);                
                curToken = processEndCurlBracket();
            } else {
                lineTokens.add(curToken);                
                curToken = nextToken();
            }
        }
        
        if (lineTokens.size() > 0) {
            tokenMap.put(curLineOff, lineTokens);            
        }
    }

    public Map<Integer, List<LogicToken>> getTokenMap() {
        return tokenMap;
    }
        
    public boolean isKeyword(String word) {
        word = word.toUpperCase();
        word = word.trim();
        if (keywords.contains(word)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isLogicalOperator(String word) {
        word = word.toUpperCase();
        word = word.trim();
        for (int i = 0; i < logicalOperatorKeywords.length; i++) {
            String temp = logicalOperatorKeywords[i];
            if (temp.compareToIgnoreCase(word) == 0) {
                return true;
            }
        }
        return false;
    }
    
    public String dumpTokenMap() {
        StringBuffer buffer = new StringBuffer();
        for (Entry<Integer,List<LogicToken>> entry : tokenMap.entrySet()) {
            buffer.append(SAFRUtilities.LINEBREAK + "Line Offset - " + entry.getKey() + 
                SAFRUtilities.LINEBREAK);
            
            // loop tokens 
            for (LogicToken token : entry.getValue()) {
                buffer.append("\tType " + token.getType().toString() + 
                    ", Start " + token.getStart() +
                    ", End " + token.getEnd() +
                    ", String " + token.getString() +
                    SAFRUtilities.LINEBREAK);
            }
        }
        return buffer.toString();
    }
    
    protected LogicToken processInsideCurlBracket() {
        LogicToken curToken;
        int tokStartPos = curLinePos;
        StringBuffer tokBuffer = new StringBuffer(); 
        // process inside of bracket
        int c = read();
        while (c != CR_KEY && c != LF_KEY && c!= EOF_KEY && c != '}') {
            tokBuffer.append((char)c);
            c = read();
        }
        unread(c);
        curToken = new LogicToken(LogicTokenType.INCBRACKET, tokStartPos, curLinePos, tokBuffer.toString(), curlBracketGroup);
        return curToken;
    }
    
    protected LogicToken processEndCurlBracket() {
        LogicToken curToken;
        int tokStartPos = curLinePos;
        StringBuffer tokBuffer = new StringBuffer(); 
        int c = read();
        if (c == '}') {
            tokBuffer.append((char)c);
            curToken = new LogicToken(LogicTokenType.RCBRACKET, tokStartPos, curLinePos, tokBuffer.toString(), curlBracketGroup);
            curlBracketGroup.add(curToken);            
        } else  {
            unread(c);
            curToken = nextToken();
        }        
        curlBracketGroup = null;
        return curToken;
    }
    
    /**
     * Returns the next lexical token in the document.
     */
    private LogicToken nextToken() {
        int c;
        int tokStartPos = curLinePos;
        StringBuffer tokBuffer = new StringBuffer(); 
        while (true) {
            switch (c = read()) {
            case '{':
                tokBuffer.append((char)c);
                curlBracketGroup = new HashSet<LogicToken>();
                LogicToken lcbracket = new LogicToken(LogicTokenType.LCBRACKET, tokStartPos, curLinePos, tokBuffer.toString(), curlBracketGroup);
                curlBracketGroup.add(lcbracket);
                return lcbracket;
            case '(':
                tokBuffer.append((char)c);
                Set<LogicToken> normBracketGroup = new HashSet<LogicToken>();
                LogicToken lbracket = new LogicToken(LogicTokenType.LBRACKET, tokStartPos, curLinePos, tokBuffer.toString(), normBracketGroup);
                normBracketGroup.add(lbracket);
                normBrackets.push(lbracket);
                return lbracket;
            case ')':
                tokBuffer.append((char)c);
                LogicToken rbracket;
                if (normBrackets.size() > 0) {
                    LogicToken clbracket = normBrackets.pop();
                    rbracket = new LogicToken(LogicTokenType.RBRACKET, tokStartPos, curLinePos, tokBuffer.toString(), clbracket.getGroup());
                    clbracket.getGroup().add(rbracket);
                } else {
                    rbracket = new LogicToken(LogicTokenType.RBRACKET, tokStartPos, curLinePos, tokBuffer.toString());
                }
                return rbracket;
            case EOF_KEY:
                tokBuffer.append((char)c);
                return new LogicToken(LogicTokenType.EOF,tokStartPos, curLinePos, tokBuffer.toString());
            case CR_KEY :
                tokBuffer.append((char)c);
                c = read();
                if (c == LF_KEY) {
                    tokBuffer.append((char)c);                    
                } else {
                    unread(c);
                }                 
                return new LogicToken(LogicTokenType.EOL,tokStartPos, curLinePos, tokBuffer.toString());
            case LF_KEY :
                tokBuffer.append((char)c);
                c = read();
                if (c == CR_KEY) {
                    tokBuffer.append((char)c);                    
                } else {
                    unread(c);
                }                 
                return new LogicToken(LogicTokenType.EOL,tokStartPos, curLinePos, tokBuffer.toString());
            case '\'': 
                tokBuffer.append((char)c);
                c = read();
                while (true) {
                    if ( (c == EOF_KEY) || (c == CR_KEY) || (c == LF_KEY) ) {
                        unread(c);
                        return new LogicToken(LogicTokenType.COMMENT, tokStartPos, curLinePos, tokBuffer.toString());
                    }
                    tokBuffer.append((char)c);
                    c = read();
                }
            case '\"': // string
                tokBuffer.append((char)c);
                c = read();
                while (true) {
                    if ( (c == EOF_KEY) || (c == CR_KEY) || (c == LF_KEY) || (c == '\"')) {
                        if (c == '\"') {
                            tokBuffer.append((char)c);                            
                        } else {
                            unread(c);
                        }
                        return new LogicToken(LogicTokenType.STRING, tokStartPos, curLinePos, tokBuffer.toString());
                    }
                    tokBuffer.append((char)c);
                    c = read();
                }
            // number
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                tokBuffer.append((char)c);
                c = read();
                while (Character.isDigit((char)c)) {
                    tokBuffer.append((char)c);
                    c = read();
                }
                unread(c);
                return new LogicToken(LogicTokenType.NUMBER, tokStartPos, curLinePos, tokBuffer.toString());                

            default:
                if (Character.isWhitespace((char) c)) {
                    do {
                        tokBuffer.append((char)c);
                        c = read();
                    } while (Character.isWhitespace((char) c) && c != CR_KEY && c != LF_KEY);
                    unread(c);
                    return new LogicToken(LogicTokenType.WHITE, tokStartPos, curLinePos, tokBuffer.toString());
                }
                else if (isIdentifierStart((char) c)) {
                    do {
                        tokBuffer.append((char) c);
                        c = read();
                    } while (isIdentifierPart((char) c));
                    unread(c);
                    if (keywords.contains(tokBuffer.toString())) {
                        return processKeyword(tokStartPos, tokBuffer);
                    } else {
                        return new LogicToken(LogicTokenType.WORD, tokStartPos, curLinePos, tokBuffer.toString());                        
                    }
                    
                } else {
                    tokBuffer.append((char) c);                    
                }                
                return new LogicToken(LogicTokenType.OTHER,tokStartPos, curLinePos, tokBuffer.toString());
            }
        }
    }

    protected LogicToken processKeyword(int tokStartPos, StringBuffer tokBuffer) {
        if (tokBuffer.toString().equals("IF")) {
            Set<LogicToken> ifGroup = new HashSet<LogicToken>();
            LogicToken token =  new LogicToken(LogicTokenType.IF, tokStartPos, curLinePos, tokBuffer.toString(),ifGroup);
            ifGroup.add(token);
            ifBlocks.push(token);
            return token;
        } else if (tokBuffer.toString().equals("THEN")) {
            if (ifBlocks.size() > 0) {
                LogicToken ifTok = ifBlocks.peek();
                LogicToken token =  new LogicToken(LogicTokenType.THEN, tokStartPos, curLinePos, tokBuffer.toString(),ifTok.getGroup());
                ifTok.getGroup().add(token);
                return token;
            } else {
                return new LogicToken(LogicTokenType.THEN, tokStartPos, curLinePos, tokBuffer.toString());                                
            }
        } else if (tokBuffer.toString().equals("ELSE")) {
            if (ifBlocks.size() > 0) {
                LogicToken ifTok = ifBlocks.peek();
                LogicToken token =  new LogicToken(LogicTokenType.ELSE, tokStartPos, curLinePos, tokBuffer.toString(),ifTok.getGroup());
                ifTok.getGroup().add(token);
                return token;
            } else {
                return new LogicToken(LogicTokenType.ELSE, tokStartPos, curLinePos, tokBuffer.toString());                                
            }
        } else if (tokBuffer.toString().equals("ENDIF")) {
            if (ifBlocks.size() > 0) {
                LogicToken ifTok = ifBlocks.pop();
                LogicToken token =  new LogicToken(LogicTokenType.ENDIF, tokStartPos, curLinePos, tokBuffer.toString(),ifTok.getGroup());
                ifTok.getGroup().add(token);
                return token;
            } else {
                return new LogicToken(LogicTokenType.ENDIF, tokStartPos, curLinePos, tokBuffer.toString());                                
            }
        } else {
            return new LogicToken(LogicTokenType.KEY, tokStartPos, curLinePos, tokBuffer.toString());
        }
    }

    private boolean isIdentifierStart(Character c) {

        // check if the word start is a no , '_' or letter
        if (c == '_' || Character.isDigit(c) || Character.isLetter(c)) {
            return true;
        }
        return false;
    }

    private boolean isIdentifierPart(Character c) {
        if (Character.isJavaIdentifierPart(c) && (c != '$')) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns next character.
     */
    private int read() {
        if ((curLineOff + curLinePos) < inStr.length()) {
            return inStr.charAt(curLineOff + curLinePos++);
        }
        return EOF_KEY;
    }
    
    private void unread(int c) {
        if (c != EOF_KEY)
            curLinePos--;
    }

    public LogicToken findToken(int start, int end) {
        
        // find line
        Entry<Integer,List<LogicToken>> lastEntry = null;
        for (Entry<Integer,List<LogicToken>> entry : tokenMap.entrySet()) {
            if (entry.getKey() > end) {
                break;
            }
            lastEntry = entry;
        }
        if (lastEntry == null) {
            return null;            
        }
        else {
            // find token on line
            for (LogicToken token : lastEntry.getValue()) {
                if ((lastEntry.getKey() + token.getStart()) > start ||
                    (lastEntry.getKey() + token.getEnd() > end)) {
                    return null;
                }
                if ((lastEntry.getKey() + token.getStart()) == start &&
                    (lastEntry.getKey() + token.getEnd() == end)) {
                    return token;
                }
            }
            return null;            
        }
    }

    
}
