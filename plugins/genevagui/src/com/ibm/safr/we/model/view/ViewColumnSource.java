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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.data.transfer.ViewColumnSourceTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Code;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.ModelUtilities;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.utilities.importer.ModelTransferProvider;
import com.ibm.safr.we.model.view.View.Property;

public class ViewColumnSource extends SAFREnvironmentalComponent {
	
	static transient Logger logger = Logger
			.getLogger("com.ibm.safr.we.model.view.ViewColumnSource");

	/*
	 * The following columns of table VIEWCOLUMNSOURCE are not represented in
	 * this class because they are redundant.
	 */
	/* data visible at the UI */
	private View view; // VIEWID
	private Integer viewColumnId; // VIEWCOLUMNID
	private ViewColumn viewColumn; // VIEWCOLUMNID
	private Integer viewSourceId; // VIEWSOURCEID
	private ViewSource viewSource; // VIEWSOURCEID
	private Code sourceType; // SOURCETYPEID
	private Integer lrFieldId; // LRFIELDID
	private LRField lrField;
	private Integer sortKeyTitleLRFieldId; // SORTTITLELRFIELDID
	private LRField sortKeyTitleLRField;
	private Integer lookupPathId; // LOOKUPID
	private LookupQueryBean lookupQueryBean;
	private String sourceValue; // SRCVAL
	private Code effectiveDateTypeCode; // EFFDATTYPE
	private String effectiveDateValue; // EFFDATVALUE
	private Integer effectiveDateLRFieldId;// EFFDATELRFIELDID
	private LRField effectiveDateLRField;
	private LogicalRecordQueryBean logicalRecordQueryBean;
	private Integer sortKeyTitleLookupPathId; // SORTTITLEPATHID
    private LookupQueryBean sortKeyTitleLookupPathQueryBean;
	private LogicalRecordQueryBean sortKeyTitleLogicalRecordQueryBean;

	// will loaded in View.java using zipper
	private String extractColumnAssignment;
	// taken from compiler during view activation and will be stored in DB after
	// converting to blobs using SAFRBlobs.dll
	private byte[] compiledExtractColumnAssignment;

	private List<String> loadWarnings;

	private ModelTransferProvider provider;

	/**
	 * This constructor is used to create a new ViewColumnSource object. A
	 * ViewColumnSource associates a ViewColumn with a ViewSource, so references
	 * to these two objects must be supplied to this constructor. The two
	 * objects must share the same parent View. The ViewColumnSource ID will be
	 * initialized to zero and then set to a unique value when the
	 * ViewColumnSource object is persisted via its <code>store()</code> method.
	 * 
	 * @param viewColumn
	 *            the associated ViewColumn
	 * @param viewSource
	 *            the associated ViewSource
	 * @throws IllegalArgumentException
	 *             if the ViewColumn and ViewSource do not share the same parent
	 *             View
	 */
	ViewColumnSource(ViewColumn viewColumn, ViewSource viewSource) {
		super(viewColumn.getEnvironmentId());

		if (!viewColumn.getView().equals(viewSource.getView())) {
			// the column and source must be from the same view
			throw new IllegalArgumentException(
					"ViewColumn and ViewSource must share the same parent View.");
		}

		this.view = viewColumn.getView();
		this.viewColumn = viewColumn;
		this.viewSource = viewSource;
		// effective date type is by default RunDate.
		this.effectiveDateTypeCode = SAFRApplication.getSAFRFactory()
				.getCodeSet(CodeCategories.RELPERIOD).getCode(
						Codes.RELPERIOD_RUNDATE);
		this.effectiveDateValue = "Runday()";
	}

	/**
	 * This constructor instantiates an existing ViewColumnSource object for the
	 * specified View object from the data contained in the specified data
	 * transfer object.
	 * <p>
	 * For Import only, if the View parameter is null the object will be marked
	 * for deletion.
	 * 
	 * @param view
	 *            the parent View
	 * @param trans
	 *            the ViewColumnSourceTransfer object
	 */
	ViewColumnSource(View view, ViewColumnSourceTransfer trans)
			throws DAOException, SAFRException {
		super(trans);
		this.view = view;
		
		if (view == null && isForImport()) {
			markDeleted();
		} else {
			// CQ9628 check that the parent View contains the View Source referenced
			// by this View Column Source.
			ViewSource vs = getViewSource();
			if (vs == null) {
				String msg = "ViewSource [" + viewSourceId
						+ "] for ViewColumnSource [" + this.getId()
						+ "] does not exist in View [" + this.getView().getId()
						+ "].";
				logger.log(
						Level.SEVERE,
						msg
								+ SAFRUtilities.LINEBREAK + "A View Column Source refers to a View Source which is not referenced by the parent View.");
				throw new SAFRNotFoundException(msg);
			}
			// CQ9628 do the same check for View Column
			ViewColumn vc = getViewColumn();
			if (vc == null) {
				String msg = "ViewColumn [" + viewColumnId
						+ "] for ViewColumnSource [" + this.getId()
						+ "] does not exist in View [" + this.getView().getId()
						+ "].";
				logger.log(
						Level.SEVERE,
						msg
								+ SAFRUtilities.LINEBREAK + "A View Column Source refers to a View Column which is not referenced by the parent View.");
				throw new SAFRNotFoundException(msg);
			}
		}		
		
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		ViewColumnSourceTransfer trans = (ViewColumnSourceTransfer) safrTrans;

		loadWarnings = new ArrayList<String>();

		this.viewColumnId = trans.getViewColumnId();
		if (viewColumn != null && viewColumn.getId() != trans.getViewColumnId()) {
			this.viewColumn = null;
		}
		this.viewSourceId = trans.getViewSourceId();
		if (viewSource != null && viewSource.getId() != trans.getViewSourceId()) {
			this.viewSource = null;
		}
		try {
			this.sourceType = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.COLSRCTYPE).getCode(trans.getSourceTypeId()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add(" does not have a valid source type. Please select a valid source type before saving.");
			this.sourceType = null;
		}

		// CQ 8056. Nikita. 08/07/2010. Separate properties for Lookup Field and
		// Source LR Field to enable resetting of appropriate values in UI on
		// changing the source type
		if (this.sourceType != null) {
			if (this.sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD ||
			    this.sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
				this.lrFieldId = trans.getSourceLRFieldId();
				if (lrField != null
						&& lrField.getId() != trans.getSourceLRFieldId()) {
					this.lrField = null;
				}
			} 
		}
		this.sortKeyTitleLRFieldId = trans.getSortKeyTitleLRFieldId();
		if (sortKeyTitleLRField != null
				&& sortKeyTitleLRField.getId() != trans
						.getSortKeyTitleLRFieldId()) {
			this.sortKeyTitleLRField = null;
		}
		this.lookupPathId = trans.getLookupPathId();
		if (lookupQueryBean != null
				&& lookupQueryBean.getId() != trans.getLookupPathId()) {
			this.lookupQueryBean = null;
		}
		this.sourceValue = trans.getSourceValue();
		try {
			this.effectiveDateTypeCode = ModelUtilities.getCodeFromKey(
					CodeCategories.RELPERIOD, trans.getEffectiveDateTypeCode());
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add(" does not have a valid effective date type. Please select a valid effective date type, if required, before saving.");
			this.effectiveDateTypeCode = null;
		}
		this.effectiveDateValue = trans.getEffectiveDateValue();
		this.effectiveDateLRFieldId = trans.getEffectiveDateLRFieldId();
		if (effectiveDateLRField != null
				&& effectiveDateLRField.getId() != trans
						.getEffectiveDateLRFieldId()) {
			this.effectiveDateLRField = null;
		}
		this.sortKeyTitleLookupPathId = trans.getSortKeyTitleLookupPathId();
		if (sortKeyTitleLookupPathQueryBean != null
				&& sortKeyTitleLookupPathQueryBean.getId() != trans
						.getSortKeyTitleLookupPathId()) {
			this.sortKeyTitleLookupPathQueryBean = null;
		}
		this.extractColumnAssignment = trans.getExtractColumnLogic();
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		ViewColumnSourceTransfer trans = (ViewColumnSourceTransfer) safrTrans;
		trans.setViewId(view.getId());
		trans.setViewColumnId(getViewColumn().getId());
		trans.setViewSourceId(getViewSource().getId());
		trans.setSourceTypeId(this.sourceType.getGeneralId());
		trans.setLookupPathId(this.lookupPathId);
		if (this.sourceType.getGeneralId() == Codes.SOURCE_FILE_FIELD ||
		    this.sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
			trans.setSourceLRFieldId(this.lrFieldId);
		} 
		trans.setSourceValue(this.sourceValue);
		trans.setSortKeyTitleLRFieldId(this.sortKeyTitleLRFieldId);
		trans.setEffectiveDateTypeCode(effectiveDateTypeCode == null ? null
				: effectiveDateTypeCode.getKey());
		trans.setEffectiveDateValue(this.effectiveDateValue);
		trans.setEffectiveDateLRFieldId(this.effectiveDateLRFieldId);
		trans.setSortKeyTitleLookupPathId(this.sortKeyTitleLookupPathId);
		trans.setExtractColumnLogic(extractColumnAssignment);
	}

	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * @return the viewColumn
	 */
	public ViewColumn getViewColumn() {
		if (viewColumn == null) {
			List<ViewColumn> vcs = view.getViewColumns().getActiveItems();
			for (ViewColumn vc : vcs) {
				if (vc.getId().equals(viewColumnId)) {
					this.viewColumn = vc;
					break;
				}
			}
		}
		return viewColumn;
	}

	/**
	 * @return the viewSource
	 */
	public ViewSource getViewSource() {
		if (viewSource == null) {
			List<ViewSource> vss = view.getViewSources().getActiveItems();
			for (ViewSource vs : vss) {
				if (vs.getId().equals(viewSourceId)) {
					this.viewSource = vs;
					break;
				}
			}
		}
		return viewSource;
	}

	/**
	 * @return the sourceType
	 */
	public Code getSourceType() {
		return sourceType;
	}

	/**
	 * @param sourceType
	 *            the sourceTypeCode to set
	 * @throws NullPointerException
	 *             if the parameter is null
	 */
	public void setSourceType(Code sourceType) {
		if (sourceType == null) {
			throw new NullPointerException(
					"Column Source Type code cannot be null.");
		}
		this.sourceType = sourceType;

		// Default LT comment created by CQ4748 removed by CQ10096.
		// Now set to empty string.
		this.extractColumnAssignment = "";

		this.sourceValue = null;
		this.logicalRecordQueryBean = null;
		this.lookupPathId = null;
		this.lookupQueryBean = null;
		this.lrFieldId = null;
		this.lrField = null;
		this.effectiveDateTypeCode = null;
		this.effectiveDateValue = null;
		this.effectiveDateLRFieldId = null;
		this.effectiveDateLRField = null;

		if (sourceType.getGeneralId() == Codes.LOOKUP_FIELD) {
			effectiveDateTypeCode = SAFRApplication.getSAFRFactory()
					.getCodeSet(CodeCategories.RELPERIOD).getCode(
							Codes.RELPERIOD_RUNDATE);
			effectiveDateValue = "Runday()";
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the id of the LrField used in the sort key title.
	 */
	public Integer getSortKeyTitleLRFieldId() {
		return sortKeyTitleLRFieldId;
	}

	/**
	 * @return the id of the Lookup Path used in the sort key title.
	 */
	public Integer getSortKeyTitleLookupPathId() {
		return sortKeyTitleLookupPathId;
	}

    public void setSortKeyTitleLookupPathId(Integer sortKeyTitleLookupPathId) {
        this.sortKeyTitleLookupPathId = sortKeyTitleLookupPathId;
    }

	/**
	 * @return the sourceLRField
	 * @throws SAFRException
	 *             if the source field is not found.
	 */
	public LRField getLRField() throws SAFRException {
		if (lrField == null) {
			if (lrFieldId != null && lrFieldId > 0) {
			    SAFRFactory factory = SAFRApplication.getSAFRFactory();
				this.lrField = factory.getLRField(lrFieldId, getEnvironmentId(), false);
			}
		}
		return lrField;
	}

    public Integer getLRFieldID() {
        return lrFieldId;
    }

	/**
	 * @param sourceLRField
	 *            the sourceLRField to set
	 * @throws SAFRException
	 */
	public void setLRFieldColumn(LRField sourceLRField) throws SAFRException {
		this.lrField = sourceLRField;
		if (sourceLRField == null) {
			this.lrFieldId = null;
		} else {
			this.lrFieldId = sourceLRField.getId();
			copyValuesIntoViewColumn(sourceLRField);
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

    /**
     * @param sourceLRField
     *            the sourceLRField to set
     * @throws SAFRException
     */
    public void setLRField(LRField sourceLRField) throws SAFRException {
        this.lrField = sourceLRField;
        if (sourceLRField == null) {
            this.lrFieldId = null;
        } else {
            this.lrFieldId = sourceLRField.getId();
        }
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }

	
    /**
     * @param sourceLRField
     *            the sourceLRField to set
     * @throws SAFRException
     */
    public void setLRFieldPaste(LRField sourceLRField) throws SAFRException {
        this.lrField = sourceLRField;
        if (sourceLRField == null) {
            this.lrFieldId = null;
        } else {
            this.lrFieldId = sourceLRField.getId();
        }
        markModified();
        view.makeViewInactive();
        view.markUpdated();
    }
	
	public Integer getLookupPathId() {
		return lookupPathId;
	}

    public void copyFromViewColumnSource(ViewColumnSource vcs) {        
        this.sourceType = vcs.sourceType;
        this.sourceValue = vcs.sourceValue;
        this.lrFieldId = vcs.lrFieldId;
        this.lrField = vcs.lrField;
        this.sortKeyTitleLRFieldId = vcs.sortKeyTitleLRFieldId;
        this.sortKeyTitleLRField = vcs.sortKeyTitleLRField;
        this.lookupPathId = vcs.lookupPathId;
        this.lookupQueryBean = vcs.lookupQueryBean;
        this.effectiveDateTypeCode = vcs.effectiveDateTypeCode;
        this.effectiveDateValue = vcs.effectiveDateValue;
        this.effectiveDateLRField = vcs.effectiveDateLRField;
        this.effectiveDateLRFieldId = vcs.effectiveDateLRFieldId;
        this.logicalRecordQueryBean = vcs.logicalRecordQueryBean;
        this.sortKeyTitleLookupPathId = vcs.sortKeyTitleLookupPathId;
        this.sortKeyTitleLookupPathQueryBean = vcs.sortKeyTitleLookupPathQueryBean;
        this.extractColumnAssignment = vcs.extractColumnAssignment;
    }
	
	private void copyValuesIntoViewColumn(LRField field) {
		// copying values of Column Source into the View Column
		if (field.getHeading1() == null || field.getHeading1().equals("")) {
			getViewColumn().setHeading1(field.getName());
		} else {
			getViewColumn().setHeading1(field.getHeading1());
		}
		getViewColumn().setHeading2(field.getHeading2());
		getViewColumn().setHeading3(field.getHeading3());
		getViewColumn().setDateTimeFormatCode(field.getDateTimeFormatCode());
		getViewColumn().setLength(field.getLength());
		getViewColumn().setHeaderAlignmentCode(field.getHeaderAlignmentCode());
		getViewColumn().setDecimals(field.getDecimals());
		getViewColumn().setScaling(field.getScaling());
		getViewColumn().setSigned(field.isSigned());
		getViewColumn().setNumericMaskCode(field.getNumericMaskCode());
		getViewColumn().setSortkeyFooterLabel(field.getSubtotalLabel());
		getViewColumn().setDataTypeCode(field.getDataTypeCode());
	}

	/**
	 * @return the sortKeyTitleLRField
	 * @throws SAFRException
	 */
	public LRField getSortKeyTitleLRField() throws SAFRException {
		if (sortKeyTitleLRField == null) {
			if (sortKeyTitleLRFieldId != null && sortKeyTitleLRFieldId > 0) {
				if (isForImport()) {
					// this is called from import, use the model provider to get
					// the field.
					LRFieldTransfer trans = (LRFieldTransfer) provider.get(
							LRFieldTransfer.class, sortKeyTitleLRFieldId);
					this.sortKeyTitleLRField = SAFRApplication.getSAFRFactory()
							.initLRField(trans);
				} else {
					this.sortKeyTitleLRField = SAFRApplication.getSAFRFactory()
							.getLRField(sortKeyTitleLRFieldId, this.getEnvironmentId(), false);
				}
			}
		}
		return sortKeyTitleLRField;
	}

	/**
	 * @param sortKeyTitleLRField
	 *            the sortKeyTitleLRField to set
	 */
	public void setSortKeyTitleLRField(LRField sortKeyTitleLRField) {
		this.sortKeyTitleLRField = sortKeyTitleLRField;
		if (sortKeyTitleLRField == null) {
			this.sortKeyTitleLRFieldId = null;
			this.sortKeyTitleLogicalRecordQueryBean = null;
		} else {
			this.sortKeyTitleLRFieldId = sortKeyTitleLRField.getId();
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

    public void setSortKeyTitleLRFieldId(Integer id) {
        sortKeyTitleLRFieldId = id;
    }
	
	/**
	 * @return the sourceValue
	 */
	public String getSourceValue() {
		return sourceValue;
	}

	/**
	 * @param sourceValue
	 *            the sourceValue to set
	 */
	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the effectiveDateTypeCode
	 */
	public Code getEffectiveDateTypeCode() {
		return effectiveDateTypeCode;
	}

	/**
	 * @param effectiveDateTypeCode
	 *            the effectiveDateTypeCode to set
	 */
	public void setEffectiveDateTypeCode(Code effectiveDateTypeCode) {
		this.effectiveDateTypeCode = effectiveDateTypeCode;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the effectiveDateValue
	 */
	public String getEffectiveDateValue() {
		return effectiveDateValue;
	}

	/**
	 * @param effectiveDateValue
	 *            the effectiveDateValue to set
	 */
	public void setEffectiveDateValue(String effectiveDateValue) {
		this.effectiveDateValue = effectiveDateValue;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the effectiveDateLRField
	 * @throws SAFRException
	 */
	public LRField getEffectiveDateLRField() throws SAFRException {
		if (effectiveDateLRField == null) {
			if (effectiveDateLRFieldId != null && effectiveDateLRFieldId > 0) {
				this.effectiveDateLRField = SAFRApplication.getSAFRFactory()
						.getLRField(effectiveDateLRFieldId, getEnvironmentId(), false);
			}
		}
		return effectiveDateLRField;
	}

	/**
	 * @param effectiveDateLRField
	 *            the effectiveDateLRField to set
	 */
	public void setEffectiveDateLRField(LRField effectiveDateLRField) {
		this.effectiveDateLRField = effectiveDateLRField;
		effectiveDateLRFieldId = effectiveDateLRField.getId();
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * Returns the logic text for the ViewColumnSource's Extract Column
	 * Assignment.
	 * 
	 * @return a String of logic text
	 */
	public String getExtractColumnAssignment() {
		return extractColumnAssignment;
	}

    public void setExtractColumnAssignmentBasic(String extractColumnAssignment) {
        this.extractColumnAssignment = extractColumnAssignment;        
        markModified();
    }
    
	/**
	 * Sets the logic text for the ViewColumnSource's Extract Column Assignment.
	 * 
	 * @param extractColumnAssignment
	 *            a String of logic text
	 */
	public void setExtractColumnAssignment(String extractColumnAssignment) {
		//TC18596 only modify if value has changed
		if ((this.extractColumnAssignment == null && extractColumnAssignment != null)
				|| (this.extractColumnAssignment != null && !this.extractColumnAssignment
						.equals(extractColumnAssignment))) {
		    this.setExtractColumnAssignmentBasic(extractColumnAssignment);
			markModified();
			view.makeViewInactive();
	        view.markUpdated();
		}
	}

	void setCompiledExtractColumnAssignment(byte[] compiledExtractColumnAssignment) {
		this.compiledExtractColumnAssignment = compiledExtractColumnAssignment;
	}

	public byte[] getCompiledExtractColumnAssignment() {
		return compiledExtractColumnAssignment;
	}

	/**
	 * @return the Logical Record Query bean.
	 * @throws DAOException
	 */
	public LogicalRecordQueryBean getLogicalRecordQueryBean()
			throws DAOException {
		if (logicalRecordQueryBean == null) {
			if (lrFieldId != null && lrFieldId > 0) {
				logicalRecordQueryBean = SAFRQuery.queryLogicalRecordByField(
				    lrFieldId, getEnvironmentId());
			}
		}
		return logicalRecordQueryBean;
	}

	/**
	 * This method is used to set the Logical Record Query Bean.
	 * 
	 * @param logicalRecordQueryBean
	 *            : the Logical Record Query bean which is to be set.
	 * @throws DAOException
	 */
	public void setLogicalRecordQueryBean(
			LogicalRecordQueryBean logicalRecordQueryBean) {
		this.logicalRecordQueryBean = logicalRecordQueryBean;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the lookup query bean.
	 * @throws DAOException
	 */
	public LookupQueryBean getLookupQueryBean() throws DAOException {
		if (lookupQueryBean == null) {
			if (lookupPathId != null && lookupPathId > 0) {
				lookupQueryBean = SAFRQuery.queryLookupPath(lookupPathId,
						getEnvironmentId());
			}
		}
		return lookupQueryBean;
	}

	/**
	 * This method is used to set the lookup Query bean.
	 * 
	 * @param lookupQueryBean
	 *            : the query bean which is to be set.
	 */
	public void setLookupQueryBean(LookupQueryBean lookupQueryBean) {
		this.lookupQueryBean = lookupQueryBean;
		if (lookupQueryBean == null) {
			this.lookupPathId = null;
		} else {
			this.lookupPathId = lookupQueryBean.getId();
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * @return the Lookup Path query bean of the sort key title.
	 * @throws DAOException
	 */
	public LookupQueryBean getSortKeyTitleLookupPathQueryBean()
			throws DAOException {
		if (sortKeyTitleLookupPathQueryBean == null) {
			if (sortKeyTitleLookupPathId != null
					&& sortKeyTitleLookupPathId > 0) {
				sortKeyTitleLookupPathQueryBean = SAFRQuery.queryLookupPath(
						sortKeyTitleLookupPathId, getEnvironmentId());
			}
		}
		return sortKeyTitleLookupPathQueryBean;
	}

	/**
	 * This method is used to set the Lookup Path query bean of the sort key
	 * title.
	 * 
	 * @param sortKeyTitleLookupPathQueryBean
	 */
	public void setSortKeyTitleLookupPathQueryBean(
			LookupQueryBean sortKeyTitleLookupPathQueryBean) {
		this.sortKeyTitleLookupPathQueryBean = sortKeyTitleLookupPathQueryBean;
		if (sortKeyTitleLookupPathQueryBean == null) {
			this.sortKeyTitleLookupPathId = null;
		} else {
			this.sortKeyTitleLookupPathId = sortKeyTitleLookupPathQueryBean
					.getId();
		}
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	/**
	 * Gets the Logical record query bean containing the sort key title field.
	 * 
	 * @return the logical record query bean or null if no sort key title field
	 *         exist.
	 * @throws DAOException
	 */
	public LogicalRecordQueryBean getSortKeyTitleLogicalRecordQueryBean()
			throws DAOException {
		if (sortKeyTitleLogicalRecordQueryBean == null) {
			if (sortKeyTitleLRFieldId != null && sortKeyTitleLRFieldId > 0) {
				sortKeyTitleLogicalRecordQueryBean = SAFRQuery
						.queryLogicalRecordByField(sortKeyTitleLRFieldId,
								getEnvironmentId());
			}
		}
		return sortKeyTitleLogicalRecordQueryBean;
	}

	public void setSortKeyTitleLogicalRecordQueryBean(
			LogicalRecordQueryBean sortKeyTitleLogicalRecordQueryBean) {
		this.sortKeyTitleLogicalRecordQueryBean = sortKeyTitleLogicalRecordQueryBean;
		markModified();
		view.makeViewInactive();
        view.markUpdated();
	}

	@Override
	public void store() throws SAFRException, DAOException {
		// TODO Auto-generated method stub

	}

	/**
	 * Validate Extract Column Assignment using this View.
	 * 
	 * @param logicText
	 *            The format record filter logic text to be validated.
	 * @throws DAOException
	 * @throws SAFRException
	 *             SAFRValidation exception will be thrown with a list of
	 *             validation errors.
	 */
	public void validateExtractColumnAssignment(String logicText)
			throws DAOException, SAFRException {
	    CompilerFactory.checkSyntax(LogicTextType.Extract_Column_Assignment,
				logicText, view, getViewSource(), getViewColumn());
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
		// TODO Auto-generated method stub
		return null;
	}

	List<String> getLoadWarnings() {
		return loadWarnings;
	}

	public void setModelTransferProvider(ModelTransferProvider provider) {
		this.provider = provider;
	}

	public void validate() throws SAFRValidationException {
		SAFRValidationException safrValidationException = new SAFRValidationException();
		
		if (this.getSourceType() == null) {
			safrValidationException.setErrorMessage(Property.COLUMN_SOURCE,
					"View Column Source for Column No. "
							+ this.getViewColumn().getColumnNo()
							+ " and Source Sequence No. "
							+ this.getViewSource().getSequenceNo()
							+ " does not have a valid Source Type.");
			throw safrValidationException;
		}
	}

}
