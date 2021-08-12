
package com.ibm.safr.we.model.utilities;

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


import java.util.List;

import com.ibm.safr.we.model.utilities.importer.DependentComponentNode;

public class MockNameConfirmWarningStrategy implements NameConfirmWarningStrategy {

	private boolean result = false;
	private boolean confirmed = false;
	private NamesWarningCallback callback;

	public MockNameConfirmWarningStrategy(boolean result, NamesWarningCallback callback) {
		this.result = result;
		this.callback = callback;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public boolean confirmWarning(String topic, String message) {
		confirmed = true;
		return result;
	}

	public boolean confirmWarning(String topic, String shortMessage,
			String detailMessage) {
		confirmed = true;
		return result;
	}

	public boolean confirmWarning(String topic, String shortMessage,
			List<DependencyData> dependencyList) {
		confirmed = true;
		return result;
	}

	public boolean correctNamesWarning(String topic, String shortMessage,
			DependentComponentNode dcnRoot) {
		confirmed = true;
		return callback.processDCN(dcnRoot);
	}

}
