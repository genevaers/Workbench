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


import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.ControlRecord;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPath;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.LookupPathStep;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.ViewFolder;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.diff.DiffBaseNode.DiffNodeState;
import com.ibm.safr.we.model.diff.DiffNode.MetaType;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;

public class DiffNodeFactory {

	public static DiffNodeComp generateDiffComp(SAFREnvironmentalComponent lhs, SAFREnvironmentalComponent rhs) throws SAFRException {

	    DiffNodeComp ret = null;
		if (lhs instanceof UserExitRoutine) {
		    ret = DiffNode.getGenerated(DiffUserExitRoutine.class, lhs.getId());
		    if (ret == null) {
    			ret = new DiffUserExitRoutine((UserExitRoutine)lhs,(UserExitRoutine)rhs);
    	        ret.generateTree();
		    }
		}
		else if (lhs instanceof PhysicalFile) {
            ret = DiffNode.getGenerated(DiffPhysicalFile.class, lhs.getId());
            if (ret == null) {
                ret = new DiffPhysicalFile((PhysicalFile)lhs,(PhysicalFile)rhs);
                ret.generateTree();
            }
        }
        else if (lhs instanceof LogicalFile) {
            ret = DiffNode.getGenerated(DiffLogicalFile.class, lhs.getId());
            if (ret == null) {
                ret = new DiffLogicalFile((LogicalFile)lhs,(LogicalFile)rhs);
                ret.generateTree();
            }
        }
        else if (lhs instanceof LRField) {
            ret = DiffNode.getGenerated(DiffLRField.class, lhs.getId());
            if (ret == null) {
                ret = new DiffLRField((LRField)lhs,(LRField)rhs);
                ret.generateTree();
            }
        }
        else if (lhs instanceof LogicalRecord) {
            ret = DiffNode.getGenerated(DiffLogicalRecord.class, lhs.getId());
            if (ret == null) {
                ret = new DiffLogicalRecord((LogicalRecord)lhs, (LogicalRecord)rhs);
                ret.generateTree();
            }
        }       		
        else if (lhs instanceof LookupPath) {
            ret = DiffNode.getGenerated(DiffLookupPath.class, lhs.getId());
            if (ret == null) {
                ret = new DiffLookupPath((LookupPath)lhs, (LookupPath)rhs);
                ret.generateTree();
            }
        }               
        else if (lhs instanceof LookupPathStep) {
            ret = DiffNode.getGenerated(DiffLookupPathStep.class, lhs.getId());
            if (ret == null) {
                ret = new DiffLookupPathStep((LookupPathStep)lhs,(LookupPathStep)rhs);
                ret.generateTree();
            }
        }               
        else if (lhs instanceof LookupPathSourceField) { 
            Integer key = lhs.getId()*100 + ((LookupPathSourceField) lhs).getKeySeqNbr();  
            ret = DiffNode.getGenerated(DiffLookupPathSrcField.class, key);
            if (ret == null) {
                ret = new DiffLookupPathSrcField((LookupPathSourceField)lhs,(LookupPathSourceField)rhs);
                ret.generateTree();
            }
        }
        else if (lhs instanceof ControlRecord) {
            ret = DiffNode.getGenerated(DiffControlRecord.class, lhs.getId());
            if (ret == null) {
                ret = new DiffControlRecord((ControlRecord)lhs,(ControlRecord)rhs);
                ret.generateTree();
            }
        }               		
        else if (lhs instanceof ViewSource) {
            ret = DiffNode.getGenerated(DiffViewSource.class, lhs.getId());
            if (ret == null) {
                ret = new DiffViewSource((ViewSource)lhs,(ViewSource)rhs);
                ret.generateTree();
            }
        }                       
        else if (lhs instanceof ViewColumn) {
            ret = DiffNode.getGenerated(DiffViewColumn.class, lhs.getId());
            if (ret == null) {
                ret = new DiffViewColumn((ViewColumn)lhs,(ViewColumn)rhs);
                ret.generateTree();
            }
        }
        else if (lhs instanceof ViewColumnSource) {
            ret = DiffNode.getGenerated(DiffViewColumnSource.class, lhs.getId());
            if (ret == null) {
                ret = new DiffViewColumnSource((ViewColumnSource)lhs,(ViewColumnSource)rhs);
                ret.generateTree();
            }
        }                       		
        else if (lhs instanceof ViewSortKey) {
            ret = DiffNode.getGenerated(DiffViewSortKey.class, lhs.getId());
            if (ret == null) {
                ret = new DiffViewSortKey((ViewSortKey)lhs,(ViewSortKey)rhs);
                ret.generateTree();
            }
        }
        else if (lhs instanceof View) {
            ret = DiffNode.getGenerated(DiffView.class, lhs.getId());
            if (ret == null) {
                ret = new DiffView((View)lhs,(View)rhs);
                ret.generateTree();
            }
        }                               
        else if (lhs instanceof ViewFolder) {
            ret = DiffNode.getGenerated(DiffViewFolder.class, lhs.getId());
            if (ret == null) {
                ret = new DiffViewFolder((ViewFolder)lhs,(ViewFolder)rhs);
                ret.generateTree();
            }
        }                               
		else {
			return null;
		}
		return ret;
	}
	
	static DiffNodeComp generateNodeComp(SAFREnvironmentalComponent comp, DiffNodeState state, int otherEnv) throws SAFRException {
        DiffNodeComp ret = null;	    
		if (comp instanceof UserExitRoutine) {
            ret = DiffNode.getGenerated(NodeUserExitRoutine.class, comp.getId());
            if (ret == null) {
                ret = new NodeUserExitRoutine((UserExitRoutine)comp, state, otherEnv);
                ret.generateTree();
            }
		}
		else if (comp instanceof PhysicalFile) {
            ret = DiffNode.getGenerated(NodePhysicalFile.class, comp.getId());
            if (ret == null) {
                ret = new NodePhysicalFile((PhysicalFile)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof LogicalFile) {
            ret = DiffNode.getGenerated(NodeLogicalFile.class, comp.getId());
            if (ret == null) {
                ret = new NodeLogicalFile((LogicalFile)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof LRField) {
            ret = DiffNode.getGenerated(NodeLRField.class, comp.getId());
            if (ret == null) {
                ret = new NodeLRField((LRField)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof LogicalRecord) {
            ret = DiffNode.getGenerated(NodeLogicalRecord.class, comp.getId());
            if (ret == null) {
                ret = new NodeLogicalRecord((LogicalRecord)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof LookupPath) {
            ret = DiffNode.getGenerated(NodeLookupPath.class, comp.getId());
            if (ret == null) {
                ret = new NodeLookupPath((LookupPath)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof LookupPathStep) {
            ret = DiffNode.getGenerated(NodeLookupPathStep.class, comp.getId());
            if (ret == null) {
                ret = new NodeLookupPathStep((LookupPathStep)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof LookupPathSourceField) {
            // generate unique id
            Integer key = comp.getId()*100 + ((LookupPathSourceField) comp).getKeySeqNbr();  
            ret = DiffNode.getGenerated(NodeLookupPathSrcField.class, key);
            if (ret == null) {
                ret = new NodeLookupPathSrcField((LookupPathSourceField)comp, state, otherEnv);
                ret.generateTree();
            }
        }
        else if (comp instanceof ControlRecord) {
            ret = DiffNode.getGenerated(NodeControlRecord.class, comp.getId());
            if (ret == null) {
                ret = new NodeControlRecord((ControlRecord)comp, state, otherEnv);
                ret.generateTree();
            }
        }                       		
        else if (comp instanceof ViewSource) {
            ret = DiffNode.getGenerated(NodeViewSource.class, comp.getId());
            if (ret == null) {
                ret = new NodeViewSource((ViewSource)comp, state, otherEnv);
                ret.generateTree();
            }
        }                       
        else if (comp instanceof ViewColumn) {
            ret = DiffNode.getGenerated(NodeViewColumn.class, comp.getId());
            if (ret == null) {
                ret = new NodeViewColumn((ViewColumn)comp, state, otherEnv);
                ret.generateTree();
            }
        }                       
        else if (comp instanceof ViewColumnSource) {
            ret = DiffNode.getGenerated(NodeViewColumnSource.class, comp.getId());
            if (ret == null) {
                ret = new NodeViewColumnSource((ViewColumnSource)comp, state, otherEnv);
                ret.generateTree();
            }
        }                       
        else if (comp instanceof ViewSortKey) {
            ret = DiffNode.getGenerated(NodeViewSortKey.class, comp.getId());
            if (ret == null) {
                ret = new NodeViewSortKey((ViewSortKey)comp, state, otherEnv);
                ret.generateTree();
            }
        }                       
        else if (comp instanceof View) {
            ret = DiffNode.getGenerated(NodeView.class, comp.getId());
            if (ret == null) {
                ret = new NodeView((View)comp, state, otherEnv);
                ret.generateTree();
            }
        }                       
		else {
			return null;
		}
        return ret;
	}

    static SAFREnvironmentalComponent generateComp(MetaType type, int id, int env) {
        try {
            switch (type) {
            case USER_EXIT_ROUTINES:
                return SAFRApplication.getSAFRFactory().getUserExitRoutine(id, env);
            case PHYSICAL_FILES:
                return SAFRApplication.getSAFRFactory().getPhysicalFile(id, env);
            case LOGICAL_FILES:
                return SAFRApplication.getSAFRFactory().getLogicalFile(id, env);
            case LOGICAL_RECORDS:
                return SAFRApplication.getSAFRFactory().getLogicalRecord(id, env);
            case LOOKUP_PATHS:
                return SAFRApplication.getSAFRFactory().getLookupPath(id, env);
            case CONTROL_RECORDS:
                return SAFRApplication.getSAFRFactory().getControlRecord(id, env);
            default:
                return null;
            }
        } catch (SAFRException e) {
            return null;
        }
    }
	
}
