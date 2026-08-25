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
-- Create the database and tablespaces for the
-- the SAFR metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL &$DBSCH. SAFRTEST'
--
-- 1) Replace occurrences of DB2 subsystem &$DBSUB.
-- 2) Replace user id with DBA access user id &$DBUSER.
-- 3) Replace occurrences of storage group &$DBSG.
-- 4) Replace occurrences of database &$DBNAME.
-- 5) Replace occurrences of schema &$DBSCH.
--
SET CURRENT SQLID='&$DBUSER.';

CREATE STOGROUP &$DBSG.
  VOLUMES("*")
  VCAT &$DBSUB. ;
COMMIT;

  CREATE DATABASE &$DBNAME.
    BUFFERPOOL BP0
    INDEXBP    BP0
    CCSID      EBCDIC
    STOGROUP   &$DBSG.;

    GRANT BINDADD TO &$DBSCH.;
    GRANT CREATE IN COLLECTION &$DBSCH. TO &$DBSCH.;

    COMMIT;
