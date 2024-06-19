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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.FileAssociationTransfer;
import com.ibm.safr.we.data.transfer.LogicalFileTransfer;
import com.ibm.safr.we.exceptions.SAFRDependencyException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.ComponentAssociation;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.base.SAFRPersistentObject;

/**
 * Represents a SAFR Logical File.
 * 
 */
public class LogicalFile extends SAFREnvironmentalComponent {

	private SAFRAssociationList<ComponentAssociation> logicalRecordAssociations;
	private SAFRAssociationList<FileAssociation> physicalFileAssociations;
	// this map contains the dependencies of the Physical file to views. Key is
	// the association id and the value is the dependent component transfer
	// object.
	Map<Integer, List<DependentComponentTransfer>> existingDependentComponents = new HashMap<Integer, List<DependentComponentTransfer>>();

	/**
	 * This constructor is used when defining a new Logical File in the
	 * application. It will initialize the Logical File ID to zero and
	 * EnvironmentId to specified EnvironmentId to which Logical File belongs
	 * to. The Logical File ID will be reset automatically to a unique value
	 * when the Logical File object is persisted via its <code>store()</code>
	 * method.
	 */
	LogicalFile(Integer environmentId) {
		super(environmentId);
		logicalRecordAssociations = new SAFRAssociationList<ComponentAssociation>();
		physicalFileAssociations = new SAFRAssociationList<FileAssociation>();
	}

	/**
	 * Create a Logical File object containing the data in the specified
	 * transfer object. Used to instantiate existing Logical File objects.
	 * 
	 * @param trans
	 *            the LogicalFileTransfer object
	 * @param fullInit
	 *            specify if this logical file object should be fully
	 *            initialized with all properties.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	LogicalFile(LogicalFileTransfer trans) throws DAOException, SAFRException {
		super(trans);
		logicalRecordAssociations = new SAFRAssociationList<ComponentAssociation>();
		physicalFileAssociations = new SAFRAssociationList<FileAssociation>();
		if (!trans.isForImport()) {
			// load only if already stored in DB.
			logicalRecordAssociations = SAFRAssociationFactory
					.getLogicalFileToLogicalRecordAssociations(this);
			physicalFileAssociations = SAFRAssociationFactory
					.getLogicalFileToPhysicalFileAssociations(this);
		}
	}

	/**
	 * Returns a read-only list of logical records that this logical file has
	 * been associated with. This list is immutable, so it is not possible to
	 * add or remove list elements. The mutator methods of the List interface
	 * (add, remove, etc) cannot be used.
	 * 
	 * @return a SAFRImmutableList of logical record associations.
	 */
	public SAFRImmutableList<ComponentAssociation> getLogicalRecordAssociations() {
		return new SAFRImmutableList<ComponentAssociation>(
				logicalRecordAssociations);
	}

	/**
	 * @return the associated physical file list.
	 */
	public SAFRList<FileAssociation> getPhysicalFileAssociations() {
		return physicalFileAssociations;
	}

	public boolean addAssociatedPhysicalFile(FileAssociation thisAssoc) {
		// first search if this association is already present in the list as
		// DELETED.
		// TODO SAFRList should have Add override.
		FileAssociation thatAssoc;
		boolean found = false;
		boolean result = false;
		Iterator<FileAssociation> i = physicalFileAssociations.iterator();
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
			result = physicalFileAssociations.add(thisAssoc);
			markModified();
		}
		return result;
	}

	/**
	 * This method is used to remove physical files associated to logical file.
	 * If a physical file,associated to a view, is deleted, then
	 * SAFRValidationException is thrown along with a message specifying the
	 * view dependencies of that physical file.
	 * 
	 * @param associatedPhysicalFile
	 *            : logical file to physical file association.
	 * @param safrValidationToken
	 * @throws DAOException
	 * @throws SAFRValidationException
	 *             when a dependency is found along with token set as
	 *             DEPENDENCY_WARNING.
	 */
	public void removeAssociatedPhysicalFile(
			FileAssociation associatedPhysicalFile,
			SAFRValidationToken safrValidationToken) throws DAOException,
			SAFRValidationException {

		if (safrValidationToken != null
				&& safrValidationToken.getTokenId() != this.hashCode()) {
			throw new IllegalArgumentException(
					"The validation token does not identify this "
							+ this.getClass().getName());
		}
		// generate string only if the persistence is not NEW and the token is
		// null
		if (safrValidationToken == null
				&& associatedPhysicalFile.getPersistence() != SAFRPersistence.NEW) {
			String dependencies = "";
			List<DependentComponentTransfer> depViews;

			Integer LFPFAssociationId = associatedPhysicalFile
					.getAssociationId();

			// get the list of dependent views from database.
			depViews = DAOFactoryHolder.getDAOFactory()
					.getLogicalFileDAO().getAssociatedPFViewDependencies(
							getEnvironmentId(), LFPFAssociationId);
			
			if (!depViews.isEmpty()) {

				dependencies = "Physical File: ";
				dependencies += associatedPhysicalFile
						.getAssociatedDescriptor() + SAFRUtilities.LINEBREAK;

				// Generate dependency string
	            Map<ComponentType, List<DependentComponentTransfer>> depMap = 
	                new HashMap<ComponentType, List<DependentComponentTransfer>>();
	            depMap.put(ComponentType.View, depViews);
	            // Use common logic in SAFRDependencyException to generate dependency string
	            // Should perhaps throw SAFRDependencyException instead however they don't 
	            // support validation tokens needed by the invoker of this method.
				SAFRDependencyException depex = new SAFRDependencyException(depMap);
				dependencies += depex.getDependencyString();
				
				for (DependentComponentTransfer depComp : depViews) {
					existingDependentComponents.put(depComp.getId(),depViews);
				}

			}
			// delete directly if no dependency.
			else {
				physicalFileAssociations.remove(associatedPhysicalFile);
				markModified();
			}

			if (!dependencies.equals("")) {
				SAFRValidationException exception = new SAFRValidationException();
				SAFRValidationToken token = new SAFRValidationToken(this,
						SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_WARNING);
				exception.setSafrValidationToken(token);
				exception.setErrorMessage(Property.PF_ASSOCIATION_DEP,
						dependencies);
				throw exception;
			}
		} else {
			// if token is not null that means user has confirmed the deletion
			// the dependency.
			physicalFileAssociations.remove(associatedPhysicalFile);
			markModified();
		}

	}

	@Override
	public void store() throws SAFRException, DAOException {
		if (isForMigration()) {
			if (!SAFRApplication.getUserSession().isAdminOrMigrateInUser(getEnvironmentId())) {
				String msg = "The user is not authorized to migrate into Environment "+ getEnvironmentId();
				throw new SAFRException(msg);
			}
		} else {
			if (this.id == 0) {
				if (!hasCreatePermission()) {
					throw new SAFRException("The user is not authorized to create a new logical file.");
				}
			} else {
				if (!hasUpdateRights()) {
					throw new SAFRException("The user is not authorized to update this logical file.");
				}
			}
		}
		
		if (!isForMigration()) {
			// if user is not a System Admin or Environment Admin then check edit
			// rights on the components which are used in the Logical File.
			if (!SAFRApplication.getUserSession().isSystemAdministrator()) {
				String messageString = "The user does not have necessary edit rights on the following Physical File(s)." + SAFRUtilities.LINEBREAK;
				String compListString = "";
				for (FileAssociation fileAssoc : this.getPhysicalFileAssociations()) {
					if (fileAssoc.getPersistence().equals(SAFRPersistence.NEW)) {
						if (SAFRApplication.getUserSession().getEditRights(
								ComponentType.PhysicalFile,
								fileAssoc.getAssociatedComponentIdNum(),
								this.getEnvironmentId()) == EditRights.None) {
						    
							compListString += "    "+ fileAssoc.getAssociatedComponentName()
								+ " ["+ fileAssoc.getAssociatedComponentIdNum() + "]" + SAFRUtilities.LINEBREAK;
						}
					}
				}
				if (!compListString.equals("")) {
					// no rights on associated PF
					throw new SAFRException(messageString + compListString);
				}
			}
		} 
		
		List<SAFRPersistentObject> savedObjs = new ArrayList<SAFRPersistentObject>();
		LogicalFileTransfer trans = new LogicalFileTransfer();
		setTransferData(trans);

		// CQ 7329 Kanchan Rauthan 04/03/2010 To show error if logical file is
		// already deleted from database and user still tries to save it.
		boolean success = false;
		try {

			while (!success) {
				try {
					// CQ 7826 Santhosh 26/05/2010 Implement 2-phase commit DB
					// transactionality
					// Begin Transaction
					DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
					trans = DAOFactoryHolder.getDAOFactory().getLogicalFileDAO().persistLogicalFile(trans);
					setObjectData(trans);
					savedObjs.add(this);

                    if (!isForMigration() && !SAFRApplication.getUserSession().isSystemAdministrator()) {
                        SAFRApplication.getUserSession().getGroup().assignComponentFullRights(
                            this, ComponentType.LogicalFile);
                    }
                    
					// save associated PFs
					
					// extract PFs to be deleted
					List<Integer> deletionIds = new ArrayList<>(); 
					for (FileAssociation association : physicalFileAssociations) {
						if (association.getPersistence() == SAFRPersistence.DELETED) {
							deletionIds.add(association.getAssociationId());
						}
					}
					
					// resequence in PF ID order
				    SortedMap<Integer, FileAssociation> assocMapPf = new TreeMap<Integer, FileAssociation>();
					for (FileAssociation assoc : physicalFileAssociations.getActiveItems()) {
					    assocMapPf.put(assoc.getAssociatedComponentIdNum(),assoc);
					}
					int counter = 1;
					for (FileAssociation assoc : assocMapPf.values()) {
						if (counter != assoc.getSequenceNo()) {
							assoc.setSequenceNo(counter);
						}
						counter++;
					}
					
					// prepare to store PF associations
					List<FileAssociationTransfer> daoList = new ArrayList<FileAssociationTransfer>();
					HashMap<FileAssociationTransfer, FileAssociation> map = new HashMap<FileAssociationTransfer, FileAssociation>();
					for (FileAssociation association : physicalFileAssociations.getActiveItems()) {
						// create transfer object
						FileAssociationTransfer assocTrans = new FileAssociationTransfer();
						association.setTransferFromObject(assocTrans);
						// CQ 9142. Nikita. 23/02/2011
						// Associating component id should not be set as 0
						// (0 id is for NEW associations only)
						if (assocTrans.getAssociatingComponentId() == 0) {
							assocTrans.setAssociatingComponentId(this.getId());
							assocTrans.setAssociatingComponentName(this.getName());
						}
						// Only store NEW or MODIFIED associations
						if (association.getPersistence() != SAFRPersistence.OLD) {
							daoList.add(assocTrans);
							map.put(assocTrans, association);
						}
					}

					// call DAO to delete
					if (deletionIds.size() > 0) {
						DAOFactoryHolder.getDAOFactory().getLogicalFileDAO()
								.deleteAssociatedPFs(getEnvironmentId(), deletionIds);
					}

					// call DAO to add/update
					if (daoList.size() > 0) {
						daoList = DAOFactoryHolder.getDAOFactory()
								.getLogicalFileDAO().persistAssociatedPFs(daoList,id);
						for (FileAssociationTransfer assocTrans : daoList) {
							FileAssociation fileAssoc = map.get(assocTrans);
							fileAssoc.setObjectFromTransfer(assocTrans);
							savedObjs.add(fileAssoc);
						}
					}

					physicalFileAssociations.flushDeletedItems();

			        SAFRApplication.getModelCount().incCount(this.getClass(), 1);
			        SAFRApplication.getModelCount().incCount(FileAssociation.class, physicalFileAssociations.size());
					
					success = true;

				} catch (DAOUOWInterruptedException e) {
					// UOW interrupted so retry it
					if (DAOFactoryHolder.getDAOFactory().getDAOUOW()
							.isMultiComponentScope()) {
						throw e;
					} else {
						continue;
					}
				}

			} // end while(!success)
		} catch (SAFRNotFoundException snfe) {
			throw new SAFRException("The logical file with id "+ this.getId()
				+ " cannot be updated as its already been deleted from the database.",snfe);
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

	/**
	 * This enum maintains the properties of logical file.
	 * 
	 */
	public enum Property {
		NAME, COMMENT, PF_ASSOCIATION, PF_ASSOCIATION_DEP, PF_ASSOCIATION_DEP_IMPORT, LP_ASSOCIATION
	}

	/**
	 * Validate method is used to validate a Logical File object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the logical file, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRException, DAOException {
		SAFRValidator safrValidator = new SAFRValidator();

		SAFRValidationException safrValidationException = new SAFRValidationException();
		String dependencyMessage = getDeletedAssociationDependencyString();
		if (!dependencyMessage.equals("")) {
			SAFRValidationToken token = new SAFRValidationToken(this,
					SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_ERROR);
			safrValidationException.setSafrValidationToken(token);
			safrValidationException.setErrorMessage(
					Property.PF_ASSOCIATION_DEP, dependencyMessage);
			throw safrValidationException;
		}

        dependencyMessage = this.getLookupDependencyString();
        if (!dependencyMessage.equals("")) {
            SAFRValidationToken token = new SAFRValidationToken(
                this,SAFRValidationType.DEPENDENCY_LOOKUP_ERROR);
            safrValidationException.setSafrValidationToken(token);            
            safrValidationException.setErrorMessage(Property.LP_ASSOCIATION,dependencyMessage);
            throw safrValidationException;
        }		
		
		if ((this.getName() == null) || (this.getName() == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"Logical File name cannot be empty.");
		} else {
			if (this.getName().length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of Logical File name "
		        		+ ModelUtilities.formatNameForErrMsg(
					    getName(),(isForImport() || isForMigration()))						
						+ "cannot exceed 48 characters.");
			} else if (this.isDuplicate()) {
				safrValidationException
						.setErrorMessage(
								Property.NAME,
								"The Logical File name '"
										+ getName()
										+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(getName())) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The Logical File name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),(isForImport() || isForMigration()))
                                        + "should begin "
                                        + "with a letter and should comprise of letters"
                                        + ", numbers, pound sign (#) and underscores only.");
            }
		}
		if ((this.getComment() != null)
				&& (this.getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH)) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment cannot be more than 254 characters.");
		}
		if ((this.physicalFileAssociations == null)
				|| (this.physicalFileAssociations.getActiveItems().size() <= 0)) {
			safrValidationException.setErrorMessage(Property.PF_ASSOCIATION,
					"There must be at least one corresponding physical file.");
		}

		if (!safrValidationException.getErrorMessages().isEmpty()) {
			SAFRValidationToken token = new SAFRValidationToken(this,
					SAFRValidationType.ERROR);
			safrValidationException.setSafrValidationToken(token);
			throw safrValidationException;
		}
	}

	private boolean isDuplicate() throws DAOException, SAFRException {
		LogicalFileTransfer logicalFileTransfer = null;
		logicalFileTransfer = DAOFactoryHolder.getDAOFactory()
				.getLogicalFileDAO().getDuplicateLogicalFile(getName(),
						getId(), getEnvironmentId());

		if (logicalFileTransfer == null) {
			return false;
		} else {
			return true;
		}
	}

    private String getLookupDependencyString() throws DAOException {
        String dependencies = "";
        
        // check for more than one PF association
        if (physicalFileAssociations.getActiveItems().size() > 1) {
            // means we cannot be the target of any lookup path steps
            List<DependentComponentTransfer> depComps;
            depComps = DAOFactoryHolder.getDAOFactory().getLogicalFileDAO().
                getAssociatedLookupDependencies(getEnvironmentId(), id);
            
            if (!depComps.isEmpty()) {
                dependencies = "Lookup Paths: " + SAFRUtilities.LINEBREAK;
                for (DependentComponentTransfer depComp : depComps) {
                    dependencies += "    " + depComp.getName()
                        + " [" + depComp.getId() + "]" + SAFRUtilities.LINEBREAK;
                }
            }            
        }
        return dependencies;
    }
	
	private String getDeletedAssociationDependencyString() {
		String dependencies = "";
		Map<Integer, List<DependentComponentTransfer>> dependentComponents = new HashMap<Integer, List<DependentComponentTransfer>>();
		Map<Integer, FileAssociation> temproraryMap = new HashMap<Integer, FileAssociation>();

		List<Integer> exceptionList = new ArrayList<Integer>();
		List<Integer> LFPFAssociationIds = new ArrayList<Integer>();
		// exceptionList is the list of association ids which have been
		// retrieved in previous dependency checks.
		for (Integer LFPFAssocIds : existingDependentComponents.keySet()) {
			exceptionList.add(LFPFAssocIds);
		}

		for (FileAssociation LFPFAssociation : physicalFileAssociations) {
			if (LFPFAssociation.getPersistence() == SAFRPersistence.DELETED) {
				temproraryMap.put(LFPFAssociation.getAssociationId(),
						LFPFAssociation);
				LFPFAssociationIds.add(LFPFAssociation.getAssociationId());
			}
		}
		if (LFPFAssociationIds.size() > 0) {
			try {
				// depedentComponents contains the list of dependencies of LF-PF
				// association to a view which are newly created and not
				// retrieved in previous dependency checks.
				dependentComponents = DAOFactoryHolder.getDAOFactory()
						.getLogicalFileDAO().getAssociatedPFViewDependencies(
								getEnvironmentId(), LFPFAssociationIds,
								exceptionList);
			} catch (DAOException e) {
				throw new SAFRException("Failed to getAssociatedPFViewDependencies.");
			}
		} else {
			return "";
		}

		if (!dependentComponents.isEmpty()) {
			for (Integer LFPFAssociationId : LFPFAssociationIds) {
				if (dependentComponents.containsKey(LFPFAssociationId)) {
					// get the list of dependent views from database.
					dependencies += "Physical File: ";
					FileAssociation fileAssoc = temproraryMap
							.get(LFPFAssociationId);
					fileAssoc.setPersistence(SAFRPersistence.OLD);
					dependencies += fileAssoc.getAssociatedComponentName();
					dependencies += "["
							+ fileAssoc.getAssociatedComponentIdNum() + "]" + SAFRUtilities.LINEBREAK;

					dependencies += "    VIEWS :" + SAFRUtilities.LINEBREAK;
					List<DependentComponentTransfer> depCompTransfers;
					depCompTransfers = dependentComponents
							.get(LFPFAssociationId);
					for (DependentComponentTransfer depComp : depCompTransfers) {
						dependencies += "        " + depComp.getName() + " ["
								+ depComp.getId() + "]" + SAFRUtilities.LINEBREAK;
					}
				}
			}
		}
		return dependencies;
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {
	    
        if (hasCreatePermission()) {
	    
    		LogicalFile logicalFileCopy = SAFRApplication.getSAFRFactory()
    				.createLogicalfile();
    
    		logicalFileCopy.setName(newName);
    		logicalFileCopy.setComment(this.getComment());
    
    		for (FileAssociation assoc : this.physicalFileAssociations.getActiveItems()) {
    			FileAssociation fileAssoc = new FileAssociation(logicalFileCopy,
    				assoc.getAssociatedComponentIdNum(), assoc.getAssociatedComponentName(), 
    				assoc.getAssociatedComponentRights());
    
    			logicalFileCopy.addAssociatedPhysicalFile(fileAssoc);
    		}
    
    		logicalFileCopy.validate();
    		logicalFileCopy.store();
    
    		return logicalFileCopy;
        }
        else {
            throw new SAFRException("The user is not authorized to create a Logical File.");
        }
	}
}
