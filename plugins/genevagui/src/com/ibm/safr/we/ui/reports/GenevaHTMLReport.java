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

import static j2html.TagCreator.body;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.join;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.script;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.DivTag;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public abstract class GenevaHTMLReport {
	
	protected Path reportPath;
	private FileWriter fw;
	static String toggleScript = "function toggleDiv(divname) {" + "var ele = document.getElementById(divname);"
			+ "if (ele.style.display == \"none\") {" + "ele.style.display = \"block\";" + "}" + "else {"
			+ "ele.style.display = \"none\";" + "}" + "}";

	public void write() {
		File output = reportPath.toFile();
		try {
			
			fw = new FileWriter(output);
			fw.write(
					html(
							head(
									meta().withContent("text/html; charset=UTF-8"),
									link().withRel("stylesheet").withType("text/css").withHref("https://www.w3schools.com/w3css/4/w3.css"),
									link().withRel("stylesheet").withType("text/css").withHref("https://www.w3schools.com/lib/w3-colors-flat.css"),
									link().withRel("stylesheet").withType("text/css").withHref("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"),
									script(join(toggleScript)).withLang("Javascript")
							),
							body(
									bodyContent()
							)).renderFormatted());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	abstract protected ContainerTag<DivTag> bodyContent();
	
	protected Path makeHtmlDirIfNeeded(Path path) {
		Path htmlPath = path.resolve("html");
		if(!htmlPath.toFile().exists()) {
			htmlPath.toFile().mkdirs();
		}
		return htmlPath;
	}

	public String getUrl() {
		return "file://" + reportPath.toString();
	}


}
