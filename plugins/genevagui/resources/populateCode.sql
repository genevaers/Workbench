--****************************************************************
--
--     Copyright Contributors to the GenevaERS Project.
-- SPDX-License-Identifier: Apache-2.0
--
--***********************************************************************
--*                                                                           
--*   Licensed under the Apache License, Version 2.0 (the "License");         
--*   you may not use this file except in compliance with the License.        
--*   You may obtain a copy of the License at                                 
--*                                                                           
--*     http://www.apache.org/licenses/LICENSE-2.0                            
--*                                                                           
--*   Unless required by applicable law or agreed to in writing, software     
--*   distributed under the License is distributed on an "AS IS" BASIS,       
--*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
--*   or implied.
--*   See the License for the specific language governing permissions and     
--*   limitations under the License.                                          
--***********************************************************************
--  Insert rows into CODE for the GENEVAERS metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL :schemaV SAFRTEST'
--      for PostgreSQL, :schemaV is a passed in variable when executed. 
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of schema :schemaV
--
DELETE FROM :schemaV.CODE;

INSERT INTO :schemaV.CODE VALUES ('ACCMETHOD','SEQIN',1,
                              'Sequential');
INSERT INTO :schemaV.CODE VALUES ('ACCMETHOD','KSDS', 3,
                              'VSAM - Ordered');
INSERT INTO :schemaV.CODE VALUES ('ACCMETHOD','DB2SQ',6,
                              'Db2 via SQL');                             
INSERT INTO :schemaV.CODE VALUES ('CODESET','EBCDI',1,
                              'EBCDIC');
INSERT INTO :schemaV.CODE VALUES ('CODESET','ASCII',2,
                              'ASCII');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','ALNUM',1,
                              'Alphanumeric');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','NUMER',3,
                              'Zoned Decimal');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','PACKD',4,
                              'Packed');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','PSORT',5,
                              'Packed Sortable');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','BINRY',6,
                              'Binary');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','BSORT',7,
                              'Binary Sortable');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','BCD', 8,
                              'Binary Coded Decimal');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','MSKNM',9,
                              'Masked Numeric');
INSERT INTO :schemaV.CODE VALUES ('DATATYPE','EDNUM',10,
                              'Edited Numeric');
                              
INSERT INTO :schemaV.CODE VALUES ('DBMSROWFMT','SQL',1,
                              'SQL Standard');

INSERT INTO :schemaV.CODE VALUES ('DESTTYPE','MAILU', 1,
                              'Email to User');
INSERT INTO :schemaV.CODE VALUES ('DESTTYPE','MAILG', 2,
                              'Email to Group');


INSERT INTO :schemaV.CODE VALUES ('DSORG','PS', 1,
                              'Physical Sequential');
INSERT INTO :schemaV.CODE VALUES ('DSORG','DA', 2,
                              'Direct Access');
INSERT INTO :schemaV.CODE VALUES ('DSORG','VSAM',3,
                              'VSAM');
INSERT INTO :schemaV.CODE VALUES ('DSORG','PO', 4,
                              'Partitioned');

INSERT INTO :schemaV.CODE VALUES ('EFFDATEKEY','RDATE',1,
                              'Run Date');
INSERT INTO :schemaV.CODE VALUES ('EFFDATEKEY','BDATE',2,
                              'Batch Date');

INSERT INTO :schemaV.CODE VALUES ('ENDIAN','BIG', 1,
                              'Big');
INSERT INTO :schemaV.CODE VALUES ('ENDIAN','LITLE',2,
                              'Little');

INSERT INTO :schemaV.CODE VALUES ('EXITTYPE','READ', 1,
                              'Read');
INSERT INTO :schemaV.CODE VALUES ('EXITTYPE','WRITE',2,
                              'Write');
INSERT INTO :schemaV.CODE VALUES ('EXITTYPE','LKUP', 3,
                              'Lookup');
INSERT INTO :schemaV.CODE VALUES ('EXITTYPE','FORMT',4,
                              'Format');

INSERT INTO :schemaV.CODE VALUES ('EXTRACT','SORTK',1,
                              'Sort Key');
INSERT INTO :schemaV.CODE VALUES ('EXTRACT','STTLK',2,
                              'Sort Title Key');
                              
INSERT INTO :schemaV.CODE VALUES ('EXTRACT','AREDT',3,
                              'DT Area');
INSERT INTO :schemaV.CODE VALUES ('EXTRACT','ARECT',4,
                              'CT Area');

INSERT INTO :schemaV.CODE VALUES ('FILETYPE','DISK', 2,
                              'Disk File');
INSERT INTO :schemaV.CODE VALUES ('FILETYPE','TAPE', 3,
                              'Tape File');
INSERT INTO :schemaV.CODE VALUES ('FILETYPE','PIPE', 4,
                              'Pipe');
INSERT INTO :schemaV.CODE VALUES ('FILETYPE','TOKEN', 5,
                              'Token');
INSERT INTO :schemaV.CODE VALUES ('FILETYPE','DATAB', 6,
                              'Database');
INSERT INTO :schemaV.CODE VALUES ('FILETYPE','REXIT', 7,
                        'Read User-Exit - Standard');
INSERT INTO :schemaV.CODE VALUES ('FILETYPE','PEXIT', 8,
                        'Read User-Exit - Pipe');

INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','NONE', 0,
                              'Unspecified');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','YMD', 1,
                              'YYMMDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','Y/M/D',2,
                              'YY/MM/DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CYMD', 3,
                              'CCYYMMDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CY/MD',4,
                              'CCYY/MM/DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','DMY', 5,
                              'DDMMYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','D/M/Y',6,
                              'DD/MM/YY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','DMCY', 7,
                              'DDMMCCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','D/MCY',8,
                              'DD/MM/CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','YYDDD',9,
                              'YYDDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','Y/DDD',10,
                              'YY/DDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CYDDD',11,
                              'CCYYDDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CY/DD',12,
                              'CCYY/DDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','MMDD', 13,
                              'MMDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','MM/DD',14,
                              'MM/DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','DD/MM',15,
                              'DD/MM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','M', 16,
                              'MM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','DD', 17,
                              'DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','HMST', 19,
                              'HHNNSSTT');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','H:MST',20,
                              'HH:NN:SS.TT');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','HMS', 21,
                              'HHNNSS');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','H:M:S',22,
                              'HH:NN:SS');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','HHMM', 23,
                              'HHNN');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','HH:MM',24,
                              'HH:NN');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CYMDH',25,
                              'CCYYMMDDHHNNSS');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CYM', 30,
                              'CCYYMM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CCYY', 31,
                              'CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','YY', 32,
                              'YY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CYAP', 33,
                              'MMM DD CCYY HH:NN:SS.TT AM/PM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','POSIX',34,
                              'CCYY-MM-DD HH:NN:SS.TT');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','MDY', 35,
                              'MMDDYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','MDCY', 36,
                              'MMDDCCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','MD/CY',37,
                              'MM/DD/CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CY-DD',38,
                              'CCYY-DDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CY-M', 39,
                              'CCYY-MM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','C-M-D',40,
                              'CCYY-MM-DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CY/M', 41,
                              'CCYY/MM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','POS2', 42,
                              'CCYY/MM/DD HH:NN:SS.TT');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','D-M', 43,
                              'DD-MM');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','D-M-C',44,
                              'DD-MM-CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','D-M-Y',45,
                              'DD-MM-YY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','M-D', 46,
                              'MM-DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','M-D-C',47,
                              'MM-DD-CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','Y-DD', 48,
                              'YY-DDD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','Y-M-D',49,
                              'YY-MM-DD');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','USDAT',50,
                              'MMMM DD, CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','EUDAT',51,
                              'DD MMMM CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','DMOCY',52,
                              'DD-MMM-CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','MMMMY',53,
                              'MMMM CCYY');
INSERT INTO :schemaV.CODE VALUES ('FLDCONTENT','CYMDT',54,
                              'CCYYMMDDHHNNSSTT');

INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','COMMA',1,
                              'Comma');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','TAB', 2,
                              'TAB');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','PIPE', 3,
                              'Pipe');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','CTRLA',4,
                              'Ctrl-A');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','SLASH',6,
                              'Slash');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','BLASH',7,
                              'Back Slash');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','COLON',8,
                              'Colon');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','SCOLN',9,
                              'Semicolon');
INSERT INTO :schemaV.CODE VALUES ('FLDDELIM','TILDE',10,
                              'Tilde');

INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LNNAN',01,'No Mask');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LNN0N',02,'-Z');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN10N',03,'-Z9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN11Y',04,'-Z9.9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN12Y',05,'-Z9.99');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN13Y',06,'-Z9.999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN14Y',07,'-Z9.9999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN15Y',08,'-Z9.99999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN16Y',09,'-Z9.999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN17Y',10,'-Z9.9999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN18Y',11,'-Z9.99999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN20N',12,'-Z99');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN30N',13,'-Z999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN40N',14,'-Z9999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN50N',15,'-Z99999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN60N',16,'-Z999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN70N',17,'-Z9999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LN80N',18,'-Z99999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LYN0N',19,'-Z,ZZZ');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY10N',20,'-Z,ZZ9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY11Y',21,'-Z,ZZ9.9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY12Y',22,'-Z,ZZ9.99');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY13Y',23,'-Z,ZZ9.999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY14Y',24,'-Z,ZZ9.9999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY15Y',25,'-Z,ZZ9.99999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY16Y',26,'-Z,ZZ9.999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY17Y',27,'-Z,ZZ9.9999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','LY18Y',28,'-Z,ZZ9.99999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PYN0N',29,'(Z,ZZZ)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PYN1Y',30,'(Z,ZZZ.9)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PYN2Y',31,'(Z,ZZZ.99)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PY10N',32,'(Z,ZZ9)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PY11Y',33,'(Z,ZZ9.9)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PY12Y',34,'(Z,ZZ9.99)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PY20N',35,'(Z,Z99)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PY21Y',36,'(Z,Z99.9)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','PY22Y',37,'(Z,Z99.99)');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RNN0N',38,'Z-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN10N',39,'Z9-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN11Y',40,'Z9.9-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN12Y',41,'Z9.99-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN13Y',42,'Z9.999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN14Y',43,'Z9.9999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN15Y',44,'Z9.99999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN16Y',45,'Z9.999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN17Y',46,'Z9.9999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN18Y',47,'Z9.99999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN20N',48,'Z99-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN30N',49,'Z999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN40N',50,'Z9999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN50N',51,'Z99999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN60N',52,'Z999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN70N',53,'Z9999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RN80N',54,'Z99999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RYN0N',55,'Z,ZZZ-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY10N',56,'Z,ZZ9-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY11Y',57,'Z,ZZ9.9-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY12Y',58,'Z,ZZ9.99-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY13Y',59,'Z,ZZ9.999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY14Y',60,'Z,ZZ9.9999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY15Y',61,'Z,ZZ9.99999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY16Y',62,'Z,ZZ9.999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY17Y',63,'Z,ZZ9.9999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','RY18Y',64,'Z,ZZ9.99999999-');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UNN0N',65,'Z');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN10N',66,'Z9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN11Y',67,'Z9.9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN12Y',68,'Z9.99');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN13Y',69,'Z9.999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN14Y',70,'Z9.9999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN15Y',71,'Z9.99999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN16Y',72,'Z9.999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN17Y',73,'Z9.9999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN18Y',74,'Z9.99999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN20N',75,'Z99');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN30N',76,'Z999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN40N',77,'Z9999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN50N',78,'Z99999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN60N',79,'Z999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN70N',80,'Z9999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UN80N',81,'Z99999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UYN0N',82,'Z,ZZZ');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY10N',83,'Z,ZZ9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY11Y',84,'Z,ZZ9.9');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY12Y',85,'Z,ZZ9.99');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY13Y',86,'Z,ZZ9.999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY14Y',87,'Z,ZZ9.9999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY15Y',88,'Z,ZZ9.99999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY16Y',89,'Z,ZZ9.999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY17Y',90,'Z,ZZ9.9999999');
INSERT INTO :schemaV.CODE VALUES
    ('FORMATMASK','UY18Y',91,'Z,ZZ9.99999999');

INSERT INTO :schemaV.CODE VALUES ('FUNCTION','PDATE', 1,
                              'ProcessDate');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','PTIME', 2,
                              'ProcessTime');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','PGNUM',3,
                              'PageNumber');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','VWID', 4,
                              'ViewID');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','TEXT', 5,
                              'Text');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','CONAM', 6,
                              'CompanyName');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','VWNAM', 7,
                              'ViewName');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','VWOWN', 8,
                              'OwnerUserID');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S01LB', 11,
                              'SortKey01Label');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S01VL', 12,
                              'SortKey01Value');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S01TT', 13,
                              'SortKey01Title');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S02LB', 21,
                              'SortKey02Label');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S02VL', 22,
                              'SortKey02Value');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S02TT', 23,
                              'SortKey02Title');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S03LB', 31,
                              'SortKey03Label');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S03VL', 32,
                              'SortKey03Value');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S03TT', 33,
                              'SortKey03Title');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S04LB', 41,
                              'SortKey04Label');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S04VL', 42,
                              'SortKey04Value');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S04TT', 43,
                              'SortKey04Title');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S05LB', 51,
                              'SortKey05Label');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S05VL', 52,
                              'SortKey05Value');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','S05TT', 53,
                              'SortKey05Title');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','RDATE', 502,
                              'RunDate');
INSERT INTO :schemaV.CODE VALUES ('FUNCTION','FDATE', 503,
                              'FiscalDate');

INSERT INTO :schemaV.CODE VALUES ('JUSTIFY','LEFT', 1,
                              'Left');
INSERT INTO :schemaV.CODE VALUES ('JUSTIFY','CNTER',2,
                              'Center');
INSERT INTO :schemaV.CODE VALUES ('JUSTIFY','RIGHT',3,
                              'Right');

INSERT INTO :schemaV.CODE VALUES ('LRSTATUS','ACTVE',1,
                              'Active');
INSERT INTO :schemaV.CODE VALUES ('LRSTATUS','INACT',2,
                              'Inactive');

INSERT INTO :schemaV.CODE VALUES ('LRTYPE','FILE',1,
                              'Logical File');
INSERT INTO :schemaV.CODE VALUES ('LRTYPE','VIEW',2,
                              'View Specific Structure');

INSERT INTO :schemaV.CODE VALUES ('OUTPUTMED','HCOPY',1,
                              'Hardcopy');
INSERT INTO :schemaV.CODE VALUES ('OUTPUTMED','FILE', 3,
                              'File');
INSERT INTO :schemaV.CODE VALUES ('OUTPUTMED','DELIM',7,
                              'Delimited');

INSERT INTO :schemaV.CODE VALUES ('PAGEBRK','SAMEP',1,
                              'Same Page');
INSERT INTO :schemaV.CODE VALUES ('PAGEBRK','NEWP', 2,
                              'New Page');
INSERT INTO :schemaV.CODE VALUES ('PAGEBRK','SUPRS',3,
                              'Suppress Print');
INSERT INTO :schemaV.CODE VALUES ('PAGEBRK','TTLN1',4,
                              'Title Line 1');
INSERT INTO :schemaV.CODE VALUES ('PAGEBRK','TTLN2',5,
                              'Title Line 2');
INSERT INTO :schemaV.CODE VALUES ('PAGEBRK','TTLN3',6,
                              'Title Line 3');

INSERT INTO :schemaV.CODE VALUES ('PROGTYPE','LECOB',1,
                              'LE COBOL');
INSERT INTO :schemaV.CODE VALUES ('PROGTYPE','COB2', 2,
                              'COBOL II');
INSERT INTO :schemaV.CODE VALUES ('PROGTYPE','C', 3,
                              'C');
INSERT INTO :schemaV.CODE VALUES ('PROGTYPE','C++', 4,
                              'Visual C++');
INSERT INTO :schemaV.CODE VALUES ('PROGTYPE','Java', 5,
                              'Java');
INSERT INTO :schemaV.CODE VALUES ('PROGTYPE','ASMLR',6,
                              'Assembler');

INSERT INTO :schemaV.CODE VALUES ('RDELIMTYPE','CR', 1,
                              'CR - Carriage Return');
INSERT INTO :schemaV.CODE VALUES ('RDELIMTYPE','LF', 2,
                              'LF - Line Feed');
INSERT INTO :schemaV.CODE VALUES ('RDELIMTYPE','CRLF',3,
                              'CR/LF - Carriage Return/Line Feed');

INSERT INTO :schemaV.CODE VALUES ('READDISP','SHR',1,
                              'SHR');
INSERT INTO :schemaV.CODE VALUES ('READDISP','OLD',2,
                              'OLD');

INSERT INTO :schemaV.CODE VALUES ('RECDELIM','DELIM',1,
                              'Record Delimited');
INSERT INTO :schemaV.CODE VALUES ('RECDELIM','VARIN',4,
                              'Variable Inclusive');
INSERT INTO :schemaV.CODE VALUES ('RECDELIM','VAREX',5,
                              'Variable Exclusive');
INSERT INTO :schemaV.CODE VALUES ('RECDELIM','FIXED',6,
                              'Fixed Length');

INSERT INTO :schemaV.CODE VALUES ('RECFM','FB', 1,
                              'FB');
INSERT INTO :schemaV.CODE VALUES ('RECFM','VB', 2,
                              'VB');
INSERT INTO :schemaV.CODE VALUES ('RECFM','FBA',3,
                              'FBA');
INSERT INTO :schemaV.CODE VALUES ('RECFM','VBA',4,
                              'VBA');

INSERT INTO :schemaV.CODE VALUES ('REGENOPT','ALWYS',1,
                              'Always');
INSERT INTO :schemaV.CODE VALUES ('REGENOPT','MRGIN',2,
                              'Marginal');

INSERT INTO :schemaV.CODE VALUES ('RELPERIOD','CDATE',1,
                        'Constant (in CCYYMMDD format)');
INSERT INTO :schemaV.CODE VALUES ('RELPERIOD','EDATE',2,
                              'Source File Field');
INSERT INTO :schemaV.CODE VALUES ('RELPERIOD','SPECD',3,
                        'Specific Date (Enter CCYYMMDD)');

INSERT INTO :schemaV.CODE VALUES ('SORTBRK','NOBRK',1,
                              'No Break');
INSERT INTO :schemaV.CODE VALUES ('SORTBRK','BRK', 2,
                              'Break');
INSERT INTO :schemaV.CODE VALUES ('SORTBRK','SUPRS',3,
                              'Suppress Label');
INSERT INTO :schemaV.CODE VALUES ('SORTBRK','LABEL',4,
                              'Subtotal Label Only');

INSERT INTO :schemaV.CODE VALUES ('SORTBRKFTR','NOPRT',0,
                              'Suppress Print');
INSERT INTO :schemaV.CODE VALUES ('SORTBRKFTR','PRINT',1,
                              'Print');

INSERT INTO :schemaV.CODE VALUES ('SORTBRKHDR','PSAME',0,
                              'Print on Same Page');
INSERT INTO :schemaV.CODE VALUES ('SORTBRKHDR','PNEW', 1,
                              'Print on New Page');
INSERT INTO :schemaV.CODE VALUES ('SORTBRKHDR','NOPRT',2,
                              'Suppress Print');

INSERT INTO :schemaV.CODE VALUES ('SORTDSP','CAT', 1,
                              'Categorize');
INSERT INTO :schemaV.CODE VALUES ('SORTDSP','ASDTA',2,
                              'As Data');

INSERT INTO :schemaV.CODE VALUES ('SORTSEQ','ASCND',1,
                              'Ascending');
INSERT INTO :schemaV.CODE VALUES ('SORTSEQ','DSCND',2,
                              'Descending');

INSERT INTO :schemaV.CODE VALUES ('SPACEUNIT','CYL',1,
                              'Cylinders');
INSERT INTO :schemaV.CODE VALUES ('SPACEUNIT','TRK',2,
                              'Tracks');
INSERT INTO :schemaV.CODE VALUES ('SPACEUNIT','BLK',3,
                              'Blocks');

INSERT INTO :schemaV.CODE VALUES ('STDPARM','ETREC',1,
                              'Event Record');
INSERT INTO :schemaV.CODE VALUES ('STDPARM','EXTRT',2,
                              'Extract Record');
INSERT INTO :schemaV.CODE VALUES ('STDPARM','LKUPK',3,
                              'Lookup Key');
INSERT INTO :schemaV.CODE VALUES ('STDPARM','ETDDN',4,
                              'Event DDN');
INSERT INTO :schemaV.CODE VALUES ('STDPARM','THDNO',5,
                              'Thread Number');
INSERT INTO :schemaV.CODE VALUES ('STDPARM','ANCHR',6,
                              'Anchor');
INSERT INTO :schemaV.CODE VALUES ('STDPARM','RTCOD',7,
                              'Return Code');

INSERT INTO :schemaV.CODE VALUES ('STRDELIM','NONE', 0,
                              'No String Delimiter');
INSERT INTO :schemaV.CODE VALUES ('STRDELIM','SNGLQ',1,
                              'Single Quote');
INSERT INTO :schemaV.CODE VALUES ('STRDELIM','DBLQT',2,
                              'Double Quote');

INSERT INTO :schemaV.CODE VALUES ('SUBTOT','SUM', 2,
                              'Sum');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','MAX', 3,
                              'Maximum');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','MIN', 4,
                              'Minimum');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','FIRST',5,
                              'First');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','LAST', 6,
                              'Last');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','DCALC',7,
                              'Detail Calculation');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','BCALC',8,
                              'Break Calculation');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','DMAX', 10,
                              'Detail Maximum');
INSERT INTO :schemaV.CODE VALUES ('SUBTOT','DMIN', 11,
                              'Detail Minimum');

INSERT INTO :schemaV.CODE VALUES('SUBTOT','DFRST',12,
                           'Detail First');
INSERT INTO :schemaV.CODE VALUES('SUBTOT','DLAST',13,
                   'Detail Last');

INSERT INTO :schemaV.CODE VALUES ('VIEWSTATUS','ACTVE',1,
                              'Active');
INSERT INTO :schemaV.CODE VALUES ('VIEWSTATUS','INACT',2,
                              'Inactive');

INSERT INTO :schemaV.CODE VALUES ('VIEWTYPE','SUMRY',1,
                              'Summary');
INSERT INTO :schemaV.CODE VALUES ('VIEWTYPE','DETL', 2,
                              'Detail');
INSERT INTO :schemaV.CODE VALUES ('VIEWTYPE','DTMRG', 3,
                              'Summary DT Merge');
INSERT INTO :schemaV.CODE VALUES ('VIEWTYPE','COPY', 4,
                              'Copy Input');
INSERT INTO :schemaV.CODE VALUES ('VIEWTYPE','EXTR', 5,
                              'Extract Only');

INSERT INTO :schemaV.CODE VALUES ('VSAMORG','KSDS',1,
                              'Key-Sequenced Data Set');
INSERT INTO :schemaV.CODE VALUES ('VSAMORG','ESDS',2,
                              'Entry-Sequenced Data Set');
INSERT INTO :schemaV.CODE VALUES ('VSAMORG','RRDS',3,
                              'Relative Record Data Set');
INSERT INTO :schemaV.CODE VALUES ('VSAMORG','LSDS',4,
                              'Linear Data Set');

INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','NDD',1,
                              'New,Delete,Delete');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','NCD',2,
                              'New,Catalog,Delete');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','NCC',3,
                              'New,Catalog,Catalog');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','MDD',4,
                              'Modify,Delete,Delete');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','MCD',5,
                              'Modify,Catalog,Delete');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','MCC',6,
                              'Modify,Catalog,Catalog');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','ODD',7,
                              'Old,Delete,Delete');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','OCD',8,
                              'Old,Catalog,Delete');
INSERT INTO :schemaV.CODE VALUES ('WRITEDISP','OCC',9,
                              'Old,Catalog,Catalog');

INSERT INTO :schemaV.CODE VALUES('RECORDAGGR','SUM',2,'Sum');
INSERT INTO :schemaV.CODE VALUES('RECORDAGGR','MIN',3,'Minimum');
INSERT INTO :schemaV.CODE VALUES('RECORDAGGR','MAX',4,'Maximum');
INSERT INTO :schemaV.CODE VALUES('RECORDAGGR','CALC',7,
                           'Group Calculation');

INSERT INTO :schemaV.CODE VALUES('GROUPAGGR','SUM',2,'Sum');

INSERT INTO :schemaV.CODE VALUES('GROUPAGGR','MIN',3,'Minimum');

INSERT INTO :schemaV.CODE VALUES('GROUPAGGR','MAX',4,'Maximum');

INSERT INTO :schemaV.CODE VALUES('GROUPAGGR','CALC',7,
                           'Group Calculation');

INSERT INTO :schemaV.CODE VALUES('COLSRCTYPE','CONST',1,
                'Constant');
INSERT INTO :schemaV.CODE VALUES('COLSRCTYPE','SRCFF',3,
  'Source File Field');
INSERT INTO :schemaV.CODE VALUES('COLSRCTYPE','LKFLD',2,
      'Lookup Field');
INSERT INTO :schemaV.CODE VALUES('COLSRCTYPE','FRMLA',4,
                 'Formula');
