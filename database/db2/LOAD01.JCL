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
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SYSREC   DD DSN=GEBT.GENERS.D240708U.CONTRLRC.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.CONTRLRC.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//********************************************************************* 
//*   LOAD ENVIRON                                                      
//********************************************************************* 
//ENVIRON  EXEC DSNUPROC,UID=&SYSUID,                                   
//         SYSTEM=&DB2SYS                                               
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                     
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SYSREC   DD DSN=GEBT.GENERS.D240708U.ENVIRON.DATA,DISP=SHR            
//SYSIN    DD DSN=GEBT.GENERS.D240708U.ENVIRON.PNCH,DISP=SHR            
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//********************************************************************* 
//*   LOAD GLOBAFLD                                                     
//********************************************************************* 
//GLOBAFLD EXEC DSNUPROC,UID=&SYSUID,                                   
//         SYSTEM=&DB2SYS                                               
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                     
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SYSREC   DD DSN=GEBT.GENERS.D240708U.GLOBAFLD.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.GLOBAFLD.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//********************************************************************* 
//*   LOAD GROUPTS                                                      
//********************************************************************* 
//GROUPTS  EXEC DSNUPROC,UID=&SYSUID,                                   
//         SYSTEM=&DB2SYS                                               
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                     
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//SYSREC   DD DSN=GEBT.GENERS.D240708U.GROUPTS.DATA,DISP=SHR            
//SYSIN    DD DSN=GEBT.GENERS.D240708U.GROUPTS.PNCH,DISP=SHR            
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                      
//         DISP=(MOD,DELETE,CATLG),                                     
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                      
//********************************************************************* 
//*   LOAD LFPFASS                                                      
//********************************************************************* 
//LFPFASS  EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LFPFASS.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LFPFASS.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGFILE                                                     
//*********************************************************************
//LOGFILE  EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LOGFILE.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LOGFILE.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGREC                                                      
//*********************************************************************
//LOGREC   EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LOGREC.DATA,DISP=SHR            
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LOGREC.PNCH,DISP=SHR            
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOGRECIX                                                    
//*********************************************************************
//LOGRECIX EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LOGRECIX.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LOGRECIX.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOOKUP                                                      
//*********************************************************************
//LOOKUP   EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LOOKUP.DATA,DISP=SHR            
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LOOKUP.PNCH,DISP=SHR            
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOOKUPSR                                                    
//*********************************************************************
//LOOKUPSR EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LOOKUPSR.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LOOKUPSR.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LOOKUPST                                                    
//*********************************************************************
//LOOKUPST EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LOOKUPST.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LOOKUPST.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LRFIELD                                                     
//*********************************************************************
//LRFIELD  EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LRFIELD.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LRFIELD.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LRFLDATR                                                    
//*********************************************************************
//LRFLDATR EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LRFLDATR.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LRFLDATR.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LRFLDIDX                                                    
//*********************************************************************
//LRFLDIDX EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LRFLDIDX.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LRFLDIDX.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD LRLFASS                                                     
//*********************************************************************
//LRLFASS  EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.LRLFASS.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.LRLFASS.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD MIGRAUDT                                                    
//*********************************************************************
//MIGRAUDT EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.MIGRAUDT.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.MIGRAUDT.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD NEXTKEY                                                     
//*********************************************************************
//NEXTKEY  EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.NEXTKEY.DATA,DISP=SHR           
//SYSIN    DD DSN=GEBT.GENERS.D240708U.NEXTKEY.PNCH,DISP=SHR           
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD PHYSFILE                                                    
//*********************************************************************
//PHYSFILE EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.PHYSFILE.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.PHYSFILE.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPEV                                                    
//*********************************************************************
//SECGRPEV EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPEV.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPEV.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPJN                                                    
//*********************************************************************
//SECGRPJN EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPJN.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPJN.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPLF                                                    
//*********************************************************************
//SECGRPLF EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPLF.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPLF.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPLR                                                    
//*********************************************************************
//SECGRPLR EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPLR.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPLR.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPPF                                                    
//*********************************************************************
//SECGRPPF EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPPF.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPPF.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPUE                                                    
//*********************************************************************
//SECGRPUE EXEC DSNUPROC,UID=&SYSUID,                                  
//         SYSTEM=&DB2SYS                                              
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,                    
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPUE.DATA,DISP=SHR          
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPUE.PNCH,DISP=SHR          
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,                     
//         DISP=(MOD,DELETE,CATLG),                                    
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)                     
//*********************************************************************
//*   LOAD SECGRPUR
//*********************************************************************
//SECGRPUR EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPUR.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPUR.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVF
//*********************************************************************
//SECGRPVF EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPVF.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPVF.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SECGRPVW
//*********************************************************************
//SECGRPVW EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SECGRPVW.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SECGRPVW.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD SERVER
//*********************************************************************
//SERVER   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.SERVER.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.SERVER.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USEREXIT
//*********************************************************************
//USEREXIT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.USEREXIT.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.USEREXIT.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD USERTS
//*********************************************************************
//USERTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.USERTS.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.USERTS.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBATCH
//*********************************************************************
//VDPBATCH EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VDPBATCH.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VDPBATCH.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VDPBTCHV
//*********************************************************************
//VDPBTCHV EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VDPBTCHV.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VDPBTCHV.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VFVASS
//*********************************************************************
//VFVASS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VFVASS.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VFVASS.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWCOL
//*********************************************************************
//VIEWCOL  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VIEWCOL.DATA2,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VIEWCOL.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWFOLD
//*********************************************************************
//VIEWFOLD EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VIEWFOLD.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VIEWFOLD.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWHDFT
//*********************************************************************
//VIEWHDFT EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VIEWHDFT.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VIEWHDFT.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWSRC
//*********************************************************************
//VIEWSRC  EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VIEWSRC.DATA2,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VIEWSRC.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VIEWTS
//*********************************************************************
//VIEWTS   EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VIEWTS.DATA2,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VIEWTS.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWCOLSRC
//*********************************************************************
//VWCOLSRC EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VWCOLSRC.DATA2,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VWCOLSRC.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWLOGDEP
//*********************************************************************
//VWLOGDEP EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VWLOGDEP.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VWLOGDEP.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*********************************************************************
//*   LOAD VWSORTKY
//*********************************************************************
//VWSORTKY EXEC DSNUPROC,UID=&SYSUID,
//         SYSTEM=&DB2SYS
//SYSUT1   DD DSN=GEBT.GENERS.D240708U.ALL.SYSUT1,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SORTOUT  DD DSN=GEBT.GENERS.D240708U.ALL.SORTOUT,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//SYSREC   DD DSN=GEBT.GENERS.D240708U.VWSORTKY.DATA,DISP=SHR
//SYSIN    DD DSN=GEBT.GENERS.D240708U.VWSORTKY.PNCH,DISP=SHR
//SYSMAP   DD DSN=GEBT.GENERS.D240708U.ALL.SYSMAP,
//         DISP=(MOD,DELETE,CATLG),
//         UNIT=SYSDA,SPACE=(4000,(80,80),,,ROUND)
//*
//