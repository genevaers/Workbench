package com.ibm.safr.we.utilities;

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


import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

    private static final char COMMA = ',';
    private static final String QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";
    private static final String PATTERN = ".*[\",\\s].*";
    
    private boolean firstLine = true;
    private FileWriter writer;
    
    public CSVWriter(FileWriter writer) {
        super();
        this.writer = writer;
    }

    public static String escape( String s )
    {
        if ( s.contains( QUOTE ) )
            s = s.replace( QUOTE, ESCAPED_QUOTE );
        
        if ( s.matches(PATTERN) )
            s = QUOTE + s + QUOTE;

        return s;
    }    

    public static String escapeFirstID( String s )
    {
        if ( s.contains( QUOTE ) )
            s = s.replace( QUOTE, ESCAPED_QUOTE );
        
        s = QUOTE + s + QUOTE;

        return s;
    }    
    
    private void writeFirstLine(List<String> values) throws IOException {

        boolean first = true;
        char separator = COMMA;

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (first) {                
                if (value.startsWith("ID")) {
                    sb.append(escapeFirstID(value));
                }
                else {
                    sb.append(escape(value));                    
                }
                first = false;
            }
            else {                
                sb.append(separator);                
                sb.append(escape(value));
            }
        }
        sb.append(System.lineSeparator());
        writer.append(sb.toString());
    }
    
    public void writeLine(List<String> values) throws IOException {

        if (firstLine) {
            writeFirstLine(values);
            firstLine = false;
            return;
        }
        else {
            boolean first = true;
            char separator = COMMA;

            StringBuilder sb = new StringBuilder();
            for (String value : values) {
                if (!first) {
                    sb.append(separator);
                }
                sb.append(escape(value));
                first = false;
            }
            sb.append(System.lineSeparator());
            writer.append(sb.toString());            
        }
    }

    public void close() throws IOException {
        writer.close();        
    }

}
