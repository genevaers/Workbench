package com.ibm.safr.we.ui.reports;

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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public abstract class GenevaCSVReport {
	
	protected Path reportPath;
	protected List<String> headers = new ArrayList<>();
	protected List<List<String>> allrows = new ArrayList<>();

	public void write() {
		try (CSVPrinter printer = new CSVPrinter(new FileWriter(reportPath.toFile()), CSVFormat.EXCEL)) {
			printer.printRecord(getHeaders());
			printer.printRecords(getRows());
		 } catch (IOException ex) {
		     ex.printStackTrace();
		 }
	}
	
	abstract protected List<String> getHeaders();
	abstract protected List<List<String>> getRows();
	
	protected Path makeCsvDirIfNeeded(Path path) {
		Path csvPath = path.resolve("csv");
		if(!csvPath.toFile().exists()) {
			csvPath.toFile().mkdirs();
		}
		return csvPath;
	}

}
