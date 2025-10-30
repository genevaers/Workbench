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
//*   LOAD JOB 1
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
//*********************************************************************
//*   LOAD CODETABL                                                    
//********************************************************************* 
//CODETABL EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..CODETABL.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..CODETABL.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD CONTRLRC
//*********************************************************************
//CONTRLRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..CONTRLRC.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..CONTRLRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD ENVIRON
//********************************************************************* 
//ENVIRON  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ENVIRON.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ENVIRON.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD GLOBAFLD
//********************************************************************* 
//GLOBAFLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..GLOBAFLD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..GLOBAFLD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD GROUPTS
//********************************************************************* 
//GROUPTS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..GROUPTS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..GROUPTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LFPFASS
//*********************************************************************
//LFPFASS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LFPFASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LFPFASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGFILE
//*********************************************************************
//LOGFILE  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOGFILE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOGFILE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOGREC
//*********************************************************************
//LOGREC   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOGREC.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOGREC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGRECIX
//*********************************************************************
//LOGRECIX EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOGRECIX.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOGRECIX.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUP
//*********************************************************************
//LOOKUP   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOOKUP.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOOKUP.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUPSR
//*********************************************************************
//LOOKUPSR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOOKUPSR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOOKUPSR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUPST
//*********************************************************************
//LOOKUPST EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOOKUPST.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LOOKUPST.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFIELD
//*********************************************************************
//LRFIELD  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRFIELD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRFIELD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFLDATR
//*********************************************************************
//LRFLDATR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRFLDATR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRFLDATR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFLDIDX
//*********************************************************************
//LRFLDIDX EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRFLDIDX.DATA,
//         DISP=SHR

//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRFLDIDX.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRLFASS
//*********************************************************************
//LRLFASS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRLFASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..LRLFASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD MIGRAUDT
//*********************************************************************
//MIGRAUDT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..MIGRAUDT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..MIGRAUDT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD NEXTKEY
//*********************************************************************
//NEXTKEY  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..NEXTKEY.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..NEXTKEY.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD PHYSFILE
//*********************************************************************
//PHYSFILE EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..PHYSFILE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..PHYSFILE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPEV                                                    
//*********************************************************************
//SECGRPEV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPEV.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPEV.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPJN
//*********************************************************************
//SECGRPJN EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPJN.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPJN.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPLF
//*********************************************************************
//SECGRPLF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPLF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPLF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPLR
//*********************************************************************
//SECGRPLR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPLR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPLR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPPF                                                    
//*********************************************************************
//SECGRPPF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPPF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPPF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPUE                                                    
//*********************************************************************
//SECGRPUE EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPUE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPUE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPUR
//*********************************************************************
//SECGRPUR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPUR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPUR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVF
//*********************************************************************
//SECGRPVF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPVF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPVF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVW
//*********************************************************************
//SECGRPVW EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPVW.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SECGRPVW.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SERVER
//*********************************************************************
//SERVER   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SERVER.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..SERVER.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USEREXIT
//*********************************************************************
//USEREXIT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..USEREXIT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..USEREXIT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USERTS
//*********************************************************************
//USERTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..USERTS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..USERTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBATCH
//*********************************************************************
//VDPBATCH EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VDPBATCH.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VDPBATCH.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBTCHV
//*********************************************************************
//VDPBTCHV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VDPBTCHV.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VDPBTCHV.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VFVASS
//*********************************************************************
//VFVASS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VFVASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VFVASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWCOL
//*********************************************************************
//VIEWCOL  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWCOL.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWCOL.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWFOLD
//*********************************************************************
//VIEWFOLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWFOLD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWFOLD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWHDFT
//*********************************************************************
//VIEWHDFT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWHDFT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWHDFT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWSRC
//*********************************************************************
//VIEWSRC  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWSRC.DATA2,
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWSRC.PNCH,
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWTS
//*********************************************************************
//VIEWTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWTS.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VIEWTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWCOLSRC
//*********************************************************************
//VWCOLSRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VWCOLSRC.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VWCOLSRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWLOGDEP
//*********************************************************************
//VWLOGDEP EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VWLOGDEP.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VWLOGDEP.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWSORTKY
//*********************************************************************
//VWSORTKY EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VWSORTKY.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..VWSORTKY.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDSMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*
//