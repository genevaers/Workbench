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


import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;

public class NodePhysicalFile extends DiffNodeComp {

	PhysicalFile pf;
	int otherEnv;

	public NodePhysicalFile(PhysicalFile lhs, DiffNodeState state, int otherEnv) {
		this.pf = lhs;
		this.state = state;
		this.otherEnv = otherEnv;
	}

    protected DiffNode generateDatasetInput() throws SAFRException {
        
        InputDataset inDS = pf.new InputDataset();
        DiffNodeSection dsInput = new DiffNodeSection();            
        
        dsInput.setName("Input");         
        dsInput.addStringField("DD Name", inDS.getInputDDName(), pf.getEnvironmentId(), state);
        dsInput.addStringField("DSN", inDS.getDatasetName(), pf.getEnvironmentId(), state);
        dsInput.addIntField("Min Record Length", inDS.getMinRecordLen(), pf.getEnvironmentId(), state);
        dsInput.addIntField("Max Record Length", inDS.getMaxRecordLen(), pf.getEnvironmentId(), state);
        dsInput.setState(state);            
        return dsInput;
    }

    protected DiffNode generateDatasetOutput() throws SAFRException {
        
        OutputDataset lhsOutDS = pf.new OutputDataset();
        
        DiffNodeSection dsOutput = new DiffNodeSection();            
        
        dsOutput.setName("Output"); 
        
        dsOutput.addStringField("DD Name", lhsOutDS.getOutputDDName(), pf.getEnvironmentId(), state);
        dsOutput.addStringField("DSN", lhsOutDS.getDatasetName(), pf.getEnvironmentId(), state);
        dsOutput.addCodeField("RECFM", lhsOutDS.getRecfm(), pf.getEnvironmentId(), state);
        dsOutput.addIntField("LRECL", lhsOutDS.getLrecl(), pf.getEnvironmentId(), state);
        dsOutput.setState(state);            

        return dsOutput;
    }
	
    protected DiffNode generateDataset() throws SAFRException {
        DiffNode dataset = null;
        // process Dataset information
        Integer lhsAccess = pf.getAccessMethodCode().getGeneralId();
        if (lhsAccess != Codes.DB2VIASQL) {
            dataset = new DiffNodeSection();        
            dataset.setName("Dataset"); 
            dataset.setParent(this);
            
            DiffNode dsInput = generateDatasetInput();
            dsInput.setParent(dataset);
            dataset.addChild(dsInput);

            DiffNode dsOutput = generateDatasetOutput();
            dsOutput.setParent(dataset);
            dataset.addChild(dsOutput);
            
            dataset.setState(state);
        }
        return dataset;        
    }
    
    protected DiffNode generateSQL() throws SAFRException {
        DiffNodeSection sql = null;
        // process SQL information
        Integer access = pf.getAccessMethodCode().getGeneralId();
        if (access == Codes.DB2VIASQL) {
            sql = new DiffNodeSection();        
            sql.setName("SQL");            
            PhysicalFile.SQLDatabase sqlDb = pf.new SQLDatabase();            
            sql.addStringField("DD Name", sqlDb.getInputDDName(), pf.getEnvironmentId(), state);
            sql.addStringField("Subsystem", sqlDb.getSubSystem(), pf.getEnvironmentId(), state);
            sql.addStringField("Table Name", sqlDb.getTableName(), pf.getEnvironmentId(), state);
            sql.addCodeField("Row Format", sqlDb.getRowFormatCode(), pf.getEnvironmentId(), state);
            sql.addBoolField("Return Null Ind", sqlDb.isIncludeNullIndicators(), pf.getEnvironmentId(), state);
            sql.addLargeStringField("SQL Query", sqlDb.getSqlStatement(), pf.getEnvironmentId(), state);
            sql.setState(state);            
        }
        return sql;
    }
	
    protected DiffNode generateGeneral() throws SAFRException {
        DiffNodeSection gi = new DiffNodeSection();
        gi.setState(state);
        gi.setName("General"); 
        gi.addStringField("Name", pf.getName(), pf.getEnvironmentId(), state);
        gi.addCodeField("File Type", pf.getFileTypeCode(), pf.getEnvironmentId(), state);
        gi.addCodeField("Access Method", pf.getAccessMethodCode(), pf.getEnvironmentId(), state);
        if (pf.getUserExitRoutine() != null) {
            gi.getFields().add(nodeSingleReference(MetaType.USER_EXIT_ROUTINES, "Read Exit", 
                pf.getUserExitRoutine(), state, otherEnv));
        }
        gi.addStringField("User Exit Params", pf.getUserExitRoutineParams(), pf.getEnvironmentId(), state);
        if (DiffNode.weFields) {
            gi.addStringField("Comments", pf.getComment(), pf.getEnvironmentId(), state);
            gi.addStringField("Created By", pf.getCreateBy(), pf.getEnvironmentId(), state);
            gi.addDateField("Created Time", pf.getCreateTime(), pf.getEnvironmentId(), state);
            gi.addStringField("Modified By", pf.getModifyBy(), pf.getEnvironmentId(), state);
            gi.addDateField("Last Modified", pf.getModifyTime(), pf.getEnvironmentId(), state);
        }
        return gi;
    }
    
	protected void generateTree() throws SAFRException {		

		NodePhysicalFile tree = (NodePhysicalFile) getGenerated(NodePhysicalFile.class, pf.getId());
		if (tree == null) {
		    
			setId(pf.getId());
            setEnvID(pf.getEnvironmentId());

			setName("Physical File");
			DiffNode gi = generateGeneral();
			addChild(gi);
			
            DiffNode sql = generateSQL();
            if (sql != null) {
                addChild(sql);
            }

            DiffNode dataset = generateDataset();
            if (dataset != null) {
                addChild(dataset);
            }
			
			storeGenerated(NodePhysicalFile.class, pf.getId(), this);
			addMetadataNode(MetaType.PHYSICAL_FILES, this);
		}
	}

}
