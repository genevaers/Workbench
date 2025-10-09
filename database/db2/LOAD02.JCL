//LOAD02  JOB (ACCT),'LOAD DB STEP12,
//            NOTIFY=&SYSUID.,
//            CLASS=&$JBCLASS.,
//            MSGLEVEL=(1,1),
//            MSGCLASS=&$MSGCLASS.
//*
//********************************************************************
//*
//* (C) COPYRIGHT IBM CORPORATION 2025.
//*    Copyright Contributors to the GenevaERS Project.
//*SPDX-License-Identifier: Apache-2.0
//*
//********************************************************************
//*
//*  Licensed under the Apache License, Version 2.0 (the "License");
//*  you may not use this file except in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*     http://www.apache.org/licenses/LICENSE-2.0
//*
//*  Unless required by applicable law or agreed to in writing, software
//*  distributed under the License is distributed on an "AS IS" BASIS,
//*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
//*  or implied.
//*  See the License for the specific language governing permissions
//*  and limitations under the License.
//*                                                                   
//********************************************************************
//*   LOAD LOGIC
//********************************************************************
//*  
//   EXPORT SYMLIST=*
//*
//   SET HLQ=&$PDSHLQ.
//   SET MLQ=&$PDSMLQ.
//   SET DB2SYS=&$DBSUB.
//   SET DB2PLAN=&$DB2PLAN.
//   SET DB2PLIB=&$DB2PLIB.
//   SET DB2RLIB=&$DB2RLIB.
//*
//PROCLIB  JCLLIB ORDER=(&DB2PLIB.,
//            &HLQ..&MLQ..JCL)
//*
//JOBLIB   INCLUDE MEMBER=@INCLLIB
//*
//LOLOG    EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGIC.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(8000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGIC.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(8000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGIC.DATA2,
           DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGIC.PNCH,
           DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGIC.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(8000,(80,80),,,ROUND)
//