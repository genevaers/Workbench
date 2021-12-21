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
import java.util.Iterator;
import java.util.List;

import com.ibm.safr.we.data.transfer.CodeTransfer;
import com.ibm.safr.we.model.base.SAFRObject;

/**
 * 
 * It is set of {@link Code} groped by category.
 */
public class CodeSet extends SAFRObject {

	private String codeCategory;
	private List<Code> codes;

	/**
	 * Create an CodeSet object.
	 * 
	 * @param codeCategory
	 *            category to group codes.
	 * @param codeTransfers
	 *            collection of codetransfer object.
	 */
	public CodeSet(String codeCategory, List<CodeTransfer> codeTransfers) {
		this.codeCategory = codeCategory;
		codes = new ArrayList<Code>();
		Iterator<CodeTransfer> i = codeTransfers.iterator();
		while (i.hasNext()) {
			CodeTransfer tfr = i.next();
			Code code = new Code(tfr.getCodeValue(), tfr.getCodeDescription(),
					tfr.getGeneralId());
			codes.add(code);
		}

	}

	/**
	 * Get codeCategory of CodeSet object.
	 * 
	 * @return codeCategory of CodeSet object.
	 */
	public String getCodeCategory() {
		return codeCategory;
	}

	/**
	 * Get List of Codes of CodeSet object.
	 * 
	 * @return List of Codes of CodeSet object.
	 */
	public List<Code> getCodes() {
		return codes;
	}

	/**
	 * Get Code having given codeKey.
	 * 
	 * @param codeKey
	 *            of the Code to retrive.
	 * @return code having specified codekey.
	 * @throws IllegalArgumentException
	 *             if the Code corresponding to the Code Key is not found.
	 */
	public Code getCode(String codeKey) {
		Code code = null;
		Iterator<Code> i = codes.iterator();
		while (i.hasNext()) {
			Code temp = i.next();
			if (temp.getKey().equals(codeKey)) {
				code = temp;
				break;
			}
		}
		if (code == null) {
			throw new IllegalArgumentException("The code with key '" + codeKey
					+ "' not found in Code set '" + codeCategory + "'");
		}

		return code;

	}

	public Code getCode(Integer generalId) {
		Code code = null;
		for (Code c : codes) {
			if (c.getGeneralId().equals(generalId)) {
				code = c;
				break;
			}
		}

		if (code == null) {
			throw new IllegalArgumentException("The code with key '"
					+ generalId + "' not found in Code set '" + codeCategory
					+ "'");
		}

		return code;
	}

}
