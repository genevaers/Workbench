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


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.safr.we.constants.DateFormat;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * This abstract class represent a parser which converts &LT;Record&GT; XML
 * elements into component transfer objects. It defines one public method which
 * provides the algorithm for retrieving the elements, parsing them and
 * returning the transfer objects. Concrete subclasses must implement two
 * abstract methods which define component type-specific behavior.
 */
abstract public class RecordParser {

	protected ComponentImporter importer;

	public RecordParser(ComponentImporter importer) {
		this.importer = importer;
	}

	/**
	 * This is the only public method of the RecordParser class. It parses
	 * &LT;Record&GT; elements into transfer objects and returns a map of these
	 * transfer objects keyed by their component ID. This method delegates the
	 * specification of the XPath expression used to retrieve &LT;Record&GT;
	 * elements and the record parsing logic to abstract methods which must be
	 * implemented by subclasses.
	 * 
	 */
	public void parseRecords() throws SAFRValidationException,
			XPathExpressionException {

		// get all of the <Record> elements
		NodeList recordNodes = (NodeList) importer.getXPath().evaluate(
				getRecordExpression(), importer.getDocument(),
				XPathConstants.NODESET);
		
		// set map capacity so dynamic allocation is not necessary
		int numRecs = recordNodes.getLength();
		int initCapacity = (int) (numRecs * 1.25);
		Map<Integer, SAFRTransfer> map = new HashMap<Integer, SAFRTransfer>(initCapacity, 1);

		// parse each element into a transfer object
		Node record;
		for (int i = 0; i < recordNodes.getLength(); i++) {
			record = recordNodes.item(i);
			SAFRTransfer tfr = parseRecord(record);
			tfr.setPersistent(false); // set to false so that its inserted in DB
			// by default.
			tfr.setForImport(true); // indicate that this object is used for
			// import.
			putToMap(map, tfr);
		}
		if (!map.isEmpty()) {
			// save the map of transfer objects
			SAFRTransfer tfr = (SAFRTransfer) map.values().toArray()[0];
			importer.records.put(tfr.getClass(), map);
		}
	}

	/**
	 * Implemented by subclasses, this method returns the XPath expression for
	 * the set of &LT;Record&GT; elements parsed by the implementing subclass.
	 * For example, this will be "//PhysicalFile/Record" for the subclass that
	 * parses &LT;Record&GT; elements which are children of the
	 * &LT;PhysicalFile&GT; element.
	 * 
	 * @return a String representing an XPath expression
	 */
	abstract protected String getRecordExpression();

	/**
	 * Implemented by subclasses, this method will parse a Node representing a
	 * &LT;Record&GT; element into a transfer object of the appropriate type.
	 * 
	 * @param record
	 *            the &LT;Record&GT; element Node
	 * @return a SAFRTransfer the parsed object
	 * @throws SAFRValidationException
	 *             if errors occur manipulating field values like numbers and
	 *             dates
	 * @throws XPathExpressionException
	 *             if a syntax error is found in an XPath expression
	 */
	abstract protected SAFRTransfer parseRecord(Node record)
			throws SAFRValidationException, XPathExpressionException;

	/**
	 * Returns an ID = from the specified transfer object which can be used as
	 * the key in a map of transfer objects. Most components have a single,
	 * unique ID so the default behavior of this method is to return this ID.
	 * However, some components or associations have a composite key made up of
	 * a foreign key and a sequence number, so there is no single unique ID.
	 * This method can be overridden for these transfer classes to provide
	 * alternative behavior.
	 * 
	 * @param trans
	 *            the SAFRTransfer object
	 * @return an Integer identifier
	 */
	protected Integer getId(SAFRTransfer trans) {
		return ImportUtility.getId(trans);
	}

	protected String parseField(String fieldName, Node record)
			throws SAFRValidationException, XPathExpressionException {
		Element recordElement = (Element) record;
		Node field = (recordElement.getElementsByTagName(fieldName)).item(0);
		if (field != null) {
			return field.getTextContent();
		} else {
			SAFRValidationException sve = new SAFRValidationException();
			sve.setErrorMessage(fieldName, "Element <" + fieldName
					+ "> is missing.");
			throw sve;
		}
	}

	protected Date fieldToDate(String fieldName, String fieldValue)
			throws SAFRValidationException {
		if (fieldValue == null || fieldValue.equals("")) {
			return null;
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DateFormat.WE_XML_DATE_FORMAT);
			Date date = sdf.parse(fieldValue);
			return date;
		} catch (ParseException e) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(DateFormat.WW_XML_DATE_FORMAT);
				Date date = sdf.parse(fieldValue);
				return date;
			} catch (ParseException e1) {
				e1.printStackTrace(); // JAK temporary
				SAFRValidationException sve = new SAFRValidationException(e1);
				sve.setErrorMessage(fieldName, "Element <" + fieldName
						+ "> has an invalid date/time value '" + fieldValue
						+ "'.");
				throw sve;
			}
		}
	}

	/**
	 * Return fieldValue as an Integer. This method doesn't specify a default
	 * value so if fieldValue is null or empty string return null.
	 * 
	 * @throws SAFRValidationException
	 *             if fieldValue has non-numeric content
	 */
	protected Integer fieldToInteger(String fieldName, String fieldValue)
			throws SAFRValidationException {
		return fieldToInteger(fieldName, fieldValue, null);
	}
	
	/**
	 * Return fieldValue as an Integer. If fieldValue is null or empty string
	 * return the specified defaultValue.
	 * 
	 * @throws SAFRValidationException
	 *             if fieldValue has non-numeric content
	 */
	protected Integer fieldToInteger(String fieldName, String fieldValue, Integer defaultValue)
		throws SAFRValidationException {
		if (fieldValue == null || fieldValue.equals("")) {
			return defaultValue;
		}
		try {
			return Integer.valueOf(fieldValue);
		} catch (NumberFormatException e) {
			SAFRValidationException sve = new SAFRValidationException(e);
			sve.setErrorMessage(fieldName, "Element <" + fieldName
					+ "> has a non-numeric value '" + fieldValue + "'.");
			throw sve;
		}
	}


	/**
	 * The import data should all be related to the top-level import component.
	 * For all foreign key fields there should be a record with a matching
	 * primary key (no orphaned foreign keys). And all records other than the
	 * top-level import component should be related directly or indirectly to
	 * that top-level component, so their primary keys should be referenced by a
	 * foreign key somewhere in the import data (no unreferenced primary keys).
	 * This method must be implemented by subclasses to perform these checks for
	 * the record type represented by the subclass.
	 * 
	 * @throws SAFRValidationException
	 */
	abstract public void checkReferentialIntegrity()
			throws SAFRValidationException;

	protected void putToMap(Map<Integer, SAFRTransfer> map, SAFRTransfer tfr) {
		map.put(getId(tfr), tfr); // default behaviour
	}
}
