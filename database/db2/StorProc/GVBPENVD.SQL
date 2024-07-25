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

CREATE PROCEDURE ENVDEPENDENCY (IN P_ENVIRONID INT,
                                 OUT P_DEPENDENCIES_FLAG INT)

DYNAMIC RESULT SETS 0
LANGUAGE SQL
DISABLE DEBUG MODE 

BEGIN

--***************************************************************
--IF ANY COMPONENT IS FOUND SET THE FLAG FOR DEPENDENCIES
-- TO 1 ELSE ITS 0
--***************************************************************

IF EXISTS (SELECT * FROM CONTROLREC
                  WHERE ENVIRONID = P_ENVIRONID )
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM PHYFILE
                  WHERE ENVIRONID = P_ENVIRONID AND PHYFILEID > 0)
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM LOGFILE
                  WHERE ENVIRONID = P_ENVIRONID AND LOGFILEID > 0)
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM LOGREC
                  WHERE ENVIRONID = P_ENVIRONID)
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM LOOKUP
                  WHERE ENVIRONID = P_ENVIRONID)
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM VIEWFOLDER
                 WHERE ENVIRONID = P_ENVIRONID
                 AND VIEWFOLDERID <> 0)
                 THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM VIEW
                  WHERE ENVIRONID = P_ENVIRONID)
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE IF EXISTS (SELECT * FROM EXIT
                  WHERE ENVIRONID = P_ENVIRONID )
                  THEN SET P_DEPENDENCIES_FLAG = 1;

ELSE SET P_DEPENDENCIES_FLAG = 0;

END IF;
END IF;
END IF;
END IF;
END IF;
END IF;
END IF;
END IF;

END