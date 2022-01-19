package com.ibm.safr.we.ui.utilities;

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


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.ui.dialogs.DependencyMessageDialog;
import com.ibm.safr.we.ui.editors.ControlRecordEditor;
import com.ibm.safr.we.ui.editors.ControlRecordEditorInput;
import com.ibm.safr.we.ui.editors.EnvironmentEditor;
import com.ibm.safr.we.ui.editors.EnvironmentEditorInput;
import com.ibm.safr.we.ui.editors.GroupEditor;
import com.ibm.safr.we.ui.editors.GroupEditorInput;
import com.ibm.safr.we.ui.editors.LogicalFileEditor;
import com.ibm.safr.we.ui.editors.LogicalFileEditorInput;
import com.ibm.safr.we.ui.editors.LookupPathEditor;
import com.ibm.safr.we.ui.editors.LookupPathEditorInput;
import com.ibm.safr.we.ui.editors.UserEditor;
import com.ibm.safr.we.ui.editors.UserEditorInput;
import com.ibm.safr.we.ui.editors.UserExitRoutineEditor;
import com.ibm.safr.we.ui.editors.UserExitRoutineEditorInput;
import com.ibm.safr.we.ui.editors.ViewFolderEditor;
import com.ibm.safr.we.ui.editors.ViewFolderEditorInput;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditorInput;
import com.ibm.safr.we.ui.editors.pf.PhysicalFileEditor;
import com.ibm.safr.we.ui.editors.pf.PhysicalFileEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewEditor;
import com.ibm.safr.we.ui.editors.view.ViewEditorInput;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy.SAFRGUIContext;

public class EditorOpener {

    public static IEditorPart open(Integer id, ComponentType type) {
        if (id == null || id == 0) {
            return null;
        }
        switch (type) {
        case Group:
            return openGrpEditor(id, type);                
        case Environment:
            return openEnvEditor(id, type);                
        case ControlRecord:
            return openCREditor(id, type);
        case UserExitRoutine:
        case ReadUserExitRoutine:
        case LookupUserExitRoutine:
        case WriteUserExitRoutine:
        case FormatUserExitRoutine:
            return openExitEditor(id, type);
        case PhysicalFile:
            return openPFEditor(id, type);
        case LogicalFile:
            return openLFEditor(id, type);
        case LogicalRecord:
            return openLREditor(id, type);
        case LogicalRecordField:
            return openLRFieldEditor(id);
        case LookupPath:
            return openLUEditor(id, type);
        case View:
            return openViewEditor(id, type);
        case ViewFolder:
            return openViewFolderEditor(id, type);
        default:        
        }
        return null;
    }

    public static IEditorPart openUser(String id) {
        try {
            if (!UIUtilities.isSystemAdmin() && 
                !SAFRApplication.getUserSession().getUser().getUserid().equals(id)) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    "Error opening entered user.",
                    "Current logged in user has no rights on the entered user ID.");
                return null;
            }
            
            User user = SAFRApplication.getSAFRFactory().getUser(id); 
            user.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            UserEditorInput input = new UserEditorInput(user);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, UserEditor.ID);
        } catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered user.",
                "Cannot find user with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening user editor.",null);
        }
        return null;
    }
        
    private static IEditorPart openGrpEditor(Integer id, ComponentType type) {
        try {
            if (!UIUtilities.isSystemAdmin()) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    "Error opening entered group.",
                    "Current logged in user has no rights on the entered group ID.");
                return null;
            }
            
            Group grp = SAFRApplication.getSAFRFactory().getGroup(id); 
            grp.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            GroupEditorInput input = new GroupEditorInput(grp);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, GroupEditor.ID);
        }  catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered group.",
                "Cannot find group with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening group editor.",null);
        }
        return null;
    }
    
    private static IEditorPart openEnvEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);  
            Environment env = SAFRApplication.getSAFRFactory().getEnvironment(id); 
            env.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            EnvironmentEditorInput input = new EnvironmentEditorInput(env, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, EnvironmentEditor.ID);
        }  catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered environment.",
                "Cannot find environment with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening env editor.",null);
        }
        return null;
    }
    
    private static IEditorPart openCREditor(Integer id, ComponentType type) {
        try {
            EditRights rights = null;
            if (UIUtilities.isSystemAdminOrEnvAdmin()) {
                rights = EditRights.ReadModifyDelete;                            
            }
            else {
                rights = EditRights.Read;
            }            
            ControlRecord cr = SAFRApplication.getSAFRFactory().getControlRecord(id); 
            cr.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            ControlRecordEditorInput input = new ControlRecordEditorInput(cr, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, ControlRecordEditor.ID);
        } catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered control record.",
                "Cannot find control record with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(e,
                    "Unexpected error occurred while opening CR editor.",
                    null);
        }
        return null;
    }
    
    private static IEditorPart openLREditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);               
            LogicalRecord lr = SAFRApplication.getSAFRFactory().getLogicalRecord(id); 
            lr.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            LogicalRecordEditorInput input = new LogicalRecordEditorInput(lr, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().openEditor(input, LogicalRecordEditor.ID);
        } catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered logical record.",
                "Cannot find logical record with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening logical record editor.",null);
        }
        return null;
    }
    
    private static IEditorPart openLFEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);                                            
            LogicalFile file = SAFRApplication.getSAFRFactory().getLogicalFile(id); 
            file.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            LogicalFileEditorInput input = new LogicalFileEditorInput(file, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, LogicalFileEditor.ID);
        }  catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered logical file.",
                "Cannot find logical file with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening logical file editor.",null);
        }
        return null;
    }
    
    private static IEditorPart openPFEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);                                            
            PhysicalFile file = SAFRApplication.getSAFRFactory().getPhysicalFile(id); 
            file.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));            
            PhysicalFileEditorInput input = new PhysicalFileEditorInput(file, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, PhysicalFileEditor.ID);
        }  catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered physical file.",
                "Cannot find physical file with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening physical file editor.",null);
        }
        return null;
    }

    private static IEditorPart openLUEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);                                            
            LookupPath lp = SAFRApplication.getSAFRFactory().getLookupPath(id); 
            lp.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));
            LookupPathEditorInput input = new LookupPathEditorInput(lp, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, LookupPathEditor.ID);
        }  catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered lookup.",
                "Cannot find lookup with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening lookup editor.",null);
        }
        return null;
    }

    private static IEditorPart openExitEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);                                            
            UserExitRoutine userExit = SAFRApplication.getSAFRFactory().getUserExitRoutine(id);
            userExit.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));
            UserExitRoutineEditorInput input = new UserExitRoutineEditorInput(userExit, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, UserExitRoutineEditor.ID);
        } catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered exit ID.",
                "Cannot find exit with ID " + id + ".");             
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening user exit editor.",null);
        }
        return null;
    }
    
    private static IEditorPart openViewEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);                                            
            View file = SAFRApplication.getSAFRFactory().getView(id);
            file.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));            
            ViewEditorInput input = new ViewEditorInput(file, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, ViewEditor.ID);
        } 
        catch (SAFRDependencyException de) {
            DependencyMessageDialog.openDependencyDialog(Display.getCurrent().getActiveShell(), "Inactive Dependencies", 
                "The View could not be loaded because the following component(s) are inactive." + 
                " Please reactivate these and try again.", de.getDependencyString(), MessageDialog.ERROR,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        } catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    "Error opening entered view ID.",
                    "Cannot find view with ID " + id + ".");             
        }
        catch (Exception e) {
            UIUtilities.handleWEExceptions(e,"Unexpected error occurred while opening view editor.",null);
        }
        return null;
    }

    private static IEditorPart openViewFolderEditor(Integer id, ComponentType type) {
        try {
            EditRights rights = UIUtilities.getEditRights(id, type);                                            
            ViewFolder folder = SAFRApplication.getSAFRFactory().getViewFolder(id); 
            folder.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                        
            ViewFolderEditorInput input = new ViewFolderEditorInput(folder, rights);
            return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor(input, ViewFolderEditor.ID);
        }  catch (SAFRNotFoundException nfe) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                "Error opening entered view folder.",
                "Cannot find view folder with ID " + id + ".");             
        } catch (Exception e) {            
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening view folder editor.",null);
        }
        return null;
    }
    
    private static IEditorPart openLRFieldEditor(Integer id) {
        try {
            LRField field = SAFRApplication.getSAFRFactory().getLRField(id, false); 
            LogicalRecord lr = field.getLogicalRecord();
            lr.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.MODEL));                                    
            EditRights rights = UIUtilities.getEditRights(lr.getId(), ComponentType.LogicalRecord);  
            LogicalRecordEditorInput input = new LogicalRecordEditorInput(lr, rights);
            LogicalRecordEditor editor = (LogicalRecordEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().openEditor(input, LogicalRecordEditor.ID);            
            editor.selectRowAtField(field.getId());
            return editor;
        } catch (Exception e) {
            UIUtilities.handleWEExceptions(
                e,"Unexpected error occurred while opening logical record field editor.",null);
        }
        return null;
    }
    
}
