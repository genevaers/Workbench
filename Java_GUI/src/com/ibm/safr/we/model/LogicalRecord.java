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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.LRFieldKeyType;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.LRFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexFieldTransfer;
import com.ibm.safr.we.data.transfer.LRIndexTransfer;
import com.ibm.safr.we.data.transfer.LogicalRecordTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRActivatedComponent;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.utilities.importer.LRInformationProvider;
import com.ibm.safr.we.model.utilities.importer.ModelTransferProvider;
import com.ibm.safr.we.model.view.ViewColumn;

public class LogicalRecord extends SAFRActivatedComponent {

    static transient Logger logger = Logger
    .getLogger("com.ibm.safr.we.model.LogicalRecord");
    
    class RedefineChange {
        
        public RedefineChange(Integer fldId, Integer redefineId) {
            super();
            this.fldId = fldId;
            this.redefineId = redefineId;
        }
        
        public Integer fldId;
        public Integer redefineId;
    }
    
	private Code lrTypeCode;
	private Code lrStatusCode;
	private Integer lookupExitId;
	private UserExitRoutine lookupExitRoutine;
	private String lookupExitParams;
	private SAFRList<LRField> lrFields = new SAFRList<LRField>();

	private boolean checkLookupDependencies;
	private boolean checkViewDependencies;
	private SAFRAssociationList<ComponentAssociation> logicalFileAssociations;

    private Set<Integer> migExLookupList = new HashSet<Integer>();
    private Set<Integer> migExViewList = new HashSet<Integer>();
	
    private Set<Integer> deactivatedLookupList = new HashSet<Integer>();
    private Set<Integer> deactivatedViewList = new HashSet<Integer>();

	private LRIndex lrIndex; // instance of inner class

	private List<String> loadWarnings;

	private ModelTransferProvider provider;

	LogicalRecord(Integer environmentId) {
		super(environmentId);
		logicalFileAssociations = new SAFRAssociationList<ComponentAssociation>();
	}

	LogicalRecord(LogicalRecordTransfer trans) throws SAFRException, DAOException {
		super(trans);
		logicalFileAssociations = new SAFRAssociationList<ComponentAssociation>();
		this.lrFields.addAll(new SAFRList<LRField>());
		if (!trans.isForImport()) {
			// Call the factory to get the list of LR fields
			logicalFileAssociations = SAFRAssociationFactory
			    .getLogicalRecordToLogicalFileAssociations(this);
			this.lrFields.addAll(SAFRApplication.getSAFRFactory().getLRFields(this));

			// Append the load warnings of the LR Fields to the loadWarnings
			// list of
			// Logical Record
			for (LRField field : this.lrFields.getActiveItems()) {
				if (!field.getLoadWarnings().isEmpty()) {
					this.loadWarnings.addAll(field.getLoadWarnings());
				}
			}

			// Get the LR index if it exists
			LRIndexTransfer lrIndexTrans = null;
			try {
				lrIndexTrans = DAOFactoryHolder.getDAOFactory()
						.getLogicalRecordDAO().getLRIndex(getId(),
								getEnvironmentId());

				if (lrIndexTrans == null) {
					this.lrIndex = null;
				} else {
					this.lrIndex = new LRIndex(lrIndexTrans);
				}
			} catch (DAOException de) {
				throw new SAFRException(
						"Data access error for Logical Record Index", de);
			}
		}
	}

	public SAFRList<ComponentAssociation> getLogicalFileAssociations() {
		return logicalFileAssociations;
	}

	public boolean addAssociatedLogicalFile(ComponentAssociation thisAssoc) {
		ComponentAssociation thatAssoc;
		boolean found = false;
		boolean result = false;
		Iterator<ComponentAssociation> i = logicalFileAssociations.iterator();
		while (!found && i.hasNext()) {
			thatAssoc = i.next();
			if (thisAssoc.getAssociatedComponentIdNum().equals(
					thatAssoc.getAssociatedComponentIdNum())) {
				found = true;
				result = true;
				if (thisAssoc.getPersistence() == SAFRPersistence.NEW
						&& thatAssoc.getPersistence() == SAFRPersistence.DELETED) {
					thatAssoc.setPersistence(SAFRPersistence.OLD);
				}
			}
		}
		if (!found) {
			result = logicalFileAssociations.add(thisAssoc);
			markModified();
		}
		return result;
	}

	public void removeAssociatedLogicalFiles(
			List<ComponentAssociation> associatedLogicalFiles)
			throws DAOException, SAFRValidationException {
		removeAssociations(associatedLogicalFiles, false);
		markModified();
	}

	private void removeAssociations(
			List<ComponentAssociation> associatedLogicalFiles,
			boolean checkDependenciesOnly) throws DAOException,
			SAFRValidationException {
		String dependencies = "";
		Map<Integer, List<DependentComponentTransfer>> dependentLookups = new HashMap<Integer, List<DependentComponentTransfer>>();
		Map<Integer, List<DependentComponentTransfer>> dependentViews = new HashMap<Integer, List<DependentComponentTransfer>>();
		Map<Integer, ComponentAssociation> temporaryMap = new HashMap<Integer, ComponentAssociation>();

		List<Integer> LRLFAssociationIds = new ArrayList<Integer>();
		for (ComponentAssociation LRLFAssociation : associatedLogicalFiles) {
			if (LRLFAssociation.getAssociationId() > 0L) {
				temporaryMap.put(LRLFAssociation.getAssociationId(),
						LRLFAssociation);
				LRLFAssociationIds.add(LRLFAssociation.getAssociationId());
			}
		}

		dependentLookups = DAOFactoryHolder.getDAOFactory()
				.getLogicalRecordDAO().getAssociatedLFLookupDependencies(
						getEnvironmentId(), LRLFAssociationIds);

		dependentViews = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.getAssociatedLFViewDependencies(getEnvironmentId(),
						LRLFAssociationIds);

		if (!dependentLookups.isEmpty() || !dependentViews.isEmpty()) {
			Map<Integer, List<List<DependentComponentTransfer>>> dependentComponentMap = new HashMap<Integer, List<List<DependentComponentTransfer>>>();

			// Merging the maps of Lookups and Views into a single map
			if (!dependentLookups.isEmpty()) {
				for (Integer assocID : dependentLookups.keySet()) {
					List<List<DependentComponentTransfer>> compList = new ArrayList<List<DependentComponentTransfer>>();
					compList.add(dependentLookups.get(assocID));
					compList.add(new ArrayList<DependentComponentTransfer>());
					dependentComponentMap.put(assocID, compList);
				}
			}
			if (!dependentViews.isEmpty()) {
				for (Integer assocID : dependentViews.keySet()) {
					if (dependentComponentMap.containsKey(assocID)) {
						dependentComponentMap.get(assocID).add(1,
								dependentViews.get(assocID));
					} else {
						List<List<DependentComponentTransfer>> compList = new ArrayList<List<DependentComponentTransfer>>();
						compList
								.add(new ArrayList<DependentComponentTransfer>());
						compList.add(dependentViews.get(assocID));
						dependentComponentMap.put(assocID, compList);
					}
				}
			}

			boolean firstLF = true;
			for (Integer LRLFAssociationId : LRLFAssociationIds) {
				if (dependentComponentMap.containsKey(LRLFAssociationId)) {

					if (firstLF == true) {
						firstLF = false;
					} else {
						dependencies += SAFRUtilities.LINEBREAK;
					}
					dependencies += "Logical File: ";
					ComponentAssociation componentAssoc = temporaryMap
							.get(LRLFAssociationId);
					// if new dependencies are found while saving, restore the
					// associations by setting the persistence to OLD
					if (checkDependenciesOnly) {
						componentAssoc.setPersistence(SAFRPersistence.OLD);
					}

					dependencies += componentAssoc.getAssociatedComponentName();
					dependencies += "["
							+ componentAssoc.getAssociatedComponentIdNum()
							+ "]";

					if (dependentComponentMap.get(LRLFAssociationId).get(0)
							.size() > 0) {
						dependencies += SAFRUtilities.LINEBREAK + "    LOOKUP PATHS :";
						List<DependentComponentTransfer> depLookupTransfers = dependentLookups
								.get(LRLFAssociationId);
						for (DependentComponentTransfer depLookup : depLookupTransfers) {
							dependencies += SAFRUtilities.LINEBREAK + "        " + depLookup.getDescriptor();
						}
					}

					if (dependentComponentMap.get(LRLFAssociationId).get(1)
							.size() > 0) {
						dependencies += SAFRUtilities.LINEBREAK + "    VIEWS :";
						List<DependentComponentTransfer> depViewTransfers = dependentViews
								.get(LRLFAssociationId);
						for (DependentComponentTransfer depView : depViewTransfers) {
							dependencies += SAFRUtilities.LINEBREAK + "        " + depView.getDescriptor();
						}
					}
				} else {
					if (!checkDependenciesOnly) {
						logicalFileAssociations.remove(temporaryMap
								.get(LRLFAssociationId));
					}
				}
			}
		} else {
			if (!checkDependenciesOnly) {
				for (ComponentAssociation LRLFAssociation : associatedLogicalFiles) {
					logicalFileAssociations.remove(LRLFAssociation);
				}
			}
		}
		if (!dependencies.equals("")) {
			SAFRValidationException exception = new SAFRValidationException();
			SAFRValidationToken safrValidationToken = new SAFRValidationToken(
					this, SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR);
			exception.setSafrValidationToken(safrValidationToken);
			exception
					.setErrorMessage(Property.LF_ASSOCIATION_DEP, dependencies);
			throw exception;
		}
	}

	@Override
	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		LogicalRecordTransfer trans = (LogicalRecordTransfer) safrTrans;
		trans.setLrTypeCode(lrTypeCode.getKey()); // non-null
		trans.setLrStatusCode(lrStatusCode.getKey()); // non-null
		if (lookupExitId == null) {
		    trans.setLookupExitId(0);
		}
		else {
		    trans.setLookupExitId(lookupExitId);
		}
		if (lookupExitParams == null) {
	        trans.setLookupExitParams("");		    
		}
		else {
	        trans.setLookupExitParams(lookupExitParams);		    
		}
	}

	@Override
	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		LogicalRecordTransfer trans = (LogicalRecordTransfer) safrTrans;
		// Populate the non-list attributes from transfer object
		// CQ 8596. Nikita. 15/09/2010.
		// Handling invalid foreign keys and reporting the problem to the user
		loadWarnings = new ArrayList<String>();
		try {
			this.lrTypeCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.LRTYPE).getCode(trans.getLrTypeCode()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("This Logical Record does not have a valid type. Please select a valid type before saving.");
			this.lrTypeCode = null;
		}
		try {
			this.lrStatusCode = SAFRApplication.getSAFRFactory().getCodeSet(
					CodeCategories.LRSTATUS).getCode(trans.getLrStatusCode()); // non-null
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
			loadWarnings
					.add("This Logical Record does not have a valid status. Please select a valid status before saving.");
			this.lrStatusCode = null;
		}
		this.lookupExitId = trans.getLookupExitId();
		if (lookupExitRoutine != null
				&& lookupExitRoutine.getId() != trans.getLookupExitId()) {
			this.lookupExitRoutine = null;
		}
		this.lookupExitParams = trans.getLookupExitParams();
	}

	public Code getLRTypeCode() throws SAFRException {
		return lrTypeCode;
	}

	/**
	 * Sets the LR Type Code.
	 * 
	 * @param lrTypeCode
	 *            the LR Type Code to set
	 * @throws NullPointerException
	 *             if the Code object is null, as this is a mandatory field and
	 *             cannot be null.
	 */
	public void setLRTypeCode(Code lrTypeCode) {
		if (lrTypeCode == null) {
			throw new NullPointerException("LR Type code cannot be null.");
		}
		this.lrTypeCode = lrTypeCode;
		markModified();
	}

	public Code getLRStatusCode() throws SAFRException {
		return lrStatusCode;
	}

	/**
	 * Sets the LR Status Code.
	 * 
	 * @param lrStatusCode
	 *            the LR Status Code to set.
	 * @throws NullPointerException
	 *             if the Code object is null, as this is a mandatory field and
	 *             cannot be null.
	 */
	public void setLRStatusCode(Code lrStatusCode) {
		if (lrStatusCode == null) {
			throw new NullPointerException("LR Status code cannot be null.");
		}
        if (this.lrStatusCode != lrStatusCode &&
            lrStatusCode.getGeneralId().equals(Codes.INACTIVE)) {
            setCheckLookupDependencies(true);
            setCheckViewDependencies(true);            
        }
        
        if (this.lrStatusCode != lrStatusCode) {
            markActivated();            
        }
        
		this.lrStatusCode = lrStatusCode;				
	}

	public boolean isActive() throws SAFRException {
		boolean result = false;
		Code lrStatus = getLRStatusCode();
		if (lrStatus != null) {
			result = lrStatus.getGeneralId() == Codes.ACTIVE;
		}
		return result;
	}

	public void setActive(boolean active) {
		CodeSet lrStatusCodes = SAFRApplication.getSAFRFactory().getCodeSet(
				CodeCategories.LRSTATUS);
		if (active) {
			setLRStatusCode(lrStatusCodes
					.getCode(Integer.valueOf(Codes.ACTIVE)));
		} else {
			setLRStatusCode(lrStatusCodes.getCode(Integer
					.valueOf(Codes.INACTIVE)));
		}
	}

	public void setLookupExitId(Integer lookupExitId) {
		this.lookupExitId = lookupExitId;
		// Clear the exit routine if it's different to the specified ID
		if (lookupExitRoutine != null) {
			if (!lookupExitRoutine.getId().equals(lookupExitId)) {
				this.lookupExitRoutine = null; // will be lazy instantiated
			}
		}
		markModified();
	}

	public UserExitRoutine getLookupExitRoutine() throws SAFRException {
		if (lookupExitRoutine == null) {
			if (lookupExitId != null && lookupExitId > 0) {
				this.lookupExitRoutine = SAFRApplication.getSAFRFactory()
						.getUserExitRoutine(lookupExitId, this.getEnvironmentId());
			}
		}
		return lookupExitRoutine;
	}

	public void setLookupExitRoutine(UserExitRoutine lookupExitRoutine) {

		if (lookupExitRoutine == null) {
			this.lookupExitId = 0;
		} else {
			this.lookupExitId = lookupExitRoutine.getId();
		}
		this.lookupExitRoutine = lookupExitRoutine;
		markModified();
	}

	public String getLookupExitParams() {
		return this.lookupExitParams;
	}

	public void setLookupExitParams(String lookupExitParams) {
		this.lookupExitParams = lookupExitParams;
		markModified();
	}

	public SAFRList<LRField> getLRFields() {
		return lrFields;
	}

    public LRField findLRField(Integer id) {
        if (id == null || id == 0) {
            return null;
        }
        else {
            for (LRField fld : lrFields.getActiveItems()) {
                if (fld.getId().equals(id)) {
                    return fld;
                }
            }
        }
        return null;
    }
	

	/**
	 * Sets a flag to indicate that Lookup dependencies should be checked before
	 * saving this LR.
	 * 
	 * @param checkLookupDependencies
	 */
	public void setCheckLookupDependencies(boolean checkLookupDependencies) {
		this.checkLookupDependencies = checkLookupDependencies;
	}

	/**
	 * Sets a flag to indicate that View dependencies should be checked before
	 * saving this LR.
	 * 
	 * @param checkViewDependencies
	 */
	public void setCheckViewDependencies(boolean checkViewDependencies) {
		this.checkViewDependencies = checkViewDependencies;
	}

	public void addViewColumnAsField(ViewColumn col) {
	    LRField field = addField();
	    
	    String heading1 = trimName(col.getHeading1());
        String heading2 = trimName(col.getHeading2());
        String heading3 = trimName(col.getHeading3());
	    
	    // generate name
	    String name = 
	        (heading1.isEmpty() ? "" : heading1) +
            ((heading1.isEmpty() || heading2.isEmpty()) ? "" : "_") +
	        (heading2.isEmpty() ? "" : heading2) +
            ((heading2.isEmpty() || heading3.isEmpty()) ? "" : "_") +
            (heading3.isEmpty() ? "" : heading3);
	    
        field.setName(name);
        field.setHeading1(col.getHeading1());
        field.setHeading2(col.getHeading2());
        field.setHeading3(col.getHeading3());
        field.setComment(col.getComment());
	    field.setDataTypeCode(col.getDataTypeCode());
        field.setDateTimeFormatCode(col.getDateTimeFormatCode());
        field.setDecimals(col.getDecimals());
        field.setScaling(col.getScaling());
        field.setSigned(col.isSigned());
	    field.setLength(col.getLength());
	}

    protected String trimName(String name) {
        if (name == null) {
            return "";
        }
        name = name.trim();
        name = name.replaceAll(" ", "_");          
        // trim name until its valid
	    while (name.length() > 0) {
	        // check first character
	        if (!Character.isLetter(name.charAt(0))) {
	            name = name.substring(1, name.length());
	        } else {
	            // check rest of characters
	            boolean changed = false;
	            for (int i = 0; i < name.length(); i++) {
	                char c = name.charAt(i);
	                if (!Character.isDigit(c) && !Character.isLetter(c) && !(c == '_') && !(c == '#')) {
	                    name = name.substring(0,i) + name.substring(i+1, name.length());
	                    changed = true;
	                    break;
	                }	                
	            }
	            if (!changed) {
	                break;
	            }
	        }
	    }
        return name;
    }
	
	/**
	 * Use this method to add a new blank LR field at the end of list.
	 * 
	 * @throws SAFRException
	 * @return {@link LRField} The newly added LR field.
	 */
	public LRField addField() throws SAFRException {
		LRField field = new LRField(this);
        field.setID(SAFRApplication.getSAFRFactory().getNextLRFieldId());
		// Start Pos = Start Pos + Length (of previous row)
		// Ordinal Pos = Ordinal Pos + 1 (of previous row)
		
        // find previous field
        LRField prevField = null;
        List<LRField> flds = lrFields.getActiveItems();
        if (flds.size() > 0) {
            prevField = flds.get(flds.size()-1);
        }

        if (prevField == null) {
            field.setPositionSimple(1);            
        }
        else {
            if (prevField.getPosition() == null) {
                field.setPositionSimple(null);
            }
            else {
                field.setPositionSimple(prevField.getPosition()
                + prevField.getLength().intValue());
            }
        }
        
        lrFields.add(field);
        flds = lrFields.getActiveItems();
		if (flds.size() > 1) {
			LRField tmpField = flds.get(flds.size() - 2);
			field.setOrdinalPosition((int)tmpField.getOrdinalPosition()+1);
		} else {
			field.setOrdinalPosition(1);
		}
		markModified();
		return field;
	}
	
	

	/**
	 * Inserts a field at a particular index.
	 * 
	 * @param postField
	 *            field above which new field is to be inserted
	 * @throws SAFRException
	 * @return {@link LRField} The inserted LR field or null if index is out of
	 *         range.
	 */
	public LRField insertFieldBefore(LRField postField) throws SAFRException {
        int index; 
        if (postField == null) {
            index = 0;
        }
        else {
            index = lrFields.indexOf(postField);            
        }
		LRField field = new LRField(this);
        field.setID(SAFRApplication.getSAFRFactory().getNextLRFieldId());
		lrFields.add(index, field);
		
        List<LRField> activeItems = lrFields.getActiveItems();
		int actIndex = activeItems.indexOf(field);
        generateOrdinalPostion(actIndex, field);

        // set position based on previous field
        if (actIndex == 0) {
            field.setPositionSimple(1);                        
        }
        else {
            LRField prevField = activeItems.get(actIndex-1);
            if (prevField.getPosition() == null) {
                field.setPositionSimple(null);
            }
            else {
                field.setPositionSimple(prevField.getPosition() + prevField.getLength());
            }
        }        
                
        markModified();
		
		return field;
	}

    /**
     * Inserts a field at a particular index.
     * 
     * @param prevField
     *            field above which new field is to be inserted
     * @throws SAFRException
     * @return {@link LRField} The inserted LR field 
     */
    public LRField insertFieldAfter(LRField prevField) throws SAFRException {
        int index; 
        if (prevField == null) {
            index = -1;
        }
        else {
            index = lrFields.indexOf(prevField);            
        }
        LRField field = new LRField(this);
        field.setID(SAFRApplication.getSAFRFactory().getNextLRFieldId());
        lrFields.add(index+1, field);
        
        if (prevField == null) {
            field.setPositionSimple(1);            
        }
        else {
            if (prevField.getPosition() == null) {
                field.setPositionSimple(null);
            }
            else {
                field.setPositionSimple(prevField.getPosition()
                + prevField.getLength().intValue());
            }
        }        
        
        List<LRField> activeItems = lrFields.getActiveItems();
        int actIndex = activeItems.indexOf(field);
        generateOrdinalPostion(actIndex, field);
        markModified();
        
        return field;
    }
	
    private void generateOrdinalPostion(int index, LRField field) {
        List<LRField> activeItems = lrFields.getActiveItems();
        if (activeItems.size() > 1) {
			// update ordinal pos of this row and all rows after
			int ordPos = 1;
			if (index >= 1) {
			    ordPos = activeItems.get(index-1).getOrdinalPosition()+1;
			}
			for (int i=index ; i<activeItems.size(); i++) {
			    activeItems.get(i).setOrdinalPosition(ordPos);
                ordPos++;
			}
		} else {
			field.setOrdinalPosition(1);
		}
    }

	/**
	 * Removes a field from this Logical record. The field is only removed if no
	 * other component is dependent on it.
	 * 
	 * @param selectedLrFieldsList
	 *            list of the fields to be removed.
	 * @return list of LR fields removed.
	 * @throws DAOException
	 *             if a database access error occurs.
	 * @throws SAFRValidationException
	 *             if the field to be deleted has got dependent components. In
	 *             this situation, the field will not be deleted.
	 */
	public List<LRField> removeFields(List<LRField> selectedLrFieldsList)
			throws DAOException, SAFRValidationException {

		if (selectedLrFieldsList == null) {
			throw new NullPointerException("LR Field cannot be null");
		}
		String dependencies = "";

		// a map to allow only unique components to be returned in error
		// message
		Map<Integer, Integer> allowUniqueMap = new HashMap<Integer, Integer>();

		Map<Integer, List<DependentComponentTransfer>> dependentLookupComps;

		List<LRField> deletedLrFieldsList = new ArrayList<LRField>();

		boolean fieldsHaveDependencies = false;
		for (LRField selectedField : selectedLrFieldsList) {
			boolean fieldHasDependency = false;
			List<Integer>  fieldsToBeDeleted = new ArrayList<Integer>();
			fieldsToBeDeleted.add(selectedField.getId());
			// Lookups dependencies
			dependentLookupComps = DAOFactoryHolder.getDAOFactory()
					.getLogicalRecordDAO().getFieldLookupDependencies(
							getEnvironmentId(),
							fieldsToBeDeleted);
			
			if (!dependentLookupComps.isEmpty()) {
				// show the field label
				dependencies += SAFRUtilities.LINEBREAK + "Field '" + selectedField.getDescriptor() + "':";
				fieldsHaveDependencies = true;
				fieldHasDependency = true;
				dependencies += SAFRUtilities.LINEBREAK + "    LOOKUP PATHS:";
				List<DependentComponentTransfer> depCompTransfers;
				for (Integer fieldId : dependentLookupComps.keySet()) {
					depCompTransfers = dependentLookupComps.get(fieldId);
					for (DependentComponentTransfer dep : depCompTransfers) {
						Integer lookupID = dep.getId();
						if (!allowUniqueMap.containsKey(lookupID)) {
							// add to message only if not previously added.
							dependencies += SAFRUtilities.LINEBREAK + "        -> "
									+ dep.getDescriptor();
							allowUniqueMap.put(lookupID, lookupID);
						}
					}
				} // end for lookups
			}

			// views dependencies
			Map<Integer, List<DependentComponentTransfer>> dependentViewComps;
			dependentViewComps = DAOFactoryHolder.getDAOFactory()
					.getLogicalRecordDAO().getFieldViewDependencies(
							getEnvironmentId(),
							fieldsToBeDeleted);
			
			allowUniqueMap.clear();
			if (!dependentViewComps.isEmpty()) {
				if (!fieldHasDependency) {
					// first dep so show the field label
					dependencies += SAFRUtilities.LINEBREAK + "Field '" + selectedField.getDescriptor() + "':";
				}
				fieldsHaveDependencies = true;
				fieldHasDependency = true;
				dependencies += SAFRUtilities.LINEBREAK + "    VIEWS:";
				List<DependentComponentTransfer> depCompTransfers;
				for (Integer fieldId : dependentViewComps.keySet()) {
					depCompTransfers = dependentViewComps.get(fieldId);
					for (DependentComponentTransfer dep : depCompTransfers) {
						Integer viewID = dep.getId();
						// CQ 8056. Nikita. 20/07/2010. Show location in
						// dependency message
						String dependencyInfo = "";
						if (dep.getDependencyInfo() != null
								&& !dep.getDependencyInfo().equals("")) {
							dependencyInfo = " - " + dep.getDependencyInfo();
						}
						dependencies += SAFRUtilities.LINEBREAK + "        ->" + dep.getDescriptor() + dependencyInfo;
						allowUniqueMap.put(viewID, viewID);
					}
				} // end for views
			}
			if (!fieldHasDependency) {
				deletedLrFieldsList.add(selectedField);
			} 
			
		} // end for fields
		
		if (fieldsHaveDependencies) {
			dependencies = dependencies.substring(2); // trim the leading line feed
			SAFRValidationException exception = new SAFRValidationException();
			exception.setSafrValidationToken(new SAFRValidationToken(this,
					SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR));
			exception.setErrorMessage(Property.VIEW_LOOKUP_DEP, dependencies);
			throw exception;
		}
		
		for (LRField field : deletedLrFieldsList) {
		    
		    if (!isForMigration()) {
    	        // reset all ordinals
                List<LRField> flds = lrFields.getActiveItems();
                int ordPos = field.getOrdinalPosition();
             
                for (int i=field.getOrdinalPosition() ; i<flds.size(); i++) {
                    flds.get(i).setOrdinalPosition(ordPos);
                    ordPos++;
                }
		    }
	        
			lrFields.remove(field);
			
		}
        if (!isForMigration()) {		
            autocalcRedefine();
        }
		if (deletedLrFieldsList.size() > 0) {
			markModified();
		}
		
		return deletedLrFieldsList;
	}

	/**
	 * This method is used to move a LR Field, specified by a zero relative
	 * index, to one position up.
	 * 
	 * @param lrField
	 *            : LR Field to be moved one position up.
	 * 
	 */
	public void moveFieldUp(LRField lrField) {
		List<LRField> activeItems = lrFields.getActiveItems();

		LRField tmpField = activeItems.get((activeItems.indexOf(lrField)) - 1);

		int moveToIndex = lrFields.indexOf(tmpField);
		int moveFromIndex = lrFields.indexOf(lrField);
		lrFields.remove(moveToIndex);
		lrFields.add(moveFromIndex, tmpField);
		
        int tmpOrd = tmpField.getOrdinalPosition();
        tmpField.setOrdinalPosition(lrField.getOrdinalPosition());
        lrField.setOrdinalPosition(tmpOrd);        
        lrField.setRedefine(-1);
        lrField.setPositionSimple(null);
        autocalcRedefine();
		markModified();
	}

	/**
	 * This method is used to move a LR Field, specified by a zero relative
	 * index, to one position down.
	 * 
	 * @param field
	 *            : LR Field which is to be moved one position down.
	 */
	public void moveFieldDown(LRField field) {
		List<LRField> activeItems = lrFields.getActiveItems();
		int moveFromIndex = activeItems.indexOf(field);
		LRField tmpField = activeItems.get(moveFromIndex + 1);
		int moveToIndex = lrFields.indexOf(tmpField);
		lrFields.remove(moveToIndex);
		lrFields.add(moveFromIndex, tmpField);
		
		int tmpOrd = tmpField.getOrdinalPosition();
		tmpField.setOrdinalPosition(field.getOrdinalPosition());
		field.setOrdinalPosition(tmpOrd);	
        field.setRedefine(-1); 
        field.setPositionSimple(null);            
        autocalcRedefine();
		markModified();
	}

	/**
	 * Returns the length of the record's primary key, if any. This is the
	 * cummulative length of the LR Fields which make up the primary key.
	 * 
	 * @return a Integer indicating the primary key length
	 */
	public Integer getPrimayKeyLength() {
		// add up the lengths of the LRFields with key type PRIMARYKEY
		Integer returnlen = 0;
		List<LRField> activeFields = lrFields.getActiveItems();
		for (int i = 0; i < activeFields.size(); i++) {
			LRField field = activeFields.get(i);
			if (field.getKeyType() == LRFieldKeyType.PRIMARYKEY) {
				returnlen += field.getLength();
			}
		}
		return returnlen;
	}

	/**
	 * Returns the total length of all the fields. This should be the maximum of
	 * 'Start Pos + Length' of all fields.
	 * 
	 * @return a Integer indicating the total field length.
	 */
	public Integer getTotalLength() {
		Integer returnlen = 0;
		Integer tmpLen = 0;
		List<LRField> activeFields = lrFields.getActiveItems();
		for (int i = 0; i < activeFields.size(); i++) {
			LRField field = activeFields.get(i);
			if (field.getPosition() == null) {
			    continue;
			}
			tmpLen = field.getPosition() + field.getLength();
			if (tmpLen > returnlen) {
				returnlen = tmpLen;
			}
		}
		returnlen -= 1;
		if (returnlen < 0) {
			returnlen = 0;
		}
		return returnlen;
	}

	/**
	 * Recalculates field positions (Start position and Ordinal position).
	 * Pass the LR field from where the recalculation should start in
	 * the LR field's list.
	 * 
	 * @param fromField
	 *            field, in the LR Field's list from where recalculation should
	 *            start. To re-calculate all the rows, pass this parameter as
	 *            first field.
	 */
	public void recalculateFields(LRField fromField) {

		List<LRField> activeFields = lrFields.getActiveItems();
		int fromIndex = activeFields.indexOf(fromField);

		// out of range, exit
		if (fromIndex < 0 || fromIndex >= activeFields.size()) {
			return;
		}

		recalculateFields(activeFields.subList(fromIndex, activeFields.size()));
	}

	/**
	 * Recalculates field positions (Start position and Ordinal position).
	 * Pass the list of LR fields to be recalculated.
	 * 
	 * @param selectedLrFieldsList
	 *            LR Field's list which is to be recalculated .
	 * */

	public void recalculateFields(List<LRField> selectedLrFieldsList) {
		if (selectedLrFieldsList == null) {
			throw new NullPointerException("LR Fields cannot be null");
		}
		Map<Integer,LRField> startPositions = new HashMap<Integer,LRField>();
		boolean firstField = true;
		for (LRField field : selectedLrFieldsList) {

		    Integer parent = field.getRedefine();
		    LRField parFld = null;
		    
		    // top level recalculate
		    if (parent == null || parent == 0 || parent == -1) {
		        parent = 0;
		    }
		    else {
		        parFld = findLRField(parent);
		    }
		    
		    if (firstField) {
		        firstField = false;
                startPositions.put(parent, field);
		    }
		    else if (startPositions.containsKey(parent)) {
                LRField previousField = startPositions.get(parent);
                int len = previousField.getLength();
                if (previousField.getPosition() != null) {
                    int startPos = previousField.getPosition();
                    if (field.getRedefine() == null) {
                        field.setPositionSimple(startPos + len);                        
                        field.markModified();
                    }
                    else if (field.getRedefine().equals(-1)) {
                        field.setPositionSimple(startPos);
                        field.markModified();
                        if (field.getRedefine() != -1) {
                            parent = field.getRedefine();
                        }
                    }
                    else {
                        field.setPositionSimple(startPos + len);
                        field.markModified();
                    }
                    startPositions.put(parent, field);
                }
            } else {
                if (parFld == null) {
                    List<LRField> actItem = getLRFields().getActiveItems();
                    int index = actItem.indexOf(field);
                    if (index == 0) {
                        field.setPositionSimple(1);
                        field.markModified();
                    }
                }
                else {
                    if (isFirstChild(parFld, field)) {
                        field.setPositionSimple(parFld.getPosition());
                        field.markModified();
                    }
                }
                startPositions.put(parent, field);
            }		    
		}
		markModified();
	}

	private boolean isFirstChild(LRField parent, LRField child) {
	    return parent.getChildren().get(0).getId().equals(child.getId());
	}
	/**
	 * Resets index keys (start effective and end effective) of LR Fields. Only
	 * one Field can have start effective or end effective set.<br>
	 * <br>
	 * if a field is set to start effective, set the key type of other field to
	 * NONE, if it is start effective.<br>
	 * if a field is set to end effective, set the key type of other field to
	 * NONE, if it is end effective. <br>
	 * <br>
	 * called by a LR field whose Key type is changed to Start effective or End
	 * effective.
	 * 
	 * @param field
	 *            the field whose key type is changed to an Indexed type (Start
	 *            effective or End effective)
	 * @param keyType
	 *            the key type set. Should be one of Start effective or End
	 *            effective.
	 */
	void resetIndexKeys(LRField field, LRFieldKeyType keyType) {
		if (keyType == LRFieldKeyType.EFFENDDATE
				|| keyType == LRFieldKeyType.EFFSTARTDATE) {
			int fieldIndex = lrFields.indexOf(field);
			// List<LRField> activeFields = lrFields.getActiveItems();
			for (int i = 0; i < lrFields.size(); i++) {
				LRField tmpField = lrFields.get(i);
				if (i != fieldIndex && tmpField.getKeyType() == keyType) {
					tmpField.setKeyType(LRFieldKeyType.NONE);
				}
			}
		}
	}

	public void store() throws SAFRException, DAOException {
		if (isForMigration()) {
            if (!SAFRApplication.getUserSession().isAdminOrMigrateInUser(getEnvironmentId())) {
				String msg = "The user is not authorized to migrate into Environment "+ getEnvironmentId();
				throw new SAFRException(msg);
			}
		} else {
			if (this.id == 0) {
				if (!hasCreatePermission()) {
					throw new SAFRException("The user is not authorized to create a new logical record.");
				}
			} else {
				if (!hasUpdateRights()) {
					throw new SAFRException("The user is not authorized to update this logical record.");
				}
			}
		}
		
		// check for LR dependency
		// check for only saved LRs.
		List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();
		boolean success = false;
		try {
			while (!success) {
				try {
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					if (this.id > 0) {
						// Lookups first
						if (checkLookupDependencies) {
							DAOFactoryHolder.getDAOFactory().getLookupDAO().makeLookupPathsInactive(
								deactivatedLookupList,getEnvironmentId());
						}

						// Views
						if (checkViewDependencies) {
							DAOFactoryHolder.getDAOFactory().getViewDAO().makeViewsInactive(
								deactivatedViewList,getEnvironmentId());
						}
					}

					// CQ 7329 Kanchan Rauthan 04/03/2010 To show error if
					// logical
					// record is
					// already deleted from database and user still tries to
					// save it.

					// Save the LR content
					LogicalRecordTransfer trans = new LogicalRecordTransfer();
					setTransferData(trans);
					trans = DAOFactoryHolder.getDAOFactory()
							.getLogicalRecordDAO().persistLogicalRecord(trans);

					trans.setForImport(isForImport()); // retain import flag
					setObjectData(trans);
					savedObjs.add(this);

                    if (!isForMigration() && !SAFRApplication.getUserSession().isSystemAdministrator()) {
                        SAFRApplication.getUserSession().getGroup().assignComponentFullRights(
                            this, ComponentType.LogicalRecord);
                    }
					
					// Save the LR fields content
					List<LRFieldTransfer> transList = new ArrayList<LRFieldTransfer>();
					HashMap<LRFieldTransfer, LRField> fieldMap = new HashMap<LRFieldTransfer, LRField>();
					List<Integer> deletedIds = new ArrayList<Integer>();

					for (LRField lrField : lrFields) {
						if (lrField.getPersistence() == SAFRPersistence.DELETED) {
							// store in list to delete later
							deletedIds.add(lrField.getId());
						} else if (lrField.getPersistence() != SAFRPersistence.OLD
								|| isForImport() || isForMigration()) {
							// store in list to add/update later
							LRFieldTransfer fieldTrans = new LRFieldTransfer();
							lrField.setTransferData(fieldTrans);
							transList.add(fieldTrans);
							fieldMap.put(fieldTrans, lrField);
						}
					}

					if (transList.size() > 0) {
						transList = DAOFactoryHolder.getDAOFactory()
								.getLRFieldDAO().persistLRField(transList);
						for (int i = 0; i < transList.size(); i++) {
							LRFieldTransfer fieldTrans = transList.get(i);
							LRField lrField = fieldMap.get(fieldTrans);
							lrField.setObjectData(fieldTrans);
							savedObjs.add(lrField);

						}
					}

					if (deletedIds.size() > 0) {
						DAOFactoryHolder.getDAOFactory().getLRFieldDAO()
								.removeLRField(deletedIds, getEnvironmentId());

					}
                    lrFields.flushDeletedItems(); // physically remove the deleted items

					storeAssociatedLFs(savedObjs);
                    					
					// Save the LR index content
					storeLRIndex();

                    SAFRApplication.getModelCount().incCount(this.getClass(), 1);
                    SAFRApplication.getModelCount().incCount(ComponentAssociation.class, logicalFileAssociations.size());
                    SAFRApplication.getModelCount().incCount(LRField.class, lrFields.size());
                    SAFRApplication.getModelCount().incCount(LRIndex.class, 1);
                    if (lrIndex != null) {
                        SAFRApplication.getModelCount().incCount(LRIndexField.class, lrIndex.getLRIndexFields().size());                        
                    }
					
                    setCheckLookupDependencies(false);
                    setCheckViewDependencies(false);                    
					success = true;
					setActivated(false);
				} catch (DAOUOWInterruptedException e) {
					// UOW interrupted so retry it
					if (DAOFactoryHolder.getDAOFactory().getDAOUOW().isMultiComponentScope()) {
						throw e;
					} else {
						continue;
					}
				}

			} // end while(!success)
		} catch (SAFRNotFoundException snfe) {
			snfe.printStackTrace();
			throw new SAFRException(
					"The logical record with id "
							+ this.getId()
							+ " cannot be updated as its already been deleted from the database.",
					snfe);
		} finally {

			if (success) {
				// End Transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().end();

			} else {
				// Rollback the transaction.
				DAOFactoryHolder.getDAOFactory().getDAOUOW().fail();
				// reset the object state
				for (SAFRPersistentObject obj : savedObjs) {
					obj.undo();
				}
			}
		}

	}

    protected void storeAssociatedLFs(List<SAFRPersistentObject> savedObjs) {
        // save associated LogicalFiles.
        List<Integer> deletionIds = new ArrayList<>();
        List<ComponentAssociationTransfer> list = new ArrayList<ComponentAssociationTransfer>();
        HashMap<ComponentAssociationTransfer, ComponentAssociation> map = new HashMap<ComponentAssociationTransfer, ComponentAssociation>();
        Map<Integer, ComponentAssociation> sortedByIDMap = new TreeMap<Integer, ComponentAssociation>();
        int associationSize = logicalFileAssociations.size();
        for (int i = 0; i < associationSize; i++) {
        	ComponentAssociation association = logicalFileAssociations
        			.get(i);
        	if (association.getPersistence() == SAFRPersistence.DELETED) {
        		deletionIds.add(association.getAssociationId());

        	} else {
        		// create transfer object
        		ComponentAssociationTransfer assocTrans = new ComponentAssociationTransfer();
        		association.setTransferFromObject(assocTrans);
        		if (assocTrans.getAssociatingComponentId() == 0) {
        			assocTrans.setAssociatingComponentId(this.getId());
        			assocTrans.setAssociatingComponentName(this.getName());
        		}
        		// Only persist NEW, MODIFIED associations, leave OLD ones as they are
        		if (association.getPersistence() != SAFRPersistence.OLD){
        			list.add(assocTrans);
        			map.put(assocTrans, association);
        		}
        		// All component associations should get sorted by IDs
        		sortedByIDMap.put(association.getAssociatedComponentIdNum(),association);
        	}
        }

        // call DAO to delete
        if (deletionIds.size() > 0) {
        	DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().deleteAssociatedLF(getEnvironmentId(), deletionIds);
        }

        // call DAO to add
        if (list.size() > 0) {
        	list = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().persistAssociatedLF(list, getId());

        	for (ComponentAssociationTransfer assocTrans : list) {
        		ComponentAssociation compAssoc = map.get(assocTrans);
        		compAssoc.setObjectFromTransfer(assocTrans);
        		savedObjs.add(compAssoc);
        	}
        }

        // regenerate association list from Tree Map
        logicalFileAssociations.clear();
        logicalFileAssociations.addAll(sortedByIDMap.values());
    }

	private void storeLRIndex() throws SAFRException {
		if (isForMigration()) {
			storeLRIndexForMigration();
			return;
		}
		if (isForImport()) {
			// importing, use the index and index field transfer objects from
			// XML file
			LRInformationProvider importer = (LRInformationProvider) provider
					.getImporter();
			LRIndexTransfer lrIndexTransfer = importer.getLRIndex(this.getId());
			if (lrIndexTransfer != null) {
				lrIndexTransfer.setEnvironmentId(getEnvironmentId());
				DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
						.persistLRIndex(lrIndexTransfer);
				// store LR index fields
				List<LRIndexFieldTransfer> fields = importer
						.getLRIndexFields(lrIndexTransfer.getId());
				for (LRIndexFieldTransfer trans : fields) {
					trans.setEnvironmentId(getEnvironmentId());
				}
				DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
						.persistLRIndexFields(fields);
			}
		} else {
			if (!hasIndexFields() && lrIndex != null) {
				// Index has been removed so delete old content
				DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
						.removeLRIndexFields(lrIndex.lrIndexId,
								getEnvironmentId());
				DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
						.removeLRIndex(lrIndex.lrIndexId, lrIndex.getLrId(), getEnvironmentId());
				this.lrIndex = null;
			}

			if (hasIndexFields() && lrIndex == null) {
				// New index defined, but not yet persistent
				this.lrIndex = new LRIndex(0);
			}

			if (lrIndex != null) {
				// Persist the LRIndex content
				LRIndexTransfer lrIndexTransfer = new LRIndexTransfer();
				if (lrIndex.lrIndexId.equals(0)) {
					lrIndexTransfer.setPersistent(false); // required to allow
					// Data layer to create this index
				} else {
					lrIndexTransfer.setPersistent(true);
				}
				lrIndexTransfer.setEnvironmentId(lrIndex.getEnvId());
				lrIndexTransfer.setId(lrIndex.lrIndexId);
				lrIndexTransfer.setLrId(lrIndex.getLrId());
				lrIndexTransfer.setEffectiveStartDateLRFieldId(lrIndex
						.getEffStartDateFieldId());
				lrIndexTransfer.setEffectiveEndDateLRFieldId(lrIndex
						.getEffEndDateFieldId());
				lrIndexTransfer = DAOFactoryHolder.getDAOFactory()
						.getLogicalRecordDAO().persistLRIndex(lrIndexTransfer);
				this.lrIndex = new LRIndex(lrIndexTransfer.getId());

				// persist any LRIndexField content
				List<LRIndexField> lrIndexFields = lrIndex.getLRIndexFields();
				if (lrIndexFields.size() > 0) {
					List<LRIndexFieldTransfer> transfers = new ArrayList<LRIndexFieldTransfer>();
					for (LRIndexField lrif : lrIndexFields) {
						LRIndexFieldTransfer lrifTrans = new LRIndexFieldTransfer();
						lrifTrans.setEnvironmentId(lrif.getEnvId());
						lrifTrans.setAssociationId(0); // insert only, never
						// update.
						lrifTrans
								.setAssociatingComponentId(lrif.getLrIndexId());
						lrifTrans.setFldSeqNbr(lrif.getFieldSeqNo());
						lrifTrans.setAssociatedComponentId(lrif.getFieldId());
						transfers.add(lrifTrans);
					}
					DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
							.persistLRIndexFields(transfers);
				}
			}
		}
	}
	
	private void storeLRIndexForMigration() throws SAFRException {

		// delete any existing LR index from the target env
		DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.removeLRIndexFieldsForLR(getId(), getEnvironmentId());
		DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
				.removeLRIndexForLR(getId(), getEnvironmentId());

		// store any source LR index in the target env
		if (lrIndex != null) {
			// Persist the LRIndex content
			LRIndexTransfer lrIndexTransfer = new LRIndexTransfer();
			lrIndexTransfer.setPersistent(false);
			lrIndexTransfer.setForMigration(true);
			lrIndexTransfer.setEnvironmentId(lrIndex.getEnvId());
			lrIndexTransfer.setId(lrIndex.lrIndexId);
			lrIndexTransfer.setLrId(lrIndex.getLrId());
			lrIndexTransfer.setEffectiveStartDateLRFieldId(lrIndex
					.getEffStartDateFieldId());
			lrIndexTransfer.setEffectiveEndDateLRFieldId(lrIndex
					.getEffEndDateFieldId());
			lrIndexTransfer.setCreateTime(lrIndex.createTime);
			lrIndexTransfer.setCreateBy(lrIndex.createBy);
			lrIndexTransfer.setModifyTime(lrIndex.modifyTime);
			lrIndexTransfer.setModifyBy(lrIndex.modifyBy);
			lrIndexTransfer = DAOFactoryHolder.getDAOFactory()
					.getLogicalRecordDAO().persistLRIndex(lrIndexTransfer);

			// persist any LRIndexField content
			List<LRIndexField> lrIndexFields = lrIndex.getLRIndexFields();
			if (lrIndexFields.size() > 0) {
				List<LRIndexFieldTransfer> transfers = new ArrayList<LRIndexFieldTransfer>();
				for (LRIndexField lrif : lrIndexFields) {
					LRIndexFieldTransfer lrifTrans = new LRIndexFieldTransfer();
					lrifTrans.setPersistent(false);
					lrifTrans.setForMigration(true);
					lrifTrans.setEnvironmentId(lrif.getEnvId());
					lrifTrans.setAssociationId(lrif.lrIndexFldId);
					lrifTrans.setAssociatingComponentId(lrif.getLrIndexId());
					lrifTrans.setAssociatedComponentId(lrif.getFieldId());
					lrifTrans.setFldSeqNbr(lrif.getFieldSeqNo());
					lrifTrans.setCreateTime(lrif.createTime);
					lrifTrans.setCreateBy(lrif.createBy);
					lrifTrans.setModifyTime(lrif.modifyTime);
					lrifTrans.setModifyBy(lrif.modifyBy);
					transfers.add(lrifTrans);
				}
				DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO()
						.persistLRIndexFields(transfers);
			}
		}
	}	

	/**
	 * This enum maintains the properties of logical record.
	 * 
	 */
	public enum Property {
		LR_FIELD, LR_NAME, LF_ASSOCIATION_DEP, VIEW_LOOKUP_DEP, LF_ASSOCIATION, LOOKUP_USER_EXIT_ROUTINE, TYPE, STATUS, LF_ASSOCIATION_DEP_IMPORT, VIEW_LOOKUP_DEP_IMPORT
	}

	/**
	 * Validate method is used to validate a Logical Record object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the logical record, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRException, DAOException {
		validate(null);
	}

	/**
	 * 
	 * @param token
	 * @throws SAFRException
	 * @throws DAOException
	 * @throws IllegalArgumentException
	 *             if the token ID does not identify this object.
	 */
	public void validate(SAFRValidationToken token) throws SAFRException,
			DAOException {

		if (token != null && token.getTokenId() != this.hashCode()) {
			throw new IllegalArgumentException(
			    "The validation token does not identify this " + this.getClass().getName());
		}

		// clear the exception lists
		if (token == null) {
			deactivatedLookupList.clear();
			deactivatedViewList.clear();
		}
		deactivatedLookupList.addAll(migExLookupList);
		deactivatedViewList.addAll(migExViewList);
		
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		SAFRValidationToken safrValidationToken; // token used to return to
		// caller
		List<LRField> activeItemList = lrFields.getActiveItems();
		// to check view and lookup dependencies of deleted fields.
		if (this.id > 0) {
			List<Integer>  fieldsToBeDeleted = new ArrayList<Integer>();
			for (LRField lrField : lrFields) {
				if (lrField.getPersistence() == SAFRPersistence.DELETED) {
					fieldsToBeDeleted.add(lrField.getId());
				}
			}
			if (fieldsToBeDeleted.size() > 0) {

				String dependencies = "";
				// a map to allow only unique components to be returned in error
				// message
				Map<Integer, Integer> allowUniqueMap = new HashMap<Integer, Integer>();
				Map<Integer, List<DependentComponentTransfer>> dependentLookupComps;

				// Lookups dependencies
				dependentLookupComps = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().
				    getFieldLookupDependencies(getEnvironmentId(), fieldsToBeDeleted);
				if (!dependentLookupComps.isEmpty()) {
					dependencies += "LOOKUP PATHS :" + SAFRUtilities.LINEBREAK;
					List<DependentComponentTransfer> depCompTransfers;
					int i = 0;
					for (Integer fieldId : dependentLookupComps.keySet()) {
						i++;
						depCompTransfers = dependentLookupComps.get(fieldId);
						dependencies += "    " + i + ". Field ID[" + fieldId + "]" + SAFRUtilities.LINEBREAK;
						for (DependentComponentTransfer dep : depCompTransfers) {
							Integer lookupID = dep.getId();
							String lookupName = dep.getName();
							if (!allowUniqueMap.containsKey(lookupID)) {
								// add to message only if not previously added.
								dependencies += "        ->" + lookupName + " [" + lookupID + "]" + SAFRUtilities.LINEBREAK;
								allowUniqueMap.put(lookupID, lookupID);
							}
						}
					}
				}

				// views dependencies
				Map<Integer, List<DependentComponentTransfer>> dependentViewComps;
				dependentViewComps = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().
				    getFieldViewDependencies(getEnvironmentId(), fieldsToBeDeleted);
				allowUniqueMap.clear();
				if (!dependentViewComps.isEmpty()) {
					dependencies += "VIEWS :" + SAFRUtilities.LINEBREAK;
					List<DependentComponentTransfer> depCompTransfers;
					int i = 0;
					for (Integer fieldId : dependentViewComps.keySet()) {
						i++;
						depCompTransfers = dependentViewComps.get(fieldId);
						dependencies += "    " + i + ". Field ID[" + fieldId + "]" + SAFRUtilities.LINEBREAK;
						for (DependentComponentTransfer dep : depCompTransfers) {
							Integer viewID = dep.getId();
							String viewName = dep.getName();
							if (!allowUniqueMap.containsKey(viewID)) {
								// add to message only if not previously added.
								dependencies += "        ->" + viewName + " [" + viewID + "]" + SAFRUtilities.LINEBREAK;
								allowUniqueMap.put(viewID, viewID);
							}
						}
					}
				}
				if (dependencies != "") {
					for (SAFRPersistentObject lrField : lrFields) {
						if ((dependentLookupComps.containsKey(((SAFRComponent) lrField).getId())) || 
						    (dependentViewComps.containsKey(((SAFRComponent) lrField).getId()))) {
							if (lrField.getPersistence() == SAFRPersistence.DELETED) {
								lrField.setPersistence(SAFRPersistence.OLD);
							}
						}
					}
					safrValidationToken = new SAFRValidationToken(this,SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR);
					safrValidationException.setSafrValidationToken(safrValidationToken);
					safrValidationException.setErrorMessage(Property.VIEW_LOOKUP_DEP, dependencies);
					throw safrValidationException;
				}
			}

			// for checking dependencies of associated logical files
			List<ComponentAssociation> deletedItems = new ArrayList<ComponentAssociation>();
			for (SAFRPersistentObject item : logicalFileAssociations) {
				if (item.getPersistence() == SAFRPersistence.DELETED) {
					deletedItems.add((ComponentAssociation) item);
				}
			}
			removeAssociations(deletedItems, true);
		}

		if ((getName() == null) || (getName() == "")) {
			safrValidationException.setErrorMessage(Property.LR_NAME,
					"Logical Record name cannot be empty.");
		} else {
			if (getName().length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.LR_NAME,
					"The length of Logical Record name "
	        		+ ModelUtilities.formatNameForErrMsg(
				    getName(),(isForImport() || isForMigration()))												
					+ "cannot exceed 48 characters.");
			}
			else if (this.isDuplicate()) {
				safrValidationException.setErrorMessage(Property.LR_NAME,
				    "The Logical Record name '"+ getName()+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(getName())) {
                safrValidationException.setErrorMessage(Property.LR_NAME,
                    "The Logical Record name " + 
                    ModelUtilities.formatNameForErrMsg(getName(),(isForImport() || isForMigration())) + 
                    "should begin with a letter and should comprise of letters" + 
                    ", numbers, pound sign (#) and underscores only.");
            }
		}
		if (getLRTypeCode() == null) {
			safrValidationException.setErrorMessage(Property.TYPE, "Type cannot be empty.");
		}

		if (getLRStatusCode() == null) {
			safrValidationException.setErrorMessage(Property.STATUS, "Status cannot be empty.");
		}

		try {
			if (this.getLookupExitRoutine() != null && 
			    this.getLookupExitRoutine().getTypeCode().getGeneralId() != Codes.LOOKUP) {
				safrValidationException.setErrorMessage(Property.LOOKUP_USER_EXIT_ROUTINE,
				    "The user exit routine '" + this.lookupExitRoutine.getName() + "[" + 
				    this.lookupExitRoutine.getId() + "]' is not of type 'Lookup'. Please select a valid user exit routine.");
			}
		} catch (SAFRNotFoundException snfe) {
			// CQ 8596. Nikita. 11/10/2010
			// Don't allow user to save if the UXR is invalid
			safrValidationException.setErrorMessage(Property.LOOKUP_USER_EXIT_ROUTINE, 
			    "The user exit routine with id [" + snfe.getComponentId() + 
			    "] does not exist. Please select a valid user exit routine.");
		}

		if (getPrimayKeyLength() > 256) {
			safrValidationException.setErrorMessage(Property.LR_FIELD, 
			    "Total key length should not be greater than 256.");
		}
		if (!isPrimaryKeyInSequence()) {
			safrValidationException.setErrorMessage(Property.LR_FIELD, 
			    "The LR primary keys are not in sequence .Please define keys in sequence starting with 1.");
		}

		if (!safrValidationException.getErrorMessages().isEmpty()) {
			safrValidationToken = new SAFRValidationToken(this,SAFRValidationType.ERROR);
			safrValidationException.setSafrValidationToken(safrValidationToken);
			throw safrValidationException;
		} else {
			if (token == null || 
			    (token != null && 
			    ( (token.getValidationFailureType() != SAFRValidationType.WARNING)) && 
			      (token.getValidationFailureType() != SAFRValidationType.DEPENDENCY_LR_WARNING) )) {

				int lrFieldCount = 0;
				for (SAFRPersistentObject lrField : activeItemList) {
					try {
						lrFieldCount++;
						((LRField) lrField).validateWarning();
					} catch (SAFRValidationException sve) {
						safrValidationException.setErrorMessages(Property.LR_FIELD, sve.getErrorMessages());
					}
				}
				if (lrFieldCount == 0) {
					safrValidationException.setErrorMessage(Property.LR_FIELD, 
					    "There should be at least one LR Field.");
				}
				if (getLogicalFileAssociations().getActiveItems().isEmpty()) {
					safrValidationException.setErrorMessage(Property.LF_ASSOCIATION, 
					    "There must be at least one corresponding file.");
				}
				if (!isPrimaryKeyFieldValid()) {
					safrValidationException.setErrorMessage(Property.LR_FIELD,
					    "A field with a primary key is required before a field with an alternate key is created.");
				}

				if (getDuplicateLRFieldNameList() != null) {
					ArrayList<String> duplicateNames = this.getDuplicateLRFieldNameList();
					String errorMsg = "";
					for (String duplicateName : duplicateNames) {
						errorMsg = errorMsg + " '" + duplicateName + "'";
					}
					safrValidationException.setErrorMessage(Property.LR_FIELD, 
					    "Field Name must be unique : " + errorMsg + " already exists.");
				}

		        checkLRFields(safrValidationException);
				
				if (!safrValidationException.getErrorMessages().isEmpty()) {
					safrValidationToken = new SAFRValidationToken(this,SAFRValidationType.WARNING);
					safrValidationException.setSafrValidationToken(safrValidationToken);
					throw safrValidationException;
				}
			}
			// check for LR dependency
			// check for only saved LRs.
			if (this.id > 0) {
				String lrDependencies = "";
				List<DependentComponentTransfer> dependentComps;
				Map<Integer, String> depsIdName = new HashMap<Integer, String>();
				List<Integer> dependentLookupIds = new ArrayList<Integer>();

				Map<ComponentType, List<DependentComponentTransfer>> dependencyMap = 
				    new TreeMap<ComponentType, List<DependentComponentTransfer>>();

				// Lookups first
				if (checkLookupDependencies) {
					dependentLookupIds = new ArrayList<Integer>();
					// retrieve only those lookups not in the exception list
					dependentComps = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().getLRLookupDependencies(
					    getEnvironmentId(), this.id, deactivatedLookupList);
					if (!dependentComps.isEmpty()) {
						dependencyMap.put(ComponentType.LookupPath, dependentComps);
						safrValidationException.setDependencies(dependencyMap);
						lrDependencies = "Lookup Paths" + SAFRUtilities.LINEBREAK;
						for (DependentComponentTransfer dep : dependentComps) {
							Integer lookupID = dep.getId();
							dependentLookupIds.add(lookupID);
							String lookupName = dep.getName();
							depsIdName.put(lookupID, lookupName);
							// update the exception list
							deactivatedLookupList.add(lookupID);
						}
					}
					for (Integer lkupId : depsIdName.keySet()) {
						lrDependencies += "    " + depsIdName.get(lkupId) + " [" + lkupId + "]" + SAFRUtilities.LINEBREAK;
					}
				}

				// Views
				if (checkViewDependencies) {
					depsIdName.clear();
					// retrieve only those views not in the exception list
					dependentComps = DAOFactoryHolder.getDAOFactory().getLogicalRecordDAO().getLRViewDependencies(
					    getEnvironmentId(), this.id, dependentLookupIds, deactivatedViewList);
					if (!dependentComps.isEmpty()) {
						dependencyMap.put(ComponentType.View, dependentComps);
						safrValidationException.setDependencies(dependencyMap);
						lrDependencies += "Views" + SAFRUtilities.LINEBREAK;
						for (DependentComponentTransfer dep : dependentComps) {
							Integer viewID = dep.getId();
							String viewName = dep.getName();
							depsIdName.put(viewID, viewName);
							// update the exception list
							deactivatedViewList.add(viewID);
						}
					}
					for (Integer vwId : depsIdName.keySet()) {
						lrDependencies += "    " + depsIdName.get(vwId) + " [" + vwId + "]" + SAFRUtilities.LINEBREAK;
					}
				}

				if (!lrDependencies.equals("")) {
					safrValidationToken = new SAFRValidationToken(this, SAFRValidationType.DEPENDENCY_LR_WARNING);
					safrValidationException.setSafrValidationToken(safrValidationToken);
					safrValidationException.setErrorMessage(Property.VIEW_LOOKUP_DEP, lrDependencies);
					throw safrValidationException;
				}
			}
		}
	}

    public Set<Integer> getMigExLookupList() {
        return migExLookupList;
    }

    public Set<Integer> getMigExViewList() {
        return migExViewList;
    }
	
    protected void checkLRFields(SAFRValidationException safrValidationException)
        throws SAFRException, DAOException {
        
        List<LRField> activeItemList = lrFields.getActiveItems();
        
        // To check errors for LR Field
		for (LRField lrField : activeItemList) {
			try {
				lrField.validateError();
			} catch (SAFRValidationException sve) {
				safrValidationException.setErrorMessages(
				    Property.LR_FIELD, sve.getErrorMessages());
			}
		}
		
		// check child exceed parent
        for (LRField parent : activeItemList) {
            
            // validate parent child relationships
            List<LRField> children = parent.getChildren();
            if (children.size() > 0) {
                // determine outer child boundaries
                for (LRField child : children) {
                    if (child.getPosition() < parent.getPosition()) {
                        String message = 
                            "Position of " + child.getDescriptor() + 
                            " starts before parent field " + parent.getDescriptor();
                        safrValidationException.setErrorMessage(Property.LR_FIELD, message);                        
                    }
                    int pend = parent.getPosition() + parent.getLength();
                    int cend = child.getPosition() + child.getLength();
                    if (cend > pend) {
                        String message = 
                            "Position of " + child.getDescriptor() + 
                            " ends after parent field " + parent.getDescriptor();
                        safrValidationException.setErrorMessage(Property.LR_FIELD, message);                        
                    }
                }
            }            
        }
		
        // check field within previous but no redefine
        int curPos = 0;
        for (LRField curField : activeItemList) {
            if (curPos == 0) {
                curPos++;
                continue;
            }            
            checkPrevFields(safrValidationException, activeItemList, curPos, curField);            
            curPos++;
        }
        
    }

    protected void checkPrevFields(SAFRValidationException safrValidationException, List<LRField> activeItemList,
        int curPos, LRField curField) {
        // loop through previous fields 
        for (int i = curPos-1 ; i>=0 ; i--) {
            LRField prevField = activeItemList.get(i);
            if (prevField.getPosition() == null ||
                curField.getPosition() == null) {
                continue;
            }
            
            // if cur field encompassed by previous field then it should be a child
            int cend = curField.getPosition() + curField.getLength();
            int pend = prevField.getPosition() + prevField.getLength();
            // check if field within previous field
            if (curField.getPosition() >=  prevField.getPosition() && 
                cend <= pend) {
                // check correct redefine
                if (curField.getRedefine() == null ||
                    !curField.getRedefine().equals(prevField.getId())) {
                    String message = 
                        prevField.getDescriptor() + 
                        " should be a parent of " + curField.getDescriptor() +
                        ", please use Regenerate Redefines to fix";
                    safrValidationException.setErrorMessage(Property.LR_FIELD, message);
                }
                break;
            }                
        }
    }

	/**
	 * This method is used to check whether the Logical Record name already
	 * exist in the workbench.
	 * 
	 * @return True if Component with given name exists in database.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private boolean isDuplicate() throws DAOException, SAFRException {
		LogicalRecordTransfer logicalRecordTransfer = null;
		logicalRecordTransfer = DAOFactoryHolder.getDAOFactory()
				.getLogicalRecordDAO().getDuplicateLogicalRecord(getName(),
						getId(), getEnvironmentId());

		if (logicalRecordTransfer == null) {
			return false;
		} else {
			return true;
		}
	}

	private boolean isPrimaryKeyInSequence() {
		TreeMap<Integer, LRField> treeMap = new TreeMap<Integer, LRField>();
		List<LRField> activeItemList = lrFields.getActiveItems();
		for (LRField lrf : activeItemList) {
			if (lrf.getPkeySeqNo() > 0) {
				if (treeMap.containsKey(lrf.getPkeySeqNo())) {
					return false;
				}
				treeMap.put(lrf.getPkeySeqNo(), lrf);
			}
		}
		int sequenceNo = 0;
		for (int i = 0; i < treeMap.size(); i++) {
			sequenceNo++;
			if (!treeMap.containsKey(sequenceNo)) {
				return false;
			}
		}

		return true;

	}

	/**
	 * This method is used to check whether a field is created as primary key
	 * before any field with an alternate key is created.
	 * 
	 * @return true if validation check is met.
	 */
	private boolean isPrimaryKeyFieldValid() {
		boolean flagStartDate = false;
		boolean flagEndDate = false;
		boolean flagPrimaryKey = false;
		List<LRField> activeItemList = lrFields.getActiveItems();
		for (LRField lrf : activeItemList) {
			if (lrf.getKeyType() == LRFieldKeyType.EFFENDDATE) {
				flagEndDate = true;
			}
			if (lrf.getKeyType() == LRFieldKeyType.EFFSTARTDATE) {
				flagStartDate = true;
			}
			if (lrf.getKeyType() == LRFieldKeyType.PRIMARYKEY) {
				flagPrimaryKey = true;
			}
		}
		if ((flagEndDate && flagPrimaryKey)
				|| (flagStartDate && flagPrimaryKey)) {
			return true;
		}
		if (!flagEndDate && !flagPrimaryKey && !flagStartDate) {
			return true;
		}
		if ((!flagEndDate || !flagStartDate) && flagPrimaryKey) {
			return true;
		}

		return false;
	}

	private ArrayList<String> getDuplicateLRFieldNameList() {
		Map<String, LRField> lrFieldsMap = new HashMap<String, LRField>();
		ArrayList<String> duplicateNames = new ArrayList<String>();
		List<LRField> activeItemList = lrFields.getActiveItems();
		for (LRField lrf : activeItemList) {
		    String uFldName = lrf.getName() == null ? "" : lrf.getName().toUpperCase();
			if (lrFieldsMap.containsKey(uFldName)) {
				duplicateNames.add(lrf.getName());
			}
			if (!uFldName.isEmpty()) {
				lrFieldsMap.put(uFldName, lrf);
			}
		}
		if (duplicateNames.isEmpty()) {
			return null;
		} else {
			return duplicateNames;
		}
	}

	/*
	 * Returns true if this LR has any primary key, effective start date or
	 * effective end date fields, otherwise false.
	 */
	private boolean hasIndexFields() {
		boolean result = false;
		for (LRField lrf : lrFields) {
			if (lrf.getKeyType() != LRFieldKeyType.NONE) {
				result = true;
				break;
			}
		}
		return result;
	}

	private class LRIndex {
		private Integer lrIndexId;
		private Date createTime;
		private String createBy;
		private Date modifyTime;
		private String modifyBy;
		private List<LRIndexField> persistentLrIndexFields = new ArrayList<LRIndexField>();

		LRIndex(Integer lrIndexId) {
			this.lrIndexId = lrIndexId;
		}
		
		LRIndex(LRIndexTransfer transfer) throws SAFRException {
			this.lrIndexId = transfer.getId();
			this.createTime = transfer.getCreateTime();
			this.createBy = transfer.getCreateBy();
			this.modifyTime = transfer.getModifyTime();
			this.modifyBy = transfer.getModifyBy();
			
			// Retrieve the LR index fields
			try {
				List<LRIndexFieldTransfer> lrIndexFieldTransfers = DAOFactoryHolder.getDAOFactory()
						.getLogicalRecordDAO().getLRIndexFields(lrIndexId,
								getEnvironmentId());

				if (!lrIndexFieldTransfers.isEmpty()) {
					for (LRIndexFieldTransfer lrifTrans : lrIndexFieldTransfers) {
						persistentLrIndexFields.add(new LRIndexField(this, lrifTrans));
					}
				}
			} catch (DAOException de) {
				throw new SAFRException(
						"Data access error for Logical Record Index Fields", de);
			}
		}

		private Integer getEnvId() {
			return getEnvironmentId();
		}

		private Integer getLrId() {
			return getId();
		}

		private Integer getEffStartDateFieldId() {
			Integer id = 0;
			for (LRField lrf : lrFields) {
				if (lrf.getKeyType() == LRFieldKeyType.EFFSTARTDATE) {
					id = lrf.getId();
				}
			}
			return id;
		}

		private Integer getEffEndDateFieldId() {
			Integer id = 0;
			for (LRField lrf : lrFields) {
				if (lrf.getKeyType() == LRFieldKeyType.EFFENDDATE) {
					id = lrf.getId();
				}
			}
			return id;
		}

		/*
		 * Return a List of LRIndexFields sorted by LR field pkey sequence no.
		 * For each pkey LR field, check if a matching LR index field already
		 * exists and use it, otherwise instantiate a new one.
		 */
		private List<LRIndexField> getLRIndexFields() {
			SortedMap<Integer, LRIndexField> lrIndexFieldMap = new TreeMap<Integer, LRIndexField>();
			for (LRField lrf : lrFields.getActiveItems()) {
				if (lrf.getKeyType() == LRFieldKeyType.PRIMARYKEY) {
					LRIndexField lrIndexField = null;
					for (LRIndexField lrif : persistentLrIndexFields) {
						if (lrf.equals(lrif.lrField)) {
							// use the existing LR index field
							lrIndexField = lrif;
							break;
						}
					}
					if (lrIndexField == null) {
						// create a new LR index field
						lrIndexField = new LRIndexField(this, lrf);
					}
					lrIndexFieldMap.put(lrf.getPkeySeqNo(), lrIndexField);
				}
			}
			List<LRIndexField> lrIndexFields = new ArrayList<LRIndexField>();
			lrIndexFields.addAll(lrIndexFieldMap.values());
			return lrIndexFields;
		}

	}

	private class LRIndexField {
		private LRIndex lrIndex;
		private LRField lrField;
		private Integer lrIndexFldId;
		private Date createTime;
		private String createBy;
		private Date modifyTime;
		private String modifyBy;

		LRIndexField(LRIndex lrIndex, LRField lrField) {
			this.lrIndex = lrIndex;
			this.lrField = lrField;
			this.lrIndexFldId = 0;
		}

		LRIndexField(LRIndex lrIndex, LRIndexFieldTransfer lrifTrans) {
			this.lrIndex = lrIndex;
			this.lrIndexFldId = lrifTrans.getAssociationId();
			this.createTime = lrifTrans.getCreateTime();
			this.createBy = lrifTrans.getCreateBy();
			this.modifyTime = lrifTrans.getModifyTime();
			this.modifyBy = lrifTrans.getModifyBy();
			
			for (LRField lrf : lrFields) {
				if (lrf.getId().equals(lrifTrans.getAssociatedComponentId())) {
					this.lrField = lrf;
					break;
				}
			}
		}

		private Integer getEnvId() {
			return getEnvironmentId();
		}

		private Integer getLrIndexId() {
			return lrIndex.lrIndexId;
		}

		private Integer getFieldSeqNo() {
			return lrField.getPkeySeqNo();
		}

		private Integer getFieldId() {
			return lrField.getId();
		}

	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {

		if (hasCreatePermission()) {
			LogicalRecord logicalRecordCopy = SAFRApplication.getSAFRFactory()
					.createLogicalRecord();

			// copy the general properties...
			logicalRecordCopy.setName(newName);
			logicalRecordCopy.setComment(this.getComment());
			logicalRecordCopy.setActive(false);
			logicalRecordCopy.setLookupExitId(this.lookupExitId);
			logicalRecordCopy.setLookupExitParams(this.lookupExitParams);
			logicalRecordCopy.setLookupExitRoutine(this.lookupExitRoutine);
			logicalRecordCopy.setLRTypeCode(this.lrTypeCode);

			Map<Integer,Integer> fldMap = new HashMap<Integer,Integer>();
			// copy the LR fields...
			for (LRField lrField : this.lrFields) {

				LRField lrField1 = logicalRecordCopy.addField();
				fldMap.put(lrField.getId(), lrField1.getId());
				lrField1.setComment(lrField.getComment());
				lrField1.setDatabaseColumnName(lrField.getDatabaseColumnName());
				lrField1.setDataTypeCode(lrField.getDataTypeCode());
				lrField1.setDateTimeFormatCode(lrField.getDateTimeFormatCode());
				lrField1.setDecimals(lrField.getDecimals());
				lrField1.setDefaultValue(lrField.getDefaultValue());
				lrField1.setPositionSimple(lrField.getPosition());
				lrField1.setHeaderAlignmentCode(lrField
						.getHeaderAlignmentCode());
				lrField1.setHeading1(lrField.getHeading1());
				lrField1.setHeading2(lrField.getHeading2());
				lrField1.setHeading3(lrField.getHeading3());
				lrField1.setKeyType(lrField.getKeyType());
				lrField1.setLengthSimple(lrField.getLength());
				lrField1.setName(lrField.getName());
				lrField1.setNumericMaskCode(lrField.getNumericMaskCode());
				lrField1.setOrdinalOffset(lrField.getOrdinalOffset());
				lrField1.setOrdinalPosition(lrField.getOrdinalPosition());
				lrField1.setPkeySeqNo(lrField.getPkeySeqNo());
				lrField1.setScaling(lrField.getScaling());
				lrField1.setSigned(lrField.isSigned());
				lrField1.setSortKeyLabel(lrField.getSortKeyLabel());
				lrField1.setSubtotalLabel(lrField.getSubtotalLabel());

			}
			// Copying the LF associations.
			for (ComponentAssociation assoc : this.logicalFileAssociations
					.getActiveItems()) {
				ComponentAssociation fileAssoc = new ComponentAssociation(
						logicalRecordCopy, assoc.getAssociatedComponentIdNum(),
						assoc.getAssociatedComponentName(), assoc
								.getAssociatedComponentRights());

				logicalRecordCopy.addAssociatedLogicalFile(fileAssoc);
			}

			logicalRecordCopy.autocalcRedefine();
			logicalRecordCopy.validate();
			logicalRecordCopy.store();

			return logicalRecordCopy;
		} else {
			throw new SAFRException("The user is not authorized to create a Logical Record.");
		}
	}

	public List<String> getLoadWarnings() {
		return loadWarnings;
	}

	public void setModelTransferProvider(ModelTransferProvider provider) {
		this.provider = provider;
	}
	
    public boolean autocalcRedefine() {
        
        List<LRField> flds = getLRFields().getActiveItems();
        
        boolean changed = false;
        LRField prevField = null;
        for (LRField fld : flds) {
            if (fld.getPosition() == null) {
                continue;
            }
            boolean fldChanged = fld.autocalcRedefineField(prevField);
            if (fldChanged) {
                changed = true;
            }
            prevField = fld;
        }
        return changed;
    }
    
    public Set<Integer> getDeactivatedViews() {
        return deactivatedViewList;        
    }

    public Set<Integer> getDeactivatedLookups() {
        return deactivatedLookupList;        
    }

	public boolean hasLookupExit() {
		//There seem to be two things that determine this
		//The id and the object...?
		//Why isn't this just set to 0 at start rather than leaving it null?
		return (lookupExitId != null && lookupExitId > 0) ? true : false;
	}
    
}
