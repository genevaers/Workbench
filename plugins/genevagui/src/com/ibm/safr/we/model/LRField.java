package com.ibm.safr.we.model;

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

import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.utilities.importer.ModelTransferProvider;

public class LRField extends SAFRField {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.LRField");
    
	private Integer logicalRecordId;
	private LogicalRecord logicalRecord;
	private LRFieldKeyType keyType;
	private Integer pkeySeqNo;
	private Integer position;
	Integer redefine;
	private Integer ordinalPosition;
	private Integer ordinalOffset;
	private String databaseColumnName;

    private List<String> loadWarnings;
	private ModelTransferProvider provider;
    public static String[] EffectiveDate = { "", "Start Date", "End Date" };

	public LRField(LogicalRecord parentLR) throws SAFRException {
		super(parentLR.getEnvironment());
		this.logicalRecord = parentLR;
		this.logicalRecordId = parentLR.getId();
		initializeVariables();
	}

	LRField(LogicalRecord parentLR, LRFieldTransfer trans) throws SAFRException {
		super(trans);
		this.logicalRecord = parentLR;
	}

	// This ctor is needed to support Views, where the parent LogicalRecord
	// is not yet instantiated.
	public LRField(LRFieldTransfer trans) throws SAFRException {
		super(trans);
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		LRFieldTransfer trans = (LRFieldTransfer) safrTrans;

		loadWarnings = new ArrayList<String>();

		// Set this object from the transfer object
		this.logicalRecordId = trans.getLrId();
		if (logicalRecord != null && logicalRecord.getId() != trans.getLrId()) {
			this.logicalRecord = null;
		}
		this.position = trans.getFixedStartPos();
		this.redefine = trans.getRedefine();
		this.ordinalPosition = trans.getOrdinalPos();
		this.ordinalOffset = trans.getOrdinalOffset();
		this.databaseColumnName = trans.getDbmsColName();

		// A field may be used as an effective start date key or an effective
		// end date key or a primary key component or none of these.
		if (trans.isEffStartDate()) {
			this.keyType = LRFieldKeyType.EFFSTARTDATE;
			this.pkeySeqNo = 0;
		} else if (trans.isEffEndDate()) {
			this.keyType = LRFieldKeyType.EFFENDDATE;
			this.pkeySeqNo = 0;
		} else if (trans.getPkeySeqNo() > 0) {
			this.keyType = LRFieldKeyType.PRIMARYKEY;
			this.pkeySeqNo = trans.getPkeySeqNo();
		} else {
			this.keyType = LRFieldKeyType.NONE;
			this.pkeySeqNo = 0;
		}

		// Get load warnings, if any, from super class.
		if (!super.getLoadWarningProperties().isEmpty()) {
			for (SAFRField.Property property : super.getLoadWarningProperties()) {
				loadWarnings.add("LR Field '" + this.getName()
						+ "' does not have a valid " + property.getText()
						+ ". Please select a valid " + property.getText()
						+ ", if required, before saving.");
			}
		}
	}

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		LRFieldTransfer trans = (LRFieldTransfer) safrTrans;

		// Set the transfer object from this object
		if (this.logicalRecord != null) {
			trans.setLrId(logicalRecord.getId());
		} else {
			trans.setLrId(this.logicalRecordId);
		}
		trans.setFixedStartPos(this.position);
		trans.setRedefine(this.redefine);
		trans.setOrdinalPos(this.ordinalPosition);
		trans.setOrdinalOffset(this.ordinalOffset);
		trans.setDbmsColName(this.databaseColumnName);

		// A field may be used as an effective start date key or an effective
		// end date key or a primary key component or none of these.
		if (this.keyType == LRFieldKeyType.EFFSTARTDATE) {
			trans.setEffStartDate(true);
			trans.setEffEndDate(false);
			trans.setPkeySeqNo(0);
		} else if (this.keyType == LRFieldKeyType.EFFENDDATE) {
			trans.setEffStartDate(false);
			trans.setEffEndDate(true);
			trans.setPkeySeqNo(0);
		} else if (this.keyType == LRFieldKeyType.PRIMARYKEY) {
			trans.setEffStartDate(false);
			trans.setEffEndDate(false);
			trans.setPkeySeqNo(pkeySeqNo);
		} else {
			trans.setEffStartDate(false);
			trans.setEffEndDate(false);
			trans.setPkeySeqNo(0);
		}
	}

	/**
	 * @return the parent logicalRecord
	 */
	public LogicalRecord getLogicalRecord() throws SAFRException {
		if (logicalRecord == null) {
			if (logicalRecordId != null) {
				if (isForImport()) {
					// this is called from import, use the model provider to get
					// the LR
					LogicalRecordTransfer trans = (LogicalRecordTransfer) provider
							.get(LogicalRecordTransfer.class, logicalRecordId);
					this.logicalRecord = SAFRApplication.getSAFRFactory()
							.initLogicalRecord(trans);
				} else {
					this.logicalRecord = SAFRApplication.getSAFRFactory()
							.getLogicalRecord(logicalRecordId, getEnvironmentId());
				}
			}
		}
		return logicalRecord;
	}

    public String getEffectiveDateString() {
        if (getKeyType().equals(LRFieldKeyType.EFFSTARTDATE)) {
            return LRField.EffectiveDate[1];
        } else if (getKeyType().equals(LRFieldKeyType.EFFENDDATE)) {
            return LRField.EffectiveDate[2];
        } else {
            return LRField.EffectiveDate[0];
        }        
    }
	
	/**
	 * Indicates if this field represents some key property of the logical
	 * record. Possible key properties are primary key, effective start date or
	 * effective end date. Key types are defined by LRFieldKeyType as
	 * (PRIMARYKEY, EFFSTARTDATE, EFFENDDATE, NONE).
	 * 
	 * @return the LRFieldKeyType object
	 */
	public LRFieldKeyType getKeyType() {
		return keyType;
	}

	/**
	 * This method is used to set the Key Type. If the key type is not primary
	 * key then primary key sequence number is set to zero.
	 * 
	 * @param keyType
	 *            the LRFieldKeyType to set
	 */
	public void setKeyType(LRFieldKeyType keyType) {
		if (this.keyType == LRFieldKeyType.PRIMARYKEY
				|| keyType == LRFieldKeyType.PRIMARYKEY) {
			// check lookup dependencies while saving, if the Primary key is
			// modified.
			logicalRecord.setCheckLookupDependencies(true);
		}
		this.keyType = keyType;
		if (keyType != LRFieldKeyType.PRIMARYKEY) {
			pkeySeqNo = 0;
			// if the key type is set to an effective dated one, then reset keys
			// of other fields.
			if (keyType != LRFieldKeyType.NONE) {
				logicalRecord.resetIndexKeys(this, keyType);
			}
		}
        logicalRecord.markUpdated();
		markModified();
	}

	/**
	 * Returns this field's sequence no. within the record's primary key.
	 * Returns zero if the field is not part of the primary key.
	 * 
	 * @return the pkeySeqNo
	 * @see getKeyType
	 */
	public Integer getPkeySeqNo() {
		if (keyType == LRFieldKeyType.PRIMARYKEY) {
			return pkeySeqNo;
		} else {
			return new Integer(0);
		}
	}

	/**
	 * @param pkeySeqNo
	 *            the pkeySeqNo to set
	 */
	public void setPkeySeqNo(Integer pkeySeqNo) {
		this.pkeySeqNo = pkeySeqNo;
		logicalRecord.setCheckLookupDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setDataTypeCode(Code dataType) {
		super.setDataTypeCode(dataType);
		logicalRecord.setCheckLookupDependencies(true);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	/**
	 * @return the fixedStartPosition
	 */
	public Integer getPosition() {
		return position;
	}

	/**
	 * @param fixedStartPosition
	 *            the fixedStartPosition to set
	 */
	public void setPosition(Integer fixedStartPosition) {
	    boolean blankPos = position == null ? true : false;  
		this.position = fixedStartPosition;
		logicalRecord.setCheckLookupDependencies(true);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
		if (blankPos) {
		    logicalRecord.autocalcRedefine();
		}
		else {
		    autocalcRedefine();
		}
	}

    public void setPositionSimple(Integer fixedStartPosition) {
        this.position = fixedStartPosition;
    }

    public boolean autocalcRedefine() {
        List<LRField> flds = logicalRecord.getLRFields().getActiveItems();
        int index = flds.indexOf(this);
        
        // find previous field
        LRField prevField = null;
        if (flds.size()>1 && index>0) {
            prevField = flds.get(index-1);            
        }
        
        return autocalcRedefineField(prevField);
    }
    
	public boolean autocalcRedefineField(LRField prevField) {
        boolean changed;
        Integer curRedefine = redefine;
        autocalcRedefineR(prevField);
        if ( curRedefine == null || redefine == null ) {
            if ( (curRedefine == null || curRedefine.equals(0)) &&
                 (redefine == null || redefine.equals(0))) {
                changed = false;                            
            }
            else {
                changed = true;                                            
            }
        }
        else if (curRedefine.equals(redefine)) {
            changed = false;
        }
        else {
            changed = true;
        }
        return changed;
    }

    private void autocalcRedefineR(LRField pField) {
        if (pField == null) {
            redefine = 0;
            markModified();
        }
        else if (getPosition() == null || pField.getPosition() == null) {
            redefine = -1;
            markModified();
        }
        else {
            int pend = pField.getPosition() + pField.getLength();
            int cend = getPosition() + getLength();
            if (getPosition() >= pField.getPosition() &&
                cend <= pend) {
                redefine = pField.getId();
                markModified();
            }
            else {
                LRField npField = logicalRecord.findLRField(pField.getRedefine());
                autocalcRedefineR(npField);
            }
        }
    }
    
    public boolean correctRedefine(Integer redefine) {
        List<LRField> flds = logicalRecord.getLRFields().getActiveItems();
        int idx = flds.indexOf(this);
        LRField nextFld = null;
        if (idx + 1 < flds.size()) {
            nextFld = flds.get(idx+1);
        }
        else {
            return true;
        }
        if (nextFld.isRedefineSet()) {
            LRField nextFldParent = logicalRecord.findLRField(nextFld.getRedefine());
            // check for between
            if (nextFldParent.getOrdinalPosition() < this.getOrdinalPosition()) {
                Integer possLevel = getLevelFromRedefine(redefine);
                Integer nextLevel = nextFld.getLevel();
                
                if (nextLevel < possLevel) {
                    return true;
                }
                else if (
                    nextLevel == possLevel &&
                    nextFld.getRedefine().equals(redefine)) {
                    return true;                    
                }
                else {
                    return false;
                }
            }
            else {
                return true;
            }
        }
        else {
            return true;
        }
    }

    public void setLengthSimple(Integer length) {
        super.setLength(length);
    }
    
    @Override
	public void setLength(Integer length) {
		super.setLength(length);
		logicalRecord.setCheckLookupDependencies(true);
		logicalRecord.setCheckViewDependencies(true);		
        logicalRecord.markUpdated();
		markModified();
		autocalcRedefine();		
	}

	@Override
	public void setDateTimeFormatCode(Code dateTimeFormat) {
		super.setDateTimeFormatCode(dateTimeFormat);
		logicalRecord.setCheckLookupDependencies(true);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setSigned(Boolean signed) {
		super.setSigned(signed);
		logicalRecord.setCheckLookupDependencies(true);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setDecimals(Integer decimals) {
		super.setDecimals(decimals);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		super.setDefaultValue(defaultValue);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		logicalRecord.setCheckViewDependencies(true);
		logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setScaling(Integer scaling) {
		super.setScaling(scaling);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	@Override
	public void setNumericMaskCode(Code numericMask) {
		super.setNumericMaskCode(numericMask);
		logicalRecord.setCheckViewDependencies(true);
        logicalRecord.markUpdated();
		markModified();
	}

	/**
	 * @return the redefines
	 */
	public Integer getRedefine() {
		return redefine;
	}

    public boolean isRedefineSet() {
        return (redefine != null && redefine != 0 && redefine != -1);
    }	
	
    /**
     * @return the level
     */
    public Integer getLevel() {
        Integer parent = getRedefine();
        return getLevelFromRedefine(parent);
    }

    public Integer getLevelFromRedefine(Integer parent) {
        int level = 1;
        if (parent != null && parent == -1) {
            return null;
        }
        while (parent != null && parent != 0 && parent != -1) {
            LRField parFld = logicalRecord.findLRField(parent);
            parent = parFld.getRedefine();
            level++;
        }
        return level;
    }

    /**
     * @return is descendent
     */
    public boolean isDescendant(LRField chkFld) {
        Integer parent = getRedefine();
        List<LRField> flds = logicalRecord.getLRFields().getActiveItems();        
        while (parent != null && parent != 0 && parent != -1) {
            LRField parFld = null;
            for (LRField fld : flds) {
                if (fld.getId().equals(parent)) {
                    parFld = fld;
                    break;
                }
            }
            if (parFld == null) {
                return false;
            }
            else if (parFld.getId().equals(chkFld.getId())) {
                return true;
            }
            parent = parFld.getRedefine();
        }
        return false;
    }

    /**
     * @return get parent
     */
    public LRField getParent() {
        LRField parFld = null;
        Integer parent = getRedefine(); 
        if (parent != null && parent != 0 && parent != -1) {
            parFld = logicalRecord.findLRField(parent);
        }
        return parFld;
    }
    
    /**
     * @return get ancestor
     */
    public LRField getAncestor() {
        LRField parFld = this;
        Integer parent = getRedefine(); 
        while (parent != null && parent != 0 && parent != -1) {
            parFld = logicalRecord.findLRField(parent);
            parent = parFld.getRedefine();
        }
        return parFld;
    }

    /**
     * @return get previous field
     */
    public LRField getPreviousField() {
        List<LRField> items = logicalRecord.getLRFields().getActiveItems();
        if (this.getOrdinalPosition() <= 1) {
            return null;            
        }
        else {
            return items.get(this.getOrdinalPosition()-2);
        }
    }

    /**
     * @return get previous field of a certain level
     */
    public LRField getPreviousFieldOfLevel(int level) {
        List<LRField> items = logicalRecord.getLRFields().getActiveItems();
        LRField prev = null;
        for (int i=this.getOrdinalPosition()-2 ; i>=0 ; i++) {
            LRField curr = items.get(i);
            if (curr.getLevel().equals(level)) {
                prev = curr;
                break;
            }
        }
        return prev;
    }
    
    /**
     * @return get ancestor
     */
    public LRField getNextField() {
        List<LRField> items = logicalRecord.getLRFields().getActiveItems();
        if (this.getOrdinalPosition() >= items.size()) {
            return null;            
        }
        else {
            return items.get(this.getOrdinalPosition());
        }
    }
    
    /**
     * @return get list of parents going back to the ancestor
     */
    public List<LRField> getFamilyLine() {
        List<LRField> ancs = new ArrayList<LRField>();
        ancs.add(this);
        LRField parFld = this;        
        Integer parent = getRedefine(); 
        while (parent != null && parent != 0 && parent != -1) {
            parFld = logicalRecord.findLRField(parent);
            ancs.add(0, parFld);
            parent = parFld.getRedefine();
        }
        return ancs;
    }

    public List<LRField> getChildren() {
        List<LRField> flds = logicalRecord.getLRFields().getActiveItems();        
        List<LRField> children = new ArrayList<LRField>();
        for (LRField fld : flds) {
            if (fld.getRedefine() != null && 
                fld.getRedefine().equals(getId())) {
                children.add(fld);
            }
        }
        return children;
    }
    
	/**
	 * @param redefines
	 *            the redefines to set
	 * @throws SAFRValidationException 
	 */
	public void setRedefine(Integer redefine) {
	    if (redefine != null) {
            LRField redefineFld = logicalRecord.findLRField(redefine);
            if (redefineFld != null) {
                // check for redefine loops
                if (redefine.equals(getId()) || redefineFld.isDescendant(this)) {
    			    logger.log(Level.SEVERE, "Redefines must not loop");
                }
            }
	    }	 
        this.redefine = redefine;
        logicalRecord.markUpdated();
        markModified();
	}

	/**
	 * @return the ordinalPosition
	 */
	public Integer getOrdinalPosition() {
		return ordinalPosition;
	}

	/**
	 * @param ordinalPosition
	 *            the ordinalPosition to set
	 */
	public void setOrdinalPosition(Integer ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
        logicalRecord.markUpdated();
		markModified();
	}

	/**
	 * @return the ordinalOffset
	 */
	public Integer getOrdinalOffset() {
		return ordinalOffset;
	}

	/**
	 * @param ordinalOffset
	 *            the ordinalOffset to set
	 */
	public void setOrdinalOffset(Integer ordinalOffset) {
		this.ordinalOffset = ordinalOffset;
        logicalRecord.markUpdated();
		markModified();
	}

	/**
	 * @return the databaseColumnName
	 */
	public String getDatabaseColumnName() {
		return databaseColumnName;
	}

	/**
	 * @param databaseColumnName
	 *            the databaseColumnName to set
	 */
	public void setDatabaseColumnName(String databaseColumnName) {
		this.databaseColumnName = databaseColumnName;
        logicalRecord.markUpdated();
		markModified();
	}

	private void initializeVariables() {
		keyType = LRFieldKeyType.NONE;
		pkeySeqNo = new Integer(0); // for LRFieldKeyType.PRIMARYKEY
		position = 1;
		redefine = -1;
	}

	public void store() throws SAFRException, DAOException {
		LRFieldTransfer trans = new LRFieldTransfer();
		setTransferData(trans);
		trans = DAOFactoryHolder.getDAOFactory().getLRFieldDAO()
				.persistLRField(trans);
		setObjectData(trans);

		SAFRValidationException safrValidationException = new SAFRValidationException();
		safrValidationException.setContextMessage("This logic should never be executed.");
		safrValidationException.setSafrValidationType(SAFRValidationType.ERROR);
			throw safrValidationException;
	}

	/**
	 * This enum maintains the properties of LR field.
	 * 
	 */
	public enum Property {
		NAME, COMMENT, FIELD_DATATYPE, DECIMAL_PLACES, FIXED_POSITION, EFFECTIVE_DATE, REDEFINE
	}

	public void validateError() throws SAFRException, DAOException {

		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();

		String name = getName() == null ? "" : getName();
		if (!name.equals("")) {

			if (name.length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME, 
				    getName() + " :The length of LR Field name cannot exceed 48 characters.");
			}
			if (!safrValidator.isNameValid(name)) {
				safrValidationException.setErrorMessage(Property.NAME, getName()
					+ " :The LR Field name should begin "
					+ "with a letter and should comprise of letters"
					+ ", numbers, pound sign (#) and underscores only.");
			}
			// SAFR keywords cannot be used in names eg. data,date.
			if (name.equalsIgnoreCase("data") || name.equalsIgnoreCase("date")) {
				safrValidationException.setErrorMessage(Property.NAME,
				    name + " :SAFR keywords mentioned below cannot be used as Field name: DATA, DATE.");
			}
		}
		if (safrValidationException.getErrorMessages().isEmpty() && (getComment() != null)
				&& (getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH)) {
			safrValidationException.setErrorMessage(Property.COMMENT, 
			    name + " :Comment cannot be more than 254 characters.");
		}
		
		if (!safrValidationException.getErrorMessages().isEmpty()) {
			safrValidationException.setSafrValidationType(SAFRValidationType.ERROR);
			throw safrValidationException;
		}
	}

	public void validateWarning() throws SAFRException, DAOException {

		SAFRValidationException safrValidationException = new SAFRValidationException();
		try {
			super.validate();
		} catch (SAFRValidationException sve) {
			ArrayList<String> errorMsgs = sve.getErrorMessages();
			ArrayList<String> modifiedErrorMsgs = new ArrayList<String>();
			for (String errorMsg : errorMsgs) {
				errorMsg = " : " + errorMsg;
				if (this.getName() != null) {
					errorMsg = this.getName() + errorMsg;
				}
				modifiedErrorMsgs.add(errorMsg);
			}
			if (!modifiedErrorMsgs.isEmpty()) {
				safrValidationException.setErrorMessages(Property.FIELD_DATATYPE, modifiedErrorMsgs);
			}

		}
		// LR Field name cannot be empty.
		String name = getName();
		if (name == null || name.equals("")) {
			safrValidationException.setErrorMessage(Property.NAME, 
			    " : LR Field name cannot be empty.");
		}
		// Decimal places value cannot be less than 0..
		if (getDecimals() < 0) {
			safrValidationException.setErrorMessage(Property.DECIMAL_PLACES, 
			    getName() + " : Decimal places value cannot be less than 0.");
		}

        if (position != null && position <= 0) {
			safrValidationException.setErrorMessage(Property.FIXED_POSITION, 
			    getName() + " : Fixed position can not be 0.");
		}
		// effective date start or end fields should have date/time format.
		if ((keyType == LRFieldKeyType.EFFSTARTDATE) || (keyType == LRFieldKeyType.EFFENDDATE)) {
			if (getDateTimeFormatCode() == null) {
				safrValidationException.setErrorMessage(Property.EFFECTIVE_DATE, 
				    getName() + " : Effective Date Start or End fields should have Date/Type format.");
			}
		}

        if (safrValidationException.getErrorMessages().isEmpty() && redefine != null && redefine == -1) {
            safrValidationException.setErrorMessage(Property.REDEFINE, 
                name + " :Level/Redefine has not been set, make sure length and position are correct if inside a redefine");
        }

        // fixed position can not be blank
        if (safrValidationException.getErrorMessages().isEmpty() && position == null) {
            safrValidationException.setErrorMessage(Property.FIXED_POSITION,
                  getName() + " : Fixed position can not be blank.");
        } 
        		
		if (!safrValidationException.getErrorMessages().isEmpty()) {
			safrValidationException.setSafrValidationType(SAFRValidationType.WARNING);
			throw safrValidationException;
		}
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException, SAFRException {
		return null;
	}

	List<String> getLoadWarnings() {
		return loadWarnings;
	}

	protected void setID(int id) {
	    this.id = id;
	}
	
    protected boolean isFieldSibling(LRField posSib) {
        Integer posSibParent = posSib.getRedefine();
        
        if (this.getId().equals(posSib.getId())) {
            return false;
        }
        else if (redefine == -1) {
            return false;
        }
        else if (redefine == null || redefine.equals(0)) {
            if (posSibParent == null || posSibParent.equals(0)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (redefine.equals(posSibParent)) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    public void setLogicalRecord(LogicalRecord lr) {
        this.logicalRecord = lr;        
    }
	
}
