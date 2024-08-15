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

CREATE PROCEDURE UPDVWLOGIC (IN P_ENVTID INT,
                             IN DOC CLOB)

RESULT SETS 0
LANGUAGE SQL
DISABLE DEBUG MODE 

BEGIN
  DECLARE PDOC XML;
  SET PDOC = XMLPARSE(DOCUMENT DOC PRESERVE WHITESPACE);

  FOR OP AS SELECT X.LOGTYPE LOGTYPE,X.VIEWID VIEWID,
    X.CELLID CELLID,X.LOGIC LOGIC
    FROM XMLTABLE ('$doc/Root/Operation' passing PDOC as "doc" 
    COLUMNS
     "LOGTYPE"         CHAR(3) PATH 'LOGTYPE',
     "VIEWID"          INT PATH 'VIEWID', 
     "CELLID"          INT PATH 'CELLID',
     "LOGIC"           CLOB(32 M) PATH 'LOGIC') AS X
  DO
    CASE OP.LOGTYPE

    WHEN 'FRF' THEN

      UPDATE VIEW SET FORMATFILTLOGIC=OP.LOGIC
      WHERE ENVIRONID=P_ENVTID
      AND VIEWID=OP.VIEWID;

    WHEN 'ERF' THEN

      UPDATE VIEWSOURCE SET EXTRACTFILTLOGIC=OP.LOGIC
      WHERE ENVIRONID=P_ENVTID
      AND VIEWID=OP.VIEWID
      AND VIEWSOURCEID=OP.CELLID;

    WHEN 'ERO' THEN

      UPDATE VIEWSOURCE SET EXTRACTOUTPUTLOGIC=OP.LOGIC
      WHERE ENVIRONID=P_ENVTID
      AND VIEWID=OP.VIEWID
      AND VIEWSOURCEID=OP.CELLID;
      
    WHEN 'FCC' THEN

      UPDATE VIEWCOLUMN SET FORMATCALCLOGIC=OP.LOGIC
      WHERE ENVIRONID=P_ENVTID
      AND VIEWID=OP.VIEWID 
      AND VIEWCOLUMNID=OP.CELLID;
           
    WHEN 'ECC' THEN

      UPDATE VIEWCOLUMNSOURCE SET EXTRACTCALCLOGIC=OP.LOGIC
      WHERE ENVIRONID=P_ENVTID
      AND VIEWID=OP.VIEWID
      AND VIEWCOLUMNSOURCEID=OP.CELLID;
          
    END CASE;
  
  END FOR;

END