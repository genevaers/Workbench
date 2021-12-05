lexer grammar GenevaERSLexer;
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

SOURCE:                 S O U R C E;
DESTINATION:            D E S T I N A T I O N;
DEST:                   D E S T;
PROCEDURE:              P R O C E D U R E;
PROC:                   P R O C;
EXTRACT:                E X T R A C T;
EXT:                    E X T;
FILE:                   F I L E;
OR:                     O R;
AND:                    A N D;
LHS:                    L H S;
NOT:                    N O T;
REPEAT:                 R E P E A T;
ALL:                    A L L;
SELECT:                 S E L E C T;
SELECTIF:               S E L E C T I F;
SKIPF:                  S K I P;
SKIPIF:                 S K I P I F;
COLUMN:                 C O L U M N;
IF:                     I F;
THEN:                   T H E N;
ELSE:                   E L S E; 
ENDIF:                  E N D I F;
LIKE:                   L I K E;
MATCHES:                M A T C H E S;
CONTAINS:               C O N T A I N S;
BEGINS_WITH:            B E G I N S '_' W I T H;
ENDS_WITH:              E N D S '_' W I T H;
DAYSBETWEEN:            D A Y S B E T W E E N;
MONTHSBETWEEN:          M O N T H S B E T W E E N;
YEARSBETWEEN:           Y E A R S B E T W E E N;
NEWDATE:                N E W D A T E;
DATE:                   D A T E;

// Date Codes
CCYY:                   C C Y Y;
CCYYDDD:                C C Y Y D D D;
CCYYMMDD:               C C Y Y M M D D;
MMDDCCYY:               M M D D C C Y Y;
MMDDYY:                 M M D D Y Y;
CCYYMM:                 C C Y Y M M;
DDMMYY:                 D D M M Y Y;
DDMMCCYY:               D D M M C C Y Y;
DD:                     D D;
MM:                     M M;
MMDD:                   M M D D;
YYDDD:                  Y Y D D D;
YYMMDD:                 Y Y M M D D;
YY:                     Y Y;

// Date Function Codes
RUNDAY:                 R U N D A Y;
RUNMONTH:               R U N M O N T H;
RUNYEAR:                R U N Y E A R;
FISCALDAY:              F I S C A L D A Y;
FISCALMONTH:            F I S C A L M O N T H ;
FISCALYEAR:             F I S C A L Y E A R;
TIMESTAMP:              T I M E S T A M P;
BATCHDATE:              B A T C H D A T E;

// Is Function Codes
ISSPACES:               I S S P A C E S;
ISNOTSPACES:            I S N O T S P A C E S;
ISNUMERIC:              I S N U M E R I C;
ISNOTNUMERIC:           I S N O T N U M E R I C;
ISNULL:                 I S N U L L;
ISNOTNULL:              I S N O T N U L L;
ISFOUND:                I S F O U N D;
ISNOTFOUND:             I S N O T F O U N D;

// Field Prefixes
CURRENT:                C U R R E N T;
PRIOR:                  P R I O R;

// Write Function stuff
WRITE:                  W R I T E;
VIEW:                   V I E W;
INPUT:                  I N P U T;
DATA:                   D A T A;
MOD:                    M O D;
USEREXIT:               U S E R E X I T;
DEFAULT:                D E F A U L T;

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

// String function codes
SUBSTR:                 S U B S T R;
LEFT:                   L E F T;
RIGHT:                  R I G H T;

// These are internal rule things?
LRFIELD_REF:            'LRFIELD_REF';
LP_REF:                 'LP_REF';
LPFIELD_REF:            'LPFIELD_REF';
FILE_REF:               'FILE_REF';
PROC_REF:               'PROC_REF';
COLUMN_ASSIGN:          'COLUMN_ASSIGN';
COLREF_ASSIGN:          'COLREF_ASSIGN';
LPSOURCE_KEY:           'LPSOURCE_KEY';
SYMBOL_LIST:            'SYMBOL_LIST';
LONG_NUM:               'LONG_NUM';

// Separators
COMMA:                  ',';
SEMICOLON:              ';';
LPAREN:                 '(';
RPAREN:                 ')';

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
CONCAT:                 '&';

//NL		   : '\n';

// Whitespace and Comments
WS : [ \t\r\n]+ -> skip ;

// Comments start with the single quote character ' and run to the end of a line
// You can also put comment after a \ character (which means ignore the rest of this line
// and continue parsing on the next line)
SLCOMMENT  : ( '\'' | '\\' ) ( ~('\n') )*;


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
           
CURLED_NAME     : '{' META_REF '}'
                ;

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
