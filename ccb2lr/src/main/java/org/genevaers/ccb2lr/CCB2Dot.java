package org.genevaers.ccb2lr;

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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Generate a dot file of the AST so we can see a pretty picture. <BR>
 * To dot the picture more readable we can filter on views and columns.
 * Filtering defaults to not enabled.
 */
public class CCB2Dot {
    /**
     *
     */
    // Node Type Colours
    private static final String ZONED = "lightgrey";
    private static final String GROUP = "deepskyblue";
    private static final String OCCURS = "lightgreen";
    private static final String BINARY = "skyblue";
    private static final String PACKED = "gold";
    private static final String ALPHA = "pink";
    // Node Function Shapes
    private static final String EMITABLE = "octagon";
    private static final String FRAMEWORK = "rect";
    private static FileWriter fw;
    private static String[] views;
    private static String[] cols;
    private static boolean nodeEnabled = true;
    private static boolean filter = false;
    private static String lf_id;
    static int nodeNum = 1;
    private static String idString;
    private static String label;
    private static String colour;
    private static String shape;

    private static boolean reverseArrow = false; // Default arrow direction
    private static boolean dataflow = false; // Default arrow direction

    CCB2Dot() {

    }

    public static void write(CobolCollection cobolCollection, Path dest) {
        try {
            fw = new FileWriter(dest.toFile());
            writeFields(cobolCollection.getFields());
            fw.write("}\n");
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeFromRecord(CobolCollection cobolCollection, Path dest) {
        try {
            fw = new FileWriter(dest.toFile());
            writeRecord(cobolCollection.getRecordGroup());
            fw.write("}\n");
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void writeFields(List<CobolField> fields) throws IOException {
        writeHeader();
        writeNodes(fields);
    }

    private static void writeRecord(CobolField record) throws IOException {
        writeHeader();
        writeRecordNodes(record);
    }

    private static void writeRecordNodes(CobolField record) throws IOException {
        dotField(record);
        CobolField n = record.next();
        String rec = "subgraph cluster"+n.getSection() + " { label=\"Section "+ n.getSection() + "\" node [shape=plaintext] rank=same\n";
        fw.write(rec);
        int section = 0;
        while(n != null) {
            if(section != n.getSection()) {
                section = n.getSection();
                if(section > 1) {
                    fw.write("}\n");
                }
                String subg = "subgraph cluster"+n.getSection() + " { label=\"Section "+ n.getSection() + "\" node [shape=plaintext] rank=same\n";
                fw.write(subg);
            }            
            dotField(n);
            n = n.next();
        }
        fw.write("}\n");
        writeEdges(record);
        n = record.next();
        while(n != null) {
            writeEdges(n);
            n = n.next();
        }
    }

    private static void writeNodes(List<CobolField> fields) throws IOException {
        Iterator<CobolField> fi = fields.iterator();
        int section = 0;
        while(fi.hasNext()) {
            CobolField f = fi.next();
            if(section != f.getSection()) {
                section = f.getSection();
                if(section > 1) {
                    fw.write("}\n");
                }
                String subg = "subgraph cluster"+f.getSection() + " { label=\"Section "+ f.getSection() + "\" node [shape=plaintext] rank=same\n";
                fw.write(subg);
            }            
            dotField(f);
        }
        fw.write("}\n");
        Iterator<CobolField> fi2 = fields.iterator();
        while(fi2.hasNext()) {
            CobolField f = fi2.next();
            writeEdges(f);
        }
    }


    private static void writeEdges(CobolField f) throws IOException {
        if(f.getParent() != null) {
            fw.write(f.getName().replace('-','_') + " -> " + f.getParent().getName().replace('-','_') +"\n");
        }
        if(f.getFirstChild() != null) {
            fw.write(f.getName().replace('-','_') + " -> " + f.getFirstChild().getName().replace('-','_') +"\n");
        }
        if(f.getNextSibling() != null) {
            fw.write(f.getName().replace('-','_') + " -> " + f.getNextSibling().getName().replace('-','_') +"\n");
        }
        if(f.getPreviousSibling() != null) {
            fw.write(f.getName().replace('-','_') + " -> " + f.getPreviousSibling().getName().replace('-','_') +"\n");
        }
    }

    private static void dotField(CobolField f) throws IOException {
        idString = "";
        label = "";
        colour = "red";
        shape = "oval";
        if (f != null) {
            switch (f.getType()) {
                case ALPHA:
                dotAlpha(f);
                break;
            case ZONED:
                dotZoned(f);
                break;
            case PACKED:
            dotPacked(f);
                break;
            case BINARY:
            dotBinary(f);
                break;
            case OCCURSGROUP:
            dotOccurs(f);
                break;
            case GROUP:
            dotGroup(f);
                break;
            default:
            wtf(f);
                break;
                }
        }
        fw.write(idString + "[label=\"" + label + "\" " + "color=" + colour + " shape=" + shape
        + " style=filled]\n");
    }

    private static void dotPacked(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        colour = PACKED;
        shape = FRAMEWORK;
    }

    private static void wtf(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        shape = FRAMEWORK;
    }

    private static void dotGroup(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        colour = GROUP;
        shape = FRAMEWORK;
    }

    private static void dotOccurs(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        colour = OCCURS;
        shape = FRAMEWORK;
    }

    private static void dotBinary(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        colour = BINARY;
        shape = FRAMEWORK;
    }

    private static void dotZoned(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        colour = ZONED;
        shape = FRAMEWORK;
    }

    private static void dotAlpha(CobolField f) {
        idString = f.getName().replace('-','_');
        label = f.getName();
        colour = ALPHA;
        shape = FRAMEWORK;
    }


    private static void writeHeader() throws IOException {
        fw.write("digraph xml {\nrankdir=TB\nnewrank=true;\n// Cobol Tree Nodes\n");
        fw.write(
                "graph [label=\" Cobol Tree Nodes\", labelloc=t, labeljust=center, fontname=Helvetica, fontsize=22, concentrate=false];\n");
        fw.write("labeljust=center;\n");
    }

 
}
