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
delete from :schemaV.viewcolumnsource where environid = 1;
delete from :schemaV.viewsource where environid = 1;
delete from :schemaV.lrlfassoc where environid = 1;
delete from :schemaV.lfpfassoc where environid = 1;
delete from :schemaV.lrfieldattr where environid = 1;
delete from :schemaV.lrfield where environid = 1;
delete from :schemaV.logrec where environid = 1;
delete from :schemaV.logfile where environid = 1;
delete from :schemaV.phyfile where environid = 1;
delete from :schemaV.viewsource where environid = 1;
delete from :schemaV.viewcolumn where environid = 1;
delete from :schemaV.vfvassoc where environid = 1;
delete from :schemaV.view where environid = 1;
