//BLDDB05  JOB (ACCT),'BUILD DB STEP5',                                
//            NOTIFY=&SYSUID.,                                         
//            CLASS=A,                                                 
//            MSGLEVEL=(1,1),                                          
//            MSGCLASS=H                                               
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
//* load CODETABL and the other table
//********************************************************************
//*  
//   EXPORT SYMLIST=*                                                  
//*                                                                    
//   SET HLQ='GEBT'                                                    
//   SET MLQ='RTC23321'                                                
//   SET DB2SYS=DM13
//   SET DB2PLAN=DSNTEP13
//   SET DB2PLIB=DSN.V13R1M0.PROCLIB
//   SET DB2RLIB=DSN131.RUNLIB.LOAD
//*                                                                    
//PROCLIB  JCLLIB ORDER=(&DB2PLIB.,                                     
//            &HLQ..&MLQ..JCL)                                       
//*                                                                    
//JOBLIB   INCLUDE MEMBER=@INCLLIB                                     
//*                                                                    
//*********************************************************************
//*   LOAD CODE TABLE                                                  
//*********************************************************************
//RUNTEP1 EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -                             
            LIB(&DB2RLIB)                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINCO)                      
//*                                                                    
//*********************************************************************
//*   LOAD OTHER TABLES                                                
//*********************************************************************
//RUNTEP2 EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -                             
            LIB(&DB2RLIB)                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINTB)                      
//*                                                                    
//*********************************************************************
//*   LOAD C_NEXTKEYID                                                 
//*********************************************************************
//RUNTEP3 EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -                             
            LIB(&DB2RLIB)                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINNX)
//*                                              
//