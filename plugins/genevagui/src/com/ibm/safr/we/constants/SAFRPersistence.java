package com.ibm.safr.we.constants;

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


/**
 * Represents the persistence status of an object. A NEW object is not yet
 * persistent. An OLD object still reflects its persistent state (it has not
 * been modified yet). A MODIFIED object has been changed from its last
 * persistent state. A DELETED object has been marked for the permanent removal
 * of its persistent state.
 * 
 */
public enum SAFRPersistence {

	NEW, OLD, MODIFIED, DELETED;

}
