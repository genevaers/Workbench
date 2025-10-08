//LOAD01  JOB (ACCT),'LOAD DB STEP1',
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
//*   LOAD CODETABL
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
//CODETABL EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.CODETABL.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.CODETABL.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD CONTRLRC
//*********************************************************************
//CONTRLRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//