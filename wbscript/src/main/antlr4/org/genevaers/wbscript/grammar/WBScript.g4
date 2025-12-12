grammar WBScript;
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
import ScriptLexer;

goal            : stmtList EOF   ;

stmtList        : ( stmt )*
                ;

stmt            : createStatement 
                ;

createStatement : CREATE component
                ;

//Make component for each type so we can then list the appropriate property names for adds and setStatement
//demand  a SAVE at the close of the CREATE

component       : cr
                | pf
                | lf
                | lr
                | view
		            ;

cr              : CONTROL_RECORD META_REF
                  (crsets)*
                ;


crsets          : SET crfields;

crfields        : NAME META_REF
                | FIRST_FISCAL NUM
                | BEGINNING_PERIOD NUM
                | ENDING_PERIOD NUM
//                | COMMENTS COMMENT
                ;

addStatement :  ADD STRING
                ;

pf              : PHYSICAL_FILE META_REF
                  (pfsets)*
                ;

pfsets          : SET pf_fields;

//If we add grammar to check say values of FILE_TYPE this will duplicate the Code table checks
//But will help a user
//META_REF -> TEXT_VAL or something?

pf_fields       : FILE_TYPE META_REF
                | ACCESS_METHOD META_REF
                | INPUT_DD_NAME META_REF
                | OUTPUT_DD_NAME META_REF
                | MIN_RECORD_LENGTH NUM
                ;

lf              : LOGICAL_FILE META_REF
                  (lfadds)+
                ;

lfadds          : ADD META_REF;

lr              : LOGICAL_RECORD META_REF
                  (lradds)+
                ;

lradds          : ADD FIELD META_REF
                | ADD LOGICAL_FILE META_REF;

view            : VIEW META_REF
                  (viewadds)*
                ;

viewadds        : ADD view_add_fields;

view_add_fields : CONTROL_RECORD META_REF
                | column
                | view_source
                | column_source;

column          : COLUMN
                (columnsets)*
                ;

columnsets      : SET columnfields;

columnfields    : DATA_TYPE META_REF
                | LENGTH NUM
                ;

view_source     : VIEW_SOURCE
                  (vs_sets)*;

vs_sets         : SET vs_set_fields;

vs_set_fields   : LOGICAL_RECORD META_REF
                | LOGICAL_FILE META_REF;

column_source   : COLUMN_SOURCE
                cs_set;

cs_set          : SET cs_src_type;

cs_src_type     : TYPE cs_type;

cs_type         : CONSTANT META_REF
                | SOURCE_FILE_FIELD META_REF
                | FORMULA
                | LOOKUP_FIELD;