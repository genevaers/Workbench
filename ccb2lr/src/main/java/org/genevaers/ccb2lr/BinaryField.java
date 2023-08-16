package org.genevaers.ccb2lr;

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


public class BinaryField extends CobolField {

    @Override
    public FieldType getType() {
        return FieldType.BINARY;
    }

    @Override
    public int getLength() {
        int len = picLength;
        // For Binary the field length implies the width
        // At least in some circumstances.
        // No single byte - see https://www.ibm.com/docs/en/i/7.4?topic=clause-computational-5-comp-5-phrase-binary
        if(len > 9) {
            len = 8;
        } else if(len > 5){
            len = 4;
        } else {
            len = 2;
        }
        return len;
    }
    
}
