//BLDDB03  JOB (ACCT),'BUILD DB STEP3',
//            NOTIFY=&SYSUID.,
//            CLASS=&$JBCLASS.,
//            MSGLEVEL=(1,1),
//            MSGCLASS=&$JBCLASS.
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
//* create C_*, E_* and X_* indexes
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
//*   BUILD C_ INDEXES                                                 
//*********************************************************************
//BLDCIX  EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -                             
            LIB('&DB2RLIB.')                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIXC)                      
//*                                                                    
//*********************************************************************
//*   BUILD E_ INDEXES                                                 
//*********************************************************************
//BLDEIX  EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -                             
            LIB('&DB2RLIB.')                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIXE)                      
//*                                                                    
//*********************************************************************
//*   BUILD X_INDEXES                                                  
//*********************************************************************
//BLDXIX  EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -                             
            LIB('&DB2RLIB.')                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIXX)
//                                               
