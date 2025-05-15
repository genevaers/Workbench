package org.genevaers.ccb2lr;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Generate a dot file of the AST so we can see a pretty picture. <BR>
 * To dot the picture more readable we can filter on views and columns.
 * Filtering defaults to not enabled.
 */
public class CCB2Dot {
    static transient Logger logger = Logger.getLogger("org.genevaers.ccb2lr.CCB2Dot");
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
    private static final String FRAMEWORK = "rect";
    private static FileWriter fw;
    static int nodeNum = 1;
    private static String idString;
    private static String label;
    private static String colour;
    private static String shape;

    public static void write(CobolCollection cobolCollection, Path dest) {
        try {
            fw = new FileWriter(dest.toFile());
            writeFields(cobolCollection.getFields());
            fw.write("}\n");
            fw.close();
        } catch (IOException e) {
            logger.severe("Cannot write COBOL File\n" + e.getMessage());
        }
    }

    public static void writeFromRecord(CobolCollection cobolCollection, Path dest) {
        try {
            fw = new FileWriter(dest.toFile());
            writeRecord(cobolCollection.getRecordGroup());
            addRedeines(cobolCollection.getRedefines());
            fw.write("}\n");
            fw.close();
        } catch (IOException e) {
            logger.severe("Cannot write COBOL record\n" + e.getMessage());
        }
    }

    private static void addRedeines(Collection<CobolField> redefines) throws IOException {
        Iterator<CobolField> ri = redefines.iterator();
        while(ri.hasNext()) {
            CobolField r = ri.next();
            dotField(r);
            writeEdges(r);
        }
    }

    private static void writeFields(Collection<CobolField> collection) throws IOException {
        writeHeader();
        writeNodes(collection);
    }

    private static void writeRecord(CobolField cbf) throws IOException {
        writeHeader();
        writeRecordNodes(cbf);
    }

    private static void writeRecordNodes(CobolField cbf) throws IOException {
        dotField(cbf);
        CobolField n = cbf.next();
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
        writeEdges(cbf);
        n = cbf.next();
        while(n != null) {
            writeEdges(n);
            n = n.next();
        }
    }

    private static void writeNodes(Collection<CobolField> collection) throws IOException {
        Iterator<CobolField> fi = collection.iterator();
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
        Iterator<CobolField> fi2 = collection.iterator();
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
            checkRedefine(f);
        }
        fw.write(idString + "[label=\"" + label + "\" " + "color=" + colour + " shape=" + shape
        + " style=filled]\n");
    }

    private static void checkRedefine(CobolField f) {
        if(f.isRedefines()) {
            shape = "hexagon";
            label += "\n [" + f.redefinedName + "]";
        }
    }

    private static void dotPacked(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
        colour = PACKED;
        shape = FRAMEWORK;
    }

    private static void formatLabel(CobolField f) {
        label = f.getName() + " " + f.getPosition() + "(" + f.getLength() + ")";
    }

    private static void wtf(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
        shape = FRAMEWORK;
    }

    private static void dotGroup(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
        colour = GROUP;
        shape = FRAMEWORK;
    }

    private static void dotOccurs(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
        colour = OCCURS;
        shape = FRAMEWORK;
    }

    private static void dotBinary(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
        colour = BINARY;
        shape = FRAMEWORK;
    }

    private static void dotZoned(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
        colour = ZONED;
        shape = FRAMEWORK;
    }

    private static void dotAlpha(CobolField f) {
        idString = f.getName().replace('-','_');
        formatLabel(f);
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
