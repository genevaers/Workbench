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
-- Create the C_ tables for the SAFR metadata
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

  CREATE TABLESPACE CODETABL IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;

  CREATE TABLE &$DBSCH..CODETABLE
     (SRCID                CHAR(10) FOR SBCS DATA NOT NULL,
      KEYID                CHAR(5) FOR SBCS DATA NOT NULL,
      GENERALID            INTEGER WITH DEFAULT NULL,
      DESCRIPTION          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      REF1                 VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      REF2                 VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      REF3                 VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      UPDATEFLG            SMALLINT WITH DEFAULT NULL,
      CONSTRAINT PK__CODETABLE
      PRIMARY KEY (SRCID,
                   KEYID))
    IN &$DBNAME..CODETABL
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE CONTRLRC IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..C_CONTROLREC
     (ENVIRONID            INTEGER NOT NULL,
      CONTROLID            INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      CLIENTNAME           VARCHAR(64) FOR SBCS DATA NOT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      FISCALMONTH          INTEGER WITH DEFAULT NULL,
      FISCALYEAR           CHAR(18) FOR SBCS DATA WITH DEFAULT NULL,
      CURRENTSYSDATETIME   CHAR(18) FOR SBCS DATA WITH DEFAULT NULL,
      FIRSTMONTH           INTEGER WITH DEFAULT NULL,
      LOWVALUE             INTEGER WITH DEFAULT NULL,
      HIGHVALUE            INTEGER WITH DEFAULT NULL,
      MAXPARTSEQNBR        INTEGER WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__C_CONTROLREC
      PRIMARY KEY (ENVIRONID,
                   CONTROLID))
    IN &$DBNAME..CONTRLRC
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE ENVIRON IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..C_ENVIRONTBL
     (ENVIRONID            INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      ENVIRON              CHAR(48) FOR SBCS DATA WITH DEFAULT NULL,
      SEQUENCENUM          SMALLINT WITH DEFAULT NULL,
      EDITABLEIND          SMALLINT WITH DEFAULT NULL,
      ARCHIVEIND           SMALLINT WITH DEFAULT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__C_ENVIRONTBL
      PRIMARY KEY (ENVIRONID))
    IN &$DBNAME..ENVIRON
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE NEXTKEY IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..C_NEXTKEYID
     (TABLENAME            CHAR(48) FOR SBCS DATA NOT NULL,
      KEYID                INTEGER WITH DEFAULT NULL,
      CONSTRAINT PK__C_NEXTKEYID
      PRIMARY KEY (TABLENAME))
    IN &$DBNAME..NEXTKEY
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
------------------------------------------------------------------------
  CREATE TABLESPACE GROUPTS IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..C_SECGROUPS
     (SECGROUPID           INTEGER 
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      GROUPDESC            VARCHAR(64) FOR SBCS DATA
        WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__C_SECGROUPS
      PRIMARY KEY (SECGROUPID))
    IN &$DBNAME..GROUPTS
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE USERTS IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..C_USERTBL
     (USERID               CHAR(8) FOR SBCS DATA NOT NULL,
      PASSWORD             VARCHAR(150) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBUSERID             CHAR(30) FOR SBCS DATA WITH DEFAULT NULL,
      DBPASSWORD           VARCHAR(150) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBIDIND              SMALLINT WITH DEFAULT NULL,
      DBSCHEMAIND          SMALLINT WITH DEFAULT NULL,
      DBSCHEMA             VARCHAR(18) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBCONNSTR            VARCHAR(2024) FOR SBCS DATA
        WITH DEFAULT NULL,
      FIRSTNAME            VARCHAR(54) FOR SBCS DATA
        WITH DEFAULT NULL,
      MIDDLEINIT           VARCHAR(1) FOR SBCS DATA WITH DEFAULT NULL,
      LASTNAME             VARCHAR(54) FOR SBCS DATA
        WITH DEFAULT NULL,
      EMAIL                VARCHAR(50) FOR SBCS DATA
        WITH DEFAULT NULL,
      DEFENVIRONID         INTEGER WITH DEFAULT NULL,
      DEFFOLDERID          INTEGER WITH DEFAULT NULL,
      DEFGROUPID           INTEGER WITH DEFAULT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      ADMININD             SMALLINT WITH DEFAULT NULL,
      ADMINSECIND          SMALLINT WITH DEFAULT NULL,
      SECFLAGS             INTEGER WITH DEFAULT NULL,
      EXTRACTMAXRECCNT     INTEGER WITH DEFAULT NULL,
      LOGIN1STTIMESTAMP    TIMESTAMP WITH DEFAULT NULL,
      LOGINLASTTIMESTAMP   TIMESTAMP WITH DEFAULT NULL,
      COMMENTS             VARCHAR(200) FOR SBCS DATA
        WITH DEFAULT NULL,
      ARCHIVEPATH          VARCHAR(500) FOR SBCS DATA
        WITH DEFAULT NULL,
      LOGFILEPATH          VARCHAR(500) FOR SBCS DATA
        WITH DEFAULT NULL,
      PWDCHNGFLG           CHAR(1) FOR SBCS DATA WITH DEFAULT NULL,
      EXPIRYDATE           TIMESTAMP WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__C_USERTBL
      PRIMARY KEY (USERID))
    IN &$DBNAME..USERTS
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE VIEWFOLD IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..C_VIEWFOLDERS
     (ENVIRONID            INTEGER NOT NULL,
      VIEWFOLDERID         INTEGER 
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      DESCRIPTION          VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH1          VARCHAR(254) FOR SBCS DATA WITH DEFAULT NULL,
      CSFILEPATH2          VARCHAR(254) FOR SBCS DATA WITH DEFAULT NULL,
      CSFILEPATH3          VARCHAR(254) FOR SBCS DATA WITH DEFAULT NULL,
      CSFILEPATH4          VARCHAR(254) FOR SBCS DATA WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__C_VIEWFOLDERS
      PRIMARY KEY (ENVIRONID,
                   VIEWFOLDERID))
    IN &$DBNAME..VIEWFOLD
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
