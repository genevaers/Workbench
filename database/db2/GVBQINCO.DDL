******************************************************************
--
-- (C) COPYRIGHT IBM CORPORATION 2011, 2023.
--     Copyright Contributors to the GenevaERS Project.
-- SPDX-License-Identifier: Apache-2.0
--
-- ******************************************************************
--
--   Licensed under the Apache License, Version 2.0 (the "License");
--   you may not use this file except in compliance with the License.
--   You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
--   or implied.
--   See the License for the specific language governing permissions
--   and limitations under the License.
--
-- ****************************************************************
--
--
--  Insert rows into CODETABLE for the SAFR metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL &$DBSCH. SAFRTEST'
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of schema &$DBSCH.
--
SET CURRENT SQLID='&$DBUSER.';

DELETE FROM &$DBSCH..CODETABLE;

INSERT INTO &$DBSCH..CODETABLE VALUES ('ACCMETHOD','SEQIN',1,
                              'Sequential',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('ACCMETHOD','KSDS', 3,
                              'VSAM - Ordered',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('ACCMETHOD','DB2SQ',6,
                              'Db2 via SQL',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('CODESET','EBCDI',1,
                              'EBCDIC',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('CODESET','ASCII',2,
                              'ASCII',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','ALNUM',1,
                              'Alphanumeric',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','NUMER',3,
                              'Zoned Decimal',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','PACKD',4,
                              'Packed',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','PSORT',5,
                              'Packed Sortable',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','BINRY',6,
                              'Binary',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','BSORT',7,
                              'Binary Sortable',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','BCD', 8,
                              'Binary Coded Decimal',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','MSKNM',9,
                              'Masked Numeric',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DATATYPE','EDNUM',10,
                              'Edited Numeric',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('DBMSROWFMT','SQL',1,
                              'SQL Standard',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('DSORG','PS', 1,
                              'Physical Sequential',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DSORG','DA', 2,
                              'Direct Access',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DSORG','VSAM',3,
                              'VSAM',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('DSORG','PO', 4,
                              'Partitioned',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('EFFDATEKEY','RDATE',1,
                              'Run Date',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EFFDATEKEY','BDATE',2,
                              'Batch Date',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('ENDIAN','BIG', 1,
                              'Big',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('ENDIAN','LITLE',2,
                              'Little',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('EXITTYPE','READ', 1,
                              'Read',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EXITTYPE','WRITE',2,
                              'Write',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EXITTYPE','LKUP', 3,
                              'Lookup',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EXITTYPE','FORMT',4,
                              'Format',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('EXTRACT','SORTK',1,
                              'Sort Key',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EXTRACT','STTLK',2,
                              'Sort Title Key',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EXTRACT','AREDT',3,
                              'DT Area',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('EXTRACT','ARECT',4,
                              'CT Area',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','DISK', 2,
                              'Disk File',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','TAPE', 3,
                              'Tape File',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','PIPE', 4,
                              'Pipe',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','TOKEN', 5,
                              'Token',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','DATAB', 6,
                              'Database',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','REXIT', 7,
                              'Read User-Exit - Standard',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FILETYPE','PEXIT', 8,
                              'Read User-Exit - Pipe',
                              NULL,NULL,NULL,0);
                              
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','YMD', 1,
                              'YYMMDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','Y/M/D',2,
                              'YY/MM/DD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CYMD', 3,
                              'CCYYMMDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CY/MD',4,
                              'CCYY/MM/DD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','DMY', 5,
                              'DDMMYY',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','D/M/Y',6,
                              'DD/MM/YY',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','DMCY', 7,
                              'DDMMCCYY',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','D/MCY',8,
                              'DD/MM/CCYY',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','YYDDD',9,
                              'YYDDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','Y/DDD',10,
                              'YY/DDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CYDDD',11,
                              'CCYYDDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CY/DD',12,
                              'CCYY/DDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','MMDD', 13,
                              'MMDD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','MM/DD',14,
                              'MM/DD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','DD/MM',15,
                              'DD/MM',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','M', 16,
                              'MM',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','DD', 17,
                              'DD',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','HMST', 19,
                              'HHNNSSTT',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','H:MST',20,
                              'HH:NN:SS.TT',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','HMS', 21,
                              'HHNNSS',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','H:M:S',22,
                              'HH:NN:SS',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','HHMM', 23,
                              'HHNN',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','HH:MM',24,
                              'HH:NN',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CYMDH',25,
                              'CCYYMMDDHHNNSS',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CYM', 30,
                              'CCYYMM',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CCYY', 31,
                              'CCYY',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','YY', 32,
                              'YY',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CYAP', 33,
                              'MMM DD CCYY HH:NN:SS.TT AP',
                              'Sybase date',NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','POSIX',34,
                              'CCYY-MM-DD HH:NN:SS.TT',
                              'Posix date',NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','MDY', 35,
                              'MMDDYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','MDCY', 36,
                              'MMDDCCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','MD/CY',37,
                              'MM/DD/CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CY-DD',38,
                              'CCYY-DDD',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CY-M', 39,
                              'CCYY-MM',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','C-M-D',40,
                              'CCYY-MM-DD',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CY/M', 41,
                              'CCYY/MM',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','POS2', 42,
                              'CCYY/MM/DD HH:NN:SS.TT',
                               NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','D-M', 43,
                              'DD-MM',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','D-M-C',44,
                              'DD-MM-CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','D-M-Y',45,
                              'DD-MM-YY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','M-D', 46,
                              'MM-DD',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','M-D-C',47,
                              'MM-DD-CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','Y-DD', 48,
                              'YY-DDD',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','Y-M-D',49,
                              'YY-MM-DD',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','USDAT',50,
                              'MMMM DD, CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','EUDAT',51,
                              'DD MMMM CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','DMOCY',52,
                              'DD-MMM-CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','MMMMY',53,
                              'MMMM CCYY',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDCONTENT','CYMDT',54,
                              'CCYYMMDDHHNNSSTT',NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','COMMA',1,
                              'Comma',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','TAB', 2,
                              'TAB',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','PIPE', 3,
                              'Pipe',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','CTRLA',4,
                              'Ctrl-A',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','SLASH',6,
                              'Slash',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','BLASH',7,
                              'Back Slash',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','COLON',8,
                              'Colon',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','SCOLN',9,
                              'Semicolon',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FLDDELIM','TILDE',10,
                              'Tilde',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LNNAN',01,'No Mask',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LNN0N',02,'-Z',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN10N',03,'-Z9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN11Y',04,'-Z9.9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN12Y',05,'-Z9.99',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN13Y',06,'-Z9.999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN14Y',07,'-Z9.9999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN15Y',08,'-Z9.99999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN16Y',09,'-Z9.999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN17Y',10,'-Z9.9999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN18Y',11,'-Z9.99999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN20N',12,'-Z99',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN30N',13,'-Z999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN40N',14,'-Z9999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN50N',15,'-Z99999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN60N',16,'-Z999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN70N',17,'-Z9999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LN80N',18,'-Z99999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LYN0N',19,'-Z,ZZZ',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY10N',20,'-Z,ZZ9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY11Y',21,'-Z,ZZ9.9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY12Y',22,'-Z,ZZ9.99',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY13Y',23,'-Z,ZZ9.999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY14Y',24,'-Z,ZZ9.9999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY15Y',25,'-Z,ZZ9.99999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY16Y',26,'-Z,ZZ9.999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY17Y',27,'-Z,ZZ9.9999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','LY18Y',28,'-Z,ZZ9.99999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PYN0N',29,'(Z,ZZZ)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PYN1Y',30,'(Z,ZZZ.9)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PYN2Y',31,'(Z,ZZZ.99)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PY10N',32,'(Z,ZZ9)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PY11Y',33,'(Z,ZZ9.9)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PY12Y',34,'(Z,ZZ9.99)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PY20N',35,'(Z,Z99)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PY21Y',36,'(Z,Z99.9)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','PY22Y',37,'(Z,Z99.99)',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RNN0N',38,'Z-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN10N',39,'Z9-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN11Y',40,'Z9.9-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN12Y',41,'Z9.99-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN13Y',42,'Z9.999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN14Y',43,'Z9.9999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN15Y',44,'Z9.99999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN16Y',45,'Z9.999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN17Y',46,'Z9.9999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN18Y',47,'Z9.99999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN20N',48,'Z99-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN30N',49,'Z999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN40N',50,'Z9999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN50N',51,'Z99999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN60N',52,'Z999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN70N',53,'Z9999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RN80N',54,'Z99999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RYN0N',55,'Z,ZZZ-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY10N',56,'Z,ZZ9-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY11Y',57,'Z,ZZ9.9-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY12Y',58,'Z,ZZ9.99-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY13Y',59,'Z,ZZ9.999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY14Y',60,'Z,ZZ9.9999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY15Y',61,'Z,ZZ9.99999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY16Y',62,'Z,ZZ9.999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY17Y',63,'Z,ZZ9.9999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','RY18Y',64,'Z,ZZ9.99999999-',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UNN0N',65,'Z',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN10N',66,'Z9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN11Y',67,'Z9.9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN12Y',68,'Z9.99',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN13Y',69,'Z9.999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN14Y',70,'Z9.9999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN15Y',71,'Z9.99999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN16Y',72,'Z9.999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN17Y',73,'Z9.9999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN18Y',74,'Z9.99999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN20N',75,'Z99',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN30N',76,'Z999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN40N',77,'Z9999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN50N',78,'Z99999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN60N',79,'Z999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN70N',80,'Z9999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UN80N',81,'Z99999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UYN0N',82,'Z,ZZZ',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY10N',83,'Z,ZZ9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY11Y',84,'Z,ZZ9.9',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY12Y',85,'Z,ZZ9.99',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY13Y',86,'Z,ZZ9.999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY14Y',87,'Z,ZZ9.9999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY15Y',88,'Z,ZZ9.99999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY16Y',89,'Z,ZZ9.999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY17Y',90,'Z,ZZ9.9999999',
    NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES
    ('FORMATMASK','UY18Y',91,'Z,ZZ9.99999999',
    NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','PDATE', 1,
                              'ProcessDate',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','PTIME', 2,
                              'ProcessTime',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','PGNUM',3,
                              'PageNumber',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','VWID', 4,
                              'ViewID',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','TEXT', 5,
                              'Text',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','CONAM', 6,
                              'CompanyName',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','VWNAM', 7,
                              'ViewName',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','VWOWN', 8,
                              'OwnerUserID',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S01LB', 11,
                              'SortKey01Label',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S01VL', 12,
                              'SortKey01Value',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S01TT', 13,
                              'SortKey01Title',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S02LB', 21,
                              'SortKey02Label',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S02VL', 22,
                              'SortKey02Value',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S02TT', 23,
                              'SortKey02Title',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S03LB', 31,
                              'SortKey03Label',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S03VL', 32,
                              'SortKey03Value',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S03TT', 33,
                              'SortKey03Title',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S04LB', 41,
                              'SortKey04Label',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S04VL', 42,
                              'SortKey04Value',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S04TT', 43,
                              'SortKey04Title',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S05LB', 51,
                              'SortKey05Label',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S05VL', 52,
                              'SortKey05Value',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','S05TT', 53,
                              'SortKey05Title',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','RDATE', 502,
                              'RunDate',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('FUNCTION','FDATE', 503,
                              'FiscalDate',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('JUSTIFY','LEFT', 1,
                              'Left',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('JUSTIFY','CNTER',2,
                              'Center',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('JUSTIFY','RIGHT',3,
                              'Right',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('LRSTATUS','ACTVE',1,
                              'Active',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('LRSTATUS','INACT',2,
                              'Inactive',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('LRTYPE','FILE',1,
                              'Logical File',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('LRTYPE','VIEW',2,
                              'View Specific Structure',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('OUTPUTMED','HCOPY',1,
                              'Hardcopy',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('OUTPUTMED','FILE', 3,
                              'File',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('OUTPUTMED','DELIM',7,
                              'Delimited',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('PAGEBRK','SAMEP',1,
                              'Same Page',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PAGEBRK','NEWP', 2,
                              'New Page',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PAGEBRK','SUPRS',3,
                              'Suppress Print',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PAGEBRK','TTLN1',4,
                              'Title Line 1',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PAGEBRK','TTLN2',5,
                              'Title Line 2',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PAGEBRK','TTLN3',6,
                              'Title Line 3',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('PROGTYPE','LECOB',1,
                              'LE COBOL',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PROGTYPE','COB2', 2,
                              'COBOL II',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PROGTYPE','C', 3,
                              'C',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PROGTYPE','C++', 4,
                              'Visual C++',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PROGTYPE','Java', 5,
                              'Java',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('PROGTYPE','ASMLR',6,
                              'Assembler',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('RDELIMTYPE','CR', 1,
                              'CR - Carriage Return',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RDELIMTYPE','LF', 2,
                              'LF - Line Feed',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RDELIMTYPE','CRLF',3,
                              'CR/LF - Carriage Return/Line Feed',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('READDISP','SHR',1,
                              'SHR',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('READDISP','OLD',2,
                              'OLD',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('RECDELIM','DELIM',1,
                              'Record Delimited',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RECDELIM','VARIN',4,
                              'Variable Inclusive',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RECDELIM','VAREX',5,
                              'Variable Exclusive',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RECDELIM','FIXED',6,
                              'Fixed Length',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('RECFM','FB', 1,
                              'FB',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RECFM','VB', 2,
                              'VB',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RECFM','FBA',3,
                              'FBA',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RECFM','VBA',4,
                              'VBA',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('REGENOPT','ALWYS',1,
                              'Always',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('REGENOPT','MRGIN',2,
                              'Marginal',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('RELPERIOD','CDATE',1,
                              'Constant (in CCYYMMDD format)',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RELPERIOD','EDATE',2,
                              'Source File Field',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('RELPERIOD','SPECD',3,
                              'Specific Date (Enter CCYYMMDD)',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRK','NOBRK',1,
                              'No Break',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRK','BRK', 2,
                              'Break',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRK','SUPRS',3,
                              'Suppress Label',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRK','LABEL',4,
                              'Subtotal Label Only',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRKFTR','NOPRT',0,
                              'Suppress Print',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRKFTR','PRINT',1,
                              'Print',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRKHDR','PSAME',0,
                              'Print on Same Page',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRKHDR','PNEW', 1,
                              'Print on New Page',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTBRKHDR','NOPRT',2,
                              'Suppress Print',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTDSP','CAT', 1,
                              'Categorize',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTDSP','ASDTA',2,
                              'As Data',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTSEQ','ASCND',1,
                              'Ascending',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SORTSEQ','DSCND',2,
                              'Descending',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('SPACEUNIT','CYL',1,
                              'Cylinders',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SPACEUNIT','TRK',2,
                              'Tracks',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SPACEUNIT','BLK',3,
                              'Blocks',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','ETREC',1,
                              'Event Record',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','EXTRT',2,
                              'Extract Record',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','LKUPK',3,
                              'Lookup Key',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','ETDDN',4,
                              'Event DDN',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','THDNO',5,
                              'Thread Number',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','ANCHR',6,
                              'Anchor',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STDPARM','RTCOD',7,
                              'Return Code',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('STRDELIM','NONE', 0,
                              'No String Delimiter',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STRDELIM','SNGLQ',1,
                              'Single Quote',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('STRDELIM','DBLQT',2,
                              'Double Quote',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','SUM', 2,
                              'Sum',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','MAX', 3,
                              'Maximum',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','MIN', 4,
                              'Minimum',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','FIRST',5,
                              'First',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','LAST', 6,
                              'Last',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','DCALC',7,
                              'Detail Calculation',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','BCALC',8,
                              'Break Calculation',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','DMAX', 10,
                              'Detail Maximum',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('SUBTOT','DMIN', 11,
                              'Detail Minimum',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES('SUBTOT','DFRST',12,
                           'Detail First',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES('SUBTOT','DLAST',13,
                   'Detail Last',NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWSTATUS','ACTVE',1,
                              'Active',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWSTATUS','INACT',2,
                              'Inactive',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWTYPE','SUMRY',1,
                              'Summary',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWTYPE','DETL', 2,
                              'Detail',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWTYPE','DTMRG', 3,
                              'Summary DT Merge',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWTYPE','COPY', 4,
                              'Copy Input',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VIEWTYPE','EXTR', 5,
                              'Extract Only',
                              NULL,NULL,NULL,0);


INSERT INTO &$DBSCH..CODETABLE VALUES ('VSAMORG','KSDS',1,
                              'Key-Sequenced Data Set',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VSAMORG','ESDS',2,
                              'Entry-Sequenced Data Set',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VSAMORG','RRDS',3,
                              'Relative Record Data Set',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('VSAMORG','LSDS',4,
                              'Linear Data Set',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','NDD',1,
                              'New,Delete,Delete',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','NCD',2,
                              'New,Catalog,Delete',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','NCC',3,
                              'New,Catalog,Catalog',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','MDD',4,
                              'Modify,Delete,Delete',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','MCD',5,
                              'Modify,Catalog,Delete',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','MCC',6,
                              'Modify,Catalog,Catalog',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','ODD',7,
                              'Old,Delete,Delete',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','OCD',8,
                              'Old,Catalog,Delete',
                              NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES ('WRITEDISP','OCC',9,
                              'Old,Catalog,Catalog',
                              NULL,NULL,NULL,0);

INSERT INTO &$DBSCH..CODETABLE VALUES('RECORDAGGR','SUM',2,'Sum',
                           NULL,NULL,NULL,NULL);
INSERT INTO &$DBSCH..CODETABLE VALUES('RECORDAGGR','MIN',3,'Minimum',
                           NULL,NULL,NULL,NULL);
INSERT INTO &$DBSCH..CODETABLE VALUES('RECORDAGGR','MAX',4,'Maximum',
                           NULL,NULL,NULL,NULL);
INSERT INTO &$DBSCH..CODETABLE VALUES('RECORDAGGR','CALC',7,
                           'Group Calculation',NULL,NULL,NULL,NULL);

INSERT INTO &$DBSCH..CODETABLE VALUES('GROUPAGGR','SUM',2,'Sum',
                           'ALLOWED_WITH_RECAGGR_SUM',NULL,NULL,NULL);
INSERT INTO &$DBSCH..CODETABLE VALUES('GROUPAGGR','MIN',3,'Minimum',
                           'ALLOWED_WITH_RECAGGR_SUM',NULL,NULL,NULL);
INSERT INTO &$DBSCH..CODETABLE VALUES('GROUPAGGR','MAX',4,'Maximum',
                           'ALLOWED_WITH_RECAGGR_SUM',NULL,NULL,NULL);
INSERT INTO &$DBSCH..CODETABLE VALUES('GROUPAGGR','CALC',7,
                           'Group Calculation',NULL,NULL,NULL,NULL);

INSERT INTO &$DBSCH..CODETABLE VALUES('COLSRCTYPE','CONST',1,
  'Constant', NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES('COLSRCTYPE','SRCFF',3,
  'Source File Field',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES('COLSRCTYPE','LKFLD',2,
  'Lookup Field',NULL,NULL,NULL,0);
INSERT INTO &$DBSCH..CODETABLE VALUES('COLSRCTYPE','FRMLA',4,
  'Formula', NULL,NULL,NULL,0);

