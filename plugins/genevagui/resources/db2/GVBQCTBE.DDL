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
-- Create the E_ tables for the SAFR metadata
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
-----------------------------------------------------------------------
  CREATE TABLESPACE LOGFILE IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;

  CREATE TABLE &$DBSCH..E_FILETBL
     (ENVIRONID            INTEGER NOT NULL,
      FILEID               INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      FILENAME             VARCHAR(48) FOR SBCS DATA NOT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_FILETBL
      PRIMARY KEY (ENVIRONID,
                   FILEID))
    IN &$DBNAME..LOGFILE
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE GLOBAFLD IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_FLDTBL
     (ENVIRONID            INTEGER NOT NULL,
      FLDID                INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      FLD                  VARCHAR(48) FOR SBCS DATA NOT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      FLDFMTCD             CHAR(5) FOR SBCS DATA NOT NULL,
      SIGNEDIND            SMALLINT WITH DEFAULT NULL,
      MAXLEN               INTEGER WITH DEFAULT NULL,
      DECIMALCNT           INTEGER WITH DEFAULT NULL,
      ROUNDING             INTEGER WITH DEFAULT NULL,
      FLDCONTENTCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      JUSTIFYCD            CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      RELFLDTYPECD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      RELFLDID             INTEGER WITH DEFAULT NULL,
      DEFAULTVAL           VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      SRCXLRFLDID          INTEGER WITH DEFAULT NULL,
      SRCLOGICID           INTEGER WITH DEFAULT NULL,
      SRCPROGRAMID         INTEGER WITH DEFAULT NULL,
      HDRJUSTIFYCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      HDRLINE1             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      HDRLINE2             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      HDRLINE3             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      FLDLABEL             VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      SUBTLABEL            VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      SORTKEYLABEL         VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      INPUTMASK            VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      RPTMASK              VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      RPTMAXLEN            INTEGER WITH DEFAULT NULL,
      XMLTAG               VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_FLDTBL
      PRIMARY KEY (ENVIRONID,
                   FLDID))
    IN &$DBNAME..GLOBAFLD
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE LOOKUPSR IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_JOINSOURCEKEYTBL
     (ENVIRONID            INTEGER NOT NULL,
      XJOINSTEPID          INTEGER NOT NULL,
      KEYSEQNBR            INTEGER NOT NULL,
      SRCFLDTYPE           INTEGER WITH DEFAULT NULL,
      SRCXLRFLDID          INTEGER WITH DEFAULT NULL,
      SRCXLRFILEID         INTEGER WITH DEFAULT NULL,
      SRCJOINID            INTEGER WITH DEFAULT NULL,
      SRCVALUEFMTCD        CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      SIGNED               SMALLINT WITH DEFAULT NULL,
      STARTPOSITION        INTEGER WITH DEFAULT NULL,
      SRCVALUELEN          INTEGER WITH DEFAULT NULL,
      DECIMALCNT           INTEGER WITH DEFAULT NULL,
      FLDCONTENTCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ROUNDING             INTEGER WITH DEFAULT NULL,
      JUSTIFYCD            CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      MASK                 CHAR(48) FOR SBCS DATA WITH DEFAULT NULL,
      SYMBOLICNAME         VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      SRCVALUE             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      EFFDATEFIELDIND      SMALLINT WITH DEFAULT NULL,
      KEYXLRFLDID          INTEGER WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_JOINSOURCEKE
      PRIMARY KEY (ENVIRONID,
                   XJOINSTEPID,
                   KEYSEQNBR))
    IN &$DBNAME..LOOKUPSR
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
----------------------------------------------------------------------
  CREATE TABLESPACE LOOKUP IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_JOINTBL
     (ENVIRONID            INTEGER NOT NULL,
      JOINID               INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      JOINNAME             VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      SRCLRID              INTEGER WITH DEFAULT NULL,
      DESTXLRFILEID        INTEGER WITH DEFAULT NULL,
      VALIDIND             SMALLINT WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTACTTIMESTAMP     TIMESTAMP WITH DEFAULT NULL,
      LASTACTUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_JOINTBL
      PRIMARY KEY (ENVIRONID,
                   JOINID))
    IN &$DBNAME..LOOKUP
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
---------------------------------------------------------------------
  CREATE TABLESPACE LOGRECIX IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_LRINDEXTBL
     (ENVIRONID            INTEGER NOT NULL,
      LRINDEXID            INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      LRINDEX              VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      LRID                 INTEGER NOT NULL,
      EFFDATESTARTFLDID    INTEGER WITH DEFAULT NULL,
      EFFDATEENDFLDID      INTEGER WITH DEFAULT NULL,
      RANGEFLDID           INTEGER WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_LRINDEXTBL
      PRIMARY KEY (ENVIRONID,
                   LRINDEXID))
    IN &$DBNAME..LOGRECIX
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
---------------------------------------------------------------------
  CREATE TABLESPACE LOGREC IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_LRTBL
     (ENVIRONID            INTEGER NOT NULL,
      LRID                 INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      LR                   VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      PKINDEXID            INTEGER WITH DEFAULT NULL,
      LRTYPECD             CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      LRSTATUSCD           CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      LOOKUPEXITID         INTEGER WITH DEFAULT NULL,
      LOOKUPEXITSTARTUP    VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      XMLTAG               VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTACTTIMESTAMP     TIMESTAMP WITH DEFAULT NULL,
      LASTACTUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_LRTBL
      PRIMARY KEY (ENVIRONID,
                   LRID))
    IN &$DBNAME..LOGREC
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
---------------------------------------------------------------------
  CREATE TABLESPACE MIGRAUDT IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_MIGAUDITTBL
     (AUDITID              INTEGER NOT NULL GENERATED ALWAYS
        AS IDENTITY
          (START WITH 1, INCREMENT BY 1, NO CACHE, NO CYCLE,
           ORDER, MAXVALUE 2147483647, MINVALUE 1),
      SRCENVIRONID         INTEGER NOT NULL,
      DESTENVIRONID        INTEGER NOT NULL,
      ENTITYTYPE           VARCHAR(28) FOR SBCS DATA NOT NULL,
      ENTITYID             INTEGER NOT NULL,
      ENTITYNAME           VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      MIGRATIONDATE        TIMESTAMP WITH DEFAULT NULL,
      MIGRATEDBY           CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      MESSAGETEXT          VARCHAR(3000) FOR SBCS DATA
        WITH DEFAULT NULL,
      CONSTRAINT PK__E_MIGAUDITTBL
      PRIMARY KEY (AUDITID))
    IN &$DBNAME..MIGRAUDT
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE PHYSFILE IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_PARTITIONTBL
     (ENVIRONID            INTEGER NOT NULL,
      PARTITIONID          INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      PARTITIONNAME        VARCHAR(48) FOR SBCS DATA NOT NULL,
      FILETYPECD           CHAR(5) FOR SBCS DATA NOT NULL,
      SERVERID             INTEGER WITH DEFAULT 1,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      ACCESSMETHODCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      CODESETCD            CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ENDIANCD             CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      DYNAMICALLOCIND      SMALLINT WITH DEFAULT NULL,
      RECLEN               INTEGER WITH DEFAULT NULL,
      MAXRECLEN            INTEGER WITH DEFAULT NULL,
      OUTPUTMAXRECCNT      INTEGER WITH DEFAULT NULL,
      READEXITID           INTEGER WITH DEFAULT NULL,
      READEXITSTARTUP      VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      RECDELIMTYPECD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      RECORDDELIMITERCD    CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      FIELDDELIMITERCD     CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      STRINGDELIMITERCD    CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      CNTLRECTYPECD        CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      VERNBRSTARTPOS       INTEGER WITH DEFAULT NULL,
      VERNBRLEN            INTEGER WITH DEFAULT NULL,
      VERNBRFLDFMTCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      RECCNTSTARTPOS       INTEGER WITH DEFAULT NULL,
      RECCNTLEN            INTEGER WITH DEFAULT NULL,
      RECCNTFLDFMTCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      STARTRECORD          INTEGER WITH DEFAULT NULL,
      ENDRECORD            INTEGER WITH DEFAULT NULL,
      EXTRACTTHREADNBR     INTEGER WITH DEFAULT NULL,
      FILENAMEISROOTIND    SMALLINT WITH DEFAULT NULL,
      THREADPERVOLUMEIND   SMALLINT WITH DEFAULT NULL,
      CSFILEPATH1          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH2          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH3          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH4          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILENAME           VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBMSCONNSTRING       VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBMSDATABASE         VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBMSTABLE            VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      DBMSROWFMTCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      DBMSINCLNULLSIND     SMALLINT WITH DEFAULT NULL,
      DBMSSQL              VARCHAR(1280) FOR SBCS DATA
        WITH DEFAULT NULL,
      DSN                  VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      DDNAMEINPUT          CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      DDNAMEEXTRACT        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      DDNAMEOUTPUT         CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      READDISPCD           CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      WRITEDISPCD          CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCDEVICETYPE      CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCSPACEUNITCD     CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCSPACEPRIMQTY    INTEGER WITH DEFAULT NULL,
      ALLOCSPACESECQTY     INTEGER WITH DEFAULT NULL,
      ALLOCRELUNUSEDIND    SMALLINT WITH DEFAULT NULL,
      ALLOCLRECL           INTEGER WITH DEFAULT NULL,
      ALLOCBLKSIZE         INTEGER WITH DEFAULT NULL,
      ALLOCKEYLEN          INTEGER WITH DEFAULT NULL,
      ALLOCKEYPOS          INTEGER WITH DEFAULT NULL,
      ALLOCDSORGCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCVSAMORGCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCRECFMCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCOPTCD           VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      ALLOCMEMBERNAME      CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCVOLSER          CHAR(6) FOR SBCS DATA WITH DEFAULT NULL,
      ALLOCVOLSEQNBR       INTEGER WITH DEFAULT NULL,
      ALLOCVOLCNT          INTEGER WITH DEFAULT NULL,
      ALLOCSYSOUT          VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      ALLOCSYSOUTCOPIES    INTEGER WITH DEFAULT NULL,
      ALLOCSYSOUTOUTLIM    INTEGER WITH DEFAULT NULL,
      ALLOCSYSOUTHOLDIND   SMALLINT WITH DEFAULT NULL,
      ALLOCMODELDSCB       VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      RETENTIONPERIOD      INTEGER WITH DEFAULT NULL,
      EXPIRATIONDATE       TIMESTAMP WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      DISKFILETYPECD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_FILEPARTITIO
      PRIMARY KEY (ENVIRONID,
                   PARTITIONID))
    IN &$DBNAME..PHYSFILE
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
----------------------------------------------------------------------
  CREATE TABLESPACE USEREXIT IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_PROGRAMTBL
     (ENVIRONID            INTEGER NOT NULL,
      PROGRAMID            INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      "PROGRAM"            VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      MODULEID             CHAR(18) FOR SBCS DATA WITH DEFAULT NULL,
      EXITTYPECD           CHAR(5) FOR SBCS DATA NOT NULL,
      PROGRAMTYPECD        CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      OPTIMIZEIND          SMALLINT WITH DEFAULT NULL,
      CSFILEPATH1          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH2          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH3          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILEPATH4          VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CSFILENAME           VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_PROGRAMTBL
      PRIMARY KEY (ENVIRONID,
                   PROGRAMID))
    IN &$DBNAME..USEREXIT
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
---------------------------------------------------------------------
  CREATE TABLESPACE SERVER IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_SERVERTBL
     (ENVIRONID            INTEGER NOT NULL,
      SERVERID             INTEGER NOT NULL,
      COMPUTERNAME         VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      DESCRIPTIVENAME      VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      SERVERTYPECD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      DRIVERCD             CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      DBNAME               VARCHAR(64) FOR SBCS DATA
        WITH DEFAULT NULL,
      USERID               VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      PASSWORD             VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      INTSECFLAG           SMALLINT WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_SERVERTBL
      PRIMARY KEY (ENVIRONID,
                   SERVERID))
    IN &$DBNAME..SERVER
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE VDPBATCH IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_VDPBATCH
     (ENVIRONID            INTEGER NOT NULL,
      BATCHID              INTEGER NOT NULL,
      BATCHDATE            TIMESTAMP WITH DEFAULT NULL,
      BIGENDIAN            SMALLINT WITH DEFAULT NULL,
      ASCII                SMALLINT WITH DEFAULT NULL,
      VERSIONINFO          INTEGER WITH DEFAULT NULL,
      SERVERCOUNT          INTEGER WITH DEFAULT NULL,
      INPUTFILECOUNT       INTEGER WITH DEFAULT NULL,
      PGMFILECOUNT         INTEGER WITH DEFAULT NULL,
      LRCOUNT              INTEGER WITH DEFAULT NULL,
      LRFIELDCOUNT         INTEGER WITH DEFAULT NULL,
      LRINDEXFIELDCOUNT    INTEGER WITH DEFAULT NULL,
      JOINSTEPCOUNT        INTEGER WITH DEFAULT NULL,
      CALLEDPGMPARMCOUNT   INTEGER WITH DEFAULT NULL,
      VIEWCOUNT            INTEGER WITH DEFAULT NULL,
      EXTRACTRECCOUNT      INTEGER WITH DEFAULT NULL,
      RUNCPUSECONDS        INTEGER WITH DEFAULT NULL,
      MAXEXTRACTFILENUM    INTEGER WITH DEFAULT NULL,
      CONSTRAINT PK__E_VDPBATCH
      PRIMARY KEY (ENVIRONID,
                   BATCHID))
    IN &$DBNAME..VDPBATCH
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
------------------------------------------------------------------------
  CREATE TABLESPACE VDPBTCHV IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_VDPBATCHVIEWS
     (ENVIRONID            INTEGER NOT NULL,
      BATCHVIEWID          INTEGER NOT NULL,
      BATCHID              INTEGER NOT NULL,
      VIEWID               INTEGER NOT NULL,
      SUCCESS              SMALLINT WITH DEFAULT NULL,
      STARTTIME            TIMESTAMP WITH DEFAULT NULL,
      ENDTIME              TIMESTAMP WITH DEFAULT NULL,
      CONSTRAINT PK__E_VDPBATCHVIEW
      PRIMARY KEY (ENVIRONID,
                   BATCHVIEWID))
    IN &$DBNAME..VDPBTCHV
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
------------------------------------------------------------------------
  CREATE TABLESPACE VIEWHDFT IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_VIEWHEADERFOOTER
     (ENVIRONID            INTEGER NOT NULL,
      HEADERFOOTERID       INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      VIEWID               INTEGER WITH DEFAULT NULL,
      STDFUNCCD            CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      JUSTIFYCD            CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      ROWNUMBER            INTEGER WITH DEFAULT NULL,
      COLNUMBER            INTEGER WITH DEFAULT NULL,
      LENGTH               INTEGER WITH DEFAULT NULL,
      MASK                 VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      ITEMTEXT             VARCHAR(255) FOR SBCS DATA
        WITH DEFAULT NULL,
      HEADERFOOTERIND      SMALLINT WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_VIEWHEADERFO
      PRIMARY KEY (ENVIRONID,
                   HEADERFOOTERID))
    IN &$DBNAME..VIEWHDFT
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
-----------------------------------------------------------------------
  CREATE TABLESPACE VIEWTS IN &$DBNAME.
    USING STOGROUP &$DBSG.
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE NO
    CCSID EBCDIC;
    
  CREATE TABLE &$DBSCH..E_VIEWTBL
     (ENVIRONID            INTEGER NOT NULL,
      VIEWID               INTEGER
        GENERATED BY DEFAULT AS IDENTITY
        (NO CACHE ORDER) NOT NULL,
      VIEWNAME             VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      EFFDATE              TIMESTAMP WITH DEFAULT NULL,
      TERMDATE             TIMESTAMP WITH DEFAULT NULL,
      VIEWFOLDERID         INTEGER WITH DEFAULT 0,
      VIEWSTATUSCD         CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      VIEWTYPECD           CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      OWNERUSERID          CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      FREQUENCYCD          CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      FREQPASS             SMALLINT WITH DEFAULT NULL,
      EXTRACTFILEID        INTEGER WITH DEFAULT NULL,
      EXTRACTFILEPARTNBR   INTEGER WITH DEFAULT NULL,
      OUTPUTMEDIACD        CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      OUTPUTLRID           INTEGER WITH DEFAULT NULL,
      XFILEPARTITIONID     INTEGER WITH DEFAULT NULL,
      OUTPUTDESTID         INTEGER WITH DEFAULT NULL,
      OUTPUTDESTSTRING     VARCHAR(48) FOR SBCS DATA
        WITH DEFAULT NULL,
      OUTPUTRECLEN         INTEGER WITH DEFAULT NULL,
      PAGESIZE             INTEGER WITH DEFAULT NULL,
      LINESIZE             INTEGER WITH DEFAULT NULL,
      HEADERLINESMAX       INTEGER WITH DEFAULT NULL,
      NUMBEROFCOPIES       INTEGER WITH DEFAULT NULL,
      OUTPUTDTLIND         SMALLINT WITH DEFAULT NULL,
      ZEROSUPPRESSIND      SMALLINT WITH DEFAULT NULL,
      REGENREQDIND         SMALLINT WITH DEFAULT NULL,
      REGENOPTCD           CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      EXTRACTFORMATCD      INTEGER WITH DEFAULT NULL,
      EXTRACTMAXRECCNT     INTEGER WITH DEFAULT NULL,
      EXTRACTSUMMARYIND    SMALLINT WITH DEFAULT NULL,
      EXTRACTSUMMARYBUF    INTEGER WITH DEFAULT NULL,
      OUTPUTMAXRECCNT      INTEGER WITH DEFAULT NULL,
      OUTPUTTITLEOPTID     INTEGER WITH DEFAULT NULL,
      OUTPUTFOOTEROPTID    INTEGER WITH DEFAULT NULL,
      PAGEORIENTATIONCD    CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      PAGESIZECD           CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      CONTROLID            INTEGER NOT NULL,
      PROCESSASOF          TIMESTAMP WITH DEFAULT NULL,
      LOOKUPASOF           TIMESTAMP WITH DEFAULT NULL,
      PROCRELPERIODCD      CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      LOOKUPRELPERIODCD    CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      VALIDIND             SMALLINT WITH DEFAULT NULL,
      WRITEEXITID          INTEGER WITH DEFAULT NULL,
      WRITEEXITSTARTUP     VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      FORMATEXITID         INTEGER WITH DEFAULT NULL,
      FORMATEXITSTARTUP    VARCHAR(32) FOR SBCS DATA
        WITH DEFAULT NULL,
      FILERECTYPECD        CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      FILERECDELIMCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      FILEFLDDELIMCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      FILESTRDELIMCD       CHAR(5) FOR SBCS DATA WITH DEFAULT NULL,
      DIRECTORYIND         SMALLINT WITH DEFAULT NULL,
      DIRECTORYID          INTEGER WITH DEFAULT NULL,
      FILLERROR            VARCHAR(64) FOR SBCS DATA
        WITH DEFAULT NULL,
      FILLTRUNCATION       VARCHAR(64) FOR SBCS DATA
        WITH DEFAULT NULL,
      FILLOVERFLOW         VARCHAR(64) FOR SBCS DATA
        WITH DEFAULT NULL,
      CHECKOUTTIMESTAMP    TIMESTAMP WITH DEFAULT NULL,
      CHECKOUTUSERID       CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      XMLTAG               VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      COMMENTS             VARCHAR(254) FOR SBCS DATA
        WITH DEFAULT NULL,
      CREATEDTIMESTAMP     TIMESTAMP WITH DEFAULT,
      CREATEDUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      LASTMODTIMESTAMP     TIMESTAMP WITH DEFAULT,
      LASTMODUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      FORMATFILTLOGIC      CLOB(32 M),
      COMPILER             VARCHAR(30),
      LASTACTTIMESTAMP     TIMESTAMP WITH DEFAULT NULL,
      LASTACTUSERID        CHAR(8) FOR SBCS DATA WITH DEFAULT NULL,
      CONSTRAINT PK__E_VIEWTBL
      PRIMARY KEY (ENVIRONID,
                   VIEWID))
    IN &$DBNAME..VIEWTS
    AUDIT NONE
    DATA CAPTURE NONE
    CCSID      EBCDIC
    NOT VOLATILE;
  COMMIT;
  
  CREATE LOB TABLESPACE FORFLBLB
    IN &$DBNAME.
    USING STOGROUP &$DBSG.
    LOG YES
    DSSIZE 4 G
    BUFFERPOOL BP0
    LOCKSIZE ANY
    LOCKMAX SYSTEM
    CLOSE YES;
  COMMIT;

  CREATE AUXILIARY TABLE &$DBSCH..FORMATFILTLOGIC_LOB
    IN &$DBNAME..FORFLBLB
    STORES &$DBSCH..E_VIEWTBL COLUMN FORMATFILTLOGIC;
  COMMIT;  
  
  CREATE UNIQUE INDEX &$DBSCH..FORMATFILTLOGIC_IDX
  ON &$DBSCH..FORMATFILTLOGIC_LOB;  
  COMMIT;    
