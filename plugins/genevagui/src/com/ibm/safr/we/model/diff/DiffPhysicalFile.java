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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.PhysicalFile;
import com.ibm.safr.we.model.PhysicalFile.InputDataset;
import com.ibm.safr.we.model.PhysicalFile.OutputDataset;

public class DiffPhysicalFile extends DiffNodeComp {

	private PhysicalFile lhs;
	private PhysicalFile rhs;
		
	public DiffPhysicalFile(PhysicalFile lhs, PhysicalFile rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	protected DiffNode generateGeneral() throws SAFRException {
		// process general information
		DiffNodeSection gi = new DiffNodeSection();
		gi.setName("General"); 
		gi.setParent(this);
		Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
		diff.add(gi.addStringField("Name", lhs.getName(), rhs.getName(), lhs.getEnvironmentId(), rhs.getEnvironmentId())); 
		diff.add(gi.addCodeField("File Type", lhs.getFileTypeCode(), rhs.getFileTypeCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
		diff.add(gi.addCodeField("Access Method", lhs.getAccessMethodCode(), rhs.getAccessMethodCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
		
		List<DiffFieldReference> refList = diffSingleReferences(MetaType.USER_EXIT_ROUTINES, "Read Exit", 
		    lhs.getUserExitRoutine(), rhs.getUserExitRoutine(), 
		    lhs.getEnvironment().getId(), rhs.getEnvironment().getId());
		for (DiffFieldReference ref :refList) {		    
			diff.add(ref.getState());
			gi.getFields().add(ref);
		}
		
        diff.add(gi.addStringField("User Exit Params", lhs.getUserExitRoutineParams(), rhs.getUserExitRoutineParams(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        if (DiffNode.weFields) {
            diff.add(gi.addStringField("Comments", lhs.getComment(), rhs.getComment(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));		
            diff.add(gi.addStringField("Created By", lhs.getCreateBy(), rhs.getCreateBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Created Time", lhs.getCreateTime(), rhs.getCreateTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addStringField("Modified By", lhs.getModifyBy(), rhs.getModifyBy(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(gi.addDateField("Last Modified", lhs.getModifyTime(), rhs.getModifyTime(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        }
		
		if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
			gi.setState(DiffNodeState.Different);
		}
		else {
			gi.setState(DiffNodeState.Same);			
		}		
		return gi;
	}

    protected DiffNode generateSQL() throws SAFRException {
        DiffNodeSection sql = null;
        // process SQL information
        Integer lhsAccess = lhs.getAccessMethodCode().getGeneralId();
        Integer rhsAccess = rhs.getAccessMethodCode().getGeneralId();
        if (lhsAccess == Codes.DB2VIASQL || rhsAccess == Codes.DB2VIASQL) {
            sql = new DiffNodeSection();        
            sql.setName("SQL"); 
            sql.setParent(this);
            Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
            
            PhysicalFile.SQLDatabase lhsSqlDb = lhs.new SQLDatabase();
            PhysicalFile.SQLDatabase rhsSqlDb = rhs.new SQLDatabase();
            
            diff.add(sql.addStringField("Subsystem", lhsSqlDb.getSubSystem(), rhsSqlDb.getSubSystem(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(sql.addStringField("DD Name", lhsSqlDb.getInputDDName(), rhsSqlDb.getInputDDName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(sql.addLargeStringField("SQL Query", lhsSqlDb.getSqlStatement(), rhsSqlDb.getSqlStatement(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(sql.addStringField("Table Name", lhsSqlDb.getTableName(), rhsSqlDb.getTableName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(sql.addCodeField("Row Format", lhsSqlDb.getRowFormatCode(), rhsSqlDb.getRowFormatCode(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
            diff.add(sql.addBoolField("Return Null Ind", lhsSqlDb.isIncludeNullIndicators(), rhsSqlDb.isIncludeNullIndicators(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));

            if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
                sql.setState(DiffNodeState.Different);
            }
            else {
                sql.setState(DiffNodeState.Same);            
            }       
        }
        return sql;
    }

    protected DiffNode generateDatasetInput() throws SAFRException {
        
        InputDataset lhsInDS = lhs.new InputDataset();
        InputDataset rhsInDS = rhs.new InputDataset();
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        DiffNodeSection dsInput = new DiffNodeSection();            
        
        dsInput.setName("Input"); 
        
        diff.add(dsInput.addStringField("DD Name", lhsInDS.getInputDDName(), rhsInDS.getInputDDName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(dsInput.addStringField("DSN", lhsInDS.getDatasetName(), rhsInDS.getDatasetName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(dsInput.addIntField("Min Record Length", lhsInDS.getMinRecordLen(), rhsInDS.getMinRecordLen(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(dsInput.addIntField("Max Record Length", lhsInDS.getMaxRecordLen(), rhsInDS.getMaxRecordLen(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            dsInput.setState(DiffNodeState.Different);
        }
        else {
            dsInput.setState(DiffNodeState.Same);            
        }       
        return dsInput;
    }

    protected DiffNode generateDatasetOutput() throws SAFRException {
        
        OutputDataset lhsOutDS = lhs.new OutputDataset();
        OutputDataset rhsOutDS = rhs.new OutputDataset();
        
        Set<DiffNodeState> diff = new HashSet<DiffNodeState>();
        DiffNodeSection dsOutput = new DiffNodeSection();            
        
        dsOutput.setName("Output"); 
        
        diff.add(dsOutput.addStringField("DD Name", lhsOutDS.getOutputDDName(), rhsOutDS.getOutputDDName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(dsOutput.addStringField("DSN", lhsOutDS.getDatasetName(), rhsOutDS.getDatasetName(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(dsOutput.addCodeField("RECFM", lhsOutDS.getRecfm(), rhsOutDS.getRecfm(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        diff.add(dsOutput.addIntField("LRECL", lhsOutDS.getLrecl(), rhsOutDS.getLrecl(), lhs.getEnvironmentId(), rhs.getEnvironmentId()));
        
        if (diff.contains(DiffNodeState.Different) || diff.contains(DiffNodeState.Added) || diff.contains(DiffNodeState.Removed)) {
            dsOutput.setState(DiffNodeState.Different);
        }
        else {
            dsOutput.setState(DiffNodeState.Same);            
        }       

        return dsOutput;
    }
    
    protected DiffNode generateDataset() throws SAFRException {
        DiffNode dataset = null;
        // process Dataset information
        Integer lhsAccess = lhs.getAccessMethodCode().getGeneralId();
        Integer rhsAccess = rhs.getAccessMethodCode().getGeneralId();
        if (lhsAccess != Codes.DB2VIASQL || rhsAccess != Codes.DB2VIASQL) {
            dataset = new DiffNodeSection();        
            dataset.setName("Dataset"); 
            dataset.setParent(this);
            
            DiffNode dsInput = generateDatasetInput();
            dsInput.setParent(dataset);
            dataset.addChild(dsInput);

            DiffNode dsOutput = generateDatasetOutput();
            dsOutput.setParent(dataset);
            dataset.addChild(dsOutput);
            
            if ((dsInput.getState() == DiffNodeState.Same) &&
                (dsOutput.getState() == DiffNodeState.Same)) {
                dataset.setState(DiffNodeState.Same);
            }
            else {
                dataset.setState(DiffNodeState.Different);
            }   
            
        }
        return dataset;        
    }
    
	@Override
	protected void generateTree()  throws SAFRException {
		DiffPhysicalFile tree = (DiffPhysicalFile) getGenerated(DiffPhysicalFile.class, lhs.getId());
		if (tree == null) {
			setId(lhs.getId());
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
            
			if ((gi.getState() == DiffNodeState.Same) &&
			    (sql == null || sql.getState() == DiffNodeState.Same) &&
                (dataset == null || dataset.getState() == DiffNodeState.Same)) {
				setState(DiffNodeState.Same);
			}
			else {
				setState(DiffNodeState.Different);
			}	
			storeGenerated(DiffPhysicalFile.class, lhs.getId(), this);
			addMetadataNode(MetaType.PHYSICAL_FILES, this);			
		}
	}

}
