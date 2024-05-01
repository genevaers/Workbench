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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SAFRPersistence;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.DAOUOWInterruptedException;
import com.ibm.safr.we.data.transfer.ViewFolderTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.associations.ViewFolderViewAssociation;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.security.UserSession;

/**
 * Represents a SAFR View Folder.A View Folder is like a directory where the
 * View definitions get stored.
 */
public class ViewFolder extends SAFREnvironmentalComponent {

	private static final Integer MAX_VIEWFOLDER_NAME_LENGTH = 32;

    private SAFRAssociationList<ViewFolderViewAssociation> viewAssociations = new SAFRAssociationList<ViewFolderViewAssociation>();
	
	/**
	 * This constructor is called when a View Folder is created for the first
	 * time. A View Folder is dependent on the environment.It will initialize
	 * the ViewFolder ID to zero and EnvironmentId to specified EnvironmentId to
	 * which view folder belongs. The ID will be reset automatically to a unique
	 * value when the ViewFolder object is persisted via its
	 * <code>store()</code> method.
	 */
	public ViewFolder(Integer environmentId) {
		super(environmentId);
	}

	/**
	 * This constructor is called for an already created View Folder to transfer
	 * data using a ViewFolder Transfer object.
	 */
	ViewFolder(ViewFolderTransfer trans) {
		super(trans);
        if (!trans.isForImport()) {
            // load only if already stored in DB.
            viewAssociations = SAFRAssociationFactory.getViewFolderToViewAssociations(this);
        }
		
	}

	@Override
	public void store() throws SAFRException, DAOException {
		UserSession userSession = SAFRApplication.getUserSession();
		if (isForMigration()) {
			if (userSession.isOrdinaryUser(this.getEnvironmentId())
					&& !userSession.hasPermission(Permissions.MigrateIn,
							this.getEnvironmentId())) {
				String msg = "The user is not authorized to migrate into Environment "
						+ getEnvironmentId();
				throw new SAFRException(msg);
			}
		} else {
			// CQ 7705 Shruti Shukla(22/04/10) To implement security at model
			// layer.
			if (this.id == 0) {
				if (!hasCreatePermission()) {
					throw new SAFRException(
							"The user is not authorized to create a new view folder.");
				}
			} else {
				if (!hasUpdateRights()) {
					throw new SAFRException(
							"The user is not authorized to update this view folder.");
				}
			}
		}
		
        boolean success = false;
        try {

            while (!success) {
		
        		try {		    
                    // Begin Transaction
                    DAOFactoryHolder.getDAOFactory().getDAOUOW().begin();
                    ViewFolderTransfer trans = new ViewFolderTransfer();
                    setTransferData(trans);            
        			trans = DAOFactoryHolder.getDAOFactory().getViewFolderDAO().persistViewFolder(trans);
        			setObjectData(trans);
        
        			if (isForMigration()) {
        				if (userSession.getEditRights(ComponentType.ViewFolder,
        				    this.id, this.getEnvironmentId()) == EditRights.None) {
        					// no edit rights so assign read rights
        					userSession.getGroup().assignComponentEditRights(this,
        							ComponentType.ViewFolder, EditRights.Read);
        				}
        			} else {
        				// JAK: note, this does not replace any existing rights.
        				// CQ 7675 Nikita 16/04/2010 Assign full rights to general user
        				// on the View Folder created by him
        				if (!SAFRApplication.getUserSession().getUser().isSystemAdmin()) {
        					SAFRApplication.getUserSession().getGroup().assignComponentFullRights(
        					    this,ComponentType.ViewFolder);
        				}
        			}
        			
        			storeAssociatedViews();       			
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
            } 
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
            }
        }
	}

    protected void storeAssociatedViews() {
        // save associated LogicalFiles.
        List<Integer> deletionIds = new ArrayList<Integer>();
        List<ViewFolderViewAssociationTransfer> list = new ArrayList<ViewFolderViewAssociationTransfer>();
        HashMap<ViewFolderViewAssociationTransfer, ViewFolderViewAssociation> map = new HashMap<ViewFolderViewAssociationTransfer, ViewFolderViewAssociation>();
        Map<Integer, ViewFolderViewAssociation> sortedByIDMap = new TreeMap<Integer, ViewFolderViewAssociation>();
        for (ViewFolderViewAssociation association : viewAssociations) {
            if (association.getPersistence() == SAFRPersistence.DELETED) {
            	deletionIds.add(association.getAssociationId());
            } else {
                // create transfer object
                ViewFolderViewAssociationTransfer assocTrans = new ViewFolderViewAssociationTransfer();
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
            DAOFactoryHolder.getDAOFactory().getViewFolderDAO().deleteAssociatedViews(getEnvironmentId(),deletionIds);
        }

        // call DAO to add
        if (list.size() > 0) {
            DAOFactoryHolder.getDAOFactory().getViewFolderDAO().persistAssociatedViews(list);
            for (ViewFolderViewAssociationTransfer assocTrans : list) {
                ViewFolderViewAssociation compAssoc = map.get(assocTrans);
                compAssoc.setObjectFromTransfer(assocTrans);
            }
        }

        // regenerate association list from Tree Map
        viewAssociations.clear();
        viewAssociations.addAll(sortedByIDMap.values());
    }
	
	/**
	 * This enum maintains the properties of a view folder.
	 * 
	 */
	public enum Property {
		NAME, COMMENT
	}

	/**
	 * Validate method is used to validate an View Folder object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the view folder, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();
		String name = getName();
		if ((name == null) || (name == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"View Folder name cannot be empty");
		} else {
			if (name.length() > MAX_VIEWFOLDER_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of View Folder name "  
			            +ModelUtilities.formatNameForErrMsg(
						getName(),(isForImport() || isForMigration()))
						+ "cannot exceed 32 characters.");
			}
			else if (this.isDuplicate()) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The View Folder name '"
                                        + name
                                        + "' already exists. Please specify a different name.");
            }
			if (!safrValidator.isNameValid(name)) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The View Folder name " 
				        		+ ModelUtilities.formatNameForErrMsg(
							    getName(),(isForImport() || isForMigration()))
								+ "should begin with letter and "
								+ "comprise of letters"
								+ ", numbers, pound sign (#) and underscores only.");
			}
			
		}
		if ((getComment() != null)
				&& (getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH)) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment cannot be more than 254 characters.");
		}
		if (!safrValidationException.getErrorMessages().isEmpty())
			throw safrValidationException;
	}

	private boolean isDuplicate() throws DAOException, SAFRException {
		try {
			ViewFolderTransfer viewFolderTransfer = null;
			viewFolderTransfer = DAOFactoryHolder.getDAOFactory()
					.getViewFolderDAO().getDuplicateViewFolder(getName(),
							getId(), getEnvironmentId());
			if (viewFolderTransfer == null) {
				return false;
			} else {
				return true;
			}
		} catch (DAOException de) {
			throw new SAFRException("Data access error for View Folder", de);
		}
	}

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException, SAFRException {
        if (hasCreatePermission()) {
            
            ViewFolder viewFolder = SAFRApplication.getSAFRFactory().createViewFolder();
    
            viewFolder.setName(newName);
            viewFolder.setComment(this.getComment()); 
            
            for (ViewFolderViewAssociation assoc : viewAssociations.getActiveItems()) {
                ViewFolderViewAssociation viewAssoc = new ViewFolderViewAssociation(viewFolder,
                    assoc.getAssociatedComponentIdNum(), assoc.getAssociatedComponentName(), 
                    assoc.getAssociatedComponentRights());
                viewFolder.addAssociatedView(viewAssoc);
            }
            
            viewFolder.validate();
            viewFolder.store();    
            return viewFolder;
        }
        else {
            throw new SAFRException("The user is not authorized to create a View Folder.");
        }
	}

    public boolean addAssociatedView(ViewFolderViewAssociation addAssoc) {
        boolean found = viewAssociations.add(addAssoc);
        if (!found) {
            markModified();
        }
        return found; 
    }
	
    public void removeViewAssociations(List<ViewFolderViewAssociation> remAssocs) {
        for (ViewFolderViewAssociation assoc : remAssocs) {
            viewAssociations.remove(assoc);
        }
    }

    public void removeViewAssociation(ViewFolderViewAssociation remAssoc) {
        viewAssociations.remove(remAssoc);
    }
    
    public SAFRAssociationList<ViewFolderViewAssociation> getViewAssociations() {
        return viewAssociations;
    }
}
