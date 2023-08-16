package com.ibm.safr.we.model;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Folder {
	private int folderId;

	  private String folderName;

	  private List views;

	  public Folder(int folderId, String folderName) {
	    this.folderId = folderId;
	    this.folderName = folderName;
	    views = new LinkedList();
	  }

	  public int getFolderId() {
	    return folderId;
	  }


	  public String getFolderName() {
	    return folderName;
	  }

	  
	  public boolean add(Views view) {
	    boolean added = views.add(view);
	    if (added)
	      view.setFolder(this);
	    return added;
	  }

	  public List getViews() {
	    return Collections.unmodifiableList(views);
	  }
}
