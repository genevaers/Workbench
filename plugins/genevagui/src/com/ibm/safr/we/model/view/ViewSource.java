package com.ibm.safr.we.model.view;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.UserExitRoutine;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;

public class ViewSource extends SAFREnvironmentalComponent {

	private View view; // VIEWID
	private Integer sequenceNo; // SRCSEQNBR
	private List<LogicalRecordQueryBean> lrList = null;
	private Map<Integer, List<LookupQueryBean>> lrIdTolookupBeanMap = null;
	private Map<Integer, Integer> lookupIdToLogicalRecordIdMap = null;
	private Map<Integer, List<LRField>> lookupIdToLRFieldsMap = new HashMap<Integer, List<LRField>>();
	private Map<Integer, List<LookupPathSourceField>> lookupIdToSymbolicSourceFieldsMap = new HashMap<Integer, List<LookupPathSourceField>>();

	private LogicalRecord sourceLR;

	// input
    private Integer lrFileAssociationId; // INLRLFASSOCID
    private ComponentAssociation lrFileAssociation;
    
	// will be loaded in View.java using zipper
	private String extractRecordFilter; // EXTRACTFILTLOGIC
	// taken from compiler during view activation and will be stored in DB after
	// converting to blobs using SAFRBlobs.dll
	private byte[] compiledExtractRecordFilter;

	// output
    private Integer extractFileAssociationId; // Extr Output File, OUTLFPFASSOCID
    private FileAssociation extractFileAssociation;
    private Integer writeExitId; // Extract Phase, WRITEEXITID
    private UserExitRoutine writeExit; 
    private String writeExitParams; // Extract Phase, WRITEEXITPARAM
    private boolean extractOutputOverride; // EXTRACTOUTPUTIND
	private String extractRecordOutput; // EXTRACTOUTPUTLOGIC
	/**
	 * This constructor is used when creating a new ViewSource for the parent
	 * View. The Environment ID will be set to the same Environment as the
	 * parent View. The ViewSource ID will be initialized to zero and then set
	 * to a unique value when the ViewSource object is persisted via its
	 * <code>store()</code> method.
	 */
	ViewSource(View parentView) {
		super(parentView.getEnvironmentId());
		this.view = parentView;
	}

	/**
	 * Create a ViewSource object for the parent View, containing the data in
	 * the specified transfer object. Used to instantiate existing ViewSource
	 * objects.
	 * 
	 * @param trans
	 *            the ViewSourceTransfer object
	 * @throws DAOException
	 */
	ViewSource(View parentView, ViewSourceTransfer trans) throws DAOException {
		super(trans);
		this.view = parentView;
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		ViewSourceTransfer trans = (ViewSourceTransfer) safrTrans;
		this.sequenceNo = trans.getSourceSeqNo();
		this.lrFileAssociationId = trans.getLRFileAssocId();
		if (lrFileAssociation != null && lrFileAssociation.getAssociationId() != trans.getLRFileAssocId()) {
			this.lrFileAssociation = null;
		}
        this.extractRecordFilter = trans.getExtractFilterLogic();
		this.extractFileAssociationId = trans.getExtractFileAssociationId();
		this.writeExitId = trans.getWriteExitId();
		this.writeExitParams = trans.getWriteExitParams();
        this.extractOutputOverride = trans.isExtractOutputOverride();
        this.extractRecordOutput = trans.getExtractRecordOutput();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		ViewSourceTransfer trans = (ViewSourceTransfer) safrTrans;
		trans.setViewId(view.getId());
		trans.setSourceSeqNo(sequenceNo);
		trans.setLRFileAssocId(lrFileAssociationId);
		trans.setExtractFilterLogic(extractRecordFilter);
        trans.setExtractFileAssociationId(extractFileAssociationId);
        trans.setWriteExitId(writeExitId);
        trans.setWriteExitParams(writeExitParams);
        trans.setExtractOutputOverride(extractOutputOverride);
        trans.setExtractRecordOutput(extractRecordOutput);
	}

	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * @return the sequenceNo
	 */
	public Integer getSequenceNo() {
		return sequenceNo;
	}

	/**
	 * @param sequenceNo
	 *            the sequenceNo to set
	 */
	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	public Integer getLrFileAssociationId() {
		return lrFileAssociationId;
	}

    public void setLrFileAssociationId(Integer lrFileAssociationId) {
        this.lrFileAssociationId = lrFileAssociationId;
    }
    	
	/**
	 * @return the lrFileAssociation
	 */
	public ComponentAssociation getLrFileAssociation() throws DAOException,
			SAFRException {
		if (lrFileAssociation == null) {
			if (lrFileAssociationId != null && lrFileAssociationId > 0) {
				// lazy initialize and cache the object
				this.lrFileAssociation = SAFRAssociationFactory
						.getLogicalRecordToLogicalFileAssociation(
								lrFileAssociationId, getEnvironmentId());
			}
		}
		return lrFileAssociation;
	}
	
	/**
	 * @param lrFileAssociation
	 *            the lrFileAssociation to set
	 */
	public void setLrFileAssociation(ComponentAssociation lrFileAssociation) {
		this.lrFileAssociation = lrFileAssociation;
		if (lrFileAssociation == null) {
			this.lrFileAssociationId = null;
		} else {
			this.lrFileAssociationId = lrFileAssociation.getAssociationId();
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * Returns the logic text for the ViewSource's Extract Record Filter.
	 * 
	 * @return a String of logic text
	 */
	public String getExtractRecordFilter() {
		return extractRecordFilter;
	}

    /**
     * Sets the logic text for the ViewSource's Extract Record Filter.
     * 
     * @param extractRecordFilter
     *            a String of logic text
     */
    public void setExtractRecordFilter(String extractRecordFilter) {
        this.extractRecordFilter = extractRecordFilter;
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }
	
	/**
	 * This is a package private setter to be used by model package only.This
	 * method sets the logic text for viewSource's Extract Record Filter without
	 * affecting the state of the view.
	 * 
	 * @param extractRecordFilter
	 *            :a String of logic text
	 */
	public void setCompiledExtractRecordFilter(
			byte[] compiledExtractRecordFilter) {
		this.compiledExtractRecordFilter = compiledExtractRecordFilter;
	}

	public byte[] getCompiledExtractRecordFilter() {
		return compiledExtractRecordFilter;
	}

    public Integer getExtractFileAssociationId() {
        return extractFileAssociationId;
    }

    public void setExtractFileAssociationId(Integer extractFileAssociationId) {
        this.extractFileAssociationId = extractFileAssociationId;
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }

    /**
     * @return the extractFileAssociation
     */
    public FileAssociation getExtractFileAssociation() throws DAOException,
            SAFRException {
        if (extractFileAssociation == null) {
            if (extractFileAssociationId != null) {
                // lazy initialize and cache the object
                this.extractFileAssociation = SAFRAssociationFactory
                        .getLogicalFileToPhysicalFileAssociation(
                                extractFileAssociationId, getEnvironmentId());
            }
        }
        return extractFileAssociation;
    }

    /**
     * @param extractFileAssociation
     *            the extractFileAssociation to set
     */
    public void setExtractFileAssociation(FileAssociation extractFileAssociation) {
        this.extractFileAssociation = extractFileAssociation;
        if (extractFileAssociation == null) {
            this.extractFileAssociationId = null;
        } else {
            this.extractFileAssociationId = extractFileAssociation.getAssociationId();
        }
        view.makeViewInactive();
        markModified();
        view.markUpdated();
    }
    
    public Integer getWriteExitId() {
        return writeExitId;
    }

    public void setWriteExitId(Integer writeExitId) {
        this.writeExitId = writeExitId;
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }

    /**
     * @return the writeExit
     */
    public UserExitRoutine getWriteExit() throws SAFRException {
        if (writeExit == null) {
            if (writeExitId != null && writeExitId > 0) {
                // lazy initialize and cache the object
                this.writeExit = SAFRApplication.getSAFRFactory()
                        .getUserExitRoutine(writeExitId, this.getEnvironmentId());
            }
        }
        return writeExit;
    }

    /**
     * @param writeExit
     *            the writeExit to set
     */
    public void setWriteExit(UserExitRoutine writeExit) {

        if (writeExit == null) {
            this.writeExitId = null;
        } else {
            this.writeExitId = writeExit.getId();
        }
        this.writeExit = writeExit;
        view.makeViewInactive();
        view.markUpdated();
        markModified();
    }
    
    public String getWriteExitParams() {
        return writeExitParams;
    }

    public void setWriteExitParams(String writeExitParams) {
        this.writeExitParams = writeExitParams;
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }
	
    public boolean isExtractOutputOverriden() {
        return extractOutputOverride;
    }

    public boolean isExtractOutputOverrideBlocked() {
        return extractOutputOverride == false;
    }

    public void setExtractOutputOverride(boolean extractOutputOverride) {
        this.extractOutputOverride = extractOutputOverride;
    }

    /**
     * Sets the logic text for the ViewSource's Extract Record Filter.
     * 
     * @param extractRecordFilter
     *            a String of logic text
     */
    public void setExtractRecordOutput(String extractRecordOutput) {
        this.extractRecordOutput = extractRecordOutput;
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }

    /**
     * Returns the logic text for the ViewSource's Extract Record Filter.
     * 
     * @return a String of logic text
     */
    public String getExtractRecordOutput() {
        return extractRecordOutput;
    }
    
	/**
	 * @return a read-only list of the ViewColumnSources for this ViewSource
	 */
	public SAFRImmutableList<ViewColumnSource> getViewColumnSources() {
		List<ViewColumnSource> result = new ArrayList<ViewColumnSource>();
		List<ViewColumnSource> vcss = view.getViewColumnSources()
				.getActiveItems();
		for (ViewColumnSource vcs : vcss) {
			// Mustufa;CQ8232;Check the sequence number which is more reliable
			// then Id. As Id is 0 for all view sources of a new view.
			if (vcs.getViewSource().getSequenceNo()
					.equals(this.getSequenceNo())) {
				result.add(vcs);
			}
		}
		return new SAFRImmutableList<ViewColumnSource>(result);
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO Auto-generated method stub

	}

	private void getViewSourceLookupPathDetails() throws DAOException,
			SAFRException {
		Integer lrId = this.getLrFileAssociation().getAssociatingComponentId();
		Integer envId = this.getEnvironmentId();
		List<LookupQueryBean> lookupQueryBeanList = null;
		List<EnvironmentalQueryBean> envQueryBeanList = null;

		// get the map from the database which has Logical Record Id as the
		// key and list of Environmental Query bean as values
		Map<Integer, List<EnvironmentalQueryBean>> lkLRmap = DAOFactoryHolder
				.getDAOFactory().getViewSourceDAO()
				.getViewSourceLookupPathDetails(lrId, envId);

		lrList = new ArrayList<LogicalRecordQueryBean>();
		lrIdTolookupBeanMap = new HashMap<Integer, List<LookupQueryBean>>();
		lookupIdToLogicalRecordIdMap = new HashMap<Integer, Integer>();

		for (Integer logicalRecordId : lkLRmap.keySet()) {
			
			lookupQueryBeanList = new ArrayList<LookupQueryBean>();
			
			envQueryBeanList = lkLRmap.get(logicalRecordId);
			// the map contains a Logical Record Query Bean as the first value
			// for every item in the keyset.
			LogicalRecordQueryBean lrbean = (LogicalRecordQueryBean) envQueryBeanList.get(0);
			if (!SAFRApplication.getUserSession().isSystemAdministrator()) {
				// get edit rights and change the query bean.
				EditRights rights = SAFRApplication.getUserSession().getEditRights(
				    ComponentType.LogicalRecord,lrbean.getId());
				lrbean = new LogicalRecordQueryBean(lrbean.getEnvironmentId(),
						lrbean.getId(), lrbean.getName(), lrbean.getStatus(), 
						lrbean.getTotLen(), lrbean.getKeyLen(), lrbean.getType(),
						rights, lrbean.getCreateTime(), lrbean.getCreateBy(), 
						lrbean.getModifyTime(), lrbean.getModifyBy(), lrbean.getActivatedTime(), lrbean.getActivatedBy());
			}
			lrList.add(lrbean);

			// other than the first item, all other values are Lookup Query
			// Beans.Loop through the map to get a list of LookupQueryBeans.
			List<Integer> lookupIds = null;
			for (int i = 1; i < envQueryBeanList.size(); i++) {
				lookupIds = new ArrayList<Integer>();

				LookupQueryBean lookupbean = (LookupQueryBean) envQueryBeanList
						.get(i);
				lookupIds.add(lookupbean.getId());

				if (!SAFRApplication.getUserSession().isSystemAdministrator()) {
	                EditRights lookupRights = SAFRApplication.getUserSession().getEditRights(
	                    ComponentType.LookupPath,lookupbean.getId());
					lookupbean = new LookupQueryBean(lookupbean
							.getEnvironmentId(), lookupbean.getId(), lookupbean.getName(), 
							lookupbean.getSourceLR(), lookupbean.getValidInd(), 
							lookupbean.getnSteps(), lookupbean.getTargetLR(), lookupbean.getTargetLF(), 
							lookupRights, lookupbean.getCreateTime(),lookupbean.getCreateBy(), 
							lookupbean.getModifyTime(), lookupbean.getModifyBy(),
							lookupbean.getActivatedTime(), lookupbean.getActivatedBy());
				}

				lookupQueryBeanList.add(lookupbean);

				// using the above map create another map which has Lookup Id as
				// the key
				// and Logical record id as value.
				Integer lookupId = envQueryBeanList.get(i).getId();
				lookupIdToLogicalRecordIdMap.put(lookupId, logicalRecordId);
			}
			lrIdTolookupBeanMap.put(logicalRecordId, lookupQueryBeanList);
		}

	}

	/**
	 * This method is used to get the list of logical records.
	 * 
	 * @return list of logical record query beans or an empty list if no logical
	 *         records are found.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public List<LogicalRecordQueryBean> getLookupLogicalRecords()
			throws DAOException, SAFRException {
		if (lrList == null) {
			this.getViewSourceLookupPathDetails();
		}
		return lrList;
	}

	/**
	 * This method is used to get the list of lookup path query beans associated
	 * with the specified Logical record id.
	 * 
	 * @param logicalRecordId
	 *            : the id of the logical record whose associated lookups are to
	 *            be fetched.
	 * @return the list of lookup query beans or a null list if no lookup paths
	 *         are found.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public List<LookupQueryBean> getLookupPaths(Integer logicalRecordId)
			throws DAOException, SAFRException {
		if (lrIdTolookupBeanMap == null) {
			this.getViewSourceLookupPathDetails();
		}
		return lrIdTolookupBeanMap.get(logicalRecordId);
	}

	/**
	 * This method is used to get all the lookup paths.
	 * 
	 * @return a list of lookup path query bean or an empty list if no lookup
	 *         paths are found.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public List<LookupQueryBean> getAllLookupPaths() throws DAOException,
			SAFRException {
		List<LookupQueryBean> lookupQueryBeanList = new ArrayList<LookupQueryBean>();
		if (lrIdTolookupBeanMap == null) {
			this.getViewSourceLookupPathDetails();
		}
		for (Integer lrId : lrIdTolookupBeanMap.keySet()) {
			// CQ7377 SANTHOSH 14/01/2010 (Applied condition to check that if a
			// lookup already exists do not add another lookup which was adding
			// all without checking this condition).
			List<LookupQueryBean> list = lrIdTolookupBeanMap.get(lrId);
			for (LookupQueryBean lookupQueryBean : list) {
				if ((!lookupQueryBeanList.contains(lookupQueryBean))) {
					lookupQueryBeanList.add(lookupQueryBean);
				}
			}

		}
		Collections.sort(lookupQueryBeanList);
		return lookupQueryBeanList;
	}

	/**
	 * This method is used to get the list of LR Fields of the specified Lookup
	 * path id.
	 * 
	 * @param lookupPathId
	 *            : the id of the lookup path whose LR fields are to be
	 *            retrieved.
	 * @return the list of LR fields or an empty list if no lookup fields are
	 *         found.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public List<LRField> getLookupFields(Integer lookupPathId)
			throws SAFRException, DAOException {
		if (lookupIdToLRFieldsMap.containsKey(lookupPathId)) {
			return lookupIdToLRFieldsMap.get(lookupPathId);
		} else {
			List<LRField> lrFieldList = new ArrayList<LRField>();
			if (lookupIdToLogicalRecordIdMap == null) {
				this.getViewSourceLookupPathDetails();
			}

			// use the lookupIdLogicalRecordIdMap map to search for the LR of
			// the lookup.
			Integer logicalRecordId = lookupIdToLogicalRecordIdMap
					.get(lookupPathId);
			if (logicalRecordId == null) {
				return lrFieldList;
			}

			// get all the LR fields of the logicalRecordId and make a
			// list of LR fields.
			List<LRFieldTransfer> lrFieldTransferList = DAOFactoryHolder
					.getDAOFactory().getLRFieldDAO().getLRFields(
							getEnvironmentId(), logicalRecordId);
			for (int i = 0; i < lrFieldTransferList.size(); i++) {
				LRField lrField = new LRField(lrFieldTransferList.get(i));
				lrFieldList.add(lrField);
			}

			// put it in temporary map which has Lookup id as key and List
			// of LR fields as value.
			lookupIdToLRFieldsMap.put(lookupPathId, lrFieldList);

			return lrFieldList;
		}
	}

	public Integer getTargetLR(Integer lookupId) {
	    return lookupIdToLogicalRecordIdMap.get(lookupId);
	}
	
	LRField getLookupField(Integer lookupPathId, Integer fieldId)
			throws DAOException, SAFRException {
		// returns an LR field from a lookup path using the
		// lookupIdToLRFieldsMap.
		// returns null if not found.
		List<LRField> fields = getLookupFields(lookupPathId);
		if (!fields.isEmpty()) {
			// check the fields and return the matching one.
			for (LRField field : fields) {
				if (field.getId().equals(fieldId)) {
					return field; // field found
				}
			}
		}
		return null;
	}

	LRField getSourceLRField(Integer fieldId) throws DAOException,
			SAFRException {
		// returns the LR field from the source LR.
		if (sourceLR == null) {
			// initialize the source LR first.
			sourceLR = SAFRApplication.getSAFRFactory().getLogicalRecord(
					getLrFileAssociation().getAssociatingComponentId(),
					getEnvironmentId());
		}
		// search the LR and return the matching field.
		for (LRField field : sourceLR.getLRFields()) {
			if (field.getId().equals(fieldId)) {
				return field;
			}
		}
		// return null if no matching fields found
		return null;
	}

	/**
	 * This method is used to get the Symbolic Source Fields of a lookup path.
	 * 
	 * @param lookupId
	 *            : the id of the lookup whose symbolic source fields are to be
	 *            retrieved.
	 * @return a list of Lookup Path source fields which are of type symbol or
	 *         an empty list if no symbolic Source fields are found.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public List<LookupPathSourceField> getLookupSymbolicFields(Integer lookupId)
			throws DAOException, SAFRException {
		if (lookupIdToSymbolicSourceFieldsMap.containsKey(lookupId)) {
			return lookupIdToSymbolicSourceFieldsMap.get(lookupId);
		} else {
			Integer envId = this.getLrFileAssociation().getEnvironmentId();
			// This list is required to get the map which has lookupId as the
			// key and List of Symbolic source fields as value
			List<Integer> lookupIdList = new ArrayList<Integer>();

			List<LookupQueryBean> lookupQueryBeans = this.getAllLookupPaths();

			for (LookupQueryBean lookupQueryBean : lookupQueryBeans) {
				lookupIdList.add(lookupQueryBean.getId());
			}

			// get the map which contains Lookup id as key and list of
			// LookupSourceFieldTransfer as value.
			Map<Integer, List<LookupPathSourceFieldTransfer>> symbolicLRFieldsTransfer = DAOFactoryHolder
					.getDAOFactory().getViewSourceDAO()
					.getLookupPathSymbolicFields(lookupIdList, envId);
			// use the map to get the list of LookupSourceFields.
			for (Integer lookupPathId : symbolicLRFieldsTransfer.keySet()) {
                List<LookupPathSourceField> symbolicLookupSourceFields = new ArrayList<LookupPathSourceField>();			    
				for (LookupPathSourceFieldTransfer lookupSourceFieldTransfer : symbolicLRFieldsTransfer
						.get(lookupPathId)) {
					LookupPathSourceField lookupPathSourceField = new LookupPathSourceField(
							lookupSourceFieldTransfer);
					symbolicLookupSourceFields.add(lookupPathSourceField);
				}
				lookupIdToSymbolicSourceFieldsMap.put(lookupPathId,
						symbolicLookupSourceFields);

			}

			return lookupIdToSymbolicSourceFieldsMap.get(lookupId);
		}
	}

	/**
	 * Validate Extract Record Filter using this View.
	 * 
	 * @param logicText
	 *            The format record filter logic text to be validated.
	 * @throws DAOException
	 * @throws SAFRException
	 *             SAFRValidation exception will be thrown with a list of
	 *             validation errors.
	 */
	public void validateExtractRecordFilter(String logicText) {
	    LogicTextSyntaxChecker.checkSyntaxExtractFilter(logicText, view, this);
	}

    public void validateExtractRecordOutput(String logicText) throws SAFRException {
    	LogicTextSyntaxChecker.checkSyntaxExtractOutput(logicText, view, this);
    }
	
	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getDescriptor() {
		String srcDesc = "";
		if (lrFileAssociation != null) {
			srcDesc = lrFileAssociation.getAssociatingDescriptor() + "."
					+ lrFileAssociation.getAssociatedDescriptor();
		}
		return srcDesc;
	}

}
