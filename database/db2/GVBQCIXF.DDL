******************************************************************
--
-- (C) COPYRIGHT IBM CORPORATION 2011, 2025.
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
-- Create the E_ indexes for the SAFR metadata with LOGICTBL and E_LOGICTBL_LOB
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

  SET CURRENT SQLID='SAFRBLD';               
  CREATE UNIQUE INDEX SAFRNEIL.E_LOGICTBL    
    ON SAFRNEIL.E_LOGICTBL                   
     (ENVIRONID             ASC,             
      VIEWID                ASC,             
      TYPECD                ASC)             
    USING STOGROUP SASGNEIL                  
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
  CREATE UNIQUE INDEX SAFRNEIL.E_LOGICTBL_LOB
    ON SAFRNEIL.E_LOGICTBL_LOB               
    USING STOGROUP SASGNEIL                  
    PRIQTY 48 SECQTY 720                     
    ERASE  NO                                
    FREEPAGE 0 PCTFREE 10                    
    GBPCACHE CHANGED                         
    BUFFERPOOL BP0                           
    CLOSE YES                                
    COPY NO                                  
    PIECESIZE 4 G;                           
  COMMIT;                                    
