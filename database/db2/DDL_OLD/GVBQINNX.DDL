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
--  Insert rows into C_NEXTKEYID for the SAFR metadata
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
INSERT INTO &$DBSCH..C_NEXTKEYID
SELECT LOWER(NAME),0
FROM SYSIBM.SYSTABLES
WHERE CREATOR = '&$DBSCH.'
AND (   NAME LIKE 'E%'
     OR  NAME LIKE 'C%'
     OR  NAME LIKE 'X%')
       AND NAME not in ('c_environtbl','e_servertbl','c_secgroups',
                        'c_viewfolders','e_partitiontbl','e_filetbl',
                        'x_filepartitiontbl',
                        'C_ENVIRONTBL','E_SERVERTBL','C_SECGROUPS',
                        'C_VIEWFOLDERS','E_PARTITIONTBL','E_FILETBL',
                        'X_FILEPARTITIONTBL');
