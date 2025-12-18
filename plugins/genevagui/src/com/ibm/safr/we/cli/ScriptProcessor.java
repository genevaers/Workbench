package com.ibm.safr.we.cli;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ScriptProcessor {
    private static Logger logger = Logger.getLogger("com.ibm.safr.we.cl");;  
    private static SAFRComponent currentComponent = null;
    private static boolean useAdded = false;

    //simple parser for the moment... more a POC.
    public static void readFile(File fname) {
        CliProcessor clip = new CliProcessor();
        try {
            clip.processScript(fname);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            SAFRLogger.logAll(logger, Level.INFO, "Error reading " + fname + ": " + e.getMessage());
        }
//        try (FileReader fr = new FileReader(fname)) {
//            BufferedReader br = new BufferedReader(fr); 
//            String line;
//            while ((line = br.readLine()) != null) {
//                line = line.trim();
//                String[] words = line.split("\\s+");
//                switch(words[0].toLowerCase()) {
//                case "create":
//                    processCreate(words);
//                    break;
//                case "set":
//                    processSet(words);
//                    break;
//                case "add":
//                    processAdd(words);
//                    break;
//                case "save":
//                    processSave();
//                    break;
//                }
//
//                for (String word : words) {
//                    System.out.println(word);
//                }
//            }
//        } catch (IOException e) {
//            SAFRLogger.logAll(logger, Level.INFO, "Error reading " + fname + ": " + e.getMessage());
//        }
    }

    private static void processSave() {
        if(currentComponent != null) {
            currentComponent.validate();
            currentComponent.store();
        } else {
            SAFRLogger.logAll(logger, Level.SEVERE, "No current component defined");                        
        }
    }

    private static SAFRComponent processAdd(String[] words) {
        if(words.length > 1) {
            //get the appropriate SetHandler for the current component
            //Use a map?
                switch(currentComponent.getComponentType()) {
                case ControlRecord:
                    CRHandler.set(currentComponent, words);
                    break;
                case Environment:
                    break;
                case FormatUserExitRoutine:
                    break;
                case LogicalFile:
                    LFHandler.add(currentComponent, words);
                    break;
                case LogicalRecord:
                    return LRHandler.add(currentComponent, words);
                case LogicalRecordField:
                    break;
                case LookupPath:
                    break;
                case LookupUserExitRoutine:
                    break;
                case PhysicalFile:
                    break;
                case ReadUserExitRoutine:
                    break;
                case UserExitRoutine:
                    break;
                case View:
                    ViewHandler.add(currentComponent, words);
                    return currentComponent;
                case ViewFolder:
                    break;
                case WriteUserExitRoutine:
                    break;
                default:
                    break;
                
            }
        } else {
            SAFRLogger.logAll(logger, Level.INFO, "Create with no component type");
            
        }
        return null;
    }

    private static void processSet(String[] words) {
        if(words.length > 1) {
                //get the appropriate SetHandler for the current component
                //Use a map?
                switch(currentComponent.getComponentType()) {
                case ControlRecord:
                    CRHandler.set(currentComponent, words);
                    break;
                case Environment:
                    break;
                case FormatUserExitRoutine:
                    break;
                case LogicalFile:
                    break;
                case LogicalRecord:
                    break;
                case LogicalRecordField:
                    break;
                case LookupPath:
                    break;
                case LookupUserExitRoutine:
                    break;
                case PhysicalFile:
                    PFHandler.set(currentComponent, words);
                    PFHandler.setPfInput(((PhysicalFile)currentComponent).new InputDataset());
                    PFHandler.setPfOutput(((PhysicalFile)currentComponent).new OutputDataset());
                    break;
                case ReadUserExitRoutine:
                    break;
                case UserExitRoutine:
                    break;
                case View:
                    ViewHandler.set(currentComponent, words);
                    break;
                case ViewFolder:
                    break;
                case WriteUserExitRoutine:
                    break;
                default:
                    break;
                
            }
        } else {
            SAFRLogger.logAll(logger, Level.INFO, "Create with no component type");
            
        }
    }

    private static void processCreate(String[] words) {
        if(words.length > 1) {
            currentComponent = CreateHandler.create(words);
        } else {
            SAFRLogger.logAll(logger, Level.INFO, "Create with no component type");
            
        }
    }
}
