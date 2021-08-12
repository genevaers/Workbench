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


import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.view.ViewSortKey;

/**
 * SAFRValidator class is a utility used to validate fields.
 */

public class SAFRValidator {

	/**
	 * This method is used to validate the name which should start with a letter
	 * and should comprise of letters,numbers, underscores and pound sign (hash)
	 * only.
	 * 
	 * @param componentName
	 * @return true if String ComponentName is valid else return false.
	 */
	public boolean isNameValid(String componentName) {
		if (!Character.isLetter(componentName.charAt(0))) {
			return false;
		}
		int length = componentName.length();
		for (int i = 0; i < length; i++) {
			char c = componentName.charAt(i);
			if (!isCharacterValid(c)) {
				return false;
			}
		}
		return true;
	}

    /**
     * This method is used to validate the character which should be
     * one of letters,numbers, underscores and pound sign (hash) only.
     * 
     * @param c character to check
     * @return true if c is valid else return false.
     */	
	public boolean isCharacterValid(char c) {
        if (!Character.isDigit(c) && !Character.isLetter(c) && !(c == '_')
                && !(c == '#')) {
            return false;
        }
        else {
            return true;
        }	   
	}
	
    private int getValidLength(Code content, Code type) {
        switch (type.getGeneralId()) {
        case Codes.ALPHANUMERIC:
        case Codes.MASKED_NUMERIC:
        case Codes.EDITED_NUMERIC:
        case Codes.ZONED_DECIMAL:
            return content.getDescription().length();
        case Codes.PACKED:
        case Codes.PACKED_SORTABLE:
            return (content.getDescription().length()/2) + 1;
        case Codes.BINARY:
        case Codes.BINARY_SORTABLE:
            return getBinaryLength(content.getDescription().length());
        case Codes.BINARY_CODED_DECIMAL:
            return (content.getDescription().length()/2);
        default: 
            break;
        }
        return 0;
        
    }
	
    protected int getBinaryLength(int len) {
        int ret;
          if (len <= 2 ) {
              ret = 1;
          } else if (len <= 4) {
              ret = 2;
          } else if (len <= 9) {
              ret = 4;
          } else if (len <= 19) {
              ret = 8;
          } else {
              ret = 16;
          }
          return ret;
    }

	public enum Property {
		DATATYPE
	}

	/**
	 * This method is used to verify and validate the Field depending upon the
	 * DataType.
	 * 
	 * @param field
	 * @throws SAFRException
	 */
	public void verifyField(SAFRField field) throws SAFRException {
		SAFRValidationException validationException = new SAFRValidationException();
		verifyDataType(field.getDataTypeCode(), field.getLength(), field
				.getDecimals(), field.isSigned(), field.getDateTimeFormatCode(),
				validationException);

		if (field.getDataTypeCode() != null) {
			int fieldGeneralId = field.getDataTypeCode().getGeneralId().intValue();
			if (fieldGeneralId == Codes.MASKED_NUMERIC
					&& field.getNumericMaskCode() == null) {
				validationException
						.setErrorMessage(
								Property.DATATYPE,
								"Data type errors: A Numeric Mask format should be added to a Masked Numeric Fields.");
			}

			if (fieldGeneralId == Codes.ALPHANUMERIC && field.getScaling() != null && field.getScaling() != 0) {
				validationException
						.setErrorMessage(Property.DATATYPE,
								"Data type errors: Alphanumeric data type should not have scaling.");

			}
			if ((fieldGeneralId != Codes.ALPHANUMERIC) && (field.getScaling() != null)
					&& ((field.getScaling() < -9) || (field.getScaling() > 9))) {
				validationException
						.setErrorMessage(Property.DATATYPE,
								"Data type errors: Scaling factor should be in the range -9 to +9");

			}
		}

		if (!validationException.getErrorMessages().isEmpty())
			throw validationException;
	}

	/**
	 * This method is used to verify and validate the sort key depending upon
	 * the DataType.
	 * 
	 * @param viewSortKey
	 * @throws SAFRException
	 */
	public void verifySortKey(ViewSortKey viewSortKey) throws SAFRException {
		SAFRValidationException validationException = new SAFRValidationException();

		verifyDataType(viewSortKey.getDataTypeCode(), viewSortKey.getLength(),
				viewSortKey.getDecimalPlaces(), viewSortKey.isSigned(),
				viewSortKey.getDateTimeFormatCode(), validationException);
		if (!validationException.getErrorMessages().isEmpty())
			throw validationException;
	}

	private void verifyDataType(Code dataType, int length, int decimals,
			boolean signed, Code dateTimeFormat,
			SAFRValidationException validationException) {
		if (dataType == null) {
			validationException.setErrorMessage(Property.DATATYPE,
					"Data type errors: Please specify a data type.");
		} else {
			int generalId = dataType.getGeneralId().intValue();
			switch (generalId) {
			case Codes.ZONED_DECIMAL:
				if ((length < 1) || (length > 16)) {
					validationException
							.setErrorMessage(
									Property.DATATYPE,
									"Data type errors: Zoned Decimal must have minimum length of 1 and maximum length of 16(inclusive).");

				}
				break;
			case Codes.ALPHANUMERIC:

				if (decimals > 0) {

					validationException
							.setErrorMessage(Property.DATATYPE,
									"Data type errors: Alphanumeric cannot have decimals.");

				}
				if (signed) {
					validationException.setErrorMessage(Property.DATATYPE,
							"Data type errors: Alphanumeric cannot be signed.");

				}

				if ((length < 1) || (length > 256)) {

					validationException
							.setErrorMessage(Property.DATATYPE,
									"Data type errors: Alphanumeric length must be between 1 and 256 (inclusive).");

				}

				break;
			case Codes.BINARY_SORTABLE:
				if (!(length == 1 || length == 2 || length == 4 || length == 8)) {

					validationException
							.setErrorMessage(
									Property.DATATYPE,
									"Data type errors: Binary sortable must have length one, two, four or eight only.");

				}
				break;

			case Codes.BINARY_CODED_DECIMAL:
				if ((length < 1) || (length > 10)) {

					validationException.setErrorMessage(Property.DATATYPE,
							"Data type errors: BCD must have minimum length of 1 and  "
									+ "maximum length of 10.");

				}

				break;

			case Codes.PACKED:
				if ((length < 1) || (length > 16)) {

					validationException
							.setErrorMessage(Property.DATATYPE,
									"Data type errors: Packed must have length in between 1 and 16.");

				}
				break;
			case Codes.PACKED_SORTABLE:
				if ((length < 1) || (length > 16)) {

					validationException
							.setErrorMessage(Property.DATATYPE,
									"Data type errors: Packed sortable must have length in between 1 and 16.");

				}
				break;
			case Codes.BINARY:
				if (!(length == 1 || length == 2 || length == 4 || length == 8)) {

					validationException
							.setErrorMessage(Property.DATATYPE,
									"Data type errors: Binary must have length one, two,four or eight only.");

				}

				if (dateTimeFormat != null) {
					if (signed) {
						validationException
								.setErrorMessage(Property.DATATYPE,
										"Data type errors: Binary with date time format must not be signed.");

					}

				}
				break;

			case Codes.MASKED_NUMERIC:

				if ((length < 1) || (length > 256)) {

					validationException
							.setErrorMessage(
									Property.DATATYPE,
									"Data type errors: Masked numeric length must be between one and 256 (inclusive).");

				}
				break;

			case Codes.EDITED_NUMERIC:
				if (dateTimeFormat != null) {
					validationException
							.setErrorMessage(
									Property.DATATYPE,
									"Data type errors: Explicit date time format disallowed for edited numeric type.");

				}
				if ((length < 1) || (length > 256)) {

					validationException
							.setErrorMessage(
									Property.DATATYPE,
									"Data type errors: Edited numeric length must be between one and 256 (inclusive).");

				}
				break;

			}

	        if (dateTimeFormat != null) {
	            int validLength = getValidLength(dateTimeFormat, dataType);
	            if (length < validLength) {

	                validationException.setErrorMessage(Property.DATATYPE,
	                        "Data type errors: Date time format '"
	                                + dateTimeFormat.getDescription()
	                                + "' must have length of at least "
	                                + validLength + ".");

	            }
	        }
			
			if (generalId != Codes.ALPHANUMERIC) {
				if (decimals > 9) {
					validationException
							.setErrorMessage(Property.DATATYPE,
									"Data type errors: Decimal places value should not be greater than 9");
				}
			}
		}
	}
}
