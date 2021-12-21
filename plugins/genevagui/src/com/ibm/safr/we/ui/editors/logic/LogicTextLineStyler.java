package com.ibm.safr.we.ui.editors.logic;

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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.ibm.safr.we.model.logic.LogicTextParser;
import com.ibm.safr.we.model.logic.LogicToken;
import com.ibm.safr.we.model.logic.LogicTokenType;

/**
 * A class for scanning keywords and implementing various listeners for styled
 * text.
 * 
 */
public class LogicTextLineStyler implements LineStyleListener, ExtendedModifyListener {
	
	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.ui.editors.LineStyler");
	
	// Stop this handler processing modify events caused by this handler
	private boolean inHandler = false;
	
	private LogicTextParser parser;
	
	private Map<LogicTokenType,Color> colorMap = new HashMap<LogicTokenType,Color>();
	
	private Set<LogicToken> selectionGroup = null;
	
	public LogicTextLineStyler(LogicTextParser parser) {
	    this.parser = parser;
		initializeColors();
	}

	private Color getColor(LogicTokenType type) {
	    if (colorMap.containsKey(type)) {
	        return colorMap.get(type);
	    } else {
	        return colorMap.get(LogicTokenType.OTHER);
	    }
	}

	private void initializeColors() {
		Display display = Display.getDefault();
		display.getSystemColor(SWT.COLOR_BLACK);
		colorMap.put(LogicTokenType.WORD, display.getSystemColor(SWT.COLOR_BLACK));
        colorMap.put(LogicTokenType.WHITE, display.getSystemColor(SWT.COLOR_BLACK));
        colorMap.put(LogicTokenType.OTHER, display.getSystemColor(SWT.COLOR_BLACK));
        colorMap.put(LogicTokenType.NUMBER, display.getSystemColor(SWT.COLOR_BLACK));
        colorMap.put(LogicTokenType.KEY, display.getSystemColor(SWT.COLOR_BLUE));
        colorMap.put(LogicTokenType.IF, display.getSystemColor(SWT.COLOR_BLUE));
        colorMap.put(LogicTokenType.THEN, display.getSystemColor(SWT.COLOR_BLUE));
        colorMap.put(LogicTokenType.ELSE, display.getSystemColor(SWT.COLOR_BLUE));
        colorMap.put(LogicTokenType.ENDIF, display.getSystemColor(SWT.COLOR_BLUE));
        colorMap.put(LogicTokenType.COMMENT, display.getSystemColor(SWT.COLOR_DARK_GREEN));
        colorMap.put(LogicTokenType.STRING, display.getSystemColor(SWT.COLOR_MAGENTA));
        colorMap.put(LogicTokenType.INCBRACKET, display.getSystemColor(SWT.COLOR_BLACK));		
	}

	/**
	 * Event.detail line start offset (input) Event.text line text (input)
	 * LineStyleEvent.styles Enumeration of StyleRanges, need to be in order.
	 * (output) LineStyleEvent.background line background color (output)
	 */
	public void lineGetStyle(LineStyleEvent event) {
		Vector<StyleRange> styles = new Vector<StyleRange>();

        parser.parse(((StyledText)event.widget).getText());
		
		if (parser.getTokenMap().isEmpty()) {
		    return;
		}
		
		// find parser tokens
		List<LogicToken> tokens = parser.getTokenMap().get(event.lineOffset);
		if (tokens == null) {
		    return;
		}
		
		// generate a style for each token
        Color defaultFgColor = ((Control) event.widget).getForeground();
        
		for (LogicToken token : tokens) {
		    Color background = null;
		    if (selectionGroup != null && selectionGroup.contains(token)) {
		        Display display = Display.getDefault();
		        background = display.getSystemColor(SWT.COLOR_GRAY);
		    }
            Color color = getColor(token.getType());
            if ((!color.equals(defaultFgColor)) || background!=null) {                
                StyleRange style = new StyleRange(
                    event.lineOffset + token.getStart(), 
                    token.getString().length(), color, background);
                styles.addElement(style);
            }
		    
		}
		
		event.styles = new StyleRange[styles.size()];
		styles.copyInto(event.styles);

	}

	public void modifyText(ExtendedModifyEvent e) {
		if (inHandler) {
			return;
		}
		else {
			inHandler = true;
		}
		StyledText sText = (StyledText) e.widget;
		// get index of the first line where test is replaced.
		int startIndex = sText.getLineAtOffset(e.start);

		// get index of the last line where test is replaced.
		int endIndex = sText.getLineAtOffset((e.start + e.length));
		// Loop from first line to last line of the replaced text.
		for (int k = startIndex; k <= endIndex; k++) {
			// CQ 5707 : Jaydeep : Feb 02,2010. :Changed the string splitting
			// logic.
			// get line text.
			String lineText = sText.getLine(k);
			int lineStart = sText.getOffsetAtLine(k);
			int fromIndex = 0;
			// split the string on words which are not in comment and Not in
			// quotes.
			String[] keyArray = lineText.split("[^a-zA-Z0-9_'\"]");
			int start = lineStart;
			int commentCntr = 0, quoteCntr = 0;
			// from index initially equal to index of 1st character of line and
			// then each time it will be updated to
			// point to
			// end of the each word in the array.
			for (int i = 0; i < keyArray.length; i++) {
				String word = keyArray[i];
				start = lineStart + lineText.indexOf(word, fromIndex);
				fromIndex = fromIndex + word.length();
				quoteCntr += countCharacters(word, '"');
				commentCntr += countCharacters(word, '\'');

				// word is logical operator and and is not in proper case.
				if ((commentCntr == 0) && (quoteCntr % 2 == 0)
						&& parser.isLogicalOperator(word)
						&& (!word.matches("(And|Or|Not)"))) {
					// convert 1st character of the logical operator to
					// upper case.
					word = word.toLowerCase();
					char[] charArray = word.toCharArray();
					charArray[0] = Character.toUpperCase(charArray[0]);
					word = new String(charArray);
					sText.replaceTextRange(start, word.length(), word);
					continue;
				}

				// check if word is in lower case.
				if ((commentCntr == 0) && (quoteCntr % 2 == 0) && (word.compareTo(word.toUpperCase()) != 0)) {
					// if it is keyword.
					if (parser.isKeyword(word)) {
						word = word.toUpperCase();
						replaceTextRange(sText, start, word.length(), word);
					}
				}
			}
		}
		
		inHandler = false;
	}
	
	/**
	 * Allow sub class to override replaceTextRange behaviour 
	 */
	protected void replaceTextRange(StyledText sText, int start, int length, String word) {
		sText.replaceTextRange(start, word.length(), word);		
	}
	
	private int countCharacters(String word, char ch) {
		int count = 0;
		for (char cha : word.toCharArray()) {
			if (cha == ch) {
				count++;
			}
		}
		return count;
	}

    public void setSelection(int start, int end) {
        LogicToken selected = parser.findToken(start, end);
        if (selected == null) {
            selectionGroup = null;
        } else {
            selectionGroup = selected.getGroup();
        }
        
    }
}
