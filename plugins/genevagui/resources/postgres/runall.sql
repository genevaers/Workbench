--****************************************************************
--
--     Copyright Contributors to the GenevaERS Project.
-- SPDX-License-Identifier: Apache-2.0
--
--***********************************************************************
--*                                                                           
--*   Licensed under the Apache License, Version 2.0 (the "License");         
--*   you may not use this file except in compliance with the License.        
--*   You may obtain a copy of the License at                                 
--*                                                                           
--*     http://www.apache.org/licenses/LICENSE-2.0                            
--*                                                                           
--*   Unless required by applicable law or agreed to in writing, software     
--*   distributed under the License is distributed on an "AS IS" BASIS,       
--*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
--*   or implied.
--*   See the License for the specific language governing permissions and     
--*   limitations under the License.                                          
--***********************************************************************

-- Create the database
-- Want a parameter to define the schema name
\i createdatabase.sql

\c :dbname -

\echo Create and Fill the schema

\i genevaERSEmpty.sql

\dn  

\i populateCode.sql

\i populateinitState.sql

\echo Install the stored functions

\i getLRDependencies.sql

\i getMaxIds.sql

\i getVersion.sql

\i getViewDependencies.sql

\i getviewprops.sql

\i insertlrfield.sql

\i insertlrfieldWithId.sql

\i insertColumn.sql

\i insertColumnWithId.sql

\i insertlookupsrcfield.sql

\i insertlookupsrcfieldWithId.sql

\i insertViewColumnSource.sql

\i insertViewColumnSourceWithId.sql

\i insertSecGrpRights.sql

\i newEnvironmentProc.sql

\i updatelrfield.sql

\i updatelrfieldWithId.sql

\i updateViewColumn.sql

\i updateViewColumnWithId.sql

\i updateViewColumnSource.sql

\i updateViewColumnSourceWithId.sql

\i checkEnvironmentDependencies.sql

\i clearEnvironment.sql

\i deleteEnvironment.sql

\i deleteView.sql

\echo All done.