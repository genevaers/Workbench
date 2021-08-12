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


import java.util.List;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.OutputFormat;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.diff.DiffBaseNode.DiffNodeState;
import com.ibm.safr.we.model.view.HeaderFooterItem;
import com.ibm.safr.we.model.view.View;

public class NodeViewReportGenerator {

    private DiffNodeComp parent;
    private View view;
    private DiffNodeState state;
    
    public NodeViewReportGenerator(DiffNodeComp parent, View view, DiffNodeState state) {
        super();
        this.parent = parent;
        this.view = view;
        this.state = state;
    }

    protected DiffNode generateTree() throws SAFRException {
        if (view.getOutputFormat() == OutputFormat.Format_Report) {
            return generateReportSection();
        } else {
            return null;
        }
    }

    protected DiffNode generateReportSection() throws SAFRException {
        DiffNodeSection report = new DiffNodeSection();
        report.setName("Report"); 
        report.setParent(parent);

        DiffNodeSection details = generateReportDetails();
        report.addChild(details);
        details.setParent(report);            
                    
        DiffNode headers = generateReportHeaders(report);
        report.addChild(headers);
        headers.setParent(report);
        
        report.setState(state);
        return report;                    
    }

    protected DiffNodeSection generateReportDetails() {
        DiffNodeSection details = new DiffNodeSection();
        details.setName("Details");
        details.setState(state);
        
        details.addIntField("Lines Per Page", view.getLinesPerPage(), view.getEnvironmentId(), state);
        details.addIntField("Report Width", view.getReportWidth(), view.getEnvironmentId(), state);
        
        return details;
    }

    protected DiffNode generateReportHeaders(DiffNodeSection report) {
        DiffNodeSection headers = new DiffNodeSection();
        headers.setName("Header/Footer"); 
        headers.setState(state);

        // diff header 
        String headStr[] = loadHeaderFooter(view.getHeader());

        headers.addLargeStringField("Header Left", headStr[0], view.getEnvironmentId(), state);
        headers.addLargeStringField("Header Center", headStr[1], view.getEnvironmentId(), state);
        headers.addLargeStringField("Header Right", headStr[2], view.getEnvironmentId(), state);

        // diff footer 
        String footStr[] = loadHeaderFooter(view.getFooter());

        headers.addLargeStringField("Footer Left", footStr[0], view.getEnvironmentId(), state);
        headers.addLargeStringField("Footer Center", footStr[1], view.getEnvironmentId(), state);
        headers.addLargeStringField("Footer Right", footStr[2], view.getEnvironmentId(), state);
        
        return headers;
    }
    
    /**
     * This function is used to retrieve the Header/Footer items from the model
     * and convert it into a string array representing the Header/Footer text
     * for the Left, Center and Right selections.
     * 
     * @param headerFooter
     *            the HeaderFooter object
     * @return string array representing the combined Header/Footer text
     */
    private String[] loadHeaderFooter(View.HeaderFooterItems headerFooter) {
        List<HeaderFooterItem> hfItems = headerFooter.getItems();
        Code functionCode;
        Code justifyCode;
        int row;
        String itemText;

        int leftCounter = 1;
        int centerCounter = 1;
        int rightCounter = 1;
        String functionString = "";
        StringBuffer leftString = new StringBuffer();
        StringBuffer centerString = new StringBuffer();
        StringBuffer rightString = new StringBuffer();

        // combinedHFText will contain 3 items - one for the text of each
        // selection -Left, Center and Right
        String[] combinedHFText = new String[3];

        for (HeaderFooterItem item : hfItems) {
            functionCode = item.getFunctionCode();
            justifyCode = item.getJustifyCode();
            row = item.getRow();
            itemText = item.getItemText();

            if (functionCode.getGeneralId() == Codes.HF_TEXT) { // user text
                if (itemText != null) {
                    switch (justifyCode.getGeneralId()) {
                    case Codes.LEFT:
                        if (leftCounter != row) {
                            leftString.append(SAFRUtilities.LINEBREAK);
                        }
                        leftString.append(itemText);
                        leftCounter = row;
                        break;
                    case Codes.CENTER:
                        if (centerCounter != row) {
                            centerString.append(SAFRUtilities.LINEBREAK);
                        }
                        centerString.append(itemText);
                        centerCounter = row;
                        break;
                    case Codes.RIGHT:
                        if (rightCounter != row) {
                            rightString.append(SAFRUtilities.LINEBREAK);
                        }
                        rightString.append(itemText);
                        rightCounter = row;
                        break;
                    }
                }
            } else { // function code
                functionString = "&[" + functionCode.getDescription() + "]";
                switch (justifyCode.getGeneralId()) {
                case Codes.LEFT:
                    if (leftCounter != row) {
                        leftString.append(SAFRUtilities.LINEBREAK);
                    }
                    leftString.append(functionString);
                    leftCounter = row;
                    break;
                case Codes.CENTER:
                    if (centerCounter != row) {
                        centerString.append(SAFRUtilities.LINEBREAK);
                    }
                    centerString.append(functionString);
                    centerCounter = row;
                    break;
                case Codes.RIGHT:
                    if (rightCounter != row) {
                        rightString.append(SAFRUtilities.LINEBREAK);
                    }
                    rightString.append(functionString);
                    rightCounter = row;
                    break;
                }
            }
        }

        combinedHFText[0] = leftString.toString();
        combinedHFText[1] = centerString.toString();
        combinedHFText[2] = rightString.toString();
        return combinedHFText;
    }
    
    
}
