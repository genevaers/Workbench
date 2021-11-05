package com.ibm.safr.we.model.utilities.importer;

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


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.data.transfer.ComponentAssociationTransfer;
import com.ibm.safr.we.data.transfer.LookupPathSourceFieldTransfer;
import com.ibm.safr.we.data.transfer.SAFRComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRCancelException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.base.SAFRObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.ConfirmWarningStrategy;
import com.ibm.safr.we.utilities.SAFRLogger;


public class ImportUtility extends SAFRObject {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.model.utilities.importer.ImportUtility");
	
	public enum Property {
		TARGET_ENVIRONMENT, COMPONENT_TYPE, FILES
	}
	
	private EnvironmentQueryBean targetEnvironment;
	private ComponentType componentType;
	private List<ImportFile> files;
	private ImportFile currentFile;
	private ConfirmWarningStrategy confirmWarningStrategy;
	private DocumentBuilder builder;
	private Document document;
	private XPath xpath;

	public ImportUtility(EnvironmentQueryBean targetEnvironment,
			ComponentType componentType,
			List<ImportFile> files) {
		super();
		this.targetEnvironment = targetEnvironment;
		this.componentType = componentType;
		this.files = files;
		
	}

	/**
	 * This method is used to get the target environment into which the
	 * component is to be imported.
	 * 
	 * @return the target environment query bean.
	 */
	public EnvironmentQueryBean getTargetEnvironment() {
		return targetEnvironment;
	}

	/**
	 * This method is used to set the target environment into which the
	 * component is to be imported.
	 * 
	 * @param targetEnvironment
	 *            : The target environment into which the component is to be
	 *            imported.
	 */
	public void setTargetEnvironment(EnvironmentQueryBean targetEnvironment) {
		this.targetEnvironment = targetEnvironment;
	}

	/**
	 * This method is used to get the type of component which is to be imported.
	 * 
	 * @return the type of component which is to be imported.
	 */
	public ComponentType getComponentType() {
		return componentType;
	}

	/**
	 * This method is used to set the type of component which is to be imported.
	 * 
	 * @param componentType
	 *            : the type of component which is to be imported.
	 */
	public void setComponentType(ComponentType componentType) {
		this.componentType = componentType;
	}

	/**
	 * This method is used to get the list of import files.
	 * 
	 * @return the list of ImportFiles to be imported.
	 */
	public List<ImportFile> getFiles() {
		return files;
	}

	/**
	 * Store the import files.
	 * 
	 * @param files
	 *            a list of ImportFiles
	 */
	public void setFiles(List<ImportFile> files) {
		this.files = files;
	}

	public ConfirmWarningStrategy getConfirmWarningStrategy() {
		return confirmWarningStrategy;
	}

	public void setConfirmWarningStrategy(ConfirmWarningStrategy strategy) {
		this.confirmWarningStrategy = strategy;
	}

	// package private. used only by import implementation.
	XPath getXPath() {
		return xpath;
	}

	// package private. used only by import implementation.
	Document getDocument() {
		return document;
	}

	// package private. used only by import implementation.
	ImportFile getCurrentFile() {
		return currentFile;
	}

	// package private. used only by import implementation.
	
	
	public void importMetadata() throws SAFRException {

		showParams();
		if(componentType == ComponentType.CobolCopyBook) {
			//We can do some copybook validaton here later
		} else {
			validate();
			try {
				initXMLFactories();
			} catch (ParserConfigurationException e1) {
				// xml parser runtime problem so cannot continue
				e1.printStackTrace();
				throw new SAFRFatalException(
						"An XML parser system error occurred. Cannot continue with import. ",
						e1);
			}
		}
		
		// Clear previous results for the files to be imported
		for (ImportFile f : files) {
			f.setResult(null);
			f.setErrorMsg(null);
			f.setException(null);
		}
		
		//Want to iterate the list of files and retain a list of renamed components
		//so declare here and retain for each file iteration 
		ComponentImporter importer = null;
		switch (componentType) {
		case PhysicalFile:
			importer = new PhysicalFileImporter(this);
			break;
		case LogicalFile:
			importer = new LogicalFileImporter(this);
			break;
		case LogicalRecord:
			importer = new LogicalRecordImporter(this);
			break;
		case LookupPath:
			importer = new LookupPathImporter(this);
			break;
        case View:
			importer = new ViewImporter(this);
            break;
        case ViewFolder:
            importer = new ViewFolderImporter(this);
            break;
        default :
            break;
		}
		for (ImportFile file : files) {
			currentFile = file;
			if(componentType == ComponentType.CobolCopyBook) {
				CopybookImporter cbi = new CopybookImporter();
				try {
					cbi.importCopybook(file, targetEnvironment.getId());
				} catch (SAFRException | XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				parseAndImportXML(importer, file);
			}
		} // end for loop, import next file
		
		// Log any errors reported during import
		logErrors();
	}

	private void parseAndImportXML(ComponentImporter importer, ImportFile file) {
		try {
			// Parse the XML
		    if (file.getStream() == null) {
		        document = builder.parse(file.getFile());			        
		    }
		    else {
		        document = builder.parse(file.getStream());                   			        
		    }
			importer.doImportMetadata();
			file.setResult(ActivityResult.PASS);
		} catch (IOException e) {
			// xml parser error
			String msg = e.getMessage()
					+ ". "
					+ (e.getCause() != null ? e.getCause().getMessage()
							: "");
			file.setResult(ActivityResult.FAIL);
			file.setErrorMsg(msg);
		} catch (SAXException e) {
			// xml parser error
			String msg = e.getMessage()
					+ ". "
					+ (e.getCause() != null ? e.getCause().getMessage()
							: "");
			file.setResult(ActivityResult.FAIL);
			file.setErrorMsg(msg);
		} catch (SAFRValidationException e) {
			// anticipated data error
			SAFRValidationType tokenType = null, excType = null;
			if (e.getSafrValidationToken() != null) {
				tokenType = e.getSafrValidationToken()
						.getValidationFailureType();
			}
			excType = e.getSafrValidationType();
			if (excType == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR
					|| excType == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR
					|| excType == SAFRValidationType.DEPENDENCY_PF_ASSOCIATION_WARNING
					|| tokenType == SAFRValidationType.DEPENDENCY_LF_ASSOCIATION_ERROR
					|| tokenType == SAFRValidationType.DEPENDENCY_LR_FIELDS_ERROR) {
				// it's a dependency error
				file.setException(e);
				file.setErrorMsg("Dependency error."); // default msg
			} else {
				// it's a general validation ERROR
				file.setErrorMsg(e.getMessageString());
			}
			file.setResult(ActivityResult.FAIL);
		} catch (SAFRCancelException e) {
			// file import cancelled
			file.setErrorMsg("Import cancelled." + SAFRUtilities.LINEBREAK + e.getMessage());
			file.setResult(ActivityResult.FAIL);
		} catch (SAFRException e) {
		    logger.log(Level.SEVERE, "Error importing " + file.getName() , e);
			// unexpected system error
			file.setErrorMsg(e.getMessage());
			file.setResult(ActivityResult.SYSTEMERROR);
			String msg = "Importing stopped at file "
				+ file.getName() + " because:" + SAFRUtilities.LINEBREAK + e.getMessage();
			throw new SAFRException(msg, e);
		} catch (XPathExpressionException e) {
			// only occurs on xpath syntax error (programming error)
			throw new SAFRFatalException(
					"An XPath system error occurred. Cannot continue with import. ",
					e);
		}
	}
	
	private void logErrors() {
		boolean hasErrors = false;
		StringBuffer buffer = new StringBuffer();
		List<String> errors = new ArrayList<String>();
		for (ImportFile file : files) {
			errors.clear();
			if (file.getResult() == ActivityResult.FAIL) {
				hasErrors = true;
			} else {
				continue; // skip this file
			}
			// use " + SAFRUtilities.LINEBREAK + " line feeds for opening log with Notepad
			buffer.append(SAFRUtilities.LINEBREAK + "File:   " + file.getName());
			buffer.append(SAFRUtilities.LINEBREAK + "Result: " + file.getResult());
			buffer.append(SAFRUtilities.LINEBREAK + "Errors: ");
			buffer.append(file.getErrorMsg());
			if (file.getException() != null) {
				buffer.append(SAFRUtilities.LINEBREAK);
				String contextMsg = "";
				String detailMsg = "";
				SAFRValidationException sve = file.getException();
				SAFRValidationType svt;
				if (sve.getSafrValidationToken() != null) {
					svt = sve.getSafrValidationToken().getValidationFailureType();
				} else {
					svt = sve.getSafrValidationType();
				}
				switch (svt) {
				case DEPENDENCY_LF_ASSOCIATION_ERROR:
					contextMsg = sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT);
					detailMsg = sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP);
					break;
				case DEPENDENCY_LR_FIELDS_ERROR:
					contextMsg = sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT);
					detailMsg = sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP);
					break;
				case DEPENDENCY_PF_ASSOCIATION_WARNING:
					contextMsg = sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP_IMPORT);
					detailMsg = sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP);
                default:
                    break;
				}
				buffer.append(contextMsg);
				buffer.append(SAFRUtilities.LINEBREAK);
				buffer.append(detailMsg);
			}
			buffer.append(SAFRUtilities.LINEBREAK);
		}
		if (hasErrors) {
		    SAFRLogger.logAll(logger, Level.SEVERE, "Import completed with the following errors..." + buffer.toString());
		} else {
		    SAFRLogger.logAll(logger, Level.INFO,"Import completed without errors.");
		}
		SAFRLogger.logEnd(logger);
	}

	
	private void validate() throws SAFRValidationException {
		
		HashSet<String> timestamps = new HashSet<>();
		SAFRValidationException safrValidationException = new SAFRValidationException();

		if (this.targetEnvironment == null) {
			safrValidationException.setErrorMessage(
					Property.TARGET_ENVIRONMENT,
					"Please select a target environment.");
		}

		if (this.componentType == null) {
			safrValidationException.setErrorMessage(Property.COMPONENT_TYPE,
					"Please select a component type.");
		} else if (this.componentType != ComponentType.PhysicalFile
				&& this.componentType != ComponentType.LogicalFile
				&& this.componentType != ComponentType.LogicalRecord
				&& this.componentType != ComponentType.LookupPath
				&& this.componentType != ComponentType.View
				&& this.componentType != ComponentType.ViewFolder) {
			safrValidationException
					.setErrorMessage(
							Property.COMPONENT_TYPE,
							"The only component types that can be imported are "
									+ "Physical Files, Logical Files, Logical Records, "
									+ "Lookup Paths, Views and View Folders.");
		}

		List<PhysicalFileQueryBean> queryAllPhysicalFiles = SAFRQuery.queryAllPhysicalFiles(targetEnvironment.getId(),SortType.SORT_BY_ID);
		List<LogicalRecordQueryBean> queryAllLogicalRecords = SAFRQuery.queryAllLogicalRecords(targetEnvironment.getId(),SortType.SORT_BY_ID);
		List<LogicalFileQueryBean> queryAllLogicalFile = SAFRQuery.queryAllLogicalFiles(targetEnvironment.getId(), SortType.SORT_BY_ID);
		List<ViewQueryBean> queryAllView = SAFRQuery.queryAllViews(targetEnvironment.getId(), SortType.SORT_BY_ID);
		List<ViewFolderQueryBean> queryAllViewfolder = SAFRQuery.queryAllViewFolders(targetEnvironment.getId(), SortType.SORT_BY_ID);
		List<LookupQueryBean> queryAllLookups = SAFRQuery.queryAllLookups(targetEnvironment.getId(), SortType.SORT_BY_ID);

		if(!queryAllView.isEmpty() || !queryAllPhysicalFiles.isEmpty() || !queryAllLogicalRecords.isEmpty() || !queryAllLogicalFile.isEmpty() || queryAllViewfolder.size()>1 || !queryAllLookups.isEmpty()) {
			safrValidationException.setErrorMessage(Property.COMPONENT_TYPE, "Imports are allowed only into empty environments");
		}
		// check files
		if (this.files.size() < 1) {
			safrValidationException.setErrorMessage(Property.FILES,
					"Please select one or more XML files to import.");
		} else {
			StringBuffer fileNames = new StringBuffer();
			for (ImportFile file : files) {
				File f = file.getFile();
				if (f != null) {
				    if (!f.exists()) {
	                    fileNames.append(" ");
	                    fileNames.append(f.getName());
				    }
				    else {
				        // make sure that the version of the xml is 3
				        try (Stream<String> lines = Files.lines(Paths.get(f.getAbsolutePath()))) {
				            Iterator<String> it = lines.iterator();
				            boolean notAtStart = true;
				            while(it.hasNext() && notAtStart) {
				            	String l = it.next();
				            	if(l.equalsIgnoreCase("<safrxml>")) {
				            		notAtStart = false;
				            	}
				            }
							if (notAtStart) {
								safrValidationException.setErrorMessage(Property.FILES,
										"File is not a WorkBench XML file " + f.getName());
							} else {
								it.next();
								it.next();
								String verline = it.next();
								if (!verline.trim().equals("<XMLVERSION>3</XMLVERSION>")) {
									safrValidationException.setErrorMessage(Property.FILES,
											"File is the wrong xml version " + f.getName());
								} else {
									String typeline = it.next();
									if (typeline == null) {
										safrValidationException.setErrorMessage(Property.FILES,
												"Couldn't read type from file " + f.getName());
									} else if (!typeline.trim().equals("<TYPE>" + getComponentString() + "</TYPE>")) {

										if (!f.getName().equals("")) {
											safrValidationException.setErrorMessage(Property.FILES,
													"The document has type "
															+ typeline.trim().substring(6,
																	(typeline.trim().length() - 7))
															+ " " + "but it should be " + getComponentString());
										}

									}
								}
							}
						} catch (IOException e) {
							safrValidationException.setErrorMessage(Property.FILES,
									"Couldn't read version from file " + f.getName());
						}
					}
				}
			}
			if (fileNames.length() > 0) {
				safrValidationException.setErrorMessage(Property.FILES,
						"These file(s) do not exist:" + fileNames + ".");
			}
		}
		

		if (!safrValidationException.getErrorMessages().isEmpty()) {
			safrValidationException.setSafrValidationType(SAFRValidationType.ERROR);
			throw safrValidationException;
		}
		timestamps.clear();
	}

    private String getComponentString() {
        if (componentType == ComponentType.PhysicalFile) {
            return "PhysicalFile";
        } else if (componentType == ComponentType.LogicalFile) {
            return "LogicalFile";
        } else if (componentType == ComponentType.LogicalRecord) {
            return "LogicalRecord";
        } else if (componentType == ComponentType.LookupPath) {
            return "Lookup";
        } else if (componentType == ComponentType.View) {
            return "View";
        } else if (componentType == ComponentType.ViewFolder) {
            return "ViewFolder";
        } else {
            return "";
        }
    }
	
	private void initXMLFactories() throws ParserConfigurationException {
		// Create document builder to be used later
		DocumentBuilderFactory docBuilder = DocumentBuilderFactory
				.newInstance();
		builder = docBuilder.newDocumentBuilder();
		// Create XPath object to be used later
		XPathFactory xpathFact = XPathFactory.newInstance();
		xpath = xpathFact.newXPath();
	}
	
	private void showParams() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Importing");
		buffer.append(SAFRUtilities.LINEBREAK + "Target Env ID   = " + (targetEnvironment != null ? targetEnvironment.getId() : null));
		buffer.append(SAFRUtilities.LINEBREAK + "Component Type  = " + (componentType != null ? componentType.getLabel() : null));
		buffer.append(SAFRUtilities.LINEBREAK + "XML files");
		for (ImportFile file : files) {
			buffer.append(SAFRUtilities.LINEBREAK + "\t" + file.getName());
		}
        buffer.append(SAFRUtilities.LINEBREAK);
		SAFRLogger.logAllSeparator(logger, Level.INFO, buffer.toString());
	}


	public static Integer getId(SAFRTransfer trans) {
		// JAK: this is a utility helper method which casts the transfer
		// object to a sub type to get its ID. It is a workaround pending
		// further consideration of the transfer class hierarchy and
		// placement of the ID getter methods.
		Integer id;
		if (trans instanceof LookupPathSourceFieldTransfer) {
			id = (((LookupPathSourceFieldTransfer) trans).getLookupPathStepId() * 100) 
			+ ((LookupPathSourceFieldTransfer) trans).getKeySeqNbr();
		} else if (trans instanceof SAFRComponentTransfer) {
			id = ((SAFRComponentTransfer) trans).getId();
		} else if (trans instanceof ComponentAssociationTransfer) {
			id = ((ComponentAssociationTransfer) trans).getAssociationId();
		} else {
			// JAK: do not remove this else branch - I will later.
			// It's here temporarily during development of Import
			// while we determine exactly which transfer types are needed.
			throw new IllegalStateException(
					"Unexpected transfer type: "
							+ trans.getClass().getName());
		}
		return id;
	}

	public static void replacePrimaryKeyId(SAFRTransfer trans, Integer newId) {
		if (trans instanceof SAFRComponentTransfer) {
			((SAFRComponentTransfer) trans).setId(newId);
		} else if (trans instanceof ComponentAssociationTransfer) {
			((ComponentAssociationTransfer) trans).setAssociationId(newId);
		} else {
			// This runtime exception indicates a programming error.
			throw new IllegalStateException(
					"Unexpected transfer type: "
							+ trans.getClass().getName());
		}
	}
	
	/**
	 * Split a message string to separate lines of a default length 70
	 * to improve output format.
	 * 
	 * @param message the message string
	 * @return the message string with line feed chars inserted
	 */
	public static String splitMessage(String message) {
		return splitMessage(message, 70); //default msg line len
	}
	
	/**
	 * Split a message string to separate lines of the specified length
	 * to improve output format.
	 *  
	 * @param message the message string
	 * @param lineLength length of each line
	 * @return the message string with line feed chars inserted
	 */
	public static String splitMessage(String message, int lineLength) {
		int maxLen = message.length();
		int i = 0;
		int j = lineLength-1;
		String outMsg = "";
		while (j < maxLen) {
			if (message.charAt(j) == ' ') {
				outMsg += message.substring(i, j);
				outMsg += SAFRUtilities.LINEBREAK;
				i = j + 1;
				j += lineLength;
			} else {
				j++;
			}
		}
		outMsg += message.substring(i);
		return outMsg;
	}

}
