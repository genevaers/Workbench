package com.ibm.safr.we.exceptions;

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
 * Occurs when any logic text that is being compiled has an error. 
 */

public class SAFRCompilerParseException extends SAFRCompilerException {

    private static final long serialVersionUID = 1L;

    public SAFRCompilerParseException() {
        super();
    }

    public SAFRCompilerParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SAFRCompilerParseException(String message) {
        super(message);
    }

    public SAFRCompilerParseException(Throwable cause) {
        super(cause);
    }

}
