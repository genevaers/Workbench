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
                  savestment
                ;

savestment      : SAVE;

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
                | first_fiscal
                | begin_period
                | end_period 
//                | COMMENTS COMMENT
                ;

begin_period    : BEGINNING_PERIOD NUM;
first_fiscal    : FIRST_FISCAL NUM;
end_period      : ENDING_PERIOD NUM;

addStatement :  ADD STRING
                ;

pf              : PHYSICAL_FILE META_REF
                  (pfsets)*
                ;

pfsets          : SET pf_fields;

//If we add grammar to check say values of FILE_TYPE this will duplicate the Code table checks
//But will help a user
//META_REF -> TEXT_VAL or something?

pf_fields       : pf_file_type 
                | pf_access_method
                | pf_input_dd
                | pf_output_dd
                | pf_min_rec_len
                ;

pf_file_type    : FILE_TYPE META_REF;
pf_access_method: ACCESS_METHOD META_REF;
pf_input_dd     : INPUT_DD_NAME META_REF;
pf_output_dd    : OUTPUT_DD_NAME META_REF;
pf_min_rec_len  : MIN_RECORD_LENGTH NUM;


lf              : LOGICAL_FILE META_REF
                  (lfadds)+
                ;

lfadds          : ADD lf_pf;
lf_pf           : META_REF;

lr              : LOGICAL_RECORD META_REF
                  (lradds)+
                ;

lradds          : ADD lradditem;

lradditem       : lraddifeld
                | lraddlf;

lraddifeld      : FIELD META_REF;

lraddlf         : LOGICAL_FILE META_REF;

view            : VIEW META_REF
                  (viewadds)*
                ;

viewadds        : ADD view_add_fields;

view_add_fields : vw_add_cr
                | column
                | view_source
                | column_source;

vw_add_cr       : CONTROL_RECORD META_REF;

column          : COLUMN
                (columnsets)*
                ;

columnsets      : SET columnfields;

columnfields    : col_fld_data
                | col_length
                ;

col_fld_data    : DATA_TYPE META_REF;
col_length      : LENGTH NUM;

view_source     : VIEW_SOURCE
                  (vs_sets)*;

vs_sets         : SET vs_set_fields;

vs_set_fields   : vs_log_rec
                | vs_log_file
                ;

vs_log_rec      : LOGICAL_RECORD META_REF;
vs_log_file     : LOGICAL_FILE META_REF;

column_source   : COLUMN_SOURCE
                (cs_set)*;

cs_set          : SET cs_src_type;

cs_src_type     : TYPE cs_type;

cs_type         : cs_const
                | cs_field
                | cs_formula 
                | cs_lookup
                ; 

cs_const        : CONSTANT META_REF;
cs_field        : SOURCE_FILE_FIELD META_REF;
cs_formula      : FORMULA;
cs_lookup       : LOOKUP_FIELD;