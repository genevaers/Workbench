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
 * An exception generated from inside the compiler when a call on the ISAFRCompiler interface
 * fails. At the moment this inherits from RuntimeException for historical reasons. The previous 
 * COM interface used COMException's which were runtime exceptions. Perhaps later we can convert to 
 * checked exceptions.  
 */

public class SAFRCompilerException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public SAFRCompilerException() {
    }

    public SAFRCompilerException(String message) {
        super(message);
    }

    public SAFRCompilerException(Throwable cause) {
        super(cause);
    }

    public SAFRCompilerException(String message, Throwable cause) {
        super(message, cause);
    }
}
