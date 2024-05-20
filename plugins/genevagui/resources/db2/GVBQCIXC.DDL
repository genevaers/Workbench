-- ******************************************************************
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
-- Create the C_ indexes for the SAFR metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL &$DBSCH. SAFRTEST'
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of storage group &$DBSG.
-- 3) Replace occurrences of schema &$DBSCH.
--
SET CURRENT SQLID='&$DBUSER.';
  CREATE UNIQUE INDEX &$DBSCH..IX_VIEWFOLDERS
    ON &$DBSCH..C_VIEWFOLDERS
     (ENVIRONID             ASC,
      VIEWFOLDERID          ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
---------DDL CREATE INDEX-----------------------------------------------
------------------------------------------------------------------------
  CREATE UNIQUE INDEX &$DBSCH..CODETABLE
    ON &$DBSCH..CODETABLE
     (SRCID                 ASC,
      KEYID                 ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE INDEX &$DBSCH..VDPBLD08
    ON &$DBSCH..CODETABLE
     (SRCID                 ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE UNIQUE INDEX &$DBSCH..IDX_CONTROLREC
    ON &$DBSCH..C_CONTROLREC
     (ENVIRONID             ASC,
      CONTROLID             ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE UNIQUE INDEX &$DBSCH..C_CONTROLREC
    ON &$DBSCH..C_CONTROLREC
     (ENVIRONID             ASC,
      CONTROLID             ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE UNIQUE INDEX &$DBSCH..C_ENVIRONTBL
    ON &$DBSCH..C_ENVIRONTBL
     (ENVIRONID             ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE UNIQUE INDEX &$DBSCH..C_NEXTKEYID
    ON &$DBSCH..C_NEXTKEYID
     (TABLENAME             ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE UNIQUE INDEX &$DBSCH..C_SECGROUPS
    ON &$DBSCH..C_SECGROUPS
     (SECGROUPID            ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
  CREATE UNIQUE INDEX &$DBSCH..C_USERTBL
    ON &$DBSCH..C_USERTBL
     (USERID                ASC)
    USING STOGROUP &$DBSG.
    PRIQTY 48 SECQTY 720
    ERASE  NO
    FREEPAGE 0 PCTFREE 10
    GBPCACHE CHANGED
    NOT CLUSTER
    BUFFERPOOL BP0
    CLOSE YES
    COPY NO
    PIECESIZE 2 G;
  COMMIT;
