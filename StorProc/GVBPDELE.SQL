-- ******************************************************************
--
-- (C) COPYRIGHT IBM CORPORATION 2010, 2023.
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

CREATE PROCEDURE DELETEENVIRON (IN P_ENVIRONID INT)

DYNAMIC RESULT SETS 0
LANGUAGE SQL
DISABLE DEBUG MODE

BEGIN

--**************************************************************
--DELETES THE PHYSICAL FILE AND LOGICAL FILE ASSOCIATIONS
-- RELATED TO THE ENVIRONMENT
--**************************************************************

   DELETE FROM LFPFASSOC WHERE ENVIRONID = P_ENVIRONID;

--**************************************************************
--DELETES THE LOGICAL FILES RELATED TO THE ENVIRONMENT
--**************************************************************

    DELETE FROM LOGFILE WHERE ENVIRONID = P_ENVIRONID;

--**************************************************************
--DELETES THE PHYSICAL FILES AND THEIR ASSOCIATIONS RELATED TO
--THE ENVIRONMENT
--**************************************************************

    DELETE FROM PHYFILE WHERE ENVIRONID = P_ENVIRONID;

--**************************************************************
--DELETES THE VIEW FOLDERS RELATED TO THE ENVIRONMENT
--**************************************************************

    DELETE FROM VIEWFOLDER WHERE ENVIRONID = P_ENVIRONID;

--**************************************************************
--DELETES THE CONTROL RECORDS RELATED TO THE ENVIRONMENT
--**************************************************************

    DELETE FROM CONTROLREC WHERE ENVIRONID = P_ENVIRONID;

--**************************************************************
--DELETES THE ASSOCIATIONS OF THE ENVIRONMENT WITH ANY GROUP
--**************************************************************

    DELETE FROM SECENVIRON WHERE ENVIRONID = P_ENVIRONID;

--**************************************************************
--DELETE THE ENVIRONMENT
--**************************************************************

   DELETE FROM ENVIRON WHERE ENVIRONID = P_ENVIRONID;

END
