grammar ExtractParser;
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
import GenevaERSLexer;

cast        : '<' 
                   (ALPHA 
                  | NODTF 
                  | K_BINARY 
                  | BCD 
                  | EDITED 
                  | MASKED 
                  | PACKED 
                  | BINARY 
                  | SPACKED 
                  | SZONED 
                  | ZONED)
                  '>'
            ;

//predicate       : ( LPAREN exprBoolOr RPAREN )
//                | exprBoolUnary AND exprBoolUnary
//                | exprBoolUnary OR exprBoolUnary
//                ;

predicate       : exprBoolOr
                ;

exprBoolOr      : exprBoolAnd ( OR exprBoolAnd )*
                ;

exprBoolAnd     : exprBoolUnaryResolve ( AND exprBoolUnaryResolve )*
                ;

exprBoolUnaryResolve : exprBoolUnary
                ;

exprBoolUnary   : ( NOT )? exprBoolAtom
                ;

exprBoolAtom    : ( LPAREN exprBoolOr RPAREN )
                  | stringComp
                  | arithComp
                  | isFunctions
                  | isFounds
                ;

// is Functions
isFunctions     : ( ISSPACES
                  | ISNOTSPACES
                  | ISNUMERIC
                  | ISNOTNUMERIC
                  | ISNULL
                  | ISNOTNULL ) LPAREN valueRefResolve RPAREN
                ;

valueRefResolve : castValueRef
                ;

castValueRef    : (cast)? valueRef
                ;

valueRef        :fieldRef
                | lookupField
                | colRef
                ;

isFounds       : ( ISFOUND
                 | ISNOTFOUND ) LPAREN lookupRef RPAREN
               ;

// comparisons
stringComp      : exprConcatString ( LT
                                   | GT
                                   | LE
                                   | GE
                                   | EQ
                                   | NE
                                   | CONTAINS
                                   | BEGINS_WITH
                                   | ENDS_WITH ) exprConcatString
                ;

 arithComp      : arithExpr ( LT
                            | GT
                            | LE
                            | GE
                            | EQ
                            | NE ) arithExpr
                ;

lookupRef       : CURLED_NAME
                | '{' META_REF symbollist '}'
                | '{' META_REF effDate '}'
                | '{' META_REF effDate symbollist '}'
                ;

// string expressions
stringExpr       : cast?    exprStringAtom
                          | exprConcatString
                 ;

exprConcatString : exprStringAtom ( (CONCAT) exprStringAtom)*
                 ;

exprStringAtom  : LPAREN exprConcatString RPAREN
                | colRef
                | fieldRef
                | lookupField
                | stringAtom
                | dateConstant
                | strfunc
                ;

// arithmetic expressions
arithExpr       : (PLUS | MINUS | cast)? exprArithAtom
                | exprArithAddSub MULT exprArithAddSub
                | exprArithAddSub DIV exprArithAddSub
                | exprArithAddSub PLUS exprArithAddSub
                | exprArithAddSub MINUS exprArithAddSub
                ;

exprArithAddSub : exprArithMulDiv ((
                    PLUS |
                    MINUS) exprArithMulDiv)*
                ;

exprArithMulDiv : exprArithResolveUnary ((
                    MULT |
                    DIV ) exprArithResolveUnary)*
                ;

exprArithResolveUnary : exprArithUnary
                      ;

exprArithUnary  : (PLUS | MINUS | cast)* exprArithAtom
                ;

exprArithAtom   : (LPAREN exprArithAddSub RPAREN)
                | colRef
                | fieldRef
                | lookupField
                | numAtom
                | betweenFunc
                | dateConstant
		        		;

colRef          : COL_REF
                ;

fieldRef        : CURLED_NAME
                | CURRENT LPAREN CURLED_NAME RPAREN
                | PRIOR   LPAREN CURLED_NAME RPAREN
                ;

lookupField     : '{' DOTTED_NAME '}'
                | '{' DOTTED_NAME symbollist '}'
                | '{' DOTTED_NAME effDate '}'
                | '{' DOTTED_NAME effDate symbollist '}'
                ;

symbollist      : ';' symbolEntry*
                ;

effDate         : ',' effDateValue
                ;

effDateValue    : fieldRef
                | dateConstant
                ;

symbolEntry     : SYMBIDENT '=' NUM
                | SYMBIDENT '=' STRING
                ;

eventSelector   : CURRENT LPAREN fieldRef RPAREN
                | PRIOR   LPAREN fieldRef RPAREN
                ;

constant        : numAtom | stringAtom | dateConstant
				;

numAtom         : NUM
                ;

stringAtom      : string
                | repeat
                | all
                ;

repeat          : REPEAT LPAREN string COMMA NUM RPAREN
                ;

all             : ALL LPAREN string RPAREN
                ;

string          :  STRING
                ;

// dates
dateConstant    : dateFunc
                | period
                ;

dateFunc        : DATE LPAREN string COMMA ( CCYY
				                                   | CCYYDDD
                                           | CCYYMMDD
                                           | MMDDCCYY
                                           | MMDDYY
                                           | CCYYMM
                                           | DDMMYY
                                           | DDMMCCYY
                                           | DD
                                           | MM
                                           | MMDD
                                           | YYDDD
                                           | YYMMDD
                                           | YY  ) RPAREN
                ;

period          : runDate
                | fiscalDate
                | timeStamp
                ;

runDate         : ( RUNDAY
                |   RUNMONTH
                |   RUNYEAR ) LPAREN ( unaryInt )? RPAREN
                ;

fiscalDate      : ( FISCALDAY
                |   FISCALMONTH
                |   FISCALYEAR ) LPAREN ( unaryInt )? RPAREN
                ;

timeStamp       : BATCHDATE LPAREN ( unaryInt )? RPAREN
                ;

unaryInt        : unaryIntResolve
                ;

unaryIntResolve : (MINUS | PLUS)? NUM
                ;

// betweenFunc
betweenFunc     : ( DAYSBETWEEN
                  | MONTHSBETWEEN
                  | YEARSBETWEEN ) LPAREN dateArg COMMA dateArg RPAREN
			        	;

dateArgLP       : dateArg (.)*?
                ;

dateArg         : string
                | dateConstant
                | fieldRef
                ;

// string functions

strfunc         : substr
                | left
                | right
                ;

substr          : SUBSTR LPAREN stringExpr COMMA NUM (COMMA NUM)? RPAREN
                ;

left            : LEFT LPAREN stringExpr COMMA NUM RPAREN
                ;

right           : RIGHT LPAREN stringExpr COMMA NUM RPAREN
                ;

// write statements
writeStatement  : WRITE LPAREN (writeParam)? (COMMA writeParam)*  RPAREN
                ;

writeParam      : source
                | destination
                | procedure
                ;

source          : SOURCE EQ sourceArg
                ;

sourceArg       : VIEW | INPUT | DATA
                ;

destination     : (DESTINATION | DEST) EQ destArg
                ;

destArg         : extractArg | fileArg | DEFAULT
                ;

extractArg      : ( EXTRACT | EXT ) EQ NUM
                ;

fileArg         : FILE EQ file
                ;

file            : '{' DOTTED_NAME '}'
                ;

procedure       : ( PROCEDURE | PROC ) EQ exitArg
                | USEREXIT EQ exitArg
                ;

exitArg         : ( writeExit | LPAREN writeExit COMMA string RPAREN )
                ;

writeExit       : CURLED_NAME
                ;


