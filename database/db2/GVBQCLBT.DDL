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
-- Create the E_LOGICTBL table for the SAFR metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL &$DBSCH. SAFRTEST'
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of storage group &$DBSG.
-- 3) Replace occurrences of database &$DBNAME.
-- 4) Replace occurrences of schema &$DBSCH.
--
SET CURRENT SQLID='&$DBUSER.';
SET CURRENT APPLICATION COMPATIBILITY = 'V12R1M503';

CREATE TABLESPACE LOGIC IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;

CREATE TABLE &$DBSCH..E_LOGICTBL
     (ENVIRONID            INTEGER NOT NULL,
      VIEWID               INTEGER NOT NULL,
      TYPECD               SMALLINT NOT NULL,
      LOGIC_ROWID          ROWID NOT NULL GENERATED ALWAYS,
      LOGIC                BLOB(32 M) WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_LOGICTBL
      PRIMARY KEY (ENVIRONID,
                   VIEWID,
                   TYPECD))
    IN &$DBNAME..LOGIC
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
  
  CREATE LOB TABLESPACE LOGICBLB
    IN &$DBNAME.
    USING STOGROUP &$DBSG.
    LOG YES
    DSSIZE 4 G
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE YES;
  COMMIT;

  CREATE AUXILIARY TABLE &$DBSCH..E_LOGICTBL_LOB
    IN &$DBNAME..LOGICBLB
    STORES &$DBSCH..E_LOGICTBL COLUMN LOGIC;
  COMMIT;
