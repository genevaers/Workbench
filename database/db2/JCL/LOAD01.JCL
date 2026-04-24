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
//   SET LOADHLQ=&$LOADHLQ.
//   SET LOADMLQ=&$LOADMLQ.
//   SET DB2SYS=&$DBSUB.
//   SET DB2PLIB=&$DB2PLIB.
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
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..CODETABL.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..CODETABL.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD CONTRLRC
//*********************************************************************
//CONTRLRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..CONTRLRC.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..CONTRLRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD ENVIRON
//********************************************************************* 
//ENVIRON  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..ENVIRON.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..ENVIRON.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD GLOBAFLD
//********************************************************************* 
//GLOBAFLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..GLOBAFLD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..GLOBAFLD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//********************************************************************* 
//*   LOAD GROUPTS
//********************************************************************* 
//GROUPTS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..GROUPTS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..GROUPTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LFPFASS
//*********************************************************************
//LFPFASS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LFPFASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LFPFASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGFILE
//*********************************************************************
//LOGFILE  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LOGFILE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LOGFILE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOGREC
//*********************************************************************
//LOGREC   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LOGREC.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LOGREC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGRECIX
//*********************************************************************
//LOGRECIX EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LOGRECIX.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LOGRECIX.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUP
//*********************************************************************
//LOOKUP   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LOOKUP.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LOOKUP.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUPSR
//*********************************************************************
//LOOKUPSR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LOOKUPSR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LOOKUPSR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LOOKUPST
//*********************************************************************
//LOOKUPST EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LOOKUPST.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LOOKUPST.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFIELD
//*********************************************************************
//LRFIELD  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LRFIELD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LRFIELD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFLDATR
//*********************************************************************
//LRFLDATR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LRFLDATR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LRFLDATR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRFLDIDX
//*********************************************************************
//LRFLDIDX EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LRFLDIDX.DATA,
//         DISP=SHR

//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LRFLDIDX.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD LRLFASS
//*********************************************************************
//LRLFASS  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..LRLFASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..LRLFASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD MIGRAUDT
//*********************************************************************
//MIGRAUDT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..MIGRAUDT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..MIGRAUDT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD NEXTKEY
//*********************************************************************
//NEXTKEY  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..NEXTKEY.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..NEXTKEY.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD PHYSFILE
//*********************************************************************
//PHYSFILE EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..PHYSFILE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..PHYSFILE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPEV                                                    
//*********************************************************************
//SECGRPEV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPEV.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPEV.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPJN
//*********************************************************************
//SECGRPJN EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPJN.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPJN.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPLF
//*********************************************************************
//SECGRPLF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPLF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPLF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPLR
//*********************************************************************
//SECGRPLR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPLR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPLR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPPF                                                    
//*********************************************************************
//SECGRPPF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPPF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPPF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPUE                                                    
//*********************************************************************
//SECGRPUE EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPUE.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPUE.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPUR
//*********************************************************************
//SECGRPUR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPUR.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPUR.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVF
//*********************************************************************
//SECGRPVF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPVF.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPVF.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVW
//*********************************************************************
//SECGRPVW EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SECGRPVW.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SECGRPVW.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SERVER
//*********************************************************************
//SERVER   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..SERVER.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..SERVER.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USEREXIT
//*********************************************************************
//USEREXIT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..USEREXIT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..USEREXIT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USERTS
//*********************************************************************
//USERTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..USERTS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..USERTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBATCH
//*********************************************************************
//VDPBATCH EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VDPBATCH.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VDPBATCH.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBTCHV
//*********************************************************************
//VDPBTCHV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VDPBTCHV.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VDPBTCHV.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VFVASS
//*********************************************************************
//VFVASS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VFVASS.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VFVASS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWCOL
//*********************************************************************
//VIEWCOL  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VIEWCOL.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VIEWCOL.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWFOLD
//*********************************************************************
//VIEWFOLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VIEWFOLD.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VIEWFOLD.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWHDFT
//*********************************************************************
//VIEWHDFT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VIEWHDFT.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VIEWHDFT.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWSRC
//*********************************************************************
//VIEWSRC  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VIEWSRC.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VIEWSRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWTS
//*********************************************************************
//VIEWTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VIEWTS.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VIEWTS.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWCOLSRC
//*********************************************************************
//VWCOLSRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VWCOLSRC.DATA2,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VWCOLSRC.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWLOGDEP
//*********************************************************************
//VWLOGDEP EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VWLOGDEP.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VWLOGDEP.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWSORTKY
//*********************************************************************
//VWSORTKY EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=&LOADHLQ..&LOADMLQ..ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=&LOADHLQ..&LOADMLQ..VWSORTKY.DATA,
//         DISP=SHR
//SYSIN    DD DSN=&LOADHLQ..&LOADMLQ..VWSORTKY.PNCH,
//         DISP=SHR
//SYSMAP   DD DSN=&LOADHLQ..&LOADMLQ..ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*
//