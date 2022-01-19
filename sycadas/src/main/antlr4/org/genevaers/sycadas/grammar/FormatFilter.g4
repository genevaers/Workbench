grammar FormatFilter;
/*
 * Copyright Contributors to the GenevaERS Project.
 * (c) Copyright IBM Corporation 2020.
 * SPDX-License-Identifier: Apache-2.0  
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
import FormatParser;


goal            : (stmt)? EOF
                ;

stmt            : ( IF predicate THEN stmtThenSelect )
                | SKIPIF LPAREN predicate RPAREN
                | SELECTIF LPAREN predicate RPAREN
				| stmtIfSkip
                ;

stmtIfSelect    : IF predicate THEN stmtThenSelect  ( ELSE stmtIfSelect )? ENDIF
                ;

stmtIfSkip      : IF predicate THEN stmtThenSkip  ( ELSE stmtIfSkip )? ENDIF
                ;

stmtThenSelect	: SELECT 
                | stmtIfSelect
                ;

stmtThenSkip	: SKIPF
                | stmtIfSkip
                ;

