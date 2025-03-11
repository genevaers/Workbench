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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.safr.we.SAFRImmutableList;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.PhysicalFileTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRNotFoundException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;

/**
 * Describes characteristics of physical data source, such as a file or
 * database, which Logical File can be associated with. The characteristics
 * common to all types of physical files are captured directly within this
 * top-level class. The characteristics that are specific to different types of
 * physical file are captured by inner classes defined within this class.
 * <p>
 * These inner classes are:
 * <ul>
 * <li>InputDataset - defines characteristics of a dataset used for input.
 * <li>OutputDataset - defines characteristics of a dataset used for output.
 * <li>SQLDatabase - defines characteristics of a SQL database used for input.
 * </ul>
 * SQLDatabase is used where the access method is 'DB2 via SQL' or 'DB2 via
 * VSAM'. The other two are used for the remaining access methods.
 * 
 */
public class PhysicalFile extends SAFREnvironmentalComponent {

    enum DiskFileType {
        DATAB("DATAB"),REXIT("REXIT"),PEXIT("PEXIT");
        
        String code;
        
        DiskFileType(String code) {
            this.code = code;
        }
        
        public static DiskFileType getTypeFromString(String codeStr) {
            for (DiskFileType type : DiskFileType.values()) {
                if (codeStr.equals(type.code)) {
                    return type;
                }
            }
            return null;
        }
        
        public String getCode() {
            return code;
        }        
    }
    
	// common physical file attributes
	private Code fileTypeCode;
	private Code accessMethodCode;
	private Integer readExitId;
	private UserExitRoutine userExitRoutine;
	private String readExitParams;
	private SAFRAssociationList<FileAssociation> logicalFileAssociations;

	// input dataset attributes
	private String inputDDName; // also used for SQLDatabase
    private String datasetName; // reused for output dataset
	private int minRecordLen;
	private int maxRecordLen;

	// output dataset attributes
	private String outputDDName;
	private Code recfm;
	private int lrecl;

	// sql attributes
    private String subSystem;
    private String sqlStatement;
    private String schema;
	private String tableName;
	private Code rowFormatCode;
	private boolean includeNullIndicators;

	private List<String> loadWarnings;

	/**
	 * This constructor is used when defining a new PhysicalFile in the
	 * application. It will initialize the PhysicalFile ID to zero and
	 * EnvironmentId to specified EnvironmentId to which PhysicalFile belongs
	 * to. The PhysicalFile ID will be reset automatically to a unique value
	 * when the PhysicalFile object is persisted via its <code>store()</code>
	 * method.
	 */
	PhysicalFile(Integer environmentId) {
		super(environmentId);
		List<Code> fcodeList = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FILETYPE).getCodes();
		fileTypeCode = fcodeList.get(1);
        List<Code> acodeList = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD).getCodes();
        accessMethodCode = acodeList.get(2);
		logicalFileAssociations = new SAFRAssociationList<FileAssociation>();	
		readExitId = null;
	}

	/**
	 * Create an PhysicalFile object containing the data in the specified
	 * transfer object. Used to instantiate existing PhysicalFile objects.
	 * 
	 * @param trans
	 *            PhysicalFile data transfer object
	 * @throws SAFRException
	 * @throws DAOException
	 */
	PhysicalFile(PhysicalFileTransfer trans) throws DAOException, SAFRException {
		super(trans);
		logicalFileAssociations = new SAFRAssociationList<FileAssociation>();
		if (!trans.isForImport()) {
			// load only if already stored in DB.
			logicalFileAssociations = SAFRAssociationFactory
					.getPhysicalFileToLogicalFileAssociations(this);
		}
	}

	/**
	 * Returns a read-only list of logical files that this physical file has
	 * been associated with. This list is immutable, so it is not possible to
	 * add or remove list elements. The mutator methods of the List interface
	 * (add, remove, etc) cannot be used.
	 * 
	 * @return a SAFRImmutableList of logical file associations.
	 */
	public SAFRImmutableList<FileAssociation> getLogicalFileAssociations() {
		return new SAFRImmutableList<FileAssociation>(logicalFileAssociations);
	}

	protected void setObjectData(SAFRTransfer safrTrans) {
		super.setObjectData(safrTrans);
		PhysicalFileTransfer trans = (PhysicalFileTransfer) safrTrans;

		// set common attributes

		// mandatory (non-null) Code fields.
		loadWarnings = new ArrayList<String>();
        try {
            this.fileTypeCode = getModelFileTypeCode(trans.getFileTypeCode(), trans.getDiskFileTypeCode());
        } catch (IllegalArgumentException iae) {
            loadWarnings.add("This Physical File does not have a valid file type. Please select a valid file type before saving.");
            this.accessMethodCode = null;
        }
		try {
			this.accessMethodCode = SAFRApplication.getSAFRFactory()
					.getCodeSet(CodeCategories.ACCMETHOD).getCode(
							trans.getAccessMethodCode());
		} catch (IllegalArgumentException iae) {
			loadWarnings.add("This Physical File does not have a valid access method. Please select a valid access method before saving.");
			this.accessMethodCode = null;
		}
		this.readExitId = trans.getReadExitId();
		if (userExitRoutine != null
				&& userExitRoutine.getId() != trans.getReadExitId()) {
			this.userExitRoutine = null;
		}
		this.readExitParams = trans.getReadExitParams();

		// set input dataset attributes
		this.inputDDName = trans.getInputDDName();
        this.datasetName = trans.getDatasetName();
        this.minRecordLen = trans.getMinRecordLen();
        this.maxRecordLen = trans.getMaxRecordLen();
        
        // output
		this.outputDDName = trans.getOutputDDName();

        try {
            this.recfm = ModelUtilities.getCodeFromKey(
                    CodeCategories.RECFM, trans.getRecfm());
        } catch (IllegalArgumentException iae) {
            loadWarnings
                    .add("This Physical File does not have a valid RECFM. Please select a valid RECFM, if required, before saving.");
            this.recfm = null;
        }
        this.lrecl = trans.getLrecl();
        
		// set SQL data attributes
        this.subSystem = trans.getSubSystem();
        this.sqlStatement = trans.getSqlStatement();
        generateSchema();
		try {
			this.rowFormatCode = ModelUtilities.getCodeFromKey(
					CodeCategories.DBMSROWFMT, trans.getRowFormatCode());
		} catch (IllegalArgumentException iae) {
			loadWarnings.add("This Physical File does not have a valid row format. Please select a valid row format, if required, before saving.");
			this.rowFormatCode = null;
		}
		this.includeNullIndicators = trans.isIncludeNullIndicators();

	}

    protected void generateSchema() {
        if (sqlStatement != null) {
            // set schema based on value in sql statement
            Pattern pattern = Pattern.compile("\\s*SELECT\\s+\\S+\\s+FROM\\s+(\\S+)\\.(\\S+)\\s*");
            Matcher matcher = pattern.matcher(sqlStatement);
            if (matcher.find()) {
                this.schema = matcher.group(1);
                this.tableName = matcher.group(2);
            }
        }
    }
	
    public static Code getModelFileTypeCode(String fileType, String diskFileType) {
        
        CodeSet fileTypeSet = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.FILETYPE);
        if (diskFileType == null || diskFileType.length() == 0) {
            return fileTypeSet.getCode(fileType);
        } else {
            return fileTypeSet.getCode(diskFileType);
        }        
    }

    protected String getTransFileTypeString(Code fileType) {
        
        DiskFileType diskType = DiskFileType.getTypeFromString(fileType.getKey());
        if (diskType == null) {
            return fileType.getKey();
        } if(diskType == DiskFileType.PEXIT) {
            return "PIPE";            
        }
        else {
            return "DISK";
        }
    }
    
    protected String getTransDiskFileTypeString(Code fileType) {
        
        DiskFileType diskType = DiskFileType.getTypeFromString(fileType.getKey());
        if (diskType == null) {
            return null;
        } else {
            return diskType.getCode();
        }
    }
	

	protected void setTransferData(SAFRTransfer safrTrans) {
		super.setTransferData(safrTrans);
		PhysicalFileTransfer trans = (PhysicalFileTransfer) safrTrans;

		// set common attributes

		// mandatory (non-null) Code fields
		trans.setFileTypeCode(getTransFileTypeString(fileTypeCode));
        trans.setDiskFileTypeCode(getTransDiskFileTypeString(fileTypeCode));
		trans.setAccessMethodCode(accessMethodCode.getKey());

		if(readExitId != null && readExitId == 0)
			readExitId = null;
		trans.setReadExitId(readExitId);
		trans.setReadExitParams(readExitParams);

		// set input dataset attributes
		trans.setDatasetName(datasetName);
		trans.setInputDDName(inputDDName);
		trans.setMinRecordLen(minRecordLen);
		trans.setMaxRecordLen(maxRecordLen);

		// set output dataset attributes
		trans.setOutputDDName(outputDDName);
        trans.setRecfm(recfm == null ? null : recfm.getKey());
        trans.setLrecl(lrecl);
        
		// set SQL data attributes
        trans.setSubSystem(subSystem);
        CodeSet accMethods = SAFRApplication.getSAFRFactory().getCodeSet(CodeCategories.ACCMETHOD);
        trans.setSqlStatement(sqlStatement);            
		trans.setTableName(tableName);
		trans.setRowFormatCode(rowFormatCode == null ? null : rowFormatCode.getKey());
		trans.setIncludeNullIndicators(includeNullIndicators);

	}

	/**
	 * Get FileTypeCode of the PhysicalFile.
	 * 
	 * @return FileTypeCode of the PhysicalFile.
	 */
	public Code getFileTypeCode(){
		return fileTypeCode;
	}

	public void setFileTypeCode(Code fileTypeCode) {
		if (fileTypeCode == null) {
			throw new NullPointerException("File Type code cannot be null.");
		}
		this.fileTypeCode = fileTypeCode;
		markModified();
	}

	/**
	 * Get accessMethodCode of the PhysicalFile.
	 * 
	 * @return accessMethodCode of the PhysicalFile.
	 */
	public Code getAccessMethodCode() {
		return accessMethodCode;
	}

	/**
	 * Set accessMethodCode of the PhysicalFile.
	 * 
	 * @param accessMethodCode
	 *            to set accessMethodCode of the PhysicalFile.
	 * @throws NullPointerException
	 *             if the Code object is null, as this is a mandatory field and
	 *             cannot be null.
	 */
	public void setAccessMethod(Code accessMethodCode) {
		if (accessMethodCode == null) {
			throw new NullPointerException("Access Method code cannot be null.");
		}
		this.accessMethodCode = accessMethodCode;
		markModified();
	}

	/**
	 * Get UserExitRoutine of the physical file
	 * 
	 * @return UserExitRoutine of the physical file
	 * @throws SAFRException
	 */
	public UserExitRoutine getUserExitRoutine() throws SAFRException {
		if (userExitRoutine == null) {
			if (readExitId != null && readExitId > 0) {
				this.userExitRoutine = SAFRApplication.getSAFRFactory()
				.getUserExitRoutine(readExitId, this.getEnvironmentId());
			}
		}
		return userExitRoutine;
	}

	/**
	 * Set UserExitRoutine of the physical file
	 * 
	 * @param userExitRoutine
	 *            to set UserExitRoutine of the physical file
	 */
	public void setUserExitRoutine(UserExitRoutine userExitRoutine) {

		if (userExitRoutine == null || userExitRoutine.getId() == 0) {
			this.readExitId = null;
		} else {
			this.readExitId = userExitRoutine.getId();
		}

		this.userExitRoutine = userExitRoutine;
		markModified();
	}

	/**
	 * Get UserExitRoutineParameters of the physical file
	 * 
	 * @return UserExitRoutineParameters of the physical file
	 */
	public String getUserExitRoutineParams() {
		return readExitParams;
	}

	/**
	 * Set UserExitRoutineParameters of the physical file
	 * 
	 * @param readExitParams
	 *            to set UserExitRoutineParameters of the physical file
	 */
	public void setUserExitRoutineParams(String readExitParams) {
		this.readExitParams = readExitParams;
		markModified();
	}

	/**
	 * This enum maintains the properties of a Physical File.
	 * 
	 */
	public enum Property {
		NAME, FILE_TYPE, ACCESS_METHOD, MIN_RECORD_LENGTH, MAX_REC_LENGTH, COMMENT, USER_EXIT_ROUTINE
	}

	/**
	 * Validate method is used to validate a Physical File object.If any
	 * validation condition is not met then this method throws a list of all the
	 * error messages.
	 * 
	 * @throws SAFRValidationException
	 *             : This method will set all the error messages along with the
	 *             key, which is a property of the Physical File, and throws
	 *             SAFRValidationException when any validation condition is not
	 *             met.
	 * @throws SAFRException
	 * @throws DAOException
	 */
	public void validate() throws SAFRValidationException, SAFRException,
			DAOException {
		SAFRValidator safrValidator = new SAFRValidator();
		SAFRValidationException safrValidationException = new SAFRValidationException();

		// validate common attributes
		String name = getName();
		if ((name == null) || (name == "")) {
			safrValidationException.setErrorMessage(Property.NAME,
					"Physical File name cannot be empty");
		} else {
			if (name.length() > ModelUtilities.MAX_NAME_LENGTH) {
				safrValidationException.setErrorMessage(Property.NAME,
						"The length of Physical File name "
		        		+ ModelUtilities.formatNameForErrMsg(
					    getName(),(isForImport() || isForMigration()))						
						+ "cannot exceed 48 characters.");
			}
			else if (this.isDuplicate()) {
				safrValidationException
						.setErrorMessage(
								Property.NAME,
								"The Physical File name '"
										+ name
										+ "' already exists. Please specify a different name.");
			}
            if (!safrValidator.isNameValid(name)) {
                safrValidationException
                        .setErrorMessage(
                                Property.NAME,
                                "The Physical File name "
                                        + ModelUtilities.formatNameForErrMsg(
                                        getName(),(isForImport() || isForMigration()))
                                        + "should begin "
                                        + " with a letter and should comprise of letters"
                                        + ", numbers, pound sign (#) and underscores only.");
            }
		}
		if (getComment() != null
				&& getComment().length() > ModelUtilities.MAX_COMMENT_LENGTH) {
			safrValidationException.setErrorMessage(Property.COMMENT,
					"Comment cannot be more than 254 characters.");
		}

		if (this.fileTypeCode == null) {
			safrValidationException.setErrorMessage(Property.FILE_TYPE,
					"File Type cannot be empty");
		}
		if (this.accessMethodCode == null) {
			safrValidationException.setErrorMessage(Property.ACCESS_METHOD,
					"Access Method cannot be empty");
		}

		if (fileTypeCode.getGeneralId().equals(Codes.FILE_REXIT)) {
		    if (getUserExitRoutine() == null) {
                safrValidationException.setErrorMessage(Property.USER_EXIT_ROUTINE,
                    "Must have a read exit selected when file type is 'Read User-Exit Routine'");		        
		    }
		    else {
    	        try {
    	            if (getUserExitRoutine().getTypeCode().getGeneralId() != Codes.READ) {
    	                safrValidationException.setErrorMessage(Property.USER_EXIT_ROUTINE,
    	                    "The user exit routine '"+ userExitRoutine.getName() + "[" + 
    	                    userExitRoutine.getId() + "]' is not of type 'Read'. Please select a valid user exit routine.");
    	            }
    	        } catch (SAFRNotFoundException snfe) {
    	            safrValidationException.setErrorMessage(Property.USER_EXIT_ROUTINE,
    	                "The user exit routine with id ["+ snfe.getComponentId()+ "] does not exist. Please select a valid user exit routine.");
    	        }		    
    		}
		}

		// validate dataset attributes
		if (minRecordLen < 0) {
			safrValidationException.setErrorMessage(Property.MIN_RECORD_LENGTH,
					"The Minimum Record Length should be >= 0.");
		}
		if (maxRecordLen < 0) {
			safrValidationException.setErrorMessage(Property.MAX_REC_LENGTH,
					"The Maximum Record Length should be >= 0.");
		}

		if (!safrValidationException.getErrorMessages().isEmpty()) {
			throw safrValidationException;
		}

	}

	/**
	 * This method is used to check whether the Physical File name already exist
	 * in the workbench.
	 * 
	 * @return true if the Physical file name already exists.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private boolean isDuplicate() throws DAOException, SAFRException {

		try {
			PhysicalFileTransfer physicalFileTransfer = null;
			physicalFileTransfer = DAOFactoryHolder.getDAOFactory()
					.getPhysicalFileDAO().getDuplicatePhysicalFile(getName(),
							getId(), getEnvironmentId());
			if (physicalFileTransfer == null) {
				return false;
			} else
				return true;
		} catch (DAOException de) {
			throw new SAFRException("Data access error for Physical File.", de);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.safr.we.model.SAFRPersistentObject#store()
	 */
	@Override
	public void store() throws SAFRException, DAOException {
		if (isForMigration()) {
			if (!SAFRApplication.getUserSession().isAdminOrMigrateInUser(getEnvironmentId())) {
				String msg = "The user is not authorized to migrate into Environment "
						+ getEnvironmentId();
				throw new SAFRException(msg);
			}
		} else {
			if (this.id == 0) {
				if (!hasCreatePermission()) {
					throw new SAFRException("The user is not authorized to create a new physical file.");
				}
			} else {
				if (!hasUpdateRights()) {
					throw new SAFRException("The user is not authorized to update this physical file.");
				}
			}
		}
		
		if (!isForMigration()) {
			// if user is not a System Admin then check edit
			// rights on the components which are used in the Physical File.
			if (!SAFRApplication.getUserSession().isSystemAdministrator() && 
			    this.getUserExitRoutine() != null) {

				if (SAFRApplication.getUserSession().getEditRights(
						ComponentType.UserExitRoutine,
						this.getUserExitRoutine().getId(),
						this.getEnvironmentId()) == EditRights.None) {
					// no rights on user exit routine used in this PF
					throw new SAFRException(
							"The user does not have necessary edit rights on the read user exit Routine "
									+ " '"
									+ this.getUserExitRoutine().getName()
									+ " ["
									+ this.getUserExitRoutine().getId()
									+ "]' " + "used in Physical File.");
				}
			}
		} 
		
		PhysicalFileTransfer trans = new PhysicalFileTransfer();
		setTransferData(trans);

		try {
			trans = DAOFactoryHolder.getDAOFactory().getPhysicalFileDAO()
					.persistPhysicalFile(trans);
			setObjectData(trans);
			
	        SAFRApplication.getModelCount().incCount(this.getClass(), 1);       
			
            if (!isForMigration() && !SAFRApplication.getUserSession().isSystemAdministrator()) {
                SAFRApplication.getUserSession().getGroup().assignComponentFullRights(
                    this, ComponentType.PhysicalFile);
            }

		} catch (SAFRNotFoundException snfe) {
			throw new SAFRException(
					"The physical file with id "
							+ this.getId()
							+ " cannot be updated as its already been deleted from the database.",
					snfe);
		}

	}

	/**
	 * This inner class defines the characteristics of a PhysicalFile that are
	 * specific to an input dataset.
	 * 
	 */
	public class InputDataset {

		/**
		 * Returns the dataset name. JCL parameter 'DSN'.
		 */
		public String getDatasetName() {
			return datasetName;
		}

		/**
		 * Sets the dataset name.
		 * 
		 * @param datasetName
		 *            the datasetName to set
		 */
		public void setDatasetName(String datasetName) {
			PhysicalFile.this.datasetName = datasetName;
			markModified();
		}

		/**
		 * Returns the data definition name (DD name), a symbolic name for the
		 * input dataset. JCL parameter 'DDNAME'. A PhysicalFile can have only a
		 * single input DDName and this DDName will be used for both the
		 * InputDataset and the SQLDatabase definitions, if both are specified.
		 */
		public String getInputDDName() {
			return inputDDName;
		}

		/**
		 * Set the input DD name.
		 * 
		 * @param inputDDName
		 *            the inputDDName to set
		 */
		public void setInputDDName(String inputDDName) {
			PhysicalFile.this.inputDDName = inputDDName;
			markModified();
		}


	    public int getMinRecordLen() {
	        return minRecordLen;
	    }

	    public void setMinRecordLen(int minRecordLen) {
	        PhysicalFile.this.minRecordLen = minRecordLen;
	    }

	    public int getMaxRecordLen() {
	        return maxRecordLen;
	    }

	    public void setMaxRecordLen(int maxRecordLen) {
	        PhysicalFile.this.maxRecordLen = maxRecordLen;
	    }

	
	} // end of inner class InputDataset

	/**
	 * This inner class defines the characteristics of a PhysicalFile that are
	 * specific to an output dataset.
	 * 
	 */
	public class OutputDataset {

		/**
		 * Returns the output dataset name.
		 */
		public String getDatasetName() {
			return datasetName;
		}

		// No setter method for output dataset name.

		/**
		 * Returns the data definition name (DD name), a symbolic name for the
		 * output dataset. JCL parameter 'DDNAME'.
		 */
		public String getOutputDDName() {
			return outputDDName;
		}

		/**
		 * Sets the output DD name.
		 * 
		 * @param outputDDName
		 *            the outputDDName to set
		 */
		public void setOutputDDName(String outputDDName) {
			PhysicalFile.this.outputDDName = outputDDName;
			markModified();
		}
		
        public Code getRecfm() {
            return recfm;
        }

        public void setRecfm(Code recfm) {
            PhysicalFile.this.recfm = recfm;
            markModified();
        }

        public int getLrecl() {
            return lrecl;
        }

        public void setLrecl(int lrecl) {
            PhysicalFile.this.lrecl = lrecl;
            markModified();
        }

	} // end of inner class OutputDataset

	/**
	 * This inner class defines the characteristics of a PhysicalFile that are
	 * specific to an SQL database.
	 * 
	 */
	public class SQLDatabase {

	    
        public String getSubSystem() {
            return subSystem;
        }

        public void setSubSystem(String subSystem) {
            PhysicalFile.this.subSystem = subSystem;
            markModified();
        }

        /**
         * Returns the data definition name (DD name), a symbolic name for the
         * SQL database. A PhysicalFile can have only a single input DDName and
         * this DDName will be used for both the InputDataset and the
         * SQLDatabase definitions, if both are specified.
         */
        public String getInputDDName() {
            return inputDDName;
        }

        /**
         * Set the input DD name.
         * 
         * @param inputDDName
         *            the inputDDName to set
         */
        public void setInputDDName(String inputDDName) {
            PhysicalFile.this.inputDDName = inputDDName;
            markModified();
        }

        /**
         * Returns the SQL statement.
         */
        public String getSqlStatement() {
            return sqlStatement;
        }

        /**
         * Sets the SQL statement.
         * 
         * @param sqlStatement
         *            the sql statement string to be set.
         */
        public void setSqlStatement(String sqlStatement) {
            PhysicalFile.this.sqlStatement = sqlStatement;
            markModified();
        }
        
		/**
		 * Returns the SQL table name.
		 */
		public String getTableName() {
			return tableName;
		}

		/**
		 * Sets the SQL table name.
		 * 
		 * @param tableName
		 *            the name of the table to be set.
		 */
		public void setTableName(String tableName) {
			PhysicalFile.this.tableName = tableName;
			markModified();
		}

        /**
         * Returns the SQL schema
         */
        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            PhysicalFile.this.schema = schema;
            markModified();
        }
		
		/**
		 * Return the Code representing the SQL row format (standard SQL or
		 * internal to the database).
		 * 
		 * @throws SAFRException
		 *             if the code cannot be retrieved
		 */
		public Code getRowFormatCode() throws SAFRException {
			return rowFormatCode;
		}

		/**
		 * Sets the row format Code.
		 * 
		 * @param rowFormatCode
		 *            the code of row format to be set.
		 */
		public void setRowFormatCode(Code rowFormatCode) {
			PhysicalFile.this.rowFormatCode = rowFormatCode;
			markModified();
		}

		/**
		 * Returns true if SQL result set should include null indicators,
		 * otherwise false.
		 */
		public boolean isIncludeNullIndicators() {
			return includeNullIndicators;
		}

		/**
		 * Set a boolean to specify whether to include null indicators.
		 * 
		 * @param includeNullIndicators
		 *            true if the physical file should include the null
		 *            indicators, otherwise false.
		 */
		public void setIncludeNullIndicators(boolean includeNullIndicators) {
			PhysicalFile.this.includeNullIndicators = includeNullIndicators;
			markModified();
		}

	}// end if inner class SQLDatabase

	@Override
	public SAFRComponent saveAs(String newName) throws SAFRValidationException,
			SAFRException {

        if (hasCreatePermission()) {
            	    
    		PhysicalFile physicalFileCopy = SAFRApplication.getSAFRFactory().createPhysicalFile();
    		physicalFileCopy.setName(newName);
    		physicalFileCopy.setFileTypeCode(this.getFileTypeCode());
    		physicalFileCopy.setAccessMethod(this.getAccessMethodCode());
    		physicalFileCopy.setUserExitRoutine(this.getUserExitRoutine());
    		physicalFileCopy.setUserExitRoutineParams(this.getUserExitRoutineParams());
    		physicalFileCopy.setComment(this.getComment());
    
    		// creating a copy of sql database
    		PhysicalFile.SQLDatabase sqlDb = this.new SQLDatabase();
    		PhysicalFile.SQLDatabase sqlDbCopy = physicalFileCopy.new SQLDatabase();
    		sqlDbCopy.setSubSystem(sqlDb.getSubSystem());
    		sqlDbCopy.setIncludeNullIndicators(sqlDb.isIncludeNullIndicators());
    		sqlDbCopy.setInputDDName(sqlDb.getInputDDName());
    		sqlDbCopy.setRowFormatCode(sqlDb.getRowFormatCode());
    		sqlDbCopy.setSqlStatement(sqlDb.getSqlStatement());
    		sqlDbCopy.setTableName(sqlDb.getTableName());
    
    		// creating a copy of input dataset
    		PhysicalFile.InputDataset inputDataset = this.new InputDataset();
    		PhysicalFile.InputDataset inputDatasetCopy = physicalFileCopy.new InputDataset();
    		inputDatasetCopy.setDatasetName(inputDataset.getDatasetName());
    		inputDatasetCopy.setInputDDName(inputDataset.getInputDDName());
    		inputDatasetCopy.setMaxRecordLen(inputDataset.getMaxRecordLen());
    		inputDatasetCopy.setMinRecordLen(inputDataset.getMinRecordLen());
    
            // creating a copy of output dataset
    		PhysicalFile.OutputDataset outputDataset = this.new OutputDataset();
    		PhysicalFile.OutputDataset outputDatasetCopy = physicalFileCopy.new OutputDataset();    
            outputDatasetCopy.setOutputDDName(outputDataset.getOutputDDName());
    		outputDatasetCopy.setLrecl(outputDataset.getLrecl());
    		outputDatasetCopy.setRecfm(outputDataset.getRecfm());
    		physicalFileCopy.validate();
    		physicalFileCopy.store();
    
    		return physicalFileCopy;
        }
        else {
            throw new SAFRException("The user is not authorized to create a Physical File.");
        }     		
	}

	public List<String> getLoadWarnings() {
		return loadWarnings;
	}

}
