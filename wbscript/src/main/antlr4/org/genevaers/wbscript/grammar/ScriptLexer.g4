lexer grammar ScriptLexer;
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
//Keywords

CREATE:                 C R E A T E;
ADD:                    A D D;
SET:                    S E T;

//component Names
CONTROL_RECORD:        C O N T R O L ' ' R E C O R D;
PHYSICAL_FILE:         P H Y S I C A L ' ' F I L E;
LOGICAL_FILE:          L O G I C A L ' ' F I L E;
LOGICAL_RECORD:        L O G I C A L ' ' R E C O R D;
VIEW:                  V I E W;

//Control Record Fields
FIRST_FISCAL:          F I R S T ' ' F I S C A L ' ' M O N T H;
BEGINNING_PERIOD:      B E G I N N I N G ' ' P E R I O D;
ENDING_PERIOD:         E N D I N G ' ' P E R I O D;

//Physical File Fields
FILE_TYPE:             F I L E ' ' T Y P E;
ACCESS_METHOD:         A C C E S S ' ' M E T H O D;
INPUT_DD_NAME:         I N P U T ' ' D D ' ' N A M E;
OUTPUT_DD_NAME:        O U T P U T ' ' D D ' ' N A M E;
MIN_RECORD_LENGTH:     M I N ' ' R E C O R D ' ' L E N G T H;

//Logical File Fields

//Logical Record Fields
FIELD:                 F I E L D;
//Values how do we add accepable Values?  DISK

//View Fields
COLUMN:                C O L U M N;
VIEW_SOURCE:           V I E W ' ' S O U R C E;
COLUMN_SOURCE:         C O L U M N ' ' S O U R C E;

// Column sources
SOURCE_FILE_FIELD:     S O U R C E ' ' F I L E ' ' F I E L D;
CONSTANT:              C O N S T A N T;
FORMULA:               F O R M U L A;
LOOKUP_FIELD:          L O O K U P ' ' F I E L D;


//Common Field names
NAME:                  N A M E;
COMMENTS:              C O M M E N T S;
LENGTH:                L E N G T H;
DATA_TYPE:             D A T A ' ' T Y P E;
TYPE:                  T Y P E;

// Data Type Codes
ALPHA:                  'ALPHA';
NODTF:                  'NODTF';
K_BINARY:               'BINARY';
BCD:                    'BCD';
EDITED:                 'EDITED';
MASKED:                 'MASKED';
PACKED:                 'PACKED';
BINARY:                 'SBINARY';
SPACKED:                'SPACKED';
SZONED:                 'SZONED';
ZONED:                  'ZONED';


/*// Separators
COMMA:                  ',';
SEMICOLON:              ';';
LPAREN:                 '(';
RPAREN:                 ')';

CAST       :  '<' LETTER+ '>';  // No tabs or new lines in cast.

// Operators
EQ:                     '=';
NE:                     '<>';
GT:                     '>';
LT:                     '<';
GE:                     '>=';
LE:                     '<=';
PLUS:                   '+';
MINUS:                  '-';
MULT:                   '*';
DIV:                    '/';
CONCAT:                 '&';*/

//Move to the Parser?
// CAST        : '<'   ALPHA 
//                   | NODTF 
//                   | K_BINARY 
//                   | BCD 
//                   | EDITED 
//                   | MASKED 
//                   | PACKED 
//                   | BINARY 
//                   | SPACKED 
//                   | SZONED 
//                   | ZONED '>'
//             ;

NL		   : '\n';

// Whitespace and Comments
WS : [ \t\r\n]+ -> skip ;

// Comments start with the single quote character ' and run to the end of a line
// You can also put comment after a \ character (which means ignore the rest of this line
// and continue parsing on the next line)
SLCOMMENT  : ( '\'' | '\\' ) ( ~('\n' | '\r') )* -> skip;
//COMMENT    : LETTER+ (LETTER | DIGIT | '_')*;

//Literals
// NB there is no signed integer, instead there is a unary minus operator that can be applied to INTEGER

FLOAT      : DIGITS '.' DIGITS
           ;

NUM        : DIGITS
           ;

STRING     :  '"' ( ~( '\t' | '\n' | '"' ) )* '"';  // No tabs or new lines in strings.

SYMBIDENT  : '$' ( '_' | LETTER | DIGITS )*
           ;

EXP	     : '^'
           ;

META_REF   : LETTER+ (LETTER | DIGIT | '_')*
           ;
           
CURLED_NAME     : LEFTBRACE META_REF RIGHTBRACE
                ;

LEFTBRACE   : '{';
RIGHTBRACE  : '}';

DOTTED_NAME : LETTER+ (LETTER | DIGIT | '_')* '.' LETTER+ (LETTER | DIGIT | '_')*
            ;

COL_REF	   : C O L '.' (DIGIT)+ 
           ;

DIGITS     : DIGIT+
           ;

fragment DIGIT
           : [0-9]
           ;

fragment LETTER
           : [a-zA-Z]
           ;

fragment A : [aA]; // match either an 'a' or 'A'
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
