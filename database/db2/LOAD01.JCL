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
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..CODETABL.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..CODETABL.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD CONTRLRC
//*********************************************************************
//CONTRLRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..CONTRLRC.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..CONTRLRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD ENVIRON
//********************************************************************* 
//ENVIRON  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ENVIRON.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ENVIRON.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD GLOBAFLD
//********************************************************************* 
//GLOBAFLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..GLOBAFLD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..GLOBAFLD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD GROUPTS
//********************************************************************* 
//GROUPTS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..GROUPTS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..GROUPTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LFPFASS
//*********************************************************************
//LFPFASS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LFPFASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LFPFASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGFILE
//*********************************************************************
//LOGFILE  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGFILE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGFILE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOGREC
//*********************************************************************
//LOGREC   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGREC.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGREC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGRECIX
//*********************************************************************
//LOGRECIX EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGRECIX.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOGRECIX.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUP
//*********************************************************************
//LOOKUP   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOOKUP.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOOKUP.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUPSR
//*********************************************************************
//LOOKUPSR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOOKUPSR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOOKUPSR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUPST
//*********************************************************************
//LOOKUPST EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOOKUPST.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LOOKUPST.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFIELD
//*********************************************************************
//LRFIELD  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRFIELD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRFIELD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFLDATR
//*********************************************************************
//LRFLDATR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRFLDATR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRFLDATR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFLDIDX
//*********************************************************************
//LRFLDIDX EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRFLDIDX.DATA,
//         DISP=SHR

//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRFLDIDX.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRLFASS
//*********************************************************************
//LRLFASS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRLFASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..LRLFASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD MIGRAUDT
//*********************************************************************
//MIGRAUDT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..MIGRAUDT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..MIGRAUDT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD NEXTKEY
//*********************************************************************
//NEXTKEY  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..NEXTKEY.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..NEXTKEY.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD PHYSFILE
//*********************************************************************
//PHYSFILE EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..PHYSFILE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..PHYSFILE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPEV                                                    
//*********************************************************************
//SECGRPEV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPEV.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPEV.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPJN
//*********************************************************************
//SECGRPJN EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPJN.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPJN.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPLF
//*********************************************************************
//SECGRPLF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPLF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPLF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPLR
//*********************************************************************
//SECGRPLR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPLR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPLR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPPF                                                    
//*********************************************************************
//SECGRPPF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPPF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPPF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPUE                                                    
//*********************************************************************
//SECGRPUE EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPUE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPUE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPUR
//*********************************************************************
//SECGRPUR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPUR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPUR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVF
//*********************************************************************
//SECGRPVF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPVF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPVF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVW
//*********************************************************************
//SECGRPVW EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPVW.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SECGRPVW.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SERVER
//*********************************************************************
//SERVER   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SERVER.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..SERVER.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USEREXIT
//*********************************************************************
//USEREXIT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..USEREXIT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..USEREXIT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USERTS
//*********************************************************************
//USERTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..USERTS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..USERTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBATCH
//*********************************************************************
//VDPBATCH EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VDPBATCH.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VDPBATCH.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBTCHV
//*********************************************************************
//VDPBTCHV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VDPBTCHV.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VDPBTCHV.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VFVASS
//*********************************************************************
//VFVASS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VFVASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VFVASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWCOL
//*********************************************************************
//VIEWCOL  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWCOL.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWCOL.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWFOLD
//*********************************************************************
//VIEWFOLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWFOLD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWFOLD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWHDFT
//*********************************************************************
//VIEWHDFT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWHDFT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWHDFT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWSRC
//*********************************************************************
//VIEWSRC  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWSRC.DATA2,
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWSRC.PNCH,
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWTS
//*********************************************************************
//VIEWTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWTS.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VIEWTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWCOLSRC
//*********************************************************************
//VWCOLSRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VWCOLSRC.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VWCOLSRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWLOGDEP
//*********************************************************************
//VWLOGDEP EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VWLOGDEP.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VWLOGDEP.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWSORTKY
//*********************************************************************
//VWSORTKY EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VWSORTKY.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..VWSORTKY.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&$FROMPDSHLQ..&$FROMPDMHLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*
//